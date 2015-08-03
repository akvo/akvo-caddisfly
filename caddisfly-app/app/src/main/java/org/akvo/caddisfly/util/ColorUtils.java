/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.util.SparseIntArray;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.model.ColorCompareInfo;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of utility functions for color calculations and analysis
 */
public final class ColorUtils {

    private static final int GRAY_TOLERANCE = 10;

    private ColorUtils() {
    }

    public static Bundle getPpmValue(Bitmap bitmap, TestInfo testInfo, int length, int maxColorDistance) {
        ColorInfo photoColor = getColorFromBitmap(bitmap, length);
        Bundle bundle = new Bundle();

        bundle.putString("color",
                String.format("%d  %d  %d", Color.red(photoColor.getColor()),
                        Color.green(photoColor.getColor()),
                        Color.blue(photoColor.getColor()))
        );

        bundle.putDouble("quality", photoColor.getQuality());

        ArrayList<ResultRange> tempColorRange = new ArrayList<>();

        tempColorRange.add(testInfo.getRanges().get(0));
        analyzeColorHsv(photoColor, tempColorRange, bundle);

        tempColorRange.add(testInfo.getRanges().get(testInfo.getRanges().size() - 1));
        analyzeColorHsv(photoColor, tempColorRange, bundle);

        tempColorRange.add(1, testInfo.getRanges().get((testInfo.getRanges().size() / 2) - 1));
        analyzeColorHsv(photoColor, tempColorRange, bundle);

        analyzeColorHsv(photoColor, testInfo.getRanges(), bundle);

        analyzeColor(photoColor, testInfo.getSwatches(), maxColorDistance, bundle);

        return bundle;
    }

