package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FormatXxx implements IFormat.Reader {

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        try {
            stream.skip(0x27);

            int num_of_colors = BinaryHelper.readInt16LE(stream);
            stream.skip(0xD3);
            int palette_offset = BinaryHelper.readInt32LE(stream);
            for (int i = 0; i <= num_of_colors; i++) {
                pattern.addThread(new EmbThread(0, 0, 0, "", ""));
            }
            int dx, dy;
            byte b1, b2;
            int stitch_type;
            boolean is_jump_stitch = false;

            for (int s = 0x100; s < palette_offset; s++) {
                b1 = (byte)stream.read();
                s++;
                b2 = (byte)stream.read();
                stitch_type = IFormat.NORMAL;
                if (is_jump_stitch) {
                    stitch_type = IFormat.TRIM;
                }
                is_jump_stitch = false;
                if (b1 == 0x7E || b1 == 0x7D) {
                    s++;
                    dx = (short) ((b2 & 0xFF) + (stream.read() << 8));
                    s++;
                    dy = (short) BinaryHelper.readInt16LE(stream);
                    s++;
                    stitch_type = IFormat.TRIM;
                } else if (b1 == 0x7F) {
                    if (b2 != 0x17 && b2 != 0x46 && b2 >= 8) {
                        b1 = 0;
                        b2 = 0;
                        is_jump_stitch = true;
                        stitch_type = IFormat.STOP;
                    } else if (b2 == 1) {
                        s++;
                        b1 = (byte)stream.read();
                        s++;
                        b2 = (byte)stream.read();
                        stitch_type = IFormat.TRIM;
                    } else {
                        continue;
                    }
                    dx = xxx_decode_byte(b1);
                    dy = xxx_decode_byte(b2);
                } else {
                    dx = xxx_decode_byte(b1);
                    dy = xxx_decode_byte(b2);
                }
                pattern.addStitchRel(dx, dy, stitch_type, true);
            }
            stream.skip(6);
            ArrayList<EmbThread> threadList = pattern.getThreadList();
            for (int i = 0; i <= num_of_colors; i++) {
                stream.read();
                int r = stream.read();
                int g = stream.read();
                int b = stream.read();
                threadList.get(i).setColor(new EmbColor(r, g, b));
            }

        } catch (IOException ex) {

        }
        pattern.getFlippedPattern(false, true);
        pattern.addStitchRel(0, 0, IFormat.END, true);
    }

    int xxx_decode_byte(int b) {
        if (b >= 0x80) {
            return (-~b) - 1;
        }
        return b;
    }
}