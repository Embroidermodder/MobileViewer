package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import java.io.IOException;

public class EmbReaderPCS extends EmbReader {

    private static final double R = 5d / 3d;


    static double pcsDecode(int a1, int a2, int a3) {
        int res = (a1 & 0xFF) + ((a2 & 0xFF) << 8) + ((a3 & 0xFF) << 16);
        return ((res << 8) >> 8);
    }

    @Override
    protected void read() throws IOException {
        char allZeroColor = 1;
        int i;
        byte[] b = new byte[9];
        double dx, dy;
        int st, version, hoopSize;
        int colorCount;
        version = readInt8();
        hoopSize = readInt8();  /* 0 for PCD, 1 for PCQ (MAXI), 2 for PCS with small hoop(80x80), */
        /* and 3 for PCS with large hoop (115x120) */

        colorCount = readInt16LE();

        for (i = 0; i < colorCount; i++) {
            int color = readInt24BE();
            EmbThread t = new EmbThread(color, EmbThread.getHexColor(color), "" + i);
            if (t.getRed() != 0 || t.getGreen() != 0 || t.getBlue() != 0) {
                allZeroColor = 0;
            }
            pattern.addThread(t);
            skip(1);
        }

        st = readInt16LE();
        for (i = 0; i < st; i++) {
            if (readFully(b) != b.length) break;
            dx = (pcsDecode(b[1], b[2], b[3]) / R);
            dy = -(pcsDecode(b[5], b[6], b[7]) / R);
            if ((b[8] & 0x01) != 0) {
                pattern.addStitchRel(0, 0, EmbPattern.COLOR_CHANGE, true);
            } else if ((b[8] & 0x04) != 0) {
                moveAbs(dx, dy);
            } else {
                stitchAbs(dx, dy);
            }
        }
        end();
    }

}
