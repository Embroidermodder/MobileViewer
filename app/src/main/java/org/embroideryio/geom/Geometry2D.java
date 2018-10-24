package org.embroideryio.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Static function helper class. Embroidery does a considerable amount of 2D Geometry and extends
 * into well understood areas that are not popular enough to be covered by Math or other such static
 * functions.
 */

public class Geometry2D {
    public static final int DIRECTION_CCW = 0;
    public static final int DIRECTION_CW = 1;
    public static final double TAU = Math.PI * 2;
    public static final int INVALID_POINT = -1;

    static public Point towards(Point from, Point to, double amount, Point result) {
        if (result == null) result = new PointDirect();
        double nx = (amount * (to.getX() - from.getX())) + from.getX();
        double ny = (amount * (to.getY() - from.getY())) + from.getY();
        result.setLocation(nx, ny);
        return result;

    }

    public static Point midPoint(Point point0, Point point1, Point result) {
        if (result == null) result = new PointDirect();
        result.setLocation((point1.getX() + point0.getX()) / 2, (point1.getY() + point0.getY()) / 2);
        return result;
    }

    public static double distanceSq(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        dx *= dx;
        dy *= dy;
        return dx + dy;
    }

    public static double distance(float x0, float y0, float x1, float y1) {
        return Math.sqrt(distanceSq(x0, y0, x1, y1));
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

    public static double distance(Point f, double px, double py) {
        return Math.sqrt(distanceSq(f, px, py));
    }

    public static double distanceSq(Point f, double px, double py) {
        return distanceSq(f.getX(), f.getY(), px, py);
    }

    public static double manhattanDistance(Point f, Point g) {
        return Math.abs(f.getX() - g.getX()) + Math.abs(f.getY() - g.getY());
    }

    public static double towards(double a, double b, double amount) {
        return (amount * (b - a)) + a;
    }

    public static double angle(double x0, double y0, double x1, double y1) {
        return Math.toDegrees(angleR(x0, y0, x1, y1));
    }

    public static double angleR(double x0, double y0, double x1, double y1) {
        return Math.atan2(y1 - y0, x1 - x0);
    }

    public static float[] pack(List<Point> pointList) {
        float[] pack = new float[pointList.size() * 2];
        int itr = 0;
        for (Point p : pointList) {
            pack[itr++] = (float) p.getX();
            pack[itr++] = (float) p.getY();
        }
        return pack;
    }

    public static double deltaAngleR(double angle0, double angle1) {
        double delta = angle0 - angle1;
        if (delta > Math.PI) {
            return (2 * Math.PI) - delta;
        }
        if (delta < -Math.PI) {
            return delta + (2 * Math.PI);
        }
        return delta;
    }


    public static void sortPointsCW(List<Point> points) {
        double xsum = 0, ysum = 0;
        for (Point p : points) {
            xsum += p.getX();
            ysum += p.getY();
        }
        double sum = (double) points.size();
        final double cx = xsum / sum;
        final double cy = ysum / sum;
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point a, Point b) {
                if ((a.getX() >= 0) && (b.getX() < 0))
                    return 1; //if a.x >= 0 and b.x< 0 then return true
                else if ((a.getX() == 0) && (b.getX() == 0))
                    return (a.getY() > b.getY()) ? 1 : -1; //elseif a.x == 0 and b.x == 0 then return a.y > b.y

                double det = (a.getX() - cx) * (b.getY() - cy) - (b.getX() - cx) * (a.getY() - cy);
                if (det < 0) return 1;
                else if (det > 0) return -1;

                double d1 = (a.getX() - cx) * (a.getX() - cx) + (a.getY() - cy) * (a.getY() - cy);
                double d2 = (b.getX() - cx) * (b.getX() - cx) + (b.getY() - cy) * (b.getY() - cy);
                if (d1 == d2) return 0;
                return (d1 > d2) ? 1 : -1;
            }
        });
    }

    public static Point oriented(Point from, Point to, double r) {
        return oriented(from, to, r, null);
    }

    public static Point oriented(Point from, Point to, double r, Point result) {
        if (result == null) result = new PointDirect();
        double radians = angleR(from, to);
        result.setLocation(from.getX() + r * Math.cos(radians), from.getY() + r * Math.sin(radians));
        return result;
    }

    public static Point oriented(double from_x, double from_y, double to_x, double to_y, double r) {
        return oriented(from_x, from_y, to_x, to_y, r, null);
    }

    public static Point oriented(double from_x, double from_y, double to_x, double to_y, double r, Point result) {
        if (result == null) result = new PointDirect();
        double radians = angleR(from_x, from_y, to_x, to_y);
        result.setLocation(from_x + r * Math.cos(radians), from_y + r * Math.sin(radians));
        return result;
    }

    public static Point polar(Point from, double radians, double r) {
        return polar(from, radians, r, null);
    }

    public static Point polar(double fx, double fy, double radians, double r) {
        return polar(fx, fy, radians, r, null);
    }

    public static Point polar(Point from, double radians, double r, Point result) {
        if (result == null) result = new PointDirect();
        result.setLocation(from.getX() + r * Math.cos(radians), from.getY() + r * Math.sin(radians));
        return result;
    }

    public static Point polar(double fx, double fy, double radians, double r, Point result) {
        if (result == null) result = new PointDirect();
        result.setLocation(fx + r * Math.cos(radians), fy + r * Math.sin(radians));
        return result;
    }


    public static double angle(Point p, Point q) {
        return Math.toDegrees(angleR(p, q));
    }

    public static double angleR(Point p, Point q) {
        return Math.atan2(p.getY() - q.getY(), p.getX() - q.getX());
    }

    public static double angle(Point p, double x, double y) {
        return Math.toDegrees(angleR(p, x, y));
    }

    public static double angleR(Point p, double x, double y) {
        return Math.atan2(y - p.getY(), x - p.getX());
    }

    public static Point reflection(Point a, Point b) {
        return new PointDirect(a.getX() + a.getX() - b.getX(), a.getY() + a.getY() - b.getY()); //x + x - position.x, y + y - position.y
    }


    /**
     * Performs deCasteljau's algorithm for a bezier curve defined by the given control points.
     * <p>
     * A cubic for example requires four points. So it should get an array of 8 values
     *
     * @param controlpoints (x,y) coord list of the Bezier curve.
     * @param returnArray   Array to store the solved points. (can be null)
     * @param t             Amount through the curve we are looking at.
     * @return returnArray
     */
    public static float[] deCasteljau(float[] controlpoints, float[] returnArray, double t) {
        returnArray = deCasteljauEnsureCapacity(returnArray, controlpoints.length / 2);
        System.arraycopy(controlpoints, 0, returnArray, 0, controlpoints.length);
        return deCasteljau(returnArray, controlpoints.length / 2, t);
    }


    /**
     * Performs deCasteljau's algorithm for a bezier curve defined by the given control points.
     * <p>
     * A cubic for example requires four points. So it should get an array of 8 values
     *
     * @param array  (x,y) coord list of the Bezier curve, with needed interpolation space.
     * @param length Length of curve in points. 2 = Line, 3 = Quad 4 = Cubic, 5 = Quintic...
     * @param t      Amount through the curve we are looking at.
     * @return returnArray
     */
    public static float[] deCasteljau(float[] array, int length, double t) {
        int m = length * 2;
        int index = m; //start after the control points.
        int skip = m - 2; //skip if first compare is the last control position.
        array = deCasteljauEnsureCapacity(array, length);
        for (int i = 0, s = array.length - 2; i < s; i += 2) {
            if (i == skip) {
                m = m - 2;
                skip += m;
                continue;
            }
            array[index++] = (float) ((t * (array[i + 2] - array[i])) + array[i]);
            array[index++] = (float) ((t * (array[i + 3] - array[i + 1])) + array[i + 1]);
        }
        return array;
    }


    /**
     * Given controlpoints and the order, this function returns the subdivided curve and
     * the relevant data are the first (order + order - 1) datum. Additional space will have
     * been created and returned as working space for making the relevant curve.
     * <p>
     * Given 1, 2, 3 it will build
     * <p>
     * 6
     * 4 5
     * 1 2 3
     * <p>
     * And reorder that to return, 1 4 6 7 3.
     * [0, midpoint] are one curve.
     * [midpoint,end] are another curve.
     * <p>
     * Both curves reuse the midpoint.
     * <p>
     * UNTESTED!
     *
     * @param controlPoints at least order control points must be valid.
     * @param order         the cardinality of the curve.
     * @param t             the amount through that curve.
     * @return
     */
    public static float[] deCasteljauDivide(float[] controlPoints, int order, double t) {
        controlPoints = deCasteljau(controlPoints, order, t);
        int size = order + order - 1;
        int midpoint = order;
        int width = order;

        for (int r = 1, w = 0; w < size; w++) {
            if (r == midpoint) {
                width = order - 1;
                r = width;
            } else {
                r += width--;
                controlPoints[(w << 1)] = controlPoints[(r << 1)];
                controlPoints[(w << 1) + 1] = controlPoints[(r << 1) + 1];
            }
        }
        //reverse the second half values.
        int m = (size + midpoint) / 2;
        for (int i = midpoint * 2, s = size * 2; i < m; i += 2, s -= 2) {
            swapPoints(controlPoints, i, s);
        }
        return controlPoints;
    }

    public static float[] deCasteljauEnsureCapacity(float[] array, int order) {
        int sizeRequired = order * (order + 1); //equation converts to 2-float 1-position format.
        if (array == null) return new float[sizeRequired];
        if (sizeRequired > array.length) {
            return Arrays.copyOf(array, sizeRequired); //insure capacity
        }
        return array;
    }

    public static Point relative(Point point, double x, double y) {
        return new PointDirect(point.getX() + x, point.getY() + y);
    }


    public static void towardsCubic(double[] xy, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double t) {
        double x, y;
        x = (1 - t) * (1 - t) * (1 - t) * x0 + 3 * (1 - t) * (1 - t) * t * x1 + 3 * (1 - t) * t * t * x2 + t * t * t * x3;
        y = (1 - t) * (1 - t) * (1 - t) * y0 + 3 * (1 - t) * (1 - t) * t * y1 + 3 * (1 - t) * t * t * y2 + t * t * t * y3;
        xy[0] = x;
        xy[1] = y;
    }

    public static void towardsQuad(double[] xy, double x0, double y0, double x1, double y1, double x2, double y2, double t) {
        double x, y;
        x = (1 - t) * (1 - t) * x0 + 2 * (1 - t) * t * x1 + t * t * x2;
        y = (1 - t) * (1 - t) * y0 + 2 * (1 - t) * t * y1 + t * t * y2;
        xy[0] = x;
        xy[1] = y;
    }

    public static float[] getClosestWithinCurve(float[] controlpoints, int order, double x, double y, double threshold) {
        Queue<float[]> candidates = new ConcurrentLinkedQueue<>();
        candidates.add(controlpoints);
        double nearestPoint = Double.POSITIVE_INFINITY;
        float[] best = null;

        while (!candidates.isEmpty()) {
            float[] current = candidates.poll();
            double farPoint = Double.NEGATIVE_INFINITY;
            double nearPoint = Double.POSITIVE_INFINITY;
            for (int i = 0, s = order; i < s; i++) {
                double d = distanceSq(x, y, current[i << 1], current[(i << 1) + 1]);
                if (d > farPoint) {
                    farPoint = d;
                }
                if (d < nearPoint) {
                    nearPoint = d;
                }
            }
            if (farPoint < threshold) {
                threshold = farPoint;
            }
            if (nearPoint > threshold) {
                continue;
            }
            if (nearPoint < nearestPoint) {
                nearestPoint = nearPoint;
                best = controlpoints;
            }
            float[] firstcurve = new float[order * 2];
            float[] secondcurve = new float[order * 2];
            controlpoints = deCasteljauDivide(controlpoints, order, 0.5);
            System.arraycopy(controlpoints, 0, firstcurve, 0, firstcurve.length);
            System.arraycopy(controlpoints, firstcurve.length - 2, secondcurve, 0, secondcurve.length);
            candidates.add(firstcurve);
            candidates.add(secondcurve);
            if (threshold == 0) break;
        }
        return best;
    }

    public static double minDistanceSq(float[] points, double x, double y) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i += 2) {
            double d = distanceSq(x, y, points[i], points[i + 1]);
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

    public static int getIndexOfClosestPoint(PointsDirect points, double x, double y) {
        return getIndexOfClosestPoint(points, x, y, Float.POSITIVE_INFINITY);
    }

    public static int getIndexOfClosestPoint(PointsDirect points, double x, double y, double distancelimit) {
        return getIndexOfClosestPoint(points.pointlist, points.count, x, y, distancelimit);
    }

    public static double distanceSegment(Points points, int i) {
        return Math.sqrt(distanceSqSegment(points, i));
    }

    public static double distanceSqSegment(Points points, int i) {
        return distanceSqBetweenIndex(points, i, i + 1);
    }

    public static int getIndexOfClosestPoint(float[] pointlist, int count, double x, double y, double distancelimit) {
        int index = INVALID_POINT;
        double min = distancelimit * distancelimit, current;
        for (int i = 0; i < count; i += 2) {
            float px = pointlist[i];
            float py = pointlist[i + 1];
            current = distanceSq(px, py, x, y);
            if ((current < min) || ((current == min) && (index != 0))) { //give preference to equidistant endpoints, matters when they are coincident.
                min = current;
                index = (i >> 1);
            }
        }
        return index;
    }

    public static int getIndexOfClosestPoint(float[] pointlist, int count, float x, float y, int excludeStart, int excludeEnd) {
        int index = INVALID_POINT;
        double min = Double.POSITIVE_INFINITY, current;
        excludeStart <<= 1;
        excludeEnd <<= 1;
        for (int i = 0; i < count; i += 2) {
            if ((i >= excludeStart) && (i <= excludeEnd)) continue;
            float px = pointlist[i];
            float py = pointlist[i + 1];
            current = distanceSq(px, py, x, y);
            if (current <= min) {
                min = current;
                index = (i >> 1);
            }
        }
        return index;
    }

    public static int getIndexOfClosestSegment(PointsDirect points, float px, float py, double limit) {
        limit *= limit;
        int selected = INVALID_POINT;
        double minimum = limit;
        double distance;
        float previousx = Float.NaN;
        float previousy = Float.NaN;
        for (int i = 0, s = points.count; i < s; i += 2) {
            float currentx = points.pointlist[i];
            float currenty = points.pointlist[i + 1];
            if ((!Float.isNaN(previousx)) && (!Float.isNaN(previousy))) {
                distance = distanceSqFromSegment(px, py, previousx, previousy, currentx, currenty);
                if (distance < minimum) {
                    minimum = distance;
                    selected = i >> 1;
                }
            }
            previousx = currentx;
            previousy = currenty;
        }
        return selected - 1;
    }


    public static double distanceBetweenIndex(Points points, int p0, int p1) {
        return Math.sqrt(distanceSqBetweenIndex(points, p0, p1));
    }

    public static double distanceBetweenIndex(Points points, int p0, double x, double y) {
        return Math.sqrt(distanceSqBetweenIndex(points, p0, x, y));
    }

    public static double distanceSqBetweenIndex(Points points, int p0, double x, double y) {
        float x0 = points.getX(p0);
        float y0 = points.getY(p0);
        return distanceSq(x0, y0, x, y);
    }

    public static double distanceSqBetweenIndex(Points points, int p0, int p1) {
        if (p0 == p1) return 0;
        int size = points.size();
        if (p0 >= size || p1 >= size || p0 < 0 || p1 < 0) return 0;
        float x0 = points.getX(p0);
        float y0 = points.getY(p0);

        float x1 = points.getX(p1);
        float y1 = points.getY(p1);

        return distanceSq(x0, y0, x1, y1);
    }

    public static int getIndexOfClosestEndpoint(PointsDirect points, float x, float y, double rangeLimit) {
        if (points.count == 0) return INVALID_POINT;

        int index = INVALID_POINT;
        double min = rangeLimit * rangeLimit, current;

        float px = points.pointlist[0];
        float py = points.pointlist[1];

        current = distanceSq(px, py, x, y);
        if (current <= min) {
            min = current;
            index = 0;
        }
        px = points.pointlist[points.count - 2];
        py = points.pointlist[points.count - 1];

        current = distanceSq(px, py, x, y);
        if (current <= min) {
            //min = current;
            index = (points.count / 2) - 1;
        }
        return index;

    }

    public static double distanceBetweenSegmentAndPoint(Points points, int segmentIndex, double x, double y) {
        return Math.sqrt(distanceSqFromSegment((float) x, (float) y, points.getX(segmentIndex), points.getY(segmentIndex), points.getX(segmentIndex + 1), points.getY(segmentIndex + 1)));
    }

    public static double distanceSqBetweenSegmentAndPoint(Points points, int segmentIndex, double x, double y) {
        return distanceSqFromSegment((float) x, (float) y, points.getX(segmentIndex), points.getY(segmentIndex), points.getX(segmentIndex + 1), points.getY(segmentIndex + 1));
    }

    /**
     * @param x
     * @param y
     * @return theoretical minimum distance that given position must be from any position in the pointlist.
     * <p>
     * No position in the pointlist can be closer than the returned distance.
     * <p>
     * Given the internal maintained bounds of the pointlist, a position outside those bounds must be some distance
     * from the closest position. Using only the bounds this function determines the mathematically required minimum
     * from the given position to the bounds.
     */

    public static double distanceQuickFail(PointsDirect points, float x, float y) {
        points.computeBounds(false); //If the bounds in excessive, too bad. Do not recalculate.
        switch ((points.maxX < x ? 0b1000 : 0) + (points.maxY < y ? 0b0100 : 0) + (points.minX > x ? 0b0010 : 0) + (points.minY > y ? 0b0001 : 0)) {
            case 0b000: //Point is within bounds.
                return 0;
            case 0b0001: //Point is above the top.
                return points.minY - y;
            case 0b0010: //Point is to left of left.
                return points.minX - x;
            case 0b0011: //Point is both left of leftmost and above the top.
                return distance(x, y, points.minX, points.minY);
            case 0b0100: //Point is below the bottom
                return y - points.maxY;
            case 0b0110: //Point is below the bottom and to the left of left.
                return distance(x, y, points.minX, points.maxY);
            case 0b1000: //Point is right of rightmost.
                return x - points.maxX;
            case 0b1001: //Point is right of rightmost and above the top.
                return distance(x, y, points.maxX, points.minY);
            case 0b1100: //Point is right of rightmost and below the bottom.
                return distance(x, y, points.maxX, points.maxY);
        }
        return Double.POSITIVE_INFINITY;
    }
    
    //ANDROID:
//
//    public static void union(PointsDirect points, RectF bounds) {
//        points.computeBounds(true);
//        bounds.union(points.minX, points.minY, points.maxX, points.maxY);
//    }
//
//    public static void getBounds(PointsDirect points, RectF bounds) {
//        points.computeBounds(true);
//        bounds.set(points.minX, points.minY, points.maxX, points.maxY);
//    }
//
//    /**
//     * Sets the bounds to the intersection, even if they do not intersect.
//     *
//     * @param bounds rectangle that the pointlist's bounds should be intersected with.
//     */
//    public static void intersect(PointsDirect points, RectF bounds) {
//        points.computeBounds(true);
//        bounds.set(Math.max(bounds.left, points.minX), Math.max(bounds.top, points.minY),
//                Math.min(bounds.right, points.maxX), Math.min(bounds.bottom, points.maxY));
//    }

    public static double getClosestWithinCurve(Curve element, double x, double y) {
        //TODO: Replace this with the complex polygon hull subdivision algorithm.
        Point n0, n1, n2, n3;
        double t = 0;

        switch (element.size()) {
            case 0:
                return -1;
            case 1:
                return -1;
            case 2:
                n0 = element.getPoint(0);
                n1 = element.getPoint(1);
                t = amountThroughSegment(x, y, n0.getX(), n0.getY(), n1.getX(), n1.getY());
                return t;
            case 3:
                n0 = element.getPoint(0);
                n1 = element.getPoint(1);
                n2 = element.getPoint(2);
                t = getClosestPointToQuadBezier(x, y, 8, 3, n0.getX(), n0.getY(), n1.getX(), n1.getY(), n2.getX(), n2.getY());
                return t;
            case 4:
                n0 = element.getPoint(0);
                n1 = element.getPoint(1);
                n2 = element.getPoint(2);
                n3 = element.getPoint(3);
                t = getClosestPointToCubicBezier(x, y, 9, 3, n0.getX(), n0.getY(), n1.getX(), n1.getY(), n2.getX(), n2.getY(), n3.getX(), n3.getY());
                return t;
        }
        return t;
    }

    public static Point getClosestPointWithinCurve(Curve element, double x, double y, double distance) {
        double[] values = new double[4];
        Point n0, n1, n2, n3;
        double t = 0;
        double bx = 0, by = 0;

        switch (element.size()) {
            case 0:
                return null;
            case 1:
                return null;
            case 2:
                n0 = element.getPoint(0);
                n1 = element.getPoint(1);
                t = amountThroughSegment(x, y, n0.getX(), n0.getY(), n1.getX(), n1.getY());
                bx = towards(n0.getX(), n1.getX(), t);
                by = towards(n0.getY(), n1.getY(), t);
                break;
            case 3:
                n0 = element.getPoint(0);
                n1 = element.getPoint(1);
                n2 = element.getPoint(2);
                t = getClosestPointToQuadBezier(x, y, 8, 3, n0.getX(), n0.getY(), n1.getX(), n1.getY(), n2.getX(), n2.getY());
                towardsQuad(values, n0.getX(), n0.getY(), n1.getX(), n1.getY(), n2.getX(), n2.getY(), t);
                bx = values[0];
                by = values[1];
                break;
            case 4:
                n0 = element.getPoint(0);
                n1 = element.getPoint(1);
                n2 = element.getPoint(2);
                n3 = element.getPoint(3);
                t = getClosestPointToCubicBezier(x, y, 9, 3, n0.getX(), n0.getY(), n1.getX(), n1.getY(), n2.getX(), n2.getY(), n3.getX(), n3.getY());
                towardsCubic(values, n0.getX(), n0.getY(), n1.getX(), n1.getY(), n2.getX(), n2.getY(), n3.getX(), n3.getY(), t);
                bx = values[0];
                by = values[1];
                break;

        }
        return new PointDirect(bx, by);
    }

    public static double getClosestPointToCubicBezier(double fx, double fy, int slices, int iterations, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        return getClosestPointToCubicBezier(iterations, fx, fy, 0, 1d, slices, x0, y0, x1, y1, x2, y2, x3, y3);
    }

    private static double getClosestPointToCubicBezier(int iterations, double fx, double fy, double start, double end, int slices, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        if (iterations <= 0) return (start + end) / 2;
        double tick = (end - start) / (double) slices;
        double x, y, dx, dy;
        double best = 0;
        double bestDistance = Double.POSITIVE_INFINITY;
        double currentDistance;
        double t = start;
        while (t <= end) {
            //B(t) = (1-t)**3 p0 + 3(1 - t)**2 t P1 + 3(1-t)t**2 P2 + t**3 P3
            x = (1 - t) * (1 - t) * (1 - t) * x0 + 3 * (1 - t) * (1 - t) * t * x1 + 3 * (1 - t) * t * t * x2 + t * t * t * x3;
            y = (1 - t) * (1 - t) * (1 - t) * y0 + 3 * (1 - t) * (1 - t) * t * y1 + 3 * (1 - t) * t * t * y2 + t * t * t * y3;

            //x = (1 - t) * (1 - t) * x0 + 2 * (1 - t) * t * x1 + t * t * x2; //quad.
            //y = (1 - t) * (1 - t) * y0 + 2 * (1 - t) * t * y1 + t * t * y2; //quad.
            dx = x - fx;
            dy = y - fy;
            dx *= dx;
            dy *= dy;
            currentDistance = dx + dy;
            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                best = t;
            }
            t += tick;
        }
        return getClosestPointToCubicBezier(iterations - 1, fx, fy, Math.max(best - tick, 0d), Math.min(best + tick, 1d), slices, x0, y0, x1, y1, x2, y2, x3, y3);
    }

    public static double getClosestPointToQuadBezier(double fx, double fy, int slices, int iterations, double x0, double y0, double x1, double y1, double x2, double y2) {
        return getClosestPointToQuadBezier(iterations, fx, fy, 0, 1d, slices, x0, y0, x1, y1, x2, y2);
    }

    private static double getClosestPointToQuadBezier(int iterations, double fx, double fy, double start, double end, int slices, double x0, double y0, double x1, double y1, double x2, double y2) {
        if (iterations <= 0) return (start + end) / 2;
        double tick = (end - start) / (double) slices;
        double x, y, dx, dy;
        double best = 0;
        double bestDistance = Double.POSITIVE_INFINITY;
        double currentDistance;
        double t = start;
        while (t <= end) {
            x = (1 - t) * (1 - t) * x0 + 2 * (1 - t) * t * x1 + t * t * x2; //quad.
            y = (1 - t) * (1 - t) * y0 + 2 * (1 - t) * t * y1 + t * t * y2; //quad.
            dx = x - fx;
            dy = y - fy;
            dx *= dx;
            dy *= dy;
            currentDistance = dx + dy;
            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                best = t;
            }
            t += tick;
        }
        return getClosestPointToQuadBezier(iterations - 1, fx, fy, Math.max(best - tick, 0d), Math.min(best + tick, 1d), slices, x0, y0, x1, y1, x2, y2);
    }


    public static double amountThroughSegment(double px, double py, double ax, double ay, double bx, double by) {
        double vAPx = px - ax;
        double vAPy = py - ay;
        double vABx = bx - ax;
        double vABy = by - ay;
        double sqDistanceAB = vABx * vABx + vABy * vABy; //a.distanceSq(b);
        double ABAPproduct = vABx * vAPx + vABy * vAPy;
        double amount = ABAPproduct / sqDistanceAB;
        if (amount > 1) amount = 1;
        if (amount < 0) amount = 0;
        return amount;
    }

    public static double distanceSqFromSegment(double px, double py, double ax, double ay, double bx, double by) {
        double amount = amountThroughSegment(px, py, ax, ay, bx, by);
        double qx = (amount * (bx - ax)) + ax;
        double qy = (amount * (by - ay)) + ay;
        double dx = px - qx;
        double dy = py - qy;
        dx *= dx;
        dy *= dy;
        return dx + dy;
    }


    /**
     * Elliptical arc implementation based on the SVG specification notes
     * Adapted from the Batik library (Apache-2 license) by SAU
     */
    public static float[][] convertArcToCubicCurves(double x0, double y0, double x, double y, double rx, double ry,
                                                    double rotateAngleDegrees, boolean largeArcFlag, boolean sweepFlag) {
        return convertArcToCubicCurves(x0, y0, x, y, rx, ry, rotateAngleDegrees, largeArcFlag, sweepFlag, Math.PI / 2);
    }

    public static float[][] convertArcToCubicCurves(double x0, double y0, double x, double y, double rx, double ry,
                                                    double rotateAngleDegrees, boolean largeArcFlag, boolean sweepFlag, double sweepLimit) {
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        double rotateAngleRadians = Math.toRadians(rotateAngleDegrees % 360.0);
        double cosAngle = Math.cos(rotateAngleRadians);
        double sinAngle = Math.sin(rotateAngleRadians);

        double x1 = (cosAngle * dx2 + sinAngle * dy2);
        double y1 = (-sinAngle * dx2 + cosAngle * dy2);
        rx = Math.abs(rx);
        ry = Math.abs(ry);

        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;

        // check that radii are large enough
        double radiiCheck = Px1 / Prx + Py1 / Pry;
        if (radiiCheck > 1) {
            rx = Math.sqrt(radiiCheck) * rx;
            ry = Math.sqrt(radiiCheck) * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        // Step 2 : Compute (cx1, cy1)
        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
                / ((Prx * Py1) + (Pry * Px1));
        sq = (sq < 0) ? 0 : sq;
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;

        // Compute the angle start
        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1.0 : 1.0;
        double startAngle = sign * Math.acos(p / n);

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
        double sweepAngle = sign * Math.acos(p / n);
        if (!sweepFlag && sweepAngle > 0) {
            sweepAngle -= TAU;
        } else if (sweepFlag && sweepAngle < 0) {
            sweepAngle += TAU;
        }
        sweepAngle %= TAU;
        startAngle %= TAU;

        // Add the curve sections.
        int arcRequired = ((int) (Math.ceil(Math.abs(sweepAngle) / sweepLimit)));

        float[][] curves = new float[arcRequired][];

        double slice = sweepAngle / (double) arcRequired;

        for (int i = 0, m = curves.length; i < m; i++) {
            double sAngle = (i * slice) + startAngle;
            double eAngle = ((i + 1) * slice) + startAngle;
            curves[i] = convertArcToCurve(null, rotateAngleRadians, sAngle, eAngle, cx, cy, rx, ry);
        }
        return curves;

    }

    public static float[] convertArcToCurve(float[] curve, double theta, double startAngle, double endAngle, double x0, double y0, double rx, double ry) {
        if ((curve == null) || (curve.length > 8)) curve = new float[8];
        double slice = endAngle - startAngle;

        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        double p1En1x, p1En1y;
        double p2En2x, p2En2y;
        double ePrimen1x, ePrimen1y;
        double ePrimen2x, ePrimen2y;

        double alpha = Math.sin(slice) * (Math.sqrt(4 + 3 * Math.pow(Math.tan((slice) / 2), 2)) - 1) / 3;

        double cosStartAngle, sinStartAngle;
        cosStartAngle = Math.cos(startAngle);
        sinStartAngle = Math.sin(startAngle);

        p1En1x = x0 + rx * cosStartAngle * cosTheta - ry * sinStartAngle * sinTheta;
        p1En1y = y0 + rx * cosStartAngle * sinTheta + ry * sinStartAngle * cosTheta;

        ePrimen1x = -rx * cosTheta * sinStartAngle - ry * sinTheta * cosStartAngle;
        ePrimen1y = -rx * sinTheta * sinStartAngle + ry * cosTheta * cosStartAngle;

        double cosEndAngle, sinEndAngle;
        cosEndAngle = Math.cos(endAngle);
        sinEndAngle = Math.sin(endAngle);

        p2En2x = x0 + rx * cosEndAngle * cosTheta - ry * sinEndAngle * sinTheta;
        p2En2y = y0 + rx * cosEndAngle * sinTheta + ry * sinEndAngle * cosTheta;

        ePrimen2x = -rx * cosTheta * sinEndAngle - ry * sinTheta * cosEndAngle;
        ePrimen2y = -rx * sinTheta * sinEndAngle + ry * cosTheta * cosEndAngle;

        curve[0] = (float) p1En1x;
        curve[1] = (float) p1En1y;
        curve[2] = (float) (p1En1x + alpha * ePrimen1x);
        curve[3] = (float) (p1En1y + alpha * ePrimen1y);
        curve[4] = (float) (p2En2x - alpha * ePrimen2x);
        curve[5] = (float) (p2En2y - alpha * ePrimen2y);
        curve[6] = (float) p2En2x;
        curve[7] = (float) p2En2y;
        return curve;
    }


    /**
     * Elliptical arc implementation based on the SVG specification notes
     * Adapted from the Batik library (Apache-2 license) by SAU
     */
    public static float[] convertArcToPoints(double x0, double y0, double x, double y, double rx, double ry,
                                             double rotateAngleDegrees, boolean largeArcFlag, boolean sweepFlag, int interpolatedPoints) {
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        double rotateAngleRadians = Math.toRadians(rotateAngleDegrees % 360.0);
        double cosAngle = Math.cos(rotateAngleRadians);
        double sinAngle = Math.sin(rotateAngleRadians);

        double x1 = (cosAngle * dx2 + sinAngle * dy2);
        double y1 = (-sinAngle * dx2 + cosAngle * dy2);
        rx = Math.abs(rx);
        ry = Math.abs(ry);

        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;

        // check that radii are large enough
        double radiiCheck = Px1 / Prx + Py1 / Pry;
        if (radiiCheck > 1) {
            rx = Math.sqrt(radiiCheck) * rx;
            ry = Math.sqrt(radiiCheck) * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        // Step 2 : Compute (cx1, cy1)
        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
                / ((Prx * Py1) + (Pry * Px1));
        sq = (sq < 0) ? 0 : sq;
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;

        // Compute the angle start
        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1.0 : 1.0;
        double startAngle = sign * Math.acos(p / n);

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
        double sweepAngle = sign * Math.acos(p / n);
        if (!sweepFlag && sweepAngle > 0) {
            sweepAngle -= TAU;
        } else if (sweepFlag && sweepAngle < 0) {
            sweepAngle += TAU;
        }
        sweepAngle %= TAU;
        startAngle %= TAU;

        //Add Segments.
        double slice = Math.toRadians(sweepAngle) / (double) interpolatedPoints;

        double t;
        double cosT, sinT;
        float[] points = new float[2 * (2 + interpolatedPoints)];
        points[0] = (float) x0;
        points[1] = (float) y0;
        for (int i = 1; i < interpolatedPoints - 1; i++) {
            t = (i * slice) + startAngle;
            cosT = Math.cos(t);
            sinT = Math.sin(t);
            double px = cx + rx * cosT * cosAngle - ry * sinT * sinAngle;
            double py = cy + rx * cosT * sinAngle + ry * sinT * cosAngle;
            int m = i * 2;
            points[m] = (float) px;
            points[m + 1] = (float) py;
        }
        points[points.length - 2] = (float) x;
        points[points.length - 1] = (float) y;
        return points;
    }

    public static float[] convertArcToPoints(double left, double top, double right, double bottom, double startAngle, float sweepAngle, double theta, int interpolatedPoints) {
        double slice = Math.toRadians(sweepAngle) / (double) (interpolatedPoints - 1);
        double cx = (left + right) / 2;
        double cy = (top + bottom) / 2;

        double rx = (right - left) / 2;
        double ry = (bottom - top) / 2;
        startAngle = Math.toRadians(startAngle);
        theta = Math.toRadians(theta);

        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        double t;
        double cosT, sinT;
        float[] points = new float[2 * interpolatedPoints];
        for (int i = 0; i < interpolatedPoints; i++) {
            t = (i * slice) + startAngle;
            cosT = Math.cos(t);
            sinT = Math.sin(t);
            double px = cx + rx * cosT * cosTheta - ry * sinT * sinTheta;
            double py = cy + rx * cosT * sinTheta + ry * sinT * cosTheta;
            int m = i * 2;
            points[m] = (float) px;
            points[m + 1] = (float) py;
        }
        return points;
    }


    public static void swapPoints(float[] pointlist, int xindex0, int xindex1) {
        float tx, ty;
        tx = pointlist[xindex0];
        ty = pointlist[xindex0 + 1];
        pointlist[xindex0] = pointlist[xindex1];
        pointlist[xindex0 + 1] = pointlist[xindex1 + 1];
        pointlist[xindex1] = tx;
        pointlist[xindex1 + 1] = ty;
    }

    public static float[] reverse(float[] pointlist) {
        reverse(pointlist, pointlist.length);
        return pointlist;
    }

    public static float[] reverse(float[] pointlist, int start, int end) {
        int m = (start + end) / 2;
        for (int i = start, s = end; i < m; i += 2, s -= 2) {
            swapPoints(pointlist, i, s);
        }
        return pointlist;
    }

    public static float[] reverse(float[] pointlist, int count) {
        int m = count / 2;
        for (int i = 0, s = count - 2; i < m; i += 2, s -= 2) {
            swapPoints(pointlist, i, s);
        }
        return pointlist;
    }

    public static float[] segmentLine(float... values) {
        return values;
    }

    public static float[] segmentLine(Point center, float x, float y) {
        return new float[]{(float) center.getX(), (float) center.getY(), x, y};
    }

    public static int binarySearchX(Points points, double x) {
        return binarySearchX(points, 0, points.size(), x);
    }

    public static int binarySearchX(Points points, int fromIndex, int toIndex, double x) {
        int low = fromIndex;
        int high = toIndex - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = points.getX(mid);
            int cmp = Double.compare(midVal, x);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    public static int binarySearchY(Points points, double y) {
        return binarySearchY(points, 0, points.size(), y);
    }

    public static int binarySearchY(Points points, int fromIndex, int toIndex, double y) {
        int low = fromIndex;
        int high = toIndex - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = points.getY(mid);
            int cmp = Double.compare(midVal, y);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    public static int shortDrop(PointsDirect points, double minDistance, int index) {
        float[] pointlist = points.pointlist;
        int count = points.count;
        if (count <= 2) return index;
        double minDistanceSq = minDistance * minDistance;
        int arrayIndex = index << 1;

        float sx = pointlist[0];
        float sy = pointlist[1];
        float ex, ey;
        int positionSegmentStart = 0;

        boolean dropped = false;
        for (int positionSegmentEnd = 2, s = count; positionSegmentEnd < s; positionSegmentEnd += 2) {
            if ((positionSegmentStart == 0) && (positionSegmentEnd == (count - 2))) break;
            ex = pointlist[positionSegmentEnd];
            ey = pointlist[positionSegmentEnd + 1];
            if (distanceSq(sx, sy, ex, ey) < minDistanceSq) {
                if (positionSegmentEnd == arrayIndex) {
                    points.setNan(positionSegmentStart >> 1);
                } else {
                    points.setNan(positionSegmentEnd >> 1);
                }
                dropped = true;
            } else {
                sx = ex;
                sy = ey;
                positionSegmentStart = positionSegmentEnd;
            }
        }
        if (dropped) return nanDrop(points, index);
        return index;
    }

    public static int nanDrop(PointsDirect points, int index) {
        float[] pointlist = points.pointlist;
        int count = points.count;
        int arrayIndex = index << 1;
        int returnIndex = index;
        int validPosition = 0;

        float px, py;
        for (int pos = 0; pos < count; pos += 2) {
            if (pos == arrayIndex) returnIndex = validPosition >> 1;

            px = pointlist[pos];
            py = pointlist[pos + 1];

            if (!Float.isNaN(px)) {
                pointlist[validPosition] = px;
                pointlist[validPosition + 1] = py;
                validPosition += 2;
            }

        }
        points.count = validPosition;
        return returnIndex;
    }

    public static int longSplit(PointsDirect points, double maxDistance, int index) {
        float[] pointlist = points.pointlist;
        int count = points.count;
        if (count <= 2) return index;
        double maxDistanceSq = maxDistance * maxDistance;
        double lineDistance;
        int splits = 0;

        float ex1 = pointlist[count - 2];
        float ey1 = pointlist[count - 1];
        float sx1, sy1;

        for (int pos = count - 2; pos > 1; pos -= 2) {
            sx1 = pointlist[pos - 2];
            sy1 = pointlist[pos - 1];
            lineDistance = distanceSq(sx1, sy1, ex1, ey1);
            if (lineDistance > maxDistanceSq) {
                lineDistance = Math.sqrt(lineDistance);
                int breaks1 = (int) (Math.ceil(lineDistance / maxDistance)) - 1;
                splits += breaks1;
            }
            ex1 = sx1;
            ey1 = sy1;
        }
        if (splits == 0) {
            return index;
        }

        int splitCount = (splits * 2) + count;
        points.ensureCapacity(splitCount);
        pointlist = points.pointlist;

        int writePos = splitCount;
        float ex = pointlist[count - 2];
        float ey = pointlist[count - 1];
        float sx, sy;

        int returnIndex = (writePos) >> 1;

        pointlist[writePos - 2] = ex;
        pointlist[writePos - 1] = ey;
        writePos -= 2;
        int relativeIndex = index << 1;

        for (int readPos = count - 2; readPos > 1; readPos -= 2) {
            if (readPos == relativeIndex) {
                returnIndex = (writePos) >> 1;
            }
            sx = pointlist[readPos - 2];
            sy = pointlist[readPos - 1];
            lineDistance = distanceSq(sx, sy, ex, ey);
            if (lineDistance > maxDistanceSq) {
                lineDistance = Math.sqrt(lineDistance);
                int breaks = (int) (Math.ceil(lineDistance / maxDistance)) - 1;

                float stepX = (ex - sx) / (breaks + 1);
                float stepY = (ey - sy) / (breaks + 1);

                for (int q = breaks; q >= 0; q--) {
                    pointlist[writePos - 2] = sx + (stepX * q);
                    pointlist[writePos - 1] = sy + (stepY * q);
                    writePos -= 2;
                }
            } else {
                pointlist[writePos - 2] = sx;
                pointlist[writePos - 1] = sy;
                writePos -= 2;
            }
            ex = sx;
            ey = sy;
        }
        points.count = splitCount;
        if (relativeIndex == 0) return 0;
        return returnIndex;
    }

    public static List<PointsDirect> nanDelinated(PointsDirect layer) {
        return nanDelinated(layer, null);
    }

    public static List<PointsDirect> nanDelinated(PointsDirect series, ArrayList<PointsDirect> delinated) {
        if (delinated == null) delinated = new ArrayList<>();
        int from = -1;
        int to = -1;
        PointsDirect psadd;
        for (int i = 0, m = series.size(); i < m; i++) {
            float px = series.getX(i);
            if (!Float.isNaN(px)) {
                if (from == -1) {
                    from = i;
                }
                to = i;
            } else {
                if (from != to) {
                    psadd = new PointsDirect();
                    psadd.setPack(series.subList(from, to + 1));
                    delinated.add(psadd);
                }
                from = -1;
                to = -1;
            }
        }
        if (from != to) { //-1 or 1 position layers are skipped.
            psadd = new PointsDirect();
            psadd.setPack(series.subList(from, to + 1));
            delinated.add(psadd);
        }
        return delinated;
    }

    public static void snap(Points layer) {
        for (int i = 0, size = layer.size(); i < size; i++) {
            layer.setLocation(i, (float) Math.rint(layer.getX(i)), (float) Math.rint(layer.getY(i)));
        }
    }

    public static Point lineIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            return new PointDirect((int) (x1 + ua * (x2 - x1)), (int) (y1 + ua * (y2 - y1)));
        }
        return null;
    }

    public static Point lineRayIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f) { //intersection within b can be beyond the bounds.
            return new PointDirect((int) (x1 + ua * (x2 - x1)), (int) (y1 + ua * (y2 - y1)));
        }
        return null;
    }
}
