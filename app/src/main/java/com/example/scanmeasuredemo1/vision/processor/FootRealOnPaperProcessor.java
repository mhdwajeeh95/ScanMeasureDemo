package com.example.scanmeasuredemo1.vision.processor;

import android.graphics.PointF;

import com.example.scanmeasuredemo1.model.Contour;
import com.example.scanmeasuredemo1.model.LineSegment;
import com.example.scanmeasuredemo1.vision.utils.OcvUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/9/2018.
 */

public class FootRealOnPaperProcessor extends BaseProcessor {

    public static Double PAPER_WHITE_THRESHOLD = 100.0;
    public static Double CANNY_THRESHOLD1 = 75.0;
    public static Double CANNY_THRESHOLD2 = 200.0;
    public static Double HOUGH_RHO = 3.0;
    public static Double HOUGH_THETA = 3.0;
    public static int HOUGH_THRESHOLD = 200;

    @Override
    public List<Contour> processImage(byte[] data, int imageFormat, int width, int height) {
        return processImage(OcvUtils.byteImageToMat(data, imageFormat, width, height, Core.ROTATE_90_CLOCKWISE));
    }

    private static List<Contour> processImage(Mat previewFrame) {

        return getContours(previewFrame);
    }

    private static List<Contour> getContours(Mat src) {

        List<Contour> contourList = new ArrayList<>();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9.0, 9.0));
        Size size = new Size(src.size().width, src.size().height);
        Mat grayImage = new Mat(size, CvType.CV_8UC4);
        Mat cannedImage = new Mat(size, CvType.CV_8UC1);
        Mat dilate = new Mat(size, CvType.CV_8UC1);


        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5.0, 5.0), 0.0);

//        // get brightest pixel in image
//        Double paperColor=Core.minMaxLoc(grayImage).maxVal;
//        Double paperColorThresholdDif=30.0;
//        System.out.println("paper color"+paperColor);

        // threshold paper color
        Imgproc.threshold(grayImage, grayImage, PAPER_WHITE_THRESHOLD, 255.0, Imgproc.THRESH_BINARY);

        Imgproc.Canny(grayImage, cannedImage, CANNY_THRESHOLD1, CANNY_THRESHOLD2);
        Imgproc.dilate(cannedImage, dilate, kernel);

        List<LineSegment> houghLines = houghLines(cannedImage);
        List<PointF> houghLinesIntersection = LineSegment.linesIntersections(houghLines);


        // remove outside image points
        int idx = 0;
        while (idx < houghLinesIntersection.size()) {
            PointF p = houghLinesIntersection.get(idx);
            if (p.x < 0 || p.x > size.width || p.y < 0 || p.y > size.height)
                houghLinesIntersection.remove(idx);
            else idx++;
        }


        Contour paperContour = null;

        // remove identical points
        HashSet<PointF> set = new HashSet<>(houghLinesIntersection);
        if (set.size() >= 4) {
            paperContour = new Contour(new ArrayList<>(set), new Contour.Size(size.width, size.height));
            paperContour.transformTo4Point();
            contourList.add(paperContour);
        }


        // finding the foot contour
        if (paperContour != null) {

            // draw white border line for the paper on image
            Point p1 = OcvUtils.toCvPoint(paperContour.getCorners().get(2));
            Point p2 = OcvUtils.toCvPoint(paperContour.getCorners().get(3));
            Imgproc.line(grayImage, p1, p2, new Scalar(255.0, 255.0, 255.0), 5);

            // debug
//            if (ScanActivity.imageView != null) {
//                Mat img = grayImage;
//                Bitmap bitmap1 = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(img, bitmap1);
//                OcvUtils.rotateBitmap(bitmap1, 90);
//                ScanActivity.imageView.setImageBitmap(bitmap1);
//            }

            Imgproc.Canny(grayImage, cannedImage, CANNY_THRESHOLD1, CANNY_THRESHOLD2);
            Imgproc.dilate(cannedImage, dilate, kernel);

            ArrayList<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(dilate, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            // sort contours ascending (area wise)
            Collections.sort(contours, (o1, o2) -> {
                double a1 = Imgproc.contourArea(o1);
                double a2 = Imgproc.contourArea(o2);
                if (Math.abs(a1 - a2) < 0.001)
                    return 0;
                else if (a1 > a2) return -1;
                else return 1;
            });

            // maximum contour area to another contour area
            // this value represents a percent value
            double maxContourPercent = 0.9;

            // delete contours that is almost similar to previous
            int i = 1;
            while (i < contours.size()) {
                double a1 = Imgproc.contourArea(contours.get(i - 1));
                double a2 = Imgproc.contourArea(contours.get(i));

                if ((maxContourPercent * a1) < a2)
                    contours.remove(i);
                else
                    i++;
            }


            // calculate paper contour area
            MatOfPoint paperPoints = new MatOfPoint();
            paperPoints.fromList(OcvUtils.toCvPoint(paperContour.getCorners()));
            double paperArea = Imgproc.contourArea(paperPoints);


            // get the foot contour
            for (int index = 0; index < contours.size(); index++) {
                double footPaperPercent = 0.9;
                double currentContourArea = Imgproc.contourArea(contours.get(index));

                if (currentContourArea <= (footPaperPercent * paperArea)) {
                    MatOfPoint2f c2f = new MatOfPoint2f(contours.get(index).toArray());
                    Double peri = Imgproc.arcLength(c2f, true);
                    MatOfPoint2f approx = new MatOfPoint2f();
                    Imgproc.approxPolyDP(c2f, approx, 0.003 * peri, true);
                    List<PointF> points = OcvUtils.toPointF(Arrays.asList(approx.toArray()));

                    // check if all points inside paper
                    boolean allInside = true;
                    for (PointF footPoint : points)
                        for (PointF paperPoint : paperContour.getCorners())
                            if (Math.abs(footPoint.x - paperPoint.x) < 10 && Math.abs(footPoint.y - paperPoint.y) < 10) {
                                allInside = false;
                                break;
                            }

                    if (allInside) {
                        contourList.add(new Contour(points, new Contour.Size(size.width, size.height)));
                        break;
                    }
                }
            }

            hierarchy.release();

        }


        grayImage.release();
        cannedImage.release();
        kernel.release();
        dilate.release();
        src.release();

        return contourList;

    }

