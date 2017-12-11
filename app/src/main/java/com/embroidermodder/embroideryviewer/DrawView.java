package com.embroidermodder.embroideryviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

public class DrawView extends View {
    private static final float MARGIN = 0.05f;
    public ArrayList<Listener> listeners;

    Tool tool = new ToolPan();
    final Paint _paint = new Paint();
    Matrix viewMatrix;
    Matrix invertMatrix;
    private int _height;
    private int _width;
    private EmbPatternViewer root = null;
    private EmbPattern embPattern = null;
    private RectF viewPort;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        _paint.setStrokeWidth(2);
        _paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public void initWindowSize() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        _width = size.x;
        _height = size.y;
        viewPort = new RectF(0, 0, _width, _height);
        calculateViewMatrixFromPort();
    }

    public void scale(float deltaScale, float x, float y) {
        viewMatrix.postScale(deltaScale, deltaScale, x, y);
        calculateViewPortFromMatrix();
    }

    public void pan(float dx, float dy) {
        viewMatrix.postTranslate(dx, dy);
        calculateViewPortFromMatrix();
    }

    public void calculateViewMatrixFromPort() {
        float scale = Math.min(_height / viewPort.height(), _width / viewPort.width());
        viewMatrix = new Matrix();
        if (scale != 0) {
            viewMatrix.postTranslate(-viewPort.left, -viewPort.top);
            viewMatrix.postScale(scale, scale);
        }
        calculateInvertMatrix();
    }

    public void calculateViewPortFromMatrix() {
        float[] positions = new float[]{
                0, 0,
                _width, _height
        };
        calculateInvertMatrix();
        invertMatrix.mapPoints(positions);
        viewPort.set(positions[0], positions[1], positions[2], positions[3]);
    }

    public void calculateInvertMatrix() {
        invertMatrix = new Matrix(viewMatrix);
        invertMatrix.invert(invertMatrix);
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //anything happening with event here is the X Y of the raw screen event, relative to the view.
        if (tool.rawTouch(this, event)) return true;
        if (invertMatrix != null) event.transform(invertMatrix);
        //anything happening with event now deals with the scene space.
        return tool.touch(this, event);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (root != null) {
            canvas.save();
            if (viewMatrix != null) canvas.concat(viewMatrix);
            for (StitchBlock stitchBlock : root) {
                stitchBlock.draw(canvas, _paint);
            }
            canvas.restore();
        }
    }

    public String getStatistics() {
        return root.getStatistics(getContext());
    }

    public EmbPattern getEmbPattern() {
        return embPattern;
    }

    public void setPattern(EmbPattern pattern) {
        if (pattern == null) return;
        this.embPattern = pattern;
        this.root = new EmbPatternViewer(pattern);

        if (!root.isEmpty()) {
            viewPort = root.calculateBoundingBox();
            float scale = Math.min(_height / viewPort.height(), _width / viewPort.width());
            float extraWidth = _width - (viewPort.width() * scale);
            float extraHeight = _height - (viewPort.height() * scale);
            viewPort.offset(-extraWidth / 2, -extraHeight / 2);
            viewPort.inset(-viewPort.width() * MARGIN, -viewPort.height() * MARGIN);
        }
        calculateViewMatrixFromPort();
        notifyChange(0);
    }

    public void addListener(Listener listener) {
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        if (listeners == null) return;
        listeners.remove(listener);
        if (listeners.isEmpty()) listeners = null;
    }

    public void notifyChange(int id) {
        if (listeners == null) return;
        for (Listener listener : listeners) {
            listener.notifyChange(id);
        }
    }

    public interface Listener {
        void notifyChange(int id);
    }

}
