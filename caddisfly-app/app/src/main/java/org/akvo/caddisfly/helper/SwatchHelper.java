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

package org.akvo.caddisfly.helper;

import android.content.Context;
import android.graphics.Color;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.model.ColorCompareInfo;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.DateUtil;
import org.akvo.caddisfly.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static org.apache.commons.math3.util.Precision.round;

public final class SwatchHelper {

    private static final int MAX_DISTANCE = 999;

    private SwatchHelper() {
    }

    /**
     * Analyzes the color and returns a result info.
     *
     * @param photoColor The color to compare
     * @param swatches   The range of colors to compare against
     */
    public static ResultDetail analyzeColor(int steps, ColorInfo photoColor, List<Swatch> swatches) {

        ColorCompareInfo colorCompareInfo;

        //Find the color within the generated gradient that matches the photoColor
        colorCompareInfo = getNearestColorFromSwatches(photoColor.getColor(), swatches);

        //set the result
        ResultDetail resultDetail = new ResultDetail(-1, photoColor.getColor());
        if (colorCompareInfo.getResult() > -1) {
            resultDetail.setResult(colorCompareInfo.getResult());
        }
        resultDetail.setCalibrationSteps(steps);
        resultDetail.setMatchedColor(colorCompareInfo.getMatchedColor());
        resultDetail.setDistance(colorCompareInfo.getDistance());

        return resultDetail;
    }

