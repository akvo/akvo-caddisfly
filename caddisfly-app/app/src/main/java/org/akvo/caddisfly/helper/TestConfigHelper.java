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

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Utility functions to parse a text config json text
 */
public final class TestConfigHelper {

    // Files
    private static final String CONFIG_FILE = "tests.json";

    private TestConfigHelper() {
    }

    /**
     * Returns a TestInfo instance filled with test config for the given test code
     *
     * @param testCode the test code
     * @return the TestInfo instance
     */
    @Deprecated
    public static TestInfo loadTestConfigurationByCode(String testCode) {

        ArrayList<TestInfo> tests = loadConfigurationsForAllTests();

        for (TestInfo test : tests) {
            if (test.getCode().equalsIgnoreCase(testCode)) {
                return test;
            }
        }
        return null;
    }

    /**
     * Returns a TestInfo instance filled with test config for the given uuid
     *
     * @param uuid the test uuid
     * @return the TestInfo instance
     */
    public static TestInfo loadTestConfigurationByUuid(String uuid) {

        ArrayList<TestInfo> tests = loadConfigurationsForAllTests();

        for (TestInfo test : tests) {
            if (test.getUuid().contains(uuid)) {
                return test;
            }
        }
        return null;
    }

    /**
     * Load all the tests and their configurations from the json config text
     *
     * @return ArrayList of TestInfo instances filled with config
     */
    public static ArrayList<TestInfo> loadConfigurationsForAllTests() {

        JSONArray customTestsArray = null, array;
        ArrayList<TestInfo> tests = new ArrayList<>();

        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG), CONFIG_FILE);

        //Look for external json config file otherwise use the internal default one
        String jsonText;
        if (file.exists()) {
            jsonText = FileUtil.loadTextFromFile(file);

            try {
                customTestsArray = new JSONObject(jsonText).getJSONArray("tests");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        jsonText = FileUtil.readRawTextFile(CaddisflyApp.getApp(), R.raw.tests_config);
        try {

            array = new JSONObject(jsonText).getJSONArray("tests");

            int customIndex = -1;
            if (customTestsArray != null) {
                customIndex = array.length() - 1;
                for (int i = 0; i < customTestsArray.length(); i++) {
                    array.put(customTestsArray.get(i));
                }
            }

            int experimentalIndex = -1;
            if (AppPreferences.isDiagnosticMode()) {
                jsonText = FileUtil.readRawTextFile(CaddisflyApp.getApp(), R.raw.experimental_tests_config);
                JSONArray experimentalArray = new JSONObject(jsonText).getJSONArray("tests");

                experimentalIndex = array.length();

                for (int i = 0; i < experimentalArray.length(); i++) {
                    array.put(experimentalArray.get(i));
                }
            }

            for (int i = 0; i < array.length(); i++) {

                if (customIndex == i || experimentalIndex == i) {
                    TestInfo testGroup = new TestInfo();
                    testGroup.setGroup(true);
                    if (experimentalIndex == i) {
                        testGroup.setIsDiagnostic(true);
                    }
                    testGroup.setRequiresCalibration(true);
                    tests.add(testGroup);
                }

                try {
                    JSONObject item = array.getJSONObject(i);

                    //Get the test type
                    CaddisflyApp.TestType type;
                    if (item.has("subtype")) {
                        switch (item.getString("subtype")) {
                            case "color":
                            case "colour":
                                type = CaddisflyApp.TestType.COLORIMETRIC_LIQUID;
                                break;
                            case "strip":
                            case "striptest":
                                type = CaddisflyApp.TestType.COLORIMETRIC_STRIP;
                                break;
                            case "sensor":
                                type = CaddisflyApp.TestType.SENSOR;
                                break;
                            case "coliform":
                            case "coliforms":
                                type = CaddisflyApp.TestType.TURBIDITY_COLIFORMS;
                                break;
                            default:
                                //Invalid test type skip it
                                continue;
                        }
                    } else if (item.has("type")) {
                        // Backward compatibility
                        switch (item.getInt("type")) {
                            case 0:
                                type = CaddisflyApp.TestType.COLORIMETRIC_LIQUID;
                                break;
                            case 1:
                                type = CaddisflyApp.TestType.COLORIMETRIC_STRIP;
                                break;
                            case 2:
                                type = CaddisflyApp.TestType.SENSOR;
                                break;
                            case 3:
                                type = CaddisflyApp.TestType.TURBIDITY_COLIFORMS;
                                break;
                            default:
                                //Invalid test type skip it
                                continue;
                        }
                    } else {
                        //Invalid test type skip it
                        continue;
                    }

                    // get uuids
                    JSONArray uuidArray = item.getJSONArray("uuid");
                    ArrayList<String> uuids = new ArrayList<>();
                    for (int ii = 0; ii < uuidArray.length(); ii++) {

                        String newUuid = uuidArray.getString(ii);
                        boolean found = false;
                        for (TestInfo test : tests) {
                            for (String uuid : test.getUuid()) {
                                if (uuid.equalsIgnoreCase(newUuid)) {
                                    found = true;
                                }
                            }

                        }
                        if (!found) {
                            uuids.add(newUuid);
                        }
                    }

                    if (uuids.isEmpty()) {
                        continue;
                    }

                    //Get the name for this test
                    JSONArray nameArray = item.getJSONArray("name");

                    Hashtable<String, String> namesHashTable =
                            new Hashtable<>(nameArray.length(), nameArray.length());

                    //Load test names in different languages
                    for (int j = 0; j < nameArray.length(); j++) {
                        if (!nameArray.isNull(j)) {
                            Iterator iterator = nameArray.getJSONObject(j).keys();
                            String key = (String) iterator.next();
                            String name = nameArray.getJSONObject(j).getString(key);
                            namesHashTable.put(key, name);
                        }
                    }

                    //Load results
                    JSONArray resultsArray = null;
                    if (item.has("results")) {
                        resultsArray = item.getJSONArray("results");
                    }

                    //Load the dilution percentages
                    String dilutions = "0";
                    if (item.has("dilutions")) {
                        dilutions = item.getString("dilutions");
                    }
                    String[] dilutionsArray = dilutions.split(",");

                    //Load the ranges
                    String ranges = "0";
                    if (item.has("ranges")) {
                        ranges = item.getString("ranges");
                    }

                    String[] rangesArray = ranges.split(",");

                    boolean isDiagnostic = false;
                    if (item.has("diagnostic")) {
                        isDiagnostic = item.getString("diagnostic").equalsIgnoreCase("true");
                    }

                    //Load the ranges
                    int monthsValid = 6;
                    if (item.has("monthsValid")) {
                        monthsValid = item.getInt("monthsValid");
                    }

                    String[] defaultColorsArray = new String[0];
                    if (item.has("defaultColors")) {
                        String defaultColors = item.getString("defaultColors");
                        defaultColorsArray = defaultColors.split(",");
                    }

                    //Create TestInfo object
                    tests.add(new TestInfo(
                            namesHashTable,
                            item.has("code") ? item.getString("code").toUpperCase() : "",
                            item.has("unit") ? item.getString("unit") : "",
                            type,
                            //if calibrate not specified then default to false otherwise use specified value
                            item.has("calibrate") && item.getString("calibrate").equalsIgnoreCase("true"),
                            rangesArray, defaultColorsArray,
                            dilutionsArray, isDiagnostic, monthsValid, uuids, resultsArray));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tests;
    }
}