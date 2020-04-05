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


import static com.embroidermodder.embroideryviewer.EmmPattern.COLOR_CHANGE;
import static com.embroidermodder.embroideryviewer.EmmPattern.JUMP;
import static com.embroidermodder.embroideryviewer.EmmPattern.STITCH;

public class DrawView extends View implements EmmPattern.Listener, EmmPattern.Provider {
    private static final int DRAWMODE_QUICK = 0;
    private static final int DRAWMODE_FANCY = 1;
    private static final float MARGIN = 0.05f;
    private static final float PIXELS_PER_MM = 10;
    private final EmmPattern emmPattern = new EmmPattern();
    private EmmPatternFancyView fancyView = new EmmPatternFancyView(emmPattern);
    private EmmPatternQuickView quickView = new EmmPatternQuickView(emmPattern);
    private final Paint _paint = new Paint();
    private int _height;
    private int _width;
    int drawMode = DRAWMODE_FANCY;

    Tool tool = new ToolPan();
    Matrix viewMatrix;
    Matrix invertMatrix;


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
        emmPattern.addListener(this);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            drawMode = DRAWMODE_QUICK;
        } else {
            drawMode = DRAWMODE_FANCY;
            postInvalidate();
        }
        //anything happening with event here is the X Y of the raw screen event, relative to the view.
        if (tool.rawTouch(this, event)) return true;
        if (invertMatrix != null) event.transform(invertMatrix);
        //anything happening with event now deals with the scene space.
        return tool.touch(this, event);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        if (viewMatrix != null) canvas.concat(viewMatrix);
        if ((drawMode == DRAWMODE_QUICK) || (fancyView == null)) {
            if (quickView != null){
                quickView.draw(canvas);
            }
        } else {
            fancyView.draw(canvas);
        }
        canvas.restore();
    }

    public String getStatistics() {
        Context context = getContext();
        RectF bounds = new RectF();
        emmPattern.getBounds(bounds);
        StringBuilder sb = new StringBuilder();
        int totalSize = emmPattern.countStitchCommands(STITCH);
        int jumpCount = emmPattern.countStitchCommands(JUMP);
        int colorCount = emmPattern.countStitchCommands(COLOR_CHANGE);
        sb.append(context.getString(R.string.normal_stitches)).append(totalSize).append('\n');
        sb.append(context.getString(R.string.jumps)).append(jumpCount).append('\n');
        sb.append(context.getString(R.string.colors)).append(colorCount).append('\n');
        sb.append(context.getString(R.string.size)).append(convert(bounds.width())).append(" mm X ").append(convert(bounds.height())).append(" mm\n");
        return sb.toString();
    }

    public String convert(float v) {
        return String.format("%.1f", pixelstomm(v));
    }

    private float pixelstomm(float v) {
        return v / PIXELS_PER_MM;
    }

    @Override
    public EmmPattern getPattern() {
        return emmPattern;
    }

    public void setPattern(EmmPattern pattern) {
        if (pattern == null) return;
        this.emmPattern.setPattern(pattern);
        this.quickView = new EmmPatternQuickView(emmPattern);
        this.fancyView = new EmmPatternFancyView(emmPattern);
        pattern.notifyChange(EmmPattern.NOTIFY_CHANGE);
        invalidate();
    }

    @Override
    public void notifyChange(int id) {
        this.fancyView = new EmmPatternFancyView(emmPattern);
        this.quickView = new EmmPatternQuickView(emmPattern);
        if (!fancyView.isEmpty()) {
            viewPort = emmPattern.getBounds(viewPort);
            float scale = Math.min(_height / viewPort.height(), _width / viewPort.width());
            float extraWidth = _width - (viewPort.width() * scale);
            float extraHeight = _height - (viewPort.height() * scale);
            viewPort.offset(-extraWidth / 2, -extraHeight / 2);
            viewPort.inset(-viewPort.width() * MARGIN, -viewPort.height() * MARGIN);
        }
        calculateViewMatrixFromPort();
        invalidate();
    }

}
