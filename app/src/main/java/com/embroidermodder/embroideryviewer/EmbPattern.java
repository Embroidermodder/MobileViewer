package com.embroidermodder.embroideryviewer;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.embroidermodder.embroideryviewer.geom.DataPoints;
import com.embroidermodder.embroideryviewer.geom.Point;
import com.embroidermodder.embroideryviewer.geom.PointDirect;
import com.embroidermodder.embroideryviewer.geom.PointIterator;
import com.embroidermodder.embroideryviewer.geom.Points;
import com.embroidermodder.embroideryviewer.geom.PointsIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EmbPattern {
    static final int FLIP_HORIZONTAL = 0;
    static final int FLIP_VERTICAL = 1;
    static final int FLIP_BOTH = 2;

    static final int ROTATE_RIGHT = 0;
    static final int ROTATE_LEFT = 1;

    static final int RIGHT_TO_LEFT = 0;
    static final int LEFT_TO_RIGHT = 1;

    public static final int NOTIFY_CORRECT_LENGTH = 1; //these are just provisional.
    public static final int NOTIFY_ROTATED = 2;
    public static final int NOTIFY_FLIP = 3;
    public static final int NOTIFY_THREADS_FIX = 4;
    public static final int NOTIFY_METADATA = 5;
    public static final int NOTIFY_STITCH_CHANGE = 6;
    public static final int NOTIFY_LOADED = 7;
    public static final int NOTIFY_CHANGE = 8;
    public static final int NOTIFY_THREAD_COLOR = 9;

    private DataPoints stitches = new DataPoints();
    private final ArrayList<EmbThread> _threadList;

    public static final String PROP_FILENAME = "filename";
    public static final String PROP_NAME = "name";
    public static final String PROP_CATEGORY = "category";
    public static final String PROP_AUTHOR = "author";
    public static final String PROP_KEYWORDS = "keywords";
    public static final String PROP_COMMENTS = "comments";

    public String filename;
    public String name;
    public String category;
    public String author;
    public String keywords;
    public String comments;


    private float _previousX = 0;
    private float _previousY = 0;


    private ArrayList<Listener> listeners;

    public EmbPattern(EmbPattern p) {
        this.filename = p.filename;
        this.name = p.name;
        this.category = p.category;
        this.author = p.author;
        this.keywords = p.keywords;
        this.comments = p.comments;
        this._threadList = new ArrayList<>(p._threadList.size());
        for (EmbThread thread : p.getThreadList()) {
            addThread(new EmbThread(thread));
        }
        this.stitches = new DataPoints(p.stitches);
    }

    public EmbPattern() {
        _threadList = new ArrayList<>();
    }

    public void setPattern(EmbPattern p) {
        this.filename = p.filename;
        this.name = p.name;
        this.category = p.category;
        this.author = p.author;
        this.keywords = p.keywords;
        this.comments = p.comments;
        this._threadList.clear();
        for (EmbThread thread : p.getThreadList()) {
            addThread(new EmbThread(thread));
        }
        this.stitches = new DataPoints(p.stitches);
    }

    public DataPoints getStitches() {
        return stitches;
    }

    public void correctForMaxStitchLength(double maxStitchLength, double maxJumpLength) {
        double maxLen;
        double dx, dy, xx, yy, maxXY, addX, addY;
        int splits, flagsToUse;
        for (int i = 1; i < stitches.size(); i++) {
            xx = stitches.getX(i - 1);
            yy = stitches.getY(i - 1);
            dx = stitches.getX(i) - xx;
            dy = stitches.getY(i) - yy;

            if ((Math.abs(dx) > maxStitchLength) || (Math.abs(dy) > maxStitchLength)) {
                maxXY = Math.abs(dx);
                if (Math.abs(dy) > maxXY) {
                    maxXY = Math.abs(dy);
                }
                if ((stitches.getData(i) & (IFormat.JUMP | IFormat.TRIM)) > 0) {
                    maxLen = maxJumpLength;
                } else {
                    maxLen = maxStitchLength;
                }
                splits = (int) Math.ceil((double) maxXY / maxLen);
                if (splits > 1) {
                    flagsToUse = stitches.getData(i);
                    addX = dx / splits;
                    addY = dy / splits;
                    for (int j = 1; j < splits; j++) {
                        stitches.add(i, new PointDirect(xx + addX * j, yy + addY * j, flagsToUse));
                        i++;
                    }
                    i--;
                }
            }
        }
        notifyChange(NOTIFY_CORRECT_LENGTH);
    }

    public void rotate_90(int direction) {
        if (direction == ROTATE_LEFT) {
            for (Point p : new PointIterator<Points>(stitches)) {
                p.setLocation(p.getY(), p.getX());
            }
        } else if (direction == ROTATE_RIGHT) {
            for (Point p : new PointIterator<Points>(stitches)) {
                p.setLocation(p.getY(), -p.getX());
            }
        }
        notifyChange(NOTIFY_ROTATED);

    }

    public void rel_flip(int flip_type) {
        switch (flip_type) {
            case FLIP_HORIZONTAL:
                for (Point p : new PointIterator<Points>(stitches)) {
                    p.setLocation(-p.getX(), p.getY());
                }
                break;
            case FLIP_VERTICAL:
                for (Point p : new PointIterator<Points>(stitches)) {
                    p.setLocation(p.getX(), -p.getY());
                }
                break;
            case FLIP_BOTH:
                for (Point p : new PointIterator<Points>(stitches)) {
                    p.setLocation(-p.getX(), -p.getY());
                }
        }
        //todo: The stitches relies on datapoints now so, the points can be moved with a matrix applied to them.
        notifyChange(NOTIFY_FLIP);
    }

    public void fixColorCount() {
        int threadIndex = 0;
        boolean starting = true;
        for (int i = 0, ie = stitches.size(); i < ie; i++) {
            int data = stitches.getData(i);
            if (data == IFormat.NORMAL) {
                if (starting) threadIndex++;
                starting = false;
            } else if (((data & IFormat.STOP) != 0) || ((data & IFormat.COLOR_CHANGE) != 0)) {//TODO: Stop should not be considered color change.
                if (starting) continue;//if colorchange op, before any stitches, ignore it.
                threadIndex++;
            }
        }
        while (_threadList.size() < threadIndex) {
            addThread(getThreadOrFiller(_threadList.size()));
        }
        notifyChange(NOTIFY_THREADS_FIX);
    }

    public ArrayList<EmbThread> getThreadList() {
        return _threadList;
    }

    public void addThread(EmbThread thread) {
        _threadList.add(thread);
    }

    public EmbThread getThread(int index) {
        return _threadList.get(index);
    }

    public EmbThread getRandomThread() {
        return new EmbThread(0xFF000000 | (int) (Math.random() * 0xFFFFFF), "Random");
    }

    public EmbThread getThreadOrFiller(int index) {
        if (_threadList.size() <= index) {
            if (index < 60) return FormatPec.getThreadByIndex(index);
            return getRandomThread();
        }
        return _threadList.get(index);
    }

    public int getThreadCount() {
        if (_threadList == null) return 0;
        return _threadList.size();
    }

    public boolean isEmpty() {
        if (stitches == null) return true;
        if (stitches.isEmpty()) {
            return _threadList.isEmpty();
        }
        return false;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String value) {
        filename = value;
        notifyChange(NOTIFY_METADATA);
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

    public float getPreviousX() {
        return _previousX;
    }

    public float getPreviousY() {
        return _previousY;
    }

    public RectF calculateBoundingBox() {
        double left = Double.POSITIVE_INFINITY;
        double top = Double.POSITIVE_INFINITY;
        double right = Double.NEGATIVE_INFINITY;
        double bottom = Double.NEGATIVE_INFINITY;
        for (Point p : new PointIterator<Points>(stitches)) {
            left = Math.min(left, p.getX());
            top = Math.min(top, p.getY());
            right = Math.max(right, p.getX());
            bottom = Math.max(bottom, p.getY());
        }
        return new RectF((float) left, (float) top, (float) right, (float) bottom);
    }

    /**
     * Flip will flip the entire pattern about the specified axis
     *
     * @param horizontal should pattern be flipped about the x-axis
     * @param vertical   should pattern be flipped about the xy-axis
     * @return the flipped pattern
     */
    public EmbPattern getFlippedPattern(boolean horizontal, boolean vertical) {
        if (horizontal) {
            if (vertical) {
                rel_flip(FLIP_BOTH);
            } else {
                rel_flip(FLIP_HORIZONTAL);
            }
        } else {
            if (vertical) {
                rel_flip(FLIP_VERTICAL);
            }
        }
        return this;
    }

    public EmbPattern getPositiveCoordinatePattern() {
        RectF boundingRect = this.calculateBoundingBox();
        float dx = -boundingRect.left;
        float dy = -boundingRect.top;
        for (Point p : new PointIterator<Points>(stitches)) {
            p.setLocation(p.getX() - dx, p.getY() - dy);
        }
        return this;
    }

    public EmbPattern getCenteredPattern() {
        RectF boundingRect = this.calculateBoundingBox();
        float dx = boundingRect.centerX();
        float dy = boundingRect.centerY();
        for (Point p : new PointIterator<Points>(stitches)) {
            p.setLocation(p.getX() - dx, p.getY() - dy);
        }
        return this;
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
                            return IFormat.NORMAL;
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
                            if (((data & IFormat.STOP) != 0) || ((data & IFormat.COLOR_CHANGE) != 0)) {
                                threadIndex++;
                            }
                            if (data == IFormat.NORMAL) {
                                points.setIndex_start(i);
                                break;
                            }
                        }
                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data != IFormat.NORMAL) {
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
                    final int NOT_CALCULATED = 0;
                    final int HAS_NEXT = 1;
                    final int ENDED = 2;

                    int mode = NOT_CALCULATED;

                    private void calculate() {
                        boolean starting = points.getIndex_start() == -1;
                        points.setIndex_start(points.getIndex_stop());
                        points.setIndex_stop(-1);

                        for (int i = points.getIndex_start(), ie = stitches.size(); i < ie; i++) {
                            int data = stitches.getData(i);
                            if (data == IFormat.NORMAL) starting = false;
                            if (((data & IFormat.STOP) != 0) || ((data & IFormat.COLOR_CHANGE) != 0)) { //TODO: Only process color changes, do not use stops as color changes.
                                if (starting)
                                    continue; //colorchange before any normal stitches, this does not change anything.
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
                        threadIndex++;
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

    public interface Listener {
        void notifyChange(int id);
    }

    public interface Provider {
        EmbPattern getPattern();
    }

}