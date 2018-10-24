package com.embroidermodder.embroideryviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbObject;
import com.embroidermodder.embroideryviewer.EmbroideryFormats.EmmThread;
import com.embroidermodder.embroideryviewer.geom.Points;
import com.embroidermodder.embroideryviewer.geom.PointsDirect;


import java.util.ArrayList;

/**
 * Created by Tat on 12/24/2017.
 */

public class EmmPatternQuickView {
    EmmPattern pattern;
    ArrayList<BlockDraw> drawers = new ArrayList<>();
    float minX = Float.POSITIVE_INFINITY;
    float minY = Float.POSITIVE_INFINITY;
    float maxX = Float.NEGATIVE_INFINITY;
    float maxY = Float.NEGATIVE_INFINITY;

    public EmmPatternQuickView(EmmPattern pattern) {
        this.pattern = pattern;
        BlockDraw draw = null;
        EmmThread lastThread = null;

        for (EmbObject sb : pattern.asStitchEmbObjects()) {
            EmmThread currentThread = sb.getThread();
            if (lastThread != currentThread) {
                draw = new BlockDraw();
                draw.paint.setColor(currentThread.getOpaqueColor());
                drawers.add(draw);
                lastThread = currentThread;
            }
            if (draw == null) continue;
            draw.addAsSegments(sb.getPoints());
        }
    }


    public void draw(Canvas canvas) {
        for (BlockDraw blockDraw : drawers) {
            canvas.drawLines(blockDraw.list.pointlist, 0, blockDraw.list.count, blockDraw.paint);
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

    private class BlockDraw {
        public PointsDirect list = new PointsDirect();
        public Paint paint = new Paint();

        public void addAsSegments(Points points) {
            int count = list.count;
            if (points.size() < 2) return;
            int addingcount = (((points.size() * 2) - 4) * 2) + 4;
            list.ensureCapacity(count + addingcount);
            float x = points.getX(0);
            float y = points.getY(0);
            list.add(x, y);
            int k = 1;
            for (int s = points.size() - 1; k < s; k++) {
                x = points.getX(k);
                y = points.getY(k);
                if (points.getData(k) != EmmPattern.STITCH) {
                    Log.e("", "Fail " + points.getPoint(k));
                }
                list.add(x, y);
                list.add(x, y);
            }
            x = points.getX(k);
            y = points.getY(k);
            list.add(x, y);
            if (list.getMaxX() > maxX) maxX = list.getMaxX();
            if (list.getMinX() < minX) minX = list.getMinX();
            if (list.getMaxY() > maxY) maxY = list.getMaxY();
            if (list.getMinY() < minY) minY = list.getMinY();
        }
    }
}
