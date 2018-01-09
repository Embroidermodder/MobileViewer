package com.embroidermodder.embroideryviewer;
import android.graphics.RectF;

import com.embroidermodder.embroideryviewer.geom.Point;
import com.embroidermodder.embroideryviewer.geom.PointIterator;
import com.embroidermodder.embroideryviewer.geom.Points;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;


public class FormatDst implements IFormat.Reader, IFormat.Writer  {
    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

    private int decodeFlags(byte b) {
        int returnCode = 0;
        if (b == -13) {
            return IFormat.END;
        }
        if ((b & 0x80) > 0) {
            returnCode |= IFormat.TRIM;
        }
        if ((b & 0x40) > 0) {
            returnCode |= IFormat.COLOR_CHANGE;
        }
        return returnCode;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        byte[] b = new byte[3];
        try {
            stream.skip(0x200);
            while (true) {

                if(stream.read(b) != 3) {
                    break;
                }
                if (Thread.currentThread().isInterrupted()) return;
                int x = 0;
                int y = 0;
                if ((b[0] & 0x01) > 0) {
                    x += 1;
                }
                if ((b[0] & 0x02) > 0) {
                    x -= 1;
                }
                if ((b[0] & 0x04) > 0) {
                    x += 9;
                }
                if ((b[0] & 0x08) > 0) {
                    x -= 9;
                }
                if ((b[0] & 0x80) > 0) {
                    y += 1;
                }
                if ((b[0] & 0x40) > 0) {
                    y -= 1;
                }
                if ((b[0] & 0x20) > 0) {
                    y += 9;
                }
                if ((b[0] & 0x10) > 0) {
                    y -= 9;
                }
                if ((b[1] & 0x01) > 0) {
                    x += 3;
                }
                if ((b[1] & 0x02) > 0) {
                    x -= 3;
                }
                if ((b[1] & 0x04) > 0) {
                    x += 27;
                }
                if ((b[1] & 0x08) > 0) {
                    x -= 27;
                }
                if ((b[1] & 0x80) > 0) {
                    y += 3;
                }
                if ((b[1] & 0x40) > 0) {
                    y -= 3;
                }
                if ((b[1] & 0x20) > 0) {
                    y += 27;
                }
                if ((b[1] & 0x10) > 0) {
                    y -= 27;
                }
                if ((b[2] & 0x04) > 0) {
                    x += 81;
                }
                if ((b[2] & 0x08) > 0) {
                    x -= 81;
                }
                if ((b[2] & 0x20) > 0) {
                    y += 81;
                }
                if ((b[2] & 0x10) > 0) {
                    y -= 81;
                }
                int flags = decodeFlags(b[2]);

                if (flags == IFormat.END) {
                    break;
                }
                pattern.addStitchRel(x, y, flags, true);
            }
        } catch (IOException ex) {
        }
        pattern.rel_flip(1);
        pattern.getFlippedPattern(false, true);
        pattern.addStitchRel(0, 0, IFormat.END, true);
        pattern.fixColorCount();
    }

    static int setBit(int pos) {
        return 1 << pos;
    }

