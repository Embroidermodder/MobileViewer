package com.embroidermodder.embroideryviewer.geom;

/**
 * Indexed implementation of point.
 * With a given index and a Points implementation, it implements Point.
 *
 * This is so that classes like PointsDirect can return Point objects without
 * actually containing copies the underlying class objects.
 *
 * @param <E>
 */
public class PointIndex<E extends Points> implements Point {

    int index;
    E list;

    public PointIndex(E list, int index) {
        this.list = list;
        this.index = index;
    }

    @Override
    public double getX() {
        return list.getX(index);
    }

    @Override
    public double getY() {
        return list.getY(index);
    }

    @Override
    public int data() {
        return list.getData(index);
    }

    public void setLocation(int sx, int sy) {
        setLocation((double) sx, (double) sy);
    }

    @Override
    public void setLocation(double x, double y) {
        list.setLocation(index, (float) x, (float) y);
    }

    public double distance(Point p) {
        return Geometry2D.distance(this, p);
    }

    public double distance(double x, double y) {
        return Math.sqrt(distanceSq(x, y));
    }

    public double distanceSq(Point p) {
        return Geometry2D.distanceSq(this, p);
    }

    public double distanceSq(double px, double py) {
        return Geometry2D.distanceSq(this, px, py);
    }

    public double manhattanDistance(Point p) {
        return Geometry2D.manhattanDistance(this, p);
    }

    public void translate(double dX, double dY) {
        if (Double.isNaN(dX)) {
            return;
        }
        if (Double.isNaN(dY)) {
            return;
        }
        setLocation(getX() + dX, getY() + dY);
    }

    @Override
    public String toString() {
        return "Point{"
                + "x=" + getX()
                + ", y=" + getY()
                + '}';
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PointIndex<?> that = (PointIndex<?>) o;

        if (index != that.index) {
            return false;
        }
        return list != null ? list.equals(that.list) : that.list == null;

    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + (list != null ? list.hashCode() : 0);
        return result;
    }
}
