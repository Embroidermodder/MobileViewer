package com.embroidermodder.embroideryviewer;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FormatInfTest {
    @Test
    public void formatInf_ReadCorrect() throws Exception {
        byte[] fileContents = new byte[] {0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x08,
                0x00, 0x00, 0x00, 0x11, // remaining bytes
                0x00, 0x00, 0x00, 0x01, // number of colors
                // repeated threads from here
                0x00, 0x00,
                0x00, 0x00,
                0x01, 0x02, 0x03, // R, G, B
                0x00, 0x00, // needle number 0
                0x00, 0x00, // two zero length strings
        };
        DataInputStream anyInputStream = new DataInputStream(new ByteArrayInputStream(fileContents));
        FormatInf inf = new FormatInf();
        EmbPattern p = new EmbPattern();
        inf.read(p, anyInputStream);
        ArrayList<EmbThread> threadList = p.getThreadList();
        assertEquals(threadList.size(), 1);
        assertEquals(threadList.get(0).getColor().red, 1);
    }

    @Test
    public void formatInf_ReadCorrect2() throws Exception {
        byte[] fileContents = new byte[] {0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x08,
                0x00, 0x00, 0x00, 0x1A,
                0x00, 0x00, 0x00, 0x02,

                0x00, 0x00,
                0x00, 0x00,
                0x02, 0x03, 0x04,
                0x00, 0x00,
                0x00, 0x00,

                0x00, 0x00,
                0x00, 0x00,
                0x03, 0x04, 0x05,
                0x00, 0x00,
                0x00, 0x00,
        };
        DataInputStream anyInputStream = new DataInputStream(new ByteArrayInputStream(fileContents));
        FormatInf inf = new FormatInf();
        EmbPattern p = new EmbPattern();
        inf.read(p, anyInputStream);
        ArrayList<EmbThread> threadList = p.getThreadList();
        assertEquals(threadList.size(), 2);
        assertEquals(threadList.get(0).getColor().red, 2);
        assertEquals(threadList.get(1).getColor().red, 3);
    }
}