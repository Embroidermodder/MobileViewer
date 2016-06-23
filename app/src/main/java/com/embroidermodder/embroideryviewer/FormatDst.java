package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;

public class FormatDst implements IFormatReader {

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

    private int decodeFlags(byte b) {
        int returnCode = 0;
        if (b == 0xF3) {
            return StitchType.END;
        }
        if ((b & 0x80) > 0) {
            returnCode |= StitchType.JUMP;
        }
        if ((b & 0x40) > 0) {
            returnCode |= StitchType.STOP;
        }
        return returnCode;
    }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        byte[] b = new byte[3];

        try {
            stream.skip(0x200);
            while (stream.read(b) == 3) {
                int x = 0;
                int y = 0;
                if ((b[0] & 0x01) > 0) {
                    x += 1;
                }
                if ((b[0] & 0x02) > 0) {
                    x -= 1;
                }
                if ((b[0] & 0x04) > 0) {
                    x += 9;
                }
                if ((b[0] & 0x08) > 0) {
                    x -= 9;
                }
                if ((b[0] & 0x80) > 0) {
                    y += 1;
                }
                if ((b[0] & 0x40) > 0) {
                    y -= 1;
                }
                if ((b[0] & 0x20) > 0) {
                    y += 9;
                }
                if ((b[0] & 0x10) > 0) {
                    y -= 9;
                }
                if ((b[1] & 0x01) > 0) {
                    x += 3;
                }
                if ((b[1] & 0x02) > 0) {
                    x -= 3;
                }
                if ((b[1] & 0x04) > 0) {
                    x += 27;
                }
                if ((b[1] & 0x08) > 0) {
                    x -= 27;
                }
                if ((b[1] & 0x80) > 0) {
                    y += 3;
                }
                if ((b[1] & 0x40) > 0) {
                    y -= 3;
                }
                if ((b[1] & 0x20) > 0) {
                    y += 27;
                }
                if ((b[1] & 0x10) > 0) {
                    y -= 27;
                }
                if ((b[2] & 0x04) > 0) {
                    x += 81;
                }
                if ((b[2] & 0x08) > 0) {
                    x -= 81;
                }
                if ((b[2] & 0x20) > 0) {
                    y += 81;
                }
                if ((b[2] & 0x10) > 0) {
                    y -= 81;
                }
                int flags = decodeFlags(b[2]);
                if (flags == StitchType.END) {
                    break;
                }
                p.addStitchRel(x, y, flags, true);
            }
        } catch (IOException ex) {

        }
        return p.getFlippedPattern(false, true);
    }
}
