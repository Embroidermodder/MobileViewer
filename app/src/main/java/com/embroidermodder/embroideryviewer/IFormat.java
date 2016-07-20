package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;

public class IFormat {
    public static int NORMAL = 0;
    public static int JUMP = 1;
    public static int TRIM = 2;
    public static int STOP = 4;
    public static int END = 8;


    public static IFormat.Reader getReaderByFilename(String filename) {
        filename = filename.toLowerCase();
        switch (filename.substring(filename.length() - 4)) {
            case ".col":
                return new FormatCol();
            case ".exp":
                return new FormatExp();
            case ".dst":
                return new FormatDst();
            case ".jef":
                return new FormatJef();
            case ".pcs":
                return new FormatPcs();
            case ".pec":
                return new FormatPec();
            case ".pes":
                return new FormatPes();
            case ".sew":
                return new FormatSew();
            case ".xxx":
                return new FormatXxx();
            default:
                return null;
        }
    }

    public interface Reader {
        boolean hasColor();

        boolean hasStitches();

        Pattern read(DataInputStream stream);
    }
}
