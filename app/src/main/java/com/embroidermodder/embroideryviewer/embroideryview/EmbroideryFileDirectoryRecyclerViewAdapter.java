package com.embroidermodder.embroideryviewer.embroideryview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.embroidermodder.embroideryviewer.R;

import org.embroideryio.embroideryio.EmbroideryIO;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class EmbroideryFileDirectoryRecyclerViewAdapter extends RecyclerView.Adapter<EmbroideryFileDirectoryRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private final OnListFragmentInteractionListener mListener;
    private ThumbnailLoader loader;
    @Nullable
    File[] files;
    @Nullable
    File path;
    @Nullable
    File parent;

    @Nullable
    final FilenameFilter filter;

    public EmbroideryFileDirectoryRecyclerViewAdapter(File path, OnListFragmentInteractionListener listener, ThumbnailLoader loader) {
        mListener = listener;
        this.path = path;
        this.parent = path.getParentFile();
        this.filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, @NonNull String s) {
                File file = new File(dir, s);
                if (file.isDirectory()) return true;
                return EmbroideryIO.getReaderByFilename(s) != null;
            }
        };
        this.loader = loader;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filedir_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        vh.thumbnailView.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ThumbnailView thumbnailView = holder.thumbnailView;
        if (thumbnailView == null) return;
        thumbnailView.clear();

        validateFilesList();
        if (files == null) return;

        File file = files[position];
        thumbnailView.setFile(file);
        thumbnailView.isParent = file == parent;
        loader.remove(thumbnailView);
        loader.add(thumbnailView);
        loader.start();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        ThumbnailView thumbnailView = holder.thumbnailView;
        if (thumbnailView == null) return;
        thumbnailView.clear();
        loader.remove(thumbnailView);
    }


    public void validateFilesList() {
        if (files == null) {
            File[] directorylist = path.listFiles(filter);
            if (directorylist == null) directorylist = new File[0];
            Collections.sort(Arrays.asList(directorylist), new Comparator<File>() {
                @Override
                public int compare(@NonNull File o1, @NonNull File o2) {
                    if ((o1.isDirectory()) && (!o2.isDirectory())) {
                        return -1;
                    }
                    if ((!o1.isDirectory()) && (o2.isDirectory())) {
                        return 1;
                    }
                    return 0;
                }
            });
            parent = path.getParentFile();
            if (parent == null) {
                files = directorylist;
            } else {
                files = new File[directorylist.length + 1];
                System.arraycopy(directorylist, 0, files, 1, directorylist.length);
                files[0] = parent;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (path.isFile()) return 1;
        validateFilesList();
        return files.length;
    }


    @Override
    public void onClick(View view) {
        ThumbnailView thumbnailView = (ThumbnailView) view;
        File file = thumbnailView.file;
        if (file == null) return;

        if (file.isDirectory()) {
            path = file;
            parent = null;
            files = null;
            notifyDataSetChanged();
            return;
        }
        if (mListener != null) mListener.onListFragmentInteraction(file);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ThumbnailView thumbnailView;

        public ViewHolder(@NonNull View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnailView);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + thumbnailView.toString() + "'";
        }
    }
}
