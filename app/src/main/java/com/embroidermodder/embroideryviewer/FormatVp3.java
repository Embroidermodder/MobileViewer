package com.embroidermodder.embroideryviewer;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FormatVp3 implements IFormat.Reader { //}, IFormat.Writer {

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

    private String vp3ReadString(InputStream stream) throws IOException {
        int stringLength = BinaryHelper.readInt16BE(stream);
        byte content[] = new byte[stringLength];
        stream.read(content);

        return new String(content);
    }

    private static int vp3Decode(int inputByte)
    {
        return (inputByte > 0x80) ? -((~inputByte) + 1) : inputByte;
    }

    private static short vp3DecodeInt16(int input)
    {
        if(input > 0x8000)
        {
            return (short)-((short) ((~input) + 1));
        }
        return (short)input;
    }

    private class vp3Hoop
    {
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

    private vp3Hoop vp3ReadHoopSection(InputStream stream) throws IOException {
        vp3Hoop hoop = new vp3Hoop();
        hoop.right = BinaryHelper.readInt32BE(stream);
        hoop.bottom = BinaryHelper.readInt32BE(stream);
        hoop.left = BinaryHelper.readInt32BE(stream);
        hoop.top = BinaryHelper.readInt32BE(stream);

        hoop.threadLength = BinaryHelper.readInt32LE(stream);
        hoop.unknown2 = (byte)stream.read();
        hoop.numberOfColors = stream.read();
        hoop.unknown3 = BinaryHelper.readInt16BE(stream);
        hoop.unknown4 = BinaryHelper.readInt32BE(stream);
        hoop.numberOfBytesRemaining = BinaryHelper.readInt32BE(stream);

        hoop.xOffset = BinaryHelper.readInt32BE(stream);
        hoop.yOffset = BinaryHelper.readInt32BE(stream);

        hoop.byte1 = (byte)stream.read();
        hoop.byte2 = (byte)stream.read();
        hoop.byte3 = (byte)stream.read();

    /* Centered hoop dimensions */
        hoop.right2 = BinaryHelper.readInt32BE(stream);
        hoop.left2 = BinaryHelper.readInt32BE(stream);
        hoop.bottom2 = BinaryHelper.readInt32BE(stream);
        hoop.top2 = BinaryHelper.readInt32BE(stream);

        hoop.width = BinaryHelper.readInt32BE(stream);
        hoop.height = BinaryHelper.readInt32BE(stream);
        return hoop;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        try {
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
            some = (byte)stream.read(); /* 0 */
            softwareVendorString = vp3ReadString(stream);
            someShort = (short)BinaryHelper.readInt16LE(stream);
            someByte = (byte)stream.read();
            bytesRemainingInFile = BinaryHelper.readInt32BE(stream);
            fileCommentString = vp3ReadString(stream);
            vp3ReadHoopSection(stream);

            anotherCommentString = vp3ReadString(stream);

    /* TODO: review v1 thru v18 variables and use emb_unused() if needed */
            v1 = (byte)stream.read();
            v2 = (byte)stream.read();
            v3 = (byte)stream.read();
            v4 = (byte)stream.read();
            v5 = (byte)stream.read();
            v6 = (byte)stream.read();
            v7 = (byte)stream.read();
            v8 = (byte)stream.read();
            v9 = (byte)stream.read();
            v10 = (byte)stream.read();
            v11 = (byte)stream.read();
            v12 = (byte)stream.read();
            v13 = (byte)stream.read();
            v14 = (byte)stream.read();
            v15 = (byte)stream.read();
            v16 = (byte)stream.read();
            v17 = (byte)stream.read();
            v18 = (byte)stream.read();

            stream.read(magicCode); /* 0x78 0x78 0x55 0x55 0x01 0x00 */

            anotherSoftwareVendorString = vp3ReadString(stream);
            numberOfColors = BinaryHelper.readInt16BE(stream);
            stream.read();
            for (i = 0; i < numberOfColors; i++) {
                EmbThread t = new EmbThread();
                pattern.addThread(t);
                byte tableSize;
                int startX, startY;
                String threadColorNumber, colorName, threadVendor;
                int unknownThreadString, numberOfBytesInColor;


                stream.read();
                stream.read();
                int sectionLength = BinaryHelper.readInt32BE(stream);
                startX = BinaryHelper.readInt32BE(stream);
                startY = BinaryHelper.readInt32BE(stream);
                pattern.addStitchAbs(startX / 100, -startY / 100, IFormat.JUMP, true);

                tableSize = (byte)stream.read();
                stream.read();

                int r = stream.read() & 0xFF;
                int g = stream.read() & 0xFF;
                int b = stream.read() & 0xFF;
                stream.skip(6 * tableSize - 1);
                threadColorNumber = vp3ReadString(stream);
                colorName = vp3ReadString(stream);
                threadVendor = vp3ReadString(stream);
                t.setColor(new EmbColor(r, g, b));
                t.setDescription(colorName);
                t.setCatalogNumber(threadColorNumber);

                if (i > 0) {
                    pattern.addStitchRel(0, 0, IFormat.STOP, true);
                }
                int offsetToNextColorX = BinaryHelper.readInt32BE(stream);
                int offsetToNextColorY = BinaryHelper.readInt32BE(stream);

                unknownThreadString = BinaryHelper.readInt16BE(stream);
                stream.skip(unknownThreadString);
                numberOfBytesInColor = BinaryHelper.readInt32BE(stream);
                stream.skip(3);
                int position = 0;
                while (position < numberOfBytesInColor - 1) {
                    int x = vp3Decode((byte)stream.read());
                    int y = vp3Decode((byte)stream.read());
                    position += 2;
                    if (x == -128) { //0x80) {
                        switch (y) {
                            case 0x00:
                            case 0x03:
                                break;
                            case 0x01:
                                x = vp3DecodeInt16(BinaryHelper.readInt16BE(stream));
                                y = vp3DecodeInt16(BinaryHelper.readInt16BE(stream));
                                BinaryHelper.readInt16BE(stream);
                                position += 6;
                                pattern.addStitchRel(x, y, IFormat.TRIM, true);
                                break;
                            default:
                                break;
                        }
                    } else {
                        pattern.addStitchRel(x, y, IFormat.NORMAL, true);
                    }
                }
            }

        }
        catch(IOException e) {
            Log.v("DEBUG", e.toString());
        }
        pattern.addStitchRel(0, 0, IFormat.END, true);
    }

    private void vp3WriteString(OutputStream stream, String str) throws IOException {
        BinaryHelper.writeShortBE(stream, str.length());
        stream.write(str.getBytes());
    }

//    void vp3PatchByteCount(OutputStream file, int offset, int adjustment) {
//        int currentPos = embFile_tell(file);
//        embFile_seek(file, offset, SEEK_SET);
//        BinaryHelper.writeInt32BE(file, currentPos - offset + adjustment);
//        embFile_seek(file, currentPos, SEEK_SET);
//    }


    public void write(EmbPattern pattern, DataOutputStream stream) {
//        try {
//        int remainingBytesPos, remainingBytesPos2;
//        int colorSectionStitchBytes;
//        int first = 1;
//        int numberOfColors = 0;
//        EmbColor color = new EmbColor(0xFE, 0xFE, 0xFE);
//        RectF bounds = pattern.calculateBoundingBox();
//
//        pattern.correctForMaxStitchLength(pattern, 3200.0, 3200.0); /* VP3 can encode signed 16bit deltas */
//
//        pattern.getFlippedPattern(false, true);
//
//            stream.write("%vsm%".getBytes());
//            stream.write(0);
//            vp3WriteString(stream, "Embroidermodder");
//            stream.write(0);
//            stream.write(2);
//            stream.write(0);
//        remainingBytesPos = embFile_tell(stream);
//        BinaryHelper.writeInt32(stream, 0); /* placeholder */
//        vp3WriteString(stream, "");
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.right * 1000));
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.bottom * 1000));
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.left * 1000));
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.top * 1000));
//        BinaryHelper.writeInt32(stream, 0); /* this would be some (unknown) function of thread length */
//        stream.write(0);
//
//        numberOfColors = 0;

