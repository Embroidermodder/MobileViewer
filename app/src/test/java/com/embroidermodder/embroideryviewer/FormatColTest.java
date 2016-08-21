package com.embroidermodder.embroideryviewer;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class FormatColTest {

    @Test
    public void formatInf_ReadCorrectThread() throws Exception {
        String fileContents = "1\r0,1,2,3\n\r";
        DataInputStream anyInputStream = new DataInputStream(new ByteArrayInputStream(fileContents.getBytes()));
        FormatCol col = new FormatCol();
        EmbPattern p = new EmbPattern();
        col.read(p, anyInputStream);
        ArrayList<EmbThread> threadList = p.getThreadList();
        assertEquals(threadList.size(), 1);
        assertEquals(threadList.get(0).getColor().red, 3);
    }

    @Test
    public void formatCol_ReadCorrect2Thread() throws Exception {
        String fileContents = "2\r0,1,2,3\n\r1,2,3,4\n\r";
        DataInputStream anyInputStream = new DataInputStream(new ByteArrayInputStream(fileContents.getBytes()));
        FormatCol col = new FormatCol();
        EmbPattern p = new EmbPattern();
        col.read(p, anyInputStream);
        ArrayList<EmbThread> threadList = p.getThreadList();
        assertEquals(threadList.size(), 2);
        assertEquals(threadList.get(0).getColor().red, 3);
        assertEquals(threadList.get(1).getColor().red, 4);
    }
}
