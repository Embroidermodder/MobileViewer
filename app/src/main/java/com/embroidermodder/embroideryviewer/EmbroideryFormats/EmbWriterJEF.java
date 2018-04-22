package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import com.embroidermodder.embroideryviewer.geom.DataPoints;

import java.io.IOException;

public class EmbWriterJEF extends EmbWriter {
    final class DefineConstants {
        public static final int HOOP_110X110 = 0;
        public static final int HOOP_50X50 = 1;
        public static final int HOOP_140X200 = 2;
        public static final int HOOP_126X110 = 3;
        public static final int HOOP_200X200 = 4;
    }

    @Override
    public void write() throws IOException {
        DataPoints stitches = pattern.getStitches();
        pattern.fixColorCount();
        int colorCount = 0;
        int designWidth;
        int designHeight;
        int offsets;

        int pointCount = 0;
        colorCount = pattern.getThreadlist().size();
        pointCount = pattern.getStitches().size();
        byte b[] = new byte[4];

        //-------------I NEED TO CHANGE HERE CALCULATION OF OFF SET
        offsets = 0x74 + (colorCount * 8);
        writeInt32(offsets);
        //writeInt32(0x0A);
        writeInt32(0x14);
        //time and date
        write(String.format("20122017218088").getBytes());
        writeInt8(0x00);
        writeInt8(0x00);
        writeInt32(colorCount);

        writeInt32(pointCount);


        designWidth = (int) (stitches.getWidth());
        designHeight = (int) (stitches.getHeight());

        writeInt32(jefGetHoopSize(designWidth, designHeight));
        /* Distance from center of Hoop */
        writeInt32(designWidth / 2); // left
        writeInt32(designHeight / 2); // top
        writeInt32(designWidth / 2); // right
        writeInt32(designHeight / 2); // bottom

        /* Distance from default 110 x 110 Hoop */
        if (Math.min(550 - designWidth / 2, 550 - designHeight / 2) >= 0) {
            writeInt32(Math.max(-1, 550 - designWidth / 2)); // left
            writeInt32(Math.max(-1, 550 - designHeight / 2)); // top
            writeInt32(Math.max(-1, 550 - designWidth / 2)); // right
            writeInt32(Math.max(-1, 550 - designHeight / 2)); // bottom
        } else {
            writeInt32(-1);
            writeInt32(-1);
            writeInt32(-1);
            writeInt32(-1);
        }

        /* Distance from default 50 x 50 Hoop */
        if (Math.min(250 - designWidth / 2, 250 - designHeight / 2) >= 0) {
            writeInt32(Math.max(-1, 250 - designWidth / 2)); // left
            writeInt32(Math.max(-1, 250 - designHeight / 2)); // top
            writeInt32(Math.max(-1, 250 - designWidth / 2)); // right
            writeInt32(Math.max(-1, 250 - designHeight / 2)); // bottom
        } else {
            writeInt32(-1);
            writeInt32(-1);
            writeInt32(-1);
            writeInt32(-1);
        }

        /* Distance from default 140 x 200 Hoop */
        writeInt32(700 - designWidth / 2); // left
        writeInt32(1000 - designHeight / 2); // top
        writeInt32(700 - designWidth / 2); // right
        writeInt32(1000 - designHeight / 2); // bottom

            /* repeated Distance from default 140 x 200 Hoop /
            / TODO: Actually should be distance to custom hoop */
        writeInt32(630 - designWidth / 2); // left
        writeInt32(550 - designHeight / 2); // top
        writeInt32(630 - designWidth / 2); // right
        writeInt32(550 - designHeight / 2); // bottom


        EmbThread[] threadSet = EmbThreadJef.getThreadSet();
        for (EmbObject embObject : pattern.asColorEmbObjects()) {
            writeInt32(EmbThread.findNearestIndex(embObject.getThread().color, threadSet));
        }
        for (int i = 0; i < colorCount; i++) {
            writeInt32(0x0D);
        }
        int flags = EmbPattern.NO_COMMAND;

        double dx, dy, xx = 0, yy = 0;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            float x = stitches.getX(i);
            float y = stitches.getY(i);
            dx = x - xx;
            dy = y - yy;
            flags = stitches.getData(i);
            encode(b, (byte) Math.round(dx), (byte) Math.round(-dy), flags); //-dy encode as field is yflipped vs java/android
            writeInt8(b[0]);
            writeInt8(b[1]);
            if ((b[0] == -128) && ((b[1] == 1) || (b[1] == 2) || (b[1] == 4))) {
                writeInt8(b[2]);
                writeInt8(b[3]);
            }
            xx = x;
            yy = y;
        }
        if (flags != EmbPattern.END) {
            writeInt8(0x80);
            writeInt8(0x10);
        }
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
        if ((flags & EmbPattern.COMMAND_MASK) == EmbPattern.STITCH) {
            b[0] = dx;
            b[1] = dy;
            return;
        }
        b[0] = (byte) 0x80;
        b[2] = dx;
        b[3] = dy;
        switch (flags & EmbPattern.COMMAND_MASK) {
            default:
                b[0] = dx;
                b[1] = dy;
                break;
            case EmbPattern.COLOR_CHANGE:
                b[1] = 1;
                break;
            case EmbPattern.STOP:
                b[1] = 1;
                break;
            case EmbPattern.END:
                b[1] = 0x10;
                break;
            case EmbPattern.JUMP:
                b[1] = 2;
                break;
            case EmbPattern.TRIM:
                b[1] = 2;
                break;
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
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

}
