package com.embroidermodder.embroideryviewer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private int SELECT_FILE = 1;
    private Intent _intent;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Pattern p = new Pattern();
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
                    //URL could not be fetched from either data or stream.
                    return;
                }
                p = ReadFromUri(returnUri);

            } catch (FileNotFoundException ex) {

            }
        }
        if (drawView == null) {
            drawView = new DrawView(this, p);
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainContentArea);
            relativeLayout.addView(drawView);
        }
        else {
            drawView.setPattern(p);
            drawView.invalidate();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                    intent.setDataAndType(uri, "*/*");
                    startActivityForResult(Intent.createChooser(intent, "Open folder"), SELECT_FILE);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        if(null != this._intent) {
            outState.putParcelable("intent", this._intent);
        }
    }

    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState) {
        if(null != savedInstanceState && savedInstanceState.containsKey("intent")) {
            Intent intent = (Intent)savedInstanceState.getParcelable("intent");
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

    public void showStatistics() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(drawView.getStatistics());
        builder.show();
    }

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if(userChoosenTask.equals("Open folder"))
//                        cameraIntent();
//                } else {
//                    //code for deny
//                }
//                break;
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
                drawView.setPattern(p);
                drawView.invalidate();
        } catch (FileNotFoundException ex) {
        }
    }

    private Pattern ReadFromUri(Uri uri) throws FileNotFoundException {

        IFormatReader formatReader = Pattern.getReaderByFilename(uri.getPath());
        if (formatReader == null) {
            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String filename = returnCursor.getString(nameIndex);
            formatReader = Pattern.getReaderByFilename(filename);
        }
        if (formatReader == null) {
            return new Pattern();
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
}
