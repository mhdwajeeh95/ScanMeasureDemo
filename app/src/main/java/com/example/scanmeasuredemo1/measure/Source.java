package com.example.scanmeasuredemo1.measure;

import android.graphics.Bitmap;

import com.example.scanmeasuredemo1.model.Contour;

import java.util.List;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/4/2018.
 *
 * this class used as data holder in entire application
 *
 */

public class Source {

    // the current processed image bitmap
    public static Bitmap currentBitmap;
    // the current detected contours in image
    public static List<Contour> currentContours;

}
