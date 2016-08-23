package com.embroidermodder.embroideryviewer;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David Olsen on 12/22/2015.
 * Derived from proprietary code, 8/23/2016.
 * Released under EmbroiderModder/MobileView licensing. 8/23/2016.
 * <p/>
 * <p/>
 * The path parser class parses strings into doubles, floats, ints, longs, and strings
 * from strings which match the W3 specification for SVG path grammar in 8.3.9 of SVG 1.1
 * https://www.w3.org/TR/SVG/paths.html#PathDataBNF
 * <p/>
 * However the commands accepted are dynamically set during construction.
 * <p/>
 * While the utility for SVG is obvious, the class itself allows one to quickly serialize
 * and unserialize data, and quickly and effectively parse predetermined command data,
 * which is what SVG is doing.
 * <p/>
 * Due to its use in floats/doubles command "e/E" should not be used.
 * <p/>
 * Strings, must either be surrounded with forwardslashes (/) or not contain any command data.
 * Strings are not needed for SVG paths.
 * <p/>
 * <p/>
 */

public class PathParser {
    private final Pattern commandParser;
    private static final Pattern doubleParser = Pattern.compile("(?:[-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?)");
    private static final Pattern longParser = Pattern.compile("[-+]?[0-9]+");
    private static final Pattern stringParser = Pattern.compile("[^/]+");


    public PathParser(String commands) {
        commandParser = Pattern.compile("(?i)[" + commands + "](?:[^/" + commands + "]|/[^/]*/)*");
    }

    public interface ParseCommand {
        boolean matched(String s, Values values);
    }

    public class Values {
        protected CharSequence ops;
        protected int start;

        public int getOps(long[] v, int count) {
            if (start >= ops.length()) return 0;
            Matcher matcher = longParser.matcher(ops);
            for (int i = 0; i < count; i++) {
                if (!matcher.find(start)) {
                    count = i;
                    break;
                }
                v[i] = Long.valueOf(matcher.group());
                start = matcher.end();
            }
            return count;
        }

        public int getOps(int[] v, int count) {
            if (start >= ops.length()) return 0;
            Matcher matcher = longParser.matcher(ops);
            for (int i = 0; i < count; i++) {
                if (!matcher.find(start)) {
                    count = i;
                    break;
                }
                v[i] = Integer.valueOf(matcher.group());
                start = matcher.end();
            }
            return count;
        }

        public int getOps(double[] v, int count) {
            if (start >= ops.length()) return 0;
            Matcher matcher = doubleParser.matcher(ops);
            for (int i = 0; i < count; i++) {
                if (!matcher.find(start)) {
                    count = i;
                    break;
                }
                v[i] = Double.valueOf(matcher.group());
                start = matcher.end();
            }
            return count;
        }

        public int getOps(float[] v, int count) {
            if (start >= ops.length()) return 0;
            Matcher matcher = doubleParser.matcher(ops);
            for (int i = 0; i < count; i++) {
                if (!matcher.find(start)) {
                    count = i;
                    break;
                }
                v[i] = Float.valueOf(matcher.group());
                start = matcher.end();
            }
            return count;
        }


        public Long getLong() {
            if (start >= ops.length()) return null;
            Matcher matcher = longParser.matcher(ops);
            if (!matcher.find(start)) return null;
            start = matcher.end();
            return Long.valueOf(matcher.group());
        }

        public Integer getInteger() {
            if (start >= ops.length()) return null;
            Matcher matcher = longParser.matcher(ops);
            if (!matcher.find(start)) return null;
            start = matcher.end();
            return Integer.valueOf(matcher.group());
        }

        public Double getDouble() {
            if (start >= ops.length()) return null;
            Matcher matcher = doubleParser.matcher(ops);
            if (!matcher.find(start)) return null;
            start = matcher.end();
            return Double.valueOf(matcher.group());
        }

        public Float getFloat() {
            if (start >= ops.length()) return null;
            Matcher matcher = doubleParser.matcher(ops);
            if (!matcher.find(start)) return null;
            start = matcher.end();
            return Float.valueOf(matcher.group());
        }

        public String getString() {
            if (start >= ops.length()) return null;
            Matcher matcher = stringParser.matcher(ops);
            if (!matcher.find(start)) return null;
            start = matcher.end();
            return matcher.group();
        }

        public long getLong(long defaultValue) {
            Long v = getLong();
            if (v == null) return defaultValue;
            return v;
        }

        public int getInt(int defaultValue) {
            Integer v = getInteger();
            if (v == null) return defaultValue;
            return v;
        }

        public double getDouble(double defaultValue) {
            Double v = getDouble();
            if (v == null) return defaultValue;
            return v;
        }

        public float getFloat(float defaultValue) {
            Float v = getFloat();
            if (v == null) return defaultValue;
            return v;
        }

        public Iterable<? extends Long> asLongStream() {
            return new NumberStream<Long>() {
                @Override
                protected Long getNumber() {
                    return getLong();
                }
            };
        }

        public Iterable<? extends Integer> asIntegerStream() {
            return new NumberStream<Integer>() {
                @Override
                protected Integer getNumber() {
                    return getInteger();
                }
            };
        }

        public Iterable<? extends Double> asDoubleStream() {
            return new NumberStream<Double>() {
                @Override
                protected Double getNumber() {
                    return getDouble();
                }
            };
        }

        public Iterable<? extends Float> asFloatStream() {
            return new NumberStream<Float>() {
                @Override
                protected Float getNumber() {
                    return getFloat();
                }
            };
        }

        public void reset() {
            start = 0;
        }

        private abstract class NumberStream<T extends Number> implements Iterable<T> {
            abstract protected T getNumber();

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    T value;
                    boolean computed = false;

                    private void compute() {
                        computed = true;
                        value = getNumber();
                    }

                    @Override
                    public boolean hasNext() {
                        if (!computed) compute();
                        return value != null;
                    }

                    @Override
                    public T next() {
                        T v = value;
                        value = null;
                        computed = false;
                        return v;
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        }
    }

    public boolean parse(String path, ParseCommand parser) {
        return (path != null) && parse(path, parser, new Values());
    }

    public boolean parse(String path, ParseCommand parser, Values values) {
        if (path == null) return false;
        Matcher match = commandParser.matcher(path);
        while (match.find()) {
            int start = match.start();
            values.reset();
            values.ops = path.subSequence(start + 1, match.end());
            String command = path.substring(start, start + 1);
            if (parser.matched(command, values)) {
                return true;
            }
        }
        return false;
    }
}
