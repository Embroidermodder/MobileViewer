package com.embroidermodder.embroideryviewer;

import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Point;
import com.embroidermodder.embroideryviewer.geom.PointIterator;
import com.embroidermodder.embroideryviewer.geom.Points;

import java.io.IOException;

public class EmbWriterJEF extends EmbWriter {
    final class DefineConstants {
        public static final int HOOP_126X110 = 0;
        public static final int HOOP_110X110 = 1;
        public static final int HOOP_50X50 = 2;
        public static final int HOOP_140X200 = 3;
        public static final int HOOP_230X200 = 4;
    }

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
        t.fix_color_count = true;
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
        //pattern.rel_flip(1);
        //pattern.correctForMaxStitchLength(127, 127);
        pattern.fixColorCount();
        int colorCount = 0;
        int designWidth;
        int designHeight;
        int offsets;
        double dx, dy;
        double xx = 0.0, yy = 0.0;
        int pointCount = 0;
        colorCount = pattern.getThreadlist().size();
        pointCount = pattern.getStitches().size();
        byte b[] = new byte[4];

        //-------------I NEED TO CHANGE HERE CALCULATION OF OFF SET
        offsets = 0x74 + (colorCount * 8);
        writeInt32(offsets);
        writeInt32(0x0A);
        //time and date
        write(String.format("20122017218088").getBytes());
        writeInt8(0x00);
        writeInt8(0x00);
        writeInt32(colorCount);

        int jumpAndStopCount = 0;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int data = stitches.getData(i);
            switch (data) {
                case EmbPattern.STOP:
                case EmbPattern.COLOR_CHANGE:
                case EmbPattern.JUMP:
                case EmbPattern.TRIM:
                    jumpAndStopCount++;
                    break;

            }
        }

        writeInt32(pointCount + jumpAndStopCount);


        designWidth = (int) (stitches.getWidth());
        designHeight = (int) (stitches.getHeight());

        writeInt32(jefGetHoopSize(designWidth, designHeight));
            /* Distance from center of Hoop */
        writeInt32( designWidth / 2); // left
        writeInt32( designHeight / 2); // top
        writeInt32( designWidth / 2); // right
        writeInt32( designHeight / 2); // bottom

            /* Distance from default 110 x 110 Hoop */
        if (Math.min(550 - designWidth / 2, 550 - designHeight / 2) >= 0) {
            writeInt32( Math.max(-1, 550 - designWidth / 2)); // left
            writeInt32( Math.max(-1, 550 - designHeight / 2)); // top
            writeInt32( Math.max(-1, 550 - designWidth / 2)); // right
            writeInt32( Math.max(-1, 550 - designHeight / 2)); // bottom
        } else {
            writeInt32( -1);
            writeInt32( -1);
            writeInt32( -1);
            writeInt32( -1);
        }

            /* Distance from default 50 x 50 Hoop */
        if (Math.min(250 - designWidth / 2, 250 - designHeight / 2) >= 0) {
            writeInt32( Math.max(-1, 250 - designWidth / 2)); // left
            writeInt32( Math.max(-1, 250 - designHeight / 2)); // top
            writeInt32( Math.max(-1, 250 - designWidth / 2)); // right
            writeInt32( Math.max(-1, 250 - designHeight / 2)); // bottom
        } else {
            writeInt32( -1);
            writeInt32( -1);
            writeInt32( -1);
            writeInt32( -1);
        }

            /* Distance from default 140 x 200 Hoop */
        writeInt32( 700 - designWidth / 2); // left
        writeInt32( 1000 - designHeight / 2); // top
        writeInt32( 700 - designWidth / 2); // right
        writeInt32( 1000 - designHeight / 2); // bottom

            /* repeated Distance from default 140 x 200 Hoop /
            / TODO: Actually should be distance to custom hoop */
        writeInt32( 630 - designWidth / 2); // left
        writeInt32( 550 - designHeight / 2); // top
        writeInt32( 630 - designWidth / 2); // right
        writeInt32( 550 - designHeight / 2); // bottom


        EmbThread[] threadSet = EmbThreadJef.getThreadSet();
        for (EmbObject embObject : pattern.asColorEmbObjects()) {
            writeInt32( EmbThread.findNearestIndex(embObject.getThread().color, threadSet));
        }
        for (int i = 0; i < colorCount; i++) {
            writeInt32( 0x0D);
        }
        int flags;

        for (Point stitches1 : new PointIterator<Points>(pattern.getStitches())) {
            float x = (float) stitches1.getX();
            float y = (float) stitches1.getY();
            dx = x - xx;
            dy = y - yy;
            xx = x;
            yy = y;
            flags = stitches1.data();
            encode(b, (byte) Math.round(dx), (byte) Math.round(dy), flags);
            writeInt8(b[0]);
            writeInt8(b[1]);
            if ((b[0] == -128) && ((b[1] == 1) || (b[1] == 2) || (b[1] == 4))) {
                writeInt8(b[2]);
                writeInt8(b[3]);
            }
        }
        writeInt8(0x80);
        writeInt8(0x10);
    }

    private static int jefGetHoopSize(int width, int height) {
        if (width < 50 && height < 50) {
            return DefineConstants.HOOP_50X50;
        }
        if (width < 110 && height < 110) {
            return DefineConstants.HOOP_110X110;
        }
        if (width < 140 && height < 200) {
            return DefineConstants.HOOP_140X200;
        }
        return DefineConstants.HOOP_110X110;
    }

    private void encode(byte[] b, byte dx, byte dy, int flags) {
        if (flags == EmbPattern.STOP) {
            b[0] = (byte) 128;
            b[1] = 1;
            b[2] = dx;
            b[3] = dy;
        } else if (flags == EmbPattern.TRIM) {
            b[0] = (byte) 128;
            b[1] = 2;
            b[2] = dx;
            b[3] = dy;
        } else if (flags == EmbPattern.JUMP) {
            b[0] = (byte) 128;
            b[1] = 4;
            b[2] = dx;
            b[3] = dy;
        } else {
            b[0] = dx;
            b[1] = dy;
        }
    }

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

}
