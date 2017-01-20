/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.helper;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.annotation.NonNull;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public final class ImageHelper {

    private static final Scalar COLOR_GREEN = new Scalar(0, 255, 0);
    private static final int MIN_RADIUS = 30;
    private static final int MAX_RADIUS = 70;
    // minimum distance between the center coordinates of detected circles in pixels
    private static final int MIN_CIRCLE_CENTER_DISTANCE = 70;
    private static final double RESOLUTION_INVERSE_RATIO = 1.2d;

    private ImageHelper() {
    }

    // http://stackoverflow.com/questions/28401343/detect-circle-in-image-using-opencv-in-android
    /**
     * Gets the center of the backdrop in the test chamber
     *
     * @param bitmap the photo to analyse
     * @return the center point of the found circle
     */
    public static Point getCenter(@NonNull Bitmap bitmap) {

        // convert bitmap to mat
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        Mat grayMat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);

        Utils.bitmapToMat(bitmap, mat);

        // convert to grayScale
        int colorChannels = (mat.channels() == 3) ? Imgproc.COLOR_BGR2GRAY
                : ((mat.channels() == 4) ? Imgproc.COLOR_BGRA2GRAY : 1);

        Imgproc.cvtColor(mat, grayMat, colorChannels);

        // reduce the noise so we avoid false circle detection
        //Imgproc.GaussianBlur(grayMat, grayMat, new Size(9, 9), 2, 2);

        // param1 = gradient value used to handle edge detection
        // param2 = Accumulator threshold value for the
        // cv2.CV_HOUGH_GRADIENT method.
        // The smaller the threshold is, the more circles will be
        // detected (including false circles).
        // The larger the threshold is, the more circles will
        // potentially be returned.
        double param1 = 10, param2 = 100;

        // create a Mat object to store the circles detected
        Mat circles = new Mat(bitmap.getWidth(),
                bitmap.getHeight(), CvType.CV_8UC1);

        // find the circle in the image
        Imgproc.HoughCircles(grayMat, circles,
                Imgproc.CV_HOUGH_GRADIENT, RESOLUTION_INVERSE_RATIO, (double) MIN_CIRCLE_CENTER_DISTANCE, param1,
                param2, MIN_RADIUS, MAX_RADIUS);

        int numberOfCircles = (circles.rows() == 0) ? 0 : circles.cols();

        // draw the circles found on the image
        if (numberOfCircles > 0) {

            double[] circleCoordinates = circles.get(0, 0);

            int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];

            org.opencv.core.Point center = new org.opencv.core.Point(x, y);
            int foundRadius = (int) circleCoordinates[2];

            // circle outline
            Imgproc.circle(mat, center, foundRadius, COLOR_GREEN, 4);

            Utils.matToBitmap(mat, bitmap);

            return new Point((int) center.x, (int) center.y);
        }

        return null;
    }
}
