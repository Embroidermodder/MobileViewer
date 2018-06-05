package com.embroidermodder.embroideryviewer.geom;

/**
 * Wrapper of a Points class which itself implements Points.
 *
 * Used for direct subsequence values delegating to the backing implementation.
 *
 * @param <E>
 */

public class PointsIndex<E extends Points> implements Points {

    public static final int INVALID_POINT = -1;
    protected E list;
    int index_start = INVALID_POINT;
    int index_stop = INVALID_POINT;

    public PointsIndex(E list, int index_start, int index_stop) {
        this.list = list;
        this.index_start = index_start;
        this.index_stop = index_stop;
    }

    public int getIndex_start() {
        return index_start;
    }

    public void setIndex_start(int index_start) {
        this.index_start = index_start;
    }

    public int getIndex_stop() {
        return index_stop;
    }

    public void setIndex_stop(int index_stop) {
        this.index_stop = index_stop;
    }

    @Override
    public Point getPoint(final int index) {
        return new Point() {
            @Override
            public double getX() {
                int idx = (index_start + index);
                return list.getX(idx);
            }

            @Override
            public double getY() {
                int idx = (index_start + index);
                return list.getY(idx);
            }

            @Override
            public void setLocation(double x, double y) {
                int idx = (index_start + index);
                list.setLocation(idx, (float) x, (float) y);
            }

            @Override
            public int data() {
                return (index_start + index);
            }
        };
    }

    @Override
    public float getX(int index) {
        int idx = (index_start + index);
        return list.getX(idx);
    }

    @Override
    public float getY(int index) {
        int idx = (index_start + index);
        return list.getY(idx);
    }

    @Override
    public int getData(int index) {
        int idx = (index_start + index);
        return list.getData(idx);
    }

    @Override
    public void setLocation(int index, float x, float y) {
        int idx = (index_start + index);
        list.setLocation(idx, x, y);
    }

    @Override
    public int size() {
        if (index_start == INVALID_POINT) {
            return 0;
        }
        if (index_stop == INVALID_POINT) {
            return 0;
        }
        if (index_start > index_stop) {
            return 0;
        }
        return (index_stop - index_start) + 1;
    }

}