    /**
     * Get the most common color from the bitmap
     *
     * @param bitmap       The bitmap from which to extract the color
     * @param sampleLength The max length of the image to traverse
     * @return The extracted color information
     */
    private static ColorInfo getColorFromBitmap(Bitmap bitmap, int sampleLength) {
        int highestCount = 0;
        int commonColor = -1;
        int counter;

        int goodPixelCount = 0;
        int totalPixels = 0;
        double quality = 0;
        int colorsFound;

        try {

            SparseIntArray m = new SparseIntArray();

            for (int i = 0; i < Math.min(bitmap.getWidth(), sampleLength); i++) {

                for (int j = 0; j < Math.min(bitmap.getHeight(), sampleLength); j++) {

                    int color = bitmap.getPixel(i, j);

                    if (ColorUtils.isNotGray(color)) {
                        totalPixels++;

                        counter = m.get(color);
                        counter++;
                        m.put(color, counter);

                        if (counter > highestCount) {
                            commonColor = color;
                            highestCount = counter;
                        }
                    }
                }
            }

            // check the quality of the photo
            colorsFound = m.size();
            int goodColors = 0;

            for (int i = 0; i < colorsFound; i++) {
                double distance = getDistance(commonColor, m.keyAt(i));

                if (distance < 10) {
                    goodColors++;
                    goodPixelCount += m.valueAt(i);
                }
            }

            double quality1 = ((double) goodPixelCount / totalPixels) * 100d;
            double quality2 = ((double) (colorsFound - goodColors) / colorsFound) * 100d;
            quality = Math.min(quality1, (100 - quality2));

            m.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ColorInfo(commonColor, quality);
    }

    /**
     * Analyzes the color and returns a bundle with various result values
     *
     * @param photoColor The color to compare
     * @param colorRange The range of colors to compare against
     * @param bundle     Result information
     */
    private static void analyzeColor(ColorInfo photoColor, ArrayList<ResultRange> colorRange,
                                     int maxDistance, Bundle bundle) {

        bundle.putInt(Config.RESULT_COLOR_KEY, photoColor.getColor()); //NON-NLS

        ColorCompareInfo colorCompareInfo = getNearestColorFromSwatchRange(photoColor.getColor(),
                colorRange, maxDistance);

        if (colorCompareInfo.getResult() < 0) {
            bundle.putDouble(Config.RESULT_VALUE_KEY, -1); //NON-NLS
        } else {
            bundle.putDouble(Config.RESULT_VALUE_KEY, colorCompareInfo.getResult());
            bundle.putInt("MatchedColor", colorCompareInfo.getMatchedColor());
            bundle.putDouble("Distance", colorCompareInfo.getDistance());
        }
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    public static double getDistance(int color1, int color2) {
        double red, green, blue;

        red = Math.pow(Color.red(color2) - Color.red(color1), 2.0);
        green = Math.pow(Color.green(color2) - Color.green(color1), 2.0);
        blue = Math.pow(Color.blue(color2) - Color.blue(color1), 2.0);

        return Math.sqrt(blue + green + red);
    }

    /**
     * Compares the colorToFind to all colors in the color range and finds the nearest matching color
     *
     * @param colorToFind The colorToFind to compare
     * @param colorRange  The range of colors from which to return the nearest colorToFind
     * @return A parts per million (ppm) value (colorToFind index multiplied by a step unit)
     */
    private static ColorCompareInfo getNearestColorFromSwatchRange(
            int colorToFind, ArrayList<ResultRange> colorRange, int maxDistance) {

        double distance = maxDistance;

        double resultValue = -1;
        int matchedColor = -1;

        for (int i = 0; i < colorRange.size(); i++) {
            int tempColor = colorRange.get(i).getColor();

            double temp = getDistance(tempColor, colorToFind);

            if (temp == 0.0) {
                resultValue = colorRange.get(i).getValue();
                matchedColor = colorRange.get(i).getColor();
                break;
            } else if (temp < distance) {
                distance = temp;
                resultValue = colorRange.get(i).getValue();
                matchedColor = colorRange.get(i).getColor();
            }
        }

        return new ColorCompareInfo(resultValue, colorToFind, matchedColor, distance);
    }

    /**
     * Get the color that lies in between two colors
     *
     * @param startColor    The first color
     * @param endColor      The last color
     * @param incrementStep Number of expected incremental steps in between the two colors
     * @param i             The step number at which the color is to be calculated
     * @return The newly generated color
     */
    private static int getGradientColor(int startColor, int endColor, int incrementStep, int i) {
        int r = interpolate(Color.red(startColor), Color.red(endColor), incrementStep, i),
                g = interpolate(Color.green(startColor), Color.green(endColor), incrementStep, i),
                b = interpolate(Color.blue(startColor), Color.blue(endColor), incrementStep, i);

        return Color.rgb(r, g, b);
    }

    private static int interpolate(int start, int end, int steps, int count) {
        float result = (float) start
                + ((((float) end - (float) start) / steps) * count);
        return (int) result;
    }

    /**
     * Convert color value to RGB string
     *
     * @param color The color to convert
     * @return The rgb value as string
     */
    public static String getColorRgbString(int color) {
        return String.format("%d  %d  %d",
                Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Checks if a given color is not close to gray color
     *
     * @param color The color to evaluate
     * @return True if the color is not gray else False
     */
    private static boolean isNotGray(int color) {
        return Math.abs(Color.red(color) - Color.green(color)) > GRAY_TOLERANCE
                || Math.abs(Color.red(color) - Color.blue(color)) > GRAY_TOLERANCE;
    }

    /**
     * Auto generate the color swatches for the ranges of the given test type
     *
     * @param testInfo The test object
     * @return The list of generated color swatches
     */
    @SuppressWarnings("SameParameterValue")
    public static List<Pair<Double, Integer>> autoGenerateColors(TestInfo testInfo) {

        List<Pair<Double, Integer>> list = new ArrayList<>();
        double increment = 0.01;

        for (int i = 0; i < testInfo.getRanges().size() - 1; i++) {

            int startColor = testInfo.getRange(i).getColor();
            int endColor = testInfo.getRange(i + 1).getColor();
            double startValue = testInfo.getRange(i).getValue();
            int steps = (int) ((testInfo.getRange(i + 1).getValue() - startValue) / increment);

            for (int j = 0; j < steps; j++) {
                int color = ColorUtils.getGradientColor(startColor, endColor, steps, j);
                list.add(new Pair<>(startValue + (j * increment), color));
            }
        }
        return list;
    }

    /**
     * Convert rgb string color to color
     *
     * @param rgb The rgb string representation of the color
     * @return An Integer color value
     */
    public static Integer getColorFromRgb(String rgb) {
        String[] rgbArray = rgb.split("\\s+");
        return Color.rgb(Integer.valueOf(rgbArray[0]), Integer.valueOf(rgbArray[1]), Integer.valueOf(rgbArray[2]));
    }

    /**
     * Get the brightness of a given color
     *
     * @param color The color
     * @return The brightness value
     */
    public static int getBrightness(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return (int) Math.sqrt(r * r * .241 +
                        g * g * .691 +
                        b * b * .068
        );
    }

    public static boolean validateColorRange(ArrayList<ResultRange> colorRange) {

        for (ResultRange range1 : colorRange) {
            for (ResultRange range2 : colorRange) {
                if (range1 != range2) {
                    if (getDistance(range1.getColor(), range2.getColor()) < Config.MAX_VALID_COLOR_DISTANCE) {
                        return false;
                    }
                }
            }
        }

        return !(calculateSlope(colorRange) < 25 || calculateSlope(colorRange) > 39);
    }


    public static double calculateSlope(ArrayList<ResultRange> colorRange) {

        double a = 0, b, c, d;
        double xSum = 0, xSquaredSum = 0, ySum = 0;
        double slope;

        float[] colorHSV = new float[3];

        float[] hValue = new float[colorRange.size()];

        for (int i = 0; i < colorRange.size(); i++) {
            //noinspection ResourceType
            Color.colorToHSV(colorRange.get(i).getColor(), colorHSV);
            hValue[i] = colorHSV[0];
            if (hValue[i] < 100) {
                hValue[i] += 360;
            }
            a += colorRange.get(i).getValue() * hValue[i];
            xSum += colorRange.get(i).getValue();
            xSquaredSum += Math.pow(colorRange.get(i).getValue(), 2);

            ySum += hValue[i];
        }

        //Calculate the slope
        a *= colorRange.size();
        b = xSum * ySum;
        c = xSquaredSum * colorRange.size();
        d = Math.pow(xSum, 2);
        slope = (a - b) / (c - d);

        if (Double.isNaN(slope)) {
            slope = 32;
        }

        return slope;
    }

    /**
     * Experimental: Analyzes the extracted color using HSV
     *
     * @param photoColor The extracted color information
     * @param colorRange The color chart to compare against
     * @param bundle     The bundle to add results to
     */
    private static void analyzeColorHsv(ColorInfo photoColor, ArrayList<ResultRange> colorRange, Bundle bundle) {

        double a = 0, b, c, d, e, f;
        double xSum = 0, xSquaredSum = 0, ySum = 0;
        double slope, yIntercept;

        float[] colorHSV = new float[3];

        float[] hValue = new float[colorRange.size()];

        for (int i = 0; i < colorRange.size(); i++) {
            //noinspection ResourceType
            Color.colorToHSV(colorRange.get(i).getColor(), colorHSV);
            hValue[i] = colorHSV[0];
            if (hValue[i] < 100) {
                hValue[i] += 360;
            }
            a += colorRange.get(i).getValue() * hValue[i];
            xSum += colorRange.get(i).getValue();
            xSquaredSum += Math.pow(colorRange.get(i).getValue(), 2);

            ySum += hValue[i];
        }

        //Calculate the slope
        a *= colorRange.size();
        b = xSum * ySum;
        c = xSquaredSum * colorRange.size();
        d = Math.pow(xSum, 2);
        slope = (a - b) / (c - d);

        if (Double.isNaN(slope)) {
            slope = 32;
        }

        //Calculate the y intercept
        e = ySum;
        f = slope * xSum;
        yIntercept = (e - f) / colorRange.size();

        //noinspection ResourceType
        Color.colorToHSV(photoColor.getColor(), colorHSV);

        bundle.putInt(Config.RESULT_COLOR_KEY, photoColor.getColor()); //NON-NLS
        bundle.putDouble("a", a); //NON-NLS
        bundle.putDouble("b", b); //NON-NLS
        bundle.putDouble("c", c); //NON-NLS
        bundle.putDouble("d", d); //NON-NLS
        bundle.putDouble("e", e); //NON-NLS
        bundle.putDouble("f", f); //NON-NLS

        bundle.putDouble("slope", slope); //NON-NLS
        bundle.putDouble("intercept", yIntercept); //NON-NLS

        float h = colorHSV[0];
        if (h < yIntercept) {
            h += 360;
        }

        bundle.putDouble("h", h); //NON-NLS

        double value = (h - yIntercept) / slope;

        String resultKey = Config.RESULT_VALUE_KEY + "_" + colorRange.size();

        if (value < 0) {
            bundle.putDouble(resultKey, -1); //NON-NLS
        } else {
            bundle.putDouble(resultKey, value);
        }

        bundle.putString("color",
                String.format("%d  %d  %d", Color.red(photoColor.getColor()),
                        Color.green(photoColor.getColor()),
                        Color.blue(photoColor.getColor()))
        );
    }


}
