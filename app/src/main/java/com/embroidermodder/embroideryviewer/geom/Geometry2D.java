package com.embroidermodder.embroideryviewer.geom;

import java.util.Arrays;
import java.util.List;

/**
 * Static functions of various utility. There's a lot of reusable code and it's mostly put in here for geometry related
 * elements like distances etc.
 *
 */

public class Geometry2D {
    public static final double TAU = Math.PI * 2;

    static public Point towards(Point from, Point to, double amount, Point result) {
        if (result == null) {
            result = new PointDirect();
        }
        double nx = (amount * (to.getX() - from.getX())) + from.getX();
        double ny = (amount * (to.getY() - from.getY())) + from.getY();
        result.setLocation(nx, ny);
        return result;

    }

    public static Point midPoint(Point point0, Point point1, Point result) {
        if (result == null) {
            result = new PointDirect();
        }
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

    public static Point polar(Point from, double degrees, double r, Point result) {
        if (result == null) {
            result = new PointDirect();
        }
        double radians = Math.toRadians(degrees);
        result.setLocation(from.getX() + r * Math.cos(radians), from.getY() + r * Math.sin(radians));
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
     * Performs deCasteljau's algorithm for a bezier curve defined by the given
     * control points.
     * <p>
     * A cubic for example requires four points. So it should get an array of 8
     * values
     *
     * @param controlpoints (x,y) coord list of the Bezier curve.
     * @param returnArray Array to store the solved points. (can be null)
     * @param t Amount through the curve we are looking at.
     * @return returnArray
     */
    public static float[] deCasteljau(float[] controlpoints, float[] returnArray, double t) {
        returnArray = deCasteljauEnsureCapacity(returnArray, controlpoints.length / 2);
        System.arraycopy(controlpoints, 0, returnArray, 0, controlpoints.length);
        return deCasteljau(returnArray, controlpoints.length / 2, t);
    }

    /**
     * Performs deCasteljau's algorithm for a bezier curve defined by the given
     * control points.
     * <p>
     * A cubic for example requires four points. So it should get an array of 8
     * values
     *
     * @param array (x,y) coord list of the Bezier curve, with needed
     * interpolation space.
     * @param length Length of curve in points. 2 = Line, 3 = Quad 4 = Cubic, 5
     * = Quintic...
     * @param t Amount through the curve we are looking at.
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

    public static float[] deCasteljauEnsureCapacity(float[] array, int order) {
        int sizeRequired = order * (order + 1); //equation converts to 2-float 1-position format.
        if (array == null) {
            return new float[sizeRequired];
        }
        if (sizeRequired > array.length) {
            return Arrays.copyOf(array, sizeRequired); //insure capacity
        }
        return array;
    }

    public static Point relative(Point point, double x, double y) {
        return new PointDirect(point.getX() + x, point.getY() + y, point.data());
    }

    /**
     * Performs the dot product to find the amount through the segment that is closest to the point given.
     * @param px point x
     * @param py point y
     * @param ax segment start x
     * @param ay segment start y
     * @param bx segment end x
     * @param by segment end y
     * @return between 0-1 the amount through the segment closest to point
     */
    public static double amountThroughSegment(double px, double py, double ax, double ay, double bx, double by) {
        double vAPx = px - ax;
        double vAPy = py - ay;
        double vABx = bx - ax;
        double vABy = by - ay;
        double sqDistanceAB = vABx * vABx + vABy * vABy; //a.distanceSq(b);
        double ABAPproduct = vABx * vAPx + vABy * vAPy;
        double amount = ABAPproduct / sqDistanceAB;
        if (amount > 1) {
            amount = 1;
        }
        if (amount < 0) {
            amount = 0;
        }
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


    /**
     * Effectively approximate a curve with cubic curves.
     * @param x0 start x of arc
     * @param y0 start y of arc
     * @param x end x of arc
     * @param y end y of arc.
     * @param rx the radius x
     * @param ry the radius y
     * @param rotateAngleDegrees the theta rotation of the arc.
     * @param largeArcFlag large arc or small arc
     * @param sweepFlag the sweep flag for the arc (see SVG documentation etc)
     * @param sweepLimit the limit of sweep before the arc must be represented by a curve.
     * @return array of float arrays of cubic curves (end point, control point, control point, final end point)
     */
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

    /**
     * Convert an arc to a single curve.
     *
     * @param curve return value array
     * @param theta rotation of the arc.
     * @param startAngle starting angle for the arc
     * @param endAngle ending angel for the arc.
     * @param x0 arc center x.
     * @param y0 arc center y.
     * @param rx radius x
     * @param ry radius y.
     * @return single cubic curve. float[8] or the curve object given with the first 8 values used.
     */
    public static float[] convertArcToCurve(float[] curve, double theta, double startAngle, double endAngle, double x0, double y0, double rx, double ry) {
        if ((curve == null) || (curve.length > 8)) {
            curve = new float[8];
        }
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
     *
     * Convert svg style arc object into a given number of points to best approximate it.
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

    /**
     * Convert bounding rectangle style arc object into a given number of points to best approximate it.
     */
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

    public static void swapPoints(float[] pointlist, int index0, int index1) {
        float tx, ty;
        tx = pointlist[index0];
        ty = pointlist[index0 + 1];
        pointlist[index0] = pointlist[index1];
        pointlist[index0 + 1] = pointlist[index1 + 1];
        pointlist[index1] = tx;
        pointlist[index1 + 1] = ty;
    }

    public static void reverse(float[] pointlist) {
        reverse(pointlist, pointlist.length);
    }

    public static void reverse(float[] pointlist, int count) {
        int m = count / 2;
        for (int i = 0, s = count - 2; i < m; i += 2, s -= 2) {
            swapPoints(pointlist, i, s);
        }
    }

    public static float[] segmentLine(float... values) {
        return values;
    }

    public static float[] segmentLine(Point center, float x, float y) {
        return new float[]{(float) center.getX(), (float) center.getY(), x, y};
    }

    public static int shortDrop(PointsDirect points, double minDistance, int index) {
        float[] pointlist = points.pointlist;
        int count = points.count;
        if (count <= 2) {
            return index;
        }
        double minDistanceSq = minDistance * minDistance;
        int arrayIndex = index << 1;

        float sx = pointlist[0];
        float sy = pointlist[1];
        float ex, ey;
        int positionSegmentStart = 0;

        boolean dropped = false;
        for (int positionSegmentEnd = 2, s = count; positionSegmentEnd < s; positionSegmentEnd += 2) {
            if ((positionSegmentStart == 0) && (positionSegmentEnd == (count - 2))) {
                break;
            }
            ex = pointlist[positionSegmentEnd];
            ey = pointlist[positionSegmentEnd + 1];
            if (Geometry2D.distanceSq(sx, sy, ex, ey) < minDistanceSq) {
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
        if (dropped) {
            return nanDrop(points, index);
        }
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
            if (pos == arrayIndex) {
                returnIndex = validPosition >> 1;
            }

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

    /**
     * Iterates the points allocating new data only once while splitting the log segments.
     *
     * @param points PointsDirect object with points to split.
     * @param maxDistance the distance beyond which points must be subdivided.
     * @param index the index to be tracked while the process occurs.
     * @return the resulting index of the tracked index after the long segments are subdivided.
     */
    public static int longSplit(PointsDirect points, double maxDistance, int index) {
        float[] pointlist = points.pointlist;
        int count = points.count;
        if (count <= 2) {
            return index;
        }
        double maxDistanceSq = maxDistance * maxDistance;
        double lineDistance;
        int splits = 0;

        float ex1 = pointlist[count - 2];
        float ey1 = pointlist[count - 1];
        float sx1, sy1;

        for (int pos = count - 2; pos > 1; pos -= 2) {
            sx1 = pointlist[pos - 2];
            sy1 = pointlist[pos - 1];
            lineDistance = Geometry2D.distanceSq(sx1, sy1, ex1, ey1);
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
            lineDistance = Geometry2D.distanceSq(sx, sy, ex, ey);
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
        if (relativeIndex == 0) {
            return 0;
        }
        return returnIndex;
    }

    public static void snap(Points layer) {
        for (int i = 0, size = layer.size(); i < size; i++) {
            layer.setLocation(i, (float) Math.rint(layer.getX(i)), (float) Math.rint(layer.getY(i)));
        }
    }
}
