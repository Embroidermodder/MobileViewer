package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;

public class FormatExp implements IFormatReader {

    public Pattern Read(DataInputStream stream) {
        Pattern p = new Pattern();
        byte b0, b1;
        try {
            for (int i = 0; stream.available() > 0; i++) {
                int flags = StitchType.NORMAL;
                b0 = stream.readByte();
                if (stream.available() <= 0) {
                    break;
                }
                b1 = stream.readByte();
                if (stream.available() <= 0) {
                    break;
                }
                if ((b0 & 0xFF) == 0x80) {
                    if ((b1 & 1) > 0) {
                        b0 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b1 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                        flags = StitchType.STOP;
                    } else if ((b1 == 2) || (b1 == 4) || b1 == 6) {
                        flags = StitchType.TRIM;
                        if (b1 == 2) {
                            flags = StitchType.NORMAL;
                        }
                        b0 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b1 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                    } else if ((b1 & 0xFF)== 0x80) {
                        b0 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b1 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b0 = 0;
                        b1 = 0;
                        flags = StitchType.TRIM;
                    }
                }
                p.addStitchRel((float) b0 / 10.0, (float) b1 / 10.0, flags, true);
            }
        } catch (IOException ex) {
        }

        return p;
    }
}
