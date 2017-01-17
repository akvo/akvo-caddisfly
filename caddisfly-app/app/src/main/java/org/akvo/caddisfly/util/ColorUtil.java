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
import android.util.Log;
import android.util.SparseIntArray;

import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.LabColor;
import org.akvo.caddisfly.model.XyzColor;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;

import java.util.Locale;

/**
 * Set of utility functions for color calculations and analysis
 */
public final class ColorUtil {

    /**
     * The default color model used for analysis
     */
    public static final ColorModel DEFAULT_COLOR_MODEL = ColorModel.RGB;
    private static final String TAG = "ColorUtil";
    /**
     * The maximum color distance before the color is considered out of range
     */
    private static final double Xn = 0.950470;
    private static final double Yn = 1.0;
    private static final double Zn = 1.088830;
    private static final double t0 = 0.137931034;  // 4 / 29;
    private static final double t1 = 0.206896552;  // 6 / 29;
    private static final double t2 = 0.12841855;   // 3 * t1 * t1;
    private static final double t3 = 0.008856452; // t1 * t1 * t1;
    private static final int MAX_COLOR_DISTANCE_LAB = 4;

    /**
     * The minimum color distance at which the colors are considered equivalent
     */
    private static final double MIN_COLOR_DISTANCE_RGB = 6;
    private static final double MIN_COLOR_DISTANCE_LAB = 1.2;

    /**
     * The color distance within which the sampled colors should be for a valid test
     */
    private static final double MAX_SAMPLING_COLOR_DISTANCE_RGB = 11;
    private static final double MAX_SAMPLING_COLOR_DISTANCE_LAB = 1.5;

    private ColorUtil() {
    }

