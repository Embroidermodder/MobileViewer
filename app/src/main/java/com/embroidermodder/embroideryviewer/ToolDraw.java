package com.embroidermodder.embroideryviewer;

import android.view.MotionEvent;

public class ToolDraw implements Tool {

    @Override
    public boolean rawTouch(DrawView drawView, MotionEvent event) {
        return false;
    }

    @Override
    public boolean touch(DrawView drawView, MotionEvent event) {
        EmbPattern pattern = drawView.getPattern();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                pattern.addStitchAbs(event.getX(), event.getY(), IFormat.NORMAL, false);
                drawView.invalidate(); //in larger operations, you would invalidate *only* the sections that could have changed.
                break;
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                pattern.addStitchAbs(event.getX(), event.getY(), IFormat.STOP, false);
                pattern.notifyChange(0);
                break;
        }
        return true;
    }
}
