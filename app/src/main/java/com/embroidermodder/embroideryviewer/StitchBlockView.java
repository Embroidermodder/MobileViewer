package com.embroidermodder.embroideryviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class StitchBlockView extends RelativeLayout {
    public ImageButton color;
    public TextView name;
    public TextView stitches;

    public StitchBlockView(Context context) {
        super(context);
        init(context);
    }

    public StitchBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StitchBlockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public static StitchBlockView inflate(ViewGroup parent) {
        StitchBlockView itemView = (StitchBlockView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stitchblock_item, parent, false);
        return itemView;
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.stitchblock_item_child, this, true);
        color = (ImageButton) findViewById(R.id.stitchblock_color);
        name = (TextView) findViewById(R.id.stitchblock_name);
        stitches = (TextView) findViewById(R.id.stitchblock_stitches);
    }

    public void setStitchBlock(final int i, final EmbPattern pattern, final StitchBlock stitchBlock) {
        EmbThread thread = stitchBlock.getThread();
        int colorValue = thread.getColor().getAndroidColor();
        color.setBackgroundColor(colorValue);
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stitchBlock.getThread().setColor(EmbColor.Random());
                pattern.notifyChange(1);
            }
        });
        String namevalue = stitchBlock.getThread().getDescription();
        if ((namevalue == null) || ("".equals(namevalue))) {
            namevalue = asHexColor(colorValue);
        }
        name.setText(namevalue);
        stitches.setText(getContext().getString(R.string.stitchblock_stitches, stitchBlock.size()));
    }

    private String asHexColor(int color) {
        String rgb = Integer.toHexString(color);
        rgb = rgb.substring(2, rgb.length());
        return "#" + rgb;
    }
}
