package com.embroidermodder.embroideryviewer;

import android.graphics.Matrix;

import java.util.ArrayList;

public class Pattern {
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

    // Flip will flip the entire pattern about the x-axis if horz is true,
    // and/or about the y-axis if vert is true.
    public Pattern getFlippedPattern(boolean horizontal, boolean vertical) {
        double xMultiplier = horizontal ? -1.0 : 1.0;
        double yMultiplier = vertical ? -1.0 : 1.0;
        Pattern newPattern = new Pattern();
        for (EmbThread thread : this._threadList) {
            newPattern._threadList.add(new EmbThread(thread));
        }

        Matrix m = new Matrix();
        m.postScale((float)xMultiplier,(float)yMultiplier);

        for (StitchBlock sb : this.getStitchBlocks()) {
            StitchBlock newStitchBlock = new StitchBlock(sb);
            newStitchBlock.transform(m);

            newPattern.getStitchBlocks().add(newStitchBlock);
            int threadIndex = this._threadList.indexOf(sb.getThread());
            newStitchBlock.setThread(newPattern._threadList.get(threadIndex));
        }
        return newPattern;
    }

    public void addThread(EmbThread thread) {
        _threadList.add(thread);
    }

    public EmbRect calculateBoundingBox() {
        double top = Double.MAX_VALUE;
        double left = Double.MAX_VALUE;
        double bottom = Double.MIN_VALUE;
        double right = Double.MIN_VALUE;
        for (StitchBlock sb : this.getStitchBlocks()) {
                top = Math.min(top, sb.getMinY());
                left = Math.min(left, sb.getMinX());
                bottom = Math.max(bottom, sb.getMaxY());
                right = Math.max(right, sb.getMaxX());
        }
        return new EmbRect(top, left, bottom, right);
    }

    public Pattern getPositiveCoordinatePattern() {
        int moveLeft, moveTop;
        EmbRect boundingRect = this.calculateBoundingBox();
        moveLeft = (int) boundingRect.left;
        moveTop = (int) boundingRect.top;
        Pattern newPattern = new Pattern();
        for (EmbThread thread : this._threadList) {
            newPattern._threadList.add(new EmbThread(thread));
        }
        Matrix m = new Matrix();
        m.setTranslate(moveLeft,moveTop);
        for (StitchBlock sb : this.getStitchBlocks()) {
            StitchBlock newStitchBlock = new StitchBlock(sb);
            newStitchBlock.transform(m);

            newPattern.getStitchBlocks().add(newStitchBlock);
            int threadIndex = this._threadList.indexOf(sb.getThread());
            newStitchBlock.setThread(newPattern._threadList.get(threadIndex));
        }
        return newPattern;
    }

    public Pattern getCenteredPattern() {
        int moveLeft, moveTop;
        EmbRect boundingRect = this.calculateBoundingBox();
        moveLeft = (int) (boundingRect.left - (boundingRect.getWidth() / 2.0));
        moveTop = (int) (boundingRect.top - (boundingRect.getHeight() / 2.0));
        Pattern newPattern = new Pattern();
        for (EmbThread thread : this._threadList) {
            newPattern._threadList.add(new EmbThread(thread));
        }
        Matrix m = new Matrix();
        m.setTranslate(moveLeft,moveTop);
        for (StitchBlock sb : this.getStitchBlocks()) {
            StitchBlock newStitchBlock = new StitchBlock(sb);
            newStitchBlock.transform(m);

            newPattern.getStitchBlocks().add(newStitchBlock);
            int threadIndex = this._threadList.indexOf(sb.getThread());
            newStitchBlock.setThread(newPattern._threadList.get(threadIndex));
        }
        return newPattern;
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
}
