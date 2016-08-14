package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.OutputStream;

public class IFormat {
    public static final int NORMAL = 0;
    public static final int JUMP = 1;
    public static final int TRIM = 2;
    public static final int STOP = 4;
    public static final int END = 8;

    private static Object getByFilename(String filename) {
        if (filename.length() < 4) return null;
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

    public static IFormat.Reader getReaderByFilename(String filename) {
        Object o = getByFilename(filename);
        return IFormat.Reader.class.isInstance(o) ? IFormat.Reader.class.cast(o) : null;
    }

    public static IFormat.Writer getWriterByFilename(String filename) {
        Object o = getByFilename(filename);
        return IFormat.Writer.class.isInstance(o) ? IFormat.Writer.class.cast(o) : null;
    }

    public interface Reader {
        boolean hasColor();

        boolean hasStitches();

        EmbPattern read(DataInputStream stream);
    }

    public interface Writer {
        void write(EmbPattern pattern, OutputStream stream);
    }
}
