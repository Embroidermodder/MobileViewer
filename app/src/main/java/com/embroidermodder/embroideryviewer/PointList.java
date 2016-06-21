package com.embroidermodder.embroideryviewer;

import android.graphics.Matrix;

import java.util.Arrays;

/**
 * Created by Tat on 8/2/2015.
 * Derived from proprietary code, 6/21/2016.
 * Released under EmbroiderModder/MobileView licensing. 6/21/2016.
 *
 * The core importance of such a class is to allow for speed with regard to Android.
 * The canvas can very quickly render segments and with the system setup as such,
 * A pointlist can be rendered in two canvas calls, and the underlying memory is
 * maximally compact.
 *
 *         if (count >= 4) {
 *                  if ((count & 2) != 0) {
 *                          canvas.drawLines(pointlist, 0, count - 2, paint);
 *                          canvas.drawLines(pointlist, 2, count - 2, paint);
 *                  } else {
 *                          canvas.drawLines(pointlist, 0, count, paint);
 *                          canvas.drawLines(pointlist, 2, count - 4, paint);
 *                  }
 *         }
 *
 * This class can easily allow for 50,000+ stitch projects to be run with proper speed on an android device.
 */

public class PointList {
    public static final int INVALID_POINT = -1;
    protected float[] pointlist;
    protected int count;

    private float minX;
    private float minY;
    private float maxX;
    private float maxY;
    protected boolean dirtybounds;
    protected boolean excessbounds;

    private static final int MIN_CAPACITY_INCREMENT = 12;

    public PointList() {
        pointlist = new float[MIN_CAPACITY_INCREMENT * 2];
        count = 0;
        resetBounds();
    }

    public PointList(float[] pack) {
        this();
        add(0, pack);
    }

    public PointList(PointList p) {
        pointlist = p.pack();
        count = p.count();
        dirtybounds = true;
    }

    private static int newCapacity(int currentCapacity) {
        int increment = (currentCapacity < (MIN_CAPACITY_INCREMENT / 2) ?
                MIN_CAPACITY_INCREMENT : currentCapacity >> 1);
        return currentCapacity + (increment << 1);
    }

    public void ensureCapacity(int capacity) {
        if (pointlist.length > capacity) return;
        int newCapacity = newCapacity(capacity);
        float[] newPointList = new float[newCapacity];
        System.arraycopy(pointlist, 0, newPointList, 0, count);
        pointlist = newPointList;
    }

    public final void add(float px, float py) {
        ensureCapacity(count + 2);
        pointlist[count] = px;
        pointlist[count + 1] = py;
        count += 2;
        checkBounds(px, py);
    }

    public final void add(int index, float px, float py) {
        ensureCapacity(count + 2);
        index <<= 1;
        System.arraycopy(pointlist, index, pointlist, index + 2, count - index);
        pointlist[index] = px;
        pointlist[index + 1] = py;
        count += 2;
        checkBounds(px, py);
    }

    private void add(int arrayindex, float[] vars, int arraylength) {
        ensureCapacity(count + arraylength);

        if (arrayindex != count)
            System.arraycopy(pointlist, arrayindex, pointlist, arrayindex + arraylength, count - arrayindex);
        System.arraycopy(vars, 0, pointlist, arrayindex, arraylength);
        count += arraylength;
        dirtybounds = true;
    }

    public void add(PointList pointList) {
        ensureCapacity(count + pointList.count);
        System.arraycopy(pointList.pointlist, 0, pointlist, count, pointList.count);
        count += pointList.count;
        dirtybounds = true;
    }

    public void add(int index, PointList pointList) {
        add(index << 1, pointList.pointlist, pointList.count);
    }

    public final void addAll(PointList add) {
        add(count, add.pointlist, add.count);
    }

    public final void addAll(int index, PointList add) {
        add(index << 1, add.pointlist, add.count);
    }

    public final void add(int index, float... vars) {
        if (vars == null) return;
        add(index << 1, vars, vars.length);
    }

    public final void add(float[] pts) {
        add(size(), pts);
    }

    public final void remove(int index) {
        index <<= 1;
        System.arraycopy(pointlist, index + 2, pointlist, index, count - index - 2);
        count -= 2;
        excessbounds = true;
    }

    public final void setLocation(int index, float px, float py) {
        index <<= 1;
        pointlist[index] = px;
        pointlist[index + 1] = py;
        checkBounds(px, py);
        excessbounds = true;
    }

    public final void translateLocation(int index, float dx, float dy) {
        index <<= 1;
        pointlist[index] += dx;
        pointlist[index + 1] += dy;
        checkBounds(pointlist[index], pointlist[index + 1]);
    }

    public void setNan(int index) {
        index <<= 1;
        pointlist[index] = Float.NaN;
        pointlist[index + 1] = Float.NaN;
        excessbounds = true;
    }

    public final void clear() {
        count = 0;
        resetBounds();
    }

    public void setPack(float[] pack, int count) {
        this.pointlist = pack;
        this.count = count;
        dirtybounds = true;
    }

    public void setPack(float[] pack) {
        this.pointlist = pack;
        this.count = (pack != null) ? pack.length : 0;
        dirtybounds = true;
    }

