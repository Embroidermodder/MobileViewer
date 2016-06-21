package com.embroidermodder.embroideryviewer;

import android.graphics.Canvas;
import android.graphics.Paint;

public class StitchBlock extends PointList  {
    private EmbThread _thread;

    public StitchBlock() {
    }

    public StitchBlock(float[] pack) {
        super(pack);
    }

    public StitchBlock(PointList p) {
        super(p);
    }

    public EmbThread getThread(){
        return _thread;
    }

    public void setThread(EmbThread value){
        _thread = value;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(_thread.getColor().getAndroidColor());
        if (count >= 4) {
            if ((count & 2) != 0) {
                canvas.drawLines(pointlist, 0, count - 2, paint);
                canvas.drawLines(pointlist, 2, count - 2, paint);
            } else {
                canvas.drawLines(pointlist, 0, count, paint);
                canvas.drawLines(pointlist, 2, count - 4, paint);
            }
        }
    }
}

