package com.embroidermodder.embroideryviewer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StitchBlockAdapter extends BaseAdapter {
    EmbPattern pattern;

    public StitchBlockAdapter(EmbPattern pattern) {
        this.pattern = pattern;
    }

    public void setPattern(EmbPattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public int getCount() {
        return pattern.getStitchBlocks().size();
    }

    @Override
    public Object getItem(int i) {
        return pattern.getStitchBlocks().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        StitchBlock stitchBlock = pattern.getStitchBlocks().get(i);
        StitchBlockView stitchBlockView;
        if (convertView == null) {
            stitchBlockView = StitchBlockView.inflate(parent);
        }
        else {
            stitchBlockView = (StitchBlockView) convertView;
        }
        stitchBlockView.setStitchBlock(i, pattern, stitchBlock);
        return stitchBlockView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
