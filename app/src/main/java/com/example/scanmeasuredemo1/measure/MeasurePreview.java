package com.example.scanmeasuredemo1.measure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.scanmeasuredemo1.model.Contour;

import java.util.List;


/**
 * @author Mhd Wajeeh Ajajeh
 * @since 4/28/2018.
 * <p>
 * AppCompatImageView class that draws a list of contours inside it
 * then handle the adjustment of every contour points one by one
 * then handle the drawing of a line that represents the reference object
 * then adjust the reference line points
 * then get the real measure of the reference line
 * then draw the real measures of contours boundaries as text
 */

public class MeasurePreview extends AppCompatImageView {

    private static final String TAG = MeasurePreview.class.toString();

    private List<Contour> currentContours = null;
    private Paint contourPaint;
    private Path contourPath;

    private int[] contourColors = {Color.CYAN, Color.YELLOW, Color.MAGENTA};

    private Paint cornersPaint;

    private Paint refObjectLinePaint;
    private Paint refObjectPointPaint;

    private Paint measuresTextPaint;


    private boolean adjustingBoundaries = false;
    private int movingCornerIdx = -1;
    private PointF movingCornerStartPoint = null;
    private int adjustingContourIdx = -1;

    private boolean drawingReference;
    private PointF refP1 = new PointF(-1, -1), refP2 = new PointF(-1, -1);
    private PointF movingRefPoint = null;
    private PointF movingRefStartPoint = null;
    boolean referenceDrawn = false;

    private boolean refMeasured = false;
    private Double refRealMeasure;
    private Double measureScale;
    private boolean contoursScaled = false;


    public MeasurePreview(Context context) {
        super(context);
        init();
    }

    public MeasurePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MeasurePreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        setWillNotDraw(false);

        contourPaint = new Paint();
        contourPaint.setAntiAlias(true);
        contourPaint.setStyle(Paint.Style.STROKE);
        contourPaint.setStrokeWidth(8);

        contourPath = new Path();

        cornersPaint = new Paint();
        cornersPaint.setAntiAlias(true);
        cornersPaint.setColor(Color.RED);
        cornersPaint.setStyle(Paint.Style.FILL);

        refObjectLinePaint = new Paint();
        refObjectLinePaint.setAntiAlias(true);
        refObjectLinePaint.setColor(Color.BLUE);
        refObjectLinePaint.setStyle(Paint.Style.STROKE);
        refObjectLinePaint.setStrokeWidth(8);

        refObjectPointPaint = new Paint();
        refObjectPointPaint.setAntiAlias(true);
        refObjectPointPaint.setColor(Color.YELLOW);
        refObjectPointPaint.setStyle(Paint.Style.FILL);

