package org.embroideryio.embroideryio;


import org.embroideryio.geom.Points;

public interface EmbObject {
    EmbThread getThread();
    Points getPoints();
    int getType();
}
