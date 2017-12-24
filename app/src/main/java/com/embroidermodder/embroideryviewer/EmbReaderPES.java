package com.embroidermodder.embroideryviewer;

import java.io.IOException;

public class EmbReaderPES extends EmbReaderPEC {

    public void readPESHeader() throws IOException {
        String signature = readString(8);
        switch (signature) {
            case "#PES0060":
                readPESHeaderV6();
                break;
            case "#PES0050":
            case "#PES0055":
            case "#PES0056":
                readPESHeaderV5();
                break;
            case "#PES0040":
                readPESHeaderV4();
                break;
            default:
                readPESHeaderDefault();
                break;
        }
    }

    public void readPESHeaderDefault() throws IOException {
        int pecStart = readInt32LE();
        skip(pecStart - readPosition);
    }

    public void readDescriptions() throws IOException {
        int DesignStringLength = readInt8();
        String DesignName = readString(DesignStringLength);
        pattern.name = DesignName;
        int categoryStringLength = readInt8();
        String Category = readString(categoryStringLength);
        pattern.category = Category;
        int authorStringLength = readInt8();
        String Author = readString(authorStringLength);
        pattern.author = Author;
        int keywordsStringLength = readInt8();
        String keywords = readString(keywordsStringLength);
        pattern.keywords = keywords;
        int commentsStringLength = readInt8();
        String Comments = readString(commentsStringLength);
        pattern.comments = Comments;
    }
    public void readPESHeaderV4() throws IOException {        
        int pecStart = readInt32LE();
        skip(4);
        readDescriptions();
        skip(pecStart - readPosition);
    }
    
    public void readPESHeaderV5() throws IOException {        
        int pecStart = readInt32LE();
        skip(4);
        readDescriptions();
        skip(24);//36 v6
        int fromImageStringLength = readInt8();
        skip(fromImageStringLength);
        skip(24);
        int numberOfProgrammableFillPatterns = readInt16LE();
        if (numberOfProgrammableFillPatterns != 0) {
            skip(pecStart - readPosition);
            return;
        }
        int numberOfMotifPatterns = readInt16LE();
        if (numberOfMotifPatterns != 0) {
            skip(pecStart - readPosition);
            return;
        }
        int featherPatternCount = readInt16LE();
        if (featherPatternCount != 0) {
            skip(pecStart - readPosition);
            return;
        }
        int numberOfColors = readInt16LE();
        for (int i = 0; i < numberOfColors; i++) {
            readThread();
        }
        skip(pecStart - readPosition);
    }
    public void readPESHeaderV6() throws IOException {
        int pecStart = readInt32LE();
        skip(4);
        readDescriptions();
        skip(36);
        int fromImageStringLength = readInt8();
        skip(fromImageStringLength);
        skip(24);
        int numberOfProgrammableFillPatterns = readInt16LE();
        if (numberOfProgrammableFillPatterns != 0) {
            skip(pecStart - readPosition);
            return;
        }
        int numberOfMotifPatterns = readInt16LE();
        if (numberOfMotifPatterns != 0) {
            skip(pecStart - readPosition);
            return;
        }
        int featherPatternCount = readInt16LE();
        if (featherPatternCount != 0) {
            skip(pecStart - readPosition);
            return;
        }
        int numberOfColors = readInt16LE();
        for (int i = 0; i < numberOfColors; i++) {
            readThread();
        }
        skip(pecStart - readPosition);
    }

    public void readThread() throws IOException {
        int color_code_length = readInt8();
        String color_code = readString(color_code_length);
        int red = readInt8();
        int green = readInt8();
        int blue = readInt8();
        skip(5);
        int descriptionStringLength = readInt8();
        String description = readString(descriptionStringLength);

        int brandStringLength = readInt8();
        String brand = readString(brandStringLength);

        int threadChartStringLength = readInt8();
        String threadChart = readString(threadChartStringLength);

        int color = (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
        pattern.add(new EmbThread(color, description, color_code, brand, threadChart));
    }

    @Override
    public void read() throws IOException {
        readPESHeader();
        readPec();
    }

    @Override
    public boolean hasColor() {
        return true;
    }

    @Override
    public boolean hasStitches() {
        return true;
    }

}
