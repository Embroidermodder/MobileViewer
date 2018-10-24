package org.embroideryio.geom;

/**
 * Basic implementation of Point class backed by simple doubles.
 *
 */
public class PointDirect implements Point {
    double x;
    double y;
    int data = 1;


    public PointDirect(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointDirect(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointDirect(double x, double y, int data) {
        this.x = x;
        this.y = y;
        this.data = data;
    }


    public PointDirect() {
    }

    public PointDirect(Point point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int data() {
        return data;
    }
}
