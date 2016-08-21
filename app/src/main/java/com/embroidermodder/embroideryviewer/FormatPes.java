package com.embroidermodder.embroideryviewer;

import android.graphics.RectF;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class FormatPes implements IFormat.Reader {

    private final ArrayList<EmbThread> _threads;
    public FormatPes(){
        _threads = FormatPec.getThreads();
    }
    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return true;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        try {
            stream.skip(8);
            byte fullInt[] = new byte[4];
            stream.read(fullInt);
            int pecStart = ((fullInt[2] & 0xFF) << 16) + ((fullInt[1] & 0xFF) << 8) + (fullInt[0] & 0xFF);
            stream.skip(pecStart + 36);
            int numColors = (stream.read() & 0xFF) + 1;
            for (int x = 0; x < numColors; x++) {
                int index = stream.read();
                pattern.addThread(FormatPec.getThreadByIndex(index % 65));
            }
            stream.skip(484 - numColors - 1);
            FormatPec.readPecStitches(pattern, stream);
        } catch (IOException ex) {
        }
    }

    private void pesWriteSewSegSection(EmbPattern pattern, OutputStream file) throws IOException {
        int count;
        int colorCode;
        int stitchType;
        int blockCount = 0;
        int colorCount = 0;
        int newColorCode;
        int colorInfoIndex = 0;
        int i;
        RectF bounds = pattern.calculateBoundingBox();
        EmbThread previousThread = pattern.getStitchBlocks().get(0).getThread();
        for(StitchBlock stitchBlock : pattern.getStitchBlocks()) {
            if(previousThread != stitchBlock.getThread()) {
                colorCount++;
            }
            blockCount++;
        }

        blockCount = pattern.getStitchBlocks().size();
        BinaryHelper.writeShort(file, (short)blockCount); /* block count */
        BinaryHelper.writeShort(file, 0xFFFF);
        BinaryHelper.writeShort(file, 0x00);

        BinaryHelper.writeShort(file, 0x07); /* string length */
        file.write("CSewSeg".getBytes());

        short[] colorInfo = new short[colorCount * 2];
        colorCode = -1;
        blockCount = 0;
        StitchBlock lastBlock = pattern.getStitchBlocks().get(pattern.getStitchBlocks().size()- 1);
        for(StitchBlock stitchBlock : pattern.getStitchBlocks()){
            newColorCode = stitchBlock.getThread().findNearestColorIndex(_threads);
            if(newColorCode != colorCode) {
                colorInfo[colorInfoIndex++] = (short)blockCount;
                colorInfo[colorInfoIndex++] = (short)newColorCode;
                colorCode = newColorCode;
            }
//            if((flag & IFormat.JUMP) == IFormat.JUMP) {
//                stitchType = 1;
//            } else {
//                stitchType = 0;
//            }
            stitchType = 0;
            count = stitchBlock.count();
            BinaryHelper.writeShort(file, (short)stitchType); /* 1 for jump, 0 for normal */
            BinaryHelper.writeShort(file, (short)colorCode); /* color code */
            BinaryHelper.writeShort(file, (short)count); /* stitches in block */
            for (int j = 0; j < stitchBlock.count(); j++) {
                BinaryHelper.writeShort(file, (short)(stitchBlock.getX(j) - bounds.left));
                BinaryHelper.writeShort(file, (short)(stitchBlock.getY(j) + bounds.top));
            }
            if(lastBlock != stitchBlock) {
                BinaryHelper.writeShort(file, 0x8003);
            }
            blockCount++;
        }
        BinaryHelper.writeShort(file, (short)colorCount);
        for(i = 0; i < colorCount; i++) {
            BinaryHelper.writeShort(file, colorInfo[i * 2]);
            BinaryHelper.writeShort(file, colorInfo[i * 2 + 1]);
        }
        file.write(0x00);
        file.write(0x00);
        file.write(0x00);
        file.write(0x00);
    }

    private void pesWriteEmbOneSection(EmbPattern pattern, OutputStream file) throws IOException {
        int i;
        int hoopHeight = 1800, hoopWidth = 1300;
        RectF bounds;
        BinaryHelper.writeShort(file, 0x07); /* string length */
        file.write("CEmbOne".getBytes());
        bounds = pattern.calculateBoundingBox();

        BinaryHelper.writeShort(file, 0);
        BinaryHelper.writeShort(file, 0);
        BinaryHelper.writeShort(file, 0);
        BinaryHelper.writeShort(file, 0);
        BinaryHelper.writeShort(file, 0);
        BinaryHelper.writeShort(file, 0);
        BinaryHelper.writeShort(file, 0);
        BinaryHelper.writeShort(file, 0);

    /* AffineTransform */
        BinaryHelper.writeInt32(file, Float.floatToIntBits(1.0f));
        BinaryHelper.writeInt32(file, Float.floatToIntBits(0.0f));
        BinaryHelper.writeInt32(file, Float.floatToIntBits(0.0f));
        BinaryHelper.writeInt32(file, Float.floatToIntBits(1.0f));
        BinaryHelper.writeInt32(file, Float.floatToIntBits((bounds.width() - hoopWidth) / 2));
        BinaryHelper.writeInt32(file, Float.floatToIntBits((bounds.height() + hoopHeight) / 2));

        BinaryHelper.writeShort(file, 1);
        BinaryHelper.writeShort(file, 0); /* Translate X */
        BinaryHelper.writeShort(file, 0); /* Translate Y */
        BinaryHelper.writeShort(file, (short) bounds.width());
        BinaryHelper.writeShort(file, (short) bounds.height());

        for (i = 0; i < 8; i++) {
            file.write(0);
        }

    /*WriteSubObjects(br, pes, SubBlocks); */
    }

    public void write(EmbPattern pattern, FileOutputStream file) {//String fileName)
        try {
            file.write("#PES0001".getBytes());

            ByteArrayOutputStream tempArray = new ByteArrayOutputStream();
            pesWriteEmbOneSection(pattern, tempArray);
            pesWriteSewSegSection(pattern, tempArray);
            tempArray.close();
            byte[] tempData = tempArray.toByteArray();

            int pecLocation = tempData.length + 22; // check this is the right value

            file.write(pecLocation & 0xFF);
            file.write((pecLocation >> 8) & 0xFF);
            file.write((pecLocation >> 16) & 0xFF);
            file.write((pecLocation >> 24) & 0xFF);

            BinaryHelper.writeShort(file, 0x01);
            BinaryHelper.writeShort(file, 0x01);

            BinaryHelper.writeShort(file, 0x01);
            BinaryHelper.writeShort(file, 0xFFFF); /* command */
            BinaryHelper.writeShort(file, 0x00); /* unknown */

            file.write(tempData);
            FormatPec.writePecStitches(pattern, file, "PESFILENAME");
        } catch (IOException e) {

        }
    }
}
