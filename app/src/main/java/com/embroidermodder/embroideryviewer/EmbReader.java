package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public abstract class EmbReader implements IFormat.Reader {
    byte[] BYTE4 = new byte[4];
    byte[] BYTE3 = new byte[3];
    byte[] BYTE2 = new byte[2];
    byte[] BYTE1 = new byte[1];

    protected InputStream stream;
    EmbPattern pattern;

    protected int readPosition = 0;
    protected int colorIndex = 0;

    protected double lastx = 0;
    protected double lasty = 0;

    public void read(EmbPattern pattern, InputStream stream) throws IOException {
        colorIndex = 0;
        readPosition = 0;
        this.stream = stream;
        this.pattern = pattern;
        preRead(pattern);
        read();
        postRead(pattern);
    }

    protected abstract void read() throws IOException;

    public void preRead(EmbPattern input) {
    }

    public void postRead(EmbPattern input) {
    }

    public void stitchAbs(double x, double y) {
        lastx = x;
        lasty = y;
        pattern.add(lastx,lasty,EmbPattern.STITCH);
    }

    public void stitch(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty, EmbPattern.STITCH);
    }

    public void moveAbs(double x, double y) {
        lastx = x;
        lasty = y;
        pattern.add(lastx,lasty,EmbPattern.JUMP);
    }

    public void move(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty,EmbPattern.JUMP);
    }

    public void changeColor() {
        pattern.add(lastx,lasty, EmbPattern.COLOR_CHANGE);
        colorIndex++;
    }

    public void trim() {
        pattern.add(lastx,lasty,EmbPattern.TRIM);
    }

    public void stop() {
        pattern.add(lastx,lasty, EmbPattern.STOP);
    }

    public void end() {
        pattern.add(lastx,lasty,EmbPattern.END);
    }

    public void setName(String name) {
        pattern.name = name;
    }

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
        if (readFully(fullInt) != fullInt.length) {
            return -1;
        }
        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8) + ((fullInt[2] & 0xFF) << 16);
    }

    public int readInt16LE() throws IOException {
        byte fullInt[] = BYTE2;
        if (readFully(fullInt) != fullInt.length) {
            return -1;
        }
        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8);
    }

    public int readInt16BE() throws IOException {
        byte fullInt[] = BYTE2;
        if (readFully(fullInt) != fullInt.length) {
            return -1;
        }
        return (fullInt[1] & 0xFF) + ((fullInt[0] & 0xFF) << 8);
    }

    public int readInt8() throws IOException {
        byte fullInt[] = BYTE1;
        if (readFully(fullInt) != fullInt.length) {
            return -1;
        }
        return (fullInt[0] & 0xFF);
    }

    public int readFully(byte[] data) throws IOException {
        int offset = 0;
        int bytesRead;
        boolean read = false;
        while ((bytesRead = stream.read(data, offset, data.length - offset)) != -1) {
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

    public void skip(int amount) throws IOException {
        readPosition += amount;
        stream.skip(amount);
    }

}
