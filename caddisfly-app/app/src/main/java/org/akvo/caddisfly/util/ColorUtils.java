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
import android.util.SparseIntArray;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.model.ColorCompareInfo;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.HsvColor;
import org.akvo.caddisfly.model.LabColor;
import org.akvo.caddisfly.model.ResultInfo;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.XyzColor;

import java.util.ArrayList;

/**
 * Set of utility functions for color calculations and analysis
 */
public final class ColorUtils {

    private static final int GRAY_TOLERANCE = 10;
    private static final double Xn = 0.950470;
    private static final double Yn = 1.0;
    private static final double Zn = 1.088830;
    private static final double t0 = 0.137931034;  // 4 / 29;
    private static final double t1 = 0.206896552;  // 6 / 29;
    private static final double t2 = 0.12841855;   // 3 * t1 * t1;
    private static final double t3 = 0.008856452; // t1 * t1 * t1;

    private ColorUtils() {
    }

    /**
     * Analyzes the color and returns a result info
     *
     * @param photoColor The color to compare
     * @param colorRange The range of colors to compare against
     */
    public static ResultInfo analyzeColor(ColorInfo photoColor, ArrayList<Swatch> colorRange,
                                          int maxDistance, AppConfig.ColorModel colorModel) {

        //Find the color that matches the photoColor from the calibrated colorRange
        ColorCompareInfo colorCompareInfo = getNearestColorFromSwatchRange(
                photoColor.getColor(), colorRange, AppConfig.MIN_VALID_COLOR_DISTANCE);

        //If no color matches the colorRange then generate a gradient by interpolation
        if (colorCompareInfo.getResult() < 0) {

            ArrayList<Swatch> swatchRange = ColorUtils.generateGradient(colorRange, colorModel, 0.01);

            //Find the color within the generated gradient that matches the photoColor
            colorCompareInfo = getNearestColorFromSwatchRange(photoColor.getColor(),
                    swatchRange, maxDistance);
        }

        //set the result
        ResultInfo resultInfo = new ResultInfo(-1, photoColor.getColor());
        if (colorCompareInfo.getResult() > -1) {
            resultInfo.setColorModel(colorModel);
            resultInfo.setCalibrationSteps(colorRange.size());
            resultInfo.setResult(colorCompareInfo.getResult());
            resultInfo.setMatchedColor(colorCompareInfo.getMatchedColor());
            resultInfo.setDistance(colorCompareInfo.getDistance());
        }

        return resultInfo;
    }

