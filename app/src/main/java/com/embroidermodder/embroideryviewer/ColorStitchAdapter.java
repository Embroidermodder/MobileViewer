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
    static final int METADATA = 0;
    static final int COLOR = 1;
    static final int STITCH = 2;

    public ColorStitchAdapter() {
    }

    public void setPattern(EmbPattern root) {
        this.pattern = root;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case METADATA:
                return new MetaDataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.color_stitch_stitch_item, parent, false));
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
        if (6 > position) return METADATA;
        if (pattern.getThreadCount() > (position - 6)) return COLOR;
        return STITCH;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case METADATA:
                MetaDataViewHolder mHolder = (MetaDataViewHolder) holder;
                switch (position) {
                    case 0:
                        mHolder.setDataPair(EmbPattern.PROP_FILENAME,pattern.filename);
                        break;
                    case 1:
                        mHolder.setDataPair(EmbPattern.PROP_NAME,pattern.name);
                        break;
                    case 2:
                        mHolder.setDataPair(EmbPattern.PROP_AUTHOR,pattern.author);
                        break;
                    case 3:
                        mHolder.setDataPair(EmbPattern.PROP_CATEGORY,pattern.category);
                        break;
                    case 4:
                        mHolder.setDataPair(EmbPattern.PROP_KEYWORDS,pattern.keywords);
                        break;
                    case 5:
                        mHolder.setDataPair(EmbPattern.PROP_COMMENTS,pattern.comments);
                }
                break;
            case STITCH:
                ColorStitchStitchViewHolder sHolder = (ColorStitchStitchViewHolder) holder;
                Point p = pattern.getStitches().getPoint((position - 6) - pattern.getThreadCount());
                sHolder.setPoint(p);
                break;
            case COLOR:
                ColorStitchColorViewHolder cHolder = (ColorStitchColorViewHolder) holder;
                EmbThread thread = pattern.getThreadlist().get(position-6);
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
                            pattern.notifyChange(EmbPattern.NOTIFY_THREAD_COLOR);
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
                case EmbPattern.STITCH:
                    return "Stitch";
                case EmbPattern.JUMP:
                    return "Jump";
                case EmbPattern.END:
                    return "End";
                case EmbPattern.STOP:
                    return "Stop";
                case EmbPattern.COLOR_CHANGE:
                    return "ColorChange";
                    case EmbPattern.NO_COMMAND:
                        return "No Command";
                        case EmbPattern.INIT:
                            return "Init";
                case EmbPattern.TIE_OFF:
                    return "Tie-Off";
                case EmbPattern.TIE_ON:
                    return "Tie-On";
                case EmbPattern.TRIM:
                    return "Trim";
                default:
                    return "Unknown: " + data;
            }

        }
    }

    public class MetaDataViewHolder extends RecyclerView.ViewHolder {
        TextView value;
        TextView key;

        public MetaDataViewHolder(View itemView) {
            super(itemView);
            value = (TextView) itemView.findViewById(R.id.stitchblock_coords);
            key = (TextView) itemView.findViewById(R.id.stitchblock_name);
        }

        public void setDataPair(String key, String value) {
            this.key.setText(key);
            this.value.setText(value);
        }

    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
            ((TextView) itemView).setText("EMPTY");
        }
    }

}
