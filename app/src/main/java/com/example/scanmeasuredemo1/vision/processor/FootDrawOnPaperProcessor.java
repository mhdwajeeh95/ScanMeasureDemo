package com.example.scanmeasuredemo1.vision.processor;

import com.example.scanmeasuredemo1.model.Contour;
import com.example.scanmeasuredemo1.vision.utils.OcvUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/9/2018.
 */

public class FootDrawOnPaperProcessor extends BaseProcessor {

    @Override
    public List<Contour> processImage(byte[] data, int imageFormat, int width, int height) {
        return processImage(OcvUtils.byteImageToMat(data, imageFormat, width, height, Core.ROTATE_90_CLOCKWISE));
    }

    private static List<Contour> processImage(Mat previewFrame) {
        // preserve Size object because of previewFrame.release() in findContours(previewFrame)
        Size size = previewFrame.size().clone();

        ArrayList<MatOfPoint> contours = findContours(previewFrame);
        return getCorners(contours, size);
    }

    private static ArrayList<MatOfPoint> findContours(Mat src) {

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9.0, 9.0));
        Size size = new Size(src.size().width, src.size().height);
        Mat grayImage = new Mat(size, CvType.CV_8UC4);
        Mat cannedImage = new Mat(size, CvType.CV_8UC1);
        Mat dilate = new Mat(size, CvType.CV_8UC1);


        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5.0, 5.0), 0.0);
        Imgproc.threshold(grayImage, grayImage, 100, 255.0, Imgproc.THRESH_BINARY);

        // debug
//        if (ScanActivity.imageView != null) {
//            Mat img = grayImage;
//            Bitmap bitmap1 = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(img, bitmap1);
//            OcvUtils.rotateBitmap(bitmap1, 90);
//            ScanActivity.imageView.setImageBitmap(bitmap1);
//        }
        Imgproc.Canny(grayImage, cannedImage, 75, 200.0);

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


        src.release();
        hierarchy.release();
        grayImage.release();
        cannedImage.release();
        kernel.release();
        dilate.release();

        // debug
//        Mat outMat = new Mat(size, CvType.CV_8UC1);
//        src.copyTo(outMat);
//        Imgproc.drawContours(outMat, contours, 0, new Scalar(255, 0, 0,0),5);
//
//        Bitmap bitmap1 = Bitmap.createBitmap(outMat.width(), outMat.height(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(outMat, bitmap1);
//
//        PreviewActivity.imv.setImageBitmap(bitmap1);
//        outMat.release();

        return contours;
    }

    private static List<Contour> getCorners(ArrayList<MatOfPoint> contours, Size size) {
        List<Contour> contourList = new ArrayList<>();

        for (int index = 0; index < contours.size() && index < 2; index++) {
            if (index == 0) {
                MatOfPoint2f c2f = new MatOfPoint2f(contours.get(index).toArray());
                Double peri = Imgproc.arcLength(c2f, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);
                List<Point> points = Arrays.asList(approx.toArray());

                if (points.size() == 4) {
                    Contour c = new Contour(OcvUtils.toPointF(points), new Contour.Size(size.width, size.height));
                    c.transformTo4Point();
                    contourList.add(c);
                }
            } else {
                MatOfPoint2f c2f = new MatOfPoint2f(contours.get(index).toArray());
                Double peri = Imgproc.arcLength(c2f, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(c2f, approx, 0.003 * peri, true);
                List<Point> points = Arrays.asList(approx.toArray());
                contourList.add(new Contour(OcvUtils.toPointF(points), new Contour.Size(size.width, size.height)));
            }

        }

        return contourList;
    }


}
