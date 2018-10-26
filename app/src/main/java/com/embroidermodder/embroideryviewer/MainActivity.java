package com.embroidermodder.embroideryviewer;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Toast;

import com.embroidermodder.embroideryviewer.embroideryview.EmbroideryFileDirectoryFragment;
import com.embroidermodder.embroideryviewer.embroideryview.OnListFragmentInteractionListener;

import org.embroideryio.embroideryio.EmbPattern;
import org.embroideryio.embroideryio.EmbroideryIO;

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

public class MainActivity extends AppCompatActivity implements EmmPattern.Provider, OnListFragmentInteractionListener {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 100;
    final private int REQUEST_CODE_ASK_PERMISSIONS_LOAD = 101;
    final private int REQUEST_CODE_ASK_PERMISSIONS_READ = 102;
    private static final String TEMPFILE = "TEMP";
    private static final String AUTHORITY = "com.embroidermodder.embroideryviewer";
    String drawerFragmentTag;
    private final int SELECT_FILE = 1;
    private DrawView drawView;
    private DrawerLayout mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        threadLoadIntent(intent);

        mainActivity = (DrawerLayout) findViewById(R.id.mainActivity);
        drawView = (DrawView) findViewById(R.id.drawview);
        drawView.initWindowSize();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainActivity, toolbar, R.string.app_name, R.string.app_name);
        mainActivity.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * The pause method calls the internal save routine to write an EMM file to local storage.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if ((getPattern() != null) && (!getPattern().isEmpty())) {
            saveInternalFile(TEMPFILE);
        }
    }

    /**
     * The resume method calls the internal save routine to read an EMM file from local storage.
     * This will allow long term non-memory versions of the file to persist through rotation changes and other alterations.
     */
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
            EmbWriterEmm writer = new EmbWriterEmm();
            writer.write(getPattern(), fos);
            fos.flush();
            fos.close();
        } catch (IOException ioerror) {
        }
    }


    public void loadInternalFile(String filename) {
        try {
            FileInputStream fis = openFileInput(filename); //if no file exists, throws error.
            EmbReaderEmm reader = new EmbReaderEmm();
            EmmPattern pattern = new EmmPattern();
            reader.read(pattern, fis);
            setPattern(pattern);
            fis.close();
        } catch (IOException ignored) {
        } catch (OutOfMemoryError ignored) {
        }
    }

    /**
     * The backpressed method checks if there's a dialog and closes it, if not
     * it closes the share fragment, if not
     * it closes the emboridery file directory fragment, if not
     * it closes the drawer, if not
     * app closes.
     *
     */
    @Override
    public void onBackPressed() {
        if (dialogDismiss()) {
            return;
        }
        if (tryCloseFragment(ShareFragment.TAG)) {
            return;
        }
        if (tryCloseFragment(EmbroideryFileDirectoryFragment.TAG)) {
            return;
        }
        if (mainActivity.isDrawerOpen(GravityCompat.START)) {
            mainActivity.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * @param intent
     * requires single instance mode, else this does nothing.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        threadLoadIntent(intent);
    }

    /**
     * @param menu
     * Inflate the menu; this adds items to the action bar if it is present.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * @param item
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            case R.id.action_folders:
                useViewerFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * @param item File selected
     *  Fragment communcation interface to get fragment response to selected file.
     */
    @Override
    public void onListFragmentInteraction(File item) {
        loadFile(item);
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     * Request_code_ask_permission is the permission to save. This was used by now unused code that saved a .jef file in the root directory.
     *
     * Request_code_ask_permission_load is the permission requested by the embroidery_folders
     *
     * Request_code_ask_permission_read is the permission requested by an intent loading a file.
     * This is either launched with that file, or requested by the  load file to recent.
     */
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
                    loadFile(Environment.getExternalStorageDirectory());
                } else {
                    Toast.makeText(MainActivity.this, R.string.read_permissions_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_CODE_ASK_PERMISSIONS_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    threadLoadIntent(getIntent()); //restarts the load of the intent.
                } else {
                    Toast.makeText(MainActivity.this, R.string.read_permissions_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     *
     * Response from intent for response. Should be the Open File request
     * Made from Open Recent File.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                threadLoadIntent(data);
            }
        }
    }

    /**
     * @param item
     * Loads given file, switches out the pattern.
     */
    private void loadFile(File item) {
        try {
            EmmPattern pattern = new EmmPattern();
            EmbPattern pat = EmbroideryIO.read(item.getPath());
            pattern.fromEmbPattern(pat);
            setPattern(pattern);
            drawView.postInvalidate();
            tryCloseFragment(EmbroideryFileDirectoryFragment.TAG);
        } catch (IOException e) {
        }
    }

    /**
     * @param root
     * @param data
     *
     * Unused code. Exports now run through share. This however save the file to the local device.
     */
    private void saveFile(File root, String data) {
        //todo: this really only save one jef to the root directory. Decide what needs to be done.
        try {
            int n = 10000;
            Random generator = new Random();
            n = generator.nextInt(n);
            String filename = "Image-" + n + ".jef";
            File file = new File(root, filename);
            if (file.exists()) {
                    file.delete();
            }
            EmbroideryIO.write(drawView.getPattern().toEmbPattern(),file.getCanonicalPath());
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

    /**
     * Adds the folders fragment to the main area.
     */
    public void useViewerFragment() {
        dialogDismiss();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        EmbroideryFileDirectoryFragment fragment = new EmbroideryFileDirectoryFragment();
        transaction.add(R.id.mainContentArea, fragment, EmbroideryFileDirectoryFragment.TAG);
        transaction.commit();
    }

    /**
     * Adds the sharing fragment to the main area.
     */
    public void useShareFragment() {
        dialogDismiss();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ShareFragment fragment = new ShareFragment();
        transaction.add(R.id.mainContentArea, fragment, ShareFragment.TAG);
        transaction.commit();
    }

    /**
     * Adds the colorstitch fragment to the drawer.
     *
     * Other fragments can be switched into the drawer, and used instead of this one.
     */
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
    }

    /**
     * @param tag tag of the fragment to close.
     * @return true if fragment was closed.
     * Attempt to close the fragment by the tag, if such a fragment exists closes (true)
     */
    public boolean tryCloseFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragmentByTag;
        fragmentByTag = fragmentManager.findFragmentByTag(tag);
        if (fragmentByTag == null) return false;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragmentByTag);
        transaction.commit();
        if (fragmentByTag instanceof EmmPattern.Listener) {
            EmmPattern pattern = getPattern();
            if (pattern != null) {
                pattern.removeListener((EmmPattern.Listener) fragmentByTag);
            }
        }
        return true;
    }


    /**
     * @param stringResource
     *
     * toast helper to make sure the toast is called from the UI thread as is required.
     */
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

    /**
     * Show the statistics dialog.
     */
    public void showStatistics() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(drawView.getStatistics());
        builder.show();
    }


    /**
     * @return pattern, of the drawview
     * get the drawview pattern.
     */
    public EmmPattern getPattern() {
        if (drawView == null) return null;
        return drawView.getPattern();
    }

    /**
     * @param pattern
     * Ensure we are using UI thread.
     * set the pattern on the drawview and invalidate, the views.
     */
    public void setPattern(final EmmPattern pattern) {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPattern(pattern);
                }
            });
            return;
        }
        drawView.setPattern(pattern);
        getPattern().notifyChange(EmmPattern.NOTIFY_LOADED);
        drawView.invalidate();
        useColorFragment();
    }


    /**
     * @param intent
     * Load the intent, in a different thread.
     */
    public void threadLoadIntent(final Intent intent) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadFromIntent(intent);
            }
        });
        thread.start();
    }


    /**
     * @param intent
     * @return Uri gotten from the intent.
     *
     * Get the Uri from the intent.
     * There are several different ways apps put that data into an intent so several have to be tried.
     */
    public Uri getUriFromIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) return uri;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return null;

        Object object = bundle.get(Intent.EXTRA_STREAM);
        if (object instanceof Uri) {
            return (Uri) object;
        }
        return null;
    }

    /**
     * @param uri
     * @return displayname
     *
     * Tries to get the display name by the Uri, sometimes the Uri isn't a strict filename but
     * the type of file is needed for the reader.
     */
    protected String getDisplayNameByUri(Uri uri) {
        String filename = null;
        if (uri.getScheme().equalsIgnoreCase("content")) {
            Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
            if ((returnCursor != null) && (returnCursor.getCount() != 0)) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (returnCursor.moveToFirst()) filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
        } else {
            filename = uri.getPath();
        }
        return filename;
    }

    /**
     * @param intent
     * Flag the intent if it's been loaded to stop reloading on rotate.
     * Gets the URI, ensures the action is accepted and loads the file.
     */
    protected void loadFromIntent(Intent intent) {
        if (intent == null) return;
        if (intent.hasExtra("done")) return;
        intent.putExtra("done", 1);

        Uri uri = getUriFromIntent(intent);
        if (uri == null) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) {
                //todo: toast error message about the URI not being read.
            }
            return;
        }
        String mime = intent.getType();
        EmbroideryIO.Reader reader = EmbroideryIO.getReaderByMime(mime);
        //reader = IFormat.getReaderByMime(mime); Sometimes the intent *only* has the MIME type and does not have an extention.

        if (reader == null) {
            String name = getDisplayNameByUri(uri);
            //String ext = IFormat.getExtentionByDisplayName(name);
            //if (ext == null) {
            //Toast error message about how the extension doesn't exist and there's no way to know what the file is without mimetype or extension.
            //return;
            //}
            reader = EmbroideryIO.getReaderByFilename(name);
            if (reader == null) {
                toast(R.string.file_type_not_supported);
                return;
            }
        }
        EmmPattern pattern = null;
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
                    connection.getHeaderField(HttpURLConnection.HTTP_LENGTH_REQUIRED);
                    connection.setReadTimeout(1000);
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    pattern = new EmmPattern();
                    EmbPattern read_pattern = EmbroideryIO.readEmbroidery(reader,in);
                    pattern.fromEmbPattern(read_pattern);
                    in.close();
                    connection.disconnect();
                } catch (IOException e) {
                    toast(R.string.error_file_read_failed);
                    return;
                }
                break;
            case "content":
            case "file":
                try {
                    InputStream fis = getContentResolver().openInputStream(uri);
                    pattern = new EmmPattern();
                    EmbPattern embPattern = EmbroideryIO.readEmbroidery(reader, fis);
                    pattern.fromEmbPattern(embPattern);
                } catch (FileNotFoundException e) {
                    toast(R.string.error_file_not_found);
                    return;
                } catch (IOException e) {
                    toast(R.string.error_file_read_failed);
                }
                break;
        }
        if (pattern != null) {
            setPattern(pattern);
            drawView.postInvalidate();
        }
    }
}