package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FormatPcs implements IFormat.Reader {

    static float pcsDecode(int a1, int a2, int a3) {
        int res = (a1 & 0xFF) + ((a2 & 0xFF) << 8) + ((a3 & 0xFF) << 16);
        if (res > 0x7FFFFF) {
            return (-((~(res) & 0x7FFFFF) - 1));
        }
        return res;
    }

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        char allZeroColor = 1;
        int i;
        byte[] b = new byte[9];
        float dx, dy;
        int flags, st;
        byte version, hoopSize;
        int colorCount;
        try {
            version = (byte)stream.read();
            hoopSize = (byte)stream.read();  /* 0 for PCD, 1 for PCQ (MAXI), 2 for PCS with small hoop(80x80), */
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

            colorCount = BinaryHelper.readInt16LE(stream);

            for (i = 0; i < colorCount; i++) {

                int red = stream.read() & 0xFF;
                int green = stream.read() & 0xFF;
                int blue = stream.read() & 0xFF;
                EmbThread t = new EmbThread(red, green, blue, "", "");
                EmbColor col = t.getColor();
                if (col.red != 0 || col.green != 0 || col.blue != 0) {
                    allZeroColor = 0;
                }
                pattern.addThread(t);
                stream.read();
            }
            st = BinaryHelper.readInt16LE(stream);
            for (i = 0; i < st; i++) {
                flags = IFormat.NORMAL;
                if (stream.read(b) != 9) {
                    break;
                }
                if ((b[8] & 0x01) != 0) {
                    flags = IFormat.STOP;
                } else if ((b[8] & 0x04) != 0) {
                    flags = IFormat.TRIM;
                }
                dx = pcsDecode(b[1], b[2], b[3]);
                dy = pcsDecode(b[5], b[6], b[7]);
                pattern.addStitchAbs(dx, dy, flags, true);
            }
            pattern.addStitchRel(0.0f, 0.0f, IFormat.END, true);
        } catch (IOException ex) {

        }
        pattern.getFlippedPattern(false, true);
    }
}
