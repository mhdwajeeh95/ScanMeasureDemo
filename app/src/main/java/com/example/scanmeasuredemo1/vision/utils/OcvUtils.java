package com.example.scanmeasuredemo1.vision.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/3/2018.
 * <p>
 * Utility Class
 */

public class OcvUtils {

    /**
     * transform byte array image to Mat object
     *
     * @param data        the input image as byte array
     * @param imageFormat the input image format type
     * @param width       width of the image
     * @param height      height of the image
     * @param rotate      rotate degree for the image
     * @return image as Mat Object
     */
    public static Mat byteImageToMat(byte[] data, int imageFormat, int width, int height, int rotate) {
        YuvImage yuv = new YuvImage(data, imageFormat, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] bytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Mat img = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, img);
        bitmap.recycle();

        Core.rotate(img, img, rotate);

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }


    /**
     * transform byte array image to Bitmap object
     *
     * @param data        the input image as byte array
     * @param imageFormat the input image format type
     * @param width       width of the image
     * @param height      height of the image
     * @param rotate      rotate angle for the image
     * @return image as Bitmap Object
     */
    public static Bitmap byteImageToBitmap(byte[] data, int imageFormat, int width, int height, int rotate) {
        YuvImage yuv = new YuvImage(data, imageFormat, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] bytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap result = rotateBitmap(bitmap, rotate);
        bitmap.recycle();
        return result;
    }

    /**
     * rotates a Bitmap image object
     *
     * @param source the bitmap
     * @param angle  rotate angle
     * @return the rotated bitmap
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /*
    transform org.opencv.core.Point  objects to android.graphics.PointF
     */
    public static List<PointF> toPointF(List<org.opencv.core.Point> points) {
        ArrayList<PointF> result = new ArrayList<>();
        for (org.opencv.core.Point p : points)
            result.add(toPointF(p));

        return result;
    }

    public static PointF toPointF(org.opencv.core.Point point) {
        return new PointF((float) point.x, (float) point.y);
    }

    /*
     transform android.graphics.PointF objects to org.opencv.core.Point
      */
    public static List<org.opencv.core.Point> toCvPoint(List<PointF> points) {
        ArrayList<org.opencv.core.Point> result = new ArrayList<>();
        for (PointF p : points)
            result.add(toCvPoint(p));

        return result;
    }

    public static org.opencv.core.Point toCvPoint(PointF point) {
        return new org.opencv.core.Point(point.x, point.y);
    }
}
