package org.embroideryio.embroideryio;

import java.io.IOException;
import java.util.ArrayList;

public class PecReader extends EmbReader {

    static final int MASK_07_BIT = 0b01111111;
    static final int JUMP_CODE = 0x10;
    static final int TRIM_CODE = 0x20;
    static final int FLAG_LONG = 0x80;

    @Override
    public void read() throws IOException {
        skip(0x8);
        readPec();
    }

    public void readPec() throws IOException {
        skip(3);
        String label = readString(16).trim();
        if (label.length() != 0) {
            pattern.setMetadata(EmbPattern.PROP_NAME,label);
        }
        skip(0x0F);
        int pec_graphic_byte_stride = readInt8();
        int pec_graphic_icon_height = readInt8();
        skip(0x0C);
        int colorChanges = readInt8();
        if (pattern.threadlist.isEmpty()) {
            //if the threadlist is empty, we are reading a file without header threads.
            EmbThreadPec[] threadSet = EmbThreadPec.getThreadSet();
            for (int i = 0; i <= colorChanges; i++) {
                int index = (readInt8() % 65);
                pattern.threadlist.add(threadSet[index]);
            }
        } else if (pattern.threadlist.size() != colorChanges + 1) {
            //if the threadlist is not empty but also not equal to the number of colorchanges;
            //convert unique list threadList to 1 to 1 list thread.
            EmbThreadPec[] threadSet = EmbThreadPec.getThreadSet();
            EmbThread[] threadMap = new EmbThread[threadSet.length];
            ArrayList<EmbThread> queue = new ArrayList<>();
            for (int i = 0; i <= colorChanges; i++) {
                int index = (readInt8() % 65);
                EmbThread value = threadMap[index];
                if (value == null) {
                    if (!pattern.threadlist.isEmpty()) {
                        value = pattern.threadlist.remove(0);
                    } else {
                        value = threadSet[index];
                    }
                    threadMap[index] = value;
                }
                queue.add(value);
            }
            pattern.threadlist.clear();
            pattern.threadlist.addAll(queue);
        } else {
            //threadList is equal to the colors, use the default 1 header thread, to 1 color produced by some flawed PES writers.
            skip(colorChanges + 1);//since we're 1 to 1, the listed colors are irrelevant.
        }

        skip(0x200 - (0x30 + 1 + colorChanges));
        skip(0x13); //2 bytes size, 17 bytes cruft.
        readPecStitches();
    }

    public void readPecStitches() throws IOException {
        int val1, val2;
        int x, y;
        while (true) {
            val1 = readInt8();
            if (val1 == -1) {
                break;
            }
            val2 = readInt8();
            if (val2 == -1) {
                break;
            }

            int code = (val1 << 8) | val2;
            if (val1 == 0xFF) {// && val2 == 0x00) {
                break; //End command.
            }
            if (val1 == 0xFE && val2 == 0xB0) {
                skip(1);
                pattern.color_change();
                continue;
            }
            boolean jump = false;
            boolean trim = false;
            /* High bit set means 12-bit offset, otherwise 7-bit signed delta */
            if ((val1 & FLAG_LONG) != 0) {
                if ((val1 & TRIM_CODE) != 0) {
                    trim = true;
                }
                if ((val1 & JUMP_CODE) != 0) {
                    jump = true;
                }

                x = (code << 20) >> 20;

                val2 = readInt8();
                if (val2 == -1) {
                    break;
                }
            } else {
                x = (val1 << 25) >> 25;
            }
            if ((val2 & FLAG_LONG) != 0) {
                if ((val2 & TRIM_CODE) != 0) {
                    trim = true;
                }
                if ((val2 & JUMP_CODE) != 0) {
                    jump = true;
                }

                int val3 = readInt8();
                if (val3 == -1) {
                    break;
                }
                code = (val2 << 8) | val3;

                y = (code << 20) >> 20;
            } else {
                y = (val2 << 25) >> 25;
            }
            if (jump) {
                pattern.move(x, y);
            } else if (trim) {
                pattern.trim();
                pattern.move(x, y);
            } else {
                pattern.stitch(x, y);
            }
        }
        pattern.end();
    }
}
