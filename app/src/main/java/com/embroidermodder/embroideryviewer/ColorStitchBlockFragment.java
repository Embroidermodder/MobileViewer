package com.embroidermodder.embroideryviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class ColorStitchBlockFragment extends Fragment implements EmbPattern.Listener {
    public static final String TAG = "ColorStitch";
    private StitchBlockAdapter adapter;
    private ListView colorListView;

    public ColorStitchBlockFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_color_stitch_block, container, false);
    }

    public void setPattern(EmbPattern pattern) {
        if (pattern != null) {
            if (adapter == null) {
                adapter = new StitchBlockAdapter(pattern);
            }
            adapter.setPattern(pattern);
            colorListView.setAdapter(adapter);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        colorListView = (ListView) view.findViewById(R.id.colorListView);
        if (getActivity() instanceof EmbPattern.Provider) {
            setPattern(((EmbPattern.Provider) getActivity()).getPattern());
        }
    }

    @Override
    public void update(int v) {
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
