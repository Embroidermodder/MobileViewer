package com.embroidermodder.embroideryviewer.embroideryview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;


import com.embroidermodder.embroideryviewer.R;

import java.io.File;

public class ThumbnailView extends View {
    private static final Rect rect = new Rect();
    @Nullable
    public static Drawable fileDefault, directoryDefault, parentDirectoryDefault;
    public boolean isParent = false;
    @Nullable
    public File file;
    @Nullable
    private Bitmap cache;

    final Paint _paint = new Paint();

    public ThumbnailView(Context context) {
        super(context);
        init();
    }

    public ThumbnailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThumbnailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        int scaledSize = getResources().getDimensionPixelSize(R.dimen.thumbnailText);
        int minSize = getResources().getDimensionPixelSize(R.dimen.thumbnailDim);
        _paint.setTextSize(scaledSize);
        if (fileDefault == null) {
            fileDefault = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
            if (fileDefault != null) {
                fileDefault.setBounds(0, 0, minSize, minSize);
            }
        }
        if (directoryDefault == null) {
            directoryDefault = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
            if (directoryDefault != null) {
                directoryDefault.setBounds(0, 0, minSize, minSize);
            }
        }
        if (parentDirectoryDefault == null) {
            parentDirectoryDefault = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
            if (parentDirectoryDefault != null) {
                parentDirectoryDefault.setBounds(0, 0, minSize, minSize);
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (cache != null) {
            synchronized (ThumbnailView.this) {
                if (cache != null) {
                    canvas.drawBitmap(cache, 0, 0, _paint);
                }
            }
        } else {
            canvas.save();
            canvas.getClipBounds(rect);
            canvas.translate(rect.left, rect.top);
            if (file == null || file.isDirectory()) {
                if (isParent) {
                    canvas.drawColor(0xFFFFAACC);
                    parentDirectoryDefault.draw(canvas);
                } else {
                    canvas.drawColor(0xFFAACCFF);
                    directoryDefault.draw(canvas);
                }
            } else {
                fileDefault.draw(canvas);
            }
            canvas.restore();
        }
        if (file != null) {
            canvas.drawText(file.getName(), 0, getHeight(), _paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(w, h);
        int minSize = getResources().getDimensionPixelSize(R.dimen.thumbnailDim);
        size = Math.max(size, minSize);
        setMeasuredDimension(size, size);
    }


    public void clear() {
        clearCache();
        file = null;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void clearCache() {
        if (cache != null) {
            synchronized (ThumbnailView.this) {
                if (cache != null) {
                    Bitmap cacheold = cache;
                    cache = null;
                    if (!cacheold.isRecycled()) cacheold.recycle();
                }
            }
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.cache = bitmap;
        postInvalidate();
    }

    public int getDim() {
        double dim = Math.min(getWidth(), getHeight());
        int minSize = getResources().getDimensionPixelSize(R.dimen.thumbnailDim);
        if (dim < minSize) dim = minSize;
        return (int) (dim + 1);
    }
}
