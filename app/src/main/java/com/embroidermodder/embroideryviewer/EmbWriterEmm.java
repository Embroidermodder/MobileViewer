package com.embroidermodder.embroideryviewer;

import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Point;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static com.embroidermodder.embroideryviewer.EmmPattern.COLOR_CHANGE;
import static com.embroidermodder.embroideryviewer.EmmPattern.JUMP;
import static com.embroidermodder.embroideryviewer.EmmPattern.STITCH;

public class EmbWriterEmm {
    public static final int MAGIC_NUMBER = 0xE3B830DD; //EMBRMODD
    public static final int VERSION = 1;
    protected EmmPattern pattern;
    protected Stack<OutputStream> streamStack;
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
        writeInt32(thread.color);
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

    public String getName() {
        return pattern.getName();
    }


    public ArrayList<EmmThread> getUniqueThreads() {
        ArrayList<EmmThread> threads = new ArrayList<>();
        for (EmbObject object : pattern.asStitchEmbObjects()) {
            EmmThread thread = object.getThread();
            threads.remove(threads);
            threads.add(thread);
        }
        return threads;
    }

    public int getColorChanges() {
        int count = 0;
        DataPoints stitches = pattern.getStitches();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int flags = stitches.getData(i);
            switch (flags) {
                case COLOR_CHANGE:
                    count++;
            }
        }
        return count;
    }

    public int getStitchJumpCount() {
        int count = 0;
        DataPoints stitches = pattern.getStitches();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int flags = stitches.getData(i);
            switch (flags) {
                case STITCH:
                case JUMP:
                    count++;
            }
        }
        return count;
    }

    public int[] getThreadUseOrder() {
        ArrayList<EmmThread> colors = getThreads();
        ArrayList<EmmThread> uniquelist = getUniqueThreads();

        int[] useorder = new int[colors.size()];
        for (int i = 0, s = colors.size(); i < s; i++) {
            useorder[i] = uniquelist.indexOf(colors.get(i));
        }
        return useorder;
    }

    public ArrayList<EmmThread> getThreads() {
        ArrayList<EmmThread> threads = new ArrayList<>();
        for (EmbObject object : pattern.asStitchEmbObjects()) {
            threads.add(object.getThread());
        }
        return threads;
    }

    public void translate(float x, float y) {
        DataPoints stitches = pattern.getStitches();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            stitches.translate(x, y);
        }
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