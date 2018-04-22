package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import java.io.IOException;

public class EmbReaderJEF extends EmbReader {

    @Override
    public void read() throws IOException {
        EmbThreadJef[] jefThread = EmbThreadJef.getThreadSet();
        byte[] b = new byte[2];
        int stitchOffset, numberOfColors, numberOfStitches;
        stitchOffset = readInt32LE();
        skip(20);
        numberOfColors = readInt32LE();
        numberOfStitches = readInt32LE();
        skip(84);
        //A malformed JEF could say it needs more stitch colors than it actually could ever use.
        for (int i = 0; i < numberOfColors; i++) {
            int index = readInt32LE();
            if (index < 0) index = -index;
            pattern.add(jefThread[index % 79]);
        }
        skip(stitchOffset - 116 - (numberOfColors * 4));
        for (int i = 0; i < numberOfStitches + 100; i++) {
            if (readFully(b) != b.length) break;
            if (((b[0] & 0xFF) == 0x80)) {
                if ((b[1] & 0x01) != 0) { //80, 01 change color
                    if (readFully(b) != b.length) break;
                    changeColor();
                    move((float) b[0], -(float) b[1]);
                } else if ((b[1] == 0x04) || (b[1] == 0x02)) { //80, 02, trim.
                    if (readFully(b) != b.length) break;
                    trim();
                    move((float) b[0], -(float) b[1]);
                } else if (b[1] == 0x10) {
                    break;
                }
            } else {
                stitch((float) b[0], -(float) b[1]);
            }
        }
        end();
    }


    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

}
