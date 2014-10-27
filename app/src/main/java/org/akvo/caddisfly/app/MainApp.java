/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.PreferencesUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainApp extends Application {

    public final ArrayList<Double> rangeIntervals = new ArrayList<Double>();
    public final ArrayList<ColorInfo> colorList = new ArrayList<ColorInfo>();
    public DecimalFormat doubleFormat = new DecimalFormat("0.0");
    public int currentTestType = Config.FLUORIDE_SEVEN_STEP_INDEX;
    public int rangeIncrementStep = 5;
    public int rangeStartIncrement = 0;
    public double rangeIncrementValue = 0.1;
    private int maxRangeValue = 3;

    /**
     * @param context The context
     * @return The version name and number
     */
    public static String getVersion(Context context) {
        try {
            String version = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            String[] words = version.split("\\s");
            String versionString = "";
            for (String word : words) {
                try {
                    Double versionNumber = Double.parseDouble(word);
                    versionString += String.format("%.2f", versionNumber);
                } catch (NumberFormatException e) {
                    int id = context.getResources()
                            .getIdentifier(word, "string", context.getPackageName());
                    if (id > 0) {
                        versionString += context.getString(id);
                    } else {
                        versionString += word;
                    }
                }
                versionString += " ";
            }
            return versionString.trim();

        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    /**
     * Factory preset values for Fluoride (One step calibration)
     */
    public void setFluorideOneStepSwatches() {
        colorList.clear();
        rangeIntervals.clear();

        maxRangeValue = 3;
        rangeIncrementStep = 5;
        rangeStartIncrement = 0;
        rangeIncrementValue = 0.1;

        currentTestType = Config.FLUORIDE_ONE_STEP_INDEX;

        for (double i = 0.0; i <= maxRangeValue; i += ((maxRangeValue * 10) * rangeIncrementValue)) {
            rangeIntervals.add(i);
        }

        for (double i = 0; i <= maxRangeValue * 10; i++) {
            colorList.add(new ColorInfo(Color.rgb(0, 0, 0), 100));
        }

        loadCalibratedSwatches(currentTestType);
    }

    /**
     * Factory preset values for Fluoride (Seven step calibration)
     */
    public void setFluorideSevenStepSwatches() {
        colorList.clear();
        rangeIntervals.clear();

        maxRangeValue = 3;
        rangeIncrementStep = 5;
        rangeStartIncrement = 0;
        rangeIncrementValue = 0.1;

        currentTestType = Config.FLUORIDE_SEVEN_STEP_INDEX;

        for (double i = 0.0; i <= maxRangeValue; i += (rangeIncrementStep * rangeIncrementValue)) {
            rangeIntervals.add(i);
        }

        for (double i = 0; i <= maxRangeValue * 10; i++) {
            colorList.add(new ColorInfo(Color.rgb(0, 0, 0), 100));
        }

        loadCalibratedSwatches(currentTestType);
    }

    public void setLowRangeSwatches() {
        if (PreferencesUtils.getBoolean(getApplicationContext(), R.string.oneStepCalibrationKey, false)) {
            setFluorideOneStepSwatches();
        } else {
            setFluorideSevenStepSwatches();
        }
    }

    /**
     * @param testType The type of test
     */
    public void setSwatches(int testType) {
        switch (testType) {
            case Config.FLUORIDE_ONE_STEP_INDEX:
                setFluorideOneStepSwatches();
                break;
            case Config.FLUORIDE_SEVEN_STEP_INDEX:
                setFluorideSevenStepSwatches();
                break;
        }
    }

    /**
     * Load any user calibrated swatches which overrides factory preset swatches
     *
     * @param testType The type of test
     */
    void loadCalibratedSwatches(int testType) {
        MainApp context = ((MainApp) this.getApplicationContext());
        for (int i = 0; i < colorList.size(); i++) {
            if (PreferencesUtils.contains(context, String.format("%d-%d", testType, i))) {
                int value = PreferencesUtils.getInt(context, String.format("%d-%d", testType, i), -1);

                int quality = Math.max(-1, PreferencesUtils.getInt(context,
                        String.format("%d-a-%d", testType, i), -1));

                int r = Color.red(value);
                int g = Color.green(value);
                int b = Color.blue(value);

                // eliminate white and black colors
                if (r == 255 && g == 255 && b == 255) {
                    PreferencesUtils.setInt(this, String.format("%d-a-%d", testType, i), -1);
                    value = -1;
                }
                if (r == 0 && g == 0 && b == 0) {
                    PreferencesUtils.setInt(this, String.format("%d-a-%d", testType, i), -1);
                    value = -1;
                }

                ColorInfo colorInfo = new ColorInfo(value, quality);
                if (value == -1) {
                    colorInfo.setErrorCode(Config.ERROR_COLOR_IS_GRAY);
                }
                colorList.set(i, colorInfo);
            } else {
                ColorInfo colorInfo = new ColorInfo(-1, 0);
                colorInfo.setErrorCode(Config.ERROR_NOT_YET_CALIBRATED);
                colorList.set(i, colorInfo);
            }
        }
        int minQuality = PreferencesUtils.getInt(this, R.string.minPhotoQualityKey,
                Config.MINIMUM_PHOTO_QUALITY);

        ColorUtils.validateGradient(colorList, context.rangeIntervals.size(), context.rangeIncrementStep, minQuality);

    }

    /**
     * @param testType  The type of test
     * @param colorList List of swatch colors to be saved
     */
    public void saveCalibratedSwatches(int testType, ArrayList<Integer> colorList) {
        MainApp context = ((MainApp) this.getApplicationContext());
        assert context != null;

        for (int i = 0; i < colorList.size(); i++) {

            PreferencesUtils
                    .setInt(context.getApplicationContext(),
                            String.format("%d-%d", testType, i),
                            colorList.get(i));
            PreferencesUtils
                    .setInt(context.getApplicationContext(),
                            String.format("%d-a-%d", testType, i), 100);
        }
    }

    /**
     * @return The number of errors found
     */
    public int getCalibrationErrorCount() {
        MainApp mainApp = this;
        int minAccuracy = PreferencesUtils
                .getInt(mainApp, R.string.minPhotoQualityKey, Config.MINIMUM_PHOTO_QUALITY);

        int count = 0;
        for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
            final int index = i * mainApp.rangeIncrementStep;
            if (mainApp.colorList.get(index).getErrorCode() > 0 ||
                    (mainApp.colorList.get(index).getQuality() > -1 && mainApp.colorList.get(index).getQuality() < minAccuracy)) {
                count++;
            }
        }
        return count;
    }
}
