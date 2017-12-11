package com.embroidermodder.embroideryviewer;

import com.embroidermodder.embroideryviewer.geom.Points;

public interface EmbObject {
    EmbThread getThread();
    Points getPoints();
    int getType();
}
