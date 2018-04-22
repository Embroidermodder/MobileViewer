package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import java.io.IOException;

public class EmbReaderSEW extends EmbReader {
    @Override
    protected void read() throws IOException {
        byte[] b = new byte[2];
        int numberOfColors;
        EmbThreadSew[] threads = EmbThreadSew.getThreadSet();
        numberOfColors = readInt16LE();
        for (int i = 0; i < numberOfColors; i++) {
            int index = readInt16LE();
            pattern.addThread(threads[index % 79]);
        }
        skip(0x1D78 - numberOfColors * 2 - 2);
        while (true) {
            readFully(b);
            if (((b[0] & 0xFF) == 0x80)) {
                if ((b[1] & 0x01) != 0) {
                    readFully(b);
                    changeColor();
                } else if ((b[1] == 0x04) || (b[1] == 0x02)) {
                    readFully(b);
                    move(b[0], -b[1]);
                } else if (b[1] == 0x10) {
                    stitch(b[0], -b[1]);
                    break;
                }
            } else {
                stitch(b[0],-b[1]);
            }
        }
        end();
    }
}
