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

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Utility functions to parse a text config json text
 */
public final class TestConfigHelper {

    private TestConfigHelper() {
    }

    /**
     * Returns a TestInfo instance filled with test config for the given test code
     *
     * @param jsonText the json config text
     * @param testCode the test code
     * @return the TestInfo instance
     */
    @Deprecated
    public static TestInfo loadTestConfigurationByCode(String jsonText, String testCode) {

        ArrayList<TestInfo> tests = loadConfigurationsForAllTests(jsonText);

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
     * @param jsonText the json config text
     * @param uuid     the test uuid
     * @return the TestInfo instance
     */
    public static TestInfo loadTestConfigurationByUuid(String jsonText, String uuid) {

        ArrayList<TestInfo> tests = loadConfigurationsForAllTests(jsonText);

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
     * @param jsonText the json text
     * @return ArrayList of TestInfo instances filled with config
     */
    public static ArrayList<TestInfo> loadConfigurationsForAllTests(String jsonText) {

        ArrayList<TestInfo> tests = new ArrayList<>();
        try {
            JSONArray array;

            try {
                array = new JSONObject(jsonText).getJSONArray("tests");
            } catch (JSONException e) {
                //TODO: Backward compatibility can be removed
                array = new JSONObject(jsonText).getJSONObject("tests").getJSONArray("test");
            }

            for (int i = 0; i < array.length(); i++) {

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
                    JSONArray uuid = item.getJSONArray("uuid");
                    ArrayList<String> uuids = new ArrayList<>();
                    for (int ii = 0; ii < uuid.length(); ii++) {
                        uuids.add(uuid.getString(ii));
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

                    //Load the ranges
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
                            //if calibrate not specified then default to true otherwise use specified value
                            !item.has("calibrate") || item.getString("calibrate").equalsIgnoreCase("true"),
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