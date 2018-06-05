package com.embroidermodder.embroideryviewer.geom;


/**
 * Basic interface for a defined curve.
 *
 */

public interface Curve extends Points {
    Point getValue(double t);
    void addAnchorPoint(Point selectedPoint, int offset);
    void addControlPoint(Point selectedPoint, int offset);
}
