package com.embroidermodder.embroideryviewer;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
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
import android.widget.GridView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 100;
    final private int REQUEST_CODE_ASK_PERMISSIONS_LOAD = 101;
    final private int REQUEST_CODE_ASK_PERMISSIONS_READ = 102;
    private static final String TEMPFILE = "TEMP";
    private static final String AUTHORITY = "com.embroidermodder.embroideryviewer";
    String drawerFragmentTag;
    private final int SELECT_FILE = 1;
    private DrawView drawView;
    private DrawerLayout mainActivity;
    private Uri _uriToLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)
                || Intent.ACTION_EDIT.equals(action)) {
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
                final Uri finalReturnUri = returnUri;

                Thread urlReaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        readFileWrapper(finalReturnUri);
                    }
                });
                urlReaderThread.start();
            }
        }

        mainActivity = (DrawerLayout) findViewById(R.id.mainActivity);
        drawView = (DrawView) findViewById(R.id.drawview);
        drawView.initWindowSize();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainActivity, toolbar, R.string.app_name, R.string.app_name);
        mainActivity.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ((getPattern() != null) && (!getPattern().isEmpty())) {
            saveInternalFile(TEMPFILE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((getPattern() == null) || (getPattern().isEmpty())) {
            loadInternalFile(TEMPFILE);
        }
    }

    public void saveInternalFile(String filename) {
        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            EmbWriter writer = new EmbWriterEmm();
            writer.write(getPattern(), fos);
            fos.flush();
            fos.close();
        } catch (IOException ioerror) {
        }
    }


    public void loadInternalFile(String filename) {
        try {
            FileInputStream fis = openFileInput(filename); //if no file exists, throws error.
            EmbReader reader = new EmbReaderEmm();
            EmbPattern pattern = new EmbPattern();
            reader.read(pattern, fis);
            setPattern(pattern);
            fis.close();
        } catch (IOException ignored) {
        } catch (OutOfMemoryError ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        if (dialogDismiss()) {
            return;
        }
        if (tryCloseFragment(ShareFragment.TAG)) {
            return;
        }
        if (mainActivity.isDrawerOpen(GravityCompat.START)) {
            mainActivity.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
            case R.id.action_show_statistics:
                showStatistics();
                return true;
            case R.id.action_share:
                useShareFragment();
                break;
            case R.id.action_load_file:
                dialogDismiss();
                makeDialog(R.layout.embroidery_thumbnail_view);
                saveFileWrapper(new PermissionRequired() {
                    @Override
                    public void openExternalStorage(File root, String data) {
                        loadFile(root, data);
                    }
                }, Environment.getExternalStorageDirectory(), "");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveFile(Environment.getExternalStorageDirectory(), "");
                } else {
                    Toast.makeText(MainActivity.this, R.string.write_permissions_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_CODE_ASK_PERMISSIONS_LOAD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFile(Environment.getExternalStorageDirectory(), "");
                } else {
                    Toast.makeText(MainActivity.this, R.string.write_permissions_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_CODE_ASK_PERMISSIONS_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    threadReadFromUri(_uriToLoad);
                } else {
                    Toast.makeText(MainActivity.this, R.string.read_permissions_denied, Toast.LENGTH_SHORT)
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

    interface PermissionRequired {
        void openExternalStorage(File root, String data);
    }

    private void saveFileWrapper(PermissionRequired permissionRequired, File root, String data) {
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
        permissionRequired.openExternalStorage(root, data);
    }

    private void readFileWrapper(Uri uri) {
        _uriToLoad = uri;
        int hasReadExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessageOKCancel(getString(R.string.external_storage_justification_read),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        REQUEST_CODE_ASK_PERMISSIONS_READ);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS_READ);
            return;
        }
        threadReadFromUri(uri);
    }

    private void loadFile(File root, String data) {
        GridView list = (GridView) dialogView.findViewById(R.id.embroideryThumbnailList);
        File mPath = new File(root + "");
        list.setAdapter(new ThumbnailAdapter(MainActivity.this, mPath));
    }

    private void saveFile(File root, String data) {
        try {
            int n = 10000;
            Random generator = new Random();
            n = generator.nextInt(n);
            String filename = "Image-" + n + ".jef";
            IFormat.Writer format = IFormat.getWriterByFilename(filename);
            if (format != null) {
                File file = new File(root, filename);
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream outputStream = new FileOutputStream(file);
                format.write(drawView.getEmbPattern(), outputStream);
                outputStream.flush();
                outputStream.close();
            }
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

    private void onSelectFileResult(Intent data) {
        Uri uri = data.getData();
        if (uri != null) {
            readFileWrapper(uri);
        }
    }

    public void useShareFragment() {
        dialogDismiss();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ShareFragment fragment = new ShareFragment();
        transaction.add(R.id.mainContentArea, fragment, ShareFragment.TAG);
        transaction.commitAllowingStateLoss();
    }

    public void useColorFragment() {
        if (drawerFragmentTag != null) {
            tryCloseFragment(drawerFragmentTag);
        }
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ColorStitchBlockFragment fragment = new ColorStitchBlockFragment();
        drawerFragmentTag = ColorStitchBlockFragment.TAG;
        transaction.add(R.id.drawerContent, fragment, ColorStitchBlockFragment.TAG);
        transaction.commit();
        //drawView.getRoot().addListener(fragment);
    }

    public boolean tryCloseFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragmentByTag;
        fragmentByTag = fragmentManager.findFragmentByTag(tag);
        if (fragmentByTag == null) return false;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragmentByTag);
        transaction.commit();
        if (fragmentByTag instanceof DrawView.Listener) {
            drawView.removeListener((DrawView.Listener) fragmentByTag);
        }
        return true;
    }

    public void invalidateOnMainThread() {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invalidateOnMainThread();
                }
            });
            return;
        }
        drawView.invalidate();
    }

    public void toast(final int stringResource) {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast(stringResource);
                }
            });
            return;
        }
        Toast toast;
        toast = Toast.makeText(this, stringResource, Toast.LENGTH_LONG);
        toast.show();
    }

    public void showStatistics() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(drawView.getStatistics());
        builder.show();
    }

    public EmbPattern getPattern() {
        if (drawView == null) return null;
        return drawView.getEmbPattern();
    }

    public void setPattern(EmbPattern pattern) {
        drawView.setPattern(pattern);
        invalidateOnMainThread();
        useColorFragment();
    }


    public void threadReadFromUri(final Uri uri) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                readFromUri(uri);
            }
        });
        thread.start();
    }

    private void readFromUri(final Uri uri) {
        IFormat.Reader formatReader = null;
        Cursor returnCursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
        String scheme = uri.getScheme().toLowerCase();
        try {
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String filename = returnCursor.getString(nameIndex);
                formatReader = IFormat.getReaderByFilename(filename);
            } else if (scheme.equals("file")) {
                formatReader = IFormat.getReaderByFilename(uri.toString());
            }
        } catch (Exception e) {
        } finally {
            try {
                returnCursor.close();
            } catch (Exception e) {
            }
        }
        if (formatReader == null) {
            toast(R.string.file_type_not_supported);
            return;
        }
        EmbPattern pattern = null;
        switch (uri.getScheme().toLowerCase()) {
            case "http":
            case "https":
                HttpURLConnection connection;
                URL url;
                try {
                    url = new URL(uri.toString());
                } catch (MalformedURLException e) {
                    toast(R.string.error_file_not_found);
                    return;
                }
                try {
                    connection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    pattern = new EmbPattern();
                    formatReader.read(pattern, in);
                } catch (IOException e) {
                    toast(R.string.error_file_read_failed);
                    return;
                }
                break;
            case "content":
            case "file":
            default:
                try {
                    InputStream fis = getContentResolver().openInputStream(uri);
                    pattern = new EmbPattern();
                    formatReader.read(pattern, fis);
                } catch (FileNotFoundException e) {
                    toast(R.string.error_file_not_found);
                    return;
                } catch (IOException e) {
                    toast(R.string.error_file_read_failed);
                }

                break;
        }
        if (pattern == null) {
            toast(R.string.error_file_read_failed);
        }
        drawView.notifyChange(1);
        drawView.postInvalidate();
    }
}