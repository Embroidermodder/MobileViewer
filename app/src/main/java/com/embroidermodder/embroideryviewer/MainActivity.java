package com.embroidermodder.embroideryviewer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity implements Pattern.Provider {
    private int SELECT_FILE = 1;
    private Intent _intent;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Pattern p = null;
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)
                || Intent.ACTION_EDIT.equals(action)) {
            try {
                Uri returnUri = intent.getData();
                if (returnUri == null) {
                    Object object = intent.getExtras().get(Intent.EXTRA_STREAM);
                    if (object instanceof Uri) {
                        returnUri = (Uri) object;
                    }
                }
                if (returnUri == null) {
                    Toast.makeText(this,R.string.error_uri_not_retrieved,Toast.LENGTH_LONG).show();
                }
                else {
                    p = ReadFromUri(returnUri);
                    if (p == null) {
                        Toast.makeText(this, R.string.error_file_read_failed, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (FileNotFoundException ex) {
                Toast.makeText(this,R.string.error_file_not_found,Toast.LENGTH_LONG).show();
            }
        }

        if (p == null) p = new Pattern();
        drawView = (DrawView) findViewById(R.id.drawview);
        drawView.initWindowSize();
        setPattern(p);
    }
    
    @Override
    public void onBackPressed() {
        if (tryCloseFragment(ColorStitchBlockFragment.TAG)) {
            return;
        }
        super.onBackPressed();
    }

    public void setPattern(Pattern pattern) {
        drawView.setPattern(pattern);
        drawView.invalidate();
        openColorFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null != this._intent) {
            outState.putParcelable("intent", this._intent);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (null != savedInstanceState && savedInstanceState.containsKey("intent")) {
            Intent intent = (Intent) savedInstanceState.getParcelable("intent");
            onSelectFileResult(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_open_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                intent.setDataAndType(uri, "*/*");
                startActivityForResult(Intent.createChooser(intent, "Open folder"), SELECT_FILE);
                return true;
            case R.id.action_draw_mode:
                drawView.setTool(new ToolDraw());
                return true;
            case R.id.action_pan_mode:
                drawView.setTool(new ToolPan());
                return true;
            case R.id.action_show_statistics:
                showStatistics();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case "ReadDownloadsFolder":
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //code for granted
//                } else {
//                    //code for denied
//                }
//                return;
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFileResult(data);
            }
        }
    }

    private void onSelectFileResult(Intent data) {
        try {
            this._intent = data;
            Uri uri = data.getData();
            Pattern p = ReadFromUri(uri);
            if (p == null) p = new Pattern(); //read failed.
            setPattern(p);
        } catch (FileNotFoundException ex) {
        }
    }


    public void openColorFragment() {
        tryCloseFragment(ColorStitchBlockFragment.TAG);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ColorStitchBlockFragment fragment = new ColorStitchBlockFragment();
        transaction.add(R.id.mainActivity, fragment, ColorStitchBlockFragment.TAG);
        transaction.commit();
        drawView.getPattern().addListener(fragment);
    }

    public boolean tryCloseFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragmentByTag;
        fragmentByTag = fragmentManager.findFragmentByTag(tag);

        if (fragmentByTag == null) return false;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragmentByTag);
        transaction.commit();
        drawView.getPattern().removeListener(fragmentByTag);
        return true;
    }

    @Override
    public Pattern getPattern() {
        return drawView.getPattern();
    }

    public void showStatistics() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(drawView.getStatistics());
        builder.show();
    }

    private Pattern ReadFromUri(Uri uri) throws FileNotFoundException {
        IFormat.Reader formatReader = IFormat.getReaderByFilename(uri.getPath());
        if (formatReader == null) {
            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String filename = returnCursor.getString(nameIndex);
                formatReader = IFormat.getReaderByFilename(filename);
            }
        }
        if (formatReader == null) {
            return null;
        }
        ParcelFileDescriptor mInputPFD;
        mInputPFD = getContentResolver().openFileDescriptor(uri, "r");
        if (mInputPFD != null) {
            FileDescriptor fd = mInputPFD.getFileDescriptor();
            FileInputStream fis = new FileInputStream(fd);
            DataInputStream in = new DataInputStream(fis);
            Pattern p = formatReader.read(in);
            return p;
        }
        return null;
    }

    private boolean unpackZip(String path, String zipname) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(path + filename);
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
                zis.closeEntry();
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}