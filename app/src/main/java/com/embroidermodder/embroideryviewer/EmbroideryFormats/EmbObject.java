package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import com.embroidermodder.embroideryviewer.geom.Points;

public interface EmbObject {
    EmbThread getThread();
    Points getPoints();
    int getType();
}
