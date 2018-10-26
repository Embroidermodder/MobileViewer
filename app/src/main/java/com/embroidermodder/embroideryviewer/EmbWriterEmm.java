package com.embroidermodder.embroideryviewer;


import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class EmbWriterEmm {
    public static final int MAGIC_NUMBER = 0xE3B830DD; //EMBRMODD
    public static final int VERSION = 1;
    protected EmmPattern pattern;
    protected OutputStream stream;

    public EmbWriterEmm() {
    }

    public void write() throws IOException {
        writeInt32LE(MAGIC_NUMBER);
        writeInt32LE(VERSION);
        writeVersion1();
        pattern.getStitches().write(stream);
    }

    public void writeVersion1() throws IOException {
        int threadcount = pattern.getThreadCount();
        writeInt32LE(threadcount);
        if (threadcount != 0) {
            for (EmmThread thread : pattern.getThreadlist()) {
                writeThread(thread);
            }
        }
        HashMap<String, String> metadata = pattern.getMetadata();
        writeMap(metadata);
    }

    public void writeThread(EmmThread thread) throws IOException {
        writeInt32LE(thread.color);
        HashMap<String, String> metadata = thread.getMetadata();
        writeMap(metadata);
    }

    public void writeMap(Map<String, String> map) throws IOException {
        byte[] bytes;
        writeInt32LE(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            bytes = entry.getKey().getBytes();
            writeInt32LE(bytes.length);
            write(bytes);
            bytes = entry.getValue().getBytes();
            writeInt32LE(bytes.length);
            write(bytes);
        }
    }

    public void write(EmmPattern pattern, OutputStream stream) throws IOException {
        this.stream = stream;
        this.pattern = pattern;
        write();
    }

    public void writeInt32LE(int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 24) & 0xFF);
    }


    public void write(byte[] bytes) throws IOException {
        stream.write(bytes);
    }

    public void write(String string) throws IOException {
        stream.write(string.getBytes());
    }
}