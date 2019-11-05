package com.embroidermodder.embroideryviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;

import com.embroidermodder.embroideryviewer.geom.Points;
import com.embroidermodder.embroideryviewer.geom.PointsDirect;

import java.util.ArrayList;

/**
 * Created by Tat on 12/24/2017.
 */

public class EmmPatternFancyView {
    public Paint paint = new Paint();
    EmmPattern pattern;
    ArrayList<FancyLineDraw> drawers = new ArrayList<>();
    float minX = Float.POSITIVE_INFINITY;
    float minY = Float.POSITIVE_INFINITY;
    float maxX = Float.NEGATIVE_INFINITY;
    float maxY = Float.NEGATIVE_INFINITY;

    public EmmPatternFancyView(EmmPattern pattern) {
        this.pattern = pattern;
        FancyLineDraw draw;
        this.paint.setStrokeWidth(3);

        for (EmbObject sb : pattern.asStitchEmbObjects()) {
            EmmThread currentThread = sb.getThread();
            int color = currentThread.getOpaqueColor();
            Points points = sb.getPoints();
            for (int i = 0, ie = points.size()-1; i < ie; i += 1) {
                float x0 = points.getX(i);
                float y0 = points.getY(i);
                float x1 = points.getX(i+1);
                float y1 = points.getY(i+1);
                draw = new FancyLineDraw(x0,y0,x1,y1,color);
                drawers.add(draw);
            }
        }
    }


    public void draw(Canvas canvas) {
        for (FancyLineDraw f : drawers) {
            f.setPaint(this.paint);
            canvas.drawLine(f.x0, f.y0, f.x1, f.y1, this.paint);
        }
    }

    public Bitmap squareThumbnail(int thumbDim) {
        float width = maxX - minX;
        float height = maxY - minY;

        if (width == 0) width = 10;
        if (height == 0) height = 10;
        float scaleX = thumbDim / (width);
        float scaleY = thumbDim / (height);
        float scale = Math.min(scaleY, scaleX);
        float cx = minX + (width / 2);
        float cy = minY + (height / 2);

        Bitmap thumb = Bitmap.createBitmap(thumbDim, thumbDim, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(thumb);
        canvas.translate(thumb.getWidth() / 2, thumb.getHeight() / 2);
        canvas.scale(scale, scale);
        canvas.translate(-cx, -cy);
        draw(canvas);
        return thumb;
    }

    public boolean isEmpty() {
        return drawers.isEmpty();
    }


    private class FancyLineDraw {
        LinearGradient shader;
        float x0, y0, x1, y1;

        public FancyLineDraw(float x0, float y0, float x1, float y1, int color) {

            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            color = 0xFF000000 | color;
            int dark = adjust(color, -60);
            float[] positions = new float[] { 0, 0.05f, 0.5f, 0.9f, 1.0f };
            int[] colors = new int[] { dark, color, dark, color, dark };
            shader = new LinearGradient(x0, y0, x1, y1, colors, positions, Shader.TileMode.MIRROR);
        }

        public void setPaint(Paint paint) {
            paint.setShader(shader);
        }

        private int adjust(int color, int amount) {
            int a = (color >> 24) & 0xFF;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = (color) & 0xFF;
            r = clamp(r + amount);
            g = clamp(g + amount);
            b = clamp(b + amount);
            return a << 24 | r << 16 | g << 8 | b;
        }

        private int clamp(int v) {
            if (v > 255) return 255;
            if (v < 0) return 0;
            return v;
        }
    }
}