    private static void encodeRecord(OutputStream file, float x, float y, int flags) throws IOException {
        try {
            char b0, b1, b2;
            b0 = b1 = b2 = 0;
            if (x >= +41) {
                b2 += setBit(2);
                x -= 81;
            }
            if (x <= -41) {
                b2 += setBit(3);
                x += 81;
            }
            if (x >= +14) {
                b1 += setBit(2);
                x -= 27;
            }
            if (x <= -14) {
                b1 += setBit(3);
                x += 27;
            }
            if (x >= +5) {
                b0 += setBit(2);
                x -= 9;
            }
            if (x <= -5) {
                b0 += setBit(3);
                x += 9;
            }
            if (x >= +2) {
                b1 += setBit(0);
                x -= 3;
            }
            if (x <= -2) {
                b1 += setBit(1);
                x += 3;
            }
            if (x >= +1) {
                b0 += setBit(0);
                x -= 1;
            }
            if (x <= -1) {
                b0 += setBit(1);
                x += 1;
            }
            if (y >= +41) {
                b2 += setBit(5);
                y -= 81;
            }
            if (y <= -41) {
                b2 += setBit(4);
                y += 81;
            }
            if (y >= +14) {
                b1 += setBit(5);
                y -= 27;
            }
            if (y <= -14) {
                b1 += setBit(4);
                y += 27;
            }
            if (y >= +5) {
                b0 += setBit(5);
                y -= 9;
            }
            if (y <= -5) {
                b0 += setBit(4);
                y += 9;
            }
            if (y >= +2) {
                b1 += setBit(7);
                y -= 3;
            }
            if (y <= -2) {
                b1 += setBit(6);
                y += 3;
            }
            if (y >= +1) {
                b0 += setBit(7);
                y -= 1;
            }
            if (y <= -1) {
                b0 += setBit(6);
                y += 1;
            }
            b2 |= (char) 3;

            if ((flags & IFormat.END) >0) {
                b2 = (char) -13;
                b0 = b1 = (char) 0;
            }

            if ((flags & (IFormat.TRIM)) > 0) {
                b2 = (char) (b2 | 0x83);
            }
            if ((flags & IFormat.COLOR_CHANGE) == IFormat.COLOR_CHANGE) {
                b2 = (char) (b2 | 0xC3);
            }
            file.write(b0);
            file.write(b1);
            file.write(b2);
        } catch (IOException ex) {
        }
    }

    public void write(EmbPattern pattern, OutputStream file) {
        pattern.correctForMaxStitchLength(121, 121);
        try {

            RectF boundingRect;
            float xx, yy, dx, dy;
            int flags;
            int i;
            int co, st;
            int ax, ay, mx, my;
            pattern.rel_flip(1);
            co = pattern.getThreadList().size();
            st = pattern.getStitches().size();
            boundingRect = pattern.calculateBoundingBox();
            file.write(String.format("LA:%-16s", "Untitled").getBytes());
            file.write(0x0D);
            file.write(String.format("ST:%7d", st).getBytes());
            file.write(0x0D);
            file.write(String.format("CO:%3d", co - 1).getBytes()); /* number of color changes, not number of colors! */
            file.write(0x0D);
            file.write(String.format("+X:%5d", (int) (boundingRect.right * 10.0)).getBytes());
            file.write(0x0D);
            file.write(String.format("-X:%5d", (int) (Math.abs(boundingRect.left) * 10.0)).getBytes());
            file.write(0x0D);
            file.write(String.format("+Y:%5d", (int) (boundingRect.bottom * 10.0)).getBytes());
            file.write(0x0D);
            file.write(String.format("-Y:%5d", (int) (Math.abs(boundingRect.top) * 10.0)).getBytes());
            file.write(0x0D);
            ax = ay = mx = my = 0;

            String pd = "";
            if (pd.isEmpty()) {
                pd = "******";
            }
            file.write(String.format("AX:+%5d", ax).getBytes());
            file.write(0x0D);
            file.write(String.format("AY:+%5d", ay).getBytes());
            file.write(0x0D);
            file.write(String.format("MX:+%5d", mx).getBytes());
            file.write(0x0D);
            file.write(String.format("MY:+%5d", my).getBytes());
            file.write(0x0D);
            file.write(String.format("PD:%6s", pd).getBytes());
            file.write(0x0D);
            file.write(0x1a); /* 0x1a is the code for end of section. */

            /* pad out header to proper length */
            for (i = 125; i < 512; i++) {
                file.write(0x20);
            }

            /* write stitches */
            xx = yy = 0;
            for (Point stitch : new PointIterator<Points>(pattern.getStitches())) {
                dx = (float)stitch.getX() - xx;
                dy = (float)stitch.getY() - yy;
                xx += dx;
                yy += dy;
                flags = stitch.data();
                encodeRecord(file, dx, dy,  flags);
            }
            file.write(0xA1); /* finish file with a terminator character */
            file.write(0);
            file.write(0);
        } catch (IOException e) {
        }
    }
}