//        mainPointer = pattern->stitchList;
//        while(mainPointer)
//        {
//            int flag;
//            EmbColor newColor;
//
//            pointer = mainPointer;
//            flag = pointer->stitch.flags;
//            newColor = embThreadList_getAt(pattern->threadList, pointer->stitch.color).color;
//            if(newColor.r != color.r || newColor.g != color.g || newColor.b != color.b)
//            {
//                numberOfColors++;
//                color.r = newColor.r;
//                color.g = newColor.g;
//                color.b = newColor.b;
//            }
//            else if(flag & END || flag & STOP)
//            {
//                numberOfColors++;
//            }
//
//            while(pointer && (flag == pointer->stitch.flags))
//            {
//                pointer = pointer->next;
//            }
//            mainPointer = pointer;
//        }
//
//        stream.write(numberOfColors);
//        stream.write(12);
//        stream.write(0);
//        stream.write(1);
//        stream.write(0);
//        stream.write(3);
//        stream.write(0);
//
//        remainingBytesPos2 = embstream_tell(stream);
//        BinaryHelper.writeInt32(stream, 0); /* placeholder */
//
//        BinaryHelper.writeInt32BE(stream, 0); /* origin X */
//        BinaryHelper.writeInt32BE(stream, 0); /* origin Y */
//        stream.write(0);
//        stream.write(0);
//        stream.write(0);
//
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.right * 1000));
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.bottom * 1000));
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.left * 1000));
//        BinaryHelper.writeInt32BE(stream, Math.round(bounds.top * 1000));
//
//        BinaryHelper.writeInt32BE(stream, Math.round((bounds.right - bounds.left) * 1000);
//        BinaryHelper.writeInt32BE(stream, Math.round((bounds.bottom - bounds.top) * 1000);
//
//        vp3WriteString(stream, "");
//        BinaryHelper.writeShortBE(stream, 25700);
//        BinaryHelper.writeInt32BE(stream, 4096);
//        BinaryHelper.writeInt32BE(stream, 0);
//        BinaryHelper.writeInt32BE(stream, 0);
//        BinaryHelper.writeInt32BE(stream, 4096);
//        stream.write(0x78);
//        stream.write(0x78);
//        stream.write(0x50);
//        stream.write(0x50);
//        stream.write(0x01);
//        stream.write(0x00);
//        vp3WriteString(stream, "");
//        BinaryHelper.writeShortBE(stream, numberOfColors);

