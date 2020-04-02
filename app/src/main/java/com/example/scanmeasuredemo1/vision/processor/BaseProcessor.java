package com.example.scanmeasuredemo1.vision.processor;

import com.example.scanmeasuredemo1.model.Contour;

import java.util.List;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/9/2018.
 *
 *  this class represents an image processor
 */


public abstract class BaseProcessor {

    /**
     * this method should produce a list of contours detected in the image
     * the result list varies in different inherited processors
     * @param data the input image as byte array
     * @param imageFormat the input image format type
     * @param width width of the image
     * @param height height of the image
     *
     * @return the list of detected contours in image
     */
    abstract public List<Contour> processImage(byte[] data, int imageFormat, int width, int height);
}
