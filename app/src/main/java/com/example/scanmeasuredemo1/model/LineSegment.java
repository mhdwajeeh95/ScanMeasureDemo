package com.example.scanmeasuredemo1.model;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/10/2018.
 *
 * this class holds line segment data as two points
 */

public class LineSegment {

    // the line points
    public  PointF p1,p2;

    public LineSegment(PointF p1, PointF p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * @param lineSegments
     * @return list of intersection points between all line segments
     */
    public static List<PointF> linesIntersections(List<LineSegment> lineSegments) {
        List<PointF> pointList = new ArrayList<>();
        for (LineSegment lineSeg1 : lineSegments)
            for (LineSegment lineSeg2 : lineSegments) {
                PointF intersectP = intersect(lineSeg1, lineSeg2);
                if (intersectP != null) pointList.add(intersectP);
            }
        return pointList;

    }

    /**
     *
     * @param lineSeg1
     * @param lineSeg2
     * @return intersection point between the two line segments
     */
    public static PointF intersect(LineSegment lineSeg1, LineSegment lineSeg2) {
        float x1 = lineSeg1.p1.x, x2 = lineSeg1.p2.x, x3 = lineSeg2.p1.x, x4 = lineSeg2.p2.x;
        float y1 = lineSeg1.p1.y, y2 = lineSeg1.p2.y, y3 = lineSeg2.p1.y, y4 = lineSeg2.p2.y;
        float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if ((int) d == 0) return null;
        float xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        float yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
        return new PointF(xi, yi);
    }
}
