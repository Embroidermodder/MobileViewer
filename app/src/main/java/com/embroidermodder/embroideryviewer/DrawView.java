package com.embroidermodder.embroideryviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class DrawView extends View {
    private final int _height;
    private final int _width;

    Paint _paint = new Paint();
    Pattern pattern;

    Matrix viewMatrix;
    Matrix invertMatrix;

    public DrawView(Context context, Pattern pattern) {
        super(context);
        this.pattern = pattern.getPositiveCoordinatePattern();
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        _height = metrics.heightPixels;
        _width = metrics.widthPixels;
    }

    public void calculateViewMatrix() {
        EmbRect embRect = pattern.calculateBoundingBox();
        float scale = (float)Math.min(_height/embRect.getHeight(), _width/embRect.getWidth());
        viewMatrix = new Matrix();
        if (scale != 0) {
            viewMatrix.postTranslate((float) -embRect.left, (float) -embRect.top);
            viewMatrix.postScale(scale, scale);
            _paint.setStrokeWidth(scale/9.0f);
        }
        invertMatrix = new Matrix(viewMatrix);
        invertMatrix.invert(invertMatrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //anything happening with event here is the X Y of the raw screen event.
        if (invertMatrix != null) event.transform(invertMatrix);
        //anything happening with event now deals with the screen space.
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                this.pattern.addStitchAbs(event.getX(),event.getY(),StitchType.NORMAL,false);
                this.invalidate(); //in larger operations, you would invalidate *only* the sections that could have changed.
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                this.pattern.addStitchAbs(event.getX(),event.getY(),StitchType.STOP,false);
                break;
            case MotionEvent.ACTION_UP:
                this.pattern.addStitchAbs(event.getX(),event.getY(),StitchType.STOP,false);
                break;
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        //_paint.setColor(Color.WHITE);
        //_paint.setStyle(Paint.Style.FILL);
        //canvas.drawRect(new Rect(0, 0, _width, _height), _paint);
        //This is unneeded as Android will automatically blank, any invalidated region.
        //_paint.setStyle(Paint.Style.STROKE); Unneeded for the segments.

        if (viewMatrix == null) calculateViewMatrix();

        canvas.save();
        if (viewMatrix != null) canvas.setMatrix(viewMatrix);
        for(StitchBlock stitchBlock : pattern.getStitchBlocks()){
            stitchBlock.draw(canvas,_paint);
        }
        canvas.restore();
    }
}
