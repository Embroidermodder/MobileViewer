package com.embroidermodder.embroideryviewer;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FormatExp implements IFormat.Reader, IFormat.Writer {

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        byte b0, b1;
        try {
            for (int i = 0; stream.available() > 0; i++) {
                int flags = IFormat.NORMAL;
                b0 = (byte)stream.read();
                if (stream.available() <= 0) {
                    break;
                }
                b1 = (byte)stream.read();
                if (stream.available() <= 0) {
                    break;
                }
                if ((b0 & 0xFF) == 0x80) {
                    if ((b1 & 1) > 0) {
                        b0 = (byte)stream.read();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b1 = (byte)stream.read();
                        if (stream.available() <= 0) {
                            break;
                        }
                        flags = IFormat.STOP;
                    } else if ((b1 == 2) || (b1 == 4) || b1 == 6) {
                        flags = IFormat.TRIM;
                        if (b1 == 2) {
                            flags = IFormat.NORMAL;
                        }
                        b0 = (byte)stream.read();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b1 = (byte)stream.read();
                        if (stream.available() <= 0) {
                            break;
                        }
                    } else if ((b1 & 0xFF) == 0x80) {
                        b0 = (byte)stream.read();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b1 = (byte)stream.read();
                        if (stream.available() <= 0) {
                            break;
                        }
                        b0 = 0;
                        b1 = 0;
                        flags = IFormat.TRIM;
                    }
                }
                pattern.addStitchRel((float) b0, (float) b1, flags, true);
            }
        } catch (IOException ex) {
        }
        pattern.getFlippedPattern(false, true);
    }

    private void encode(byte[] b, byte dx, byte dy, int flags) {
        if (flags == IFormat.TRIM) {
            b[0] = (byte) 128;
            b[1] = 2;
            b[2] = dx;
            b[3] = dy;
        } else if (flags == IFormat.STOP) {
            b[0] = (byte) 128;
            b[1] = 1;
            b[2] = dx;
            b[3] = dy;
        } else {
            b[0] = dx;
            b[1] = dy;
        }
    }

    public void write(EmbPattern pattern, OutputStream stream) {
        double dx, dy;
        double xx = 0.0, yy = 0.0;
        int flags;
        byte b[] = new byte[4];
        try {
            for (StitchBlock stitchBlock : pattern.getStitchBlocks()) {
                for (int i = 0; i < stitchBlock.size(); i++) {
                    float x = stitchBlock.getX(i);
                    float y = stitchBlock.getY(i);
                    dx = x - xx;
                    dy = y - yy;
                    xx = x;
                    yy = y;
                    flags = IFormat.NORMAL;
                    encode(b, (byte) Math.round(dx), (byte) Math.round(dy), flags);
                    stream.write(b[0]);
                    stream.write(b[1]);
                    if ((b[0] == -128) && ((b[1] == 1) || (b[1] == 2) || (b[1] == 4))) {
                        stream.write(b[2]);
                        stream.write(b[3]);
                    }
                }
            }
            stream.write(0x1A);
            stream.close();
        } catch (IOException ex) {
        }
    }
}
