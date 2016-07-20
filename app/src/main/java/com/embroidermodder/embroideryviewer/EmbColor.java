package com.embroidermodder.embroideryviewer;

import android.graphics.Color;

import java.util.Random;

public class EmbColor {
    public final int red, green, blue;

    public EmbColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static EmbColor Random() {
        Random r = new Random(System.currentTimeMillis());
        return new EmbColor(r.nextInt(255), r.nextInt(255), r.nextInt(255));
    }

    public int getAndroidColor() {
        return Color.argb(255, this.red & 0xff, this.green & 0xff, this.blue & 0xff);
    }
}
