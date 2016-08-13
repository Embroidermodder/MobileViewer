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

public class DrawView extends View implements EmbPattern.Provider, EmbPattern.Listener {
    private static final float MARGIN = 0.05f;

    Tool tool = new ToolPan();
    final Paint _paint = new Paint();
    Matrix viewMatrix;
    Matrix invertMatrix;
    private int _height;
    private int _width;
    private EmbPattern pattern = null;
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
        _paint.setStrokeWidth(1);
        _paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public void initWindowSize() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        _width = size.x;
        _height = size.y;
    }

    public void scale(float deltascale, float x, float y) {
        viewMatrix.postScale(deltascale, deltascale, x, y);
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
        //anything happening with event here is the X Y of the raw screen event.
        event.offsetLocation(event.getRawX() - event.getX(), event.getRawY() - event.getY()); //converts the event.getX() to event.getRaw() so the title bar doesn't fubar.
        //anything happening with event here is the X Y of the raw screen event, relative to the view.
        if (tool.rawTouch(this, event)) return true;
        if (invertMatrix != null) event.transform(invertMatrix);
        //anything happening with event now deals with the scene space.
        return tool.touch(this, event);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (pattern != null) {
            canvas.save();
            if (viewMatrix != null) canvas.setMatrix(viewMatrix);
            for (StitchBlock stitchBlock : pattern.getStitchBlocks()) {
                stitchBlock.draw(canvas, _paint);
            }
            canvas.restore();
        }
    }

    public String getStatistics() {
        return pattern.getStatistics(getContext());
    }

    public void update(int v) {
        invalidate();
    }

    @Override
    public EmbPattern getPattern() {
        return pattern;
    }

    public void setPattern(EmbPattern pattern) {
        if (this.pattern != null) this.pattern.removeListener(this);
        this.pattern = pattern;
        if (pattern.getStitchBlocks().isEmpty()) {
            viewPort = new RectF(0, 0, _width, _height);
        } else {
            viewPort = pattern.calculateBoundingBox();
            float scale = Math.min(_height / viewPort.height(), _width / viewPort.width());
            float extrawidth = _width - (viewPort.width() * scale);
            float extraheight = _height - (viewPort.height() * scale);
            viewPort.offset(-extrawidth / 2, -extraheight / 2);
            viewPort.inset(-viewPort.width() * MARGIN, -viewPort.height() * MARGIN);
        }
        calculateViewMatrixFromPort();
        this.pattern.addListener(this);
    }

}
