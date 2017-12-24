package com.embroidermodder.embroideryviewer;

import java.io.IOException;

public class EmbReaderPCS extends EmbReader {


    static float pcsDecode(int a1, int a2, int a3) {
        int res = (a1 & 0xFF) + ((a2 & 0xFF) << 8) + ((a3 & 0xFF) << 16);
        if (res > 0x7FFFFF) {
            return (-((~(res) & 0x7FFFFF) - 1));
        }
        return res;
    }

    @Override
    protected void read() throws IOException {
        char allZeroColor = 1;
        int i;
        byte[] b = new byte[9];
        float dx, dy;
        int st, version, hoopSize;
        int colorCount;
            version = readInt8();
            hoopSize = readInt8();  /* 0 for PCD, 1 for PCQ (MAXI), 2 for PCS with small hoop(80x80), */
                                      /* and 3 for PCS with large hoop (115x120) */

//    switch (hoopSize) {
//        case 2:
//            p.hoop.width = 80.0;
//            p.hoop.height = 80.0;
//            break;
//        case 3:
//            p.hoop.width = 115;
//            p.hoop.height = 120.0;
//            break;
//    }

            colorCount = readInt16LE();

            for (i = 0; i < colorCount; i++) {
                int color = readInt24BE();
//                int red = readInt8() & 0xFF;
//                int green = readInt8() & 0xFF;
//                int blue = readInt8() & 0xFF;
//                EmbThread t = new EmbThread(red, green, blue, "", "");
                EmbThread t = new EmbThread(color,EmbThread.getHexColor(color),"" + i);
                if (t.getRed() != 0 || t.getGreen() != 0 || t.getBlue() != 0) {
                    allZeroColor = 0;
                }
                pattern.addThread(t);
                readInt8(); //skip(1).
            }
            st = readInt16LE();
            for (i = 0; i < st; i++) {
                readFully(b);
                dx = pcsDecode(b[1], b[2], b[3]);
                dy = pcsDecode(b[5], b[6], b[7]);
                if ((b[8] & 0x01) != 0) {
                    changeColor();
                } else if ((b[8] & 0x04) != 0) {
                    move(dx,dy);
                }
                else {
                    stitch(dx,dy);
                }
            }
            end();
    }

}
