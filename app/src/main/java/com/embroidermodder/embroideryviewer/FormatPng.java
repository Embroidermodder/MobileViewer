package com.embroidermodder.embroideryviewer;

import android.graphics.Bitmap;

import java.io.OutputStream;

public class FormatPng implements IFormat.Writer {
    @Override
    public void write(EmbPattern pattern, OutputStream stream) {
        EmbPatternViewer viewRootLayer = new EmbPatternViewer(pattern);
        Bitmap bitmap = viewRootLayer.getThumbnail(
                Math.max(pattern.getStitches().getWidth(), 1),
                Math.max(pattern.getStitches().getHeight(), 1));
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
    }
}
