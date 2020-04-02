package com.example.scanmeasuredemo1.model;

import android.graphics.PointF;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 4/29/2018.
 *
 *
 * this class holds contour data as a list of points
 *
 */

public class Contour {

    // list of points of the contour
    private List<PointF> corners;
    // the size of the scope the contour exists (size of image)
    private Size size;

    // empty constructor
    public Contour() {
        corners = null;
        size = null;
    }

    public Contour(List<PointF> corners, Size size) {
        this.corners = corners;
        this.size = size;
    }

    public List<PointF> getCorners() {
        return corners;
    }

    public void setCorners(List<PointF> corners) {
        this.corners = corners;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public boolean isEmpty() {
        if (corners == null)
            return true;
        if (corners.size() == 0)
            return true;

        return false;
    }

    /**
     * @param point
     * @return the nearest contour point to point parameter
     */
    public int findNearestCorner(PointF point) {

        double minR = Double.MAX_VALUE;
        int res = -1;
        for (int i = 0; i < corners.size(); i++) {
            PointF p = corners.get(i);
            double r = Math.pow(point.x - p.x, 2) + Math.pow(point.y - p.y, 2);
            if (r < minR) {
                minR = r;
                res = i;
            }
        }
        return res;
    }


    /**
     * transform contour to 4 point Contour by finding the points:
     * (top left , top right , bottom left , bottom right)
     */
    public void transformTo4Point() {
        List<PointF> points = corners;
        PointF p0 = Collections.min(points, (o1, o2) -> {
            double v1 = o1.x + o1.y;
            double v2 = o2.x + o2.y;
            if (Math.abs(v1 - v2) < 0.001) return 0;
            else if (v1 < v2) return -1;
            else return 1;
        });
        PointF p1 = Collections.max(points, (o1, o2) -> {
            double v1 = o1.x - o1.y;
            double v2 = o2.x - o2.y;
            if (Math.abs(v1 - v2) < 0.001) return 0;
            else if (v1 < v2) return -1;
            else return 1;
        });
        PointF p2 = Collections.max(points, (o1, o2) -> {
            double v1 = o1.x + o1.y;
            double v2 = o2.x + o2.y;
            if (Math.abs(v1 - v2) < 0.001) return 0;
            else if (v1 < v2) return -1;
            else return 1;
        });
        PointF p3 = Collections.min(points, (o1, o2) -> {
            double v1 = o1.x - o1.y;
            double v2 = o2.x - o2.y;
            if (Math.abs(v1 - v2) < 0.001) return 0;
            else if (v1 < v2) return -1;
            else return 1;
        });

        corners = Arrays.asList(p0, p1, p2, p3);
    }

    /**
     * scale this contour to another size (this will scale all the contour points to the new size)
     * @param width: width of the new size
     * @param height: height of the new size
     */
    public void scaleCorners(double width, double height) {
        if (isEmpty()) return;
        double scaleW = width / size.width, scaleH = height / size.height;
        for (PointF p : corners) {
            p.x *= scaleW;
            p.y *= scaleH;
        }
        size.width = width;
        size.height = height;
    }


    /**
     * this class holds size as width and height
     */
    public static class Size {

        public double width, height;

        public Size(double width, double height) {
            this.width = width;
            this.height = height;
        }

        public Size() {
            this(0, 0);
        }

        public Size(PointF p) {
            width = p.x;
            height = p.y;
        }

        public Size(double[] vals) {
            set(vals);
        }

        public void set(double[] vals) {
            if (vals != null) {
                width = vals.length > 0 ? vals[0] : 0;
                height = vals.length > 1 ? vals[1] : 0;
            } else {
                width = 0;
                height = 0;
            }
        }

        public double area() {
            return width * height;
        }

        public boolean empty() {
            return width <= 0 || height <= 0;
        }

        public Size clone() {
            return new Size(width, height);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(height);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(width);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof org.opencv.core.Size)) return false;
            org.opencv.core.Size it = (org.opencv.core.Size) obj;
            return width == it.width && height == it.height;
        }

        @Override
        public String toString() {
            return (int) width + "x" + (int) height;
        }

    }


}
