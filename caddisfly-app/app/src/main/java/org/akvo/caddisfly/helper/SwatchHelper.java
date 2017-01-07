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

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.ColorCompareInfo;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.DateUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SwatchHelper {

    private static final String TAG = "SwatchHelper";

    private static final int MAX_DISTANCE = 999;
    private static final int MAX_DIFFERENCE = 150;
    private static final double MAX_DIFF_FOR_AVG_CALC = 0.21;
    private static final double INCREMENT = 0.01;
    private static final int HSV_CROSSOVER_DIFFERENCE = 200;

    private SwatchHelper() {
    }

    private static double[] convertDoubles(List<Double> doubles) {
        double[] ret = new double[doubles.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = doubles.get(i);
        }
        return ret;
    }

    /**
     * Analyzes the color and returns a result info
     *
     * @param photoColor The color to compare
     * @param swatches   The range of colors to compare against
     */
    public static ResultDetail analyzeColor(int steps, ColorInfo photoColor, List<Swatch> swatches,
                                            ColorUtil.ColorModel colorModel) {

        ColorCompareInfo colorCompareInfo;

        List<Swatch> gradientSwatches = SwatchHelper.generateGradient(swatches, colorModel, INCREMENT);

        //Find the color within the generated gradient that matches the photoColor
        colorCompareInfo = getNearestColorFromSwatches(photoColor.getColor(),
                gradientSwatches);

        //set the result
        ResultDetail resultDetail = new ResultDetail(-1, photoColor.getColor());
        if (colorCompareInfo.getResult() > -1) {
            resultDetail.setResult(colorCompareInfo.getResult());
        }
        resultDetail.setColorModel(colorModel);
        resultDetail.setCalibrationSteps(steps);
        resultDetail.setMatchedColor(colorCompareInfo.getMatchedColor());
        resultDetail.setDistance(colorCompareInfo.getDistance());

        return resultDetail;
    }

    /**
     * Compares the colorToFind to all colors in the color range and finds the nearest matching color
     *
     * @param colorToFind The colorToFind to compare
     * @param swatches    The range of colors from which to return the nearest colorToFind
     * @return details of the matching color with its corresponding value
     */
    private static ColorCompareInfo getNearestColorFromSwatches(
            int colorToFind, List<Swatch> swatches) {

        double distance;
        distance = ColorUtil.getMaxDistance(AppPreferences.getColorDistanceTolerance());

        double resultValue = -1;
        int matchedColor = -1;
        double tempDistance;
        double nearestDistance = MAX_DISTANCE;
        int nearestMatchedColor = -1;

        for (int i = 0; i < swatches.size(); i++) {
            int tempColor = swatches.get(i).getColor();

            tempDistance = ColorUtil.getColorDistance(tempColor, colorToFind);
            if (nearestDistance > tempDistance) {
                nearestDistance = tempDistance;
                nearestMatchedColor = tempColor;
            }

            if (tempDistance == 0.0) {
                resultValue = swatches.get(i).getValue();
                matchedColor = swatches.get(i).getColor();
                break;
            } else if (tempDistance < distance) {
                distance = tempDistance;
                resultValue = swatches.get(i).getValue();
                matchedColor = swatches.get(i).getColor();
            }
        }

        //if no result was found add some diagnostic info
        if (resultValue == -1) {
            distance = nearestDistance;
            matchedColor = nearestMatchedColor;
        }
        return new ColorCompareInfo(resultValue, colorToFind, matchedColor, distance);
    }

    /**
     * Calculate the slope of the linear trend for a range of colors
     *
     * @param swatches the range of colors
     * @return The slope value
     */
    public static double calculateSlope(List<Swatch> swatches) {

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
     * @param testInfo the test Information
     * @return True if valid otherwise false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSwatchListValid(TestInfo testInfo) {
        boolean result = true;

        List<Swatch> swatches = testInfo.getSwatches();

        if (swatches.size() < 1) {
            return false;
        }

        Swatch previousSwatch = swatches.get(0);
        for (Swatch swatch1 : swatches) {
            if (swatch1.getColor() == Color.TRANSPARENT || swatch1.getColor() == Color.BLACK) {
                //Calibration is incomplete
                result = false;
                break;
            }
            for (Swatch swatch2 : swatches) {
                if (!swatch1.equals(swatch2) && ColorUtil.areColorsSimilar(swatch1.getColor(), swatch2.getColor())) {
                    //Duplicate color
                    result = false;
                    break;
                }
            }

            if (swatch1.getDefaultColor() != Color.TRANSPARENT) {
                if (ColorUtil.getColorDistance(swatch1.getColor(),
                        swatch1.getDefaultColor()) > ColorimetryLiquidConfig.MAX_VALID_CALIBRATION_TOLERANCE) {
                    result = false;
                    break;
                }

                int redDifference = Color.red(swatch1.getColor()) - Color.red(previousSwatch.getColor());
                int greenDifference = Color.green(swatch1.getColor()) - Color.green(previousSwatch.getColor());
                int blueDifference = Color.blue(swatch1.getColor()) - Color.blue(previousSwatch.getColor());

                if (Math.abs(swatch1.getRedDifference() - redDifference) > MAX_DIFFERENCE) {
                    result = false;
                    break;
                }

                if (Math.abs(swatch1.getGreenDifference() - greenDifference) > MAX_DIFFERENCE) {
                    result = false;
                    break;
                }

                if (Math.abs(swatch1.getBlueDifference() - blueDifference) > MAX_DIFFERENCE) {
                    result = false;
                    break;
                }
            }
            previousSwatch = swatch1;
        }

        if (result && testInfo.getHueTrend() != 0) {
            result = validateHueTrend(swatches, testInfo.getHueTrend());
        }

        return result;
    }

    private static boolean validateHueTrend(List<Swatch> swatches, int trend) {

        float[] colorHSV = new float[3];
        float previousHue = 0f;

        boolean crossed = false;
        for (int i = 0; i < swatches.size(); i++) {
            //noinspection ResourceType
            Color.colorToHSV(swatches.get(i).getColor(), colorHSV);

            if (trend < 0) {
                if (!crossed && colorHSV[0] - previousHue > HSV_CROSSOVER_DIFFERENCE) {
                    previousHue = colorHSV[0];
                    crossed = true;
                }
                if (i > 0 && previousHue < colorHSV[0]) {
                    return false;
                }
            } else {
                if (!crossed && colorHSV[0] - previousHue < -HSV_CROSSOVER_DIFFERENCE) {
                    previousHue = colorHSV[0];
                    crossed = true;
                }
                if (i > 0 && previousHue > colorHSV[0]) {
                    return false;
                }
            }
            previousHue = colorHSV[0];
        }

        return true;
    }

    public static int getCalibratedSwatchCount(List<Swatch> swatches) {

        int count = 0;
        for (Swatch swatch1 : swatches) {
            if (swatch1.getColor() != Color.TRANSPARENT && swatch1.getColor() != Color.BLACK) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Returns an average color from a list of results
     * If any color does not closely match the rest of the colors then it returns 0
     *
     * @param results the list of results
     * @return the average color
     */
    public static int getAverageColor(List<Result> results) {

        int red = 0;
        int green = 0;
        int blue = 0;

        for (int i = 0; i < results.size(); i++) {

            if (results.get(i).getResults().get(0).getColorModel() == ColorUtil.ColorModel.RGB) {
                int color1 = results.get(i).getResults().get(0).getColor();

                //if invalid color return 0
                if (color1 == 0) {
                    return 0;
                }

                //check all the colors are mostly similar otherwise return 0
                for (int j = 0; j < results.size(); j++) {
                    if (results.get(j).getResults().get(0).getColorModel() == ColorUtil.ColorModel.RGB) {
                        int color2 = results.get(j).getResults().get(0).getColor();
                        if (ColorUtil.areColorsTooDissimilar(color1, color2)) {
                            return 0;
                        }
                    }
                }
                red += Color.red(color1);
                green += Color.green(color1);
                blue += Color.blue(color1);
            }
        }

        //return an average color
        int resultCount = results.size();
        return Color.rgb(red / resultCount, green / resultCount, blue / resultCount);
    }

    /**
     * Returns the value that appears the most number of times in the array
     *
     * @param array the array of values
     * @return the most frequent value
     */
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

    /**
     * Returns the average of a list of values
     *
     * @param results the results
     * @return the average value
     */
    public static double getAverageResult(List<Result> results) {

        double result = 0;

        ArrayList<Double> resultValues = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            resultValues.add(results.get(i).getResults().get(0).getResult());
        }

        double commonResult = mostFrequent(convertDoubles(resultValues));

        for (int i = 0; i < results.size(); i++) {
            double value = results.get(i).getResults().get(0).getResult();
            if (value > -1 && Math.abs(value - commonResult) < MAX_DIFF_FOR_AVG_CALC) {
                result += value;
            } else {
                return -1;
            }
        }

        try {
            result = round(result / results.size(), 2);
        } catch (Exception ex) {
            result = -1;
        }

        return result;
    }

    //Ref: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    @SuppressWarnings("SameParameterValue")
    private static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void generateSwatches(List<Swatch> swatches, List<Swatch> testSwatches) {
        int redDifferenceTotal = 0;
        int greenDifferenceTotal = 0;
        int blueDifferenceTotal = 0;

        for (int i = 0; i < testSwatches.size(); i++) {
            Swatch clonedSwatch;
            try {
                clonedSwatch = (Swatch) testSwatches.get(i).clone();
                clonedSwatch.setColor(Color.TRANSPARENT);

                if (swatches.size() > i) {
                    Swatch swatch = swatches.get(i);
                    if (swatch.getValue() != testSwatches.get(i).getValue()) {
                        swatches.add(i, clonedSwatch);
                    } else {

                        int redDifference = Color.red(swatch.getColor()) - Color.red(swatch.getDefaultColor());
                        int greenDifference = Color.green(swatch.getColor()) - Color.green(swatch.getDefaultColor());
                        int blueDifference = Color.blue(swatch.getColor()) - Color.blue(swatch.getDefaultColor());

                        redDifferenceTotal += redDifference;
                        greenDifferenceTotal += greenDifference;
                        blueDifferenceTotal += blueDifference;
                    }
                } else {
                    swatches.add(clonedSwatch);
                }
            } catch (CloneNotSupportedException e) {
                Log.e(TAG, e.getMessage(), e);
            }

        }

        redDifferenceTotal = redDifferenceTotal / testSwatches.size();
        greenDifferenceTotal = greenDifferenceTotal / testSwatches.size();
        blueDifferenceTotal = blueDifferenceTotal / testSwatches.size();

        for (Swatch swatch : swatches) {
            if (swatch.getColor() == Color.TRANSPARENT) {
                int color = swatch.getDefaultColor();

                swatch.setColor(Color.rgb(Color.red(color) + redDifferenceTotal,
                        Color.green(color) + greenDifferenceTotal,
                        Color.blue(color) + blueDifferenceTotal));
            }
        }
    }

    public static String generateCalibrationFile(Context context, String testCode, String batchCode,
                                                 long calibrationDate, long expiryDate, String ledRgb,
                                                 boolean backdropDetect) {

        final StringBuilder calibrationDetails = new StringBuilder();

        for (Swatch swatch : CaddisflyApp.getApp().getCurrentTestInfo().getSwatches()) {
            calibrationDetails.append(String.format(Locale.US, "%.2f", swatch.getValue()))
                    .append("=")
                    .append(ColorUtil.getColorRgbString(swatch.getColor()));
            calibrationDetails.append('\n');
        }

        calibrationDetails.append("Type: ");
        calibrationDetails.append(testCode);
        calibrationDetails.append("\n");
        calibrationDetails.append("Date: ");
        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(calibrationDate));
        calibrationDetails.append("\n");
        calibrationDetails.append("Calibrated: ");
        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(calibrationDate));
        calibrationDetails.append("\n");
        calibrationDetails.append("LED RGB: ");
        calibrationDetails.append(ledRgb);
        calibrationDetails.append("\n");
        calibrationDetails.append("DetectBackdrop: ");
        calibrationDetails.append(String.valueOf(backdropDetect));
        calibrationDetails.append("\n");
        calibrationDetails.append("ReagentExpiry: ");
        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(expiryDate));
        calibrationDetails.append("\n");
        calibrationDetails.append("ReagentBatch: ");
        calibrationDetails.append(batchCode);
        calibrationDetails.append("\n");
        calibrationDetails.append("Version: ");
        calibrationDetails.append(CaddisflyApp.getAppVersion());
        calibrationDetails.append("\n");
        calibrationDetails.append("Model: ");
        calibrationDetails.append(android.os.Build.MODEL).append(" (")
                .append(android.os.Build.PRODUCT).append(")");
        calibrationDetails.append("\n");
        calibrationDetails.append("OS: ");
        calibrationDetails.append(android.os.Build.VERSION.RELEASE).append(" (")
                .append(android.os.Build.VERSION.SDK_INT).append(")");
        calibrationDetails.append("\n");
        calibrationDetails.append("DeviceId: ");
        calibrationDetails.append(ApiUtil.getInstallationId(context));
        return calibrationDetails.toString();
    }

    public static List<Swatch> loadCalibrationFromFile(Context context, String fileName) throws IOException {
        final List<Swatch> swatchList = new ArrayList<>();
        final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                CaddisflyApp.getApp().getCurrentTestInfo().getCode());

        List<String> calibrationDetails = FileUtil.loadFromFile(path, fileName);

        if (calibrationDetails != null) {

            PreferencesUtil.setBoolean(context, R.string.noBackdropDetectionKey, false);

            for (int i = calibrationDetails.size() - 1; i >= 0; i--) {
                String line = calibrationDetails.get(i);
                if (!line.contains("=")) {
                    String testCode = CaddisflyApp.getApp().getCurrentTestInfo().getCode();
                    if (line.contains("Calibrated:")) {
                        Calendar calendar = Calendar.getInstance();
                        Date date = DateUtil.convertStringToDate(line.substring(line.indexOf(':') + 1),
                                "yyyy-MM-dd HH:mm");
                        if (date != null) {
                            calendar.setTime(date);
                            PreferencesUtil.setLong(context, testCode,
                                    R.string.calibrationDateKey, calendar.getTimeInMillis());
                        }
                    }
                    if (line.contains("ReagentExpiry:")) {
                        Calendar calendar = Calendar.getInstance();
                        Date date = DateUtil.convertStringToDate(line.substring(line.indexOf(':') + 1),
                                "yyyy-MM-dd");
                        if (date != null) {
                            calendar.setTime(date);
                            PreferencesUtil.setLong(context, testCode,
                                    R.string.calibrationExpiryDateKey, calendar.getTimeInMillis());
                        }
                    }

                    if (line.contains("ReagentBatch:")) {
                        String batch = line.substring(line.indexOf(':') + 1).trim();
                        PreferencesUtil.setString(context, testCode,
                                R.string.batchNumberKey, batch);
                    }

                    if (line.contains("LED RGB:")) {
                        String rgb = line.substring(line.indexOf(':') + 1).trim();
                        PreferencesUtil.setString(context, testCode,
                                R.string.ledRgbKey, rgb);
                    }

                    if (line.contains("DetectBackdrop:")) {
                        Boolean detect = !Boolean.valueOf(line.substring(line.indexOf(':') + 1).trim());
                        PreferencesUtil.setBoolean(context, R.string.noBackdropDetectionKey, detect);
                    }

                    calibrationDetails.remove(i);
                }
            }

            for (String rgb : calibrationDetails) {
                String[] values = rgb.split("=");
                Swatch swatch = new Swatch(stringToDouble(values[0]),
                        ColorUtil.getColorFromRgb(values[1]), Color.TRANSPARENT);
                swatchList.add(swatch);
            }

            if (swatchList.size() > 0) {
                SwatchHelper.saveCalibratedSwatches(context, swatchList);

                if (AppPreferences.isDiagnosticMode()) {
                    Toast.makeText(context,
                            String.format(context.getString(R.string.calibrationLoaded), fileName),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                throw new IOException();
            }
        }
        return swatchList;
    }

    /**
     * Save a list of calibrated colors
     *
     * @param swatches List of swatch colors to be saved
     */
    public static void saveCalibratedSwatches(Context context, List<Swatch> swatches) {

        TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        for (Swatch swatch : swatches) {
            String key = String.format(Locale.US, "%s-%.2f",
                    currentTestInfo.getCode(), swatch.getValue());

            PreferencesUtil.setInt(context, key, swatch.getColor());
        }

        CaddisflyApp.getApp().loadCalibratedSwatches(currentTestInfo);
    }

    /**
     * Auto generate the color swatches for the given test type
     *
     * @param swatches The test object
     * @return The list of generated color swatches
     */
    @SuppressWarnings("SameParameterValue")
    public static List<Swatch> generateGradient(
            List<Swatch> swatches, ColorUtil.ColorModel colorModel, double increment) {

        List<Swatch> list = new ArrayList<>();

        for (int i = 0; i < swatches.size() - 1; i++) {

            int startColor = swatches.get(i).getColor();
            int endColor = swatches.get(i + 1).getColor();
            double startValue = swatches.get(i).getValue();
            int steps = (int) ((swatches.get(i + 1).getValue() - startValue) / increment);

            for (int j = 0; j < steps; j++) {
                int color = 0;
                switch (colorModel) {
                    case RGB:
                        color = ColorUtil.getGradientColor(startColor, endColor, steps, j);
                        break;
                    case LAB:
                        color = ColorUtil.labToColor(ColorUtil.getGradientLabColor(ColorUtil.colorToLab(startColor),
                                ColorUtil.colorToLab(endColor), steps, j));
                }

                list.add(new Swatch(startValue + (j * increment), color, Color.TRANSPARENT));
            }
        }
        list.add(new Swatch(swatches.get(swatches.size() - 1).getValue(),
                swatches.get(swatches.size() - 1).getColor(), Color.TRANSPARENT));

        return list;
    }

    /**
     * Validate the color by looking for missing color, duplicate colors, color out of sequence etc...
     *
     * @param swatches the range of colors
     * @return True if calibration is complete
     */
    public static boolean isCalibrationComplete(List<Swatch> swatches) {
        for (Swatch swatch : swatches) {
            if (swatch.getColor() == 0 || swatch.getColor() == Color.BLACK) {
                //Calibration is incomplete
                return false;
            }
        }
        return true;
    }

    /**
     * Convert a string number into a double value
     *
     * @param text the text to be converted to number
     * @return the double value
     */
    private static double stringToDouble(String text) {

        text = text.replaceAll(",", ".");
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        try {
            return nf.parse(text).doubleValue();
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
            return 0.0;
        }
    }
}
