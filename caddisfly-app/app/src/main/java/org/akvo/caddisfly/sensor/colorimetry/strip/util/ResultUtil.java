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

package org.akvo.caddisfly.sensor.colorimetry.strip.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.ColorDetected;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Various methods for result calculation.
 */
public final class ResultUtil {

    private static final int BORDER_SIZE = 10;
    private static final int MEASURE_LINE_HEIGHT = 55;
    private static final int MIN_COLOR_LABEL_WIDTH = 50;
    private static final int COLOR_INDICATOR_SIZE = 40;
    private static final double TITLE_FONT_SIZE = 0.8d;
    private static final double NORMAL_FONT_SCALE = 0.6d;
    private static final int MAX_RGB_INT_VALUE = 255;
    private static final double LAB_COLOR_NORMAL_DIVISOR = 2.55;
    private static final int INTERPOLATION_NUMBER = 10;
    private static final Scalar LAB_WHITE = new Scalar(255, 128, 128);
    private static final Scalar LAB_GREY = new Scalar(128, 128, 128);
    private static final double Y_COLOR_RECT = 5d; //distance from top Mat to top color rectangles
    private static final int CIRCLE_RADIUS = 20;
    private static final double X_MARGIN = 10d;
    private static final double MIN_STRIP_WIDTH = 200d;
    private static final double MAX_STRIP_HEIGHT = 80d;
    private static final double MEASURE_LINE_TOP_MARGIN = 5d;
    private static final double SINGLE_MEASURE_LINE_TOP_MARGIN = 27d;
    private static final Scalar DARK_GRAY = new Scalar(50, 50, 50);
    private static final int ARROW_TRIANGLE_LENGTH = 15;
    private static final int ARROW_TRIANGLE_HEIGHT = 20;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final int HORIZONTAL_MARGIN = 50;

    //
    private ResultUtil() {
    }

