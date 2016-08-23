package com.embroidermodder.embroideryviewer;

import java.util.ArrayList;

public class EmbThread {
    private EmbColor _color;
    private String _description;
    private String _catalogNumber;

    public EmbThread() {
    }

    public EmbThread(int color, String description, String catalogNumber) {
        this((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, description, catalogNumber);
    }

    public EmbThread(int red, int green, int blue, String description, String catalogNumber) {
        _color = new EmbColor(red, green, blue);
        _description = description;
        _catalogNumber = catalogNumber;
    }

    public EmbThread(EmbThread toCopy) {
        this.setColor(toCopy.getColor());
        this.setDescription(toCopy.getDescription());
        this.setCatalogNumber(toCopy.getCatalogNumber());
    }

    public EmbColor getColor() {
        return _color;
    }

    public void setColor(EmbColor value) {
        _color = value;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String value) {
        _description = value;
    }

    public String getCatalogNumber() {
        return _catalogNumber;
    }

    public void setCatalogNumber(String value) {
        _catalogNumber = value;
    }

    public int findNearestColorIndex(ArrayList<EmbThread> colorArray) {
        EmbColor color = this.getColor();
        double currentClosestValue = Double.MAX_VALUE;
        int closestIndex = -1;
        int red = color.red & 0xff;
        int green = color.green & 0xff;
        int blue = color.blue & 0xff;
        for (int i = 0; i < colorArray.size(); i++) {
            EmbColor c = colorArray.get(i).getColor();
            double deltaRed = red - c.red & 0xff;
            double deltaBlue = green - c.green & 0xff;
            double deltaGreen = blue - c.blue & 0xff;
            double dist = (deltaRed * deltaRed) + (deltaBlue * deltaBlue) + (deltaGreen * deltaGreen);
            if (dist <= currentClosestValue) {
                currentClosestValue = dist;
                closestIndex = i;
            }
        }
        return closestIndex;
    }
}