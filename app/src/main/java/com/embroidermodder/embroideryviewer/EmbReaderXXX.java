package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.util.ArrayList;


public class EmbReaderXXX extends EmbReader {
    @Override
    protected void read() throws IOException {
        skip(0x27);

        int num_of_colors = readInt16LE();
        skip(0xD3);
        int palette_offset = readInt32LE();
        for (int i = 0; i <= num_of_colors; i++) {
            pattern.addThread(new EmbThread(0, 0, 0, "", ""));
        }
        int dx, dy;
        byte b1, b2;
        int stitch_type;
        boolean is_jump_stitch = false;

        for (int s = 0x100; s < palette_offset; s++) {
            b1 = (byte) readInt8();
            s++;
            b2 = (byte) readInt8();
            stitch_type = EmbPattern.STITCH;
            if (is_jump_stitch) {
                stitch_type = EmbPattern.TRIM;
            }
            is_jump_stitch = false;
            if (b1 == 0x7E || b1 == 0x7D) {
                s++;
                dx = (short) ((b2 & 0xFF) + (readInt8() << 8));
                s++;
                dy = (short) readInt16LE();
                s++;
                stitch_type = EmbPattern.TRIM;
            } else if (b1 == 0x7F) {
                if (b2 != 0x17 && b2 != 0x46 && b2 >= 8) {
                    b1 = 0;
                    b2 = 0;
                    is_jump_stitch = true;
                    stitch_type = EmbPattern.STOP;
                } else if (b2 == 1) {
                    s++;
                    b1 = (byte) readInt8();
                    s++;
                    b2 = (byte) readInt8();
                    stitch_type = EmbPattern.TRIM;
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
        skip(6);
        ArrayList<EmbThread> threadList = pattern.getThreadlist();
        for (int i = 0; i <= num_of_colors; i++) {
            readInt8();
            int r = readInt8();
            int g = readInt8();
            int b = readInt8();
            threadList.get(i).setColor(r, g, b);
        }
        end();
    }

    int xxx_decode_byte(int b) {
        if (b >= 0x80) {
            return (-~b) - 1;
        }
        return b;
    }
}
