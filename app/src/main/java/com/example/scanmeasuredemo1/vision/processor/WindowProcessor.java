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

public class WindowProcessor extends BaseProcessor {

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

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20.0, 20.0));
        Size size = new Size(src.size().width, src.size().height);
        Mat grayImage = new Mat(size, CvType.CV_8UC4);
        Mat cannedImage = new Mat(size, CvType.CV_8UC1);
        Mat dilate = new Mat(size, CvType.CV_8UC1);


        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5.0, 5.0), 0.0);
        Imgproc.threshold(grayImage, grayImage, 100.0, 255.0, Imgproc.THRESH_BINARY);
        // debug
//        if (ScanActivity.imageView != null) {
//            Bitmap bitmap1 = Bitmap.createBitmap(grayImage.width(), grayImage.height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(grayImage, bitmap1);
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


        src.release();
        hierarchy.release();
        grayImage.release();
        cannedImage.release();
        kernel.release();
        dilate.release();


//        Mat outMat = new Mat(size, CvType.CV_8UC1);
//        src.copyTo(outMat);
//        Imgproc.drawContours(outMat, contours, 0, new Scalar(255, 0, 0,0),5);
//
//        Bitmap bitmap1 = Bitmap.createBitmap(outMat.width(), outMat.height(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(outMat, bitmap1);
//
//        PreviewActivity.imv.setImageBitmap(bitmap1);
//        outMat.release();

        // debug
        for (MatOfPoint m : contours) {
            double a1 = Imgproc.contourArea(m);
            System.out.println(String.format("Contour Area : %.2f", a1));
        }

        return contours;
    }

    private static List<Contour> getCorners(ArrayList<MatOfPoint> contours, Size size) {
        List<Contour> contourList = new ArrayList<>();
        // todo : enhance this ***
        int indexTo;
        if (contours.size() >= 0 && contours.size() <= 5)
            indexTo = contours.size() - 1;
        else indexTo = 4;

        for (int index = 0; index <= contours.size(); index++) {
            if (index >= 0 && index <= indexTo) {
                MatOfPoint2f c2f = new MatOfPoint2f(contours.get(index).toArray());
                Double peri = Imgproc.arcLength(c2f, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);
                List<Point> points = Arrays.asList(approx.toArray());
                if (points.size() == 4) {
                    Contour c = new Contour(OcvUtils.toPointF(points), new Contour.Size(size.width, size.height));
                    c.transformTo4Point();
                    contourList.add(c);
                    return contourList;
                }
            } else
                return contourList;
        }

        return contourList;
    }


}