    public final int size() {
        return count >> 1;
    }

    public int count() {
        return count;
    }

    public final boolean isEmpty() {
        return count == 0;
    }

    public int nanDrop(int index) {
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
        count = validPosition;
        return returnIndex;
    }

    public static void swap(float[] plist, int i0, int i1) {
        float tx, ty;
        tx = plist[i0];
        ty = plist[i0 + 1];
        plist[i0] = plist[i1];
        plist[i0 + 1] = plist[i1 + 1];
        plist[i1] = tx;
        plist[i1 + 1] = ty;
    }

    public static void reverse(float[] plist, int count) {
        int m = count / 2;
        for (int i = 0, s = count - 2; i < m; i += 2, s -= 2) {
            swap(plist, i, s);
        }
    }

    public void reverse() {
        reverse(pointlist, count);
    }

    public void transform(Matrix matrix) {
        matrix.mapPoints(pointlist);
        dirtybounds = true;
    }

    public void translate(float dx, float dy) {
        for (int i = 0, s = count - 1; i < s; i += 2) {
            pointlist[i] += dx;
            pointlist[i + 1] += dy;
        }
        maxX += dx;
        minX += dx;
        maxY += dy;
        minY += dy;
    }

    public int getIndexOfClosestPoint(float x, float y, double distancelimit) {
        int index = INVALID_POINT;
        double min = distancelimit * distancelimit, current;
        for (int i = 0; i < count; i += 2) {
            float px = pointlist[i];
            float py = pointlist[i + 1];
            current = distanceSq(px, py, x, y);
            if ((current < min) || ((current == min) && (index != 0))) { //give preference to equidistant endpoints
                min = current;
                index = (i >> 1);
            }
        }
        return index;
    }

    public float getMinX() {
        computeBounds(true);
        return minX;
    }

    public float getMaxX() {
        computeBounds(true);
        return maxX;
    }

    public float getMinY() {
        computeBounds(true);
        return minY;
    }

    public float getMaxY() {
        computeBounds(true);
        return maxY;
    }

    public float getWidth() {
        computeBounds(true);
        return maxX - minX;
    }

    public float getHeight() {
        computeBounds(true);
        return maxY - minY;
    }

    public float getCenterX() {
        computeBounds(true);
        return (minX + maxX) / 2;
    }

    public float getCenterY() {
        computeBounds(true);
        return (minY + maxY) / 2;
    }

    private void computeBounds(boolean exact) {
        if ((dirtybounds) || (excessbounds & exact)) {
            resetBounds();
            for (int i = 0; i < count; i += 2) {
                float px = pointlist[i];
                float py = pointlist[i + 1];
                checkBounds(px, py);
            }
        }
    }

    private void checkBounds(float px, float py) {
        if (px < minX) minX = px;
        if (px > maxX) maxX = px;
        if (py < minY) minY = py;
        if (py > maxY) maxY = py;
    }

    private void resetBounds() {
        minX = Float.POSITIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        excessbounds = false;
        dirtybounds = false;
    }

    public void snap() {
        for (int i = 0; i < count; i++) {
            pointlist[i] = (float) Math.rint(pointlist[i]);
        }
    }

    public float getX(int index) {
        index <<= 1;
        if (index < 0) return Float.NaN;
        if (index >= count) return Float.NaN;
        return pointlist[index];
    }

    public float getY(int index) {
        index <<= 1;
        index++;
        if (index < 0) return Float.NaN;
        if (index >= count) return Float.NaN;
        return pointlist[index];
    }


    public float[] pack() {
        return Arrays.copyOf(pointlist, count);
    }

    public boolean isLoop() {
        if (isEmpty()) return false;
        return ((getX(0) == getX(size() - 1)) && (getY(0) == getY(size() - 1)));
    }

    public void loopTo(int index) {
        if (isLoop()) {
            index <<= 1;
            remove(size() - 1);
            float[] temp = new float[pointlist.length];
            int enddex = count - index;
            System.arraycopy(pointlist, index, temp, 0, enddex);
            System.arraycopy(pointlist, 0, temp, enddex, index);
            pointlist = temp;
            add(getX(0), getY(0));
        }
    }

    public double distanceSegment(int i) {
        return Math.sqrt(distanceSqSegment(i));
    }

    public double distanceSqSegment(int i) {
        return distanceSqBetweenIndex(i, i + 1);
    }

    public double distanceBetweenIndex(int p0, int p1) {
        return Math.sqrt(distanceSqBetweenIndex(p0, p1));
    }

    public double distanceBetweenIndex(int p0, float x, float y) {
        float x0 = getX(p0);
        float y0 = getY(p0);
        return distance(x0, y0, x, y);
    }

    public double distanceSqBetweenIndex(int p0, int p1) {
        if (p0 == p1) return 0;

        float x0 = getX(p0);
        float y0 = getY(p0);

        float x1 = getX(p1);
        float y1 = getY(p1);

        return distanceSq(x0, y0, x1, y1);
    }

    public static float distanceSq(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        dx *= dx;
        dy *= dy;
        return dx + dy;
    }

    public static double distance(float x0, float y0, float x1, float y1) {
        return Math.sqrt(distanceSq(x0, y0, x1, y1));
    }
}