//        mainPointer = pattern->stitchList;
//        while(mainPointer)
//        {
//            char colorName[8] = { 0 };
//            double lastX, lastY;
//            int colorSectionLengthPos;
//            EmbStitch s;
//            int lastColor;
//
//            if (!first)
//            {
//                stream.write(0);
//            }
//            stream.write(0);
//            stream.write(5);
//            stream.write(0);
//
//            colorSectionLengthPos = embstream_tell(stream);
//            binaryWriteInt(stream, 0); /* placeholder */
//
//            pointer = mainPointer;
//            color = embThreadList_getAt(pattern->threadList, pointer->stitch.color).color;
//
//            if (first && pointer->stitch.flags & JUMP && pointer->next->stitch.flags & JUMP)
//            {
//                pointer = pointer->next;
//            }
//
//            s = pointer->stitch;
//            BinaryHelper.writeInt32BE(stream, s.xx * 1000);
//            BinaryHelper.writeInt32BE(stream, -s.yy * 1000);
//            pointer = pointer->next;
//
//            first = 0;
//
//            lastX = s.xx;
//            lastY = s.yy;
//            lastColor = s.color;
//
//            stream.write(1);
//            stream.write(0);

//            stream.write(color.r);
//            stream.write(color.g);
//            stream.write(color.b);
//
//            stream.write(0);
//            stream.write(0);
//            stream.write(0);
//            stream.write(5);
//            stream.write(40);
//
//            vp3WriteString(stream, "");
//
//            sprintf(colorName, "#%02x%02x%02x", color.b, color.g, color.r);
//
//            vp3WriteString(stream, colorName);
//            vp3WriteString(stream, "");
//
//            BinaryHelper.writeInt32BE(stream, 0);
//            BinaryHelper.writeInt32BE(stream, 0);
//
//            vp3WriteStringLen(stream, "\0", 1);
//
//            colorSectionStitchBytes = embstream_tell(stream);
//            binaryWriteInt(stream, 0); /* placeholder */
//
//            stream.write(10);
//            stream.write(246);
//            stream.write(0);
//
//            while(pointer)
//            {
//                int dx, dy;
//
//                EmbStitch s = pointer->stitch;
//                if (s.color != lastColor)
//                {
//                    break;
//                }
//                if (s.flags & END || s.flags & STOP)
//                {
//                    break;
//                }
//                dx = (s.xx - lastX) * 10;
//                dy = (s.yy - lastY) * 10;
//                lastX = lastX + dx / 10.0; /* output is in ints, ensure rounding errors do not sum up */
//                lastY = lastY + dy / 10.0;
//
//                if(dx < -127 || dx > 127 || dy < -127 || dy > 127)
//                {
//                    stream.write(128);
//                    stream.write(1);
//                    binaryWriteShortBE(stream, dx);
//                    binaryWriteShortBE(stream, dy);
//                    stream.write(128);
//                    stream.write(2);
//                }
//                else
//                {
//                    stream.write(dx);
//                    stream.write(dy);
//                }
//
//                pointer = pointer->next;
//            }
//
//            vp3PatchByteCount(stream, colorSectionStitchBytes, -4);
//            vp3PatchByteCount(stream, colorSectionLengthPos, -3);
//
//            mainPointer = pointer;
//        }
//
//        vp3PatchByteCount(stream, remainingBytesPos2, -4);
//        vp3PatchByteCount(stream, remainingBytesPos, -4);
//        }
//        catch(IOException e) {
//
//        }
    }
}
