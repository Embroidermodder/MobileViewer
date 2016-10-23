package com.embroidermodder.embroideryviewer;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.io.OutputStream;

public class FormatPng implements IFormat.Writer {
    @Override
    public void write(EmbPattern pattern, OutputStream stream) {
        RectF bounds = pattern.calculateBoundingBox();
        Bitmap bitmap = pattern.getThumbnail(bounds.width(),bounds.height());
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
    }
}
