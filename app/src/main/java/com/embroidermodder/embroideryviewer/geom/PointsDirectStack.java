package com.embroidermodder.embroideryviewer.geom;

import java.util.ArrayList;

/**
 * Created by Tat on 5/25/2015.
 *
 * Point Stack is a stack structure for pointlists.
 */

public abstract class PointsDirectStack<E extends PointsDirect> extends PointsStack<E> {

    public PointsDirectStack() {
        newPath();
    }

    public PointsDirectStack(PointsDirectStack<E> path) {
        super();
        stack = new ArrayList<>(path.stack.size());
        for (E p : path.stack) {
            add(newItem(p));
        }
    }

    abstract protected void validate();
    abstract protected E newItem(E v);
    abstract protected E getBlank();

    public float[] getPoints() {
        validate();
        return current.pack();
    }

    public float calculateMinY() {
        validate();
        float min = Float.POSITIVE_INFINITY;
        for (PointsDirect layer : stack) {
            float cmax = layer.getMinY();
            if (cmax < min) {
                min = cmax;
            }
        }
        return min;
    }

    public float calculateMinX() {
        validate();
        float min = Float.POSITIVE_INFINITY;
        for (PointsDirect layer : stack) {
            float cmax = layer.getMinX();
            if (cmax < min) {
                min = cmax;
            }
        }
        return min;
    }

    public float calculateMaxY() {
        validate();
        float max = Float.NEGATIVE_INFINITY;
        for (PointsDirect layer : stack) {
            float cmax = layer.getMaxY();
            if (cmax > max) {
                max = cmax;
            }
        }
        return max;
    }

    public float calculateMaxX() {
        validate();
        float max = Float.NEGATIVE_INFINITY;
        for (PointsDirect layer : stack) {
            float cmax = layer.getMaxX();
            if (cmax > max) {
                max = cmax;
            }
        }
        return max;
    }

    public void clear() {
        stack.clear();
        current.clear();
        stack.add(current);
    }

    public void centerize(float x, float y) {
        float minX = calculateMinX();
        float maxX = calculateMaxX();
        float minY = calculateMinY();
        float maxY = calculateMaxY();
        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;
        float dx = x - centerX;
        float dy = y - centerY;
        for (PointsDirect layer : stack) {
            layer.translate(dx, dy);
        }
    }
}
