package com.embroidermodder.embroideryviewer;

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
}