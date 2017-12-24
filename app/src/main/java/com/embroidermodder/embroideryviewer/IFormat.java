package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IFormat {
    public static String getExtentionByFileName(String name) {
        if (name == null) return null;
        String[] split = name.split("\\.");
        if (split.length <= 1) {
            return null;
        }
        return split[split.length - 1].toLowerCase();
    }

    public static IFormat.Reader getReaderByFilename(String filename) {
        String ext = getExtentionByFileName(filename);
        switch (ext) {
            case "emm":
                return new EmbReaderEmm();
            case "col":
                return new EmbReaderCOL();
            case "inf":
                return new EmbReaderINF();
            case "exp":
                return new EmbReaderEXP();
            case "dst":
                return new EmbReaderDST();
            case "jef":
                return new EmbReaderJEF();
            case "pcs":
                return new EmbReaderPCS();
            case "pec":
                return new EmbReaderPEC();
            case "pes":
                return new EmbReaderPES();
            case "sew":
                return new EmbReaderSEW();
            case "shv":
                return new EmbReaderSHV();
            case "vp3":
                return new EmbReaderVP3();
            case "xxx":
                return new EmbReaderXXX();
            case "svg":
                return new EmbReaderSVG();
            default:
                return null;
        }
    }

    public static IFormat.Writer getWriterByFilename(String filename) {
        String ext = getExtentionByFileName(filename);
        switch (ext) {
            case "exp":
                return new EmbWriterEXP();
            case "dst":
                return new EmbWriterDST();
            case "pec":
                return new EmbWriterPEC();
            case "pes":
                return new EmbWriterPES();
            case "emm":
                return new EmbWriterEmm();
            case "jef":
                return new EmbWriterJEF();
            case "png":
                return new FormatPng();
            case "svg":
                return new EmbWriterSVG();
            default:
                return null;
        }
    }

    public interface Reader {
        void read(EmbPattern pattern, InputStream stream) throws IOException;
    }

    public interface Writer {
        void write(EmbPattern pattern, OutputStream stream) throws IOException;
    }
}
