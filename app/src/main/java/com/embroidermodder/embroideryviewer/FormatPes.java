package com.embroidermodder.embroideryviewer;

import android.graphics.RectF;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class FormatPes implements IFormat.Reader, IFormat.Writer {

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
    try {
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
 //   for(StitchBlock stitchBlock : pattern.getStitchBlocks()) {
  //      if(previousThread != stitchBlock.getThread()) {
   //         colorCount++;
  //      }
  //      blockCount++;
  //  }

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
 //  for(StitchBlock stitchBlock : pattern.getStitchBlocks()){
      //  newColorCode = stitchBlock.getThread().findNearestColorIndex(_threads);
     //  if(newColorCode != colorCode) {
        //   colorInfo[colorInfoIndex++] = (short)blockCount;
       //     colorInfo[colorInfoIndex++] = (short)newColorCode;
       //     colorCode = newColorCode;
      //   }
// if((flag & IFormat.JUMP) == IFormat.JUMP) {
// stitchType = 1;
// } else {
// stitchType = 0;
// }
// stitchType = 0;
// count = stitchBlock.count();
// BinaryHelper.writeShort(file, (short)stitchType); /* 1 for jump, 0 for normal /
// BinaryHelper.writeShort(file, (short)colorCode); / color code /
// BinaryHelper.writeShort(file, (short)count); / stitches in block */

     //  for (EmbPattern.stitch stitches : pattern.getstitches()) {



       //     BinaryHelper.writeShort(file, (short)(stitches.x ));//- bounds.left
        //    BinaryHelper.writeShort(file, (short)(stitches.y));// + bounds.top
      //  }

  //      if(lastBlock != stitchBlock) {
 //           BinaryHelper.writeShort(file, 0x8003);
 //       }
//        blockCount++;
//    }
    BinaryHelper.writeShort(file, (short)colorCount);
   for(i = 0; i < colorCount; i++) {
        BinaryHelper.writeShort(file, colorInfo[i * 2]);
        BinaryHelper.writeShort(file, colorInfo[i * 2 + 1]);
    }
   file.write(0x00);
    file.write(0x00);
   file.write(0x00);
    file.write(0x00);

} catch (IOException e) {

}
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
private static void pecEncode(OutputStream file, EmbPattern p) throws IOException {
    double thisX = 0.0;
    double thisY = 0.0;
    byte stopCode = 2;
    EmbThread previousThread = p.getStitchBlocks().get(0).getThread();

    for(StitchBlock stitchBlock : p.getStitchBlocks()) {
        if(previousThread != stitchBlock.getThread()) {
            pecEncodeStop(file, stopCode);
            if (stopCode == (byte) 2) {
                stopCode = (byte) 1;
            } else {
                stopCode = (byte) 2;
            }
        }
        int flags = IFormat.TRIM;
        long deltaX, deltaY;
        for(int i = 0; i < stitchBlock.size(); i++) {
            deltaX = Math.round(stitchBlock.getX(i) - thisX);
            deltaY = Math.round(stitchBlock.getY(i) - thisY);
            thisX += (double) deltaX;
            thisY += (double) deltaY;

            if (deltaX < 63 && deltaX > -64 && deltaY < 63 && deltaY > -64 && ((flags & (IFormat.JUMP | IFormat.TRIM)) == 0)) {
                file.write((byte)((deltaX < 0) ? (deltaX + 0x80) : deltaX));
                file.write((byte)((deltaY < 0) ? (deltaY + 0x80) : deltaY));
            } else {
                encodeJump(file, (int)deltaX, flags);
                encodeJump(file, (int)deltaY, flags);
            }
            flags = IFormat.NORMAL;
        }
        previousThread = stitchBlock.getThread();
    }
    file.write(0xFF);
}

public void write(EmbPattern pattern, OutputStream file) {//String fileName)
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
     writePecStitches(pattern, file, "PESFILENAME");
    } catch (IOException e) {

    }
}

