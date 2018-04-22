package com.embroidermodder.embroideryviewer.geom;

/**
 * Created by Tat on 6/25/2017.
 */

public class PointsIndexCycle<E extends Points> implements Points {
    public static final int INVALID_POINT = -1;
    public E list;
    public int start = INVALID_POINT;
    public int endOffset = 0;

    public PointsIndexCycle() {
    }

    public PointsIndexCycle(E list) {
        this.list = list;
    }

    public PointsIndexCycle(E list, int index_start, int endOffset) {
        this.list = list;
        this.start = index_start;
        this.endOffset = endOffset;
    }


    public int getCycleIndex(int index) {
        int size = list.size();
        if (size == 0) return INVALID_POINT;
        if (start == INVALID_POINT) return INVALID_POINT;
        int idx = start + index;
        if (idx < 0) {
            int loops = (-idx / size) + 1;
            return idx + (size * loops);
        }
        else {
            return idx % size;
        }
    }

    @Override
    public Point getPoint(int index) {
        int findex = getCycleIndex(index);
        if (findex == INVALID_POINT) return null;
        return new PointIndex<>(list,findex);
    }

    @Override
    public int getData(int index) {
        int findex = getCycleIndex(index);
        if (findex == INVALID_POINT) return 0;
        return list.getData(findex);
    }

    public float getX(int index) {
        int findex = getCycleIndex(index);
        if (findex == INVALID_POINT) return Float.NaN;
        return list.getX(findex);
    }

    public float getY(int index) {
        int findex = getCycleIndex(index);
        if (findex == INVALID_POINT) return Float.NaN;
        return list.getY(findex);
    }

    @Override
    public void setLocation(int index, float x, float y) {
        int findex = getCycleIndex(index);
        if (findex == INVALID_POINT) return;
        list.setLocation(findex, x, y);
    }

    public int size() {
        return endOffset+1;
    }

}
