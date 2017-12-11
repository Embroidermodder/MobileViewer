package com.embroidermodder.embroideryviewer.geom;

/**
 * Points interface.
 *
 * Returns points as well as x, y, and data values for particular objects, within it.
 *
 */
public interface Points {

    Point getPoint(int index);

    float getX(int index);

    float getY(int index);

    void setLocation(int index, float x, float y);

    int getData(int index);

    int size();

}
