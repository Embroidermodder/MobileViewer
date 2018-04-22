package com.embroidermodder.embroideryviewer.EmbroideryFormats;


import android.util.Log;

import com.embroidermodder.embroideryviewer.geom.DataPoints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.COLOR_CHANGE;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.END;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.JUMP;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STITCH;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.STOP;
import static com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbPattern.TRIM;


public class EmbWriterDST extends EmbWriter {
    public static final String MIME = "application/x-dst";
    public static final String EXT = "dst";

    private static final int PPMM = 10;
    private static final int MAXPJ = 121; //121 tenth millimeters is max move for a single move command, positive

    private final static int DSTHEADERSIZE = 512;
    private final static int COMMANDSIZE = 3;

    @Override
    public double maxJumpDistance() {
        return MAXPJ;
    }

    @Override
    public double maxStitchDistance() {
        return MAXPJ;
    }

    @Override
    public boolean canEncode(int encode) {
        switch (encode) {
            case STOP:
            case COLOR_CHANGE:
            case STITCH:
            case JUMP:
            case TRIM:
            case END:
                return true;
        }
        return false;
    }

    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    public boolean hasStitches() {
        return true;
    }

    @Override
    public void write() throws IOException {
        float minX = pattern.getStitches().getMinX();
        float minY = pattern.getStitches().getMinY();
        float maxX = pattern.getStitches().getMaxX();
        float maxY = pattern.getStitches().getMaxY();

        float width = maxX - minX;
        float height = maxY - minY;


        ByteArrayOutputStream tempArray = new ByteArrayOutputStream();
        push(tempArray);

        DataPoints stitches = pattern.getStitches();

        byte[] command = new byte[COMMANDSIZE];
        double dx, dy, xx = 0, yy = 0;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            float x = stitches.getX(i);
            float y = stitches.getY(i);
            dx = x - xx;
            dy = y - yy;

            switch (stitches.getData(i)) {
                case STITCH:
                    encodeRecord(command, (int) (dx), (int) (dy), STITCH);
                    stream.write(command);
                    break;
                case JUMP:
                    encodeRecord(command,  (int) dx, (int) dy, JUMP);
                    stream.write(command);
                    break;
                case COLOR_CHANGE:
                    encodeRecord(command, 0, 0, COLOR_CHANGE);
                    stream.write(command);
                    break;
                case TRIM:
                    encodeRecord(command, 2, 2, JUMP);
                    stream.write(command);
                    encodeRecord(command, -4, -4, JUMP);
                    stream.write(command);
                    encodeRecord(command, 2, 2, JUMP);
                    stream.write(command);
                    break;
                case STOP:
                    encodeRecord(command, 0, 0, STOP);
                    stream.write(command);
                    break;
                case END:
                    encodeRecord(command, 0, 0, STOP);
                    stream.write(command);
                    break;
            }
            xx = x;
            yy = y;
        }
        pop();

        String name = getName();
        if (name == null) name = "Untitled";
        if (name.length() > 8) {
            name = name.substring(0, 8);
        }

        int colorchanges = getColorChanges();
        int pointsize = tempArray.size() / COMMANDSIZE;
        stream.write(String.format("LA:%-16s\r", name).getBytes());
        stream.write(String.format(Locale.ENGLISH, "ST:%7d\r", pointsize).getBytes());
        stream.write(String.format(Locale.ENGLISH, "CO:%3d\r", colorchanges).getBytes()); /* number of color changes, not number of colors! */
        stream.write(String.format(Locale.ENGLISH, "+X:%5d\r", (int) Math.ceil((PPMM * width) / 2)).getBytes());
        stream.write(String.format(Locale.ENGLISH, "-X:%5d\r", (int) Math.ceil((PPMM * height) / 2)).getBytes());
        stream.write(String.format(Locale.ENGLISH, "+Y:%5d\r", (int) Math.ceil((PPMM * width) / 2)).getBytes());
        stream.write(String.format(Locale.ENGLISH, "-Y:%5d\r", (int) Math.ceil((PPMM * height) / 2)).getBytes());
        stream.write(String.format(Locale.ENGLISH, "AX:+%5d\r", 0).getBytes());
        stream.write(String.format(Locale.ENGLISH, "AY:+%5d\r", 0).getBytes());
        stream.write(String.format(Locale.ENGLISH, "MX:+%5d\r", 0).getBytes());
        stream.write(String.format(Locale.ENGLISH, "MY:+%5d\r", 0).getBytes());
        stream.write(String.format(Locale.ENGLISH, "PD:%6s\r", "******").getBytes());
        stream.write(0x1A);
        for (int i = 125; i < DSTHEADERSIZE; i++) {
            stream.write(' ');
        }
        stream.write(tempArray.toByteArray());
    }


    private void encodeRecord(byte[] command, int x, int y, int flags) {
        y = -y;
        byte b0 = 0;
        byte b1 = 0;
        byte b2 = 0;
        switch (flags) {
            case JUMP:
                b2 += bit(7); //jumpstitch 10xxxx11
                //bit7 is the difference between move and the stitch encode.
            case STITCH:
                b2 += bit(0);
                b2 += bit(1);
                if (x > 40) {
                    b2 += bit(2);
                    x -= 81;
                }
                if (x < -40) {
                    b2 += bit(3);
                    x += 81;
                }
                if (x > 13) {
                    b1 += bit(2);
                    x -= 27;
                }
                if (x < -13) {
                    b1 += bit(3);
                    x += 27;
                }
                if (x > 4) {
                    b0 += bit(2);
                    x -= 9;
                }
                if (x < -4) {
                    b0 += bit(3);
                    x += 9;
                }
                if (x > 1) {
                    b1 += bit(0);
                    x -= 3;
                }
                if (x < -1) {
                    b1 += bit(1);
                    x += 3;
                }
                if (x > 0) {
                    b0 += bit(0);
                    x -= 1;
                }
                if (x < 0) {
                    b0 += bit(1);
                    x += 1;
                }
                if (x != 0) {
                    Log.e("Error", "Write exceeded possible distance.");
                }
                if (y > 40) {
                    b2 += bit(5);
                    y -= 81;
                }
                if (y < -40) {
                    b2 += bit(4);
                    y += 81;
                }
                if (y > 13) {
                    b1 += bit(5);
                    y -= 27;
                }
                if (y < -13) {
                    b1 += bit(4);
                    y += 27;
                }
                if (y > 4) {
                    b0 += bit(5);
                    y -= 9;
                }
                if (y < -4) {
                    b0 += bit(4);
                    y += 9;
                }
                if (y > 1) {
                    b1 += bit(7);
                    y -= 3;
                }
                if (y < -1) {
                    b1 += bit(6);
                    y += 3;
                }
                if (y > 0) {
                    b0 += bit(7);
                    y -= 1;
                }
                if (y < 0) {
                    b0 += bit(6);
                    y += 1;
                }
                if (y != 0) {
                    Log.e("Error", "Write exceeded possible distance.");
                }
                break;
            case COLOR_CHANGE:
                b2 = (byte) 0b11000011;
                break;
            case STOP:
                b2 = (byte) 0b11110011;
                break;
        }
        command[0] = b0;
        command[1] = b1;
        command[2] = b2;
    }

    private int bit(int b) {
        return 1 << b;
    }

}
