package com.embroidermodder.embroideryviewer;

import android.view.MotionEvent;

public interface Tool {
    boolean touch(DrawView drawview, MotionEvent event);
    boolean rawTouch(DrawView drawView, MotionEvent event);
}
