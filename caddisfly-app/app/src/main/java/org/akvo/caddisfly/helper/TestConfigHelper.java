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

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.SparseArray;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static org.akvo.caddisfly.sensor.SensorConstants.DEPRECATED_TESTS_FILENAME;

/**
 * Utility functions to parse a text config json text.
 */
public final class TestConfigHelper {

    // Files
    private static final int DEFAULT_MONTHS_VALID = 6;
    private static final int BIT_MASK = 0x00FFFFFF;

    private TestConfigHelper() {
    }

    /**
     * Returns a TestInfo instance filled with test config for the given uuid.
     *
     * @param uuid the test uuid
     * @return the TestInfo instance
     */
    public static TestInfo loadTestByUuid(String uuid) {

        if (uuid != null && !uuid.isEmpty()) {

            for (int i = 0; i < 3; i++) {

                String jsonText;
                switch (i) {
                    case 1:
                        // Load any custom tests from the custom test config file
                        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG),
                                SensorConstants.TESTS_META_FILENAME);
                        jsonText = FileUtil.loadTextFromFile(file);
                        break;
                    case 2:
                        // Load any experimental tests if app is in diagnostic mode
                        jsonText = AssetsManager.getInstance().loadJSONFromAsset(DEPRECATED_TESTS_FILENAME);
                        break;
                    default:
                        // Load the pre-configured tests from the app
                        jsonText = AssetsManager.getInstance().loadJSONFromAsset(SensorConstants.TESTS_META_FILENAME);
                }
                try {
                    JSONArray array = new JSONObject(jsonText).getJSONArray("tests");
                    for (int j = 0; j < array.length(); j++) {
                        JSONObject item = array.getJSONObject(j);

                        String newUuid = item.getString(SensorConstants.UUID);
                        if (uuid.equalsIgnoreCase(newUuid)) {
                            return loadTest(item);
                        }
                    }

                } catch (JSONException e) {
                    Timber.e(e);
                }

            }
        }
        return null;
    }

    /**
     * Load all the tests and their configurations from the json config text.
     *
     * @return ArrayList of TestInfo instances filled with config
     */
    public static List<TestInfo> loadTestsList() {

        List<TestInfo> tests = new ArrayList<>();

        // Load the pre-configured tests from the app
        loadTests(tests, AssetsManager.getInstance().loadJSONFromAsset(SensorConstants.TESTS_META_FILENAME), -1);

        loadTests(tests, AssetsManager.getInstance().loadJSONFromAsset(DEPRECATED_TESTS_FILENAME), -1);

        // Load any custom tests from the custom test config file
        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG), SensorConstants.TESTS_META_FILENAME);
        if (file.exists()) {
            loadTests(tests, FileUtil.loadTextFromFile(file), R.string.customTests);
        }


        return tests;
    }

    private static void loadTests(List<TestInfo> tests, String jsonText,
                                  @StringRes int groupName) {

        int groupIndex = tests.size();

        JSONArray array;
        try {
            array = new JSONObject(jsonText).getJSONArray("tests");
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject item = array.getJSONObject(i);
                    String uuid = item.getString(SensorConstants.UUID);

                    // get uuids
                    boolean found = false;
                    for (TestInfo test : tests) {
                        if (uuid.equalsIgnoreCase(test.getId())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }

                    TestInfo testInfo = loadTest(item);

                    //Create TestInfo object
                    tests.add(testInfo);

                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            // If custom tests were added then add a dummy test object for group name
            if (tests.size() > groupIndex && groupName != -1) {
                TestInfo testGroup = new TestInfo();
                testGroup.setGroup(true);
                testGroup.setRequiresCalibration(true);
                testGroup.setGroupName(groupName);
                tests.add(groupIndex, testGroup);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }

    }

    private static TestInfo loadTest(JSONObject item) {

        TestInfo testInfo = null;
        try {
            //Get the test type
            TestType type;
            if (item.has("subtype")) {
                switch (item.getString("subtype")) {
                    case "liquid-chamber":
                        type = TestType.COLORIMETRIC_LIQUID;
                        break;
                    case "strip":
                    case "striptest":
                        type = TestType.COLORIMETRIC_STRIP;
                        break;
                    case "sensor":
                        type = TestType.SENSOR;
                        break;
                    default:
                        return null;
                }
            } else {
                return null;
            }

            //Get the name for this test
            String name = item.getString("name");

            //Load results
            JSONArray resultsArray = null;
            if (item.has("results")) {
                resultsArray = item.getJSONArray("results");
            }

            //Load the dilution percentages
            String dilutions = "0";
            if (item.has("dilutions")) {
                dilutions = item.getString("dilutions");
                if (dilutions.isEmpty()) {
                    dilutions = "0";
                }
            }
            String[] dilutionsArray = dilutions.split(",");

            //Load the ranges
            String ranges = "0";
            if (item.has("ranges")) {
                ranges = item.getString("ranges");
            }

            String[] rangesArray = ranges.split(",");

            String[] defaultColorsArray = new String[0];
            if (item.has("defaultColors")) {
                String defaultColors = item.getString("defaultColors");
                defaultColorsArray = defaultColors.split(",");
            }

            // get uuids
            String uuid = item.getString(SensorConstants.UUID);

            testInfo = new TestInfo(name, type, rangesArray,
                    defaultColorsArray, dilutionsArray, uuid, resultsArray);

            testInfo.setHueTrend(item.has("hueTrend") ? item.getInt("hueTrend") : 0);

            testInfo.setDeviceId(item.has("deviceId") ? item.getString("deviceId") : "Unknown");

            testInfo.setResponseFormat(item.has("responseFormat") ? item.getString("responseFormat") : "");

            testInfo.setUseGrayScale(item.has("grayScale") && item.getBoolean("grayScale"));

            testInfo.setMonthsValid(item.has("monthsValid") ? item.getInt("monthsValid") : DEFAULT_MONTHS_VALID);

            //if calibrate not specified then default to false otherwise use specified value
            testInfo.setRequiresCalibration(item.has("calibrate") && item.getBoolean("calibrate"));

            testInfo.setIsDeprecated(item.has("deprecated") && item.getBoolean("deprecated"));

        } catch (JSONException e) {
            Timber.e(e);
        }

        return testInfo;
    }

    /**
     * Creates the json result containing the results for test.
     * @param testInfo information about the test
     * @param results the results for the test
     * @param color the color extracted
     * @param resultImageUrl the url of the image
     * @param groupingType type of grouping
     * @return the result in json format
     */
    public static JSONObject getJsonResult(TestInfo testInfo, SparseArray<String> results, int color,
                                           String resultImageUrl, StripTest.GroupType groupingType) {

        JSONObject resultJson = new JSONObject();

        try {

            resultJson.put(SensorConstants.TYPE, SensorConstants.TYPE_NAME);
            resultJson.put(SensorConstants.NAME, testInfo.getName());
            resultJson.put(SensorConstants.UUID, testInfo.getId());


            JSONArray resultsJsonArray = new JSONArray();
            for (TestInfo.SubTest subTest : testInfo.getSubTests()) {
                JSONObject subTestJson = new JSONObject();
                subTestJson.put(SensorConstants.NAME, subTest.getDesc());
                subTestJson.put(SensorConstants.UNIT, subTest.getUnit());
                subTestJson.put(SensorConstants.ID, subTest.getId());

                // If a result exists for the sub test id then add it
                if (results.size() >= subTest.getId()) {
                    subTestJson.put(SensorConstants.VALUE, results.get(subTest.getId()));
                }

                if (color > -1) {
                    subTestJson.put("resultColor", Integer.toHexString(color & BIT_MASK));

                    // Add calibration details to result
                    subTestJson.put("calibratedDate", testInfo.getCalibrationDateString());
                    subTestJson.put("reagentExpiry", testInfo.getExpiryDateString());
                    subTestJson.put("reagentBatch", testInfo.getBatchNumber());

                    JSONArray calibrationSwatches = new JSONArray();
                    for (Swatch swatch : testInfo.getSwatches()) {
                        calibrationSwatches.put(Integer.toHexString(swatch.getColor() & BIT_MASK));
                    }
                    subTestJson.put("calibration", calibrationSwatches);
                }

                resultsJsonArray.put(subTestJson);

                if (groupingType == StripTest.GroupType.GROUP) {
                    break;
                }
            }

            resultJson.put(SensorConstants.RESULT, resultsJsonArray);

            if (!resultImageUrl.isEmpty()) {
                resultJson.put(SensorConstants.IMAGE, resultImageUrl);
            }

            // Add current date to result
            resultJson.put("testDate", new SimpleDateFormat(SensorConstants.DATE_TIME_FORMAT, Locale.US)
                    .format(Calendar.getInstance().getTime()));

            // Add user preference details to the result
            resultJson.put(SensorConstants.USER, TestConfigHelper.getUserPreferences());

            // Add app details to the result
            resultJson.put(SensorConstants.APP, TestConfigHelper.getAppDetails());

            // Add standard diagnostic details to the result
            resultJson.put(SensorConstants.DEVICE, TestConfigHelper.getDeviceDetails());

        } catch (JSONException e) {
            Timber.e(e);
        }
        return resultJson;
    }

    private static JSONObject getDeviceDetails() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("model", Build.MODEL);
        details.put("product", Build.PRODUCT);
        details.put("manufacturer", Build.MANUFACTURER);
        details.put("os", "Android - " + Build.VERSION.RELEASE + " ("
                + Build.VERSION.SDK_INT + ")");
        details.put("country", Locale.getDefault().getCountry());
        details.put("language", Locale.getDefault().getLanguage());
        return details;
    }

    private static JSONObject getAppDetails() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("appVersion", CaddisflyApp.getAppVersion());
        // The current active language of the app
        details.put("language", CaddisflyApp.getAppLanguage());
        return details;
    }

    private static JSONObject getUserPreferences() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("backDropDetection", !AppPreferences.getNoBackdropDetection());
        details.put("language", PreferencesUtil.getString(CaddisflyApp.getApp(), R.string.languageKey, ""));
        return details;
    }

    /**
     * Returns a Uuid for the given shortCode.
     *
     * @param shortCode the test shortCode
     * @return the Uuid
     */
    @Deprecated
    public static String getUuidFromShortCode(String shortCode) {

        if (!shortCode.isEmpty()) {
            String uuid = getUuidByShortCode(shortCode, SensorConstants.TESTS_META_FILENAME);
            if (uuid == null) {
                uuid = getUuidByShortCode(shortCode, DEPRECATED_TESTS_FILENAME);
            }
            return uuid;
        }
        return null;
    }

    @Nullable
    private static String getUuidByShortCode(String shortCode, String filename) {
        // Load the pre-configured tests from the app
        String jsonText = AssetsManager.getInstance().loadJSONFromAsset(filename);
        try {
            JSONArray array = new JSONObject(jsonText).getJSONArray("tests");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item.has(SensorConstants.SHORT_CODE)
                        && shortCode.equalsIgnoreCase(item.getString(SensorConstants.SHORT_CODE))) {
                    return item.getString(SensorConstants.UUID);
                }
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }
}
