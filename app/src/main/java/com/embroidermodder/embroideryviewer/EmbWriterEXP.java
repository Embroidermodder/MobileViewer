package com.embroidermodder.embroideryviewer;

import com.embroidermodder.embroideryviewer.geom.DataPoints;

import java.io.IOException;

public class EmbWriterEXP extends EmbWriter {


    @Override
    public void preWrite(EmbPattern input) {
        TransCoder t = TransCoder.getTranscoder();
        float minX = input.getStitches().getMinX();
        float minY = input.getStitches().getMinY();
        float maxX = input.getStitches().getMaxX();
        float maxY = input.getStitches().getMaxY();

        t.setInitialPosition((int) ((minX + maxX) / 2), (int) ((minY + maxY) / 2));
        t.require_jumps = true;
        t.splitLongJumps = true;
        t.maxJumpLength = 127;
        t.maxStitchLength = 127;
        t.snap = true;
        if (isLockStitches()) {
            t.tie_on = true;
            t.tie_off = true;
        }
        t.transcode(input);
    }

    @Override
    public void write() throws IOException {
        DataPoints stitches = pattern.getStitches();

        int deltaX, deltaY;
        boolean jumping = false;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            switch (stitches.getData(i)) {
                case EmbPattern.STITCH:
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    deltaX = (int) Math.rint(stitches.getX(i));
                    deltaY = (int) Math.rint(stitches.getY(i));
                    stream.write(deltaX);
                    stream.write(-deltaY);
                    break;
                case EmbPattern.JUMP:
                    jumping = true;
                    deltaX = (int) Math.rint(stitches.getX(i));
                    deltaY = (int) Math.rint(stitches.getY(i));
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
        }
    }

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

}
