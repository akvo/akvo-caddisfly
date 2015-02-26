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

package org.akvo.caddisfly.util;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseIntArray;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Set of utility functions for color calculations and analysis
 */
public class ColorUtils {

    private static final int GRAY_TOLERANCE = 10;

    private static final double MAX_COLOR_DISTANCE = 70.0;

    public static Bundle getPpmValue(byte[] data, ArrayList<ResultRange> colorRange, int length) {
        ColorInfo photoColor = getColorFromByteArray(data, length);
        return analyzeColor(photoColor, colorRange);
    }

    private static ColorInfo getColorFromByteArray(byte[] data, int length) {
        //byte[] imageData = resizeImage(data, length);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return getColorFromBitmap(bitmap, length);
    }

    private static ColorInfo getColorFromBitmap(Bitmap bitmap, int sampleLength) {
        int highestCount = 0;
        int goodPixelCount = 0;

        int commonColor = -1;
        int totalPixels = 0;
        int counter;
        double quality = 0;
        int colorsFound;

        try {

            SparseIntArray m = new SparseIntArray();

            for (int i = 0; i < Math.min(bitmap.getWidth(), sampleLength); i++) {

                for (int j = 0; j < Math.min(bitmap.getHeight(), sampleLength); j++) {

                    int rgb = bitmap.getPixel(i, j);
                    int[] rgbArr = ColorUtils.getRGB(rgb);

                    if (ColorUtils.isNotGray(rgbArr)) {
                        totalPixels++;

                        counter = m.get(rgb);
                        counter++;
                        m.put(rgb, counter);

                        if (counter > highestCount) {
                            commonColor = rgb;
                            highestCount = counter;
                        }
                    }
                }
            }

            bitmap.recycle();

            colorsFound = m.size();
            int goodColors = 0;

            for (int i = 0; i < colorsFound; i++) {
                double distance = getDistance(commonColor, m.keyAt(i));

                if (distance < 10) {
                    goodColors++;
                    goodPixelCount += m.valueAt(i);
                }
            }

            m.clear();
            double quality1 = ((double) goodPixelCount / totalPixels) * 100d;
            double quality2 = ((double) (colorsFound - goodColors) / colorsFound) * 100d;
            quality = Math.min(quality1, (100 - quality2));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ColorInfo(commonColor, (int) quality);

    }

    /**
     * Analyzes the color and returns a bundle with various result values
     *
     * @param photoColor The color to compare
     * @param colorRange The range of colors to compare against
     * @return A bundle with the results
     */
    private static Bundle analyzeColor(ColorInfo photoColor, ArrayList<ResultRange> colorRange) {

        Bundle bundle = new Bundle();
        bundle.putInt(Config.RESULT_COLOR_KEY, photoColor.getColor()); //NON-NLS

        double value = getNearestColorFromSwatchRange(photoColor.getColor(), colorRange);

        if (value < 0) {
            bundle.putDouble(Config.RESULT_VALUE_KEY, -1); //NON-NLS
        } else {
            bundle.putDouble(Config.RESULT_VALUE_KEY, value);
//            int color = colorRange.get((int) Math.round((value - rangeStartUnit) / rangeStepUnit)).getColor();
//
//            bundle.putInt("standardColor", color); //NON-NLS
//
//            bundle.putString("standardColorRgb",
//                    String.format("%d  %d  %d", Color.red(color),
//                            Color.green(color),
//                            Color.blue(color))
//            );
        }

        bundle.putString("color",
                String.format("%d  %d  %d", Color.red(photoColor.getColor()),
                        Color.green(photoColor.getColor()),
                        Color.blue(photoColor.getColor()))
        );

        bundle.putInt(Config.QUALITY_KEY, photoColor.getQuality());

        return bundle;
    }

    private static double getDistance(int color, int tempColor) {
        double red, green, blue;

        red = Math.pow(Color.red(tempColor) - Color.red(color), 2.0);
        green = Math.pow(Color.green(tempColor) - Color.green(color), 2.0);
        blue = Math.pow(Color.blue(tempColor) - Color.blue(color), 2.0);

        return Math.sqrt(blue + green + red);
    }

    /**
     * Compares the color to all colors in the color range and finds the nearest matching color
     *
     * @param color      The color to compare
     * @param colorRange The range of colors from which to return the nearest color
     * @return A parts per million (ppm) value (color index multiplied by a step unit)
     */
    private static double getNearestColorFromSwatchRange(int color, ArrayList<ResultRange> colorRange) {
        double distance = MAX_COLOR_DISTANCE;
        double nearest = -1;

        double red, green, blue;
        for (int i = 0; i < colorRange.size(); i++) {
            int tempColor = colorRange.get(i).getColor();

            // compute the Euclidean distance between the two colors
            red = Math.pow(Color.red(tempColor) - Color.red(color), 2.0);
            green = Math.pow(Color.green(tempColor) - Color.green(color), 2.0);
            blue = Math.pow(Color.blue(tempColor) - Color.blue(color), 2.0);

            double temp = Math.sqrt(blue + green + red);

            if (temp == 0.0) {
                nearest = colorRange.get(i).getValue();
                break;
            } else if (temp < distance) {
                distance = temp;
                nearest = colorRange.get(i).getValue();
            }
        }

        return nearest;
    }

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

    public static String getColorRgbString(int color) {
        return String.format("%d  %d  %d",
                Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int[] getRGB(int pixel) {

        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        return new int[]{red, green, blue};
    }

    private static boolean isNotGray(int[] rgb) {
        return Math.abs(rgb[0] - rgb[1]) > GRAY_TOLERANCE
                || Math.abs(rgb[0] - rgb[2]) > GRAY_TOLERANCE;
    }

    //Reference: https://gist.github.com/alexfu/64dc37b3343b9dead0c4
/*
    public static int calculateRelativeLuminance(int color) {
        int red = (int) (Color.red(color) * LM_RED_COEFFICIENT);
        int green = (int) (Color.green(color) * LM_GREEN_COEFFICIENT);
        int blue = (int) (Color.blue(color) * LM_BLUE_COEFFICIENT);
        return red + green + blue;
    }
*/

    public static double mostFrequent(double[] ary) {
        Map<Double, Integer> m = new HashMap<>();

        for (double a : ary) {
            if (a >= 0) {
                Integer freq = m.get(a);
                m.put(a, (freq == null) ? 1 : freq + 1);
            }
        }

        int max = -1;
        double mostFrequent = -1;

        for (Map.Entry<Double, Integer> e : m.entrySet()) {
            if (e.getValue() > max) {
                mostFrequent = e.getKey();
                max = e.getValue();
            }
        }

        return mostFrequent;
    }

    @SuppressWarnings("SameParameterValue")
    public static void autoGenerateColors(TestInfo testInfo, SharedPreferences.Editor editor) {

        for (int i = 0; i < testInfo.getRanges().size() - 1; i++) {

            int startColor = testInfo.getRange(i).getColor();
            int endColor = testInfo.getRange(i + 1).getColor();
            double startValue = testInfo.getRange(i).getValue();

            int steps = (int) ((testInfo.getRange(i + 1).getValue() - startValue) / 0.1);

            for (int j = 0; j < steps; j++) {
                int color = ColorUtils.getGradientColor(startColor, endColor, steps, j);
                editor.putInt(String.format("%s-%.2f", testInfo.getCode(), startValue + (j * 0.1)), color);
            }
        }
    }

//    public static void validateGradient(ArrayList<ColorInfo> colorList, int minQuality) {
//
//        int index1, index2;
//        double previousDistance = 0;
//        int previousIndex = 0;
//        boolean errorFound = false;
//        boolean notCalibrated = false;
//
//        for (int i = 1; i < colorList.size(); i++) {
//            int color1 = colorList.get(i - 1).getColor();
//            int color2 = colorList.get(i).getColor();
//            double distance = getDistance(color1, color2);
//            colorList.get(i).setDistance(distance);
//        }
//
//        for (int i = 1; i < colorList.size() / increment; i++) {
//            index1 = (i - 1) * increment;
//            index2 = i * increment;
//            int color1 = colorList.get(index1).getColor();
//            int color2 = colorList.get(index2).getColor();
//            double distance = getDistance(color1, color2);
//            colorList.get(index2).setIncrementDistance(distance);
//        }
//
//
//        for (int i = 0; i < colorList.size(); i += increment) {
//            int color1 = colorList.get(i).getColor();
//            if (color1 == -1) {
//                colorList.get(i).setErrorCode(Config.ERROR_NOT_YET_CALIBRATED);
//                notCalibrated = true;
//            }
//        }
//
//        if (!notCalibrated) {
//
//            for (int i = 0; i < colorList.size(); i += increment) {
//                if (colorList.get(i).getErrorCode() == Config.ERROR_NOT_YET_CALIBRATED) {
//                    notCalibrated = true;
//                    break;
//                }
//            }
//        }
//
//        if (!notCalibrated) {
//
//            for (int i = 0; i < colorList.size() / increment; i++) {
//                index1 = i * increment;
//                int color1 = colorList.get(index1).getColor();
//                index2 = (i + 1) * increment;
//                int color2 = colorList.get(index2).getColor();
//                double distance = getDistance(color1, color2);
//                //Log.i("ColorInfo", String.valueOf(distance));
//                //Invalid if color is too distant from previous color in list
//                if (distance > 14 * increment) {
//                    //Only one color needs to be set as invalid
//                    if (colorList.get(index1).getErrorCode() == 0) {
//                        errorFound = true;
//
//                        if (i < (colorList.size() / increment) - 2) {
//                            int index3 = (i + 2) * increment;
//                            int color3 = colorList.get(index3).getColor();
//                            distance = getDistance(color2, color3);
//                            if (distance < 20) {
//                                colorList.get(index2).setErrorCode(Config.ERROR_OUT_OF_RANGE);
//                            } else {
//                                colorList.get(index1).setErrorCode(Config.ERROR_OUT_OF_RANGE);
//                            }
//                        } else {
//                            colorList.get(index2).setErrorCode(Config.ERROR_OUT_OF_RANGE);
//                        }
//                    }
//                }
//            }
//
//            if (!errorFound) {
//                for (int i = 0; i < colorList.size() / increment; i++) {
//                    index1 = i * increment;
//                    int color1 = colorList.get(index1).getColor();
//                    for (int j = 0; j < colorList.size() / increment; j++) {
//                        index2 = j * increment;
//                        int color2 = colorList.get(index2).getColor();
//                        if (index1 != index2) {
//                            double distance = getDistance(color1, color2);
//
//                            //Invalid if color gradient is in reverse
//                            if (i == 0 && previousDistance > distance) {
//                                colorList.get(previousIndex).setErrorCode(Config.ERROR_SWATCH_OUT_OF_PLACE);
//                            }
//
//                            previousIndex = index2;
//                            previousDistance = distance;
//                            //Log.i("ColorInfo", distance + "  =   "  + getColorRgbString(color1) + "  -  " + getColorRgbString(color2));
//
//                            //Invalid if the color is too close to any other color in the list
//                            if (distance < 2 * increment) {
//                                colorList.get(index1).setErrorCode(Config.ERROR_DUPLICATE_SWATCH);
//                            }
//                        }
//                    }
//                }
//            }
//
//            for (int i = 0; i < colorList.size() - 1; i += increment) {
//                if (colorList.get(i).getQuality() < minQuality) {
//                    colorList.get(i).setErrorCode(Config.ERROR_LOW_QUALITY);
//                }
//            }
//        }
//    }

    public static Integer getColorFromRgb(String rgb) {
        String[] rgbArray = rgb.split("\\s+");
        return Color.rgb(Integer.valueOf(rgbArray[0]), Integer.valueOf(rgbArray[1]), Integer.valueOf(rgbArray[2]));
    }

    public static int getBrightness(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return (int) Math.sqrt(
                r * r * .241 +
                        g * g * .691 +
                        b * b * .068
        );
    }

    /*@SuppressWarnings("SameParameterValue")
    public static void autoGenerateColorCurve(String testCode, ArrayList<ColorInfo> colorList,
                                              int startColor, int size,
                                              SharedPreferences.Editor editor) {
        int[] redDeviation = {
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                -1,
                -2,
                -3,
                -4,
                -4,
                -8,
                -11,
                -14,
                -17,
                -20,
                -21,
                -22,
                -23,
                -24,
                -25};

        int[] greenDeviation = {
                0,
                6,
                12,
                19,
                25,
                32,
                38,
                45,
                51,
                58,
                65,
                71,
                77,
                83,
                89,
                95,
                101,
                107,
                113,
                119,
                125,
                129,
                133,
                137,
                141,
                145,
                146,
                148,
                149,
                151,
                153};

        int[] blueDeviation = {
                0,
                -9,
                -17,
                -26,
                -34,
                -42,
                -49,
                -56,
                -63,
                -70,
                -76,
                -85,
                -93,
                -102,
                -110,
                -118,
                -124,
                -130,
                -135,
                -141,
                -146,
                -146,
                -146,
                -146,
                -146,
                -146,
                -151,
                -156,
                -161,
                -166,
                -171};

        for (int i = 0; i < size; i++) {

            int r = Color.red(startColor) + redDeviation[i];
            int g = Color.green(startColor) + greenDeviation[i];
            int b = Color.blue(startColor) + blueDeviation[i];
            int nextColor = Color.rgb(Math.min(Math.max(r, 0), 255),
                    Math.min(Math.max(g, 0), 255),
                    Math.min(Math.max(b, 0), 255));

            ColorInfo colorInfo = new ColorInfo(nextColor, 100);
            colorList.set(i, colorInfo);

            editor.putInt(String.format("%s-%s", testCode, String.valueOf(i)),
                    nextColor);
        }
    }*/

}
