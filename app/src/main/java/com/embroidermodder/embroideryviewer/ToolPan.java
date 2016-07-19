package com.embroidermodder.embroideryviewer;

import android.view.MotionEvent;

public class ToolPan implements Tool {

    float dx1;
    float dy1;
    float dx2;
    float dy2;
    float dcx;
    float dcy;

    @Override
    public boolean rawTouch(DrawView drawView, MotionEvent event) {
        float cx1 = event.getX();
        float cy1 = event.getY();
        float cx2 = Float.NaN, cy2 = Float.NaN;
        float px = cx1;
        float py = cy1;
        if (event.getPointerCount() >= 2) {
            cx2 = event.getX(1);
            cy2 = event.getY(1);
            px = (cx1 + cx2) / 2;
            py = (cy1 + cy2) / 2;
        }


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float deltascale = (float) (distance(cx1,cy1,cx2,cy2) / distance(dx1,dy1,dx2,dy2));
                float dpx = px-dcx;
                float dpy = py-dcy;
                if (!Float.isNaN(dpx)) drawView.pan(dpx, dpy);
                if (!Float.isNaN(deltascale)) drawView.scale(deltascale, px, py);
                drawView.invalidate();
                break;
            default:
                cx1 = Float.NaN;
                cy1 = Float.NaN;
                cx2 = Float.NaN;
                cy2 = Float.NaN;
                px = Float.NaN;
                py = Float.NaN;
                break;
        }
        dx1 = cx1;
        dy1 = cy1;
        dx2 = cx2;
        dy2 = cy2;
        dcx = px;
        dcy = py;
        return true;
    }

    @Override
    public boolean touch(DrawView drawView, MotionEvent event) {
        return false;
    }

    public static float distance(float x0, float y0, float x1, float y1) {
        return (float)Math.sqrt(distanceSq(x0, y0, x1, y1));
    }
    public static float distanceSq(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        dx *= dx;
        dy *= dy;
        return dx + dy;
    }
}
