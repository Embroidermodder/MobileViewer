package com.embroidermodder.embroideryviewer;

import android.graphics.Color;
import android.graphics.RectF;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class FormatSvg implements IFormat.Writer, IFormat.Reader {

    public static final String MIME = "image/svg+xml";
    public static final String EXT = "svg";

    private static final String CHARACTERS = "value";
    private static final String ELEMENT = "element";
    public static final String NAME_SVG = "svg";
    public static final String NAME_PATH = "path";
    public static final String NAME_POLYLINE = "polyline";
    public static final String ATTR_DATA = "d";
    public static final String ATTR_STROKE = "stroke";
    public static final String ATTR_STYLE = "style";
    public static final String ATTR_FILL = "fill";
    public static final String ATTR_WIDTH = "width";
    public static final String ATTR_HEIGHT = "height";
    public static final String ATTR_POINTS = "points";
    public static final String ATTR_VIEWBOX = "viewBox";
    public static final String VALUE_NONE = "none";
    public static final String ATTR_VERSION = "version";
    public static final String VALUE_SVG_VERSION = "1.1";
    public static final String ATTR_XMLNS = "xmlns";
    public static final String VALUE_XMLNS = "http://www.w3.org/2000/svg";
    public static final String ATTR_XMLNS_LINK = "xmlns:xlink";
    public static final String VALUE_XLINK = "http://www.w3.org/1999/xlink";
    public static final String ATTR_XMLNS_EV = "xmlns:ev";
    public static final String VALUE_XMLNS_EV = "http://www.w3.org/2001/xml-events";

    private static final String SVG_PATH_COMMANDS = "csqtamlzhv";
    private static Pattern hexPattern = Pattern.compile("#([0-9A-Fa-f]+)");
    private static Pattern rgbPattern = Pattern.compile("rgb\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
    private static Pattern rgbpPattern = Pattern.compile("rgb\\(\\s*(\\d+)%\\s*,\\s*(\\d+)%\\s*,\\s*(\\d+)%\\s*\\)");

    public void write(EmbPattern pattern, OutputStream out) {
        try {
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setOutput(out, "UTF-8");

            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag("", NAME_SVG);

            xmlSerializer.attribute("", ATTR_VERSION, VALUE_SVG_VERSION);
            xmlSerializer.attribute("", ATTR_XMLNS, VALUE_XMLNS);
            xmlSerializer.attribute("", ATTR_XMLNS_LINK, VALUE_XLINK);
            xmlSerializer.attribute("", ATTR_XMLNS_EV, VALUE_XMLNS_EV);
            RectF bounds = pattern.calculateBoundingBox();
            xmlSerializer.attribute("", ATTR_WIDTH, Float.toString(bounds.width()));
            xmlSerializer.attribute("", ATTR_HEIGHT, Float.toString(bounds.height()));
            xmlSerializer.attribute("", ATTR_VIEWBOX, bounds.left + " " + bounds.top + " " + bounds.width() + " " + bounds.height());

            StringBuilder d = new StringBuilder();
            for (StitchBlock sb : pattern.getStitchBlocks()) {
                if (sb.isEmpty()) continue;

                xmlSerializer.startTag("", NAME_PATH);
                double lastx = Double.NEGATIVE_INFINITY;
                double lasty = Double.NEGATIVE_INFINITY;

                double px, py;
                d.setLength(0); //sets stringBuilder empty, and reuses.
                d.append("M");
                for (int i = 0, s = sb.size(); i < s; i++) {
                    px = sb.getX(i);
                    py = sb.getY(i);
                    if ((px != lastx) || (py != lasty)) {
                        d.append(" ").append((float) px).append(",").append((float) py);
                    }
                    lastx = px;
                    lasty = py;
                }

                xmlSerializer.attribute("", ATTR_DATA, d.toString());
                /* alternatively:
                xmlSerializer.startTag("", NAME_POLYLINE);
                xmlSerializer.attribute("", ATTR_POINTS, d.toString()); //without the M start in path converter.
                 */
                xmlSerializer.attribute("", ATTR_FILL, VALUE_NONE);
                xmlSerializer.attribute("", ATTR_STROKE, asHexColor(sb.getThread().getColor().getAndroidColor()));
                xmlSerializer.endTag("", NAME_PATH);
            }
            xmlSerializer.endTag("", NAME_SVG);

            xmlSerializer.endDocument();
            out.close();
        } catch (IOException e) {

        }
    }

    private String asHexColor(int color) {
        return String.format("#%06x", 0xFFFFFF & color);
    }

    @Override
    public void read(final EmbPattern pattern, InputStream stream) {
        final PathParser parser = new PathParser(SVG_PATH_COMMANDS);
        final PathParser.ParseCommand command = new PathParser.ParseCommand() {
            @Override
            public boolean matched(String s, PathParser.Values values) {
                Float a;
                Float b;
                switch (s) {
                    case "m":
                        a = values.getFloat();
                        b = values.getFloat();
                        if (!Thread.interrupted() && b != null) {
                            pattern.addStitchRel(0, 0, IFormat.END, false);
                            pattern.addStitchRel(0, 0, IFormat.TRIM, false);
                            pattern.addStitchRel(a, b, IFormat.JUMP, false);//I have no clue.
                        }
                        a = values.getFloat();
                        b = values.getFloat();
                        while (!Thread.interrupted() && b != null) {
                            pattern.addStitchRel(a, b, IFormat.NORMAL, false);
                            a = values.getFloat();
                            b = values.getFloat();
                        }
                        break;
                    case "l":
                        a = values.getFloat();
                        b = values.getFloat();
                        while (!Thread.interrupted() && b != null) {
                            pattern.addStitchRel(a, b, IFormat.NORMAL, false);
                            a = values.getFloat();
                            b = values.getFloat();
                        }
                        break;
                    case "M":
                        a = values.getFloat();
                        b = values.getFloat();
                        if (!Thread.interrupted() && b != null) {
                            pattern.addStitchRel(0, 0, IFormat.END, false);
                            pattern.addStitchRel(0, 0, IFormat.TRIM, false);
                            pattern.addStitchAbs(a, b, IFormat.JUMP, false); //I have no clue.
                        }
                        a = values.getFloat();
                        b = values.getFloat();
                        while (!Thread.interrupted() && b != null) {
                            pattern.addStitchAbs(a, b, IFormat.NORMAL, false);
                            a = values.getFloat();
                            b = values.getFloat();
                        }
                        break;
                    case "L":
                        a = values.getFloat();
                        b = values.getFloat();
                        while (!Thread.interrupted() && b != null) {
                            pattern.addStitchAbs(a, b, IFormat.NORMAL, false);
                            a = values.getFloat();
                            b = values.getFloat();
                        }
                        break;
                    case "v":
                        a = values.getFloat();
                        while (!Thread.interrupted() && a != null) {
                            pattern.addStitchRel(0, a, IFormat.NORMAL, false);
                            a = values.getFloat();
                        }
                        break;
                    case "h":
                        a = values.getFloat();
                        while (!Thread.interrupted() && a != null) {
                            pattern.addStitchRel(a, 0, IFormat.NORMAL, false);
                            a = values.getFloat();
                        }
                        break;
                    case "V":
                        a = values.getFloat();
                        while (!Thread.interrupted() && a != null) {
                            //adds a stitch relative with regard to X, but absolute with regard to Y.
                            //Same location as the last X, but at exactly position Y.
                            a = values.getFloat();
                        }
                        break;
                    case "H":
                        a = values.getFloat();
                        while (!Thread.interrupted() && a != null) {
                            //adds a stitch relative with regard to Y, but absolute with regard to X.
                            a = values.getFloat();
                        }
                        break;
                }
                return Thread.interrupted();
            }
        };
        elementParser(stream, new Receiver() {
            @Override
            public void path(String path, int strokeColor) {
                pattern.addThread(new EmbThread(strokeColor, null, ""));
                parser.parse(path, command);
            }

            @Override
            public void start() {
            }

            @Override
            public void finished() {
            }

            @Override
            public void error() {
            }
        });
    }


    public void elementParser(InputStream file, Receiver receiver) {
        SVGHandler svgHandler = new SVGHandler(receiver);
        try {
            receiver.start();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(file, svgHandler);
            receiver.finished();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            receiver.error();
        }
    }

    private class SVGHandler extends DefaultHandler {
        Receiver receiver;
        private Stack<HashMap<String, String>> tags;

        public SVGHandler(Receiver receiver) {
            tags = new Stack<>();
            tags.push(new HashMap<String, String>());
            this.receiver = receiver;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException {
            if (Thread.interrupted()) throw new SAXException();
            HashMap<String, String> context = new HashMap<>(tags.peek());
            tags.push(context);

            int attributelen = attribs.getLength();

            for (int i = 0; i < attributelen; i++) {
                String attributeQName = attribs.getQName(i);
                String attributeValue;
                switch (attributeQName) {
                    case ATTR_STYLE:
                        attributeValue = attribs.getValue(i);
                        String[] styles = attributeValue.split(";");

                        for (String v : styles) {
                            String[] vs = v.split(":");
                            if (vs.length == 2) {
                                context.put(vs[0], vs[1]);
                            }
                        }
                        break;
                    case ATTR_DATA:
                    case ATTR_FILL:
                    case ATTR_HEIGHT:
                    case ATTR_POINTS:
                    case ATTR_STROKE:
                    case ATTR_VERSION:
                    case ATTR_VIEWBOX:
                    case ATTR_WIDTH:
                        attributeValue = attribs.getValue(i);
                        context.put(attributeQName, attributeValue);
                        break;
                }
            }
            context.put(ELEMENT, qName);

            switch (qName.toLowerCase()) {
                case NAME_PATH:
                    String path = context.get(ATTR_DATA);
                    if (path != null) {
                        receiver.path(path, parseColor(context.get(ATTR_STROKE)));
                    }
                    break;
                case NAME_POLYLINE:
                    String points = context.get(ATTR_POINTS);
                    if (points == null) {
                        break;
                    }
                    receiver.path("M" + points, parseColor(context.get(ATTR_STROKE)));
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            String s = new String(ch, start, length);
            HashMap<String, String> context = tags.peek();
            context.put(CHARACTERS, s);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            tags.pop();
        }

    }

    public static int parseColor(String string) {
        if (string == null) {
            return 0;
        }
        if (string.equalsIgnoreCase(VALUE_NONE)) {
            return 0;
        }
        return parseHex(string);
    }


    public static int parseHex(String color) {
        Matcher m;
        m = hexPattern.matcher(color);
        if (m.find()) {
            int a = 255, r = 0, g = 0, b = 0;
            String rgb = m.group(1);
            switch (rgb.length()) {
                case 3:
                    r = Integer.parseInt(rgb.substring(0, 1) + rgb.substring(0, 1), 16);
                    g = Integer.parseInt(rgb.substring(1, 2) + rgb.substring(1, 2), 16);
                    b = Integer.parseInt(rgb.substring(2, 3) + rgb.substring(2, 3), 16);
                    break;
                case 5: //error color;
                case 4:
                    a = Integer.parseInt(rgb.substring(0, 1) + rgb.substring(0, 1), 16);
                    r = Integer.parseInt(rgb.substring(1, 2) + rgb.substring(1, 2), 16);
                    g = Integer.parseInt(rgb.substring(2, 3) + rgb.substring(2, 3), 16);
                    b = Integer.parseInt(rgb.substring(3, 4) + rgb.substring(3, 4), 16);
                    break;
                case 7: //error color;
                    break;
                case 6:
                    r = Integer.parseInt(rgb.substring(0, 2), 16);
                    g = Integer.parseInt(rgb.substring(2, 4), 16);
                    b = Integer.parseInt(rgb.substring(4, 6), 16);
                    break;
                case 8:
                    a = Integer.parseInt(rgb.substring(0, 2), 16);
                    r = Integer.parseInt(rgb.substring(2, 4), 16);
                    g = Integer.parseInt(rgb.substring(4, 6), 16);
                    b = Integer.parseInt(rgb.substring(6, 8), 16);
                    break;

                default:
            }
            return Color.argb(a, r, g, b);
        }
        m = rgbPattern.matcher(color);
        if (m.find()) {
            String r = m.group(1);
            String g = m.group(2);
            String b = m.group(3);
            int rp = Integer.decode(r);
            int gp = Integer.decode(g);
            int bp = Integer.decode(b);
            return newColor(rp, gp, bp);
        }
        m = rgbpPattern.matcher(color);
        if (m.find()) {
            String r = m.group(1);
            String g = m.group(2);
            String b = m.group(3);
            int rp = Integer.decode(r);
            int gp = Integer.decode(g);
            int bp = Integer.decode(b);
            rp = (rp * 255) / 100;
            gp = (gp * 255) / 100;
            bp = (bp * 255) / 100;
            return newColor(rp, gp, bp);
        }

        return svgColor(color);
    }

    public static int newColor(int r, int g, int b) {
        return ((r & 255) << 16) | ((g & 255) << 8) | ((b & 255));
    }

    public static int svgColor(String color) {
        // https://www.w3.org/TR/SVG/types.html#ColorKeywords
        if (color == null) {
            return 0;
        }
        switch (color.toLowerCase()) {
            case VALUE_NONE:
                return -1;
            case "aliceblue":
                return newColor(240, 248, 255);
            case "antiquewhite":
                return newColor(250, 235, 215);
            case "aqua":
                return newColor(0, 255, 255);
            case "aquamarine":
                return newColor(127, 255, 212);
            case "azure":
                return newColor(240, 255, 255);
            case "beige":
                return newColor(245, 245, 220);
            case "bisque":
                return newColor(255, 228, 196);
            case "black":
                return newColor(0, 0, 0);
            case "blanchedalmond":
                return newColor(255, 235, 205);
            case "blue":
                return newColor(0, 0, 255);
            case "blueviolet":
                return newColor(138, 43, 226);
            case "brown":
                return newColor(165, 42, 42);
            case "burlywood":
                return newColor(222, 184, 135);
            case "cadetblue":
                return newColor(95, 158, 160);
            case "chartreuse":
                return newColor(127, 255, 0);
            case "chocolate":
                return newColor(210, 105, 30);
            case "coral":
                return newColor(255, 127, 80);
            case "cornflowerblue":
                return newColor(100, 149, 237);
            case "cornsilk":
                return newColor(255, 248, 220);
            case "crimson":
                return newColor(220, 20, 60);
            case "cyan":
                return newColor(0, 255, 255);
            case "darkblue":
                return newColor(0, 0, 139);
            case "darkcyan":
                return newColor(0, 139, 139);
            case "darkgoldenrod":
                return newColor(184, 134, 11);
            case "darkgray":
                return newColor(169, 169, 169);
            case "darkgreen":
                return newColor(0, 100, 0);
            case "darkgrey":
                return newColor(169, 169, 169);
            case "darkkhaki":
                return newColor(189, 183, 107);
            case "darkmagenta":
                return newColor(139, 0, 139);
            case "darkolivegreen":
                return newColor(85, 107, 47);
            case "darkorange":
                return newColor(255, 140, 0);
            case "darkorchid":
                return newColor(153, 50, 204);
            case "darkred":
                return newColor(139, 0, 0);
            case "darksalmon":
                return newColor(233, 150, 122);
            case "darkseagreen":
                return newColor(143, 188, 143);
            case "darkslateblue":
                return newColor(72, 61, 139);
            case "darkslategray":
                return newColor(47, 79, 79);
            case "darkslategrey":
                return newColor(47, 79, 79);
            case "darkturquoise":
                return newColor(0, 206, 209);
            case "darkviolet":
                return newColor(148, 0, 211);
            case "deeppink":
                return newColor(255, 20, 147);
            case "deepskyblue":
                return newColor(0, 191, 255);
            case "dimgray":
                return newColor(105, 105, 105);
            case "dimgrey":
                return newColor(105, 105, 105);
            case "dodgerblue":
                return newColor(30, 144, 255);
            case "firebrick":
                return newColor(178, 34, 34);
            case "floralwhite":
                return newColor(255, 250, 240);
            case "forestgreen":
                return newColor(34, 139, 34);
            case "fuchsia":
                return newColor(255, 0, 255);
            case "gainsboro":
                return newColor(220, 220, 220);
            case "ghostwhite":
                return newColor(248, 248, 255);
            case "gold":
                return newColor(255, 215, 0);
            case "goldenrod":
                return newColor(218, 165, 32);
            case "gray":
                return newColor(128, 128, 128);
            case "grey":
                return newColor(128, 128, 128);
            case "green":
                return newColor(0, 128, 0);
            case "greenyellow":
                return newColor(173, 255, 47);
            case "honeydew":
                return newColor(240, 255, 240);
            case "hotpink":
                return newColor(255, 105, 180);
            case "indianred":
                return newColor(205, 92, 92);
            case "indigo":
                return newColor(75, 0, 130);
            case "ivory":
                return newColor(255, 255, 240);
            case "khaki":
                return newColor(240, 230, 140);
            case "lavender":
                return newColor(230, 230, 250);
            case "lavenderblush":
                return newColor(255, 240, 245);
            case "lawngreen":
                return newColor(124, 252, 0);
            case "lemonchiffon":
                return newColor(255, 250, 205);
            case "lightblue":
                return newColor(173, 216, 230);
            case "lightcoral":
                return newColor(240, 128, 128);
            case "lightcyan":
                return newColor(224, 255, 255);
            case "lightgoldenrodyellow":
                return newColor(250, 250, 210);
            case "lightgray":
                return newColor(211, 211, 211);
            case "lightgreen":
                return newColor(144, 238, 144);
            case "lightgrey":
                return newColor(211, 211, 211);
            case "lightpink":
                return newColor(255, 182, 193);
            case "lightsalmon":
                return newColor(255, 160, 122);
            case "lightseagreen":
                return newColor(32, 178, 170);
            case "lightskyblue":
                return newColor(135, 206, 250);
            case "lightslategray":
                return newColor(119, 136, 153);
            case "lightslategrey":
                return newColor(119, 136, 153);
            case "lightsteelblue":
                return newColor(176, 196, 222);
            case "lightyellow":
                return newColor(255, 255, 224);
            case "lime":
                return newColor(0, 255, 0);
            case "limegreen":
                return newColor(50, 205, 50);
            case "linen":
                return newColor(250, 240, 230);
            case "magenta":
                return newColor(255, 0, 255);
            case "maroon":
                return newColor(128, 0, 0);
            case "mediumaquamarine":
                return newColor(102, 205, 170);
            case "mediumblue":
                return newColor(0, 0, 205);
            case "mediumorchid":
                return newColor(186, 85, 211);
            case "mediumpurple":
                return newColor(147, 112, 219);
            case "mediumseagreen":
                return newColor(60, 179, 113);
            case "mediumslateblue":
                return newColor(123, 104, 238);
            case "mediumspringgreen":
                return newColor(0, 250, 154);
            case "mediumturquoise":
                return newColor(72, 209, 204);
            case "mediumvioletred":
                return newColor(199, 21, 133);
            case "midnightblue":
                return newColor(25, 25, 112);
            case "mintcream":
                return newColor(245, 255, 250);
            case "mistyrose":
                return newColor(255, 228, 225);
            case "moccasin":
                return newColor(255, 228, 181);
            case "navajowhite":
                return newColor(255, 222, 173);
            case "navy":
                return newColor(0, 0, 128);
            case "oldlace":
                return newColor(253, 245, 230);
            case "olive":
                return newColor(128, 128, 0);
            case "olivedrab":
                return newColor(107, 142, 35);
            case "orange":
                return newColor(255, 165, 0);
            case "orangered":
                return newColor(255, 69, 0);
            case "orchid":
                return newColor(218, 112, 214);
            case "palegoldenrod":
                return newColor(238, 232, 170);
            case "palegreen":
                return newColor(152, 251, 152);
            case "paleturquoise":
                return newColor(175, 238, 238);
            case "palevioletred":
                return newColor(219, 112, 147);
            case "papayawhip":
                return newColor(255, 239, 213);
            case "peachpuff":
                return newColor(255, 218, 185);
            case "peru":
                return newColor(205, 133, 63);
            case "pink":
                return newColor(255, 192, 203);
            case "plum":
                return newColor(221, 160, 221);
            case "powderblue":
                return newColor(176, 224, 230);
            case "purple":
                return newColor(128, 0, 128);
            case "red":
                return newColor(255, 0, 0);
            case "rosybrown":
                return newColor(188, 143, 143);
            case "royalblue":
                return newColor(65, 105, 225);
            case "saddlebrown":
                return newColor(139, 69, 19);
            case "salmon":
                return newColor(250, 128, 114);
            case "sandybrown":
                return newColor(244, 164, 96);
            case "seagreen":
                return newColor(46, 139, 87);
            case "seashell":
                return newColor(255, 245, 238);
            case "sienna":
                return newColor(160, 82, 45);
            case "silver":
                return newColor(192, 192, 192);
            case "skyblue":
                return newColor(135, 206, 235);
            case "slateblue":
                return newColor(106, 90, 205);
            case "slategray":
                return newColor(112, 128, 144);
            case "slategrey":
                return newColor(112, 128, 144);
            case "snow":
                return newColor(255, 250, 250);
            case "springgreen":
                return newColor(0, 255, 127);
            case "steelblue":
                return newColor(70, 130, 180);
            case "tan":
                return newColor(210, 180, 140);
            case "teal":
                return newColor(0, 128, 128);
            case "thistle":
                return newColor(216, 191, 216);
            case "tomato":
                return newColor(255, 99, 71);
            case "turquoise":
                return newColor(64, 224, 208);
            case "violet":
                return newColor(238, 130, 238);
            case "wheat":
                return newColor(245, 222, 179);
            case "white":
                return newColor(255, 255, 255);
            case "whitesmoke":
                return newColor(245, 245, 245);
            case "yellow":
                return newColor(255, 255, 0);
            case "yellowgreen":
                return newColor(154, 205, 50);
        }
        return -1;
    }


    public interface Receiver {
        void path(String path, int strokeColor);

        void start();

        void finished();

        void error();
    }
}