    public static Mat getMatFromFile(Context context, int patchId) {

        String fileName = null;

        try {
            // Get the correct file from the patch id
            String json = FileUtil.readFromInternalStorage(context, Constant.IMAGE_PATCH);
            JSONArray imagePatchArray = new JSONArray(json);
            for (int i = 0; i < imagePatchArray.length(); i++) {
                JSONArray array = imagePatchArray.getJSONArray(i);
                if (array.getInt(1) == patchId) {
                    fileName = Constant.STRIP + array.getInt(0);
                    break;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        //if in DetectStripTask, no strip was found, an image was saved with the String Constant.ERROR
        if (FileUtil.fileExists(context, fileName + Constant.ERROR)) {
            fileName += Constant.ERROR;
        }

        // read the Mat object from internal storage
        byte[] data;
        try {
            data = FileUtil.readByteArray(context, fileName);

            if (data != null) {
                // determine cols and rows dimensions
                byte[] rows = new byte[4];
                byte[] cols = new byte[4];

                int length = data.length;
                System.arraycopy(data, length - 8, rows, 0, 4);
                System.arraycopy(data, length - 4, cols, 0, 4);

                int rowsNum = FileUtil.byteArrayToLeInt(rows);
                int colsNum = FileUtil.byteArrayToLeInt(cols);

                // remove last part
                byte[] imgData = Arrays.copyOfRange(data, 0, data.length - 8);

                // reserve Mat of proper size:
                Mat result = new Mat(rowsNum, colsNum, CvType.CV_8UC3);

                // put image data back in Mat:
                result.put(0, 0, imgData);
                return result;
            }
        } catch (IOException e) {
            Timber.e(e);
        }
        return null;
    }

    public static Bitmap makeBitmap(@NonNull Mat mat) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);

            //double max = bitmap.getHeight() > bitmap.getWidth() ? bitmap.getHeight() : bitmap.getWidth();
            //double min = bitmap.getHeight() < bitmap.getWidth() ? bitmap.getHeight() : bitmap.getWidth();
            //double ratio = min / max;
            //int width = (int) Math.max(600, max);
            //int height = (int) Math.round(ratio * width);

            return Bitmap.createScaledBitmap(bitmap, mat.width(), mat.height(), false);

        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @NonNull
    public static Mat createStripMat(@NonNull Mat mat, @NonNull Point centerPatch, boolean grouped, int maxWidth) {
        //done with lab schema, make rgb to show in image view
        // mat holds the strip image
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);

        int rightBorder = 0;

        double ratio;
        if (mat.width() < MIN_STRIP_WIDTH) {
            ratio = MAX_STRIP_HEIGHT / mat.height();
            rightBorder = BORDER_SIZE;
        } else {
            ratio = (double) (maxWidth - 10) / (double) mat.width();
        }

        Imgproc.resize(mat, mat, new Size(mat.width() * ratio, mat.height() * ratio));

        Core.copyMakeBorder(mat, mat, BORDER_SIZE + ARROW_TRIANGLE_HEIGHT, BORDER_SIZE * 2, 0, rightBorder, Core.BORDER_CONSTANT,
                new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

        // Draw an arrow to the patch
        // only draw if this is not a 'grouped' strip
        if (!grouped) {
            double x = centerPatch.x * ratio;
            double y = 0;
            MatOfPoint matOfPoint = new MatOfPoint(
                    new Point((x - ARROW_TRIANGLE_LENGTH), y),
                    new Point((x + ARROW_TRIANGLE_LENGTH), y),
                    new Point(x, y + ARROW_TRIANGLE_HEIGHT),
                    new Point((x - ARROW_TRIANGLE_LENGTH), y));

            Imgproc.fillConvexPoly(mat, matOfPoint, DARK_GRAY, Core.LINE_AA, 0);
        }
        return mat;
    }

    @NonNull
    public static Mat createDescriptionMat(String desc, int width) {
        int[] baseline = new int[1];
        Size textSizeDesc = Imgproc.getTextSize(desc, Core.FONT_HERSHEY_SIMPLEX, TITLE_FONT_SIZE, 1, baseline);
        Mat descMat = new Mat((int) Math.ceil(textSizeDesc.height) * 3, width, CvType.CV_8UC3, LAB_WHITE);
        Imgproc.putText(descMat, desc, new Point(2, descMat.height() - textSizeDesc.height),
                Core.FONT_HERSHEY_SIMPLEX, TITLE_FONT_SIZE, LAB_GREY, 2, Core.LINE_AA, false);

        return descMat;
    }

    /**
     * Create Mat with swatches for the colors in the color chart range and also write the value.
     *
     * @param colors the colors to draw
     * @param width  the final width of the Mat
     * @return the created Mat
     */
    @NonNull
    public static Mat createColorRangeMatSingle(@NonNull JSONArray colors, int width) {

        double gutterWidth = X_MARGIN;
        if (colors.length() > 10) {
            gutterWidth = 2d;
            width -= 10;
        }

        double xTranslate = (double) width / (double) colors.length();

        Mat colorRangeMat = new Mat((int) xTranslate + MEASURE_LINE_HEIGHT, width, CvType.CV_8UC3, LAB_WHITE);

        double previousPos = 0;
        for (int d = 0; d < colors.length(); d++) {
            try {

                JSONObject colorObj = colors.getJSONObject(d);

                double value = colorObj.getDouble(SensorConstants.VALUE);
                JSONArray lab = colorObj.getJSONArray(SensorConstants.LAB);
                Scalar scalarLab = new Scalar((lab.getDouble(0) / 100) * MAX_RGB_INT_VALUE,
                        lab.getDouble(1) + 128, lab.getDouble(2) + 128);

                //draw a rectangle filled with color for result value
                Point topLeft = new Point(xTranslate * d, Y_COLOR_RECT);
                Point bottomRight = new Point(topLeft.x + xTranslate - gutterWidth, Y_COLOR_RECT + xTranslate);
                Imgproc.rectangle(colorRangeMat, topLeft, bottomRight, scalarLab, -1);

                Size textSizeValue = Imgproc.getTextSize(DECIMAL_FORMAT.format(value),
                        Core.FONT_HERSHEY_SIMPLEX, NORMAL_FONT_SCALE, 2, null);
                double x = topLeft.x + (bottomRight.x - topLeft.x) / 2 - textSizeValue.width / 2;
                //draw color value below rectangle. Skip if too close to the previous label
                if (x > previousPos + MIN_COLOR_LABEL_WIDTH || d == 0) {
                    previousPos = x;

                    String label;
                    //no decimal places if too many labels to fit
                    if (colors.length() > 10) {
                        label = String.format(Locale.getDefault(), "%.0f", value);
                    } else {
                        label = DECIMAL_FORMAT.format(value);
                    }

                    //adjust x if too close to edge
                    if (x + textSizeValue.width > colorRangeMat.width()) {
                        x = colorRangeMat.width() - textSizeValue.width;
                    }

                    Imgproc.putText(colorRangeMat, label,
                            new Point(x, Y_COLOR_RECT + xTranslate + textSizeValue.height + BORDER_SIZE),
                            Core.FONT_HERSHEY_SIMPLEX,
                            NORMAL_FONT_SCALE, LAB_GREY, 2, Core.LINE_AA, false);
                }

            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return colorRangeMat;
    }

    /**
     * Create Mat to hold a rectangle for each color with the corresponding value.
     *
     * @param patches the patches on the strip
     * @param width   the width of the Mat to be returned
     * @return the Mat with the color range
     */
    @NonNull
    public static Mat createColorRangeMatGroup(@NonNull List<StripTest.Brand.Patch> patches, int width) {

        // vertical size of mat: size of color block - X_MARGIN + top distance

        double xTranslate = (double) width / (double) patches.get(0).getColors().length();
        int numPatches = patches.size();
        Mat colorRangeMat = new Mat((int) Math.ceil(numPatches * (xTranslate + X_MARGIN) - X_MARGIN),
                width, CvType.CV_8UC3, LAB_WHITE);

        JSONArray colors;
        int offset = 0;
        for (int p = 0; p < numPatches; p++) {
            colors = patches.get(p).getColors();
            for (int d = 0; d < colors.length(); d++) {
                try {

                    JSONObject colorObj = colors.getJSONObject(d);

                    double value = colorObj.getDouble(SensorConstants.VALUE);
                    JSONArray lab = colorObj.getJSONArray(SensorConstants.LAB);
                    Scalar scalarLab = new Scalar((lab.getDouble(0) / 100) * MAX_RGB_INT_VALUE,
                            lab.getDouble(1) + 128, lab.getDouble(2) + 128);

                    //draw a rectangle filled with color for result value
                    Point topLeft = new Point(xTranslate * d, offset);
                    Point bottomRight = new Point(topLeft.x + xTranslate - X_MARGIN, xTranslate + offset - X_MARGIN);
                    Imgproc.rectangle(colorRangeMat, topLeft, bottomRight, scalarLab, -1);

                    //draw color value below rectangle
                    if (p == 0) {
                        Size textSizeValue = Imgproc.getTextSize(DECIMAL_FORMAT.format(value), Core.FONT_HERSHEY_SIMPLEX,
                                NORMAL_FONT_SCALE, 1, null);
                        Point centerText = new Point(topLeft.x + (bottomRight.x - topLeft.x) / 2 - textSizeValue.width / 2,
                                colorRangeMat.height() - textSizeValue.height);
                        Imgproc.putText(colorRangeMat, DECIMAL_FORMAT.format(value), centerText,
                                Core.FONT_HERSHEY_SIMPLEX, NORMAL_FONT_SCALE, LAB_GREY, 2, Core.LINE_AA, false);
                    }

                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
            offset += xTranslate;
        }
        return colorRangeMat;
    }

    /**
     * Create a Mat to show the point at which the matched color occurs.
     *
     * @param colors        the range of colors
     * @param result        the result
     * @param colorDetected the colors extracted from the patch
     * @param width         the width of the mat to be returned
     * @return the Mat with the point or arrow drawn
     */
    @NonNull
    public static Mat createValueMeasuredMatSingle(@NonNull JSONArray colors, double result,
                                                   @NonNull ColorDetected colorDetected, int width) {

        Mat mat = new Mat(MEASURE_LINE_HEIGHT, width, CvType.CV_8UC3, LAB_WHITE);
        double xTranslate = (double) width / (double) colors.length();

        try {
            // determine where the circle should be placed
            for (int d = 0; d < colors.length(); d++) {

                double nextValue = colors.getJSONObject(Math.min(d + 1, colors.length() - 1)).getDouble(SensorConstants.VALUE);

                if (result <= nextValue) {

                    Scalar resultColor = colorDetected.getLab();
                    double value = colors.getJSONObject(d).getDouble(SensorConstants.VALUE);

                    //calculate number of pixels needed to translate in x direction
                    double transX = xTranslate * ((result - value) / (nextValue - value));
                    double left = xTranslate * d;
                    double right = left + xTranslate - X_MARGIN;

                    Point circleCenter = new Point(Math.max(10d, left + (right - left) / 2 + transX),
                            SINGLE_MEASURE_LINE_TOP_MARGIN);

                    MatOfPoint matOfPoint = new MatOfPoint(
                            new Point((circleCenter.x - ARROW_TRIANGLE_LENGTH), circleCenter.y + ARROW_TRIANGLE_LENGTH - 2),
                            new Point((circleCenter.x + ARROW_TRIANGLE_LENGTH), circleCenter.y + ARROW_TRIANGLE_LENGTH - 2),
                            new Point(circleCenter.x, (circleCenter.y + ARROW_TRIANGLE_LENGTH * 2) - 2),
                            new Point((circleCenter.x - ARROW_TRIANGLE_LENGTH), circleCenter.y + ARROW_TRIANGLE_LENGTH - 2));

                    Imgproc.fillConvexPoly(mat, matOfPoint, resultColor);

                    Imgproc.circle(mat, circleCenter, CIRCLE_RADIUS, resultColor, -1, Imgproc.LINE_AA, 0);

                    break;
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        return mat;
    }

    /**
     * Create a Mat to show the point at which the matched color occurs for group patch test.
     *
     * @param colors         the range of colors
     * @param result         the result
     * @param colorsDetected the colors extracted from the patch
     * @param width          the width of the mat to be returned
     * @return the Mat with the point or arrow drawn
     */
    @NonNull
    public static Mat createValueMeasuredMatGroup(@NonNull JSONArray colors, double result,
                                                  @NonNull ColorDetected[] colorsDetected, int width) {
        int height = COLOR_INDICATOR_SIZE * colorsDetected.length;
        Mat valueMeasuredMat = new Mat(height, width, CvType.CV_8UC3, LAB_WHITE);
        double xTranslate = (double) width / (double) colors.length();

        try {

            // determine where the circle should be placed
            for (int d = 0; d < colors.length(); d++) {

                double nextValue = colors.getJSONObject(Math.min(d + 1, colors.length() - 1)).getDouble(SensorConstants.VALUE);

                Scalar resultColor = null;
                if (result < nextValue) {

                    double value = colors.getJSONObject(d).getDouble(SensorConstants.VALUE);

                    //calculate number of pixels needed to translate in x direction
                    double transX = xTranslate * ((result - value) / (nextValue - value));

                    double left = xTranslate * d;
                    double right = left + xTranslate - X_MARGIN;
                    Point point = (transX) + xTranslate * d < X_MARGIN
                            ? new Point(X_MARGIN, MEASURE_LINE_TOP_MARGIN)
                            : new Point(left + (right - left) / 2 + transX, MEASURE_LINE_TOP_MARGIN);

                    double offset = 5;
                    for (ColorDetected aColorsDetected : colorsDetected) {
                        resultColor = aColorsDetected.getLab();

                        Imgproc.rectangle(valueMeasuredMat,
                                new Point(point.x - ARROW_TRIANGLE_LENGTH, point.y + offset),
                                new Point(point.x + ARROW_TRIANGLE_LENGTH, point.y + (ARROW_TRIANGLE_LENGTH * 2) + offset),
                                resultColor, -1, Imgproc.LINE_AA, 0);

                        offset += 2 * ARROW_TRIANGLE_LENGTH;
                    }

                    MatOfPoint matOfPoint = new MatOfPoint(
                            new Point((point.x - ARROW_TRIANGLE_LENGTH), point.y + offset),
                            new Point((point.x + ARROW_TRIANGLE_LENGTH), point.y + offset),
                            new Point(point.x, point.y + ARROW_TRIANGLE_LENGTH + offset),
                            new Point((point.x - ARROW_TRIANGLE_LENGTH), point.y + offset));

                    Imgproc.fillConvexPoly(valueMeasuredMat, matOfPoint, resultColor);

                    break;
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }

        return valueMeasuredMat;
    }

    @NonNull
    public static Mat concatenate(@NonNull Mat m1, @NonNull Mat m2) {
        int width = Math.max(m1.cols(), m2.cols());
        int height = m1.rows() + m2.rows();

        Mat result = new Mat(height, width, CvType.CV_8UC3,
                new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

        // rect works with x, y, width, height
        Rect roi1 = new Rect(0, 0, m1.cols(), m1.rows());
        Mat roiMat1 = result.submat(roi1);
        m1.copyTo(roiMat1);

        Rect roi2 = new Rect(0, m1.rows(), m2.cols(), m2.rows());
        Mat roiMat2 = result.submat(roi2);
        m2.copyTo(roiMat2);

        return result;
    }

    @NonNull
    public static Mat concatenateHorizontal(@NonNull Mat m1, @NonNull Mat m2) {
        int width = m1.cols() + m2.cols() + HORIZONTAL_MARGIN;
        int height = Math.max(m1.rows(), m2.rows());

        Mat result = new Mat(height, width, CvType.CV_8UC3,
                new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

        // rect works with x, y, width, height
        Rect roi1 = new Rect(0, 0, m1.cols(), m1.rows());
        Mat roiMat1 = result.submat(roi1);
        m1.copyTo(roiMat1);

        Rect roi2 = new Rect(m1.cols() + HORIZONTAL_MARGIN, 0, m2.cols(), m2.rows());
        Mat roiMat2 = result.submat(roi2);
        m2.copyTo(roiMat2);

        return result;
    }

    public static Mat getPatch(@NonNull Mat mat, @NonNull Point patchCenter, int subMatSize) {

        //make a subMat around center of the patch
        int minRow = (int) Math.round(Math.max(patchCenter.y - subMatSize, 0));
        int maxRow = (int) Math.round(Math.min(patchCenter.y + subMatSize, mat.height()));
        int minCol = (int) Math.round(Math.max(patchCenter.x - subMatSize, 0));
        int maxCol = (int) Math.round(Math.min(patchCenter.x + subMatSize, mat.width()));

        //  create subMat
        return mat.submat(minRow, maxRow, minCol, maxCol).clone();
    }

    public static ColorDetected getPatchColor(@NonNull Mat mat, @NonNull Point patchCenter, int subMatSize) {

        //make a subMat around center of the patch
        int minRow = (int) Math.round(Math.max(patchCenter.y - subMatSize, 0));
        int maxRow = (int) Math.round(Math.min(patchCenter.y + subMatSize, mat.height()));
        int minCol = (int) Math.round(Math.max(patchCenter.x - subMatSize, 0));
        int maxCol = (int) Math.round(Math.min(patchCenter.x + subMatSize, mat.width()));

        //  create subMat
        Mat patch = mat.submat(minRow, maxRow, minCol, maxCol);

        // compute the mean color and return it
        return OpenCVUtil.detectStripPatchColor(patch);
    }

    /*
   * Restricts number of significant digits depending on size of number
    */
    public static double roundSignificant(double value) {
        if (value < 1.0) {
            return Math.round(value * 100) / 100.0;
        } else {
            return Math.round(value * 10) / 10.0;
        }
    }

    @NonNull
    private static double[][] createInterpolTable(@NonNull JSONArray colors) {
        JSONArray patchColorValues;
        double resultPatchValueStart, resultPatchValueEnd;
        double[] pointStart;
        double[] pointEnd;
        double lInter, aInter, bInter, vInter;
        double[][] interpolTable = new double[(colors.length() - 1) * INTERPOLATION_NUMBER + 1][4];
        int count = 0;

        for (int i = 0; i < colors.length() - 1; i++) {
            try {
                patchColorValues = colors.getJSONObject(i).getJSONArray(SensorConstants.LAB);
                resultPatchValueStart = colors.getJSONObject(i).getDouble(SensorConstants.VALUE);
                pointStart = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                patchColorValues = colors.getJSONObject(i + 1).getJSONArray(SensorConstants.LAB);
                resultPatchValueEnd = colors.getJSONObject(i + 1).getDouble(SensorConstants.VALUE);
                pointEnd = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                double lStart = pointStart[0];
                double aStart = pointStart[1];
                double bStart = pointStart[2];

                double dL = (pointEnd[0] - pointStart[0]) / INTERPOLATION_NUMBER;
                double da = (pointEnd[1] - pointStart[1]) / INTERPOLATION_NUMBER;
                double db = (pointEnd[2] - pointStart[2]) / INTERPOLATION_NUMBER;
                double dV = (resultPatchValueEnd - resultPatchValueStart) / INTERPOLATION_NUMBER;

                // create 10 interpolation points, including the start point,
                // but excluding the end point

                for (int ii = 0; ii < INTERPOLATION_NUMBER; ii++) {
                    lInter = lStart + ii * dL;
                    aInter = aStart + ii * da;
                    bInter = bStart + ii * db;
                    vInter = resultPatchValueStart + ii * dV;

                    interpolTable[count][0] = lInter;
                    interpolTable[count][1] = aInter;
                    interpolTable[count][2] = bInter;
                    interpolTable[count][3] = vInter;
                    count++;
                }

                // add final point
                patchColorValues = colors.getJSONObject(colors.length() - 1).getJSONArray(SensorConstants.LAB);
                interpolTable[count][0] = patchColorValues.getDouble(0);
                interpolTable[count][1] = patchColorValues.getDouble(1);
                interpolTable[count][2] = patchColorValues.getDouble(2);
                interpolTable[count][3] = colors.getJSONObject(colors.length() - 1).getDouble(SensorConstants.VALUE);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return interpolTable;
    }

    public static double calculateResultSingle(@Nullable double[] colorValues, @NonNull JSONArray colors, int id) {
        double[][] interpolTable = createInterpolTable(colors);

        // determine closest value
        // create interpolation and extrapolation tables using linear approximation
        if (colorValues == null || colorValues.length < 3) {
            throw new IllegalArgumentException("invalid color data.");
        }

        // normalise lab values to standard ranges L:0..100, a and b: -127 ... 128
        double[] labPoint = new double[]{colorValues[0] / LAB_COLOR_NORMAL_DIVISOR, colorValues[1] - 128, colorValues[2] - 128};

        double distance;
        int index = 0;
        double nearest = Double.MAX_VALUE;

        for (int j = 0; j < interpolTable.length; j++) {
            // Find the closest point using the E94 distance
            // the values are already in the right range, so we don't need to normalize
            distance = CalibrationCard.E94(labPoint[0], labPoint[1], labPoint[2], interpolTable[j][0],
                    interpolTable[j][1], interpolTable[j][2], false);
            if (distance < nearest) {
                nearest = distance;
                index = j;
            }
        }

        if (AppPreferences.isDiagnosticMode()) {
            PreferencesUtil.setString(CaddisflyApp.getApp().getApplicationContext(),
                    Constant.DISTANCE_INFO + id, String.format(Locale.US, "%.2f", nearest));
        }

        if (nearest < Constant.MAX_COLOR_DISTANCE) {
            // return result only if the color distance is not too big
            return interpolTable[index][3];
        } else {
            return Double.NaN;
        }
    }

    public static double calculateResultGroup(double[][] colorsValueLab,
                                              @NonNull List<StripTest.Brand.Patch> patches, int id) {

        double[][][] interpolTables = new double[patches.size()][][];

        // create all interpol tables
        for (int p = 0; p < patches.size(); p++) {
            JSONArray colors = patches.get(p).getColors();

            // create interpol table for this patch
            interpolTables[p] = createInterpolTable(colors);

            if (colorsValueLab[p] == null || colorsValueLab[p].length < 3) {
                throw new IllegalArgumentException("invalid color data.");
            }
        }

        // normalise lab values to standard ranges L:0..100, a and b: -127 ... 128
        double[][] labPoint = new double[patches.size()][];
        for (int p = 0; p < patches.size(); p++) {
            labPoint[p] = new double[]{colorsValueLab[p][0] / LAB_COLOR_NORMAL_DIVISOR,
                    colorsValueLab[p][1] - 128, colorsValueLab[p][2] - 128};
        }

        double distance;
        int index = 0;
        double nearest = Double.MAX_VALUE;

        // compute smallest distance, combining all interpolation tables as we want the global minimum
        // all interpol tables should have the same length here, so we use the length of the first one

        for (int j = 0; j < interpolTables[0].length; j++) {
            distance = 0;
            for (int p = 0; p < patches.size(); p++) {
                distance += CalibrationCard.E94(labPoint[p][0], labPoint[p][1], labPoint[p][2],
                        interpolTables[p][j][0], interpolTables[p][j][1], interpolTables[p][j][2], false);
            }
            if (distance < nearest) {
                nearest = distance;
                index = j;
            }
        }

        if (AppPreferences.isDiagnosticMode()) {
            PreferencesUtil.setString(CaddisflyApp.getApp().getApplicationContext(),
                    Constant.DISTANCE_INFO + id, String.format(Locale.US, "%.2f", nearest));
        }

        if (nearest < Constant.MAX_COLOR_DISTANCE * patches.size()) {
            // return result only if the color distance is not too big
            return interpolTables[0][index][3];
        } else {
            return Double.NaN;
        }
    }
}
