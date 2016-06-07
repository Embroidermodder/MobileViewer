package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;

public class FormatPcs implements IFormatReader {

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

    static double pcsDecode(int a1, int a2, int a3) {
        int res = (a1 & 0xFF) + ((a2 & 0xFF) << 8) + ((a3 & 0xFF) << 16);
        if (res > 0x7FFFFF) {
            return (-((~(res) & 0x7FFFFF) - 1));
        }
        return res;
    }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        char allZeroColor = 1;
        int i = 0;
        byte[] b = new byte[9];
        double dx = 0, dy = 0;
        int flags = 0, st = 0;
        byte version, hoopSize;
        int colorCount;
        try {
            version = stream.readByte();
            hoopSize = stream.readByte();  /* 0 for PCD, 1 for PCQ (MAXI), 2 for PCS with small hoop(80x80), */
                                      /* and 3 for PCS with large hoop (115x120) */

//    switch (hoopSize) {
//        case 2:
//            pattern -> hoop.width = 80.0;
//            pattern -> hoop.height = 80.0;
//            break;
//        case 3:
//            pattern -> hoop.width = 115;
//            pattern -> hoop.height = 120.0;
//            break;
//    }

            colorCount = BinaryReader.readInt16BE(stream);

            for (i = 0; i < colorCount; i++) {

                int red = stream.readByte() & 0xFF;
                int green = stream.readByte() & 0xFF;
                int blue = stream.readByte() & 0xFF;
                EmbThread t = new EmbThread(red, green, blue, "", "");
                EmbColor col = t.getColor();
                if (col.red != 0 || col.green != 0 || col.blue != 0) {
                    allZeroColor = 0;
                }
                p.addThread(t);
                stream.readByte();
            }
            st = BinaryReader.readInt16BE(stream);
            for (i = 0; i < st; i++) {
                flags = StitchType.NORMAL;
                if (stream.read(b) != 9) {
                    break;
                }
                if ((b[8] & 0x01) != 0) {
                    flags = StitchType.STOP;
                } else if ((b[8] & 0x04) != 0) {
                    flags = StitchType.TRIM;
                }
                dx = pcsDecode(b[1], b[2], b[3]);
                dy = pcsDecode(b[5], b[6], b[7]);
                p.addStitchAbs(dx, dy, flags, true);
            }
            p.addStitchRel(0.0, 0.0, StitchType.END, true);
        } catch (IOException ex) {

        }
        return p.getFlippedPattern(false, true);
    }
}
