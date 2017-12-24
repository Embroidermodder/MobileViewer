
package com.embroidermodder.embroideryviewer;

import java.io.IOException;

public class EmbReaderDST extends EmbReader {
    public static final String MIME = "application/x-dst";
    public static final String EXT = "dst";

    private static final int PPMM = 10;
    private static final int MAXPJ = 121; //121 tenth millimeters is max move for a single move command, positive

    private final static int DSTHEADERSIZE = 512;
    private final static int COMMANDSIZE = 3;

    @Override
    public void postRead(EmbPattern input) {
        TransCoder t = TransCoder.getTranscoder();
        t.jumps_before_trim = 3;
        t.tie_off = true;
        t.tie_on = true;
        t.fix_color_count = true;
        t.transcode(input);
    }

    @Override
    public void read() throws IOException {
        byte[] b = new byte[DSTHEADERSIZE];
        readFully(b);

        String bytestring = new String(b);
        String[] split = bytestring.split("[\n\r]");
        for (String s : split) {
            if (s == null) continue;
            if (s.length() <= 3) continue;
            switch (s.substring(0, 2)) {
                case "LA":
                    String name = s.substring(3);
                    setName(name.trim());
                    break;
            }
        }

        byte[] command = new byte[COMMANDSIZE];
        while (true) {
            if (readFully(command) != 3) break;
            int dx = decodedx(command[0], command[1], command[2]);
            int dy = decodedy(command[0], command[1], command[2]);
            if ((command[2] & 0b11110011) == 0b11110011) {
                stop();
            } else if ((command[2] & 0b11000011) == 0b11000011) {
                changeColor();
            } else if ((command[2] & 0b10000011) == 0b10000011) {
                move(dx, dy);
            } else {
                stitch(dx, dy);
            }
        }
    }

    private int getbit(byte b, int pos) {
        int bit;
        bit = (b >> pos) & 1;
        return (bit);
    }

    private int decodedx(byte b0, byte b1, byte b2) {
        int x = 0;
        x += getbit(b2, 2) * (+81);
        x += getbit(b2, 3) * (-81);
        x += getbit(b1, 2) * (+27);
        x += getbit(b1, 3) * (-27);
        x += getbit(b0, 2) * (+9);
        x += getbit(b0, 3) * (-9);
        x += getbit(b1, 0) * (+3);
        x += getbit(b1, 1) * (-3);
        x += getbit(b0, 0) * (+1);
        x += getbit(b0, 1) * (-1);
        return x;
    }

    private int decodedy(byte b0, byte b1, byte b2) {
        int y = 0;
        y += getbit(b2, 5) * (+81);
        y += getbit(b2, 4) * (-81);
        y += getbit(b1, 5) * (+27);
        y += getbit(b1, 4) * (-27);
        y += getbit(b0, 5) * (+9);
        y += getbit(b0, 4) * (-9);
        y += getbit(b1, 7) * (+3);
        y += getbit(b1, 6) * (-3);
        y += getbit(b0, 7) * (+1);
        y += getbit(b0, 6) * (-1);
        return -y;
    }
}
