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

package org.akvo.caddisfly.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Pair;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.FileUtils;
import org.akvo.caddisfly.util.JsonUtils;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.json.JSONException;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MainApp extends Application {

    public static boolean hasCameraFlash = true;
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

    public void onCreate() {
        //AnalyticsTrackers.initialize(this);
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
                //ignore file if it is old version
                if (!text.contains("ranges")) {
                    text = FileUtils.readRawTextFile(this, R.raw.tests_json);
                }
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
    private void loadCalibratedSwatches(TestInfo testInfo) {

        MainApp context = ((MainApp) this.getApplicationContext());

        for (ResultRange range : testInfo.getRanges()) {
            //if (PreferencesUtils.contains(context, String.format("%s-%.2f", testInfo.getCode(), range.getValue()))) {

            int color = PreferencesUtils.getInt(context, String.format("%s-%.2f", testInfo.getCode(), range.getValue()), 0);

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
            int startValue = (int) (currentTestInfo.getRange(0).getValue() * 100);
            int endValue = (int) (currentTestInfo.getRange(currentTestInfo.getRanges().size() - 1).getValue() * 100);
            for (int i = startValue; i <= endValue; i += 1) {
                String key = String.format("%s-%.2f", currentTestInfo.getCode(), (i / 100f));
                ResultRange range = new ResultRange((double) i / 100,
                        PreferencesUtils.getInt(context, key, 0));
                currentTestInfo.getSwatches().add(range);
            }
        }

        //int minQuality = PreferencesUtils.getInt(this, R.string.minPhotoQualityKey,  Config.MINIMUM_PHOTO_QUALITY);

        //ColorUtils.validateGradient(colorList, minQuality);

    }

    public void storeCalibratedData(ResultRange range, final int resultColor) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String colorKey = String.format("%s-%.2f", currentTestInfo.getCode(), range.getValue());

        if (resultColor == 0) {
            editor.remove(colorKey);
        } else {
            range.setColor(resultColor);
            editor.putInt(colorKey, resultColor);
            List list = ColorUtils.autoGenerateColors(currentTestInfo);

            for (int i = 0; i < list.size(); i++) {
                //editor.putInt(((Pair) list.get(i)).first.toString(), (int) ((Pair) list.get(i)).second);

                editor.putInt(
                        String.format("%s-%.2f", currentTestInfo.getCode(), (double) ((Pair) list.get(i)).first),
                        (int) ((Pair) list.get(i)).second);

            }

        }
        editor.apply();
    }


    /**
     * @param rangeList List of swatch colors to be saved
     */
    public void saveCalibratedSwatches(ArrayList<ResultRange> rangeList) {

        MainApp context = ((MainApp) this.getApplicationContext());
        assert context != null;

        for (ResultRange range : rangeList) {
            PreferencesUtils
                    .setInt(context.getApplicationContext(),
                            String.format("%s-%.2f", currentTestInfo.getCode(), range.getValue()),
                            range.getColor());
        }
        loadCalibratedSwatches(currentTestInfo);

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        List list = ColorUtils.autoGenerateColors(currentTestInfo);

        for (int i = 0; i < list.size(); i++) {
            //editor.putInt(((Pair) list.get(i)).first.toString(), (int) ((Pair) list.get(i)).second);

            editor.putInt(
                    String.format("%s-%.2f", currentTestInfo.getCode(), (double) ((Pair) list.get(i)).first),
                    (int) ((Pair) list.get(i)).second);

        }

        editor.apply();
    }

    /**
     * @return The number of errors found
     */
    public int getCalibrationErrorCount() {
        int count = 0;
        for (int i = 0; i < currentTestInfo.getRanges().size(); i++) {
            if (currentTestInfo.getRange(i).getColor() == 0) {
                count++;
            }
        }
        return count;
    }
}
