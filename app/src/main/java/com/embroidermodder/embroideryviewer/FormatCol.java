package com.embroidermodder.embroideryviewer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class FormatCol implements IFormat.Reader {

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return false;
    }

    public Pattern read(DataInputStream stream) {
        Pattern p = new Pattern();
        int numberOfColors = 0;
        try {
            BufferedReader d = new BufferedReader(new InputStreamReader(stream));
            Scanner scanner = new Scanner(d.readLine());
            numberOfColors = scanner.nextInt();
            for (int i = 0; i < numberOfColors; i++) {
                int num, blue, green, red;
                String line = d.readLine();
                if (line == null || line.isEmpty()) {
                    i--;
                    continue;
                }
                Scanner lineScanner = new Scanner(line);
                lineScanner.useDelimiter(",");
                num = lineScanner.nextInt();
                blue = lineScanner.nextInt();
                green = lineScanner.nextInt();
                red = lineScanner.nextInt();
                EmbThread t = new EmbThread(red, green, blue, "", "");
                p.addThread(t);
            }
        } catch (IOException ex) {
        }
        return p;
    }
}