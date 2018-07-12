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

import android.util.SparseArray;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ConstantJsonKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.model.GroupType;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AssetsManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import timber.log.Timber;

import static org.akvo.caddisfly.common.Constants.MPN_TABLE_FILENAME;


/**
 * Utility functions to parse a text config json text.
 */
public final class TestConfigHelper {

    private static HashMap<String, MpnValue> mpnTable;

    private TestConfigHelper() {
    }

    /**
     * Get the most probable number for the key.
     */
    public static MpnValue getMpnValueForKey(String key) {
        if (mpnTable == null) {
            mpnTable = loadMpnTable();
        }
        return mpnTable.get(key);
    }

    private static HashMap<String, MpnValue> loadMpnTable() {

        HashMap<String, MpnValue> mapper = new HashMap<>();

        String jsonText = AssetsManager.getInstance().loadJsonFromAsset(MPN_TABLE_FILENAME);
        try {
            JSONArray array = new JSONObject(jsonText).getJSONArray("rows");

            for (int j = 0; j < array.length(); j++) {
                JSONObject item = array.getJSONObject(j);

                String key = item.getString("key");

                mapper.put(key, new MpnValue(item.getString("mpn"), item.getString("confidence"),
                        item.getString("riskCategory")));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mapper;
    }

    /**
     * Creates the json result containing the results for test.
     *
     * @param testInfo       information about the test
     * @param results        the results for the test
     * @param resultImageUrl the url of the image
     * @return the result in json format
     */
    public static JSONObject getJsonResult(TestInfo testInfo, SparseArray<String> results,
                                           SparseArray<String> brackets, String resultImageUrl) {

        JSONObject resultJson = new JSONObject();

        try {

            resultJson.put(ConstantJsonKey.TYPE, SensorConstants.TYPE_NAME);
            resultJson.put(ConstantJsonKey.NAME, testInfo.getName());
            resultJson.put(ConstantJsonKey.UUID, testInfo.getUuid());

            JSONArray resultsJsonArray = new JSONArray();
            for (Result subTest : testInfo.getResults()) {
                JSONObject subTestJson = new JSONObject();
                subTestJson.put(ConstantJsonKey.NAME, subTest.getName());
                subTestJson.put(ConstantJsonKey.UNIT, subTest.getUnit());
                subTestJson.put(ConstantJsonKey.ID, subTest.getId());

                // If a result exists for the sub test id then add it
                int id = subTest.getId();
                if (results.size() >= id) {
                    subTestJson.put(ConstantJsonKey.VALUE, results.get(id));

                    // if there is a bracket result, include it.
                    if (brackets != null && brackets.get(id) != null) {
                        subTestJson.put(ConstantJsonKey.BRACKET, brackets.get(id));
                    }
                }

                resultsJsonArray.put(subTestJson);

                if (testInfo.getGroupingType() == GroupType.GROUP) {
                    break;
                }
            }

            resultJson.put(ConstantJsonKey.RESULT, resultsJsonArray);

            if (resultImageUrl != null && !resultImageUrl.isEmpty()) {
                resultJson.put(ConstantJsonKey.IMAGE, resultImageUrl);
            }

            // Add current date to result
            resultJson.put(ConstantJsonKey.TEST_DATE, new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US)
                    .format(Calendar.getInstance().getTime()));

            // Add app details to the result
            resultJson.put(ConstantJsonKey.APP, TestConfigHelper.getAppDetails());

        } catch (JSONException e) {
            Timber.e(e);
        }
        return resultJson;
    }

    private static JSONObject getAppDetails() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("appVersion", CaddisflyApp.getAppVersion(true));
        return details;
    }
}
