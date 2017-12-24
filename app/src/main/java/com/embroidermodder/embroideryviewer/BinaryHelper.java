//package com.embroidermodder.embroideryviewer;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//
//public class BinaryHelper {
//    public static int readInt32LE(InputStream stream) throws IOException {
//        byte fullInt[] = new byte[4];
//        stream.read(fullInt);
//        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8) + ((fullInt[2] & 0xFF) << 16) + ((fullInt[3] & 0xFF) << 24);
//    }
//
//    public static int readInt32BE(InputStream stream) throws IOException {
//        byte fullInt[] = new byte[4];
//        stream.read(fullInt);
//        return (fullInt[3] & 0xFF) + ((fullInt[2] & 0xFF) << 8) + ((fullInt[1] & 0xFF) << 16) + ((fullInt[0] & 0xFF) << 24);
//    }
//
//    public static int readInt16LE(InputStream stream) throws IOException {
//        byte fullInt[] = new byte[2];
//        stream.read(fullInt);
//        return (fullInt[0] & 0xFF) + ((fullInt[1] & 0xFF) << 8);
//    }
//
//    public static int readInt16BE(InputStream stream) throws IOException {
//        byte fullInt[] = new byte[2];
//        stream.read(fullInt);
//        return (fullInt[1] & 0xFF) + ((fullInt[0] & 0xFF) << 8);
//    }
//
//    public static int readInt8(InputStream stream) throws IOException {
//        byte fullInt[] = new byte[1];
//        if(stream.read(fullInt) <= 0) throw new IOException();
//        return (fullInt[0] & 0xFF);
//    }
//
//    public static void writeShort(OutputStream stream, int value) throws IOException {
//        stream.write(value & 0xFF);
//        stream.write((value >> 8) & 0xFF);
//    }
//
//    public static void writeShortBE(OutputStream stream, int value) throws IOException {
//        stream.write((value >> 8) & 0xFF);
//        stream.write(value & 0xFF);
//    }
//
//    public static String readString(InputStream stream, int maxLength) throws IOException {
//        ArrayList<Byte> charList = new ArrayList<>();
//        int i = 0;
//        while (i < maxLength) {
//            int value = stream.read();
//            if (value == '\0') {
//                break;
//            }
//            charList.add((byte)value);
//            i++;
//        }
//        byte[] result = new byte[charList.size()];
//        for (i = 0; i < charList.size(); i++) {
//            result[i] = charList.get(i);
//        }
//        return new String(result, "UTF-8");
//    }
//
//    public static void writeInt32(OutputStream stream, int value) throws IOException {
//        stream.write(value & 0xFF);
//        stream.write((value >> 8) & 0xFF);
//        stream.write((value >> 16) & 0xFF);
//        stream.write((value >> 24) & 0xFF);
//    }
//
//    public static void writeInt32BE(OutputStream stream, int value) throws IOException {
//        stream.write((value >> 24) & 0xFF);
//        stream.write((value >> 16) & 0xFF);
//        stream.write((value >> 8) & 0xFF);
//        stream.write(value & 0xFF);
//    }
//}
