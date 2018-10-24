package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import android.graphics.Bitmap;


import com.embroidermodder.embroideryviewer.EmmPattern;
import com.embroidermodder.embroideryviewer.EmmPatternQuickView;

import java.io.OutputStream;

public class FormatPng  {
    public void write(EmmPattern pattern, OutputStream stream) {
        EmmPatternQuickView viewRootLayer = new EmmPatternQuickView(pattern);
        //Math.max(pattern.getStitches().getWidth(), 1),
        Bitmap bitmap = viewRootLayer.squareThumbnail((int)Math.max(pattern.getStitches().getHeight(), 1));
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
    }
}
