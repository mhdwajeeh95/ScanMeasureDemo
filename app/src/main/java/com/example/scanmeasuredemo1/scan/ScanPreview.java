package com.example.scanmeasuredemo1.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.example.scanmeasuredemo1.model.Contour;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Mhd Wajeeh Ajajeh
 * @since 4/28/2018.
 *
 *  SurfaceView class that draws a list of contours inside it
 */

public class ScanPreview extends SurfaceView {

    private static final String TAG = ScanPreview.class.toString();

    private List<Contour> currentContours = new ArrayList<>();
    private Paint contourPaint;
    private Path contourPath;
    private boolean contoursScaled = false;
    private int[] contourColors = {Color.CYAN, Color.YELLOW, Color.MAGENTA};

    public ScanPreview(Context context) {
        super(context);
        init();
    }

    public ScanPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        setWillNotDraw(false);

        contourPaint = new Paint();
        contourPaint.setStyle(Paint.Style.STROKE);
        contourPaint.setStrokeWidth(8);

        contourPath = new Path();
    }

    public List<Contour> getCurrentContours() {
        return currentContours;
    }

    public void setCurrentContours(List<Contour> currentContours) {
        this.currentContours = currentContours;
        contoursScaled = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!contoursScaled) scaleContours();
        drawContours(canvas);
    }

    private void scaleContours() {
        // scale contours to the preview size
        for (Contour c : this.currentContours)
            c.scaleCorners(this.getWidth(), this.getHeight());

        contoursScaled = true;
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


}
