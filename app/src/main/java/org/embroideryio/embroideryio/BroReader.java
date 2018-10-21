package org.embroideryio.embroideryio;

import java.io.IOException;

public class BroReader extends EmbReader {

    public void read_bro_stitches() throws IOException {
        byte[] b = new byte[2];
        while (true) {
            if (readFully(b) == b.length) break;

            if ((b[0] & 0xFF) != 0x80) {
                float x = (float) b[0];
                float y = -(float) b[1];
                pattern.stitch(x, y);
                continue;
            }
            int control = readInt8();
            switch (control) {
                case 0x00:
                    continue;
                case 0x02:
                    break;
                case 0xE0:
                    break;
                case 0x7E: {
                    float x = signed16(readInt16LE());
                    float y = signed16(readInt16LE());
                    pattern.move(x, -y);
                    continue;
                }
                case 0x03: {
                    float x = signed16(readInt16LE());
                    float y = signed16(readInt16LE());
                    pattern.move(x, -y);
                    continue;
                }
                case 0xE1:
                case 0xE2:
                case 0xE3:
                case 0xE4:
                case 0xE5:
                case 0xE6:
                case 0xE7:
                case 0xE8:
                case 0xE9:
                case 0xEA:
                case 0xEB:
                case 0xEC:
                case 0xED:
                case 0xEE:
                case 0xEF:
                    int needle = control - 0xE0;
                    pattern.needle_change(needle);
                    float x = signed16(readInt16LE());
                    float y = signed16(readInt16LE());
                    pattern.move(x, -y);

            }
            break;
        }
        pattern.end();
    }


    @Override
    public void read() throws IOException {
        skip(0x100);
        read_bro_stitches();
    }
}
