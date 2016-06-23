package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;

public class FormatInf implements IFormatReader {

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return false;
    }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        try {
            stream.skip(12);
            int numberOfColors = stream.readInt();
            for (int x = 0; x < numberOfColors; x++) {
                stream.skip(4);
                EmbThread t = new EmbThread();
                int red =  stream.readByte();
                int green = stream.readByte();
                int blue = stream.readByte();
                t.setColor(new EmbColor(red, green, blue));
                t.setCatalogNumber("");
                t.setDescription("");
                p.addThread(t);
                stream.skip(2);
                t.setCatalogNumber(BinaryReader.readString(stream, 50));
                t.setDescription(BinaryReader.readString(stream, 50));
            }
        } catch (IOException ex) {
        }
        return p;
    }
}
