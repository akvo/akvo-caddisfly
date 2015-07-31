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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class MainApp extends Application {

    public static boolean hasCameraFlash = true;
    public TestInfo currentTestInfo = new TestInfo(new Hashtable(), "", "", TestType.COLORIMETRIC);

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
                    versionString += String.format(Locale.US, "%.2f", versionNumber);
                } catch (NumberFormatException e) {
                    int id = context.getResources()
                            .getIdentifier(word.toLowerCase(), "string", context.getPackageName());
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

    private String getJsonText() {
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

        return text;
    }

    public void initializeCurrentTest() {
        if (currentTestInfo.getCode().isEmpty()) {
            setDefaultTest();
        } else {
            setSwatches(currentTestInfo.getCode());
        }
    }

    public void setDefaultTest() {

        ArrayList<TestInfo> tests;
        try {
            tests = JsonUtils.loadTests(getJsonText());
            if (tests.size() > 0) {
                currentTestInfo = tests.get(0);
                if (currentTestInfo.getType() == TestType.COLORIMETRIC) {
                    loadCalibratedSwatches(currentTestInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSwatches(String testCode) {
        testCode = testCode.toUpperCase();

        try {
            currentTestInfo = JsonUtils.loadJson(getJsonText(), testCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (currentTestInfo == null) {
            return;
        }

        if (currentTestInfo.getType() == TestType.COLORIMETRIC) {
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
            String key = String.format(Locale.US, "%s-%.2f", testInfo.getCode(), range.getValue());

            int color = PreferencesUtils.getInt(context, key, 0);


            range.setColor(color);
        }

        if (currentTestInfo.getRanges().size() > 0) {
            int startValue = (int) (currentTestInfo.getRange(0).getValue() * 100);
            int endValue = (int) (currentTestInfo.getRange(currentTestInfo.getRanges().size() - 1).getValue() * 100);
            for (int i = startValue; i <= endValue; i += 1) {
                String key = String.format(Locale.US, "%s-%.2f", currentTestInfo.getCode(), (i / 100f));
                ResultRange range = new ResultRange((double) i / 100,
                        PreferencesUtils.getInt(context, key, 0));
                currentTestInfo.getSwatches().add(range);
            }
        }
    }

    public void storeCalibratedData(ResultRange range, final int resultColor) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String colorKey = String.format(Locale.US, "%s-%.2f", currentTestInfo.getCode(), range.getValue());

        if (resultColor == 0) {
            editor.remove(colorKey);
        } else {
            range.setColor(resultColor);
            editor.putInt(colorKey, resultColor);
            List list = ColorUtils.autoGenerateColors(currentTestInfo);

            for (int i = 0; i < list.size(); i++) {
                @SuppressWarnings("RedundantCast")
                String key = String.format(Locale.US, "%s-%.2f", currentTestInfo.getCode(), (double) ((Pair) list.get(i)).first);

                editor.putInt(key, (int) ((Pair) list.get(i)).second);

            }

        }
        editor.apply();
    }

    /**
     * @param rangeList List of swatch colors to be saved
     */
    public void saveCalibratedSwatches(ArrayList<ResultRange> rangeList) {

        for (ResultRange range : rangeList) {
            String key = String.format(Locale.US, "%s-%.2f", currentTestInfo.getCode(), range.getValue());

            PreferencesUtils.setInt(this, key, range.getColor());
        }
        loadCalibratedSwatches(currentTestInfo);

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        List list = ColorUtils.autoGenerateColors(currentTestInfo);

        for (int i = 0; i < list.size(); i++) {
            @SuppressWarnings("RedundantCast")
            String key = String.format(Locale.US, "%s-%.2f", currentTestInfo.getCode(), (double) ((Pair) list.get(i)).first);
            editor.putInt(key, (int) ((Pair) list.get(i)).second);

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

    public enum TestType {COLORIMETRIC, SENSOR}

}
