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
import android.util.SparseArray;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ConstantJsonKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.model.GroupType;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AssetsManager;
import org.akvo.caddisfly.util.PreferencesUtil;
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

    // Files
    private static final int DEFAULT_MONTHS_VALID = 6;
    private static final int BIT_MASK = 0x00FFFFFF;

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
     * @param color          the color extracted
     * @param resultImageUrl the url of the image
     * @return the result in json format
     */
    public static JSONObject getJsonResult(TestInfo testInfo, SparseArray<String> results,
                                           SparseArray<String> brackets, int color,
                                           String resultImageUrl) {

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

                if (color > -1) {
                    subTestJson.put("resultColor", Integer.toHexString(color & BIT_MASK));

                    // todo: fix this
                    // Add calibration details to result
//                    subTestJson.put("calibratedDate", testInfo.getCalibrationDateString());
//                    subTestJson.put("reagentExpiry", testInfo.getExpiryDateString());
//                    subTestJson.put("reagentBatch", testInfo.getBatchNumber());
//
//                    JSONArray calibrationSwatches = new JSONArray();
//                    for (Swatch swatch : testInfo.getSwatches()) {
//                        calibrationSwatches.put(Integer.toHexString(swatch.getColor() & BIT_MASK));
//                    }
//                    subTestJson.put("calibration", calibrationSwatches);
                }

                resultsJsonArray.put(subTestJson);

                if (testInfo.getGroupingType() == GroupType.GROUP) {
                    break;
                }
            }

            resultJson.put(ConstantJsonKey.RESULT, resultsJsonArray);

            if (!resultImageUrl.isEmpty()) {
                resultJson.put(ConstantJsonKey.IMAGE, resultImageUrl);
            }

            // Add current date to result
            resultJson.put(ConstantJsonKey.TEST_DATE, new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US)
                    .format(Calendar.getInstance().getTime()));

            // Add user preference details to the result
            resultJson.put(ConstantJsonKey.USER, TestConfigHelper.getUserPreferences());

            // Add app details to the result
            resultJson.put(ConstantJsonKey.APP, TestConfigHelper.getAppDetails());

            // Add standard diagnostic details to the result
            resultJson.put(ConstantJsonKey.DEVICE, TestConfigHelper.getDeviceDetails());

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
            return getUuidByShortCode(shortCode, Constants.TESTS_META_FILENAME);
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    @Nullable
    private static String getUuidByShortCode(String shortCode, String filename) {
        // Load the pre-configured tests from the app
        String jsonText = AssetsManager.getInstance().loadJsonFromAsset(filename);
        try {
            JSONArray array = new JSONObject(jsonText).getJSONArray("tests");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item.has(ConstantJsonKey.SHORT_CODE)
                        && shortCode.equalsIgnoreCase(item.getString(ConstantJsonKey.SHORT_CODE))) {
                    return item.getString(ConstantJsonKey.UUID);
                }
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }
}
