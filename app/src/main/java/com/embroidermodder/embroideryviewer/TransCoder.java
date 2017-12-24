package com.embroidermodder.embroideryviewer;

import com.embroidermodder.embroideryviewer.geom.DataPoints;

import static com.embroidermodder.embroideryviewer.EmbPattern.COLOR_CHANGE;
import static com.embroidermodder.embroideryviewer.EmbPattern.COMMAND_MASK;
import static com.embroidermodder.embroideryviewer.EmbPattern.INIT;
import static com.embroidermodder.embroideryviewer.EmbPattern.JUMP;
import static com.embroidermodder.embroideryviewer.EmbPattern.NO_COMMAND;
import static com.embroidermodder.embroideryviewer.EmbPattern.STITCH;
import static com.embroidermodder.embroideryviewer.EmbPattern.STOP;
import static com.embroidermodder.embroideryviewer.EmbPattern.TIE_OFF;
import static com.embroidermodder.embroideryviewer.EmbPattern.TIE_ON;
import static com.embroidermodder.embroideryviewer.EmbPattern.TRIM;


/**
 * Created by Tat on 12/22/2017.
 */

public class TransCoder {
    int jumps_before_trim = 3;

    boolean splitLongJumps = true;
    double maxJumpLength = Double.POSITIVE_INFINITY;
    boolean splitLongStitches = true;
    double maxStitchLength = Double.POSITIVE_INFINITY;

    boolean snap = true;
    boolean combineTrimmedJumps = true;
    boolean tie_on = false;
    boolean tie_off = false;
    boolean fix_color_count = false;

    public boolean require_jumps = true;
    private double initial_x = Double.NaN;
    private double initial_y = Double.NaN;

    int sequenceEnd;
    int currentCommand;
    int nextSequenceCommand;
    boolean trimmed;
    double nextSequenceX;
    double nextSequenceY;
    int colorIndex;
    double lastx;
    double lasty;
    int lastCommand;

    public TransCoder() {
        sequenceEnd = 0;
        currentCommand = NO_COMMAND;
        lastx = Double.NaN;
        lasty = Double.NaN;
        nextSequenceCommand = NO_COMMAND;
        nextSequenceX = Double.NaN;
        nextSequenceY = Double.NaN;
        colorIndex = 0;
        trimmed = true;
    }

    public void setInitialPosition(double x, double y) {
        this.initial_x = x;
        this.initial_y = y;
    }

    private void lookahead(DataPoints dataPoints, int index) {
        nextSequenceCommand = -1;
        int currentCommand = dataPoints.getData(index);
        for (int j = index + 1, je = dataPoints.size(); j < je; j++) {
            int lookAheadCommand = dataPoints.getData(j);
            if (currentCommand != lookAheadCommand) {
                nextSequenceCommand = lookAheadCommand;
                nextSequenceX = dataPoints.getX(j - 1);
                nextSequenceY = dataPoints.getY(j - 1);
                sequenceEnd = j - index;
                break;
            }
        } //look ahead.
    }

    private void trimReady(DataPoints transcode, double x, double y) {
        if (trimmed) return;
        if ((tie_off) && (currentCommand != TIE_OFF)) {
            transcode.add((float) x, (float) y, TIE_OFF);
        }
        transcode.add((float) x, (float) y, TRIM);
        trimmed = true;
    }

    private void stitchReady(DataPoints transcode,double x, double y) {
        if (!trimmed) return;
        if ((require_jumps) && (currentCommand == TRIM)) {
            jumpTo(transcode,x, y);
        }
        if ((trimmed && tie_on) && (currentCommand != TIE_ON)) {
            transcode.add((float) x, (float) y, TIE_ON);
        }
        trimmed = false;
    }

    private void jumpTo(DataPoints transcode,double x, double y) {
        if (splitLongJumps) {
            stepToRange(transcode,x, y, maxJumpLength, JUMP);
        }
        transcode.add((float) x, (float) y, JUMP);
    }

