package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import com.embroidermodder.embroideryviewer.geom.Points;

public interface EmbObject {
    EmmThread getThread();
    Points getPoints();
    int getType();
}
