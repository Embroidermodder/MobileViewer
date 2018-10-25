package com.embroidermodder.embroideryviewer.geom;

/**
 * Points interface.
 * <p>
 * Returns points as well as x, y, and data values for particular objects, within it.
 */
public interface DPoints extends Points {
    void add(float px, float py, int data);

    void add(Point add);

    void add(int index, Point add);

    void setData(int index, int data);
}
