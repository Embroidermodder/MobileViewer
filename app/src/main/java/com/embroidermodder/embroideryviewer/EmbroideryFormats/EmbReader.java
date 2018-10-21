package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import com.embroidermodder.embroideryviewer.EmmPattern;

import java.io.IOException;
import java.io.InputStream;


public abstract class EmbReader extends ReadHelper {
    EmmPattern pattern;

    protected int colorIndex = 0;

    protected double lastx = 0;
    protected double lasty = 0;

    public void read(EmmPattern pattern, InputStream stream) throws IOException {
        colorIndex = 0;
        readPosition = 0;
        this.stream = stream;
        this.pattern = pattern;
        read();
    }

    protected abstract void read() throws IOException;

    public void stitchAbs(double x, double y) {
        lastx = x;
        lasty = y;
        pattern.add(lastx,lasty,EmmPattern.STITCH);
    }

    public void stitch(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty, EmmPattern.STITCH);
    }

    public void moveAbs(double x, double y) {
        lastx = x;
        lasty = y;
        pattern.add(lastx,lasty,EmmPattern.JUMP);
    }

    public void move(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty,EmmPattern.JUMP);
    }

    public void changeColor() {
        pattern.add(lastx,lasty, EmmPattern.COLOR_CHANGE);
        colorIndex++;
    }

    public void trim() {
        pattern.add(lastx,lasty,EmmPattern.TRIM);
    }

    public void stop() {
        pattern.add(lastx,lasty, EmmPattern.STOP);
    }

    public void end() {
        pattern.add(lastx,lasty,EmmPattern.END);
    }

    public void setName(String name) {
        pattern.name = name;
    }

}