//    private static ArrayList<MatOfPoint> findContours(Mat src) {
//
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9.0, 9.0));
//        Size size = new Size(src.size().width, src.size().height);
//        Mat grayImage = new Mat(size, CvType.CV_8UC4);
//        Mat cannedImage = new Mat(size, CvType.CV_8UC1);
//        Mat dilate = new Mat(size, CvType.CV_8UC1);
//
//
//        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY);
//        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5.0, 5.0), 0.0);
//
////        // get brightest pixel in image
////        Double paperColor=Core.minMaxLoc(grayImage).maxVal;
////        Double paperColorThresholdDif=30.0;
////        System.out.println("paper color"+paperColor);
//
//        // threshold paper color
//        Imgproc.threshold(grayImage, grayImage, 100, 255.0, Imgproc.THRESH_BINARY);
//
//        Imgproc.Canny(grayImage, cannedImage, 75, 200.0);
//        Imgproc.dilate(cannedImage, dilate, kernel);
//
//        List<Pair<PointF, PointF>> houghLines = houghLines(cannedImage);
//
//        // debug
//        for (Pair<PointF, PointF> pair : houghLines) {
//            Imgproc.line(src, new Point(pair.first.x, pair.first.y), new Point(pair.second.x, pair.second.y)
//                    , new Scalar(255.0, 0.0, 0.0), 3);
//        }
//
//        // debug
//        if (ScanActivity.imageView != null) {
//            Mat img = src;
//            Bitmap bitmap1 = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(img, bitmap1);
//            OcvUtils.rotateBitmap(bitmap1, 90);
//            ScanActivity.imageView.setImageBitmap(bitmap1);
//        }
//
//
//        ArrayList<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(dilate, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        // sort contours ascending (area wise)
//        Collections.sort(contours, (o1, o2) -> {
//            double a1 = Imgproc.contourArea(o1);
//            double a2 = Imgproc.contourArea(o2);
//            if (Math.abs(a1 - a2) < 0.001)
//                return 0;
//            else if (a1 > a2) return -1;
//            else return 1;
//        });
//
//        // maximum contour area to another contour area
//        // this value represents a percent value
//        double maxContourPercent = 0.9;
//
//        // delete contours that is almost similar to previous
//        int i = 1;
//        while (i < contours.size()) {
//            double a1 = Imgproc.contourArea(contours.get(i - 1));
//            double a2 = Imgproc.contourArea(contours.get(i));
//
//            if ((maxContourPercent * a1) < a2)
//                contours.remove(i);
//            else
//                i++;
//        }
//
//
//        src.release();
//        hierarchy.release();
//        grayImage.release();
//        cannedImage.release();
//        kernel.release();
//        dilate.release();
//
//        // debug
////        Mat outMat = new Mat(size, CvType.CV_8UC1);
////        src.copyTo(outMat);
////        Imgproc.drawContours(outMat, contours, 0, new Scalar(255, 0, 0,0),5);
////
////        Bitmap bitmap1 = Bitmap.createBitmap(outMat.width(), outMat.height(), Bitmap.Config.ARGB_8888);
////        Utils.matToBitmap(outMat, bitmap1);
////
////        PreviewActivity.imv.setImageBitmap(bitmap1);
////        outMat.release();
//
//        return contours;
//    }
//
//    private static List<Contour> getCorners(ArrayList<MatOfPoint> contours, Size size) {
//        List<Contour> contourList = new ArrayList<>();
//
//        for (int index = 0; index < contours.size() && index < 2; index++) {
//            if (index == 0) {
//                MatOfPoint2f c2f = new MatOfPoint2f(contours.get(index).toArray());
//                Double peri = Imgproc.arcLength(c2f, true);
//                MatOfPoint2f approx = new MatOfPoint2f();
//                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);
//                List<Point> points = Arrays.asList(approx.toArray());
//
//                if (points.size() == 4) {
//                    Contour c = new Contour(OcvUtils.toPointF(points), new Contour.Size(size.width, size.height));
//                    c.transformTo4Point();
//                    contourList.add(c);
//                }
//            } else {
//                MatOfPoint2f c2f = new MatOfPoint2f(contours.get(index).toArray());
//                Double peri = Imgproc.arcLength(c2f, true);
//                MatOfPoint2f approx = new MatOfPoint2f();
//                Imgproc.approxPolyDP(c2f, approx, 0.003 * peri, true);
//                List<Point> points = Arrays.asList(approx.toArray());
//                contourList.add(new Contour(OcvUtils.toPointF(points), new Contour.Size(size.width, size.height)));
//            }
//
//        }
//
//        return contourList;
//    }

    private static List<LineSegment> houghLines(Mat src) {
        List<LineSegment> lineList = new ArrayList<>();

        Mat linesMat = new Mat();

        Imgproc.HoughLines(src, linesMat, HOUGH_RHO, HOUGH_THETA * Math.PI / 180, HOUGH_THRESHOLD);

        for (int i = 0; i < linesMat.rows(); i++) {
            double data[] = linesMat.get(i, 0);
            double rho1 = data[0];
            double theta1 = data[1];
            double cosTheta = Math.cos(theta1);
            double sinTheta = Math.sin(theta1);
            double x0 = cosTheta * rho1;
            double y0 = sinTheta * rho1;
            PointF pt1 = new PointF((float) (x0 + 10000 * (-sinTheta)), (float) (y0 + 10000 * cosTheta));
            PointF pt2 = new PointF((float) (x0 - 10000 * (-sinTheta)), (float) (y0 - 10000 * cosTheta));
            lineList.add(new LineSegment(pt1, pt2));
        }

        linesMat.release();

        return lineList;
    }




//    private static List<Pair<Double,Double>> houghLinesHoughSpace(Mat src) {
//        List<Pair<Double,Double>> lineList = new ArrayList<>();
//
//        Mat linesMat = new Mat();
//
//        Imgproc.HoughLines(src, linesMat, 3, 3.0*Math.PI/180, 200);
//
//        for (int i = 0; i < linesMat.rows(); i++) {
//            double data[] = linesMat.get(i,0);
//            double rho1 = data[0];
//            double theta1 = data[1];
//
//            lineList.add(new Pair<>(rho1, theta1));
//        }
//
//        linesMat.release();
//
//        return lineList;
//    }
//
//


}

