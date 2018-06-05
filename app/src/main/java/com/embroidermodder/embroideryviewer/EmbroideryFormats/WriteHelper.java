package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;


public class WriteHelper {
    protected Stack<OutputStream> streamStack;
    protected OutputStream stream;

    public WriteHelper() {
    }

    public WriteHelper(OutputStream stream) {
        this.stream = stream;
    }

    public void setStream(OutputStream stream) {
        this.stream = stream;
    }

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

    public void writeInt8(int value) throws IOException {
        stream.write(value);
    }

    public void writeInt16LE(int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
    }

    public void writeInt16BE(int value) throws IOException {
        stream.write((value >> 8) & 0xFF);
        stream.write(value & 0xFF);
    }

    public void writeInt24LE(int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
    }

    public void writeInt24BE(int value) throws IOException {
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write(value & 0xFF);
    }

    public void writeInt32(int value) throws IOException { //Little endian.
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
