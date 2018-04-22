package com.embroidermodder.embroideryviewer.EmbroideryFormats;


import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Geometry2D;
import com.embroidermodder.embroideryviewer.geom.Point;
import com.embroidermodder.embroideryviewer.geom.Points;

import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.COLOR_CHANGE;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.COMMAND_MASK;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.END;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.JUMP;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STITCH;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STITCH_FINAL_COLOR;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STITCH_FINAL_LOCATION;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STITCH_NEW_COLOR;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STITCH_NEW_LOCATION;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.TRIM;

/**
 * Created by Tat on 12/22/2017.
 */

public class WriteEncoder {
    public double maxJumpLength = Double.POSITIVE_INFINITY;
    public double maxStitchLength = Double.POSITIVE_INFINITY;

    public boolean tie_on = false;
    public boolean tie_off = false;

    private double needle_X = 0;
    private double needle_Y = 0;

    private double translate_X = 0;
    private double translate_Y = 0;

    public void setTranslation(double x, double y) {
        translate_X = x;
        translate_Y = y;
    }

    private void jumpTo(DataPoints transcode, double x, double y) {
        stepToRange(transcode, x, y, maxJumpLength, JUMP);
        transcode.add((float) x, (float) y, JUMP);
    }

    private void stitchTo(DataPoints transcode, double x, double y) {
        stepToRange(transcode, x, y, maxStitchLength, STITCH);
        transcode.add((float) x, (float) y, STITCH);
    }

    private void stepToRange(DataPoints transcode, double x, double y, double length, int data) {
        double distanceX = x - needle_X;
        double distanceY = y - needle_Y;
        if ((Math.abs(distanceX) > length) || (Math.abs(distanceY) > length)) {
            double stepsX = Math.ceil(Math.abs(distanceX / length));
            double stepsY = Math.ceil(Math.abs(distanceY / length));
            double steps = Math.max(stepsX, stepsY);
            double stepSizeX, stepSizeY;
            if (stepsX > stepsY) {
                stepSizeX = distanceX / stepsX;
                stepSizeY = distanceY / stepsX;
            } else {
                stepSizeX = distanceX / stepsY;
                stepSizeY = distanceY / stepsY;
            }
            for (double q = 0, qe = steps, qx = needle_X, qy = needle_Y; q < qe; q += 1, qx += stepSizeX, qy += stepSizeY) {
                transcode.add((float) Math.rint(qx), (float) Math.rint(qy), data);
            }
        }
    }

    private void lockStitch(DataPoints transcode, double lockposition_x, double lockposition_y, double towards_x, double towards_y) {
        if (Geometry2D.distance(lockposition_x, lockposition_y, towards_x, towards_y) > maxStitchLength) {
            Point polar = Geometry2D.oriented(lockposition_x, lockposition_y, towards_x, towards_y, maxStitchLength);
            towards_x = polar.getX();
            towards_y = polar.getY();
        }
        stitchTo(transcode, lockposition_x, lockposition_y);
        stitchTo(transcode, Geometry2D.towards(lockposition_x, towards_x, .33), Geometry2D.towards(lockposition_y, towards_y, .33));
        stitchTo(transcode, Geometry2D.towards(lockposition_x, towards_x, .66), Geometry2D.towards(lockposition_y, towards_y, .66));
        stitchTo(transcode, Geometry2D.towards(lockposition_x, towards_x, .33), Geometry2D.towards(lockposition_y, towards_y, .33));
    }

    public void writeCode(EmbPattern p) {
        EmbPattern copy = new EmbPattern(p);
        Points layer = copy.getStitches();
        for (int i = 0, size = layer.size(); i < size; i++) { //snap & translate.
            layer.setLocation(i, (float) Math.rint(layer.getX(i) - translate_X), (float) Math.rint(layer.getY(i) - translate_Y));
        }
        p.getStitches().clear();
        p.getThreadlist().clear();
        writeCode(copy, p);
        writeThread(copy, p);
    }

    private void writeThread(EmbPattern from, EmbPattern to) {
        to.getThreadlist().addAll(from.getThreadlist());
    }

    private void writeCode(EmbPattern from, EmbPattern to) {
        DataPoints fromPoints = from.getStitches();
        DataPoints toPoints = to.getStitches();
        int currentIndexEnd = fromPoints.size();
        int currentIndex = 0;
        while (currentIndex < currentIndexEnd) {
            int processingCommand = fromPoints.getData(currentIndex) & COMMAND_MASK;
            double current_x = fromPoints.getX(currentIndex);
            double current_y = fromPoints.getY(currentIndex);
            switch (processingCommand) {
                case STITCH:
                    stitchTo(toPoints, current_x, current_y);
                    break;
                case STITCH_FINAL_LOCATION:
                    if (tie_off) {
                        int bi = currentIndex - 1;
                        double bx = fromPoints.getX(bi);
                        double by = fromPoints.getY(bi);
                        lockStitch(toPoints, current_x, current_y, bx, by);
                    }
                    stitchTo(toPoints, current_x, current_y);
                    toPoints.add((float) current_x, (float) current_y, TRIM);
                    break;
                case STITCH_FINAL_COLOR:
                    if (tie_off) {
                        int bi = currentIndex - 1;
                        double bx = fromPoints.getX(bi);
                        double by = fromPoints.getY(bi);
                        lockStitch(toPoints, current_x, current_y, bx, by);
                    }
                    stitchTo(toPoints, current_x, current_y);
                    toPoints.add((float) current_x, (float) current_y, TRIM);
                    toPoints.add((float) current_x, (float) current_y, COLOR_CHANGE);
                    break;
                case STITCH_NEW_LOCATION:
                case STITCH_NEW_COLOR:
                    jumpTo(toPoints, current_x, current_y);
                    needle_X = current_x;
                    needle_Y = current_y;
                    if (tie_on) {
                        int bi = currentIndex + 1;
                        double bx = fromPoints.getX(bi);
                        double by = fromPoints.getY(bi);
                        lockStitch(toPoints, current_x, current_y, bx, by);
                    }
                    toPoints.add((float) current_x, (float) current_y, STITCH);
                    break;
                default:
                    toPoints.add((float) current_x, (float) current_y, fromPoints.getData(currentIndex));
                    break;
            }
            needle_X = current_x;
            needle_Y = current_y;
            currentIndex++;
        }
        toPoints.add((float) needle_X, (float) needle_X, END);
    }


    public static WriteEncoder getEncoder() {
        return new WriteEncoder();
    }

}
