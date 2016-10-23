package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.io.InputStream;

public class FormatInf implements IFormat.Reader {

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return false;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        try {
            stream.skip(12);
            int numberOfColors = BinaryHelper.readInt32BE(stream);
            for (int x = 0; x < numberOfColors; x++) {
                stream.skip(4);
                EmbThread t = new EmbThread();
                int red = stream.read();
                int green = stream.read();
                int blue = stream.read();
                t.setColor(new EmbColor(red, green, blue));
                t.setCatalogNumber("");
                t.setDescription("");
                pattern.addThread(t);
                stream.skip(2);
                t.setCatalogNumber(BinaryHelper.readString(stream, 50));
                t.setDescription(BinaryHelper.readString(stream, 50));
            }
        } catch (IOException ex) {
        }
    }
}
