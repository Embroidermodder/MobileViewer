package com.embroidermodder.embroideryviewer;

import android.support.annotation.NonNull;

import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Points;
import com.embroidermodder.embroideryviewer.geom.PointsIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EmbPattern {
    public static final int NO_COMMAND = -1;
    public static final int STITCH = 0;
    public static final int JUMP = 1;
    public static final int TRIM = 2;
    public static final int STOP = 3;
    public static final int END = 4;
    public static final int COLOR_CHANGE = 5;
    public static final int INIT = 6;
    public static final int TIE_ON = 7;
    public static final int TIE_OFF = 8;
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

    public ArrayList<EmbThread> threadlist = new ArrayList<>();
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

    public EmbPattern() {
        threadlist = new ArrayList<>();
    }

    public EmbPattern(EmbPattern p) {
        this.filename = p.filename;
        this.name = p.name;
        this.category = p.category;
        this.author = p.author;
        this.keywords = p.keywords;
        this.comments = p.comments;
        this.threadlist = new ArrayList<>(p.threadlist.size());
        for (EmbThread thread : p.getThreadlist()) {
            addThread(new EmbThread(thread));
        }
        this.stitches = new DataPoints(p.stitches);
    }

    public void setPattern(EmbPattern p) {
        this.filename = p.filename;
        this.name = p.name;
        this.category = p.category;
        this.author = p.author;
        this.keywords = p.keywords;
        this.comments = p.comments;
        this.threadlist.clear();
        for (EmbThread thread : p.getThreadlist()) {
            addThread(new EmbThread(thread));
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

    void add(EmbThread embroideryThread) {
        threadlist.add(embroideryThread);
    }

    public ArrayList<EmbThread> getThreadlist() {
        return threadlist;
    }

    public void addThread(EmbThread thread) {
        threadlist.add(thread);
    }

    public EmbThread getThread(int index) {
        return threadlist.get(index);
    }

    public EmbThread getRandomThread() {
        return new EmbThread(0xFF000000 | (int) (Math.random() * 0xFFFFFF), "Random");
    }

    public EmbThread getThreadOrFiller(int index) {
        if (threadlist.size() <= index) {
            return getRandomThread();
        }
        return threadlist.get(index);
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
        if (category != null) metadata.put(PROP_CATEGORY, name);
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

    public Iterable<EmbObject> asSectionEmbObjects() {
        return new Iterable<EmbObject>() {
            @NonNull
            @Override
            public Iterator<EmbObject> iterator() {
                return new Iterator<EmbObject>() {

                    final int NOT_CALCULATED = 0;
                    final int HAS_NEXT = 1;
                    final int ENDED = 2;

                    int mode = NOT_CALCULATED;

                    int threadIndex = 0;
                    int type;

                    final PointsIndex<DataPoints> points = new PointsIndex<>(stitches, -1, 0);

                    final EmbObject object = new EmbObject() {
                        @Override
                        public EmbThread getThread() {
                            return getThreadOrFiller(threadIndex);
                        }

                        @Override
                        public Points getPoints() {
                            return points;
                        }

                        @Override
                        public int getType() {
                            return type;
                        }
                    };

                    private void calculate() {
                        points.setIndex_start(points.getIndex_stop());
                        points.setIndex_stop(-1);
                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data == COLOR_CHANGE) {
                                threadIndex++;
                            }
                            if (data == STITCH) {
                                type = STITCH;
                                points.setIndex_start(i);
                                break;
                            }
                            if (data == JUMP) {
                                type = JUMP;
                                points.setIndex_start(i);
                                break;
                            }
                        }
                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data != type) {
                                points.setIndex_stop(i);
                                break;
                            }
                        }
                        mode = ((points.getIndex_stop() == -1) || (points.getIndex_start() == points.getIndex_stop())) ? ENDED : HAS_NEXT;
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

    public Iterable<EmbObject> asStitchEmbObjects() {
        return new Iterable<EmbObject>() {
            @NonNull
            @Override
            public Iterator<EmbObject> iterator() {
                return new Iterator<EmbObject>() {
                    int threadIndex = 0;
                    final PointsIndex<DataPoints> points = new PointsIndex<>(stitches, -1, 0);

                    final EmbObject object = new EmbObject() {
                        @Override
                        public EmbThread getThread() {
                            return getThreadOrFiller(threadIndex);
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

                    private void calculate() {
                        points.setIndex_start(points.getIndex_stop());
                        points.setIndex_stop(-1);
                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data == COLOR_CHANGE) {
                                threadIndex++;
                            }
                            if (data == STITCH) {
                                points.setIndex_start(i);
                                break;
                            }
                        }
                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data != STITCH) {
                                points.setIndex_stop(i);
                                break;
                            }
                        }
                        mode = ((points.getIndex_stop() == -1) || (points.getIndex_start() == points.getIndex_stop())) ? ENDED : HAS_NEXT;
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

    public Iterable<EmbObject> asJumpEmbObjects() {
        return new Iterable<EmbObject>() {
            @NonNull
            @Override
            public Iterator<EmbObject> iterator() {
                return new Iterator<EmbObject>() {
                    int threadIndex = 0;
                    final PointsIndex<DataPoints> points = new PointsIndex<>(stitches, -1, 0);

                    final EmbObject object = new EmbObject() {
                        @Override
                        public EmbThread getThread() {
                            return getThreadOrFiller(threadIndex);
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

                    private void calculate() {
                        points.setIndex_start(points.getIndex_stop());
                        points.setIndex_stop(-1);
                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data == COLOR_CHANGE) {
                                threadIndex++;
                            }
                            if (data == JUMP) {
                                points.setIndex_start(i);
                                break;
                            }
                        }
                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data != JUMP) {
                                points.setIndex_stop(i);
                                break;
                            }
                        }
                        mode = ((points.getIndex_stop() == -1) || (points.getIndex_start() == points.getIndex_stop())) ? ENDED : HAS_NEXT;
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
                    final int NOT_CALCULATED = 0;
                    final int HAS_NEXT = 1;
                    final int ENDED = 2;

                    int mode = NOT_CALCULATED;

                    int threadIndex = 0;
                    final PointsIndex<DataPoints> points = new PointsIndex<>(stitches, -1, 0);
                    final EmbObject object = new EmbObject() {
                        @Override
                        public EmbThread getThread() {
                            return getThreadOrFiller(threadIndex);
                        }

                        @Override
                        public Points getPoints() {
                            return points;
                        }

                        @Override
                        public int getType() {
                            return -1;
                        }
                    };

                    private void calculate() {
                        points.setIndex_start(points.getIndex_stop());
                        points.setIndex_stop(-1);

                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data == COLOR_CHANGE) {
                                points.setIndex_stop(i);
                                threadIndex++;
                                break;
                            }
                        }
                        mode = ((points.getIndex_stop() == -1) || (points.getIndex_start() == points.getIndex_stop())) ? ENDED : HAS_NEXT;
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

    public List<EmbThread> getUniqueThreadList() {
        ArrayList<EmbThread> threads = new ArrayList<>();
        for (EmbThread thread : threadlist) {
            if (!threads.contains(thread)) {
                threads.add(thread);
            }
        }
        return threads;
    }

    public void translate(float dx, float dy) {
        stitches.translate(dx, dy);
    }

    public interface Listener {
        void notifyChange(int id);
    }

    public interface Provider {
        EmbPattern getPattern();
    }

    public void fixColorCount() {
        int threadIndex = 0;
        boolean starting = true;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int data = stitches.getData(i);
            if (data == STITCH) {
                if (starting) threadIndex++;
                starting = false;
            } else if ((data & COLOR_CHANGE) != 0) {
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
