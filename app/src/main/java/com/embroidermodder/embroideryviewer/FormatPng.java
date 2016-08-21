package com.embroidermodder.embroideryviewer;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.io.OutputStream;

/**
 * Created by Tat on 8/21/2016.
 */
public class FormatPng implements IFormat.Writer {
    @Override
    public void write(EmbPattern pattern, OutputStream stream) {
        RectF bounds = pattern.calculateBoundingBox();
        Bitmap bitmap = pattern.getThumbnail(bounds.width(),bounds.height());
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
    }
}
