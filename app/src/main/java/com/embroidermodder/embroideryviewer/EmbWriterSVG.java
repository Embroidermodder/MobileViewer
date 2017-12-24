package com.embroidermodder.embroideryviewer;

import android.graphics.RectF;
import android.util.Xml;

import com.embroidermodder.embroideryviewer.geom.Points;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_DATA;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_FILL;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_HEIGHT;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_STROKE;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_VERSION;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_VIEWBOX;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_WIDTH;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_XMLNS;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_XMLNS_EV;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.ATTR_XMLNS_LINK;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.NAME_PATH;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.NAME_SVG;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.VALUE_NONE;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.VALUE_SVG_VERSION;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.VALUE_XLINK;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.VALUE_XMLNS;
import static com.embroidermodder.embroideryviewer.EmbReaderSVG.VALUE_XMLNS_EV;

public class EmbWriterSVG extends EmbWriter {

    @Override
    public void write() throws IOException {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        xmlSerializer.setOutput(stream, "UTF-8");

        xmlSerializer.startDocument("UTF-8", true);

        xmlSerializer.startTag("", NAME_SVG);

        xmlSerializer.attribute("", ATTR_VERSION, VALUE_SVG_VERSION);
        xmlSerializer.attribute("", ATTR_XMLNS, VALUE_XMLNS);
        xmlSerializer.attribute("", ATTR_XMLNS_LINK, VALUE_XLINK);
        xmlSerializer.attribute("", ATTR_XMLNS_EV, VALUE_XMLNS_EV);
        float minX = pattern.getStitches().getMinX();
        float minY = pattern.getStitches().getMinY();
        float maxX = pattern.getStitches().getMaxX();
        float maxY = pattern.getStitches().getMaxY();
        RectF bounds = new RectF(minX,minY,maxX,maxY);
        //RectF bounds = pattern.calculateBoundingBox();
        xmlSerializer.attribute("", ATTR_WIDTH, Float.toString(bounds.width()));
        xmlSerializer.attribute("", ATTR_HEIGHT, Float.toString(bounds.height()));
        xmlSerializer.attribute("", ATTR_VIEWBOX, bounds.left + " " + bounds.top + " " + bounds.width() + " " + bounds.height());

        StringBuilder d = new StringBuilder();
        for (EmbObject object : pattern.asStitchEmbObjects()) {
            Points points = object.getPoints();
            xmlSerializer.startTag("", NAME_PATH);
            double lastx = Double.NEGATIVE_INFINITY;
            double lasty = Double.NEGATIVE_INFINITY;

            double px, py;
            d.setLength(0); //sets stringBuilder empty, and reuses.
            d.append("M");
            for (int i = 0, s = points.size(); i < s; i++) {
                px = points.getX(i);
                py = points.getY(i);
                if ((px != lastx) || (py != lasty)) {
                    d.append(" ").append((float) px).append(",").append((float) py);
                }
                lastx = px;
                lasty = py;
            }

            xmlSerializer.attribute("", ATTR_DATA, d.toString());
            xmlSerializer.attribute("", ATTR_FILL, VALUE_NONE);
            xmlSerializer.attribute("", ATTR_STROKE, object.getThread().getHexColor());
            xmlSerializer.endTag("", NAME_PATH);
        }
        xmlSerializer.endTag("", NAME_SVG);

        xmlSerializer.endDocument();
    }

    public boolean hasColor() {
        return false;
    }

    public boolean hasStitches() {
        return true;
    }

}
