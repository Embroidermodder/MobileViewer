package com.embroidermodder.embroideryviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;




import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;

public class EmbPattern {
    static final int FLIP_HORIZONTAL = 0;
    static final int FLIP_VERTICAL = 1;
    static final int FLIP_BOTH = 2;

    static final int ROTATE_RIGHT = 0;
    static final int ROTATE_LEFT = 1;

    static final int RIGHT_TO_LEFT = 0;
    static final int LEFT_TO_RIGHT = 1;

    private static final float PIXELS_PER_MM = 10;
    private final ArrayList<StitchBlock> _stitchBlocks;
    private final ArrayList<EmbThread> _threadList;
    private final ArrayList<EmbPattern.Listener> _listeners;
    private String _filename;
    private StitchBlock _currentStitchBlock;
    private ProgressDialog progress;
    private float _previousX = 0;
    private float _previousY = 0;

    public EmbPattern() {
        _listeners = new ArrayList<>();
        _stitchBlocks = new ArrayList<>();
        _threadList = new ArrayList<>();
        _currentStitchBlock = null;
    }
    public ArrayList<StitchBlock> getStitchBlocks()
    {
        return _stitchBlocks;
    }
    public ArrayList<EmbPoint> stitches = new ArrayList<EmbPoint>();

    public  ArrayList<EmbPoint> getstitches() {
        return stitches;
    }

    public void correctForMaxStitchLength(double maxStitchLength, double maxJumpLength) {
        double maxLen;
        float dx, dy, xx, yy, maxXY, addX, addY;
        int splits, flagsToUse;
        for (int i = 1; i < stitches.size(); i++) {
            xx = stitches.get(i-1).X;
            yy = stitches.get(i-1).Y;
            dx = stitches.get(i).X - xx;
            dy = stitches.get(i).Y - yy;

            if ((Math.abs(dx) > maxStitchLength) || (Math.abs(dy) > maxStitchLength)) {
                maxXY = Math.abs(dx);
                if (Math.abs(dy) > maxXY) {
                    maxXY = Math.abs(dy);
                }
                if ((stitches.get(i).Flags & ( IFormat.JUMP | IFormat.TRIM)) > 0) {
                    maxLen = maxJumpLength;
                } else {
                    maxLen = maxStitchLength;
                }
                splits = (int)Math.ceil((double)maxXY / maxLen);
                if (splits > 1) {
                    flagsToUse = stitches.get(i).Flags;
                    addX = dx / splits;
                    addY = dy / splits;
                    for (int j = 1; j < splits; j++) {
                        stitches.add(i, new EmbPoint(xx + addX * j, yy + addY * j, flagsToUse));
                        i++;
                    }
                    i--;
                }
            }
        }
    }

    public void rotate_90(int direction) {
        for (int s = 0; s < stitches.size(); s++) {
            float tmp = stitches.get(s).Y;
            stitches.get(s).Y = -stitches.get(s).X;
            if (direction == ROTATE_LEFT) {
                stitches.get(s).X = tmp;
            } else if (direction == ROTATE_RIGHT) {
                stitches.get(s).X = -tmp;
            }
        }
    }

    public void rel_flip(int flip_type) {
        for (int s = 0; s < stitches.size(); s++) {
            if (flip_type == FLIP_HORIZONTAL || flip_type == FLIP_BOTH) {
                stitches.get(s).X *= -1;
            }
            if (flip_type == FLIP_VERTICAL || flip_type == FLIP_BOTH) {
                stitches.get(s).Y *= -1;
            }
        }
    }

    public ArrayList<EmbThread> getThreadList() {
        return _threadList;
    }

    public String getFilename() {
        return _filename;
    }

    public void setFilename(String value) {
        _filename = value;
    }

