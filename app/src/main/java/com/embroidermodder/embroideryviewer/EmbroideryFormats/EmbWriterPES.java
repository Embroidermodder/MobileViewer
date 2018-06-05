package com.embroidermodder.embroideryviewer.EmbroideryFormats;

import com.embroidermodder.embroideryviewer.geom.DataPoints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmbWriterPES extends EmbWriterPEC {
    static final int PESVERSION = 2;
    static final String EMB_ONE = "CEmbOne";
    static final String EMB_SEG = "CSewSeg";

    private float version = 1;
    public EmbThread[] chart;

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public String getSignature() {
        float v = getVersion();
        if (v <= 1) {
            return "#PES0001";
        }
        return "#PES0060";
    }

    private void writePosition(float left, float top, float right, float bottom, float x, float y) throws IOException {
        writeInt16LE((short) (x - left));
        writeInt16LE((short) (y - bottom));
    }

    private void writeSectionEnd() throws IOException {
        writeInt16LE(0x8003);
    }

    public void writePesString8(String string) throws IOException {
        if (string == null) {
            writeInt8(0);
            return;
        }
        if (string.length() > 255) {
            string = string.substring(0, 255);
        }
        writeInt8(string.length());
        write(string.getBytes());
    }

    public void writePesString16(String string) throws IOException {
        writeInt16LE(string.length());
        write(string.getBytes());
    }


    @Override
    public void write() throws IOException {
        if ((settings & PESVERSION) == 0) {
            this.version = 1;
        } else {
            this.version = 6;
        }

        if (this.version == 1) {
            chart = EmbThreadPec.getThreadSet();
        } else {
            List<EmbThread> threads = pattern.getUniqueThreadList();
            chart = new EmbThread[threads.size()];
            chart = threads.toArray(chart);
        }
        float pattern_left = pattern.getStitches().getMinX();
        float pattern_top = pattern.getStitches().getMinY();
        float pattern_right = pattern.getStitches().getMaxX();
        float pattern_bottom = pattern.getStitches().getMaxY();

        float width = pattern_right - pattern_left;
        float height = pattern_bottom - pattern_top;
        float cx = ((pattern_left + pattern_right) / 2);
        float cy = ((pattern_top + pattern_bottom) / 2);
        pattern.translate(-cx, -cy); //move center to origin.
        pattern_left -= cx;
        pattern_right -= cx;
        pattern_top -= cy;
        pattern_bottom -= cy;

        ByteArrayOutputStream headerPesBlocks = new ByteArrayOutputStream();
        if (pattern.getStitches().isEmpty()) {
            push(headerPesBlocks);
            writePesheader(0, (int) width, (int) height);
            writeInt16LE(0x0000);
            writeInt16LE(0x0000);
            pop();
        } else {
            push(headerPesBlocks);
            writePesheader(1, (int) width, (int) height);
            writeInt16LE(0xFFFF);
            writeInt16LE(0x0000);
            writePesBlocks(pattern_left, pattern_top, pattern_right, pattern_bottom);
            pop();
        }
        String signature = getSignature();
        write(signature);
        int pecLocation = signature.length() + headerPesBlocks.size() + 4; //4 is the size of the pecLocationwrite.
        writeInt32LE(pecLocation);
        write(headerPesBlocks.toByteArray());
        writePecStitches(getName());
    }

    public void writePesheader(int distinctBlockObjects, int width, int height) throws IOException {
        float v = getVersion();
        if (v <= 1) {
            writePesheaderV1(distinctBlockObjects);
        } else {
            writePesheaderV6(distinctBlockObjects, width, height);
        }
    }

    public void writePesheaderV1(int distinctBlockObjects) throws IOException {
        writeInt16LE(0x01); //1 is scale to fit.
        writeInt16LE(0x01); // 0 = 100x100 else 130x180 or above
        writeInt16LE(distinctBlockObjects);//number of distinct blocks
    }

    public void writePesheaderV6(int distinctBlockObjects, int width, int height) throws IOException {
        writeInt16LE(0x01); // 0 = 100x100 else 130x180 or above
        writeInt8(0x30);
        writeInt8(0x32);
        String name = "untitled";
        if ((pattern.name != null) && (pattern.name.length() > 0)) {
            name = pattern.name;
        }
        writePesString8(name);
        writePesString8(pattern.category);
        writePesString8(pattern.author);
        writePesString8(pattern.keywords);
        writePesString8(pattern.comments);

        writeInt16LE(0);//boolean optimizeHoopChange = (readInt16LE() == 1);

        writeInt16LE(0);//boolean designPageIsCustom = (readInt16LE() == 1);

        writeInt16LE(0x64); //hoopwidth
        writeInt16LE(0x64); //hoopheight
        writeInt16LE(0);// 1 means "UseExistingDesignArea" 0 means "Design Page Area"        

        writeInt16LE(0xC8);//int designWidth = readInt16LE();
        writeInt16LE(0xC8);//int designHeight = readInt16LE();
        writeInt16LE(0x64);//int designPageSectionWidth = readInt16LE();
        writeInt16LE(0x64);//int designPageSectionHeight = readInt16LE();
        writeInt16LE(0x64);//int p6 = readInt16LE(); // 100

        writeInt16LE(0x07);//int designPageBackgroundColor = readInt16LE();
        writeInt16LE(0x13);//int designPageForegroundColor = readInt16LE();
        writeInt16LE(0x01); //boolean ShowGrid = (readInt16LE() == 1);
        writeInt16LE(0x01);//boolean WithAxes = (readInt16LE() == 1);
        writeInt16LE(0x00);//boolean SnapToGrid = (readInt16LE() == 1);
        writeInt16LE(100);//int GridInterval = readInt16LE();

        writeInt16LE(0x01);//int p9 = readInt16LE(); // curves?
        writeInt16LE(0x00);//boolean OptimizeEntryExitPoints = (readInt16LE() == 1);

        writeInt8(0);//int fromImageStringLength = readInt8();
        //String FromImageFilename = readString(fromImageStringLength);

        writeInt32LE(Float.floatToIntBits(1f));
        writeInt32LE(Float.floatToIntBits(0f));
        writeInt32LE(Float.floatToIntBits(0f));
        writeInt32LE(Float.floatToIntBits(1f));
        writeInt32LE(Float.floatToIntBits(0f));
        writeInt32LE(Float.floatToIntBits(0f));
        writeInt16LE(0);//int numberOfProgrammableFillPatterns = readInt16LE();
        writeInt16LE(0);//int numberOfMotifPatterns = readInt16LE();
        writeInt16LE(0);//int featherPatternCount = readInt16LE();
        //pattern.validateThreadList();
        ArrayList<EmbThread> threads = pattern.threadlist;
        writeInt16LE(threads.size());//int numberOfColors = readInt16LE();
        for (EmbThread t : threads) {
            write(t);
        }
        writeInt16LE(distinctBlockObjects);//number of distinct blocks
    }

    public void write(EmbThread thread) throws IOException {
        writePesString8(thread.getCatalogNumber());
        writeInt8(thread.getRed());
        writeInt8(thread.getGreen());
        writeInt8(thread.getBlue());
        writeInt8(0); //unknown
        writeInt32LE(0xA);
        writePesString8(thread.getDescription());
        writePesString8(thread.getBrand());
        writePesString8(thread.getChart());
    }

    public void writePesBlocks(final float pattern_left, final float pattern_top, final float pattern_right, final float pattern_bottom) throws IOException {
        if (pattern.getStitches().isEmpty()) return;

        writePesString16(EMB_ONE);
        float height = pattern_bottom - pattern_top;
        float width = pattern_right - pattern_left;
        int hoopHeight = 1800, hoopWidth = 1300;
        writeInt16LE(0);  //writeInt16LE((int) bounds.left);
        writeInt16LE(0);  //writeInt16LE((int) bounds.top);
        writeInt16LE(0);  //writeInt16LE((int) bounds.right);
        writeInt16LE(0);  //writeInt16LE((int) bounds.bottom);
        writeInt16LE(0);  //writeInt16LE((int) bounds.left);
        writeInt16LE(0);  //writeInt16LE((int) bounds.top);
        writeInt16LE(0);  //writeInt16LE((int) bounds.right);
        writeInt16LE(0);  //writeInt16LE((int) bounds.bottom);
        float transX = 0;
        float transY = 0;
        transX += 350f;
        transY += 100f + height;
        transX += hoopWidth / 2;
        transY += hoopHeight / 2;
        transX += -width / 2;
        transY += -height / 2;
        writeInt32LE(Float.floatToIntBits(1f));
        writeInt32LE(Float.floatToIntBits(0f));
        writeInt32LE(Float.floatToIntBits(0f));
        writeInt32LE(Float.floatToIntBits(1f));
        writeInt32LE(Float.floatToIntBits(transX));
        writeInt32LE(Float.floatToIntBits(transY));
        writeInt16LE(1);
        writeInt16LE(0);
        writeInt16LE(0);
        writeInt16LE((short) width);
        writeInt16LE((short) height);
        writeInt32LE(0);
        writeInt32LE(0);

        final ByteArrayOutputStream embseg = new ByteArrayOutputStream();
        push(embseg);
        writeInt16LE(0xFFFF);
        writeInt16LE(0x0000); //FFFF0000 means more blocks exist.
        writePesString16(EMB_SEG);

        final ByteArrayOutputStream colorlog = new ByteArrayOutputStream();

        int section = 0;
        int flag = -1;

        int mode;
        DataPoints points = pattern.getStitches();
        int colorIndex = 0;
        int colorCode = 0;
        EmbThread currentThread;
        push(colorlog);
        if (pattern.getThreadCount() != 0) {
            currentThread = pattern.getThread(colorIndex++);
            colorCode = EmbThread.findNearestIndex(currentThread.color, chart);
            writeInt16LE(section);
            writeInt16LE(colorCode);
        }
        float lastx = 0, lasty = 0;
        pop();
        for (int i = 0, ie = points.size(); i < ie; i++) {
            mode = points.getData(i) & EmbPattern.COMMAND_MASK;
            if ((mode != EmbPattern.END) && (flag != -1)) {
                writeSectionEnd();
            }
            final ByteArrayOutputStream seg = new ByteArrayOutputStream();
            push(seg);
            switch (mode) {
                case EmbPattern.JUMP:
                    writePosition(pattern_left, pattern_top, pattern_right, pattern_bottom, lastx, lasty);
                    while (i < ie && mode == (points.getData(i) & EmbPattern.COMMAND_MASK)) {
                        i++;
                    }
                    i--;
                    writePosition(pattern_left, pattern_top, pattern_right, pattern_bottom, points.getX(i), points.getY(i));
                    flag = 1;
                    break;
                case EmbPattern.COLOR_CHANGE:
                    currentThread = pattern.getThread(colorIndex++);
                    colorCode = EmbThread.findNearestIndex(currentThread.color, chart);
                    push(colorlog);
                    writeInt16LE(section);
                    writeInt16LE(colorCode);
                    pop();
                    //writePosition(pattern_left, pattern_top, pattern_right, pattern_bottom, points.getX(i - 2), points.getY(i - 2));
                    //writePosition(pattern_left, pattern_top, pattern_right, pattern_bottom, points.getX(i - 2), points.getY(i - 2));
                    flag = 1;
                    break;
                case EmbPattern.STITCH:
                    while (i < ie && mode == (points.getData(i) & EmbPattern.COMMAND_MASK)) {
                        lastx = points.getX(i);
                        lasty = points.getY(i);
                        writePosition(pattern_left, pattern_top, pattern_right, pattern_bottom, lastx, lasty);
                        i++;
                    }
                    i--;
                    flag = 0;
                    break;
            }
            pop();
            if (seg.size() != 0) {
                writeInt16LE(flag);
                writeInt16LE((short) colorCode);
                writeInt16LE((short) seg.size() / 4);
                write(seg.toByteArray());
                section++;
            } else {
                flag = -1;
            }
        }
        pop();
        writeInt16LE(section);
        write(embseg.toByteArray());
        int count = colorlog.size() / 4;
        writeInt16LE(count);
        write(colorlog.toByteArray());
        writeInt16LE(0x0000);
        writeInt16LE(0x0000);

        if (version == 6) {
            writeInt32LE(0);
            writeInt32LE(0);
            for (int i = 0; i < count; i++) {
                writeInt32LE(i);
                writeInt32LE(0);
            }
        }
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
