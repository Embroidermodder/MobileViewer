package com.embroidermodder.embroideryviewer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.embroidermodder.embroideryviewer.geom.Point;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ColorStitchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    EmbPattern pattern;
    static final int COLOR = 0;
    static final int STITCH = 1;

    public ColorStitchAdapter() {
    }

    public void setPattern(EmbPattern root) {
        this.pattern = root;
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case STITCH:
                return new ColorStitchStitchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.color_stitch_stitch_item, parent, false));
            case COLOR:
                return new ColorStitchColorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.color_stitch_color_item, parent, false));
            default:
                return new EmptyViewHolder(new TextView(parent.getContext()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (pattern.getThreadCount() > position) return COLOR;
        return STITCH;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case STITCH:
                ColorStitchStitchViewHolder sHolder = (ColorStitchStitchViewHolder) holder;
                Point p = pattern.getStitches().getPoint(position - pattern.getThreadCount());
                sHolder.setPoint(p);
                break;
            case COLOR:
                ColorStitchColorViewHolder cHolder = (ColorStitchColorViewHolder) holder;
                EmbThread thread = pattern.getThreadList().get(position);
                cHolder.setThread(thread);
                break;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return pattern.getThreadCount() + pattern.getStitches().size();
    }

    public class ColorStitchColorViewHolder extends RecyclerView.ViewHolder {
        ImageButton color;
        TextView name;
        EmbThread thread;

        public ColorStitchColorViewHolder(View itemView) {
            super(itemView);
            color = (ImageButton) itemView.findViewById(R.id.stitchblock_color);
            name = (TextView) itemView.findViewById(R.id.stitchblock_name);
            itemView.invalidate();
            color.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AmbilWarnaDialog dialog = new AmbilWarnaDialog(ColorStitchColorViewHolder.this.itemView.getContext(),
                            thread.getColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        // Executes, when user click Cancel button
                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {
                        }

                        // Executes, when user click OK button
                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int newColor) {
                            thread.setColor(newColor);
                            notifyItemChanged(getAdapterPosition());
                        }
                    });
                    dialog.show();
                }
            });
        }

        public void setThread(EmbThread thread) {
            this.thread = thread;
            int colorValue = thread.color;
            color.setBackgroundColor(colorValue);

            String nameValue = thread.description;
            if ((nameValue == null) || ("".equals(nameValue))) {
                nameValue = thread.getHexColor();
            }
            name.setText(nameValue);
            //stitches.setText(itemView.getContext().getString(R.string.stitchblock_stitches, 0));
            color.setBackgroundColor(thread.getColor());
            name.setText(thread.description);
        }

    }

    public class ColorStitchStitchViewHolder extends RecyclerView.ViewHolder {
        TextView coords;
        TextView name;

        public ColorStitchStitchViewHolder(View itemView) {
            super(itemView);
            coords = (TextView) itemView.findViewById(R.id.stitchblock_coords);
            name = (TextView) itemView.findViewById(R.id.stitchblock_name);
        }

        public void setPoint(Point p) {
            name.setText(getStitchName(p.data()));
            coords.setText(p.getX() + " " + p.getY());
        }

        private String getStitchName(int data) {
            switch (data) {
                case IFormat.NORMAL:
                    return "Stitch";
                case IFormat.JUMP:
                    return "Jump";
                case IFormat.END:
                    return "End";
                case IFormat.STOP:
                    return "Stop";
                case IFormat.COLOR_CHANGE:
                    return "ColorChange";
                case IFormat.TRIM:
                    return "Trim";
                case IFormat.JUMP | IFormat.TRIM:
                    return "Trim/Jump";
                default:
                    return "Unknown: " + data;
            }

        }
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
            ((TextView) itemView).setText("EMPTY");
        }
    }

}
