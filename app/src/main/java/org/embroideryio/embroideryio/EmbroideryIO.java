package org.embroideryio.embroideryio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EmbroideryIO {

    public static void setSettings(BaseIO obj, Object... settings) {
        for (int i = 0, ie = settings.length; i < ie; i += 2) {
            if (settings[i] instanceof String) {
                obj.set((String) settings[i], settings[i + 1]);
            }
        }
    }

    public static void writeEmbroidery(EmbPattern pattern, Writer writer, OutputStream out) throws IOException {
        EmbEncoder coder = new EmbEncoder();
        coder.setDefaultIO(writer);
        EmbPattern out_pattern = new EmbPattern();
        coder.transcode(pattern, out_pattern);
        writer.write(out_pattern, out);
    }


    public static void writeStream(EmbPattern pattern, String path, OutputStream out, Object... settings) throws IOException {
        EmbroideryIO.Writer writer = EmbroideryIO.getWriterByFilename(path);
        if (writer != null) {
            setSettings(writer, settings);
            writeEmbroidery(pattern, writer, out);
        }
    }

    public static void write(EmbPattern pattern, String path, Object... settings) throws IOException {
        FileOutputStream out = null;
        EmbroideryIO.Writer writer = EmbroideryIO.getWriterByFilename(path);
        try {
            if (writer != null) {
                setSettings(writer, settings);
                File file = new File(path);
                file.createNewFile();
                out = new FileOutputStream(file);
                writeEmbroidery(pattern, writer, out);
                out.flush();
                out.close();
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static EmbPattern readEmbroidery(Reader reader, InputStream in) throws IOException {
        EmbPattern pattern = new EmbPattern();
        reader.read(pattern, in);
        return pattern;
    }

    public static EmbPattern readStream(String path, InputStream out, Object... settings) throws IOException {
        EmbroideryIO.Reader reader = EmbroideryIO.getReaderByFilename(path);
        if (reader != null) {
            setSettings(reader, settings);
            return readEmbroidery(reader, out);
        }
        return null;
    }

    public static EmbPattern read(String path, Object... settings) throws IOException {
        File input = new File(path);
        if (input.exists()) {
            FileInputStream instream = null;
            try {
                instream = new FileInputStream(input);
                EmbroideryIO.Reader reader = EmbroideryIO.getReaderByFilename(path);
                if (reader == null) {
                    return null;
                }
                setSettings(reader, settings);
                return readEmbroidery(reader, instream);
            } catch (IOException ex) {
                throw ex;
            } finally {
                if (instream != null) {
                    instream.close();
                }
            }
        }
        return null;
    }

    public static String getExtentionByFileName(String name) {
        if (name == null) {
            return null;
        }
        String[] split = name.split("\\.");
        if (split.length <= 1) {
            return null;
        }
        return split[split.length - 1].toLowerCase();
    }

    public static EmbroideryIO.Reader getReaderByFilename(String filename) {
        String ext = getExtentionByFileName(filename);
        if (ext == null) {
            return null;
        }
        switch (ext) {
            case "emm":
                return new EmmReader();
            case "col":
                return new ColReader();
            case "inf":
                return new InfReader();
            case "exp":
                return new ExpReader();
            case "dst":
                return new DstReader();
            case "jef":
                return new JefReader();
            case "pcs":
                return new PcsReader();
            case "pec":
                return new PecReader();
            case "pes":
                return new PesReader();
            case "sew":
                return new SewReader();
            case "shv":
                return new ShvReader();
            case "vp3":
                return new Vp3Reader();
            case "xxx":
                return new XxxReader();
            default:
                return null;
        }
    }

    public static Reader getReaderByMime(String mime) {
        //TODO: Implement the mime type checker.
        return null;
    }

    public static EmbroideryIO.Writer getWriterByFilename(String filename) {
        String ext = getExtentionByFileName(filename);
        if (ext == null) {
            return null;
        }
        switch (ext) {
            case "exp":
                return new ExpWriter();
            case "dst":
                return new DstWriter();
            case "pec":
                return new PecWriter();
            case "pes":
                return new PesWriter();
            case "emm":
                return new EmmWriter();
            case "jef":
                return new JefWriter();
            case "pcs":
                return new PcsWriter();
            default:
                return null;
        }
    }

    public static Writer getWriterByMime(String mime) {
        //TODO: Implement the meme type writer.
        return null;
    }

    public interface BaseIO {

        void set(String key, Object value);

        Object get(String key);

        Object get(String key, Object default_value);
    }

    public interface Reader extends BaseIO {

        void read(EmbPattern pattern, InputStream stream) throws IOException;
    }

    public interface Writer extends BaseIO {

        void write(EmbPattern pattern, OutputStream stream) throws IOException;
    }
}
