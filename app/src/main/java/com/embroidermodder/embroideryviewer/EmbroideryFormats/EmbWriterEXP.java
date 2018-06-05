package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import com.embroidermodder.embroideryviewer.geom.DataPoints;

import java.io.IOException;

public class EmbWriterEXP extends EmbWriter {

    @Override
    public void write() throws IOException {
        DataPoints stitches = pattern.getStitches();

        int deltaX, deltaY;
        boolean jumping = false;
        double dx, dy, xx = 0, yy = 0;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            float x = stitches.getX(i);
            float y = stitches.getY(i);
            dx = x - xx;
            dy = y - yy;

            switch (stitches.getData(i)) {
                case EmbPattern.STITCH:
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    deltaX = (int) Math.rint(dx);
                    deltaY = (int) Math.rint(dy);
                    stream.write(deltaX);
                    stream.write(-deltaY);
                    break;
                case EmbPattern.JUMP:
                    jumping = true;
                    deltaX = (int) Math.rint(dx);
                    deltaY = (int) Math.rint(dy);
                    stream.write((byte) 0x80);
                    stream.write((byte) 0x04);
                    stream.write((byte)deltaX);
                    stream.write((byte)-deltaY);
                    break;
                case EmbPattern.COLOR_CHANGE:
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    stream.write((byte) 0x80);
                    stream.write((byte) 0x1);
                    stream.write((byte) 0x00);
                    stream.write((byte) 0x00);
                    break;
                case EmbPattern.STOP:
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    stream.write((byte) 0x80);
                    stream.write((byte) 0x1);
                    stream.write((byte) 0x00);
                    stream.write((byte) 0x00);
                    break;
                case EmbPattern.END:
                    break;
            }
            if (jumping) {
                stream.write((byte) 0x00);
                stream.write((byte) 0x00);
                jumping = false;
            }
            xx = x;
            yy = y;
        }
    }

    @Override
    public double maxJumpDistance() {
        return 127;
    }

    @Override
    public double maxStitchDistance() {
        return 127;
    }

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

}
