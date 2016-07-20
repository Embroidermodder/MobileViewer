package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;

public class FormatSew implements IFormat.Reader {

    public static EmbThread getThreadByIndex(int index) {
        switch (index) {
            case 0:
                return new EmbThread(0, 0, 0, "Unknown", "");
            case 1:
                return new EmbThread(0, 0, 0, "Black", "");
            case 2:
                return new EmbThread(255, 255, 255, "White", "");
            case 3:
                return new EmbThread(255, 255, 23, "Sunflower", "");
            case 4:
                return new EmbThread(250, 160, 96, "Hazel", "");
            case 5:
                return new EmbThread(92, 118, 73, "Green Dust", "");
            case 6:
                return new EmbThread(64, 192, 48, "Green", "");
            case 7:
                return new EmbThread(101, 194, 200, "Sky", "");
            case 8:
                return new EmbThread(172, 128, 190, "Purple", "");
            case 9:
                return new EmbThread(245, 188, 203, "Pink", "");
            case 10:
                return new EmbThread(255, 0, 0, "Red", "");
            case 11:
                return new EmbThread(192, 128, 0, "Brown", "");
            case 12:
                return new EmbThread(0, 0, 240, "Blue", "");
            case 13:
                return new EmbThread(228, 195, 93, "Gold", "");
            case 14:
                return new EmbThread(165, 42, 42, "Dark Brown", "");
            case 15:
                return new EmbThread(213, 176, 212, "Pale Violet", "");
            case 16:
                return new EmbThread(252, 242, 148, "Pale Yellow", "");
            case 17:
                return new EmbThread(240, 208, 192, "Pale Pink", "");
            case 18:
                return new EmbThread(255, 192, 0, "Peach", "");
            case 19:
                return new EmbThread(201, 164, 128, "Beige", "");
            case 20:
                return new EmbThread(155, 61, 75, "Wine Red", "");
            case 21:
                return new EmbThread(160, 184, 204, "Pale Sky", "");
            case 22:
                return new EmbThread(127, 194, 28, "Yellow Green", "");
            case 23:
                return new EmbThread(185, 185, 185, "Silver Grey", "");
            case 24:
                return new EmbThread(160, 160, 160, "Grey", "");
            case 25:
                return new EmbThread(152, 214, 189, "Pale Aqua", "");
            case 26:
                return new EmbThread(184, 240, 240, "Baby Blue", "");
            case 27:
                return new EmbThread(54, 139, 160, "Powder Blue", "");
            case 28:
                return new EmbThread(79, 131, 171, "Bright Blue", "");
            case 29:
                return new EmbThread(56, 106, 145, "Slate Blue", "");
            case 30:
                return new EmbThread(0, 32, 107, "Nave Blue", "");
            case 31:
                return new EmbThread(229, 197, 202, "Salmon Pink", "");
            case 32:
                return new EmbThread(249, 103, 107, "Coral", "");
            case 33:
                return new EmbThread(227, 49, 31, "Burnt Orange", "");
            case 34:
                return new EmbThread(226, 161, 136, "Cinnamon", "");
            case 35:
                return new EmbThread(181, 148, 116, "Umber", "");
            case 36:
                return new EmbThread(228, 207, 153, "Blonde", "");
            case 37:
                return new EmbThread(225, 203, 0, "Sunflower", "");
            case 38:
                return new EmbThread(225, 173, 212, "Orchid Pink", "");
            case 39:
                return new EmbThread(195, 0, 126, "Peony Purple", "");
            case 40:
                return new EmbThread(128, 0, 75, "Burgundy", "");
            case 41:
                return new EmbThread(160, 96, 176, "Royal Purple", "");
            case 42:
                return new EmbThread(192, 64, 32, "Cardinal Red", "");
            case 43:
                return new EmbThread(202, 224, 192, "Opal Green", "");
            case 44:
                return new EmbThread(137, 152, 86, "Moss Green", "");
            case 45:
                return new EmbThread(0, 170, 0, "Meadow Green", "");
            case 46:
                return new EmbThread(33, 138, 33, "Dark Green", "");
            case 47:
                return new EmbThread(93, 174, 148, "Aquamarine", "");
            case 48:
                return new EmbThread(76, 191, 143, "Emerald Green", "");
            case 49:
                return new EmbThread(0, 119, 114, "Peacock Green", "");
            case 50:
                return new EmbThread(112, 112, 112, "Dark Grey", "");
            case 51:
                return new EmbThread(242, 255, 255, "Ivory White", "");
            case 52:
                return new EmbThread(177, 88, 24, "Hazel", "");
            case 53:
                return new EmbThread(203, 138, 7, "Toast", "");
            case 54:
                return new EmbThread(247, 146, 123, "Salmon", "");
            case 55:
                return new EmbThread(152, 105, 45, "Cocoa Brown", "");
            case 56:
                return new EmbThread(162, 113, 72, "Sienna", "");
            case 57:
                return new EmbThread(123, 85, 74, "Sepia", "");
            case 58:
                return new EmbThread(79, 57, 70, "Dark Sepia", "");
            case 59:
                return new EmbThread(82, 58, 151, "Violet Blue", "");
            case 60:
                return new EmbThread(0, 0, 160, "Blue Ink", "");
            case 61:
                return new EmbThread(0, 150, 222, "Solar Blue", "");
            case 62:
                return new EmbThread(178, 221, 83, "Green Dust", "");
            case 63:
                return new EmbThread(250, 143, 187, "Crimson", "");
            case 64:
                return new EmbThread(222, 100, 158, "Floral Pink", "");
            case 65:
                return new EmbThread(181, 80, 102, "Wine", "");
            case 66:
                return new EmbThread(94, 87, 71, "Olive Drab", "");
            case 67:
                return new EmbThread(76, 136, 31, "Meadow", "");
            case 68:
                return new EmbThread(228, 220, 121, "Canary Yellow", "");
            case 69:
                return new EmbThread(203, 138, 26, "Toast", "");
            case 70:
                return new EmbThread(198, 170, 66, "Beige", "");
            case 71:
                return new EmbThread(236, 176, 44, "Honey Dew", "");
            case 72:
                return new EmbThread(248, 128, 64, "Tangerine", "");
            case 73:
                return new EmbThread(255, 229, 5, "Ocean Blue", "");
            case 74:
                return new EmbThread(250, 122, 122, "Sepia", "");
            case 75:
                return new EmbThread(107, 224, 0, "Royal Purple", "");
            case 76:
                return new EmbThread(56, 108, 174, "Yellow Ocher", "");
            case 77:
                return new EmbThread(208, 186, 176, "Beige Grey", "");
            case 78:
                return new EmbThread(227, 190, 129, "Bamboo", "");
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
        byte[] b = new byte[2];
        int flags;
        int numberOfColors;
        try {
            numberOfColors = BinaryReader.readInt16LE(stream);
            for (int i = 0; i < numberOfColors; i++) {
                int index = BinaryReader.readInt16LE(stream);
                p.addThread(getThreadByIndex(index % 79));
            }
            stream.skip(0x1D78 - numberOfColors * 2 - 2);
            while (true) {
                flags = IFormat.NORMAL;
                if (stream.read(b) != 2) {
                    break;
                }
                if (((b[0] & 0xFF) == 0x80)) {
                    if ((b[1] & 0x01) != 0) {
                        if (stream.read(b) != 2) {
                            break;
                        }
                        flags = IFormat.STOP;
                    } else if ((b[1] == 0x04) || (b[1] == 0x02)) {
                        if (stream.read(b) != 2) {
                            break;
                        }
                        flags = IFormat.TRIM;
                    } else if (b[1] == 0x10) {
                        p.addStitchRel(0.0f, 0.0f, IFormat.END, true);
                        break;
                    }
                }
                p.addStitchRel((float) b[0], (float) b[1], flags, true);
            }
        } catch (IOException ex) {

        }
        return p.getFlippedPattern(false, true);
    }
}
