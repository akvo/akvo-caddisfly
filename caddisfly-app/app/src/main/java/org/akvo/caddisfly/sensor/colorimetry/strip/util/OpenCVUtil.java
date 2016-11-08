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

package org.akvo.caddisfly.sensor.colorimetry.strip.util;

import android.graphics.Color;

import org.akvo.caddisfly.sensor.colorimetry.strip.model.ColorDetected;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 7/23/15
 */
@SuppressWarnings("HardCodedStringLiteral")
public final class OpenCVUtil {

    private static final int MAX_RGB_INT_VALUE = 255;
    private static final double LOWER_PERCENTAGE_BOUND = 0.9;
    private static final double UPPER_PERCENTAGE_BOUND = 1.1;

    private OpenCVUtil() {
    }

    /*
     * Computes transform matrix from one set of 4 source points to another set of 4 destination points
     * The points are ordered clockwise
     */
    private static Mat transformMatrix(double[] p1Src, double[] p2Src, double[] p3Src,
                                       double[] p4Src, double[] p1Dst, double[] p2Dst,
                                       double[] p3Dst, double[] p4Dst) {

        //source quad
        Point[] srcQuad = new Point[4];

        //destination quad corresponding with srcQuad
        Point[] dstQuad = new Point[4];

        srcQuad[0] = new Point(p1Src[0], p1Src[1]);
        srcQuad[1] = new Point(p2Src[0], p2Src[1]);
        srcQuad[2] = new Point(p3Src[0], p3Src[1]);
        srcQuad[3] = new Point(p4Src[0], p4Src[1]);

        dstQuad[0] = new Point(p1Dst[0], p1Dst[1]);
        dstQuad[1] = new Point(p2Dst[0], p2Dst[1]);
        dstQuad[2] = new Point(p3Dst[0], p3Dst[1]);
        dstQuad[3] = new Point(p4Dst[0], p4Dst[1]);

        //srcQuad and destQuad to MatOfPoint2f objects, needed in perspective transform
        MatOfPoint2f srcMat2f = new MatOfPoint2f(srcQuad);
        MatOfPoint2f dstMat2f = new MatOfPoint2f(dstQuad);

        //get a perspective transform matrix
        return Imgproc.getPerspectiveTransform(srcMat2f, dstMat2f);
    }

    public static Mat perspectiveTransform(double[] topLeft, double[] topRight,
                                           double[] bottomLeft, double[] bottomRight, Mat bgr) {

        // determine the size of the destination Mat: use the positions of the finder patterns to determine the width and height.
        // look out: the horizontal direction now refers again to the actual calibration card
        int verSize = (int) Math.round(Math.sqrt(Math.pow((topLeft[0] - topRight[0]), 2) + Math.pow((topLeft[1] - topRight[1]), 2)));
        int horSize = (int) Math.round(Math.sqrt(Math.pow((topLeft[0] - bottomLeft[0]), 2) + Math.pow((topLeft[1] - bottomLeft[1]), 2)));

        // we rotate the resulting image, so we go from a portrait view to the regular calibration card in landscape
        // so the mapping is:
        // top left source => top right destination
        // top right source => bottom right destination
        // bottom right source => bottom left destination
        // bottom left source => top left destination

        double[] trDest = new double[]{horSize - 1, 0};
        double[] brDest = new double[]{horSize - 1, verSize - 1};
        double[] blDest = new double[]{0, verSize - 1};
        double[] tlDest = new double[]{0, 0};

        Mat transformMatrix = transformMatrix(topLeft, topRight, bottomRight, bottomLeft, trDest, brDest, blDest, tlDest);

        //make a destination mat for a warp
        Mat warpMat = Mat.zeros(verSize, horSize, bgr.type());

        //do the warp
        Imgproc.warpPerspective(bgr, warpMat, transformMatrix, warpMat.size());
        return warpMat;
    }