    @SuppressWarnings("unused")
    public static double getMinDistance() {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return MIN_COLOR_DISTANCE_RGB;
            case LAB:
                return MIN_COLOR_DISTANCE_LAB;
            default:
                return MIN_COLOR_DISTANCE_RGB;
        }
    }

    public static double getMaxDistance(double defaultValue) {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                if (defaultValue > 0) {
                    return defaultValue;
                } else {
                    return ColorimetryLiquidConfig.MAX_COLOR_DISTANCE_RGB;
                }
            case LAB:
                return MAX_COLOR_DISTANCE_LAB;
            default:
                return ColorimetryLiquidConfig.MAX_COLOR_DISTANCE_RGB;
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
            Log.e(TAG, e.getMessage(), e);
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
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return getColorDistanceRgb(color1, color2);

            case LAB:
                return getColorDistanceLab(colorToLab(color1), colorToLab(color2));

            default:
                return getColorDistanceRgb(color1, color2);
        }
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    private static double getColorDistanceRgb(int color1, int color2) {
        double r, g, b;

        r = Math.pow(Color.red(color2) - Color.red(color1), 2.0);
        g = Math.pow(Color.green(color2) - Color.green(color1), 2.0);
        b = Math.pow(Color.blue(color2) - Color.blue(color1), 2.0);

        return Math.sqrt(b + g + r);
    }

    public static boolean areColorsTooDissimilar(int color1, int color2) {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return getColorDistanceRgb(color1, color2) > MAX_SAMPLING_COLOR_DISTANCE_RGB;

            case LAB:
                return getColorDistanceLab(colorToLab(color1), colorToLab(color2))
                        > MAX_SAMPLING_COLOR_DISTANCE_LAB;

            default:
                return getColorDistanceRgb(color1, color2) > MIN_COLOR_DISTANCE_RGB;
        }
    }


    public static boolean areColorsSimilar(int color1, int color2) {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return getColorDistanceRgb(color1, color2) < MIN_COLOR_DISTANCE_RGB;

            case LAB:
                return getColorDistanceLab(colorToLab(color1), colorToLab(color2))
                        < MIN_COLOR_DISTANCE_LAB;

            default:
                return getColorDistanceRgb(color1, color2) < MIN_COLOR_DISTANCE_RGB;
        }
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

    /**
     * Convert int color to Lab color
     *
     * @param color The color to convert
     * @return The lab color
     */
    @NonNull
    public static LabColor colorToLab(int color) {
        return rgbToLab(Color.red(color), Color.green(color), Color.blue(color));
    }

    //http://stackoverflow.com/questions/27090107/color-gradient-algorithm-in-lab-color-space
    @NonNull
    public static LabColor getGradientLabColor(@NonNull LabColor c1, @NonNull LabColor c2, int n, int index) {
        double alpha = (double) index / (n - 1);  // 0.0 <= alpha <= 1.0
        double L = (1 - alpha) * c1.l + alpha * c2.l;
        double a = (1 - alpha) * c1.a + alpha * c2.a;
        double b = (1 - alpha) * c1.b + alpha * c2.b;
        return new LabColor(L, a, b);
    }

    /**
     * Convert LAB color to int Color
     *
     * @param color the LAB color
     * @return int color value
     */
    public static int labToColor(@NonNull LabColor color) {
        double a, b, g, l, r, x, y, z;
        l = color.l;
        a = color.a;
        b = color.b;
        y = (l + 16) / 116;
        x = y + a / 500;
        z = y - b / 200;
        y = Yn * lab_xyz(y);
        x = Xn * lab_xyz(x);
        z = Zn * lab_xyz(z);
        r = xyz_rgb(3.2404542 * x - 1.5371385 * y - 0.4985314 * z);
        g = xyz_rgb(-0.9692660 * x + 1.8760108 * y + 0.0415560 * z);
        b = xyz_rgb(0.0556434 * x - 0.2040259 * y + 1.0572252 * z);
        r = Math.max(0, Math.min(r, 255));
        g = Math.max(0, Math.min(g, 255));
        b = Math.max(0, Math.min(b, 255));
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private static double lab_xyz(double t) {
        if (t > t1) {
            return t * t * t;
        } else {
            return t2 * (t - t0);
        }
    }

    private static double xyz_rgb(double r) {
        return Math.round(255 * (r <= 0.00304 ? 12.92 * r : 1.055 * Math.pow(r, 1 / 2.4) - 0.055));
    }

    @NonNull
    private static LabColor rgbToLab(double r, double g, double b) {
        XyzColor xyzColor = rgbToXyz(r, g, b);
        return new LabColor(116 * xyzColor.y - 16, 500 * (xyzColor.x - xyzColor.y), 200 * (xyzColor.y - xyzColor.z));
    }

    private static double rgb_xyz(double r) {
        if ((r /= 255) <= 0.04045) {
            return (r / 12.92);
        } else {
            return (Math.pow((r + 0.055) / 1.055, 2.4));
        }
    }

    private static double xyz_lab(double t) {
        if (t > t3) {
            return Math.pow(t, 1.0 / 3.0);
        } else {
            return t / t2 + t0;
        }
    }

    @NonNull
    private static XyzColor rgbToXyz(double r, double g, double b) {
        double x, y, z;
        r = rgb_xyz(r);
        g = rgb_xyz(g);
        b = rgb_xyz(b);
        x = xyz_lab((0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / Xn);
        y = xyz_lab((0.2126729 * r + 0.7151522 * g + 0.0721750 * b) / Yn);
        z = xyz_lab((0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / Zn);
        return new XyzColor(x, y, z);
    }

    //https://github.com/StanfordHCI/c3/blob/master/java/src/edu/stanford/vis/color/LAB.java
    public static double getColorDistanceLab(@NonNull LabColor x, @NonNull LabColor y) {
        // adapted from Sharma et al's MATLAB implementation at
        //  http://www.ece.rochester.edu/~gsharma/ciede2000/

        // parametric factors, use defaults
        double kl = 1, kc = 1, kh = 1;

        // compute terms
        double pi = Math.PI,
                L1 = x.l, a1 = x.a, b1 = x.b, Cab1 = Math.sqrt(a1 * a1 + b1 * b1),
                L2 = y.l, a2 = y.a, b2 = y.b, Cab2 = Math.sqrt(a2 * a2 + b2 * b2),
                Cab = 0.5 * (Cab1 + Cab2),
                G = 0.5 * (1 - Math.sqrt(Math.pow(Cab, 7) / (Math.pow(Cab, 7) + Math.pow(25, 7)))),
                ap1 = (1 + G) * a1,
                ap2 = (1 + G) * a2,
                Cp1 = Math.sqrt(ap1 * ap1 + b1 * b1),
                Cp2 = Math.sqrt(ap2 * ap2 + b2 * b2),
                Cpp = Cp1 * Cp2;

        // ensure hue is between 0 and 2pi
        double hp1 = Math.atan2(b1, ap1);
        if (hp1 < 0) {
            hp1 += 2 * pi;
        }
        double hp2 = Math.atan2(b2, ap2);
        if (hp2 < 0) {
            hp2 += 2 * pi;
        }

        double dL = L2 - L1,
                dC = Cp2 - Cp1,
                dhp = hp2 - hp1;

        if (dhp > +pi) {
            dhp -= 2 * pi;
        }
        if (dhp < -pi) {
            dhp += 2 * pi;
        }
        if (Cpp == 0) {
            dhp = 0;
        }

        // Note that the defining equations actually need
        // signed Hue and chroma differences which is different
        // from prior color difference formulae
        double dH = 2 * Math.sqrt(Cpp) * Math.sin(dhp / 2);

        // Weighting functions
        double Lp = 0.5 * (L1 + L2),
                Cp = 0.5 * (Cp1 + Cp2);

        // Average Hue Computation
        // This is equivalent to that in the paper but simpler programmatically.
        // Average hue is computed in radians and converted to degrees where needed
        double hp = 0.5 * (hp1 + hp2);
        // Identify positions for which abs hue diff exceeds 180 degrees
        if (Math.abs(hp1 - hp2) > pi) {
            hp -= pi;
        }
        if (hp < 0) {
            hp += 2 * pi;
        }

        // Check if one of the chroma values is zero, in which case set
        // mean hue to the sum which is equivalent to other value
        if (Cpp == 0) {
            hp = hp1 + hp2;
        }

        double Lpm502 = (Lp - 50) * (Lp - 50),
                Sl = 1 + 0.015 * Lpm502 / Math.sqrt(20 + Lpm502),
                Sc = 1 + 0.045 * Cp,
                T = 1 - 0.17 * Math.cos(hp - pi / 6)
                        + 0.24 * Math.cos(2 * hp)
                        + 0.32 * Math.cos(3 * hp + pi / 30)
                        - 0.20 * Math.cos(4 * hp - 63 * pi / 180),
                Sh = 1 + 0.015 * Cp * T,
                ex = (180 / pi * hp - 275) / 25,
                deltaThetaRad = (30 * pi / 180) * Math.exp(-1 * (ex * ex)),
                Rc = 2 * Math.sqrt(Math.pow(Cp, 7) / (Math.pow(Cp, 7) + Math.pow(25, 7))),
                RT = -1 * Math.sin(2 * deltaThetaRad) * Rc;

        dL = dL / (kl * Sl);
        dC = dC / (kc * Sc);
        dH = dH / (kh * Sh);

        // The CIE 00 color difference
        return Math.sqrt(dL * dL + dC * dC + dH * dH + RT * dC * dH);
    }

//    public static double getColorDistanceLab(int color1, int color2) {
//        return getColorDistanceLab(colorToLab(color1), colorToLab(color2));
//    }

    /**
     * The different types of color models
     */
    public enum ColorModel {
        RGB, LAB
    }
}
