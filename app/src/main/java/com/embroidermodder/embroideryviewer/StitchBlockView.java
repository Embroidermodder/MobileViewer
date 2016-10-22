package com.embroidermodder.embroideryviewer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import yuku.ambilwarna.AmbilWarnaDialog;


public class StitchBlockView
    extends RelativeLayout
{
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
    return (StitchBlockView) LayoutInflater.from(parent.getContext())
        .inflate(R.layout.stitchblock_item, parent, false);
  }

  private void init(Context context) {
    LayoutInflater.from(context).inflate(R.layout.stitchblock_item_child, this, true);
    color = (ImageButton) findViewById(R.id.stitchblock_color);
    name = (TextView) findViewById(R.id.stitchblock_name);
    stitches = (TextView) findViewById(R.id.stitchblock_stitches);
  }

  public void setStitchBlock(final int i, final EmbPattern pattern, final StitchBlock stitchBlock) {
    final EmbThread thread = stitchBlock.getThread();
    final int colorValue = thread.getColor().getAndroidColor();
    color.setBackgroundColor(colorValue);
    color.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view) {
        int r = Color.red(colorValue);
        int g = Color.green(colorValue);
        int b = Color.blue(colorValue);
        EmbColor embColor = new EmbColor(r, g, b);
        pickColor(stitchBlock, pattern, embColor);
      }
    });
    String nameValue = stitchBlock.getThread().getDescription();
    if ((nameValue == null) || ("".equals(nameValue))) {
      nameValue = asHexColor(colorValue);
    }
    name.setText(nameValue);
    stitches.setText(getContext().getString(R.string.stitchblock_stitches, stitchBlock.size()));
  }

  public void pickColor(final StitchBlock stitchBlock,
                        final EmbPattern pattern,
                        final EmbColor currentColor)
  {
    AmbilWarnaDialog dialog = new AmbilWarnaDialog(this.getContext(),
        currentColor.getAndroidColor(), new AmbilWarnaDialog.OnAmbilWarnaListener()
    {
      // Executes, when user click Cancel button
      @Override
      public void onCancel(AmbilWarnaDialog dialog) {
      }

      // Executes, when user click OK button
      @Override
      public void onOk(AmbilWarnaDialog dialog, int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        EmbColor embColor = new EmbColor(r, g, b);

        stitchBlock.getThread().setColor(embColor);
        pattern.notifyChange(1);
      }
    });
    dialog.show();
  }

  private String asHexColor(int color) {
    String rgb = Integer.toHexString(color);
    rgb = rgb.substring(2, rgb.length());
    return "#" + rgb;
  }
}
