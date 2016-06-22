package com.embroidermodder.embroideryviewer;

import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.ArrayList;

public class Pattern {
    private static final double PIXELS_PER_MM = 10;
    private final ArrayList<StitchBlock> _stitchBlocks;
    private final ArrayList<EmbThread> _threadList;
    private String _filename;
    private StitchBlock _currentStitchBlock;

    private double _previousX = 0;
    private double _previousY = 0;

    public Pattern() {
        _stitchBlocks = new ArrayList<>();
        _threadList = new ArrayList<>();
        _currentStitchBlock = null;
    }

    public ArrayList<StitchBlock> getStitchBlocks() {
        return _stitchBlocks;
    }

    public ArrayList<EmbThread> getThreadList(){ return _threadList; }

    public String getFilename() {
        return _filename;
    }

    public void setFilename(String value) {
        _filename = value;
    }

    public void addStitchAbs(double x, double y, int flags, boolean isAutoColorIndex) {
        if (this._currentStitchBlock == null) {
            if (_stitchBlocks.size() == 0) {
                this._currentStitchBlock = new StitchBlock();
                EmbThread thread;
                if (this._threadList.size() == 0) {
                    thread = new EmbThread();
                    thread.setColor(EmbColor.Random());
                } else {
                    thread = this._threadList.get(0);
                }
                this._currentStitchBlock.setThread(thread);
                this._threadList.add(this._currentStitchBlock.getThread());
                _stitchBlocks.add(this._currentStitchBlock);
            } else {
                this._currentStitchBlock = this._stitchBlocks.get(0);
            }
        }
        if ((flags & StitchType.END) != 0) {
            if (this._currentStitchBlock.isEmpty()) {
                return;
            }
            //pattern.FixColorCount();
        }

        if ((flags & StitchType.STOP) > 0) {
            if ((this._currentStitchBlock.isEmpty())) {
                return;
            }
            int threadIndex = 0;
            int currIndex = this._threadList.indexOf(this._currentStitchBlock.getThread());
            if (isAutoColorIndex) {
                if ((currIndex + 1) >= this._threadList.size()) {
                    EmbThread newThread = new EmbThread();
                    newThread.setColor(EmbColor.Random());
                    this._threadList.add(newThread);
                }
                threadIndex = currIndex + 1;
            }
            StitchBlock sb = new StitchBlock();
            this._currentStitchBlock = sb;
            sb.setThread(this._threadList.get(threadIndex));
            this.getStitchBlocks().add(sb);
            return;
        }
        _previousX = x;
        _previousY = y;
        this._currentStitchBlock.add((float)x,(float)y);
    }

    // AddStitchRel adds a stitch to the pattern at the relative position (dx, dy)
    // to the previous stitch. Positive y is up. Units are in millimeters.
    public void addStitchRel(double dx, double dy, int flags, boolean isAutoColorIndex) {

        double x = _previousX + dx;
        double y = _previousY + dy;

        this.addStitchAbs(x, y, flags, isAutoColorIndex);
    }

    // ChangeColor manually changes the color index to use.
    public void changeColor(byte index) {
        //this._currentColorIndex = index;
    }


    public void addThread(EmbThread thread) {
        _threadList.add(thread);
    }

    public RectF calculateBoundingBox() {
        double top = Double.POSITIVE_INFINITY;
        double left = Double.POSITIVE_INFINITY;
        double bottom = Double.NEGATIVE_INFINITY;//Double.MIN_VALUE ~= 0;
        double right = Double.NEGATIVE_INFINITY;
        for (StitchBlock sb : this.getStitchBlocks()) {
                top = Math.min(top, sb.getMinY());
                left = Math.min(left, sb.getMinX());
                bottom = Math.max(bottom, sb.getMaxY());
                right = Math.max(right, sb.getMaxX());
        }
        return new RectF((float)top, (float)left, (float)bottom, (float)right);
    }

    // Flip will flip the entire pattern about the x-axis if horz is true,
    // and/or about the y-axis if vert is true.
    public Pattern getFlippedPattern(boolean horizontal, boolean vertical) {
        double xMultiplier = horizontal ? -1.0 : 1.0;
        double yMultiplier = vertical ? -1.0 : 1.0;
        Matrix m = new Matrix();
        m.postScale((float)xMultiplier,(float)yMultiplier);
        for (StitchBlock sb : this.getStitchBlocks()) {
            sb.transform(m);
        }
        return this;
    }

