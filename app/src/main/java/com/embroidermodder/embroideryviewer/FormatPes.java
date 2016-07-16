package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;

public class FormatPes implements IFormat.Reader {

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() { return true; }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        try {
            stream.skip(8);
            byte fullInt[] = new byte[4];
            stream.read(fullInt);
            int pecStart = ((fullInt[2] & 0xFF) << 16) + ((fullInt[1] & 0xFF) << 8) + (fullInt[0] & 0xFF);
            stream.skip(pecStart + 36);
            int numColors = (stream.readByte() & 0xFF) + 1;
            for (int x = 0; x < numColors; x++) {
                int index = stream.readByte();
                p.addThread(FormatPec.getThreadByIndex(index % 65));
            }
            stream.skip(484 - numColors - 1);
            FormatPec.readPecStitches(p, stream);
        } catch (IOException ex) {
        }
        return p;
    }
}
