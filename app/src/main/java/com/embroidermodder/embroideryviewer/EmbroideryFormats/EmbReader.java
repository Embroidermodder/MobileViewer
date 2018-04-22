package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import java.io.IOException;
import java.io.InputStream;


public abstract class EmbReader extends ReadHelper implements IFormat.Reader {
    EmbPattern pattern;

    protected int colorIndex = 0;

    protected double lastx = 0;
    protected double lasty = 0;

    public void read(EmbPattern pattern, InputStream stream) throws IOException {
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
        pattern.add(lastx,lasty,EmbPattern.STITCH);
    }

    public void stitch(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty, EmbPattern.STITCH);
    }

    public void moveAbs(double x, double y) {
        lastx = x;
        lasty = y;
        pattern.add(lastx,lasty,EmbPattern.JUMP);
    }

    public void move(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty,EmbPattern.JUMP);
    }

    public void changeColor() {
        pattern.add(lastx,lasty, EmbPattern.COLOR_CHANGE);
        colorIndex++;
    }

    public void trim() {
        pattern.add(lastx,lasty,EmbPattern.TRIM);
    }

    public void stop() {
        pattern.add(lastx,lasty, EmbPattern.STOP);
    }

    public void end() {
        pattern.add(lastx,lasty,EmbPattern.END);
    }

    public void setName(String name) {
        pattern.name = name;
    }

}
