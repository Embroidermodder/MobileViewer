package com.embroidermodder.embroideryviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;

public class EmbPattern {

static final int FLIP_HORIZONTAL = 0;
static final int FLIP_VERTICAL = 1;
static final int FLIP_BOTH = 2;
static final int ROTATE_RIGHT = 0;
static final int ROTATE_LEFT = 1;
static final int RIGHT_TO_LEFT = 0;
static final int LEFT_TO_RIGHT = 1;


int normal_stitches = 0;
int jump_stitches = 0;
int trim_stitches = 0;
int total_stitches = 0;

private static final float PIXELS_PER_MM = 10;
private final ArrayList<StitchBlock> _stitchBlocks;
private  static   String nome ="";
private final ArrayList<EmbThread> _threadList;
private final ArrayList<EmbPattern.Listener> _listeners;
private String _filename;
private StitchBlock _currentStitchBlock;
private static  List<Cpoint> Points = new ArrayList<Cpoint>();
public static Cpoint CurrentPoint;
private float _previousX = 0;
private float _previousY = 0;




public EmbPattern() {
    _listeners = new ArrayList<>();
    _stitchBlocks = new ArrayList<>();
    _threadList = new ArrayList<>();
    _currentStitchBlock = null;
}
public ArrayList<StitchBlock> getStitchBlocks()
{
    return _stitchBlocks;
}
public  List<Cpoint> getPoints()
{
    return Points; // array point out embroidery
}
public ArrayList<stitch> stitches = new ArrayList<stitch>();
public  ArrayList<stitch> getstitches() {
    return stitches;
}
public ArrayList<EmbPoint> stitches1 = new ArrayList<EmbPoint>();
public  ArrayList<EmbPoint> getstitches1() {
    return stitches1;
}




public void AddPointAbs(float x, float y, int flags) {



    stitches.add(new stitch(x, y, flags));
}
public void AddPointRel(float X, float Y, int flags)
{
    CurrentPoint.X = CurrentPoint.X + X;
    CurrentPoint.Y =CurrentPoint.Y +Y;
    AddPointAbs(X, Y, flags);

}
private int CgetTotalSize() {
    int count = 0;
    for (Cpoint points : Points) {
        count +=1;
    }
    return count;
}
float dwidth;
float dheight;
String filepath;
String filename;
float min_x = 0;
float max_x = 0;
float min_y = 0;
float max_y = 0;
float x;
float y;
int color_changes1=0;
float width;
float height;
boolean use_random_colors = false;
// int scale = 100;
stitch last_stitch = null;
public void add_stitch_abs(float x, float y, int type)
{
// if(last_stitch == null && use_random_colors)
// {
//add_random_color();
// }
if(type == globals.STITCH_END)
{
return;
}
else if(type == IFormat.STOP)
{
// if(use_random_colors)
// {
// // add_random_color();
// }
color_changes1 = color_changes1+1;
}
else if(type == globals.STITCH_JUMP)
{
jump_stitches++;
}
else if(type == globals.STITCH_TRIM)
{
trim_stitches++;
}
else
{
normal_stitches++;
}
stitch s = new stitch(x, y, type);
stitches.add(s);
total_stitches++;
last_stitch = s;
}
public int gettotal() {
return total_stitches;
}
public int getcolor() {
return color_changes1;
}
public void add_stitch_rel(float dx, float dy, int type)
{
if(last_stitch != null)
{
dx += last_stitch.x;
dy += last_stitch.y;
}
add_stitch_abs(dx, dy, type);
}
public class stitch
{
float x;
float y;
int type;

