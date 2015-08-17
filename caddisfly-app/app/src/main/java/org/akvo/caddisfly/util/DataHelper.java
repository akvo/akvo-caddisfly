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

import android.graphics.Color;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.model.ColorCompareInfo;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.Swatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataHelper {

    private DataHelper() {
    }

    private static double[] convertDoubles(List<Double> doubles) {
        double[] ret = new double[doubles.size()];
        for (int i = 0; i < ret.length; i++) ret[i] = doubles.get(i);
        return ret;
    }

    /**
     * Analyzes the color and returns a result info
     *
     * @param photoColor The color to compare
     * @param swatches   The range of colors to compare against
     */
    public static ResultDetail analyzeColor(ColorInfo photoColor, ArrayList<Swatch> swatches,
                                            int maxDistance, AppConfig.ColorModel colorModel) {

        //Find the color that matches the photoColor from the calibrated colorRange
        ColorCompareInfo colorCompareInfo = getNearestColorFromSwatches(
                photoColor.getColor(), swatches, AppConfig.MIN_VALID_COLOR_DISTANCE);

        //If no color matches the colorRange then generate a gradient by interpolation
        if (colorCompareInfo.getResult() < 0) {

            ArrayList<Swatch> gradientSwatches = ColorUtils.generateGradient(swatches, colorModel, 0.01);

            //Find the color within the generated gradient that matches the photoColor
            colorCompareInfo = getNearestColorFromSwatches(photoColor.getColor(),
                    gradientSwatches, maxDistance);
        }

        //set the result
        ResultDetail resultDetail = new ResultDetail(-1, photoColor.getColor());
        if (colorCompareInfo.getResult() > -1) {
            resultDetail.setColorModel(colorModel);
            resultDetail.setCalibrationSteps(swatches.size());
            resultDetail.setResult(colorCompareInfo.getResult());
            resultDetail.setMatchedColor(colorCompareInfo.getMatchedColor());
            resultDetail.setDistance(colorCompareInfo.getDistance());
        }

        return resultDetail;
    }

    /**
     * Compares the colorToFind to all colors in the color range and finds the nearest matching color
     *
     * @param colorToFind The colorToFind to compare
     * @param swatches    The range of colors from which to return the nearest colorToFind
     * @return A parts per million (ppm) value (colorToFind index multiplied by a step unit)
     */
    private static ColorCompareInfo getNearestColorFromSwatches(
            int colorToFind, ArrayList<Swatch> swatches, double maxDistance) {

        double distance = maxDistance;

        double resultValue = -1;
        int matchedColor = -1;

        for (int i = 0; i < swatches.size(); i++) {
            int tempColor = swatches.get(i).getColor();

            double temp = ColorUtils.getColorDistanceLab(ColorUtils.colorToLab(tempColor),
                    ColorUtils.colorToLab(colorToFind));

            if (temp == 0.0) {
                resultValue = swatches.get(i).getValue();
                matchedColor = swatches.get(i).getColor();
                break;
            } else if (temp < distance) {
                distance = temp;
                resultValue = swatches.get(i).getValue();
                matchedColor = swatches.get(i).getColor();
            }
        }

        return new ColorCompareInfo(resultValue, colorToFind, matchedColor, distance);
    }

    /**
     * Calculate the slope of the linear trend for a range of colors
     *
     * @param swatches the range of colors
     * @return The slope value
     */
    public static double calculateSlope(ArrayList<Swatch> swatches) {

        double a = 0, b, c, d;
        double xSum = 0, xSquaredSum = 0, ySum = 0;
        double slope;

        float[] colorHSV = new float[3];

        float[] hValue = new float[swatches.size()];

        for (int i = 0; i < swatches.size(); i++) {
            //noinspection ResourceType
            Color.colorToHSV(swatches.get(i).getColor(), colorHSV);
            hValue[i] = colorHSV[0];
            if (hValue[i] < 100) {
                hValue[i] += 360;
            }
            a += swatches.get(i).getValue() * hValue[i];
            xSum += swatches.get(i).getValue();
            xSquaredSum += Math.pow(swatches.get(i).getValue(), 2);

            ySum += hValue[i];
        }

        //Calculate the slope
        a *= swatches.size();
        b = xSum * ySum;
        c = xSquaredSum * swatches.size();
        d = Math.pow(xSum, 2);
        slope = (a - b) / (c - d);

        if (Double.isNaN(slope)) {
            slope = 32;
        }

        return slope;
    }

    /**
     * Validate the color by looking for missing color, duplicate colors, color out of sequence etc...
     *
     * @param swatches the range of colors
     * @return True if valid otherwise false
     */
    public static boolean validateSwatchList(ArrayList<Swatch> swatches) {

        for (Swatch swatch : swatches) {
            if (swatch.getColor() == 0 || swatch.getColor() == Color.BLACK) {
                //Calibration is incomplete
                return false;
            }
            for (Swatch swatch1 : swatches) {
                if (swatch != swatch1) {
                    double value = ColorUtils.getColorDistanceLab(
                            ColorUtils.colorToLab(swatch.getColor()),
                            ColorUtils.colorToLab(swatch1.getColor()));

                    if (value <= AppConfig.MIN_VALID_COLOR_DISTANCE) {
                        //Duplicate color
                        return false;
                    }
                }
            }
        }

        return true;
        //return !(calculateSlope(swatches) < 20 || calculateSlope(swatches) > 40);
    }

    public static int getAverageColor(ArrayList<Result> results, int samplingTimes) {

        int counter = 0;

        int red = 0;
        int green = 0;
        int blue = 0;

        ArrayList<Double> distances = new ArrayList<>();

        for (int i = 1; i < results.size() - 1; i++) {
            distances.add(ColorUtils.getColorDistance(results.get(i).getResults().get(0).getColor(),
                    results.get(i + 1).getResults().get(0).getColor()));
        }

        for (int i = 0; i < distances.size(); i++) {
            if (distances.get(i) > 20) {
                return 0;
            }
        }

        //Ignore the first result
        for (int i = 1; i < results.size(); i++) {
            int color = results.get(i).getResults().get(0).getColor();
            if (color != 0) {
                counter++;
                red += Color.red(color);
                green += Color.green(color);
                blue += Color.blue(color);
            }
        }

        if (counter >= samplingTimes - 1) {
            return Color.rgb(red / counter, green / counter, blue / counter);
        } else {
            return 0;
        }
    }

    private static int getClosestMatchIndex(ArrayList<Double> results, double commonResult) {
        double difference = 9999999;
        int index = -1;

        for (int i = 0; i < results.size(); i++) {
            double value = results.get(i);
            if (value != -1 && Math.abs(commonResult - value) < difference) {
                difference = Math.abs(commonResult - value);
                index = i;
            }
        }
        return index;
    }

    private static double mostFrequent(double[] array) {
        Map<Double, Integer> map = new HashMap<>();

        for (double a : array) {
            if (a >= 0) {
                Integer freq = map.get(a);
                map.put(a, (freq == null) ? 1 : freq + 1);
            }
        }

        int max = -1;
        double mostFrequent = -1;

        for (Map.Entry<Double, Integer> e : map.entrySet()) {
            if (e.getValue() > max) {
                mostFrequent = e.getKey();
                max = e.getValue();
            }
        }

        return mostFrequent;
    }

    public static double getAverageResult(ArrayList<Result> results, int samplingTimes) {

        double result = 0;

        int counter = 0;
        ArrayList<Double> resultValues = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            resultValues.add(results.get(i).getResults().get(0).getResult());
        }

        double commonResult;

        double[] resultArray = convertDoubles(resultValues);
        //ignore first value;
        resultArray[0] = -1;
        commonResult = mostFrequent(resultArray);

        ArrayList<Double> tempResults = new ArrayList<>();

        //Ignore the first result
        for (int i = 1; i < results.size(); i++) {
            double value = results.get(i).getResults().get(0).getResult();
            if (value > -1 && Math.abs(value - commonResult) < 0.21) {
                tempResults.add(value);
            }
        }

        int totalCount = tempResults.size();
        while (tempResults.size() > 0 && counter < Math.min(totalCount, samplingTimes)) {
            int index = getClosestMatchIndex(tempResults, commonResult);
            result += tempResults.get(index);
            counter++;
            tempResults.remove(index);
        }

        if (counter >= Math.min(results.size() - 1, samplingTimes)) {
            try {
                result = round(result / counter, 2);
            } catch (Exception ex) {
                result = -1;
            }
        } else {
            result = -1;
        }

        return result;
    }

    //Ref: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    @SuppressWarnings("SameParameterValue")
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}