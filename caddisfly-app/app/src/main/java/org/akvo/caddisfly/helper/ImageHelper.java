/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
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

public class ImageHelper {


    // http://stackoverflow.com/questions/28401343/detect-circle-in-image-using-opencv-in-android
    public static Point getCenter(@NonNull Bitmap bitmap) {

    /* convert bitmap to mat */
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);
        Mat grayMat = new Mat(bitmap.getWidth(), bitmap.getHeight(),
                CvType.CV_8UC1);

        Utils.bitmapToMat(bitmap, mat);

        /* convert to grayScale */
        int colorChannels = (mat.channels() == 3) ? Imgproc.COLOR_BGR2GRAY
                : ((mat.channels() == 4) ? Imgproc.COLOR_BGRA2GRAY : 1);

        Imgproc.cvtColor(mat, grayMat, colorChannels);

        /* reduce the noise so we avoid false circle detection */
        //Imgproc.GaussianBlur(grayMat, grayMat, new Size(9, 9), 2, 2);

        // accumulator value
        double dp = 1.2d;
        // minimum distance between the center coordinates of detected circles in pixels
        double minDist = 70;

        // min and max radii (set these values as you desire)
        int minRadius = 30, maxRadius = 70;

        // param1 = gradient value used to handle edge detection
        // param2 = Accumulator threshold value for the
        // cv2.CV_HOUGH_GRADIENT method.
        // The smaller the threshold is, the more circles will be
        // detected (including false circles).
        // The larger the threshold is, the more circles will
        // potentially be returned.
        double param1 = 10, param2 = 100;

        /* create a Mat object to store the circles detected */
        Mat circles = new Mat(bitmap.getWidth(),
                bitmap.getHeight(), CvType.CV_8UC1);

        /* find the circle in the image */
        Imgproc.HoughCircles(grayMat, circles,
                Imgproc.CV_HOUGH_GRADIENT, dp, minDist, param1,
                param2, minRadius, maxRadius);

        /* get the number of circles detected */
        int numberOfCircles = (circles.rows() == 0) ? 0 : circles.cols();

        /* draw the circles found on the image */
        if (numberOfCircles > 0) {

        /* get the circle details, circleCoordinates[0, 1, 2] = (x,y,r)
         * (x,y) are the coordinates of the circle's center
         */
            double[] circleCoordinates = circles.get(0, 0);

            int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];

            //Point center = new Point(x, y);

            org.opencv.core.Point center = new org.opencv.core.Point(x, y);
            int foundRadius = (int) circleCoordinates[2];

            /* circle's outline */
            Imgproc.circle(mat, center, foundRadius, new Scalar(0, 255, 0), 4);

            /* circle's center outline */
            //            Core.rectangle(mat, new Point(x - 5, y - 5),
            //                    new Point(x + 5, y + 5),
            //                    new Scalar(0, 128, 255), -1);

        /* convert back to bitmap */
            Utils.matToBitmap(mat, bitmap);

            return new Point((int) center.x, (int) center.y);
        }

        return null;
    }
}