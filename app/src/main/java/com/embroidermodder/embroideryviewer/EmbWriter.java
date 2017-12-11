package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

public abstract class EmbWriter implements IFormat.Writer {
    protected Stack<OutputStream> streamStack;
    protected OutputStream stream;
    protected EmbPattern pattern;

    public void setObjects(EmbPattern embroideryPattern, OutputStream stream) {
        this.pattern = embroideryPattern;
        this.stream = stream;
    }

    public void write(EmbPattern embroideryPattern, OutputStream stream) throws IOException {
        setObjects(embroideryPattern, stream);
        write();
    }

    public abstract void write() throws IOException;

    public void push(OutputStream push) {
        if (streamStack == null) {
            streamStack = new Stack<>();
        }
        streamStack.push(stream);
        stream = push;
    }

    public OutputStream pop() {
        if (streamStack == null) {
            return null;
        }
        if (streamStack.isEmpty()) {
            return null;
        }
        OutputStream pop = stream;
        stream = streamStack.pop();
        return pop;
    }

    public void writeInt16LE(int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
    }

    public void writeInt16BE(int value) throws IOException {
        stream.write((value >> 8) & 0xFF);
        stream.write(value & 0xFF);
    }

    public void writeInt8(int value) throws IOException {
        stream.write(value);
    }

    public void writeInt24LE(int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
    }

    public void writeInt32(int value) throws IOException { //writes little ended.
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 24) & 0xFF);
    }

    public void writeInt32LE(int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 24) & 0xFF);
    }

    public void writeInt32BE(int value) throws IOException {
        stream.write((value >> 24) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write(value & 0xFF);
    }

    public void write(byte[] bytes) throws IOException {
        stream.write(bytes);
    }

    public void write(String string) throws IOException {
        stream.write(string.getBytes());
    }
}