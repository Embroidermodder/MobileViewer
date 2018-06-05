package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import android.graphics.Bitmap;


import com.embroidermodder.embroideryviewer.EmbPatternQuickView;

import java.io.OutputStream;

public class FormatPng implements IFormat.Writer {
    @Override
    public void write(EmbPattern pattern, OutputStream stream) {
        EmbPatternQuickView viewRootLayer = new EmbPatternQuickView(pattern);
        //Math.max(pattern.getStitches().getWidth(), 1),
        Bitmap bitmap = viewRootLayer.squareThumbnail((int)Math.max(pattern.getStitches().getHeight(), 1));
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
    }

    @Override
    public void setSettings(int settings) {

    }

    @Override
    public int getSettings() {
        return 0;
    }

    @Override
    public double maxJumpDistance() {
        return 0;
    }

    @Override
    public double maxStitchDistance() {
        return 0;
    }

    @Override
    public boolean canEncode(int encode) {
        return false;
    }

    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    public boolean hasStitches() {
        return false;
    }
}
