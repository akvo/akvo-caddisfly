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

package org.akvo.caddisfly.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.FileUtils;
import org.akvo.caddisfly.util.JsonUtils;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.json.JSONException;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;

public class MainApp extends Application {

    public final DecimalFormat doubleFormat = new DecimalFormat("0.0");
    public TestInfo currentTestInfo = new TestInfo(new Hashtable(), "", "", 0);

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

    public void setSwatches(String testCode) {
        testCode = testCode.toUpperCase();

        try {

            //final String path = getExternalFilesDir(null) + Config.CONFIG_FILE_PATH;
            final String path = Environment.getExternalStorageDirectory() + Config.CONFIG_FOLDER + Config.CONFIG_FILE;

            File file = new File(path);
            String text;

            //Look for external json config file otherwise use the internal default one
            if (file.exists()) {
                text = FileUtils.loadTextFromFile(path);
            } else {
                text = FileUtils.readRawTextFile(this, R.raw.tests_json);
            }
            currentTestInfo = JsonUtils.loadJson(text, testCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (currentTestInfo == null) {
            return;
        }

        if (currentTestInfo.getType() == 0) {
            loadCalibratedSwatches(currentTestInfo);
        }
    }

    /**
     * Load any user calibrated swatches
     *
     * @param testInfo The type of test
     */
    void loadCalibratedSwatches(TestInfo testInfo) {

        MainApp context = ((MainApp) this.getApplicationContext());

        for (ResultRange range : testInfo.getRanges()) {
            //if (PreferencesUtils.contains(context, String.format("%s-%.2f", testInfo.getCode(), range.getValue()))) {

            int color = PreferencesUtils.getInt(context, String.format("%s-%.2f", testInfo.getCode(), range.getValue()), -1);

//                int r = Color.red(color);
//                int g = Color.green(color);
//                int b = Color.blue(color);
//
//                // eliminate white and black colors
//                if (r == 255 && g == 255 && b == 255) {
//                    PreferencesUtils.setInt(this, String.format("%s-a-%d", testInfo.getCode(), range.getValue()), -1);
//                    color = -1;
//                }
//                if (r == 0 && g == 0 && b == 0) {
//                    PreferencesUtils.setInt(this, String.format("%s-a-%d", testInfo.getCode(), range.getValue()), -1);
//                    color = -1;
//                }
            range.setColor(color);
            //}
        }

        if (currentTestInfo.getRanges().size() > 0) {
            int startValue = (int) currentTestInfo.getRange(0).getValue() * 10;
            int endValue = (int) currentTestInfo.getRange(currentTestInfo.getRanges().size() - 1).getValue() * 10;
            for (int i = startValue; i <= endValue; i += 1) {
                String key = String.format("%s-%.2f", currentTestInfo.getCode(), (i / 10f));
                ResultRange range = new ResultRange((double) i / 10,
                        PreferencesUtils.getInt(context, key, -1));
                currentTestInfo.getSwatches().add(range);
            }
        }

        //int minQuality = PreferencesUtils.getInt(this, R.string.minPhotoQualityKey,  Config.MINIMUM_PHOTO_QUALITY);

        //ColorUtils.validateGradient(colorList, minQuality);

    }

    /**
     * @param testCode  The type of test
     * @param colorList List of swatch colors to be saved
     */
    public void saveCalibratedSwatches(String testCode, ArrayList<Integer> colorList) {
        testCode = testCode.toUpperCase();

        MainApp context = ((MainApp) this.getApplicationContext());
        assert context != null;

        for (int i = 0; i < colorList.size(); i++) {

            PreferencesUtils
                    .setInt(context.getApplicationContext(),
                            String.format("%s-%d", testCode, i),
                            colorList.get(i));
            PreferencesUtils
                    .setInt(context.getApplicationContext(),
                            String.format("%s-a-%d", testCode, i), 100);
        }
    }

    /**
     * @return The number of errors found
     */
    public int getCalibrationErrorCount() {
        MainApp mainApp = this;
        //int minAccuracy = PreferencesUtils
        //      .getInt(mainApp, R.string.minPhotoQualityKey, Config.MINIMUM_PHOTO_QUALITY);

        int count = 0;
        //for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
//            if (mainApp.colorList.get(index).getErrorCode() > 0 ||
//                    (mainApp.colorList.get(index).getQuality() > -1 && mainApp.colorList.get(index).getQuality() < minAccuracy)) {
//                count++;
//            }
        //}
        return count;
    }
}