private static void pecEncodeStop(OutputStream file, byte val) throws IOException {
    file.write(0xFE);
    file.write(0xB0);
    file.write(val);
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

public static ArrayList<EmbThread> getThreads(){
    ArrayList<EmbThread> threads = new ArrayList<>();
    for(int i = 0; i < 64; i++){
        threads.add(getThreadByIndex(i));
    }
    return threads;
}



public static void writePecStitches(EmbPattern pattern, OutputStream file, String fileName) throws IOException {
    try {
        byte image[][] = new byte[38][48];
        int i, currentThreadCount, graphicsOffsetValue, height, width;
        double xFactor, yFactor;
        int dotPos = fileName.lastIndexOf(".");
        int start = fileName.lastIndexOf("/");
        file.write("LA:".getBytes());
        String internalFilename = fileName.substring(Math.max(0,start), Math.max(0, dotPos));
        if(internalFilename.length() > 16){
            internalFilename = internalFilename.substring(0, 16);
        }
        file.write(internalFilename.getBytes());
        for(i = 0; i < (16-internalFilename.length()); i++) {
            file.write(0x20);
        }
        file.write(0x0D);
        for(i = 0; i < 12; i++) {
            file.write(0x20);
        }
        file.write(0xFF);
        file.write(0x00);
        file.write(0x06);
        file.write(0x26);

        for(i = 0; i < 12; i++) {
            file.write(0x20);
        }
        currentThreadCount = 0;


        ArrayList<EmbThread> pecThreads = getThreads();
        EmbThread previousThread = null;
        for(StitchBlock stitchBlock : pattern.getStitchBlocks()){
            if(stitchBlock.getThread() != previousThread) {
                currentThreadCount++;
            }
            previousThread = stitchBlock.getThread();
        }
        file.write((byte)(currentThreadCount-1));
        previousThread = null;
        for(StitchBlock stitchBlock : pattern.getStitchBlocks()){
            if(stitchBlock.getThread() != previousThread) {
                file.write((byte)stitchBlock.getThread().findNearestColorIndex(pecThreads));
            }
            previousThread = stitchBlock.getThread();
        }
        for(i = 0; i < (0x1CF - currentThreadCount); i++) {
            file.write(0x20);
        }
        file.write(0x00);
        file.write(0x00);

        ByteArrayOutputStream tempArray = new ByteArrayOutputStream();
        pecEncode(tempArray, pattern);

        graphicsOffsetValue = tempArray.size()  + 17;
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
        BinaryHelper.writeShort(file, (short)width);
/* write 2 byte y size */
        BinaryHelper.writeShort(file, (short)height);

/* Write 4 miscellaneous int16's */
        BinaryHelper.writeShort(file, (short)0x1E0);
        BinaryHelper.writeShort(file, (short)0x1B0);

        BinaryHelper.writeShortBE(file, (0x9000 | -Math.round(bounds.left)));
        BinaryHelper.writeShortBE(file, (0x9000 | -Math.round(bounds.top)));
        file.write(tempArray.toByteArray());

/* Writing all colors */
        clearImage(image);
        yFactor = 32.0 / height;
        xFactor = 42.0 / width;
        //   for(StitchBlock stitchBlock : pattern.getStitchBlocks()){
        //     for(int j = 0; j < stitchBlock.count(); j++){
        //         int x = (int)Math.round((stitchBlock.getX(j) - bounds.left) * xFactor) + 3;
        //         int y = (int)Math.round((stitchBlock.getY(j) - bounds.top) * yFactor) + 3;
        //         image[y][x] = 1;
        //     }
        // }
        writeImage(file, image);

/* Writing each individual color */
        clearImage(image);
        previousThread = pattern.getStitchBlocks().get(0).getThread();
        for(StitchBlock stitchBlock : pattern.getStitchBlocks()){
            if(previousThread != stitchBlock.getThread()) {
                writeImage(file, image);
                clearImage(image);
            }
            //  for(int j = 0; j < stitchBlock.count(); j++) {
            //      int x = (int) Math.round((stitchBlock.getX(j) - bounds.left) * xFactor) + 3;
            //      int y = (int) Math.round((stitchBlock.getY(j) - bounds.top) * yFactor) + 3;
            //     image[y][x] = 1;
            //  }
            previousThread = stitchBlock.getThread();
        }
        writeImage(file, image);

    } catch (IOException ex) {
    }
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
private static void clearImage(byte[][] image){
    for (byte[] row: image) {
        Arrays.fill(row, (byte)0);
    }
}
}