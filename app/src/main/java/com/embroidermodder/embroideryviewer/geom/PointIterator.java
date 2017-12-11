package com.embroidermodder.embroideryviewer.geom;

import java.util.Iterator;

/**
 *
 * Implementation of a Point iterator when given a Points object it will iterate through all the
 * Point values without actually needing to instance more than the single object.
 *
 */
public class PointIterator<E extends Points> implements Iterable<Point>, Iterator<Point> {

    E list;
    int index;
    Point pointIndex;

    public PointIterator(E pointlist) {
        this.list = pointlist;
        index = -1;
        pointIndex = new Point() {
            @Override
            public double getX() {
                return list.getX(index);
            }

            @Override
            public double getY() {
                return list.getY(index);
            }

            @Override
            public void setLocation(double x, double y) {
                list.setLocation(index, (float) x, (float) y);
            }

            @Override
            public int data() {
                return list.getData(index);
            }
        };
    }

    @Override
    public boolean hasNext() {
        return ((index + 1) < list.size());
    }

    @Override
    public Point next() {
        index++;
        return pointIndex;
    }

    @Override
    public Iterator<Point> iterator() {
        return this;
    }
}
