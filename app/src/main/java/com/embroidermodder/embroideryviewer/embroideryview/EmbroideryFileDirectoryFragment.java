package com.embroidermodder.embroideryviewer.embroideryview;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.embroidermodder.embroideryviewer.R;

import java.io.File;

public class EmbroideryFileDirectoryFragment extends Fragment implements OnListFragmentInteractionListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_DIRECTORY = "directory";
    public static final String TAG = "EmbroideryFileDirectory";
    private int mColumnCount = 3;
    @Nullable
    private OnListFragmentInteractionListener mListener;
    @Nullable
    String directory;
    @NonNull
    private ThumbnailLoader loader = new ThumbnailLoader();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EmbroideryFileDirectoryFragment() {
    }

    @NonNull
    public static EmbroideryFileDirectoryFragment newInstance(int columnCount, String directory) {
        EmbroideryFileDirectoryFragment fragment = new EmbroideryFileDirectoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_DIRECTORY, directory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            directory = getArguments().getString(ARG_DIRECTORY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_embroideryfileviewer, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.filedirectory_recyclerview);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
        File file = null;
        if (directory != null) {
            file = new File(directory);
            if (!file.exists()) file = null;
            else if (!file.isDirectory()) file = null;
        }
        if (file == null) {
            file = Environment.getExternalStorageDirectory();
        }
        recyclerView.setAdapter(new EmbroideryFileDirectoryRecyclerViewAdapter(file, mListener, loader));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(File item) {
        if (mListener != null) mListener.onListFragmentInteraction(item);
    }
}
