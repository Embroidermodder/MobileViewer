package com.embroidermodder.embroideryviewer;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.embroidermodder.embroideryviewer.EmbroideryFormats.EmbObject;
import com.embroidermodder.embroideryviewer.EmbroideryFormats.EmmThread;
import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Points;
import com.embroidermodder.embroideryviewer.geom.PointsIndexRange;

import org.embroideryio.embroideryio.EmbPattern;
import org.embroideryio.embroideryio.EmbThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EmmPattern {
    public static final int NO_COMMAND = -1;
    public static final int STITCH = 0;
    public static final int JUMP = 1;
    public static final int TRIM = 2;
    public static final int STOP = 3;
    public static final int END = 4;
    public static final int COLOR_CHANGE = 5;
    public static final int INIT = 6;
    public static final int TIE_OFF = 0xA0;
    public static final int TIE_ON = 0xA1;
    public static final int FRAME_OFF = 0xB0;
    public static final int FRAME_ON = 0xB1;


    public static final int STITCH_NEW_LOCATION = 0xF0;
    public static final int STITCH_NEW_COLOR = 0xF1;
    public static final int STITCH_FINAL_LOCATION = 0xF2;
    public static final int STITCH_FINAL_COLOR = 0xF3;

    public static final int COMMAND_MASK = 0xFF;


    public static final String PROP_FILENAME = "filename";
    public static final String PROP_NAME = "name";
    public static final String PROP_CATEGORY = "category";
    public static final String PROP_AUTHOR = "author";
    public static final String PROP_KEYWORDS = "keywords";
    public static final String PROP_COMMENTS = "comments";

    public static final int NOTIFY_CORRECT_LENGTH = 1; //these are just provisional.
    public static final int NOTIFY_ROTATED = 2;
    public static final int NOTIFY_FLIP = 3;
    public static final int NOTIFY_THREADS_FIX = 4;
    public static final int NOTIFY_METADATA = 5;
    public static final int NOTIFY_STITCH_CHANGE = 6;
    public static final int NOTIFY_LOADED = 7;
    public static final int NOTIFY_CHANGE = 8;
    public static final int NOTIFY_THREAD_COLOR = 9;

    public ArrayList<EmmThread> threadlist;
    public String filename;
    public String name;
    public String category;
    public String author;
    public String keywords;
    public String comments;

    private float _previousX = 0;
    private float _previousY = 0;


    private DataPoints stitches = new DataPoints();

    private ArrayList<Listener> listeners;

    public EmmPattern() {
        threadlist = new ArrayList<>();
    }

    public EmmPattern(EmmPattern p) {
        this.filename = p.filename;
        this.name = p.name;
        this.category = p.category;
        this.author = p.author;
        this.keywords = p.keywords;
        this.comments = p.comments;
        this.threadlist = new ArrayList<>(p.threadlist.size());
        for (EmmThread thread : p.getThreadlist()) {
            addThread(new EmmThread(thread));
        }
        this.stitches = new DataPoints(p.stitches);
    }

    public void setPattern(EmmPattern p) {
        this.filename = p.filename;
        this.name = p.name;
        this.category = p.category;
        this.author = p.author;
        this.keywords = p.keywords;
        this.comments = p.comments;
        this.threadlist.clear();
        for (EmmThread thread : p.getThreadlist()) {
            addThread(new EmmThread(thread));
        }
        this.stitches = new DataPoints(p.stitches);
    }

    public DataPoints getStitches() {
        return stitches;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String value) {
        filename = value;
        notifyChange(NOTIFY_METADATA);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyChange(NOTIFY_METADATA);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        notifyChange(NOTIFY_METADATA);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        notifyChange(NOTIFY_METADATA);
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
        notifyChange(NOTIFY_METADATA);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
        notifyChange(NOTIFY_METADATA);
    }

    void add(EmmThread embroideryThread) {
        threadlist.add(embroideryThread);
    }

    public ArrayList<EmmThread> getThreadlist() {
        return threadlist;
    }

    public void addThread(EmmThread thread) {
        threadlist.add(thread);
    }

    public EmmThread getThread(int index) {
        return threadlist.get(index);
    }

    public EmmThread getRandomThread() {
        return new EmmThread(0xFF000000 | (int) (Math.random() * 0xFFFFFF), "Random");
    }

    public EmmThread getThreadOrFiller(int index) {
        if (threadlist.size() <= index) {
            return getRandomThread();
        }
        return threadlist.get(index);
    }

    public EmmThread getLastThread() {
        if (threadlist == null) return null;
        if (threadlist.isEmpty()) return null;
        return threadlist.get(threadlist.size() - 1);
    }

    public int getThreadCount() {
        if (threadlist == null) return 0;
        return threadlist.size();
    }

    public boolean isEmpty() {
        if (stitches == null) return true;
        if (stitches.isEmpty()) {
            return threadlist.isEmpty();
        }
        return false;
    }

    public HashMap<String, String> getMetadata() {
        HashMap<String, String> metadata = new HashMap<>();
        if (filename != null) metadata.put(PROP_FILENAME, filename);
        if (name != null) metadata.put(PROP_NAME, name);
        if (category != null) metadata.put(PROP_CATEGORY, category);
        if (author != null) metadata.put(PROP_AUTHOR, author);
        if (keywords != null) metadata.put(PROP_KEYWORDS, keywords);
        if (comments != null) metadata.put(PROP_COMMENTS, comments);
        return metadata;
    }

    public void setMetadata(Map<String, String> map) {
        filename = map.get(PROP_FILENAME);
        name = map.get(PROP_NAME);
        category = map.get(PROP_CATEGORY);
        author = map.get(PROP_AUTHOR);
        keywords = map.get(PROP_KEYWORDS);
        comments = map.get(PROP_COMMENTS);
    }

    public Iterable<EmbObject> asStitchEmbObjects() {
        return new Iterable<EmbObject>() {
            @NonNull
            @Override
            public Iterator<EmbObject> iterator() {
                return new Iterator<EmbObject>() {
                    int threadIndex = 0;
                    EmmThread thread = null;

                    final PointsIndexRange<DataPoints> points = new PointsIndexRange<>(stitches, 0, 0);

                    final EmbObject object = new EmbObject() {
                        @Override
                        public EmmThread getThread() {
                            if (thread != null) return thread;
                            if (threadlist.size() <= threadIndex) {
                                thread = getRandomThread();
                            } else {
                                thread = threadlist.get(threadIndex);
                            }
                            return thread;
                        }

                        @Override
                        public Points getPoints() {
                            return points;
                        }

                        @Override
                        public int getType() {
                            return 0;
                        }
                    };

                    final int NOT_CALCULATED = 0;
                    final int HAS_NEXT = 1;
                    final int ENDED = 2;

                    int mode = NOT_CALCULATED;

                    private boolean iterateStart() {
                        int start = points.getStart();
                        int end = stitches.size();
                        while (start < end) {
                            int data = stitches.getData(start);
                            if ((data & COMMAND_MASK) == COLOR_CHANGE) {
                                threadIndex++;
                                thread = null;
                            }
                            if ((data & COMMAND_MASK) == STITCH) {
                                points.setStart(start);
                                return false;
                            }
                            start++;
                        }
                        points.setStart(start);
                        return true;
                    }

                    private boolean iterateLength() {
                        int length = 0;
                        for (int i = points.getStart(), ie = stitches.size(); i < ie; i++, length++) {
                            int data = stitches.getData(i);
                            if ((data & COMMAND_MASK) != STITCH) {
                                points.setLength(length);
                                return false;
                            }
                        }
                        points.setLength(length);
                        return true;
                    }

                    private void calculate() {
                        points.setStart(points.getStart() + points.getLength());
                        points.setLength(0);
                        if (points.getStart() >= stitches.size()) {
                            mode = ENDED;
                            return;
                        }
                        if (iterateStart()) {
                            mode = ENDED;
                            return;
                        }
                        iterateLength();
                        mode = HAS_NEXT;
                    }

                    @Override
                    public boolean hasNext() {
                        if (mode == NOT_CALCULATED) calculate();
                        return mode == HAS_NEXT;
                    }

                    @Override
                    public EmbObject next() {
                        mode = NOT_CALCULATED;
                        return object;
                    }
                };
            }
        };
    }

    public Iterable<EmbObject> asColorEmbObjects() {
        return new Iterable<EmbObject>() {
            @NonNull
            @Override
            public Iterator<EmbObject> iterator() {
                return new Iterator<EmbObject>() {
                    int threadIndex = -1;
                    EmmThread thread = null;
                    final PointsIndexRange<DataPoints> points = new PointsIndexRange<>(stitches, 0, 0);
                    final EmbObject object = new EmbObject() {
                        @Override
                        public EmmThread getThread() {
                            if (thread != null) return thread;
                            if (threadlist.size() <= threadIndex) {
                                thread = getRandomThread();
                            } else {
                                thread = threadlist.get(threadIndex);
                            }
                            return thread;
                        }

                        @Override
                        public Points getPoints() {
                            return points;
                        }

                        @Override
                        public int getType() {
                            return 0;
                        }
                    };

                    final int NOT_CALCULATED = 0;
                    final int HAS_NEXT = 1;
                    final int ENDED = 2;

                    int mode = NOT_CALCULATED;

                    private boolean iterateLength() {
                        int length = 0;
                        for (int i = points.getStart(), ie = stitches.size(); i < ie; i++, length++) {
                            int data = stitches.getData(i);
                            if ((data & COMMAND_MASK) == COLOR_CHANGE) {
                                points.setLength(length);
                                return false;
                            }
                        }
                        points.setLength(length);
                        return true;
                    }

                    private void calculate() {
                        if (stitches.isEmpty()) {
                            mode = ENDED;
                            return;
                        }
                        threadIndex++;
                        thread = null;
                        points.setStart(points.getStart() + points.getLength());
                        points.setLength(0);
                        if (points.getStart() < stitches.size()) {
                            int data = stitches.getData(points.getStart());
                            if ((data & COMMAND_MASK) == COLOR_CHANGE) {
                                points.setStart(points.getStart() + 1);
                            }
                        } else {
                            mode = ENDED;
                            return;
                        }
                        iterateLength();
                        mode = HAS_NEXT;
                    }

                    @Override
                    public boolean hasNext() {
                        if (mode == NOT_CALCULATED) calculate();
                        return mode == HAS_NEXT;
                    }

                    @Override
                    public EmbObject next() {
                        mode = NOT_CALCULATED;
                        return object;
                    }
                };
            }
        };
    }

    public void addListener(Listener listener) {
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        if (listeners == null) return;
        listeners.remove(listener);
        if (listeners.isEmpty()) listeners = null;
    }

    public void notifyChange(int id) {
        if (listeners == null) return;
        for (Listener listener : listeners) {
            listener.notifyChange(id);
        }
    }

    public List<EmmThread> getUniqueThreadList() {
        ArrayList<EmmThread> threads = new ArrayList<>();
        for (EmmThread thread : threadlist) {
            if (!threads.contains(thread)) {
                threads.add(thread);
            }
        }
        return threads;
    }

    public List<EmmThread> getSingletonThreadList() {
        ArrayList<EmmThread> threads = new ArrayList<>();
        EmmThread previous = null;
        for (EmmThread thread : threadlist) {
            if (!thread.equals(previous)) {
                threads.add(thread);
            }
            previous = thread;
        }
        return threads;
    }

    public void translate(float dx, float dy) {
        stitches.translate(dx, dy);
    }

    public RectF getBounds(RectF bounds) {
        if (bounds == null) bounds = new RectF();
        if (stitches.isEmpty()) {
            bounds.setEmpty();
            return bounds;
        } else {
            bounds.set(stitches.getX(0), stitches.getY(0), stitches.getX(0), stitches.getY(0));
        }
        for (int i = 1, ie = stitches.size(); i < ie; i++) {
            bounds.union(stitches.getX(i), stitches.getY(i));
        }
        return bounds;
    }

    public EmbPattern toEmbPattern() {
        EmbPattern pattern = new EmbPattern();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            pattern.addStitchAbs(stitches.getX(i),stitches.getY(i),stitches.getData(i));
        }
        for (EmmThread t : getThreadlist()) {
            EmbThread embthread = new EmbThread(t.getColor(), t.getDescription(), t.getCatalogNumber(), t.getBrand(), t.getChart());
            pattern.addThread(embthread);
        }
        pattern.setAuthor(this.author);
        pattern.setCategory(this.category);
        pattern.setKeywords(this.keywords);
        pattern.setFilename(this.filename);
        pattern.setComments(this.comments);
        pattern.setName(this.name);
        return pattern;
    }

    public void fromEmbPattern(EmbPattern pattern) {
        org.embroideryio.geom.DataPoints stitches = pattern.getStitches();
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            this.addStitchAbs(stitches.getX(i), stitches.getY(i), stitches.getData(i),true);
        }
        for (EmbThread t : pattern.getThreadlist()) {
            EmmThread embthread = new EmmThread(t.getColor(), t.getDescription(), t.getCatalogNumber(), t.getBrand(), t.getChart());
            addThread(embthread);
        }
        this.setAuthor(pattern.author);
        this.setCategory(pattern.category);
        this.setKeywords(pattern.keywords);
        this.setFilename(pattern.filename);
        this.setComments(pattern.comments);
        this.setName(pattern.name);
    }

    public interface Listener {
        void notifyChange(int id);
    }

    public interface Provider {
        EmmPattern getPattern();
    }

    public void fixColorCount() {
        int threadIndex = 0;
        boolean starting = true;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int data = stitches.getData(i) & COMMAND_MASK;
            if (data == STITCH) {
                if (starting) threadIndex++;
                starting = false;
            } else if (data == COLOR_CHANGE) {
                if (starting) continue;
                threadIndex++;
            }
        }
        while (threadlist.size() < threadIndex) {
            addThread(getThreadOrFiller(threadlist.size()));
        }
        notifyChange(NOTIFY_THREADS_FIX);
    }

    public void add(double x, double y, int flag) {
        stitches.add((float) x, (float) y, flag);
    }

    public void addStitchAbs(float x, float y, int flags, boolean isAutoColorIndex) {
        stitches.add(x, y, flags);
        _previousX = x;
        _previousY = y;
        notifyChange(NOTIFY_STITCH_CHANGE);
    }

    /**
     * AddStitchRel adds a stitch to the pattern at the relative position (dx, dy)
     * to the previous stitch. Units are in millimeters.
     *
     * @param dx               The change in X position.
     * @param dy               The change in Y position. Positive value move upward.
     * @param flags            JUMP, TRIM, NORMAL or STOP
     * @param isAutoColorIndex Should color index be auto-incremented on STOP flag
     */
    public void addStitchRel(float dx, float dy, int flags, boolean isAutoColorIndex) {
        float x = _previousX + dx;
        float y = _previousY + dy;
        this.addStitchAbs(x, y, flags, isAutoColorIndex);
    }
}
