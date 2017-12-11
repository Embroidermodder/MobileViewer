package com.embroidermodder.embroideryviewer;

import android.graphics.RectF;

import com.embroidermodder.embroideryviewer.geom.Point;
import com.embroidermodder.embroideryviewer.geom.PointIterator;
import com.embroidermodder.embroideryviewer.geom.Points;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class FormatPec implements IFormat.Reader, IFormat.Writer {

    public static void readPecStitches(EmbPattern pattern, InputStream stream) {
        try {
            while (stream.available() > 0) {
                int val1 = (stream.read() & 0xFF);
                int val2 = (stream.read() & 0xFF);

                int stitchType = IFormat.NORMAL;
                if (val1 == 0xFF && val2 == 0x00) {
                    pattern.addStitchRel(0.0f, 0.0f, IFormat.END, true);
                    break;
                }
                if (val1 == 0xFE && val2 == 0xB0) {
                    stream.read();
                    pattern.addStitchRel(0.0f, 0.0f, IFormat.STOP, true);
                    continue;
                }
                /* High bit set means 12-bit offset, otherwise 7-bit signed delta */
                if ((val1 & 0x80) > 0) {
                    if ((val1 & 0x20) > 0) {
                        stitchType = IFormat.TRIM;
                    }
                    if ((val1 & 0x10) > 0) {
                        stitchType = IFormat.JUMP;
                    }
                    val1 = ((val1 & 0x0F) << 8) + val2;

                    /* Signed 12-bit arithmetic */
                    if ((val1 & 0x800) > 0) {
                        val1 -= 0x1000;
                    }
                    val2 = stream.read() & 0xFF;
                } else if (val1 >= 0x40) {
                    val1 -= 0x80;
                }
                if ((val2 & 0x80) > 0) {
                    if ((val2 & 0x20) > 0) {
                        stitchType = IFormat.TRIM;
                    }
                    if ((val2 & 0x10) > 0) {
                        stitchType = IFormat.JUMP;
                    }
                    val2 = ((val2 & 0x0F) << 8) + (stream.read() & 0xFF);

                    /* Signed 12-bit arithmetic */
                    if ((val2 & 0x800) > 0) {
                        val2 -= 0x1000;
                    }
                } else if (val2 >= 0x40) {
                    val2 -= 0x80;
                }
                pattern.addStitchRel(val1, val2, stitchType, true);

            }
        } catch (IOException ex) {

        }
        pattern.addStitchRel(0, 0, IFormat.END, true);
    }

    public static EmbThread getThreadByIndex(int index) {
        switch (index) {
            case 0:
                return new EmbThread(0, 0, 0, "Unknown", "");
            case 1:
                return new EmbThread(14, 31, 124, "Prussian Blue", "");
            case 2:
                return new EmbThread(10, 85, 163, "Blue", "");
            case 3:
                return new EmbThread(0, 135, 119, "Teal Green", "");
            case 4:
                return new EmbThread(75, 107, 175, "Cornflower Blue", "");
            case 5:
                return new EmbThread(237, 23, 31, "Red", "");
            case 6:
                return new EmbThread(209, 92, 0, "Reddish Brown", "");
            case 7:
                return new EmbThread(145, 54, 151, "Magenta", "");
            case 8:
                return new EmbThread(228, 154, 203, "Light Lilac", "");
            case 9:
                return new EmbThread(145, 95, 172, "Lilac", "");
            case 10:
                return new EmbThread(158, 214, 125, "Mint Green", "");
            case 11:
                return new EmbThread(232, 169, 0, "Deep Gold", "");
            case 12:
                return new EmbThread(254, 186, 53, "Orange", "");
            case 13:
                return new EmbThread(255, 255, 0, "Yellow", "");
            case 14:
                return new EmbThread(112, 188, 31, "Lime Green", "");
            case 15:
                return new EmbThread(186, 152, 0, "Brass", "");
            case 16:
                return new EmbThread(168, 168, 168, "Silver", "");
            case 17:
                return new EmbThread(125, 111, 0, "Russet Brown", "");
            case 18:
                return new EmbThread(255, 255, 179, "Cream Brown", "");
            case 19:
                return new EmbThread(79, 85, 86, "Pewter", "");
            case 20:
                return new EmbThread(0, 0, 0, "Black", "");
            case 21:
                return new EmbThread(11, 61, 145, "Ultramarine", "");
            case 22:
                return new EmbThread(119, 1, 118, "Royal Purple", "");
            case 23:
                return new EmbThread(41, 49, 51, "Dark Gray", "");
            case 24:
                return new EmbThread(42, 19, 1, "Dark Brown", "");
            case 25:
                return new EmbThread(246, 74, 138, "Deep Rose", "");
            case 26:
                return new EmbThread(178, 118, 36, "Light Brown", "");
            case 27:
                return new EmbThread(252, 187, 197, "Salmon Pink", "");
            case 28:
                return new EmbThread(254, 55, 15, "Vermillion", "");
            case 29:
                return new EmbThread(240, 240, 240, "White", "");
            case 30:
                return new EmbThread(106, 28, 138, "Violet", "");
            case 31:
                return new EmbThread(168, 221, 196, "Seacrest", "");
            case 32:
                return new EmbThread(37, 132, 187, "Sky Blue", "");
            case 33:
                return new EmbThread(254, 179, 67, "Pumpkin", "");
            case 34:
                return new EmbThread(255, 243, 107, "Cream Yellow", "");
            case 35:
                return new EmbThread(208, 166, 96, "Khaki", "");
            case 36:
                return new EmbThread(209, 84, 0, "Clay Brown", "");
            case 37:
                return new EmbThread(102, 186, 73, "Leaf Green", "");
            case 38:
                return new EmbThread(19, 74, 70, "Peacock Blue", "");
            case 39:
                return new EmbThread(135, 135, 135, "Gray", "");
            case 40:
                return new EmbThread(216, 204, 198, "Warm Gray", "");
            case 41:
                return new EmbThread(67, 86, 7, "Dark Olive", "");
            case 42:
                return new EmbThread(253, 217, 222, "Flesh Pink", "");
            case 43:
                return new EmbThread(249, 147, 188, "Pink", "");
            case 44:
                return new EmbThread(0, 56, 34, "Deep Green", "");
            case 45:
                return new EmbThread(178, 175, 212, "Lavender", "");
            case 46:
                return new EmbThread(104, 106, 176, "Wisteria Violet", "");
            case 47:
                return new EmbThread(239, 227, 185, "Beige", "");
            case 48:
                return new EmbThread(247, 56, 102, "Carmine", "");
            case 49:
                return new EmbThread(181, 75, 100, "Amber Red", "");
            case 50:
                return new EmbThread(19, 43, 26, "Olive Green", "");
            case 51:
                return new EmbThread(199, 1, 86, "Dark Fuschia", "");
            case 52:
                return new EmbThread(254, 158, 50, "Tangerine", "");
            case 53:
                return new EmbThread(168, 222, 235, "Light Blue", "");
            case 54:
                return new EmbThread(0, 103, 62, "Emerald Green", "");
            case 55:
                return new EmbThread(78, 41, 144, "Purple", "");
            case 56:
                return new EmbThread(47, 126, 32, "Moss Green", "");
            case 57:
                return new EmbThread(255, 204, 204, "Flesh Pink", "");
            case 58:
                return new EmbThread(255, 217, 17, "Harvest Gold", "");
            case 59:
                return new EmbThread(9, 91, 166, "Electric Blue", "");
            case 60:
                return new EmbThread(240, 249, 112, "Lemon Yellow", "");
            case 61:
                return new EmbThread(227, 243, 91, "Fresh Green", "");
            case 62:
                return new EmbThread(255, 153, 0, "Orange", "");
            case 63:
                return new EmbThread(255, 240, 141, "Cream Yellow", "");
            case 64:
                return new EmbThread(255, 200, 200, "Applique", "");

        }
        return null;
    }

    public static ArrayList<EmbThread> getThreads() {
        ArrayList<EmbThread> threads = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            threads.add(getThreadByIndex(i));
        }
        return threads;
    }

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        try {
            stream.skip(0x38);
            int colorChanges = stream.read();
            for (int i = 0; i <= colorChanges; i++) {
                int index = stream.read();
                pattern.addThread(getThreadByIndex(index % 65));
            }
            stream.skip(0x221 - (0x38 + 1 + colorChanges));
            readPecStitches(pattern, stream);
        } catch (IOException ex) {
        }
    }

    private static void encodeJump(OutputStream file, int x, int types) throws IOException {
        int outputVal = Math.abs(x) & 0x7FF;
        int orPart = 0x80;
        if ((types & IFormat.TRIM) == IFormat.TRIM) {
            orPart |= 0x20;
        } else if ((types & IFormat.JUMP) == IFormat.JUMP) {
            orPart |= 0x10;
        }
        if (x < 0) {
            outputVal = x + 0x1000 & 0x7FF;
            outputVal |= 0x800;
        }
        file.write(((outputVal >> 8) & 0x0F) | orPart);
        file.write((outputVal & 0xFF));
    }

    private static void pecEncodeStop(OutputStream file, byte val) throws IOException {
        file.write(0xFE);
        file.write(0xB0);
        file.write(val);
    }

    private static void pecEncode(OutputStream file, EmbPattern pattern) throws IOException {
        double thisX = 0.0;
        double thisY = 0.0;
        byte stopCode = 2;
        long deltaX, deltaY;
        for (Point point : new PointIterator<Points>(pattern.getStitches())) {
            int flags = point.data();
            if (flags == IFormat.COLOR_CHANGE) {
                pecEncodeStop(file, stopCode);
                if (stopCode == (byte) 2) {
                    stopCode = (byte) 1;
                } else {
                    stopCode = (byte) 2;
                }
                continue;
            }

            deltaX = Math.round(point.getX() - thisX);
            deltaY = Math.round(point.getY() - thisY);
            thisX += (double) deltaX;
            thisY += (double) deltaY;

            if (deltaX < 63 && deltaX > -64 && deltaY < 63 && deltaY > -64 && ((flags & (IFormat.JUMP | IFormat.TRIM)) == 0)) {
                file.write((byte) ((deltaX < 0) ? (deltaX + 0x80) : deltaX));
                file.write((byte) ((deltaY < 0) ? (deltaY + 0x80) : deltaY));
            } else {
                encodeJump(file, (int) deltaX, flags);
                encodeJump(file, (int) deltaY, flags);
            }
        }
        file.write(0xFF);
    }

    private static void writeImage(OutputStream stream, byte[][] image) throws IOException {
        int i, j;
        for (i = 0; i < 38; i++) {
            for (j = 0; j < 6; j++) {
                int offset = j * 8;
                byte output = 0;
                output |= (byte) (image[i][offset] != 0 ? 1 : 0);
                output |= (byte) ((image[i][offset + 1] != (byte) 0) ? 1 : 0) << 1;
                output |= (byte) ((image[i][offset + 2] != (byte) 0) ? 1 : 0) << 2;
                output |= (byte) ((image[i][offset + 3] != (byte) 0) ? 1 : 0) << 3;
                output |= (byte) ((image[i][offset + 4] != (byte) 0) ? 1 : 0) << 4;
                output |= (byte) ((image[i][offset + 5] != (byte) 0) ? 1 : 0) << 5;
                output |= (byte) ((image[i][offset + 6] != (byte) 0) ? 1 : 0) << 6;
                output |= (byte) ((image[i][offset + 7] != (byte) 0) ? 1 : 0) << 7;
                stream.write(output);
            }
        }
    }

    private static void clearImage(byte[][] image) {
        for (byte[] row : image) {
            Arrays.fill(row, (byte) 0);
        }
    }

    public static void writePecStitches(EmbPattern pattern, OutputStream file, String fileName) throws IOException {
        byte image[][] = new byte[38][48];
        int i, currentThreadCount, graphicsOffsetValue, height, width;
        double xFactor, yFactor;
        int dotPos = fileName.lastIndexOf(".");
        int start = fileName.lastIndexOf("/");
        file.write("LA:".getBytes());
        String internalFilename = fileName.substring(Math.max(0, start), Math.max(0, dotPos));
        if (internalFilename.length() > 16) {
            internalFilename = internalFilename.substring(0, 16);
        }
        file.write(internalFilename.getBytes());
        for (i = 0; i < (16 - internalFilename.length()); i++) {
            file.write(0x20);
        }
        file.write(0x0D);
        for (i = 0; i < 12; i++) {
            file.write(0x20);
        }
        file.write(0xFF);
        file.write(0x00);
        file.write(0x06);
        file.write(0x26);

        for (i = 0; i < 12; i++) {
            file.write(0x20);
        }
        ArrayList<EmbThread> pecThreads = getThreads();

        currentThreadCount = pattern.getThreadList().size();
        file.write((byte) (currentThreadCount - 1));
        for (EmbThread thread : pattern.getThreadList()) {
            file.write((byte) EmbThread.findNearestColorIndex(thread.color, pecThreads));
        }
        for (i = 0; i < (0x1CF - currentThreadCount); i++) {
            file.write(0x20);
        }
        file.write(0x00);
        file.write(0x00);

        ByteArrayOutputStream tempArray = new ByteArrayOutputStream();
        pecEncode(tempArray, pattern);

        graphicsOffsetValue = tempArray.size() + 17;
        file.write(graphicsOffsetValue & 0xFF);
        file.write((graphicsOffsetValue >> 8) & 0xFF);
        file.write((graphicsOffsetValue >> 16) & 0xFF);

        file.write(0x31);
        file.write(0xFF);
        file.write(0xF0);

        RectF bounds = pattern.calculateBoundingBox();

        height = Math.round(bounds.height());
        width = Math.round(bounds.width());
    /* write 2 byte x size */
        BinaryHelper.writeShort(file, (short) width);
    /* write 2 byte y size */
        BinaryHelper.writeShort(file, (short) height);

    /* Write 4 miscellaneous int16's */
        BinaryHelper.writeShort(file, (short) 0x1E0);
        BinaryHelper.writeShort(file, (short) 0x1B0);

        BinaryHelper.writeShortBE(file, (0x9000 | -Math.round(bounds.left)));
        BinaryHelper.writeShortBE(file, (0x9000 | -Math.round(bounds.top)));
        file.write(tempArray.toByteArray());

    /* Writing all colors */
        clearImage(image);
        yFactor = 32.0 / height;
        xFactor = 42.0 / width;
        for (EmbObject object : pattern.asStitchEmbObjects()) {
            Points points = object.getPoints();
            for (int j = 0, je = points.size(); j < je; j++) {
                int x = (int) Math.round((points.getX(j) - bounds.left) * xFactor) + 3;
                int y = (int) Math.round((points.getY(j) - bounds.top) * yFactor) + 3;
                image[y][x] = 1;
            }
        }
        writeImage(file, image);

    /* Writing each individual color */
        clearImage(image);

        for (EmbObject object : pattern.asColorEmbObjects()) {
            clearImage(image);
            Points points = object.getPoints();
            for (int j = 0, je = points.size(); j < je; j++) {
                if (points.getData(j) != IFormat.NORMAL) continue;
                int x = (int) Math.round((points.getX(j) - bounds.left) * xFactor) + 3;
                int y = (int) Math.round((points.getY(j) - bounds.top) * yFactor) + 3;
                image[y][x] = 1;
            }
            writeImage(file, image);
        }
        writeImage(file, image);
    }

    public void write(EmbPattern pattern, OutputStream stream) {
        try {
            pattern.fixColorCount();
            //pattern.correctForMaxStitchLength(pattern, 12.7, 204.7);
            stream.write("#PEC0001".getBytes());
            writePecStitches(pattern, stream, "TEMPFILE.PEC");
        } catch (Exception e) {
        }
    }
}
