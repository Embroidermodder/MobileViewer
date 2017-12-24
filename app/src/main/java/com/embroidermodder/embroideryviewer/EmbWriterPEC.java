package com.embroidermodder.embroideryviewer;

import com.embroidermodder.embroideryviewer.geom.DataPoints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class EmbWriterPEC extends EmbWriter {

    static final int MASK_07_BIT = 0b01111111;
    static final int JUMP_CODE = 0b00010000;
    static final int TRIM_CODE = 0b00100000;
    static final int FLAG_LONG = 0b10000000;

    static final int PEC_ICON_WIDTH = 48;
    static final int PEC_ICON_HEIGHT = 38;

    @Override
    public void preWrite(EmbPattern input) {
        TransCoder t = TransCoder.getTranscoder();
        float minX = input.getStitches().getMinX();
        float minY = input.getStitches().getMinY();
        float maxX = input.getStitches().getMaxX();
        float maxY = input.getStitches().getMaxY();

        t.setInitialPosition((int) ((minX + maxX) / 2), (int) ((minY + maxY) / 2));
        t.require_jumps = true;
        t.splitLongJumps = true;
        t.maxJumpLength = 2047;
        t.maxStitchLength = 2047;
        t.snap = true;
        if (isLockStitches()) {
            t.tie_on = true;
            t.tie_off = true;
        }
        t.transcode(input);
    }

    @Override
    public void write() throws IOException {
        write("#PEC0001");
        writePecStitches(getName());
    }

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

    public static int encodeLongForm(int value) {
        value &= 0b00001111_11111111;
        value |= 0b10000000_00000000;
        return value;
    }

    public static int flagJump(int longForm) {
        return longForm | (JUMP_CODE << 8);
    }

    public static int flagTrim(int longForm) {
        return longForm | (TRIM_CODE << 8);
    }

    private void pecEncode() throws IOException {
        boolean colorchangeJump = false;
        boolean colorTwo = true;
        DataPoints stitches = pattern.getStitches();
        int deltaX, deltaY;
        boolean jumping = false;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            switch (stitches.getData(i)) {
                case EmbPattern.STITCH:
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    deltaX = (int) Math.rint(stitches.getX(i));
                    deltaY = (int) Math.rint(stitches.getY(i));
                    if (deltaX < 63 && deltaX > -64 && deltaY < 63 && deltaY > -64) {
                        stream.write(deltaX & MASK_07_BIT);
                        stream.write(deltaY & MASK_07_BIT);
                    } else {
                        deltaX = encodeLongForm(deltaX);
                        stream.write((deltaX >> 8) & 0xFF);
                        stream.write(deltaX & 0xFF);

                        deltaY = encodeLongForm(deltaY);
                        stream.write((deltaY >> 8) & 0xFF);
                        stream.write(deltaY & 0xFF);
                    }
                    break;
                case EmbPattern.JUMP:
                    jumping = true;
                    //if (index != 0) {
                    deltaX = (int) Math.rint(stitches.getX(i));
                    deltaX = encodeLongForm(deltaX);
                    if (colorchangeJump) {
                        deltaX = flagJump(deltaX);
                    } else {
                        deltaX = flagTrim(deltaX);
                    }

                    stream.write((deltaX >> 8) & 0xFF);
                    stream.write(deltaX & 0xFF);

                    deltaY = (int) Math.rint(stitches.getY(i));
                    deltaY = encodeLongForm(deltaY);
                    if (colorchangeJump) {
                        deltaY = flagJump(deltaY);
                    } else {
                        deltaY = flagTrim(deltaY);
                    }

                    stream.write((deltaY >> 8) & 0xFF);
                    stream.write(deltaY & 0xFF);
                    colorchangeJump = false;
                    //}
                    break;
                case EmbPattern.COLOR_CHANGE: //prejump
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    //if (previousColor != 0) {
                    stream.write(0xfe);
                    stream.write(0xb0);
                    stream.write((colorTwo) ? 2 : 1);
                    colorTwo = !colorTwo;
                    colorchangeJump = true;
                    //}
                    break;
                case EmbPattern.STOP:
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    stream.write((byte) 0x80);
                    stream.write((byte) 0x1);
                    stream.write((byte) 0x00);
                    stream.write((byte) 0x00);
                    break;
                case EmbPattern.END:
                    if (jumping) {
                        stream.write((byte) 0x00);
                        stream.write((byte) 0x00);
                        jumping = false;
                    }
                    stream.write(0xff);
                    break;
            }
        }
    }

    public void writePecStitches(String fileName) throws IOException {
        float minX = pattern.getStitches().getMinX();
        float minY = pattern.getStitches().getMinY();
        float maxX = pattern.getStitches().getMaxX();
        float maxY = pattern.getStitches().getMaxY();

        float width = maxX - minX;
        float height = maxY - minY;

        if (fileName == null) {
            fileName = "untitled";
        }
        write("LA:");
        if (fileName.length() > 16) {
            fileName = fileName.substring(0, 8);
        }
        write(fileName);
        for (int i = 0; i < (16 - fileName.length()); i++) {
            writeInt8(0x20);
        }
        writeInt8(0x0D);
        for (int i = 0; i < 12; i++) {
            writeInt8(0x20);
        }
        writeInt8(0xFF);
        writeInt8(0x00);
        writeInt8(0x06);
        writeInt8(0x26);
        EmbThreadPec[] threadSet = EmbThreadPec.getThreadSet();
        final EmbThread[] chart = new EmbThread[threadSet.length];

        List<EmbThread> threads = pattern.getUniqueThreadList();
        for (EmbThread thread : threads) {
            int index = EmbThreadPec.findNearestIndex(thread.getColor(), threadSet);
            threadSet[index] = null;
            chart[index] = thread;
        }

        ByteArrayOutputStream colorTempArray = new ByteArrayOutputStream();
        push(colorTempArray);
        for (EmbObject object : pattern.asColorEmbObjects()) {
            writeInt8(EmbThread.findNearestIndex(object.getThread().getColor(), chart));
        }
        pop();
        int currentThreadCount = colorTempArray.size();
        if (currentThreadCount != 0) {
            for (int i = 0; i < 12; i++) {
                writeInt8(0x20);
            }
            //56

            writeInt8(currentThreadCount - 1);
            write(colorTempArray.toByteArray());
        } else {
            writeInt8(0x20);
            writeInt8(0x20);
            writeInt8(0x20);
            writeInt8(0x20);
            writeInt8(0x64);
            writeInt8(0x20);
            writeInt8(0x00);
            writeInt8(0x20);
            writeInt8(0x00);
            writeInt8(0x20);
            writeInt8(0x20);
            writeInt8(0x20);
            writeInt8(0xFF);
        }
        for (int i = 0; i < (463 - currentThreadCount); i++) {
            writeInt8(0x20);
        } //520
        writeInt8(0x00);
        writeInt8(0x00);

        ByteArrayOutputStream tempArray = new ByteArrayOutputStream();
        push(tempArray);
        pecEncode();
        pop();

        int graphicsOffsetValue = tempArray.size() + 20; //10 //15 //17
        writeInt24LE(graphicsOffsetValue);

        writeInt8(0x31);
        writeInt8(0xFF);
        writeInt8(0xF0);

        /* write 2 byte x size */
        writeInt16LE((short) Math.round(width));
        /* write 2 byte y size */
        writeInt16LE((short) Math.round(height));

        /* Write 4 miscellaneous int16's */
        writeInt16LE((short) 0x1E0);
        writeInt16LE((short) 0x1B0);

        writeInt16BE((0x9000 | -Math.round(minX)));
        writeInt16BE((0x9000 | -Math.round(minY)));
        stream.write(tempArray.toByteArray());

        PecGraphics graphics = new PecGraphics(minX, minY, maxX, maxY, PEC_ICON_WIDTH, PEC_ICON_HEIGHT);

        for (EmbObject object : pattern.asStitchEmbObjects()) {
            graphics.draw(object.getPoints());
        }
        write(graphics.getGraphics());
        graphics.clear();

        int lastcolor = 0;
        for (EmbObject layer : pattern.asStitchEmbObjects()) {
            int currentcolor = layer.getThread().getColor();
            if ((lastcolor != 0) && (currentcolor != lastcolor)) {
                write(graphics.getGraphics());
                graphics.clear();
            }
            graphics.draw(layer.getPoints());
            lastcolor = currentcolor;
        }
        write(graphics.getGraphics());
    }

}
