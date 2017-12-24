package com.embroidermodder.embroideryviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ShareFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "ShareFragment";

    static final String CACHEDIR_PATH = "cache";

    public static final int SAVE_TYPE_DST = 0;
    public static final int SAVE_TYPE_JEF = 1;
    public static final int SAVE_TYPE_XXX = 2;
    public static final int SAVE_TYPE_EXP = 3;
    public static final int SAVE_TYPE_SVG = 4;
    public static final int SAVE_TYPE_PCS = 5;
    public static final int SAVE_TYPE_SEW = 6;
    public static final int SAVE_TYPE_VP3 = 7;
    public static final int SAVE_TYPE_PES = 8;
    public static final int SAVE_TYPE_PEC = 9;
    public static final int SAVE_TYPE_SHV = 10;
    public static final int SAVE_TYPE_HUS = 11;
    public static final int SAVE_TYPE_CVS = 12;
    public static final int SAVE_TYPE_KSM = 13;
    public static final int SAVE_TYPE_EMM = 14;

    public static final String VAR_SAVENAME = "save_name";
    public static final String VAR_SETTINGS = "emb_settings";

    public static final String DEFAULT_FILENAME = "untitled";

    static final int SETTINGS_DEFAULT = SAVE_TYPE_DST;

    private Integer[] ids = new Integer[]{
            R.id.radioDst, R.id.radioJef, R.id.radioXxx, R.id.radioExp, R.id.radioSvg, R.id.radioPcs, R.id.radioSew, R.id.radioVp3, R.id.radioPes, R.id.radioPec, R.id.radioShv, R.id.radioHus, R.id.radioCvs, R.id.radioKsm, R.id.radioEmm
    };

    private EmbPattern embPattern;
    int selected_id = SETTINGS_DEFAULT;
    Uri sharedFileUri;


    public ShareFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            embPattern = ((MainActivity) context).getPattern();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences preferences = getPreferences();
        String savename = preferences.getString(VAR_SAVENAME, null);

        View v;

        if (savename != null) {
            v = view.findViewById(R.id.textviewFilename);
            if ((v != null) && (v instanceof EditText)) {
                EditText editText = (EditText) v;
                editText.setHint(savename);
                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            saveExportSettings();
                            if (getView() != null) exportOptionsExecute();
                            dismissFragment();
                        }
                        return false;
                    }
                });

            }
        }

        setListenerToRadioButtons(view, this);

        View.OnClickListener exportClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveExportSettings();
                exportOptionsExecute();
                dismissFragment();
            }
        };
        v = view.findViewById(R.id.shareButton);
        if ((v != null) && (v instanceof ImageButton)) {
            ImageButton ib = (ImageButton) v;
            ib.setOnClickListener(exportClickListener);
        }
        selected_id = preferences.getInt(VAR_SETTINGS, selected_id);
        setStateBySettings();
        for (int i = 0, ie = ids.length; i < ie; i++) {
            v = view.findViewById(ids[i]);
            if (v instanceof RadioButton) {
                RadioButton b = (RadioButton)v;
                CharSequence text = b.getText();
                IFormat.Writer writer = IFormat.getWriterByFilename("test" + text.toString());
                if (writer == null) {
                    v.setEnabled(false);
                }
            }
        }

    }


    public SharedPreferences getPreferences() {
        return getActivity().getPreferences(Activity.MODE_PRIVATE);
    }


    public void setListenerToRadioButtons(View view, CompoundButton.OnCheckedChangeListener listener) {
        RadioButton rb;
        for (int id : ids) {
            rb = (RadioButton) view.findViewById(id);
            rb.setOnCheckedChangeListener(listener);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b) return; //ignore the unselected trigger.
        ArrayList<Integer> ids = new ArrayList<>();
        ids.addAll(Arrays.asList(this.ids));
        ids.remove((Integer) compoundButton.getId());
        unsetGroup(ids);
        compoundButton.post(new Runnable() {
            @Override
            public void run() {
                selected_id = getSettingsByState();
                setStateBySettings();
            }
        });

    }

    public void dismissFragment() {
        MainActivity activity = (MainActivity) getActivity();
        activity.tryCloseFragment(TAG);
    }

    public void saveExportSettings() {
        String name = getNameFromDialog();
        if (name == null) return;

        SharedPreferences preferences = getPreferences();
        SharedPreferences.Editor save = preferences.edit();

        save.putString(VAR_SAVENAME, name);
        validateSettings();
        save.putInt(VAR_SETTINGS, selected_id);
        save.apply();
    }
    public void validateSettings() {
        selected_id = getSettingsByState();
    }

    public int getSettingsByState() {
        int shared = 0;
        for (int id : ids) {
            Boolean b = isDialogRadioChecked(id);
            if (b) break;
            shared++;
        }
        return shared;
    }

    public void setStateBySettings() {
        setListenerToRadioButtons(getView(), null);
        View view = getView();
        if (view == null) return;
        setGroup(ids[selected_id]);
        setListenerToRadioButtons(getView(), this);
    }

    public void exportOptionsExecute() {
        File file;
        String name = getNameFromDialog();
        validateSettings();
        String filename;
        switch (selected_id) {
            case SAVE_TYPE_DST:
                filename = validateName(name, "dst");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-dst");
                break;
            case SAVE_TYPE_JEF:
                filename = validateName(name, "jef");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-jef");
                break;
            case SAVE_TYPE_XXX:
                filename = validateName(name, "xxx");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-xxx");
                break;
            case SAVE_TYPE_EXP:
                filename = validateName(name, "exp");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-exp");
                break;
            case SAVE_TYPE_SVG:
                filename = validateName(name, "svg");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "image/svg+xml");
                break;
            case SAVE_TYPE_PCS:
                filename = validateName(name, "pcs");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-pcs");
                break;
            case SAVE_TYPE_SEW:
                filename = validateName(name, "sew");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-sew");
                break;
            case SAVE_TYPE_VP3:
                filename = validateName(name, "vp3");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-vp3");
                break;
            case SAVE_TYPE_PES:
                filename = validateName(name, "pes");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-pes");
                break;
            case SAVE_TYPE_PEC:
                filename = validateName(name, "pec");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-pec");
                break;
            case SAVE_TYPE_SHV:
                filename = validateName(name, "shv");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-shv");
                break;
            case SAVE_TYPE_HUS:
                filename = validateName(name, "hus");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-hus");
                break;
            case SAVE_TYPE_CVS:
                filename = validateName(name, "cvs");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-cvs");
                break;
            case SAVE_TYPE_KSM:
                filename = validateName(name, "ksm");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-ksm");
                break;
            case SAVE_TYPE_EMM:
                filename = validateName(name, "emm");
                file = createCachedEmbroideryFile(filename, name);
                shareFile(file, name, "application/x-emm");
                break;
        }
        dismissFragment();
    }

    /*
    * File Establishers
    */

    public File getCacheFile(String filename) {
        File cachePath = new File(getActivity().getCacheDir(), CACHEDIR_PATH);
        if (!cachePath.exists()) {
            cachePath.mkdir();
            cachePath = new File(getActivity().getCacheDir(), CACHEDIR_PATH);
        } else {
            File[] files = cachePath.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) file.deleteOnExit();
            }
        }
        return new File(cachePath, filename);
    }

    public File createCachedEmbroideryFile(String filename, String name) {
        File cacheFile = getCacheFile(filename);
        return writeEmbroideryFile(cacheFile, filename, name);
    }


    public File writeEmbroideryFile(File file, String filename, String name) {
        embPattern.name = name;
        IFormat.Writer writer = IFormat.getWriterByFilename(filename);
        if (writer == null) return null;
        OutputStream out = null;
        try {

            out = new BufferedOutputStream(new FileOutputStream(file));
            writer.write(embPattern, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    public static String validateName(String name, String suffix) {
        return validateName(name, suffix, 0);
    }

    public static String validateName(String name, String suffix, int append) {
        if (!suffix.startsWith(".")) suffix = "." + suffix;
        StringBuilder sb = new StringBuilder();
        if (name == null) name = DEFAULT_FILENAME;
        sb.append(name);
        if (!name.endsWith(suffix)) {
            if (append != 0) sb.append(append);
            sb.append(suffix);
        }
        return sb.toString();
    }

    public File getRequestedSaveFile(String directory, String ext, String requestedName) {
        File file = null;
        File dir = null;
        if (directory != null) dir = new File(directory);
        if ((dir == null) || (!dir.exists())) {
            dir = Environment.getExternalStorageDirectory();
        }
        int itr = 0;
        do {
            file = new File(dir.getAbsolutePath() + '/' + validateName(requestedName, ext, itr++));
        } while (file.exists());
        return file;
    }

    public void checkRevoke() {
        if (sharedFileUri != null) {
            MainActivity activity = (MainActivity) getActivity();
            activity.revokeUriPermission(sharedFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sharedFileUri = null;
        }
    }

    public void shareFile(File file, String filename, String mime) {
        MainActivity activity = (MainActivity) getActivity();
        checkRevoke();
        if (file == null) return;
        sharedFileUri = FileProvider.getUriForFile(
                activity,
                "com.embroidermodder.embroideryviewer.fileprovider",
                file);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setDataAndType(sharedFileUri, mime);
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedFileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        List<ResolveInfo> resInfos =
                activity.getPackageManager().queryIntentActivities(shareIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resInfos) {
            activity.grantUriPermission(info.activityInfo.packageName,
                    sharedFileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_file_to)));
    }


    private String getNameFromDialog() {
        String string = null;
        View v = getView();
        if (v == null) return null;
        v = v.findViewById(R.id.textviewFilename);
        if ((v != null) && (v instanceof EditText)) {
            EditText editText = (EditText) v;
            string = editText.getText().toString();
            if (string.isEmpty()) {
                string = editText.getHint().toString();
            }
        }
        return string;
    }

    public void unsetGroup(List<Integer> group) {
        View view = getView();
        if (view == null) return;
        for (Integer id : group) {
            View v = view.findViewById(id);
            if ((v != null) && (v instanceof RadioButton)) {
                RadioButton b = (RadioButton) v;
                b.setChecked(false);
            }
        }
    }

    public void unsetGroup(int... group) {
        View view = getView();
        if (view == null) return;
        for (int id : group) {
            View v = view.findViewById(id);
            if ((v != null) && (v instanceof RadioButton)) {
                RadioButton b = (RadioButton) v;
                b.setChecked(false);
            }
        }
    }

    public void setGroup(int... group) {
        View view = getView();
        if (view == null) return;
        for (int id : group) {
            View v = view.findViewById(id);
            if ((v != null) && (v instanceof RadioButton)) {
                RadioButton b = (RadioButton) v;
                b.setChecked(true);
            }
        }
    }

    public Boolean isDialogRadioChecked(int id) {
        View v = getView().findViewById(id);
        if ((v != null) && (v instanceof RadioButton)) {
            RadioButton b = (RadioButton) v;
            return b.isChecked();
        }
        return null;
    }
}