    public Pattern getPositiveCoordinatePattern() {
        RectF boundingRect = this.calculateBoundingBox();
        Matrix m = new Matrix();
        m.setTranslate(-boundingRect.left,-boundingRect.top);
        for (StitchBlock sb : this.getStitchBlocks()) {
            sb.transform(m);
        }
        return this;
    }

    public Pattern getCenteredPattern() {
        RectF boundingRect = this.calculateBoundingBox();
        float cx = boundingRect.centerX();
        float cy = boundingRect.centerY();
        Matrix m = new Matrix();
        m.setTranslate(cx,cy);
        for (StitchBlock sb : this.getStitchBlocks()) {
            sb.transform(m);
        }
        return this;
    }

    public static IFormatReader getReaderByFilename(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith(".col")) {
            return new FormatCol();
        } else if (filename.endsWith(".exp")) {
            return new FormatExp();
        } else if (filename.endsWith(".dst")) {
            return new FormatDst();
        } else if (filename.endsWith(".pcs")) {
            return new FormatPcs();
        } else if (filename.endsWith(".pec")) {
            return new FormatPec();
        } else if (filename.endsWith(".pes")) {
            return new FormatPes();
        }
        return null;
    }

    public double pixelstocm(double v) {
        return (Math.rint(10*v)/10) / PIXELS_PER_MM;
    }
    public double convert(double v) {
        return pixelstocm(v);
    }

    public String getStatistics() {
        for (StitchBlock s : _stitchBlocks) {
            s.snap();
        }
        RectF bounds = calculateBoundingBox();
        StringBuilder sb = new StringBuilder();
        sb.append("Design name: ").append(this._filename).append('\n');
        int totalsize = getTotalSize();
        int jumpcount = getJumpCount();
        int colorcount = getColorCount();
        sb.append("Number of stitch entries: ").append(totalsize+jumpcount+colorcount).append('\n');
        sb.append(" Real stitches: ").append(totalsize).append('\n');
        sb.append(" Jumps: ").append(jumpcount).append('\n'); //I don't actually have jump stitches, just the number of jumps.
        sb.append(" Colors: ").append(colorcount).append('\n');
        sb.append("Design width x height = ").append(convert(bounds.width())).append(" x ").append(convert(bounds.height())).append('\n');
        sb.append("Center of design = ").append(convert(bounds.centerX())).append(" x ").append(convert(bounds.centerY())).append('\n');
        double totallength = getTotalLength();
        sb.append("Total length of stitches: ").append(convert(totallength)).append('\n');
        double min = getMinStitch();
        double max = getMaxStitch();
        sb.append("Maximum stitch length: ").append(convert(max)).append(" [").append(getCountRange(max,max)).append(" at this length]").append('\n');
        sb.append("Minimum stitch length: ").append(convert(min)).append(" [").append(getCountRange(min,min)).append(" at this length]").append('\n');
        sb.append("Average length of stitches: ").append(convert(totallength / (double)totalsize)).append('\n');
        sb.append("Stitch length distribution:").append('\n');
        double start = min;
        double step = (max-min)/10d;
        for (int i = 0; i < 10; i++) {
            double tmin = (i * step) + start;
            double tmax = ((i+1) * step) + start;
            sb.append(" ").append(convert(tmin)).append("- ").append(convert(tmax)).append(" == ").append(getCountRange(tmin,tmax)).append('\n');
        }
        return sb.toString();
    }

    private int getTotalSize() {
        int count = 0;
        for (StitchBlock sb : _stitchBlocks) {
            count += sb.size();
        }
        return count;
    }
    private double getTotalLength() {
        double count = 0;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size()-1; i < s; i++) {
                count += sb.distanceSegment(i);
            }
        }
        return count;
    }

    private int getJumpCount() {
        return _stitchBlocks.size();
    }

    private int getColorCount() {
        return this._threadList.size();
    }

    private double getMaxStitch() {
        double count = Double.NEGATIVE_INFINITY;
        double current;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size()-1; i < s; i++) {
                current = sb.distanceSegment(i);
                if (current > count) {
                    count = current;
                }
            }
        }
        return count;
    }

    private double getMinStitch() {
        double count = Double.POSITIVE_INFINITY;
        double current;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size()-1; i < s; i++) {
                current = sb.distanceSegment(i);
                if (current < count) {
                    count = current;
                }
            }
        }
        return count;
    }

    private int getCountRange(double min, double max) {
        int count = 0;
        double current;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size()-1; i < s; i++) {
                current = sb.distanceSegment(i);
                if ((current >= min) && (current <= max)) {
                    count++;
                }
            }
        }
        return count;
    }
}
