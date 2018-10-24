package com.embroidermodder.embroideryviewer.EmbroideryFormats;


import com.embroidermodder.embroideryviewer.EmmPattern;
import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Point;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.embroidermodder.embroideryviewer.EmmPattern.COLOR_CHANGE;
import static com.embroidermodder.embroideryviewer.EmmPattern.JUMP;
import static com.embroidermodder.embroideryviewer.EmmPattern.STITCH;


public abstract class EmbWriter extends WriteHelper {
    protected EmmPattern pattern;

    public void write(EmmPattern pattern, OutputStream stream) throws IOException {
        this.stream = stream;
        this.pattern = pattern;
        write();
    }

    public abstract void write() throws IOException;

    public String getName() {
        return pattern.getName();
    }

    public Point getFirstPosition() {
        DataPoints stitches = pattern.getStitches();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int flags = stitches.getData(i);
            switch (flags) {
                case EmmPattern.INIT:
                case STITCH:
                case JUMP:
                    return stitches.getPoint(i);
            }
        }
        return null;
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
}
