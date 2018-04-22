package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public abstract class ReadHelper implements IFormat.Reader {
    private byte[] BYTE4 = new byte[4];
    private byte[] BYTE3 = new byte[3];
    private byte[] BYTE2 = new byte[2];
    private byte[] BYTE1 = new byte[1];

    protected InputStream stream;
    protected int readPosition = 0;

    public ReadHelper() {
    }

    public ReadHelper(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void close() {
        stream = null;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    protected abstract void read() throws IOException;

    public int readInt32LE() throws IOException {
        byte fullInt[] = BYTE4;
        readFully(fullInt);
        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8) + ((fullInt[2] & 0xFF) << 16) + ((fullInt[3] & 0xFF) << 24);
    }

    public int readInt32BE() throws IOException {
        byte fullInt[] = BYTE4;
        readFully(fullInt);
        return (fullInt[3] & 0xFF) + ((fullInt[2] & 0xFF) << 8) + ((fullInt[1] & 0xFF) << 16) + ((fullInt[0] & 0xFF) << 24);
    }

    public int readInt24BE() throws IOException {
        byte fullInt[] = BYTE3;
        readFully(fullInt);
        return (fullInt[2] & 0xFF) + ((fullInt[1] & 0xFF) << 8) + ((fullInt[0] & 0xFF) << 16);
    }

    public int readInt24LE() throws IOException {
        byte fullInt[] = BYTE3;
        readFully(fullInt);
        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8) + ((fullInt[2] & 0xFF) << 16);
    }

    public int readInt16LE() throws IOException {
        byte fullInt[] = BYTE2;
        readFully(fullInt);
        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8);
    }

    public int readInt16BE() throws IOException {
        byte fullInt[] = BYTE2;
        readFully(fullInt);
        return (fullInt[1] & 0xFF) + ((fullInt[0] & 0xFF) << 8);
    }

    public int readInt8() throws IOException {
        byte fullInt[] = BYTE1;
        readFully(fullInt);
        return (fullInt[0] & 0xFF);
    }

    public int readFully(byte[] data) throws IOException {
        InputStream s = stream;
        if (s == null) {
            throw new IOException("Stream does not exist.");
        }
        int offset = 0;
        int bytesRead;
        boolean read = false;
        while ((bytesRead = s.read(data, offset, data.length - offset)) != -1) {
            read = true;
            offset += bytesRead;
            if (offset >= data.length) {
                break;
            }
        }

        if (read) {
            readPosition += offset;
        }
        return (read) ? offset : -1;
    }

    public String readString(int maxLength) throws IOException {
        String s = readString(stream, maxLength);
        readPosition += s.length();
        return s;
    }

    private static String readString(InputStream stream, int maxLength) throws IOException {
        if (stream == null) {
            throw new IOException("Stream does not exist.");
        }
        ArrayList<Byte> charList = new ArrayList<>();
        int i = 0;
        while (i < maxLength) {
            int value = stream.read();
            if (value == '\0') {
                break;
            }
            charList.add((byte) value);
            i++;
        }
        byte[] result = new byte[charList.size()];
        for (i = 0; i < charList.size(); i++) {
            result[i] = charList.get(i);
        }
        return new String(result, "UTF-8");
    }

    public synchronized void skip(int amount) throws IOException {
        readPosition += amount;
        InputStream s = stream;
        if (s == null) {
            throw new IOException("Stream does not exist.");
        }
        s.skip(amount);
    }

    public int getReadPosition() {
        return readPosition;
    }
}
