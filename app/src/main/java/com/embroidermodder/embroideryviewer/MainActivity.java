package com.embroidermodder.embroideryviewer;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity implements EmbPattern.Provider {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String AUTHORITY = "com.embroidermodder.embroideryviewer";
    String fragmentTag;
    private final int SELECT_FILE = 1;
    private Intent _intent;
    private DrawView drawView;
    private DrawerLayout mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        EmbPattern p = null;
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)
                || Intent.ACTION_EDIT.equals(action)) {
            //try {
                Uri returnUri = intent.getData();
                if (returnUri == null) {
                    Object object = intent.getExtras().get(Intent.EXTRA_STREAM);
                    if (object instanceof Uri) {
                        returnUri = (Uri) object;
                    }
                }
                if (returnUri == null) {
                    Toast.makeText(this, R.string.error_uri_not_retrieved, Toast.LENGTH_LONG).show();
                } else {
                    ReadFromUri(returnUri);
                    //if (p == null) {
                    //    Toast.makeText(this, R.string.error_file_read_failed, Toast.LENGTH_LONG).show();
                    //}
                }
            //} catch (FileNotFoundException ex) {
            //    Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_LONG).show();
            //}
        }
        //if (p == null) p = new EmbPattern();

        mainActivity = (DrawerLayout) findViewById(R.id.mainActivity);
        drawView = (DrawView) findViewById(R.id.drawview);
        drawView.initWindowSize();

        //setPattern(p);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainActivity, toolbar, R.string.app_name, R.string.app_name);
        mainActivity.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (mainActivity.isDrawerOpen(GravityCompat.START)) {
            mainActivity.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            Intent intent = savedInstanceState.getParcelable("intent");
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
            case R.id.action_share:
                //saveFileWrapper(getFilesDir());
                saveFileWrapper(Environment.getExternalStorageDirectory());
                // ContextComapgetUriForFile(getContext(),
                //Context.grantUriPermission(package, Uri,  FLAG_GRANT_READ_URI_PERMISSION);
//                Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
//                shareIntent.setType("image/jpeg");
//                shareIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
//                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
                break;
            case R.id.action_load_file:
                dialogDismiss();
                makeDialog(R.layout.embroidery_thumbnail_view);
                ListView list = (ListView) dialogView.findViewById(R.id.embroideryThumbnailList);
                File mPath = new File(Environment.getExternalStorageDirectory() + "");
                list.setAdapter(new ThumbnailAdapter(this, mPath));

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveFileWrapper(File root) {
        int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessageOKCancel(getString(R.string.external_storage_justification),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        saveFile(root);
//        Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                shareIntent.setType("application/exp");
//                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.action_open_file)));
    }

    private void saveFile(File root) {
        try {
            int n = 10000;
            Random generator = new Random();
            n = generator.nextInt(n);
            String fname = "Image-" + n + ".pec";
            IFormat.Writer format = IFormat.getWriterByFilename(fname);
            if (format != null) {
                File file = new File(root, fname);
                //Uri returnUri = FileProvider.getUriForFile(this,AUTHORITY, file);
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream outputStream = new FileOutputStream(file);
                format.write(drawView.getPattern(), outputStream);
                outputStream.flush();
                outputStream.close();
            }
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Dialog dialog;
    View dialogView;

    public boolean dialogDismiss() {
        if ((dialog != null) && (dialog.isShowing())) {
            dialog.dismiss();
            return true;
        }
        return false;
    }


    public Dialog makeDialog(int layout) {
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(layout, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        if (isFinishing()) {
            finish();
            startActivity(getIntent());
        } else {
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            return dialog;
        }
        return null;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveFile(getFilesDir());
                } else {
                    Toast.makeText(MainActivity.this, R.string.write_permissions_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

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
        this._intent = data;
        Uri uri = data.getData();
        if (uri != null) {
            ReadFromUri(uri);
        }
    }

    public void useColorFragment() {
        if (fragmentTag != null) {
            tryCloseFragment(fragmentTag);
        }
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ColorStitchBlockFragment fragment = new ColorStitchBlockFragment();
        fragmentTag = ColorStitchBlockFragment.TAG;
        transaction.add(R.id.drawerContent, fragment, ColorStitchBlockFragment.TAG);
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

    public void invalidateOnMainThread() {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            drawView.invalidate();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invalidateOnMainThread();
                }
            });
        }
    }

    @Override
    public EmbPattern getPattern() {
        if (drawView == null) return null;
        return drawView.getPattern();
    }

    public void setPattern(EmbPattern pattern) {
        drawView.setPattern(pattern);
        invalidateOnMainThread();
        useColorFragment();
    }

    public void showStatistics() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(drawView.getStatistics());
        builder.show();
    }

    private void ReadFromUri(final Uri uri) {
        IFormat.Reader formatReader = IFormat.getReaderByFilename(uri.getPath());
        if (formatReader == null) {
            Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null) {
                try {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String filename = returnCursor.getString(nameIndex);
                    formatReader = IFormat.getReaderByFilename(filename);
                } finally {
                    returnCursor.close();
                }
            }
        }
        if (formatReader == null) {
            Toast.makeText(this, R.string.error_file_read_failed, Toast.LENGTH_LONG).show();
            return;
        }

        if (("http".equalsIgnoreCase(uri.getScheme()) || ("https".equalsIgnoreCase(uri.getScheme())))) {
            final IFormat.Reader finalFormatReader = formatReader;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream in = getURLStream(uri.toString());
                    if (in == null) return;
                    DataInputStream din = new DataInputStream(in);
                    setPattern(finalFormatReader.read(din));
                }
            });
            thread.start();
        }
        else if ("content".equalsIgnoreCase(uri.getScheme()) || "file".equalsIgnoreCase(uri.getScheme())) {
            final IFormat.Reader finalFormatReader1 = formatReader;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ParcelFileDescriptor mInputPFD;
                    try {
                        mInputPFD = getContentResolver().openFileDescriptor(uri, "r");
                        if (mInputPFD != null) {
                            FileDescriptor fd = mInputPFD.getFileDescriptor();
                            InputStream fis = new FileInputStream(fd);
                            DataInputStream in = new DataInputStream(fis);
                            setPattern(finalFormatReader1.read(in));
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                }
            });
            thread.start();
        }
    }

    public static InputStream getURLStream(String uri) {
        HttpURLConnection connection;
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return null;
        }
        connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            return in;
        } catch (IOException e) {
            return null;
        } finally {
            //if (connection != null) connection.disconnect();
        }
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