    private void stitchTo(DataPoints transcode,double x, double y) {
        if (splitLongStitches) {
            stepToRange(transcode,x, y, maxStitchLength, STITCH);
        }
        transcode.add((float) x, (float) y, STITCH);
    }

    private void stepToRange(DataPoints transcode,double x, double y, double length, int data) {
        double distanceX = lastx - x;
        double distanceY = lasty - y;
        if ((Math.abs(distanceX) > length) || (Math.abs(distanceY) > length)) {
            double stepsX = Math.abs(Math.ceil(distanceX / length));
            double stepsY = Math.abs(Math.ceil(distanceY / length));
            double steps = Math.max(stepsX, stepsY);
            double stepSizeX, stepSizeY;
            if (stepsX > stepsY) {
                stepSizeX = distanceX / stepsX;
                stepSizeY = distanceY / stepsX;
            } else {
                stepSizeX = distanceX / stepsY;
                stepSizeY = distanceY / stepsY;
            }
            for (double q = 0, qe = steps - 1, qx = lastx, qy = lasty; q < qe; q += 1, qx += stepSizeX, qy += stepSizeY) {
                transcode.add((float) Math.rint(qx), (float) Math.rint(qy), data);
            }
        }
    }


    public void transcode(EmbPattern source) {
        EmbPattern copy = new EmbPattern(source);
        source.getStitches().clear();
        source.getThreadlist().clear();
        transcode(copy,source);
    }

    public void transcode(EmbPattern from, EmbPattern to) {
        DataPoints stitches = from.getStitches();
        DataPoints transcode = to.getStitches();
        int currentIndexEnd = stitches.size();
        int currentIndex = 0;
        while (currentIndex < currentIndexEnd) {
            int currentCommand = stitches.getData(currentIndex) & COMMAND_MASK;
            double x = stitches.getX(currentIndex);
            double y = stitches.getY(currentIndex);
            if (sequenceEnd <= 0) {
                lookahead(stitches,currentIndex);
            }
            if ((currentCommand == NO_COMMAND) && (currentCommand != INIT)) {
                if (!Double.isNaN(initial_y) && !Double.isNaN(initial_x)) {
                    transcode.add((float) initial_x, (float) initial_y, INIT);
                    transcode.add((float) x, (float) y, JUMP);
                } else {
                    transcode.add((float) x, (float) y, INIT);
                }
            }
            switch (currentCommand) {
                case INIT:
                    initial_x = x;
                    initial_y = y;
                    transcode.add((float) x, (float) y, INIT);
                    break;
                case TRIM:
                    trimReady(transcode,x, y);
                    break;
                case STITCH:
                    stitchReady(transcode,x, y);
                    stitchTo(transcode,x, y);
                    break;
                case JUMP:
                    if ((nextSequenceCommand == COLOR_CHANGE) ||
                            ((currentCommand == STITCH) && ((sequenceEnd - currentIndex) >= jumps_before_trim))) {
                        trimReady(transcode,x, y);
                    }
                    if (trimmed & combineTrimmedJumps) {
                        x = nextSequenceX;
                        y = nextSequenceY;
                        currentIndex += sequenceEnd-1;
                        sequenceEnd = 1;//currentIndex;
                    }
                    jumpTo(transcode,x, y);
                    break;
                case COLOR_CHANGE:
                    trimReady(transcode,x, y);
                    if (fix_color_count) {
                        if (from.getThreadCount() >= colorIndex) {
                            to.addThread(from.getThreadOrFiller(colorIndex));
                        }
                    }
                    transcode.add((float) x, (float) y, COLOR_CHANGE | (colorIndex++ << 8));
                    break;
                case STOP:
                    trimReady(transcode,x,y);
                    transcode.add((float) x, (float) y, STOP);
                    break;
                case TIE_OFF:
                case TIE_ON:

                default:
                    transcode.add((float) x, (float) y, stitches.getData(currentIndex));
                    break;
            }
            lastCommand = currentCommand;
            lastx = x;
            lasty = y;
            sequenceEnd--;
            currentIndex++;
        }
    }

    public static TransCoder getTranscoder() {
        return new TransCoder();
    }
}
