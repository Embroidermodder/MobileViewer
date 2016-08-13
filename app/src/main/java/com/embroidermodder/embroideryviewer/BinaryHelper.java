package com.embroidermodder.embroideryviewer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class BinaryHelper {
    public static int readInt32LE(DataInputStream stream) throws IOException {
        byte fullInt[] = new byte[4];
        stream.read(fullInt);
        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8) + ((fullInt[2] & 0xFF) << 16) + ((fullInt[3] & 0xFF) << 24);
    }

    public static int readInt16LE(DataInputStream stream) throws IOException {
        byte fullInt[] = new byte[2];
        stream.read(fullInt);
        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8);
    }

    public static String readString(DataInputStream stream, int maxLength) throws IOException {
        ArrayList<Byte> charList = new ArrayList<>();
        int i = 0;
        while (i < maxLength) {
            byte value = stream.readByte();
            if (value == '\0') {
                break;
            }
            charList.add(value);
            i++;
        }
        byte[] result = new byte[charList.size()];
        for (i = 0; i < charList.size(); i++) {
            result[i] = charList.get(i);
        }
        return new String(result, "UTF-8");
    }

    public static void writeShort(OutputStream stream, int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
    }

    public static void writeInt32(OutputStream stream, int value) throws IOException {
        stream.write(value & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 24) & 0xFF);
    }

    public static void writeShortBE(OutputStream stream, int value) throws IOException {
        stream.write((value >> 8) & 0xFF);
        stream.write(value & 0xFF);
    }
}
