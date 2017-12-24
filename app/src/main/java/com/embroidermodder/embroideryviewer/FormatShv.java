//package com.embroidermodder.embroideryviewer;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//public class FormatShv implements IFormat.Reader {
//
//    public static EmbThread getThreadByIndex(int index) {
//        switch (index) {
//            case 0:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 1:
//                return new EmbThread(0, 0, 255, "Blue", "");
//            case 2:
//                return new EmbThread(51, 204, 102, "Green", "");
//            case 3:
//                return new EmbThread(255, 0, 0, "Red", "");
//            case 4:
//                return new EmbThread(255, 0, 255, "Purple", "");
//            case 5:
//                return new EmbThread(255, 255, 0, "Yellow", "");
//            case 6:
//                return new EmbThread(127, 127, 127, "Gray", "");
//            case 7:
//                return new EmbThread(51, 154, 255, "Light Blue", "");
//            case 8:
//                return new EmbThread(0, 255, 0, "Light Green", "");
//            case 9:
//                return new EmbThread(255, 127, 0, "Orange", "");
//            case 10:
//                return new EmbThread(255, 160, 180, "Pink", "");
//            case 11:
//                return new EmbThread(153, 75, 0, "Brown", "");
//            case 12:
//                return new EmbThread(255, 255, 255, "White", "");
//            case 13:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 14:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 15:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 16:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 17:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 18:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 19:
//                return new EmbThread(255, 127, 127, "Light Red", "");
//            case 20:
//                return new EmbThread(255, 127, 255, "Light Purple", "");
//            case 21:
//                return new EmbThread(255, 255, 153, "Light Yellow", "");
//            case 22:
//                return new EmbThread(192, 192, 192, "Light Gray", "");
//            case 23:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 24:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 25:
//                return new EmbThread(255, 165, 65, "Light Orange", "");
//            case 26:
//                return new EmbThread(255, 204, 204, "Light Pink", "");
//            case 27:
//                return new EmbThread(175, 90, 10, "Light Brown", "");
//            case 28:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 29:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 30:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 31:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 32:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 33:
//                return new EmbThread(0, 0, 127, "Dark Blue", "");
//            case 34:
//                return new EmbThread(0, 127, 0, "Dark Green", "");
//            case 35:
//                return new EmbThread(127, 0, 0, "Dark Red", "");
//            case 36:
//                return new EmbThread(127, 0, 127, "Dark Purple", "");
//            case 37:
//                return new EmbThread(200, 200, 0, "Dark Yellow", "");
//            case 38:
//                return new EmbThread(60, 60, 60, "Dark Gray", "");
//            case 39:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 40:
//                return new EmbThread(0, 0, 0, "Black", "");
//            case 41:
//                return new EmbThread(232, 63, 0, "Dark Orange", "");
//            case 42:
//                return new EmbThread(255, 102, 122, "Dark Pink", "");
//        }
//        return null;
//    }
//
//    public boolean hasColor() {
//        return true;
//    }
//
//    public boolean hasStitches() {
//        return true;
//    }
//
//    private static int shvDecode(int inputByte) {
//        return (inputByte > 0x80) ? -((~((byte) inputByte)) + 1) : inputByte;
//    }
//
//    private static short shvDecodeShort(int input) {
//        if (input > 0x8000) {
//            return (short) -((short) ((~input) + 1));
//        }
//        return (short) input;
//    }
//
//    public void read(EmbPattern pattern, InputStream stream) {
//        int numberOfColors;
//        try {
//            int fileNameLength, designWidth, designHeight;
//            int halfDesignWidth, halfDesignHeight, halfDesignWidth2, halfDesignHeight2;
//            String headerText = "Embroidery disk created using software licensed from Viking Sewing Machines AB, Sweden";
//            int dx, dy;
//            int magicCode;
//            int something;
//            int left, top, right, bottom;
//            int something2, numberOfSections, something3;
//            boolean inJump = false;
//            stream.skip(headerText.length());
//            fileNameLength = BinaryHelper.readInt8(stream);
//            stream.skip(fileNameLength);
//            designWidth = BinaryHelper.readInt8(stream);
//            designHeight = BinaryHelper.readInt8(stream);
//            halfDesignWidth = BinaryHelper.readInt8(stream);
//            halfDesignHeight = BinaryHelper.readInt8(stream);
//            halfDesignWidth2 = BinaryHelper.readInt8(stream);
//            halfDesignHeight2 = BinaryHelper.readInt8(stream);
//            if ((designHeight % 2) == 1) {
//                stream.skip((designHeight + 1) * designWidth / 2);
//            } else {
//                stream.skip(designHeight * designWidth / 2);
//            }
//            numberOfColors = BinaryHelper.readInt8(stream);
//            magicCode = BinaryHelper.readInt16LE(stream);
//            int reserved = BinaryHelper.readInt8(stream);
//            something = BinaryHelper.readInt32LE(stream);
//
//            left = BinaryHelper.readInt16LE(stream);
//            top = BinaryHelper.readInt16LE(stream);
//            right = BinaryHelper.readInt16LE(stream);
//            bottom = BinaryHelper.readInt16LE(stream);
//
//            something2 = BinaryHelper.readInt8(stream);
//            numberOfSections = BinaryHelper.readInt8(stream);
//            something3 = BinaryHelper.readInt8(stream);
//
//            Map<Integer, Integer> stitchesPerColor = new HashMap<Integer, Integer>();
//            for (int i = 0; i < numberOfColors; i++) {
//                int colorNumber;
//                int stitchCount;
//                stitchCount = BinaryHelper.readInt32BE(stream);
//                colorNumber = BinaryHelper.readInt8(stream);
//                pattern.addThread(getThreadByIndex(colorNumber % 43));
//                stitchesPerColor.put(i, stitchCount);
//                stream.skip(9);
//            }
//            stream.skip(-2); // is this a problem? negative values?
//            int stitchesSinceStop = 0;
//            int currColorIndex = 0;
//            while (true) {
//                int b0, b1;
//                int flags;
//                if (inJump) {
//                    flags = IFormat.JUMP;
//                } else {
//                    flags = IFormat.NORMAL;
//                }
//                b0 = BinaryHelper.readInt8(stream);
//                b1 = BinaryHelper.readInt8(stream);
//                if (stitchesPerColor.containsKey(currColorIndex) && stitchesSinceStop >= stitchesPerColor.get(currColorIndex)) {
//                    pattern.addStitchRel(0, 0, IFormat.STOP, true);
//                    stitchesSinceStop = 0;
//                    currColorIndex++;
//                }
//                if (b0 == 0x80) {
//                    stitchesSinceStop++;
//                    if (b1 == 3) {
//                        continue;
//                    } else if (b1 == 0x02) {
//                        inJump = false;
//                        continue;
//                    } else if (b1 == 0x01) {
//                        int sx, sy;
//                        stitchesSinceStop += 2;
//                        sx = BinaryHelper.readInt16BE(stream);
//                        sy = BinaryHelper.readInt16BE(stream);
//                        flags = IFormat.TRIM;
//                        inJump = true;
//                        pattern.addStitchRel(shvDecodeShort(sx), shvDecodeShort(sy), flags, true);
//                        continue;
//                    }
//                }
//                dx = shvDecode(b0);
//                dy = shvDecode(b1);
//                stitchesSinceStop++;
//                pattern.addStitchRel(dx, dy, flags, true);
//            }
//        } catch (IOException ex) {
//
//        }
//        pattern.addStitchRel(0, 0, IFormat.END, true);
//    }
//}