    // detect strip by multi-step method
    // returns cut-out and rotated resulting strip as mat
    @SuppressWarnings("UnusedParameters")
    public static Mat detectStrip(Mat stripArea, StripTest.Brand brand, double ratioW, double ratioH) {
        List<Mat> channels = new ArrayList<>();
        Mat sArea = stripArea.clone();

        // Gaussian blur
        Imgproc.medianBlur(sArea, sArea, 3);
        Core.split(sArea, channels);

        // create binary image
        Mat binary = new Mat();

        // determine min and max NOT USED
        Imgproc.threshold(channels.get(0), binary, 128, MAX_RGB_INT_VALUE, Imgproc.THRESH_BINARY);

        // compute first approximation of line through length of the strip
        final WeightedObservedPoints points = new WeightedObservedPoints();
        final WeightedObservedPoints corrPoints = new WeightedObservedPoints();

        double tot, yTot;
        for (int i = 0; i < binary.cols(); i++) { // iterate over cols
            tot = 0;
            yTot = 0;
            for (int j = 0; j < binary.rows(); j++) { // iterate over rows
                if (binary.get(j, i)[0] > 128) {
                    yTot += j;
                    tot++;
                }
            }
            if (tot > 0) {
                points.add((double) i, yTot / tot);
            }
        }

        // order of coefficients is (b + ax), so [b, a]
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        List<WeightedObservedPoint> pointsList = points.toList();
        final double[] coefficient = fitter.fit(pointsList);

        // second pass, remove outliers
        double estimate, actual;

        for (int i = 0; i < pointsList.size(); i++) {
            estimate = coefficient[1] * pointsList.get(i).getX() + coefficient[0];
            actual = pointsList.get(i).getY();
            if (actual > LOWER_PERCENTAGE_BOUND * estimate && actual < UPPER_PERCENTAGE_BOUND * estimate) {
                //if the point differs less than +/- 10%, keep the point
                corrPoints.add(pointsList.get(i).getX(), pointsList.get(i).getY());
            }
        }

        final double[] coefficientCorr = fitter.fit(corrPoints.toList());
        double slope = coefficientCorr[1];
        double offset = coefficientCorr[0];

        // compute rotation angle
        double rotAngleDeg = Math.atan(slope) * 180 / Math.PI;

        //determine a point on the line, in the middle of strip, in the horizontal middle of the whole image
        int midPointX = binary.cols() / 2;
        int midPointY = (int) Math.round(midPointX * slope + offset);

        // rotate around the midpoint, to straighten the binary strip
        Mat dstBinary = new Mat(binary.rows(), binary.cols(), binary.type());
        Point center = new Point(midPointX, midPointY);
        Mat rotMat = Imgproc.getRotationMatrix2D(center, rotAngleDeg, 1.0);
        Imgproc.warpAffine(binary, dstBinary, rotMat, binary.size(), Imgproc.INTER_CUBIC + Imgproc.WARP_FILL_OUTLIERS);

        // also apply rotation to colored strip
        Mat dstStrip = new Mat(stripArea.rows(), stripArea.cols(), stripArea.type());
        Imgproc.warpAffine(stripArea, dstStrip, rotMat, binary.size(), Imgproc.INTER_CUBIC + Imgproc.WARP_FILL_OUTLIERS);

        // Compute white points in each row
        double[] rowCount = new double[dstBinary.rows()];
        int rowTot;
        for (int i = 0; i < dstBinary.rows(); i++) { // iterate over rows
            rowTot = 0;
            for (int j = 0; j < dstBinary.cols(); j++) { // iterate over cols
                if (dstBinary.get(i, j)[0] > 128) {
                    rowTot++;
                }
            }
            rowCount[i] = rowTot;
        }

        // find width by finding rising and dropping edges
        // rising edge  = largest positive difference
        // falling edge = largest negative difference
        int risePos = 0;
        int fallPos = 0;
        double riseVal = 0;
        double fallVal = 0;
        for (int i = 0; i < dstBinary.rows() - 1; i++) {
            if (rowCount[i + 1] - rowCount[i] > riseVal) {
                riseVal = rowCount[i + 1] - rowCount[i];
                risePos = i + 1;
            }
            if (rowCount[i + 1] - rowCount[i] < fallVal) {
                fallVal = rowCount[i + 1] - rowCount[i];
                fallPos = i;
            }
        }

        // cut out binary strip
        Point stripTopLeft = new Point(0, risePos);
        Point stripBottomRight = new Point(dstBinary.cols(), fallPos);

        org.opencv.core.Rect stripAreaRect = new org.opencv.core.Rect(stripTopLeft, stripBottomRight);
        Mat binaryStrip = dstBinary.submat(stripAreaRect);

        // also cut out colored strip
        Mat colorStrip = dstStrip.submat(stripAreaRect);

        // now right end of strip
        // method: first rising edge

        double[] colCount = new double[binaryStrip.cols()];
        int colTotal;
        for (int i = 0; i < binaryStrip.cols(); i++) { // iterate over cols
            colTotal = 0;
            for (int j = 0; j < binaryStrip.rows(); j++) { // iterate over rows
                if (binaryStrip.get(j, i)[0] > 128) {
                    colTotal++;
                }
            }

            //Log.d("Caddisfly", String.valueOf(colTotal));
            colCount[i] = colTotal;
        }

        stripAreaRect = getStripRectangle(binaryStrip, colCount, brand.getStripLength(), ratioW);

        Mat resultStrip = colorStrip.submat(stripAreaRect).clone();

        // release Mat objects
        stripArea.release();
        sArea.release();
        binary.release();
        dstBinary.release();
        dstStrip.release();
        binaryStrip.release();
        colorStrip.release();

        return resultStrip;
    }

