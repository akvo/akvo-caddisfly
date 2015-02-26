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

package org.akvo.caddisfly.util;

import android.graphics.Color;

import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class JsonUtils {

    public static TestInfo loadJson(String jsonText, String testCode) throws JSONException {
        ArrayList<TestInfo> tests = loadTests(jsonText);

        for (TestInfo test : tests) {
            if (test.getCode().equalsIgnoreCase(testCode)) {
                return test;
            }
        }
        return null;
    }


    public static ArrayList<TestInfo> loadTests(String jsonText) throws JSONException {
        JSONObject jsonObject;
        TestInfo testInfo;

        ArrayList<TestInfo> tests = new ArrayList<>();
        jsonObject = new JSONObject(jsonText).getJSONObject("tests");
        JSONArray array = jsonObject.getJSONArray("test");
        for (int i = 0; i < array.length(); i++) {

            try {
                JSONObject item = array.getJSONObject(i);

                JSONArray nameArray = item.getJSONArray("name");

                Hashtable<String, String> namesHashTable = new Hashtable<>(10, 10);

                //Load test names in different languages
                for (int j = 0; j < nameArray.length(); j++) {
                    if (!nameArray.isNull(j)) {
                        Iterator iterator = nameArray.getJSONObject(j).keys();
                        String key = (String) iterator.next();
                        String name = nameArray.getJSONObject(j).getString(key);
                        namesHashTable.put(key, name);
                    }
                }

                testInfo = new TestInfo(
                        namesHashTable,
                        item.getString("code").toUpperCase(),
                        item.getString("unit"),
                        item.getInt("type"));

                //Load the dilution percentages
//                String dilutions = "0";
//                if (item.has("dilutions")) {
//                    dilutions = item.getString("dilutions");
//                }
//
//                String[] dilutionsArray = dilutions.split(",");
//
//                for (String dilution : dilutionsArray) {
//                    ResultRange resultRange = new ResultRange(
//                            Double.valueOf(dilution));
//                    testInfo.addRange(resultRange);
//                }

                String ranges = "0";
                if (item.has("ranges")) {
                    ranges = item.getString("ranges");
                }

                String[] rangesArray = ranges.split(",");

                for (String range : rangesArray) {
                    ResultRange resultRange = new ResultRange(Double.valueOf(range), Color.TRANSPARENT);
                    testInfo.addRange(resultRange);
                }

                tests.add(testInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tests;
    }
}