    /**
     * Compares the colorToFind to all colors in the color range and finds the nearest matching color.
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
     * Generate the calibration details file.
     *
     * @param context         the context
     * @param testInfo        the test
     * @param batchCode       the batch number
     * @param calibrationDate date of calibration
     * @param expiryDate      expiry date of the reagent
     * @return the calibration file content
     */
    public static String generateCalibrationFile(Context context, TestInfo testInfo, String batchCode,
                                                 long calibrationDate, long expiryDate) {

        final StringBuilder calibrationDetails = new StringBuilder();

        for (Swatch swatch : testInfo.getSwatches()) {
            calibrationDetails.append(String.format(Locale.US, "%.2f", swatch.getValue()))
                    .append("=")
                    .append(ColorUtil.getColorRgbString(swatch.getColor()));
            calibrationDetails.append('\n');
        }

        calibrationDetails.append("Type: ");
        calibrationDetails.append(testInfo.getUuid());
        calibrationDetails.append("\n");
        calibrationDetails.append("Date: ");
        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(calibrationDate));
        calibrationDetails.append("\n");
        calibrationDetails.append("Calibrated: ");
        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(calibrationDate));
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

    /**
     * Load the calibration details from file.
     *
     * @param testInfo the test
     * @param fileName the file name
     * @return the list of calibrations loaded
     * @throws IOException IO exception
     */
    public static List<Calibration> loadCalibrationFromFile(TestInfo testInfo, String fileName) throws IOException {
        final List<Calibration> calibrations = new ArrayList<>();
        final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, testInfo.getUuid());

        CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();

        List<String> calibrationDetails = FileUtil.loadFromFile(path, fileName);
        if (calibrationDetails != null) {

            for (int i = calibrationDetails.size() - 1; i >= 0; i--) {
                String line = calibrationDetails.get(i);
                if (!line.contains("=")) {

                    CalibrationDetail calibrationDetail = new CalibrationDetail();
                    calibrationDetail.uid = testInfo.getUuid();

                    if (line.contains("Calibrated:")) {
                        Calendar calendar = Calendar.getInstance();
                        Date date = DateUtil.convertStringToDate(line.substring(line.indexOf(':') + 1),
                                "yyyy-MM-dd HH:mm");
                        if (date != null) {
                            calendar.setTime(date);
                            calibrationDetail.date = calendar.getTimeInMillis();
                        }
                    }
                    if (line.contains("ReagentExpiry:")) {
                        Calendar calendar = Calendar.getInstance();
                        Date date = DateUtil.convertStringToDate(line.substring(line.indexOf(':') + 1),
                                "yyyy-MM-dd");
                        if (date != null) {
                            calendar.setTime(date);
                            calibrationDetail.expiry = calendar.getTimeInMillis();
                        }
                    }

                    if (line.contains("ReagentBatch:")) {
                        calibrationDetail.batchNumber = line.substring(line.indexOf(':') + 1).trim();
                    }

                    dao.insert(calibrationDetail);

                    calibrationDetails.remove(i);
                }
            }

            for (String rgb : calibrationDetails) {
                String[] values = rgb.split("=");

                Calibration calibration = new Calibration();
                calibration.uid = testInfo.getUuid();
                calibration.date = new Date().getTime();
                calibration.color = ColorUtil.getColorFromRgb(values[1]);
                calibration.value = stringToDouble(values[0]);
                calibrations.add(calibration);
            }

            if (calibrations.size() > 0) {
                dao.insertAll(calibrations);
            } else {
                throw new IOException();
            }
        }
        return calibrations;
    }

    /**
     * Auto generate the color swatches for the given test type.
     *
     * @param swatches The test object
     * @return The list of generated color swatches
     */
    @SuppressWarnings("SameParameterValue")
    public static List<Swatch> generateGradient(List<Swatch> swatches) {

        List<Swatch> list = new ArrayList<>();

        for (int i = 0; i < swatches.size() - 1; i++) {

            int startColor = swatches.get(i).getColor();
            int endColor = swatches.get(i + 1).getColor();
            double startValue = swatches.get(i).getValue();
            double endValue = swatches.get(i + 1).getValue();
            double increment = (endValue - startValue) / ChamberTestConfig.INTERPOLATION_COUNT;
            int steps = (int) ((endValue - startValue) / increment);

            for (int j = 0; j < steps; j++) {
                int color = ColorUtil.getGradientColor(startColor, endColor, steps, j);
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
     * @param testInfo the test Information
     * @return True if valid otherwise false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSwatchListValid(TestInfo testInfo) {

        if (testInfo == null) {
            return false;
        }

        boolean result = true;

        List<Calibration> calibrations = testInfo.getCalibrations();

        if (calibrations == null || calibrations.size() < 1) {
            return false;
        }

        for (Calibration swatch1 : calibrations) {
            if (swatch1.color == Color.TRANSPARENT || swatch1.color == Color.BLACK) {
                //Calibration is incomplete
                result = false;
                break;
            }
            for (Calibration swatch2 : calibrations) {
                if (!swatch1.equals(swatch2) && ColorUtil.areColorsSimilar(swatch1.color, swatch2.color)) {
                    //Duplicate color
                    result = false;
                    break;
                }
            }
        }

        return result;
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
     * Convert a string number into a double value.
     *
     * @param text the text to be converted to number
     * @return the double value
     */
    private static double stringToDouble(String text) {

        String tempText = text.replaceAll(",", ".");
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        try {
            return nf.parse(tempText).doubleValue();
        } catch (ParseException e) {
            Timber.e(e);
            return 0.0;
        }
    }

    /**
     * Get the average value from list of results.
     *
     * @param resultDetails the result info
     * @return the average value
     */
    public static double getAverageResult(ArrayList<ResultDetail> resultDetails) {
        double result = 0;

        for (int i = 0; i < resultDetails.size(); i++) {
            int color1 = resultDetails.get(i).getColor();
            for (int j = 0; j < resultDetails.size(); j++) {
                int color2 = resultDetails.get(j).getColor();

                if (ColorUtil.getColorDistance(color1, color2) > ChamberTestConfig.MAX_COLOR_DISTANCE_RGB) {
                    return -1;
                }
            }
        }

        for (int i = 0; i < resultDetails.size(); i++) {
            double value = resultDetails.get(i).getResult();
            if (value > -1) {
                result += value;
            } else {
                return -1;
            }
        }

        try {
            result = round(result / resultDetails.size(), 2);
        } catch (Exception ex) {
            result = -1;
        }

        return result;
    }

    /**
     * Returns an average color from a list of results.
     * If any color does not closely match the rest of the colors then it returns -1
     *
     * @param resultDetails the list of results
     * @return the average color
     */
    public static int getAverageColor(ArrayList<ResultDetail> resultDetails) {

        int red = 0;
        int green = 0;
        int blue = 0;

        for (int i = 0; i < resultDetails.size(); i++) {
            int color1 = resultDetails.get(i).getColor();

            //if invalid color return 0
            if (color1 == Color.TRANSPARENT) {
                return color1;
            }

            //check all the colors are mostly similar otherwise return -1
            for (int j = 0; j < resultDetails.size(); j++) {
                int color2 = resultDetails.get(j).getColor();
                if (ColorUtil.getColorDistanceRgb(color1, color2) > ChamberTestConfig.MAX_COLOR_DISTANCE_CALIBRATION) {
                    return Color.TRANSPARENT;
                }
            }
            red += Color.red(color1);
            green += Color.green(color1);
            blue += Color.blue(color1);
        }

        //return an average color
        int resultCount = resultDetails.size();
        return Color.rgb(red / resultCount, green / resultCount, blue / resultCount);
    }

}
