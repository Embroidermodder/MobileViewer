package com.embroidermodder.embroideryviewer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class EmbReaderSHV extends EmbReader {


    private static int shvDecode(int inputByte) {
        return (inputByte > 0x80) ? -((~((byte) inputByte)) + 1) : inputByte;
    }

    private static short shvDecodeShort(int input) {
        if (input > 0x8000) {
            return (short) -((short) ((~input) + 1));
        }
        return (short) input;
    }


    @Override
    protected void read() throws IOException {
        int numberOfColors;
        try {
            int fileNameLength, designWidth, designHeight;
            int halfDesignWidth, halfDesignHeight, halfDesignWidth2, halfDesignHeight2;
            String headerText = "Embroidery disk created using software licensed from Viking Sewing Machines AB, Sweden";
            int dx, dy;
            int magicCode;
            int something;
            int left, top, right, bottom;
            int something2, numberOfSections, something3;
            boolean inJump = false;
            skip(headerText.length());
            fileNameLength = readInt8();
            skip(fileNameLength);
            designWidth = readInt8();
            designHeight = readInt8();
            halfDesignWidth = readInt8();
            halfDesignHeight = readInt8();
            halfDesignWidth2 = readInt8();
            halfDesignHeight2 = readInt8();
            if ((designHeight % 2) == 1) {
                skip((designHeight + 1) * designWidth / 2);
            } else {
                skip(designHeight * designWidth / 2);
            }
            numberOfColors = readInt8();
            magicCode = readInt16LE();
            int reserved = readInt8();
            something = readInt32LE();

            left = readInt16LE();
            top = readInt16LE();
            right = readInt16LE();
            bottom = readInt16LE();

            something2 = readInt8();
            numberOfSections = readInt8();
            something3 = readInt8();

            Map<Integer, Integer> stitchesPerColor = new HashMap<>();
            EmbThreadShv[] threadSet = EmbThreadShv.getThreadSet();
            for (int i = 0, ie = numberOfColors-1; i <= ie; i++) {
                int colorNumber;
                int stitchCount;
                stitchCount = readInt32BE();
                colorNumber = readInt8();
                pattern.addThread(threadSet[colorNumber % 43]);
                stitchesPerColor.put(i, stitchCount);
                //skip(9);
                if (i == ie) {
                    skip(7);
                }
                else {
                    skip(9);
                }
            }
            //skip(-2); // is this a problem? negative values?

            int stitchesSinceStop = 0;
            int currColorIndex = 0;
            while (true) {
                int b0, b1;
                int flags;
                if (inJump) {
                    flags = EmbPattern.JUMP;
                } else {
                    flags = EmbPattern.STITCH;
                }
                b0 = readInt8();
                b1 = readInt8();
                if (stitchesPerColor.containsKey(currColorIndex) && stitchesSinceStop >= stitchesPerColor.get(currColorIndex)) {
                    changeColor();
                    //pattern.addStitchRel(0, 0, IFormat.STOP, true);
                    stitchesSinceStop = 0;
                    currColorIndex++;
                }
                if (b0 == 0x80) {
                    stitchesSinceStop++;
                    if (b1 == 3) {
                        continue;
                    } else if (b1 == 0x02) {
                        inJump = false;
                        continue;
                    } else if (b1 == 0x01) {
                        int sx, sy;
                        stitchesSinceStop += 2;
                        sx = readInt16BE();
                        sy = readInt16BE();
                        flags = EmbPattern.TRIM;
                        inJump = true;
                        move(shvDecodeShort(sx), shvDecodeShort(sy));
                        //pattern.addStitchRel(shvDecodeShort(sx), shvDecodeShort(sy), flags, true);
                        continue;
                    }
                }
                dx = shvDecode(b0);
                dy = shvDecode(b1);
                stitchesSinceStop++;
                pattern.addStitchRel(dx, dy, flags, true);
            }
        } catch (IOException ex) {

        }
        end();
    }

}
