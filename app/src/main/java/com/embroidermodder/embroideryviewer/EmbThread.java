package com.embroidermodder.embroideryviewer;

import java.util.ArrayList;

public class EmbThread {
    private EmbColor _color;
    private String _description;
    private String _catalogNumber;

    public EmbThread() {
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
        int red = color.red;
        int green = color.green;
        int blue = color.blue;
        for (int i = 0; i < colorArray.size(); i++) {
            EmbColor c = colorArray.get(i).getColor();
            double deltaRed = red - c.red;
            double deltaBlue = green - c.green;
            double deltaGreen = blue - c.blue;
            double dist = (deltaRed * deltaRed) + (deltaBlue * deltaBlue) + (deltaGreen * deltaGreen); //closest squared color distance = still closest colordistance.
            if (dist <= currentClosestValue) {
                currentClosestValue = dist;
                closestIndex = i;
            }
        }
        return closestIndex;
    }
}