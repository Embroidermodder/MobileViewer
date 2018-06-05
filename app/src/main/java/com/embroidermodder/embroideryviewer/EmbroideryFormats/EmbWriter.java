package com.embroidermodder.embroideryviewer.EmbroideryFormats;


import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Point;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.COLOR_CHANGE;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.END;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.JUMP;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STITCH;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STOP;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.TRIM;


public abstract class EmbWriter extends WriteHelper implements IFormat.Writer {
    protected EmbPattern pattern;
    int settings;

    public void write(EmbPattern pattern, OutputStream stream) throws IOException {
        this.stream = stream;
        this.pattern = pattern;
        write();
    }

    public abstract void write() throws IOException;

    @Override
    public double maxJumpDistance() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double maxStitchDistance() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean canEncode(int encode) {
        switch (encode) {
            case STOP:
            case COLOR_CHANGE:
            case STITCH:
            case JUMP:
            case TRIM:
            case END:
                return true;
        }
        return false;
    }

    @Override
    public boolean hasColor() {
        return true;
    }

    @Override
    public boolean hasStitches() {
        return true;
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
                case STITCH:
                case JUMP:
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

    @Override
    public int getSettings() {
        return settings;
    }

    @Override
    public void setSettings(int settings) {
        this.settings = settings;
    }
}
