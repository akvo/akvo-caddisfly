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

package org.akvo.caddisfly.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.model.ColorInfo;

import java.util.Locale;

import timber.log.Timber;

/**
 * Set of utility functions for color calculations and analysis
 */
public final class ColorUtil {

    /**
     * The minimum color distance at which the colors are considered equivalent
     */
    private static final double MIN_COLOR_DISTANCE_RGB = 6;

//    /**
//     * The color distance within which the sampled colors should be for a valid test
//     */
//    private static final double MAX_SAMPLING_COLOR_DISTANCE_RGB = 15;

    private ColorUtil() {
    }

    @SuppressWarnings("unused")
    public static double getMinDistance() {
        return MIN_COLOR_DISTANCE_RGB;
    }

    public static double getMaxDistance(double defaultValue) {
        if (defaultValue > 0) {
            return defaultValue;
        } else {
            return ChamberTestConfig.MAX_COLOR_DISTANCE_RGB;
        }
    }

    /**
     * Get the most common color from the bitmap
     *
     * @param bitmap       The bitmap from which to extract the color
     * @param sampleLength The max length of the image to traverse
     * @return The extracted color information
     */
    @NonNull
    public static ColorInfo getColorFromBitmap(@NonNull Bitmap bitmap,
                                               @SuppressWarnings("SameParameterValue") int sampleLength) {
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

                    if (color != Color.TRANSPARENT) {
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
                if (areColorsSimilar(commonColor, m.keyAt(i))) {
                    goodColors++;
                    goodPixelCount += m.valueAt(i);
                }
            }

            double quality1 = ((double) goodPixelCount / totalPixels) * 100d;
            double quality2 = ((double) (colorsFound - goodColors) / colorsFound) * 100d;
            quality = Math.min(quality1, (100 - quality2));

            m.clear();

        } catch (Exception e) {
            Timber.e(e);
        }

        return new ColorInfo(commonColor, quality);
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

        return (int) Math.sqrt(r * r * .241
                + g * g * .691
                + b * b * .068
        );
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    public static double getColorDistance(int color1, int color2) {
        double r;
        double g;
        double b;

        r = Math.pow(Color.red(color2) - Color.red(color1), 2.0);
        g = Math.pow(Color.green(color2) - Color.green(color1), 2.0);
        b = Math.pow(Color.blue(color2) - Color.blue(color1), 2.0);

        return Math.sqrt(b + g + r);
    }

//    public static boolean areColorsTooDissimilar(int color1, int color2) {
//        return getColorDistanceRgb(color1, color2) > MAX_SAMPLING_COLOR_DISTANCE_RGB;
//    }

    public static boolean areColorsSimilar(int color1, int color2) {
        return getColorDistance(color1, color2) < MIN_COLOR_DISTANCE_RGB;
    }

    /**
     * Get the color that lies in between two colors
     *
     * @param startColor The first color
     * @param endColor   The last color
     * @param n          Number of steps between the two colors
     * @param i          The index at which the color is to be calculated
     * @return The newly generated color
     */
    public static int getGradientColor(int startColor, int endColor, int n, int i) {
        return Color.rgb(interpolate(Color.red(startColor), Color.red(endColor), n, i),
                interpolate(Color.green(startColor), Color.green(endColor), n, i),
                interpolate(Color.blue(startColor), Color.blue(endColor), n, i));
    }

    /**
     * Get the color component that lies between the two color component points
     *
     * @param start The first color component value
     * @param end   The last color component value
     * @param n     Number of steps between the two colors
     * @param i     The index at which the color is to be calculated
     * @return The calculated color component
     */
    private static int interpolate(int start, int end, int n, int i) {
        return (int) ((float) start + ((((float) end - (float) start) / n) * i));
    }

    /**
     * Convert color value to RGB string
     *
     * @param color The color to convert
     * @return The rgb value as string
     */
    public static String getColorRgbString(int color) {
        return String.format(Locale.getDefault(), "%d  %d  %d", Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Convert rgb string color to color
     *
     * @param rgb The rgb string representation of the color
     * @return An Integer color value
     */
    public static Integer getColorFromRgb(@NonNull String rgb) {
        String[] rgbArray = rgb.split("\\s+");
        return Color.rgb(Integer.parseInt(rgbArray[0]), Integer.parseInt(rgbArray[1]), Integer.parseInt(rgbArray[2]));
    }
}
