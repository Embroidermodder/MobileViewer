package com.embroidermodder.embroideryviewer.geom;

/**
 * Static function helper class. Embroidery does a considerable amount of 2D Geometry and extends
 * into well understood areas that are not popular enough to be covered by Math or other such static
 * functions.
 */

public class Geometry2D {

    public static double distanceSq(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        dx *= dx;
        dy *= dy;
        return dx + dy;
    }

    public static double distance(double x0, double y0, double x1, double y1) {
        return Math.sqrt(distanceSq(x0, y0, x1, y1));
    }

    public static double distance(Point f, Point g) {
        return distance(f.getX(), f.getY(), g.getX(), g.getY());
    }

    public static double distanceSq(Point f, Point g) {
        return distanceSq(f.getX(), f.getY(), g.getX(), g.getY());
    }

    public static double distanceSq(Point f, double px, double py) {
        return distanceSq(f.getX(), f.getY(), px, py);
    }

    public static double manhattanDistance(Point f, Point g) {
        return Math.abs(f.getX() - g.getX()) + Math.abs(f.getY() - g.getY());
    }

    public static Point relative(Point point, double x, double y) {
        return new PointDirect(point.getX() + x, point.getY() + y);
    }

    public static void swapPoints(float[] pointlist, int index0, int index1) {
        float tx, ty;
        tx = pointlist[index0];
        ty = pointlist[index0 + 1];
        pointlist[index0] = pointlist[index1];
        pointlist[index0 + 1] = pointlist[index1 + 1];
        pointlist[index1] = tx;
        pointlist[index1 + 1] = ty;
    }

    public static void reverse(float[] pointlist, int count) {
        int m = count / 2;
        for (int i = 0, s = count - 2; i < m; i += 2, s -= 2) {
            swapPoints(pointlist, i, s);
        }
    }

    public static void snap(Points layer) {
        for (int i = 0, size = layer.size(); i < size; i++) {
            layer.setLocation(i, (float) Math.rint(layer.getX(i)), (float) Math.rint(layer.getY(i)));
        }
    }
}