    public void addStitchAbs(float x, float y, int flags, boolean isAutoColorIndex) {
        EmbPoint s = new EmbPoint(x, y, flags);
        stitches.add(s);
        if (this._currentStitchBlock == null) {
            if (_stitchBlocks.size() == 0) {
                this._currentStitchBlock = new StitchBlock();
                EmbThread thread;
                if (this._threadList.size() == 0) {
                    thread = new EmbThread();
                    thread.setColor(EmbColor.Random());
                    this._threadList.add(thread);
                } else {
                    thread = this._threadList.get(0);
                }
                this._currentStitchBlock.setThread(thread);
                _stitchBlocks.add(this._currentStitchBlock);
            } else {
                this._currentStitchBlock = this._stitchBlocks.get(0);
            }
        }
        if ((flags & IFormat.END) != 0) {
            if (this._currentStitchBlock.isEmpty()) {
                this._stitchBlocks.remove(this._currentStitchBlock);
                return;
            }
            //pattern.FixColorCount();
        }

        if ((flags & IFormat.STOP) > 0) {
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
        if ((flags & IFormat.TRIM) > 0) {
            _previousX = x;
            _previousY = y;
            if ((this._currentStitchBlock.isEmpty())) {
                return;
            }
            int currIndex = this._threadList.indexOf(this._currentStitchBlock.getThread());
            StitchBlock sb = new StitchBlock();
            this._currentStitchBlock = sb;
            sb.setThread(this._threadList.get(currIndex));
            this.getStitchBlocks().add(sb);
            return;
        }

        _previousX = x;
        _previousY = y;
        if ((flags & IFormat.JUMP) == 0) {
            this._currentStitchBlock.add(x, y);
        }
    }

    /**
     * AddStitchRel adds a stitch to the pattern at the relative position (dx, dy)
     * to the previous stitch. Units are in millimeters.
     *
     * @param dx               The change in X position.
     * @param dy               The change in Y position. Positive value move upward.
     * @param flags            JUMP, TRIM, NORMAL or STOP
     * @param isAutoColorIndex Should color index be auto-incremented on STOP flag
     */
    public void addStitchRel(float dx, float dy, int flags, boolean isAutoColorIndex) {
        float x = _previousX + dx;
        float y = _previousY + dy;
        this.addStitchAbs(x, y, flags, isAutoColorIndex);
    }

    public void addThread(EmbThread thread) {
        _threadList.add(thread);
    }

    public RectF calculateBoundingBox() {
        float left = Float.POSITIVE_INFINITY;
        float top = Float.POSITIVE_INFINITY;
        float right = Float.NEGATIVE_INFINITY;
        float bottom = Float.NEGATIVE_INFINITY;
        for (StitchBlock sb : this.getStitchBlocks()) {
            left = Math.min(left, sb.getMinX());
            top = Math.min(top, sb.getMinY());
            right = Math.max(right, sb.getMaxX());
            bottom = Math.max(bottom, sb.getMaxY());
        }
        return new RectF(left, top, right, bottom);
    }

    /**
     * Flip will flip the entire pattern about the specified axis
     *
     * @param horizontal should pattern be flipped about the x-axis
     * @param vertical   should pattern be flipped about the xy-axis
     * @return the flipped pattern
     */
    public EmbPattern getFlippedPattern(boolean horizontal, boolean vertical) {
        float xMultiplier = horizontal ? -1.0f : 1.0f;
        float yMultiplier = vertical ? -1.0f : 1.0f;
        Matrix m = new Matrix();
        m.postScale(xMultiplier, yMultiplier);
        for (StitchBlock sb : this.getStitchBlocks()) {
            sb.transform(m);
        }
        return this;
    }

    public EmbPattern getPositiveCoordinatePattern() {
        RectF boundingRect = this.calculateBoundingBox();
        Matrix m = new Matrix();
        m.setTranslate(-boundingRect.left, -boundingRect.top);
        for (StitchBlock sb : this.getStitchBlocks()) {
            sb.transform(m);
        }
        return this;
    }

    public EmbPattern getCenteredPattern() {
        RectF boundingRect = this.calculateBoundingBox();
        float cx = boundingRect.centerX();
        float cy = boundingRect.centerY();
        Matrix m = new Matrix();
        m.setTranslate(cx, cy);
        for (StitchBlock sb : this.getStitchBlocks()) {
            sb.transform(m);
        }
        return this;
    }

    private float pixelstomm(float v) {
        return v / PIXELS_PER_MM;
    }

    public String convert(float v) {
        return String.format("%.1f", pixelstomm(v));
    }

    public String getStatistics(Context context) {
        for (StitchBlock s : _stitchBlocks) {
            s.snap();
        }
        RectF bounds = calculateBoundingBox();
        StringBuilder sb = new StringBuilder();
        //sb.append("Design name: ").append(this._filename).append('\n');
        int totalSize = getTotalSize();
        int jumpCount = getJumpCount();
        int colorCount = getColorCount();
        //  sb.append(context.getString(R.string.number_of_stitches)).append(totalSize + jumpCount + colorCount).append('\n');
        sb.append(context.getString(R.string.normal_stitches)).append(totalSize).append('\n');
        sb.append(context.getString(R.string.jumps)).append(jumpCount).append('\n');
        sb.append(context.getString(R.string.colors)).append(colorCount).append('\n');
        sb.append(context.getString(R.string.size)).append(convert(bounds.width())).append(" mm X ").append(convert(bounds.height())).append(" mm\n");
        float totalLength = getTotalLength();
        //  sb.append(context.getString(R.string.total_length)).append(convert(totalLength)).append(" mm\n");
        return sb.toString();
    }

    private int getTotalSize() {
        int count = 0;
        for (StitchBlock sb : _stitchBlocks) {
            count += sb.size();
        }
        return count;
    }

    private float getTotalLength() {
        float count = 0;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size() - 1; i < s; i++) {
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

    private float getMaxStitch() {
        float count = Float.NEGATIVE_INFINITY;
        float current;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size() - 1; i < s; i++) {
                current = sb.distanceSegment(i);
                if (current > count) {
                    count = current;
                }
            }
        }
        return count;
    }

    private float getMinStitch() {
        float count = Float.POSITIVE_INFINITY;
        float current;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size() - 1; i < s; i++) {
                current = sb.distanceSegment(i);
                if (current < count) {
                    count = current;
                }
            }
        }
        return count;
    }

    private int getCountRange(float min, float max) {
        int count = 0;
        float current;
        for (StitchBlock sb : _stitchBlocks) {
            for (int i = 0, s = sb.size() - 1; i < s; i++) {
                current = sb.distanceSegment(i);
                if ((current >= min) && (current <= max)) {
                    count++;
                }
            }
        }
        return count;
    }

    public void notifyChange(int v) {
        for (Listener listener : _listeners) {
            listener.update(v);
        }
    }

    public void addListener(Listener listener) {
        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

    public void removeListener(Object listener) {
        if (listener instanceof Listener) {
            _listeners.remove(listener);
        }
    }

    public Bitmap getThumbnail(float _width, float _height) {

        RectF viewPort = calculateBoundingBox();
        float scale = Math.min(_height / viewPort.height(), _width / viewPort.width());
        Matrix matrix = new Matrix();
        if (scale != 0) {
            matrix.postTranslate(-viewPort.left, -viewPort.top);
            matrix.postScale(scale, scale);
        }

        Bitmap bmp = Bitmap.createBitmap((int) _width, (int) _height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (matrix != null) canvas.concat(matrix);
        for (StitchBlock stitchBlock : getStitchBlocks()) {
            stitchBlock.draw(canvas, paint);
        }
        return bmp;
    }

    public interface Provider {
        EmbPattern getPattern();
    }

    public interface Listener {
        void update(int v);
    }
}