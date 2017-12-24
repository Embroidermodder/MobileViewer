package com.embroidermodder.embroideryviewer;

import java.io.IOException;


public class EmbReaderVP3 extends EmbReader {

    private String vp3ReadString() throws IOException {
        int stringLength = readInt16BE();
        byte content[] = new byte[stringLength];
        readFully(content);
        return new String(content);
    }

    private static int vp3Decode(int inputByte) {
        return (inputByte > 0x80) ? -((~inputByte) + 1) : inputByte;
    }

    private static short vp3DecodeInt16(int input) {
        if (input > 0x8000) {
            return (short) -((short) ((~input) + 1));
        }
        return (short) input;
    }

    private class vp3Hoop {
        int right;
        int bottom;
        int left;
        int top;
        int threadLength;
        byte unknown2;
        int numberOfColors;
        int unknown3;
        int unknown4;
        int numberOfBytesRemaining;

        int xOffset;
        int yOffset;

        byte byte1;
        byte byte2;
        byte byte3;

        /* Centered hoop dimensions */
        int right2;
        int left2;
        int bottom2;
        int top2;

        int width;
        int height;
    }

    private vp3Hoop vp3ReadHoopSection() throws IOException {
        vp3Hoop hoop = new vp3Hoop();
        hoop.right = readInt32BE();
        hoop.bottom = readInt32BE();
        hoop.left = readInt32BE();
        hoop.top = readInt32BE();

        hoop.threadLength = readInt32LE();
        hoop.unknown2 = (byte) readInt8();
        hoop.numberOfColors = readInt8();
        hoop.unknown3 = readInt16BE();
        hoop.unknown4 = readInt32BE();
        hoop.numberOfBytesRemaining = readInt32BE();

        hoop.xOffset = readInt32BE();
        hoop.yOffset = readInt32BE();

        hoop.byte1 = (byte) readInt8();
        hoop.byte2 = (byte) readInt8();
        hoop.byte3 = (byte) readInt8();

    /* Centered hoop dimensions */
        hoop.right2 = readInt32BE();
        hoop.left2 = readInt32BE();
        hoop.bottom2 = readInt32BE();
        hoop.top2 = readInt32BE();

        hoop.width = readInt32BE();
        hoop.height = readInt32BE();
        return hoop;
    }

    @Override
    protected void read() throws IOException {
        byte magicString[] = new byte[5];
        byte some;
        String softwareVendorString = "";
        byte v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18;
        String anotherSoftwareVendorString = "";
        int numberOfColors;
        long colorSectionOffset;
        byte magicCode[] = new byte[6];
        short someShort;
        byte someByte;
        int bytesRemainingInFile;
        String fileCommentString = ""; /* some software writes used settings here */
        int hoopConfigurationOffset;
        String anotherCommentString = "";
        int i;

        stream.read(magicString); /* %vsm% */
        some = (byte) readInt8(); /* 0 */
        softwareVendorString = vp3ReadString();
        someShort = (short) readInt16LE();
        someByte = (byte) readInt8();
        bytesRemainingInFile = readInt32BE();
        fileCommentString = vp3ReadString();
        vp3ReadHoopSection();

        anotherCommentString = vp3ReadString();

    /* TODO: review v1 thru v18 variables and use emb_unused() if needed */
        v1 = (byte) readInt8();
        v2 = (byte) readInt8();
        v3 = (byte) readInt8();
        v4 = (byte) readInt8();
        v5 = (byte) readInt8();
        v6 = (byte) readInt8();
        v7 = (byte) readInt8();
        v8 = (byte) readInt8();
        v9 = (byte) readInt8();
        v10 = (byte) readInt8();
        v11 = (byte) readInt8();
        v12 = (byte) readInt8();
        v13 = (byte) readInt8();
        v14 = (byte) readInt8();
        v15 = (byte) readInt8();
        v16 = (byte) readInt8();
        v17 = (byte) readInt8();
        v18 = (byte) readInt8();

        readFully(magicCode); /* 0x78 0x78 0x55 0x55 0x01 0x00 */

        anotherSoftwareVendorString = vp3ReadString();
        numberOfColors = readInt16BE();
        readInt8();
        for (i = 0; i < numberOfColors; i++) {
            EmbThread t = new EmbThread();
            pattern.addThread(t);
            byte tableSize;
            int startX, startY;
            String threadColorNumber, colorName, threadVendor;
            int unknownThreadString, numberOfBytesInColor;

            readInt8();
            readInt8();
            int sectionLength = readInt32BE();
            startX = readInt32BE();
            startY = readInt32BE();
            pattern.addStitchAbs(startX / 100, -startY / 100, EmbPattern.JUMP, true);

            tableSize = (byte) readInt8();
            readInt8();

            int r = readInt8() & 0xFF;
            int g = readInt8() & 0xFF;
            int b = readInt8() & 0xFF;
            skip(6 * tableSize - 1);
            threadColorNumber = vp3ReadString();
            colorName = vp3ReadString();
            threadVendor = vp3ReadString();
            t.setColor(r, g, b);
            t.setDescription(colorName);
            t.setCatalogNumber(threadColorNumber);
            t.setBrand(threadVendor);

            if (i > 0) {
                pattern.addStitchRel(0, 0, EmbPattern.STOP, true);
            }
            int offsetToNextColorX = readInt32BE();
            int offsetToNextColorY = readInt32BE();

            unknownThreadString = readInt16BE();
            skip(unknownThreadString);
            numberOfBytesInColor = readInt32BE();
            skip(3);
            int position = 0;
            while (position < numberOfBytesInColor - 1) {
                int x = vp3Decode((byte) readInt8());
                int y = vp3Decode((byte) readInt8());
                position += 2;
                if (x == -128) { //0x80) {
                    switch (y) {
                        case 0x00:
                        case 0x03:
                            break;
                        case 0x01:
                            x = vp3DecodeInt16(readInt16BE());
                            y = vp3DecodeInt16(readInt16BE());
                            readInt16BE();
                            position += 6;
                            pattern.addStitchRel(x, y, EmbPattern.TRIM, true);
                            break;
                        default:
                            break;
                    }
                } else {
                    pattern.addStitchRel(x, y, EmbPattern.STITCH, true);
                }
            }
        }
    }
}
