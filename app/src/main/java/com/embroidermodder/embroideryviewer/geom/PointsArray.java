package com.embroidermodder.embroideryviewer.geom;

import java.util.ArrayList;

/**
 * ArrayList backed implementation of Points.
 */

public class PointsArray extends ArrayList<Point> implements Points {

    @Override
    public Point getPoint(int index) {
        return get(index);
    }

    @Override
    public float getX(int index) {
        return (float) get(index).getX();
    }

    @Override
    public float getY(int index) {
        return (float) get(index).getY();
    }

    @Override
    public void setLocation(int index, float x, float y) {
        get(index).setLocation(x, y);
    }

    @Override
    public int getData(int index) {
        return get(index).data();
    }

    public void add(Points points) {
        for (int i = 0, s = points.size(); i < s; i++) {
            add(points.getPoint(i));
        }
    }
}