    private static Rect getStripRectangle(Mat binaryStrip, double[] colCount, double stripLength, double ratioW) {
        // threshold is that half of the rows in a column should be white
        int threshold = binaryStrip.rows() / 2;

        boolean found = false;

        // moving from the left, determine the first point that crosses the threshold
        double posLeft = 0;
        while (!found && posLeft < binaryStrip.cols()) {
            if (colCount[(int) posLeft] > threshold) {
                found = true;
            } else {
                posLeft++;
            }
        }
        //use known length of strip to determine right side
        double posRight = posLeft + (stripLength * ratioW);

        found = false;
        // moving from the right, determine the first point that crosses the threshold
        int posRightTemp = binaryStrip.cols() - 1;
        while (!found && posRightTemp > 0) {
            if (colCount[posRightTemp] > threshold) {
                found = true;
            } else {
                posRightTemp--;
            }
        }

        // if there is a big difference in the right position determined by the two above methods
        // then ignore the first method above and determine the left position by second method only
        if (Math.abs(posRightTemp - posRight) > 5) {
            // use known length of strip to determine left side
            posLeft = posRightTemp - (stripLength * ratioW);
            posRight = posRightTemp;
        }

        // cut out final strip
        Point stripTopLeft = new Point(posLeft, 0);
        Point stripBottomRight = new Point(posRight, binaryStrip.rows());

        return new Rect(stripTopLeft, stripBottomRight);
    }

    static ColorDetected detectStripPatchColor(Mat lab) {
        // compute mean lab color. This is the value that will be
        // used for the result computation
        Scalar mean = Core.mean(lab);
        ColorDetected colorDetected = new ColorDetected();
        colorDetected.setLab(mean);

        // compute rgb color. This will be used for display only.
        Mat rgb = new Mat();
        Imgproc.cvtColor(lab, rgb, Imgproc.COLOR_Lab2RGB);
        mean = Core.mean(rgb);

        int color = Color.rgb((int) Math.round(mean.val[0]), (int) Math.round(mean.val[1]),
                (int) Math.round(mean.val[2]));

        colorDetected.setColor(color);
        return colorDetected;
    }
}
