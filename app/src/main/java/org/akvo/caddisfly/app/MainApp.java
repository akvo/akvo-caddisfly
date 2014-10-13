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
    public int rangeIncrementStep = 5;
    public int rangeStartIncrement = 0;
    public int currentTestType = Config.FLUORIDE_2_INDEX;
    public double rangeIncrementValue = 0.1;

    public DecimalFormat doubleFormat = new DecimalFormat("0.0");

    public MainApp() {
    }

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
/*
    public static String getPreviousVersion(Context context) {
        try {
            int version = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
            return String.valueOf(version - 1);

        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }

    }*/

    /**
     * Factory preset values for Fluoride
     */
    public void setFluorideSwatches() {
        //ArrayList<Integer> presetColorList = new ArrayList<Integer>();
        colorList.clear();
        rangeIntervals.clear();

        rangeIncrementStep = 30;
        rangeIncrementValue = 0.1;
        rangeStartIncrement = 0;
        doubleFormat = new DecimalFormat("0.0");

        currentTestType = Config.FLUORIDE_INDEX;

        for (double i = 0.0; i < 3.1; i += (rangeIncrementStep * rangeIncrementValue)) {
            rangeIntervals.add(i);
        }

        for (double i = 0; i < 31; i++) {
            colorList.add(new ColorInfo(Color.rgb(0, 0, 0), 100));
        }

        loadCalibratedSwatches(currentTestType);
    }

    /**
     * Factory preset values for Fluoride
     */
    public void setFluoride2Swatches() {
        //ArrayList<Integer> presetColorList = new ArrayList<Integer>();
        colorList.clear();
        rangeIntervals.clear();

        rangeIncrementStep = 5;
        rangeIncrementValue = 0.1;
        rangeStartIncrement = 0;
        doubleFormat = new DecimalFormat("0.0");

        currentTestType = Config.FLUORIDE_2_INDEX;

        for (double i = 0.0; i < 3.1; i += (rangeIncrementStep * rangeIncrementValue)) {
            rangeIntervals.add(i);
        }

        for (double i = 0; i < 31; i++) {
            colorList.add(new ColorInfo(Color.rgb(0, 0, 0), 100));
        }

        //colorList = new ArrayList<Integer>(presetColorList);
        loadCalibratedSwatches(currentTestType);
    }


    public void setSwatches(int testType) {
        switch (testType) {
            case Config.FLUORIDE_INDEX:
                setFluorideSwatches();
                break;
            case Config.FLUORIDE_2_INDEX:
                setFluoride2Swatches();
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

    public int getCalibrationErrorCount() {
        MainApp mainApp = this;
        int minAccuracy = PreferencesUtils
                .getInt(mainApp, R.string.minPhotoQualityKey, Config.MINIMUM_PHOTO_QUALITY);

        int count = 0;
        for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
            final int index = i * mainApp.rangeIncrementStep;
/*
            int accuracy = Math.max(-1, PreferencesUtils.getInt(mainApp, String
                    .format("%d-a-%d", testType, index), -1));
*/
            if (mainApp.colorList.get(index).getErrorCode() > 0 ||
                    (mainApp.colorList.get(index).getQuality() > -1 && mainApp.colorList.get(index).getQuality() < minAccuracy)) {
                count++;
            }
        }
        return count;
    }
}
