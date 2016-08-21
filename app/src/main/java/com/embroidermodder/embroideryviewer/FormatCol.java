package com.embroidermodder.embroideryviewer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class FormatCol implements IFormat.Reader {

    public boolean hasColor() {
        return true;
    }

    public boolean hasStitches() {
        return false;
    }

    public void read(EmbPattern pattern, InputStream stream) {
        int numberOfColors;
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
                pattern.addThread(t);
            }
        } catch (IOException ex) {
        }
    }
}