    public stitch(float X, float Y,  int TYPE)
    {
        x = X;
        y = Y;
        type = TYPE;
    }

}
public void correctForMaxStitchLength(double maxStitchLength, double maxJumpLength) {
    double maxLen;
    float dx, dy, xx, yy, maxXY, addX, addY;
    int splits, flagsToUse;
    for (int i = 1; i < stitches1.size(); i++) {
        xx = stitches1.get(i-1).X;
        yy = stitches1.get(i-1).Y;
        dx = stitches1.get(i).X - xx;
        dy = stitches1.get(i).Y - yy;
        if ((Math.abs(dx) > maxStitchLength) || (Math.abs(dy) > maxStitchLength)) {
            maxXY = Math.abs(dx);
            if (Math.abs(dy) > maxXY) {
                maxXY = Math.abs(dy);
            }
            if ((stitches1.get(i).Flags & ( IFormat.JUMP | IFormat.TRIM | IFormat.STOP)) != 0) {
                maxLen = maxJumpLength;
            } else {
                maxLen = maxStitchLength;
            }
            splits = (int)Math.ceil((double)maxXY / maxLen);
            if (splits > 1) {
                flagsToUse = stitches1.get(i).Flags;
                addX = dx / splits;
                addY = dy / splits;
                for (int j = 1; j < splits; j++) {
                    stitches1.add(i, new EmbPoint(xx + addX * j, yy + addY * j, flagsToUse));
                    i++;
                }
                i--;
            }
        }
    }
}
public void rotate_90(int direction) {
    for (int s = 0; s < stitches1.size(); s++) {
        {
            float tmp = stitches1.get(s).Y;

            stitches1.get(s).Y = -stitches.get(s).x;

            if (direction == globals.ROTATE_LEFT) {
                stitches1.get(s).X = tmp;
            } else if (direction == globals.ROTATE_RIGHT) {
                stitches1.get(s).X = -tmp;
            }

        }

    }}
public void rel_flip(int flip_type) {
    for (int s = 0; s < stitches1.size(); s++) {
        if (flip_type == FLIP_HORIZONTAL || flip_type == FLIP_BOTH) {
            stitches1.get(s).X *= -1;
        }
        if (flip_type == FLIP_VERTICAL || flip_type == FLIP_BOTH) {
            stitches1.get(s).Y *= -1;
        }
    }
}
void init()
{
    width = dwidth = max_x - min_x;
    height = dheight = max_y - min_y;
   // repos_stitches();
}
void repos_stitches()
{
    if(stitches1.get(0).X != 0 && stitches1.get(0).Y != 0) // wrong start stitch // first stitch must be (0, 0, JUMP)
    {
        stitches1.add(0, new EmbPoint(0, 0, IFormat.JUMP));
    }
    stitches1.get(0).X = Math.abs(min_x);
    stitches1.get(0).Y = max_y;
    for(int s = 1; s < stitches1.size(); s++)
    {
        stitches1.get(s).X = stitches1.get(0).X + stitches1.get(s).X;
        stitches1.get(s).Y = stitches1.get(0).Y - stitches1.get(s).Y;
    }
}
public ArrayList<EmbThread> getThreadList() {
    return _threadList;
}
public String getFilename() {
    return _filename;
}
public void setFilename(String value) {
    _filename = value;
}
public void addStitchAbs(float x, float y, int flags, boolean isAutoColorIndex) {
    EmbPoint s = new EmbPoint(x, y, flags);
    stitches1.add(s);
if (this._currentStitchBlock == null) {
if (_stitchBlocks.size() == 0) {
this._currentStitchBlock = new StitchBlock();
EmbThread thread;
if (this._threadList.size() == 0) {
thread = new EmbThread();
thread.setColor(EmbColor.Random());
this._threadList.add(thread);
} else {
thread = this._threadList.get(0);
}
this._currentStitchBlock.setThread(thread);
_stitchBlocks.add(this._currentStitchBlock);
} else {
this._currentStitchBlock = this._stitchBlocks.get(0);
}
}
if ((flags & IFormat.END) != 0) {
if (this._currentStitchBlock.isEmpty()) {
this._stitchBlocks.remove(this._currentStitchBlock);
return;
}
//pattern.FixColorCount();
}
if ((flags & IFormat.STOP) > 0) {
if ((this._currentStitchBlock.isEmpty())) {
return;
}
int threadIndex = 0;
int currIndex = this._threadList.indexOf(this._currentStitchBlock.getThread());
if (isAutoColorIndex) {
if ((currIndex + 1) >= this._threadList.size()) {
EmbThread newThread = new EmbThread();
newThread.setColor(EmbColor.Random());
this._threadList.add(newThread);
}
color_changes1++;
threadIndex = currIndex + 1;
}
StitchBlock sb = new StitchBlock();
this._currentStitchBlock = sb;
sb.setThread(this._threadList.get(threadIndex));
this.getStitchBlocks().add(sb);
return;
}
if ((flags & IFormat.TRIM) > 0) {
_previousX = x;
_previousY = y;
if ((this._currentStitchBlock.isEmpty())) {
return;
}
int currIndex = this._threadList.indexOf(this._currentStitchBlock.getThread());
StitchBlock sb = new StitchBlock();
this._currentStitchBlock = sb;
sb.setThread(this._threadList.get(currIndex));
this.getStitchBlocks().add(sb);
return;
}
total_stitches++;
_previousX = x;
_previousY = y;
if ((flags & IFormat.JUMP) == 0) {
this._currentStitchBlock.add(x, y);
}
}
/**
* AddStitchRel adds a stitch to the pattern at the relative position (dx, dy)
* to the previous stitch. Units are in millimeters.
*
* @Param dx The change in X position.
* @Param dy The change in Y position. Positive value move upward.
* @Param flags JUMP, TRIM, NORMAL or STOP
* @Param isAutoColorIndex Should color index be auto-incremented on STOP flag
/
public void addStitchRel(float dx, float dy, int flags, boolean isAutoColorIndex) {
float x = _previousX + dx;
float y = _previousY + dy;
this.addStitchAbs(x, y, flags, isAutoColorIndex);
}
public void addThread(EmbThread thread) {
_threadList.add(thread);
}
public RectF calculateBoundingBox() {
float left = Float.POSITIVE_INFINITY;
float top = Float.POSITIVE_INFINITY;
float right = Float.NEGATIVE_INFINITY;
float bottom = Float.NEGATIVE_INFINITY;
for (StitchBlock sb : this.getStitchBlocks()) {
left = Math.min(left, sb.getMinX());
top = Math.min(top, sb.getMinY());
right = Math.max(right, sb.getMaxX());
bottom = Math.max(bottom, sb.getMaxY());
}
return new RectF(left, top, right, bottom);
}
/*
* Flip will flip the entire pattern about the specified axis
*
* @Param horizontal should pattern be flipped about the x-axis
* @Param vertical should pattern be flipped about the xy-axis
* @return the flipped pattern
*/
public EmbPattern getFlippedPattern(boolean horizontal, boolean vertical) {
float xMultiplier = horizontal ? -1.0f : 1.0f;
float yMultiplier = vertical ? -1.0f : 1.0f;
Matrix m = new Matrix();
m.postScale(xMultiplier, yMultiplier);
for (StitchBlock sb : this.getStitchBlocks()) {
sb.transform(m);
}
return this;
}
public EmbPattern getPositiveCoordinatePattern() {
RectF boundingRect = this.calculateBoundingBox();
Matrix m = new Matrix();
m.setTranslate(-boundingRect.left, -boundingRect.top);
for (StitchBlock sb : this.getStitchBlocks()) {
sb.transform(m);
}
return this;
}
public EmbPattern getCenteredPattern() {
RectF boundingRect = this.calculateBoundingBox();
float cx = boundingRect.centerX();
float cy = boundingRect.centerY();
Matrix m = new Matrix();
m.setTranslate(cx, cy);
for (StitchBlock sb : this.getStitchBlocks()) {
sb.transform(m);
}
return this;
}
private float pixelstomm(float v) {
return v / PIXELS_PER_MM;
}
public String convert(float v) {
return String.format("%.1f", pixelstomm(v));
}
public String getStatistics(Context context) {
for (StitchBlock s : _stitchBlocks) {
s.snap();
}
nome =IFormat.tetFileName1();
RectF bounds = calculateBoundingBox();
StringBuilder sb = new StringBuilder();
sb.append("Nome do desenho: ").append(nome).append('\n');

    int totalSize = getTotalSize();
    int jumpCount = getJumpCount();
    int colorCount = getColorCount();
    //  sb.append(context.getString(R.string.number_of_stitches)).append(totalSize + jumpCount + colorCount).append('\n');

    // sb.append("Design name: ").append(texto).append('\n');




    sb.append(context.getString(R.string.normal_stitches)).append(totalSize).append('\n');

    sb.append(context.getString(R.string.jumps)).append(jumpCount).append('\n');

    sb.append(context.getString(R.string.colors)).append(colorCount).append('\n');

    sb.append(context.getString(R.string.size)).append(convert(bounds.width()/10)).append(" Cm X\n ");
    sb.append(context.getString(R.string.size)).append(convert(bounds.height()/10)).append(" Cm Y\n");

    //  sb.append(context.getString(R.string.total_length)).append(convert(totalLength)).append(" mm\n");
    return sb.toString();
}
private int getTotalSize() {
    int count = 0;
    for (StitchBlock sb : _stitchBlocks) {
        count += sb.size();
    }
    return count;
}
private float getTotalLength() {
    float count = 0;
    for (StitchBlock sb : _stitchBlocks) {
        for (int i = 0, s = sb.size() - 1; i < s; i++) {
            count += sb.distanceSegment(i);
        }
    }
    return count;
}
private int getJumpCount() {
    return _stitchBlocks.size();
}
private int getColorCount() {
    return this._threadList.size();
}
private float getMaxStitch() {
    float count = Float.NEGATIVE_INFINITY;
    float current;
    for (StitchBlock sb : _stitchBlocks) {
        for (int i = 0, s = sb.size() - 1; i < s; i++) {
            current = sb.distanceSegment(i);
            if (current > count) {
                count = current;
            }
        }
    }
    return count;
}
private float getMinStitch() {
    float count = Float.POSITIVE_INFINITY;
    float current;
    for (StitchBlock sb : _stitchBlocks) {
        for (int i = 0, s = sb.size() - 1; i < s; i++) {
            current = sb.distanceSegment(i);
            if (current < count) {
                count = current;
            }
        }
    }
    return count;
}
private int getCountRange(float min, float max) {
    int count = 0;
    float current;
    for (StitchBlock sb : _stitchBlocks) {
        for (int i = 0, s = sb.size() - 1; i < s; i++) {
            current = sb.distanceSegment(i);
            if ((current >= min) && (current <= max)) {
                count++;
            }
        }
    }
    return count;
}
public void notifyChange(int v) {
    for (Listener listener : _listeners) {
        listener.update(v);
    }
}
public void addListener(Listener listener) {
    if (!_listeners.contains(listener)) {
        _listeners.add(listener);
    }
}
public void removeListener(Object listener) {
    if (listener instanceof Listener) {
        _listeners.remove(listener);
    }
}
public Bitmap getThumbnail(float _width, float _height) {

    RectF viewPort = calculateBoundingBox();
    float scale = Math.min(_height / viewPort.height(), _width / viewPort.width());
    Matrix matrix = new Matrix();
    if (scale != 0) {
        matrix.postTranslate(-viewPort.left, -viewPort.top);
        matrix.postScale(scale, scale);
    }

    Bitmap bmp = Bitmap.createBitmap((int) _width, (int) _height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bmp);
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    if (matrix != null) canvas.concat(matrix);
    for (StitchBlock stitchBlock : getStitchBlocks()) {
        stitchBlock.draw(canvas, paint);


    }
    return bmp;
}
public interface Provider {
    EmbPattern getPattern();
}
public interface Listener {
    void update(int v);
}
}