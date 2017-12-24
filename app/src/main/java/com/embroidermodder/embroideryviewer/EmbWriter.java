package com.embroidermodder.embroideryviewer;

import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Point;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Stack;

public abstract class EmbWriter implements IFormat.Writer {

    public static final int LOCK_STITCHES = 1;

    protected Stack<OutputStream> streamStack;
    protected OutputStream stream;
    protected EmbPattern pattern;

    private int settings;

    public void write(EmbPattern pattern, OutputStream stream) throws IOException {
        this.stream = stream;
        this.pattern = pattern;
        preWrite(pattern);
        write();
        postWrite(pattern);
    }

    public abstract void write() throws IOException;

    public void preWrite(EmbPattern input) {
    }

    public void postWrite(EmbPattern input) {
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

    public void writeInt32(int value) throws IOException {
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

    public boolean isLockStitches() {
        return (settings & LOCK_STITCHES) != 0;
    }

    public void setSettings(int settings) {
        this.settings = settings;
    }

    public String getName() {
        return pattern.getName();
    }

    public Point getFirstPosition() {
        DataPoints stitches = pattern.getStitches();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int flags = stitches.getData(i);
            switch (flags) {
                case EmbPattern.INIT:
                case EmbPattern.STITCH:
                case EmbPattern.JUMP:
                    return stitches.getPoint(i);
            }
        }
        return null;
    }

    public ArrayList<EmbThread> getUniqueThreads() {
        ArrayList<EmbThread> threads = new ArrayList<>();
        for (EmbObject object : pattern.asStitchEmbObjects()) {
            EmbThread thread = object.getThread();
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
                case EmbPattern.COLOR_CHANGE:
                    count++;
            }
        }
        return count;
    }

    public int getSegmentCount() {
        int count = 0;
        DataPoints stitches = pattern.getStitches();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int flags = stitches.getData(i);
            switch (flags) {
                case EmbPattern.STITCH:
                case EmbPattern.JUMP:
                    count++;
            }
        }
        return count;
    }

    public int[] getThreadUseOrder() {
        ArrayList<EmbThread> colors = getThreads();
        ArrayList<EmbThread> uniquelist = getUniqueThreads();

        int[] useorder = new int[colors.size()];
        for (int i = 0, s = colors.size(); i < s; i++) {
            useorder[i] = uniquelist.indexOf(colors.get(i));
        }
        return useorder;
    }

    public ArrayList<EmbThread> getThreads() {
        ArrayList<EmbThread> threads = new ArrayList<>();
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


}