    /**
     * Get the most common color from the bitmap
     *
     * @param bitmap       The bitmap from which to extract the color
     * @param sampleLength The max length of the image to traverse
     * @return The extracted color information
     */
    public static ColorInfo getColorFromBitmap(Bitmap bitmap, int sampleLength) {
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
                double distance = getColorDistanceLab(colorToLab(commonColor), colorToLab(m.keyAt(i)));

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


    /**
     * Validate the color by looking for missing color, duplicate colors, color out of sequence etc...
     *
     * @param colorRange the range of colors
     * @return True if calibration is complete
     */
    public static boolean isCalibrationComplete(ArrayList<Swatch> colorRange) {
        for (Swatch swatch : colorRange) {
            if (swatch.getColor() == 0 || swatch.getColor() == Color.BLACK) {
                //Calibration is incomplete
                return false;
            }
        }
        return true;
    }

    /**
     * Validate the color by looking for missing color, duplicate colors, color out of sequence etc...
     *
     * @param colorRange the range of colors
     * @return True if valid otherwise false
     */
    public static boolean validateColorRange(ArrayList<Swatch> colorRange) {

        for (Swatch swatch : colorRange) {
            if (swatch.getColor() == 0 || swatch.getColor() == Color.BLACK) {
                //Calibration is incomplete
                return false;
            }
            for (Swatch range2 : colorRange) {
                if (swatch != range2) {
                    double value = getColorDistanceLab(colorToLab(swatch.getColor()),
                            colorToLab(range2.getColor()));

                    if (value <= AppConfig.MIN_VALID_COLOR_DISTANCE) {
                        //Duplicate color
                        return false;
                    }
                }
            }
        }

        return true;
        //return !(calculateSlope(colorRange) < 20 || calculateSlope(colorRange) > 40);
    }

    /**
     * Calculate the slope of the linear trend for a range of colors
     *
     * @param colorRange the range of colors
     * @return The slope value
     */
    public static double calculateSlope(ArrayList<Swatch> colorRange) {

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
     * Compares the colorToFind to all colors in the color range and finds the nearest matching color
     *
     * @param colorToFind The colorToFind to compare
     * @param colorRange  The range of colors from which to return the nearest colorToFind
     * @return A parts per million (ppm) value (colorToFind index multiplied by a step unit)
     */
    private static ColorCompareInfo getNearestColorFromSwatchRange(
            int colorToFind, ArrayList<Swatch> colorRange, double maxDistance) {

        double distance = maxDistance;

        double resultValue = -1;
        int matchedColor = -1;

        for (int i = 0; i < colorRange.size(); i++) {
            int tempColor = colorRange.get(i).getColor();

            double temp = getColorDistanceLab(colorToLab(tempColor), colorToLab(colorToFind));

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
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    public static double getColorDistance(int color1, int color2) {
        double r, g, b;

        r = Math.pow(Color.red(color2) - Color.red(color1), 2.0);
        g = Math.pow(Color.green(color2) - Color.green(color1), 2.0);
        b = Math.pow(Color.blue(color2) - Color.blue(color1), 2.0);

        return Math.sqrt(b + g + r);
    }

    /**
     * Auto generate the color swatches for the ranges of the given test type
     *
     * @param colorRange The test object
     * @return The list of generated color swatches
     */
    @SuppressWarnings("SameParameterValue")
    public static ArrayList<Swatch> generateGradient(ArrayList<Swatch> colorRange,
                                                     AppConfig.ColorModel colorModel, double increment) {

        if (colorModel == AppConfig.ColorModel.HSV) {
            return getGradientHsvColor(colorRange, 200);
        }

        ArrayList<Swatch> list = new ArrayList<>();
        //double increment = 0.01;

        for (int i = 0; i < colorRange.size() - 1; i++) {

            int startColor = colorRange.get(i).getColor();
            int endColor = colorRange.get(i + 1).getColor();
            double startValue = colorRange.get(i).getValue();
            int steps = (int) ((colorRange.get(i + 1).getValue() - startValue) / increment);

            for (int j = 0; j < steps; j++) {
                int color = 0;
                switch (colorModel) {
                    case RGB:
                        color = ColorUtils.getGradientColor(startColor, endColor, steps, j);
                        break;
                    case LAB:
                        color = ColorUtils.labToColor(ColorUtils.getGradientLabColor(colorToLab(startColor),
                                colorToLab(endColor), steps, j));
                }

                list.add(new Swatch(startValue + (j * increment), color));
            }
        }
        list.add(new Swatch(colorRange.get(colorRange.size() - 1).getValue(),
                colorRange.get(colorRange.size() - 1).getColor()));

        return list;
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
    private static int getGradientColor(int startColor, int endColor, int n, int i) {
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
        return String.format("%d  %d  %d", Color.red(color), Color.green(color), Color.blue(color));
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
     * Convert int color to Lab color
     *
     * @param color The color to convert
     * @return The lab color
     */
    public static LabColor colorToLab(int color) {
        return rgbToLab(Color.red(color), Color.green(color), Color.blue(color));
    }

    //http://stackoverflow.com/questions/27090107/color-gradient-algorithm-in-lab-color-space
    private static LabColor getGradientLabColor(LabColor c1, LabColor c2, int n, int index) {
        double alpha = (double) index / (n - 1);  // 0.0 <= alpha <= 1.0
        double L = (1 - alpha) * c1.L + alpha * c2.L;
        double a = (1 - alpha) * c1.a + alpha * c2.a;
        double b = (1 - alpha) * c1.b + alpha * c2.b;
        return new LabColor(L, a, b);
    }

    private static int labToRgb(LabColor color) {
        double a, b, g, l, r, x, y, z;
        l = color.L;
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

    private static LabColor rgbToLab(double r, double g, double b) {
        XyzColor xyzColor = rgb2xyz(r, g, b);
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

    private static XyzColor rgb2xyz(double r, double g, double b) {
        double x, y, z;
        r = rgb_xyz(r);
        g = rgb_xyz(g);
        b = rgb_xyz(b);
        x = xyz_lab((0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / Xn);
        y = xyz_lab((0.2126729 * r + 0.7151522 * g + 0.0721750 * b) / Yn);
        z = xyz_lab((0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / Zn);
        return new XyzColor(x, y, z);
    }

    private static int labToColor(LabColor color) {
        return labToRgb(color);
    }

    // create gradient from yellow to red to black with 100 steps
    //var gradient = hsvGradient(100, [{h:0.14, s:0.5, b:1}, {h:0, s:1, b:1}, {h:0, s:1, b:0}]);
    // http://stackoverflow.com/questions/2593832/how-to-interpolate-hue-values-in-hsv-colour-space
    @SuppressWarnings("SameParameterValue")
    private static ArrayList<Swatch> getGradientHsvColor(ArrayList<Swatch> colors, int steps) {
        int parts = colors.size() - 1;
        ArrayList<Swatch> gradient = new ArrayList<>();
        int gradientIndex = 0;
        double increment = 0.01;
        double partSteps = Math.floor(steps / parts);
        double remainder = steps - (partSteps * parts);
        for (int col = 0; col < parts; col++) {

            double startValue = colors.get(col).getValue();

            float[] hsvColor = new float[3];

            Color.RGBToHSV(Color.red(colors.get(col).getColor()),
                    Color.green(colors.get(col).getColor()),
                    Color.blue(colors.get(col).getColor()), hsvColor);
            HsvColor c1 = new HsvColor(hsvColor[0], hsvColor[1], hsvColor[2]);

            Color.RGBToHSV(Color.red(colors.get(col + 1).getColor()),
                    Color.green(colors.get(col + 1).getColor()),
                    Color.blue(colors.get(col + 1).getColor()), hsvColor);
            HsvColor c2 = new HsvColor(hsvColor[0], hsvColor[1], hsvColor[2]);

            // determine clockwise and counter-clockwise distance between hues
            double distCCW = (c1.h >= c2.h) ? c1.h - c2.h : 1 + c1.h - c2.h;
            double distCW = (c1.h >= c2.h) ? 1 + c2.h - c1.h : c2.h - c1.h;

            // ensure we get the right number of steps by adding remainder to final part
            if (col == parts - 1) partSteps += remainder;

            // make gradient for this part
            for (int step = 0; step < partSteps; step++) {
                double p = step / partSteps;
                // interpolate h, s, b
                float h = (float) ((distCW <= distCCW) ? c1.h + (distCW * p) : c1.h - (distCCW * p));
                if (h < 0) h = 1 + h;
                if (h > 1) h = h - 1;
                float s = (float) ((1 - p) * c1.s + p * c2.s);
                float v = (float) ((1 - p) * c1.v + p * c2.v);

                hsvColor[0] = h;
                hsvColor[1] = s;
                hsvColor[2] = v;
                // add to gradient array
                gradient.add(gradientIndex, new Swatch(startValue + (step * increment),
                        Color.HSVToColor(hsvColor)));

                gradientIndex++;
            }
        }
        return gradient;
    }

    //https://github.com/StanfordHCI/c3/blob/master/java/src/edu/stanford/vis/color/LAB.java
    public static double getColorDistanceLab(LabColor x, LabColor y) {
        // adapted from Sharma et al's MATLAB implementation at
        //  http://www.ece.rochester.edu/~gsharma/ciede2000/

        // parametric factors, use defaults
        double kl = 1, kc = 1, kh = 1;

        // compute terms
        double pi = Math.PI,
                L1 = x.L, a1 = x.a, b1 = x.b, Cab1 = Math.sqrt(a1 * a1 + b1 * b1),
                L2 = y.L, a2 = y.a, b2 = y.b, Cab2 = Math.sqrt(a2 * a2 + b2 * b2),
                Cab = 0.5 * (Cab1 + Cab2),
                G = 0.5 * (1 - Math.sqrt(Math.pow(Cab, 7) / (Math.pow(Cab, 7) + Math.pow(25, 7)))),
                ap1 = (1 + G) * a1,
                ap2 = (1 + G) * a2,
                Cp1 = Math.sqrt(ap1 * ap1 + b1 * b1),
                Cp2 = Math.sqrt(ap2 * ap2 + b2 * b2),
                Cpp = Cp1 * Cp2;

        // ensure hue is between 0 and 2pi
        double hp1 = Math.atan2(b1, ap1);
        if (hp1 < 0) hp1 += 2 * pi;
        double hp2 = Math.atan2(b2, ap2);
        if (hp2 < 0) hp2 += 2 * pi;

        double dL = L2 - L1,
                dC = Cp2 - Cp1,
                dhp = hp2 - hp1;

        if (dhp > +pi) dhp -= 2 * pi;
        if (dhp < -pi) dhp += 2 * pi;
        if (Cpp == 0) dhp = 0;

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
        if (Math.abs(hp1 - hp2) > pi) hp -= pi;
        if (hp < 0) hp += 2 * pi;

        // Check if one of the chroma values is zero, in which case set
        // mean hue to the sum which is equivalent to other value
        if (Cpp == 0) hp = hp1 + hp2;

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

}
