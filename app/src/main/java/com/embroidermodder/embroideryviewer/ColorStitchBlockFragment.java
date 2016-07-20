package com.embroidermodder.embroideryviewer;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class ColorStitchBlockFragment extends Fragment implements Pattern.Listener {
    public static final String TAG = "ColorStitch";
    private StitchBlockAdapter adapter;
    private ListView colorListView;

    public ColorStitchBlockFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_color_stitch_block, container, false);
    }

    public void setPattern(Pattern pattern) {
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
        if (getActivity() instanceof Pattern.Provider) {
            setPattern(((Pattern.Provider)getActivity()).getPattern());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void update(int v) {
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
