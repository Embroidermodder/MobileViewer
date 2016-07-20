package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;

public class FormatPec implements IFormat.Reader {

    public static void readPecStitches(Pattern pattern, DataInputStream stream) {
        try {
            while (stream.available() > 0) {
                int val1 = (stream.readByte() & 0xFF);
                int val2 = (stream.readByte() & 0xFF);

                int stitchType = IFormat.NORMAL;
                if (val1 == 0xFF && val2 == 0x00) {
                    pattern.addStitchRel(0.0f, 0.0f, IFormat.END, true);
                    break;
                }
                if (val1 == 0xFE && val2 == 0xB0) {
                    stream.readByte();
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
                    val2 = stream.readByte() & 0xFF;
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
                    val2 = ((val2 & 0x0F) << 8) + (stream.readByte() & 0xFF);

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

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        try {
            stream.skip(0x38);
            int colorChanges = stream.readByte();
            for (int i = 0; i <= colorChanges; i++) {
                int index = stream.readByte();
                p.addThread(getThreadByIndex(index % 65));
            }
            stream.skip(0x221 - (0x38 + 1 + colorChanges));
            readPecStitches(p, stream);
        } catch (IOException ex) {
        }
        return p;
    }
}
