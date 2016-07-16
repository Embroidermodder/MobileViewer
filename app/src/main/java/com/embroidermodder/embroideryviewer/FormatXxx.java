package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FormatXxx implements IFormatReader {

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        try {
            stream.skipBytes(0x27);

            int num_of_colors = BinaryReader.readInt16LE(stream);
            stream.skipBytes(0xD3);
            int palette_offset = BinaryReader.readInt32LE(stream);
            for (int i = 0; i <= num_of_colors; i++) {
                p.addThread(new EmbThread(0, 0, 0, "", ""));
            }
            int dx, dy;
            byte b1, b2;
            int stitch_type;
            boolean is_jump_stitch = false;

            for (int s = 0x100; s < palette_offset; s++) {
                b1 = stream.readByte();
                s++;
                b2 = stream.readByte();
                stitch_type = StitchType.NORMAL;
                if (is_jump_stitch) {
                    stitch_type = StitchType.TRIM;
                }
                is_jump_stitch = false;
                if (b1 == 0x7E || b1 == 0x7D) {
                    s++;
                    dx = (short) ((b2 & 0xFF) + (stream.readByte() << 8));
                    s++;
                    dy = (short)BinaryReader.readInt16LE(stream);
                    s++;
                    stitch_type = StitchType.TRIM;
                } else if (b1 == 0x7F) {
                    if (b2 != 0x17 && b2 != 0x46 && b2 >= 8) {
                        b1 = 0;
                        b2 = 0;
                        is_jump_stitch = true;
                        stitch_type = StitchType.STOP;
                    } else if (b2 == 1) {
                        s++;
                        b1 = stream.readByte();
                        s++;
                        b2 = stream.readByte();
                        stitch_type = StitchType.TRIM;
                    } else {
                        continue;
                    }
                    dx = xxx_decode_byte(b1);
                    dy = xxx_decode_byte(b2);
                } else {
                    dx = xxx_decode_byte(b1);
                    dy = xxx_decode_byte(b2);
                }
                p.addStitchRel(dx, dy, stitch_type, true);
            }
            stream.skipBytes(6);
            ArrayList<EmbThread> threadList = p.getThreadList();
            for (int i = 0; i <= num_of_colors; i++) {
                stream.readByte();
                int r = stream.readByte();
                int g = stream.readByte();
                int b = stream.readByte();
                threadList.get(i).setColor(new EmbColor(r, g, b));
            }

        } catch (IOException ex) {

        }
        return p.getFlippedPattern(false, true);
    }

    int xxx_decode_byte(int b) {
        if (b >= 0x80) {
            return (-~b) - 1;
        }
        return b;
    }
}