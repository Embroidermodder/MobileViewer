package com.embroidermodder.embroideryviewer;

import java.io.IOException;

public class EmbReaderEXP extends EmbReader {

    @Override
    public void read() throws IOException {
        byte[] b = new byte[2];
        while (readFully(b) == b.length) {
            if ((b[0] & 0xFF) == 0x80) {
                switch (b[1] & 0xFF) {
                    case 0x80:
                        skip(2);
                        stop();
                        break;
                    case 0x02:
                        stitch((float) b[0], -(float) b[1]);
                        break;
                    case 0x04:
                        if (readFully(b) != b.length) break;
                        move((float) b[0], -(float) b[1]);
                        break;
                    default:
                        if ((b[1] & 1) != 0) {
                            //odd
                            if (readFully(b) != b.length) break;
                            changeColor();
                            move((float) b[0], -(float) b[1]);
                        } else {
                            //even
                            if (readFully(b) != b.length) break;
                            stop();
                            move((float) b[0], -(float) b[1]);
                        }
                }
            } else {
                stitch((float) b[0], -(float) b[1]);
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
