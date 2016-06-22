package com.embroidermodder.embroideryviewer;

import android.view.MotionEvent;

public class ToolPan implements Tool {

    float dx;
    float dy;

    @Override
    public boolean rawTouch(DrawView drawView, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float cx = event.getX();
                float cy = event.getY();
                if (!Float.isNaN(dx)) drawView.pan(dx-cx,dy-cy);
                dx = cx;
                dy = cy;
                drawView.invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                dx = event.getX();
                dy = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                dx = Float.NaN;
                dy = Float.NaN;
                break;
        }
        return true;
    }

    @Override
    public boolean touch(DrawView drawView, MotionEvent event) {
        return false;
    }
}
