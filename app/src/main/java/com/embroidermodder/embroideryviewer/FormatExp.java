package com.embroidermodder.embroideryviewer;


import java.io.DataInputStream;
import java.io.IOException;

public class FormatExp implements IFormat.Reader {

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        byte b0, b1;
        try {
            for (int i = 0; stream.available() > 0; i++) {
                int flags = IFormat.NORMAL;
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
                        flags = IFormat.STOP;
                    } else if ((b1 == 2) || (b1 == 4) || b1 == 6) {
                        flags = IFormat.TRIM;
                        if (b1 == 2) {
                            flags = IFormat.NORMAL;
                        }
                        b0 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b1 = stream.readByte();
                        if (stream.available() <= 0) {
                            break;
                        }
                    } else if ((b1 & 0xFF) == 0x80) {
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
                        flags = IFormat.TRIM;
                    }
                }
                p.addStitchRel((float) b0, (float) b1, flags, true);
            }
        } catch (IOException ex) {
        }

        return p.getFlippedPattern(false, true);
    }
}