        measuresTextPaint = new Paint();
        measuresTextPaint.setAntiAlias(true);
        measuresTextPaint.setColor(Color.YELLOW);
        measuresTextPaint.setTextSize(40);
        measuresTextPaint.setStyle(Paint.Style.FILL);

    }

    public List<Contour> getCurrentContours() {
        return currentContours;
    }

    public void setCurrentContours(List<Contour> currentContours) {
        this.currentContours = currentContours;

        contoursScaled = false;

        // transform to  4 point contours
        for (Contour c : this.currentContours) {
            c.transformTo4Point();
        }

        invalidate();
    }

    /*
    call this when you finish adjusting the current contour
       or finished drawing or adjusting the reference object
     */
    public void nextAdjustBoundary() {
        adjustingContourIdx++;
        adjustingBoundaries = (adjustingContourIdx < currentContours.size());
        drawingReference = !adjustingBoundaries;
        invalidate();
    }

    public boolean isAdjustingBoundaries() {
        return adjustingBoundaries;
    }

    public boolean isReferenceDrawn() {
        return referenceDrawn;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!contoursScaled) scaleContours();
        drawContours(canvas);
        drawCorners(canvas);
        drawReference(canvas);
        drawMeasures(canvas);
    }

    private void scaleContours() {
        // scale contours to the preview size
        for (Contour c : this.currentContours)
            c.scaleCorners(this.getWidth(), this.getHeight());

        contoursScaled = true;
    }

    private void drawCorners(Canvas canvas) {
        if (!adjustingBoundaries) return;

        for (PointF p : currentContours.get(adjustingContourIdx).getCorners()) {
            canvas.drawCircle(p.x, p.y, 25, cornersPaint);
        }
    }

    private void drawContours(Canvas canvas) {
        for (int i = 0; i < currentContours.size(); i++) {
            contourPaint.setColor(contourColors[i % contourColors.length]);
            drawContour(canvas, currentContours.get(i));
        }
    }

    private void drawContour(Canvas canvas, Contour contour) {
        contourPath.reset();
        contourPath.moveTo(contour.getCorners().get(0).x, contour.getCorners().get(0).y);
        for (PointF p : contour.getCorners()) {
            contourPath.lineTo(p.x, p.y);
        }
        contourPath.lineTo(contour.getCorners().get(0).x, contour.getCorners().get(0).y);

        canvas.drawPath(contourPath, contourPaint);
    }


    private void drawReference(Canvas canvas) {
        if (!drawingReference) return;

        //check if reference points existed
        if (refP1.x == -1 || refP2.x == -1) return;

        // draw reference object line
        canvas.drawLine(refP1.x, refP1.y, refP2.x, refP2.y, refObjectLinePaint);

        // draw reference object points
        canvas.drawCircle(refP1.x, refP1.y, 20, refObjectPointPaint);
        canvas.drawCircle(refP2.x, refP2.y, 20, refObjectPointPaint);


    }

    private void drawMeasures(Canvas canvas) {
        // check if reference object measured
        if (!refMeasured) return;


        // draw reference object measure
        drawTextMeasure(canvas, refP1, refP2);

        // draw boundaries measures
        for (Contour C : currentContours) {
            List<PointF> corners = C.getCorners();
            drawTextMeasure(canvas, corners.get(0), corners.get(corners.size() - 1));
            for (int i = 0; i < corners.size() - 1; i++) {
                drawTextMeasure(canvas, corners.get(i), corners.get(i + 1));
            }
        }
    }

    private void drawTextMeasure(Canvas canvas, PointF p1, PointF p2) {
        float x1 = p1.x, x2 = p2.x, y1 = p1.y, y2 = p2.y;
        float h = getHeight(), w = getWidth();

        Double mPix = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        Double mReal = measureScale * mPix;

        String mStr = String.format("%.2f", mReal);

        // text distance from line
        int TEXT_DIST = 25;

        // check if line horizontal or vertical
        boolean hor = (Math.abs(x1 - x2) > Math.abs(y1 - y2));

        if (hor) {
            // check whether to draw above or below
            boolean above = (Math.min(y1, y2) > h - Math.max(y1, y2));
            canvas.drawText(mStr, (x1 + x2) / 2,
                    above ? Math.min(y1, y2) - TEXT_DIST : Math.max(y1, y2) + TEXT_DIST, measuresTextPaint);

        } else {
            // check whether to draw left or right
            boolean left = (Math.min(x1, x2) > w - Math.max(x1, x2));
            canvas.drawText(mStr, left ? Math.min(x1, x2) - 3 * TEXT_DIST : Math.max(x1, x2) + TEXT_DIST
                    , (y1 + y2) / 2, measuresTextPaint);

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchAdjustBoundary(event) || touchDrawReference(event);
    }

    private boolean touchAdjustBoundary(MotionEvent event) {

        if (!adjustingBoundaries) return false;

        float x = event.getX(), y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                movingCornerIdx = currentContours.get(adjustingContourIdx).findNearestCorner(new PointF(x, y));
                movingCornerStartPoint = new PointF(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                float difX = x - movingCornerStartPoint.x, difY = y - movingCornerStartPoint.y;
                PointF p = currentContours.get(adjustingContourIdx).getCorners().get(movingCornerIdx);
                p.x += difX;
                p.y += difY;
                movingCornerStartPoint.x = x;
                movingCornerStartPoint.y = y;
                invalidate();
                break;

//            case MotionEvent.ACTION_UP:
//                movingCornerStartPoint = null;
//                movingCornerIdx = -1;
//                break;
        }

        return true;
    }

    private boolean touchDrawReference(MotionEvent event) {
        if (!drawingReference) return false;

        if (referenceDrawn) return touchAdjustReference(event);

        float x = event.getX(), y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                refP1.x = x;
                refP1.y = y;
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                refP2.x = x;
                refP2.y = y;
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                referenceDrawn = true;
                break;

        }

        return true;

    }

    private boolean touchAdjustReference(MotionEvent event) {
        float x = event.getX(), y = event.getY();


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                movingRefStartPoint = new PointF(x, y);

                // find nearest adjusting point
                PointF nearP;
                Double r1 = Math.pow(refP1.x - x, 2) + Math.pow(refP1.y - y, 2);
                Double r2 = Math.pow(refP2.x - x, 2) + Math.pow(refP2.y - y, 2);

                if (r1 < r2) movingRefPoint = refP1;
                else movingRefPoint = refP2;
                break;

            case MotionEvent.ACTION_MOVE:
                float difX = x - movingRefStartPoint.x, difY = y - movingRefStartPoint.y;
                movingRefPoint.x += difX;
                movingRefPoint.y += difY;
                movingRefStartPoint.x = x;
                movingRefStartPoint.y = y;
                invalidate();
                break;

        }

        return true;
    }

    // called when reference object measurement input
    public void onReferenceMeasure(double d) {
        refMeasured = true;
        refRealMeasure = d;

        Double refPixLength = Math.sqrt(Math.pow(refP1.x - refP2.x, 2) + Math.pow(refP1.y - refP2.y, 2));
        measureScale = refRealMeasure / refPixLength;
        invalidate();
    }
}
