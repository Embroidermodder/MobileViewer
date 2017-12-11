package com.embroidermodder.embroideryviewer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Geometry2D;
import com.embroidermodder.embroideryviewer.geom.PointsIndex;


public class StitchBlock extends PointsIndex<DataPoints> {
    private EmbThread _thread;
    private int type;

    public StitchBlock(DataPoints list, int index_start, int index_stop, EmbThread thread, int type) {
        super(list, index_start, index_stop);
        this._thread = thread;
        this.type = type;
    }

    public StitchBlock(DataPoints list, int start, int stop) {
        super(list,start,stop);
    }

    public EmbThread getThread() {
        return _thread;
    }

    public void setThread(EmbThread value) {
        _thread = value;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (_thread == null) return;
        paint.setColor(_thread.getColor());

        int count = (getIndex_stop() - getIndex_start()) << 1;
        int start = getIndex_start() << 1;
        if (count >= 4) {
            if ((count & 2) != 0) {
                canvas.drawLines(list.pointlist, start + 0, count - 2, paint);
                canvas.drawLines(list.pointlist, start + 2, count - 2, paint);
            } else {
                canvas.drawLines(list.pointlist, start + 0, count, paint);
                canvas.drawLines(list.pointlist, start + 2, count - 4, paint);
            }
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float distanceSegment(int i) {
        return (float) Math.sqrt(distanceSqSegment(i));
    }

    public float distanceSqSegment(int i) {
        return (float) Geometry2D.distanceSq(getPoint(i), getPoint(i + 1));
    }

    public void transform(Matrix matrix) {
        matrix.mapPoints(list.pointlist,getIndex_start(),list.pointlist,getIndex_start(),getIndex_stop()-getIndex_start());
        list.resetBounds();
    }
}

