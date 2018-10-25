package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.embroidermodder.embroideryviewer.EmbWriterEmm.MAGIC_NUMBER;


public class EmbReaderEmm {
    protected InputStream stream;
    protected int readPosition = 0;
    protected int colorIndex = 0;
    protected double lastx = 0;
    protected double lasty = 0;
    HashMap<String,String> map = new HashMap<>();
    EmmPattern pattern;
    private byte[] BYTE4 = new byte[4];
    private byte[] BYTE3 = new byte[3];
    private byte[] BYTE2 = new byte[2];
    private byte[] BYTE1 = new byte[1];

    public EmbReaderEmm() {
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

    protected void read() throws IOException {
        int magic_number = readInt32LE();
        if (magic_number != MAGIC_NUMBER) return;
        int version = readInt32LE();
        if (version != EmbWriterEmm.VERSION) return;
        readVersion1();
        this.pattern.getStitches().read(stream);
    }

    public void readVersion1() throws IOException {
        int thread_count = readInt32LE();
        for (int i = 0; i < thread_count; i++) {
            EmmThread thread = readThread();
            pattern.addThread(thread);
        }
        pattern.setMetadata(readMap());
    }

    public EmmThread readThread() throws IOException {
        EmmThread thread = new EmmThread();
        thread.setColor(readInt32LE());
        thread.setMetadata(readMap());
        return thread;
    }

    public HashMap<String, String> readMap() throws IOException {
        map.clear();
        int size = readInt32LE();
        for (int i = 0; i < size; i++) {
            int ks = readInt32LE();
            String key = readString(ks);
            int vs = readInt32LE();
            String value = readString(vs);
            map.put(key,value);
        }
        return map;
    }

    public void read(EmmPattern pattern, InputStream stream) throws IOException {
        colorIndex = 0;
        readPosition = 0;
        this.stream = stream;
        this.pattern = pattern;
        read();
    }

    public void stitchAbs(double x, double y) {
        lastx = x;
        lasty = y;
        pattern.add(lastx,lasty,EmmPattern.STITCH);
    }

    public void stitch(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty, EmmPattern.STITCH);
    }

    public void moveAbs(double x, double y) {
        lastx = x;
        lasty = y;
        pattern.add(lastx,lasty,EmmPattern.JUMP);
    }

    public void move(double dx, double dy) {
        lastx = lastx + dx;
        lasty = lasty + dy;
        pattern.add(lastx,lasty,EmmPattern.JUMP);
    }

    public void changeColor() {
        pattern.add(lastx,lasty, EmmPattern.COLOR_CHANGE);
        colorIndex++;
    }

    public void trim() {
        pattern.add(lastx,lasty,EmmPattern.TRIM);
    }

    public void stop() {
        pattern.add(lastx,lasty, EmmPattern.STOP);
    }

    public void end() {
        pattern.add(lastx,lasty,EmmPattern.END);
    }

    public void setName(String name) {
        pattern.name = name;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
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