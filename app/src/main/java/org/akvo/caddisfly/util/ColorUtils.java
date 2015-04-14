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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.util.SparseIntArray;

import org.akvo.caddisfly.Config;
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

    private static final double MAX_COLOR_DISTANCE = 70.0;

    private ColorUtils() {
    }

    public static Bundle getPpmValue(Bitmap bitmap, ArrayList<ResultRange> colorRange, int length) {
        ColorInfo photoColor = getColorFromBitmap(bitmap, length);
        return analyzeColor(photoColor, colorRange);
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
        }

        bundle.putString("color",
                String.format("%d  %d  %d", Color.red(photoColor.getColor()),
                        Color.green(photoColor.getColor()),
                        Color.blue(photoColor.getColor()))
        );

        //bundle.putInt(Config.QUALITY_KEY, photoColor.getQuality());

        return bundle;
    }

    public static double getDistance(int color, int tempColor) {
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

    @SuppressWarnings("SameParameterValue")
    public static List<Pair<String, Integer>> autoGenerateColors(TestInfo testInfo) {

        List<Pair<String, Integer>> list = new ArrayList<>();
        double increment = 0.01;

        for (int i = 0; i < testInfo.getRanges().size() - 1; i++) {

            int startColor = testInfo.getRange(i).getColor();
            int endColor = testInfo.getRange(i + 1).getColor();
            double startValue = testInfo.getRange(i).getValue();
            int steps = (int) ((testInfo.getRange(i + 1).getValue() - startValue) / increment);

            for (int j = 0; j < steps; j++) {
                int color = ColorUtils.getGradientColor(startColor, endColor, steps, j);
                list.add(new Pair(String.format("%s-%.2f", testInfo.getCode(), startValue + (j * increment)), color));
            }
        }
        return list;
    }

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
}
