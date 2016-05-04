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
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.Log;

import org.akvo.caddisfly.util.ColorUtil;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class ImageHelper {


    // http://stackoverflow.com/questions/28401343/detect-circle-in-image-using-opencv-in-android
    public static Point getCenter(int radius, @NonNull Bitmap bitmap, boolean drawPath) {

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

        Log.e("Caddisfly", String.valueOf(numberOfCircles));
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

    @SuppressWarnings("SameParameterValue")
    public static Point getCenter(int x, int y, int radius, @NonNull Bitmap image, boolean drawPath) {

        ArrayList<EdgePoint> edgePoints = new ArrayList<>();

        Point center = null;
        int sampleCount = 5;
        for (int sample = 0; sample < sampleCount; sample++) {
            edgePoints.clear();

            if (x < 0 || y < 0) {
                return null;
            }

            edgePoints.add(getEdgePoint(0, x, y, radius, image, drawPath));
            edgePoints.add(getEdgePoint(45, x, y, radius, image, drawPath));
            edgePoints.add(getEdgePoint(90, x, y, radius, image, drawPath));
            edgePoints.add(getEdgePoint(135, x, y, radius, image, drawPath));
            edgePoints.add(getEdgePoint(180, x, y, radius, image, drawPath));
            edgePoints.add(getEdgePoint(225, x, y, radius, image, drawPath));
            edgePoints.add(getEdgePoint(270, x, y, radius, image, drawPath));
            edgePoints.add(getEdgePoint(315, x, y, radius, image, drawPath));

            int longestIndex = -1;
            int length = 0;
            for (int i = 0; i < 4; i++) {
                int diameter = edgePoints.get(i).length + edgePoints.get(i + 4).length;
                if (diameter > length) {
                    length = diameter;
                    longestIndex = i;
                }
            }

            if (edgePoints.get(longestIndex).length < edgePoints.get(longestIndex + 4).length) {
                longestIndex = longestIndex + 4;
            }

            edgePoints.get(longestIndex).setLength(0);

            length = 0;
            for (int i = 0; i < 4; i++) {
                int diameter = edgePoints.get(i).length + edgePoints.get(i + 4).length;
                if (diameter > length) {
                    length = diameter;
                    longestIndex = i;
                }
            }

            if (edgePoints.get(longestIndex).length < edgePoints.get(longestIndex + 4).length) {
                longestIndex = longestIndex + 4;
            }

            edgePoints.get(longestIndex).setLength(0);

            for (int i = edgePoints.size() - 1; i >= 0; i--) {
                if (edgePoints.get(i).length == 0) {
                    edgePoints.remove(i);
                }
            }

            for (int i = 0; i < edgePoints.size(); i++) {
                if (edgePoints.get(i).length > radius) {
                    return null;
                }
            }

            if (edgePoints.size() > 4) {
                center = circleCenter(edgePoints.get(0).getPoint(),
                        edgePoints.get(2).getPoint(),
                        edgePoints.get(4).getPoint());
                x = center.x;
                y = center.y;
            }
        }

        return center;
    }

    private static EdgePoint getEdgePoint(int angle, int x, int y, int radius, @NonNull Bitmap image, boolean drawPath) {

        int counter;
        if (x >= 0 && y >= 0) {
            switch (angle) {
                case 0:
                    counter = y;
                    for (int i = y - 1; i >= Math.max(0, y - radius); i--) {
                        if (!isEdgePixel(image.getPixel(x, i))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(x, i, Color.WHITE);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(x, counter), distance(new Point(x, y), new Point(x, counter)));
                case 45:
                    counter = x;
                    int tempY = y;
                    for (int i = x; i < x + radius; i++) {
                        if (!isEdgePixel(image.getPixel(i, Math.max(0, tempY--)))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(i, Math.max(0, tempY), Color.WHITE);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(counter, tempY), distance(new Point(x, y), new Point(counter, tempY)));
                case 90:
                    counter = x;
                    for (int i = x; i < x + radius; i++) {
                        if (!isEdgePixel(image.getPixel(i, y))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(i, y, Color.WHITE);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(counter, y), distance(new Point(x, y), new Point(counter, y)));
                case 135:
                    counter = x;
                    tempY = y;
                    for (int i = x; i < x + radius; i++) {
                        if (!isEdgePixel(image.getPixel(i, tempY++))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(i, tempY, Color.WHITE);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(counter, tempY), distance(new Point(x, y), new Point(counter, tempY)));
                case 180:
                    counter = y;
                    for (int i = y; i < y + radius; i++) {
                        if (!isEdgePixel(image.getPixel(x, i))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(x, i, Color.WHITE);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(x, counter), distance(new Point(x, y), new Point(x, counter)));
                case 225:
                    counter = x;
                    tempY = y;
                    for (int i = x - 1; i >= Math.max(0, x - radius); i--) {
                        if (!isEdgePixel(image.getPixel(i, tempY++))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(i, tempY, Color.WHITE);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(counter, tempY), distance(new Point(x, y), new Point(counter, tempY)));
                case 270:
                    counter = x;
                    for (int i = x - 1; i >= Math.max(0, x - radius); i--) {
                        if (!isEdgePixel(image.getPixel(i, y))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(i, y, Color.WHITE);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(counter, y), distance(new Point(x, y), new Point(counter, y)));
                case 315:
                    counter = x;
                    tempY = y;
                    for (int i = x - 1; i >= Math.max(0, x - radius); i--) {
                        if (!isEdgePixel(image.getPixel(i, Math.max(0, tempY--)))) {
                            counter = i;
                            if (drawPath) {
                                image.setPixel(i, Math.max(0, tempY), Color.RED);
                            }
                        } else {
                            break;
                        }
                    }
                    return new EdgePoint(new Point(counter, tempY), distance(new Point(x, y), new Point(counter, tempY)));
            }
        }

        return new EdgePoint(null, 0);
    }

    private static int distance(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isEdgePixel(int color) {
        return ColorUtil.getBrightness(color) < 120;
        //return (int) ColorUtil.getColorDistanceLab(color, Color.rgb(0, 0, 0)) < 30;
    }

    //http://stackoverflow.com/questions/4103405/what-is-the-algorithm-for-finding-the-center-of-a-circle-from-three-points
    private static Point circleCenter(Point A, Point B, Point C) {

        float yDelta_a = B.y - A.y;
        float xDelta_a = B.x - A.x;
        float yDelta_b = C.y - B.y;
        float xDelta_b = C.x - B.x;
        Point center = new Point(0, 0);

        float aSlope = yDelta_a / xDelta_a;
        float bSlope = yDelta_b / xDelta_b;

        Point AB_Mid = new Point((A.x + B.x) / 2, (A.y + B.y) / 2);
        Point BC_Mid = new Point((B.x + C.x) / 2, (B.y + C.y) / 2);

        if (yDelta_a == 0)         //aSlope == 0
        {
            center.x = AB_Mid.x;
            if (xDelta_b == 0)         //bSlope == INFINITY
            {
                center.y = BC_Mid.y;
            } else {
                center.y = (int) (BC_Mid.y + (BC_Mid.x - center.x) / bSlope);
            }
        } else if (yDelta_b == 0)               //bSlope == 0
        {
            center.x = BC_Mid.x;
            if (xDelta_a == 0)             //aSlope == INFINITY
            {
                center.y = AB_Mid.y;
            } else {
                center.y = (int) (AB_Mid.y + (AB_Mid.x - center.x) / aSlope);
            }
        } else if (xDelta_a == 0)        //aSlope == INFINITY
        {
            center.y = AB_Mid.y;
            center.x = (int) (bSlope * (BC_Mid.y - center.y) + BC_Mid.x);
        } else if (xDelta_b == 0)        //bSlope == INFINITY
        {
            center.y = BC_Mid.y;
            center.x = (int) (aSlope * (AB_Mid.y - center.y) + AB_Mid.x);
        } else {
            center.x = (int) ((aSlope * bSlope * (AB_Mid.y - BC_Mid.y) - aSlope * BC_Mid.x + bSlope * AB_Mid.x) / (bSlope - aSlope));
            center.y = (int) (AB_Mid.y - (center.x - AB_Mid.x) / aSlope);
        }

        return center;
    }

    private static class EdgePoint {
        private final Point point;
        private int length;

        public EdgePoint(Point point, int length) {
            this.point = point;
            this.length = length;
        }

        public Point getPoint() {
            return point;
        }

        @SuppressWarnings("SameParameterValue")
        public void setLength(int length) {
            this.length = length;
        }
    }
}