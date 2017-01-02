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

package org.akvo.caddisfly.model;

import android.graphics.Color;
import android.support.annotation.StringRes;
import android.util.Log;

import org.akvo.caddisfly.sensor.SensorConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Model to hold test configuration information
 */
public class TestInfo {

    private static final String TAG = "TestInfo";

    private static final double RESULT_ERROR_MARGIN = 0.2;
    private final HashMap names;
    private final List<String> uuid;
    private final List<Swatch> swatches;
    private final TestType testType;
    private final List<Integer> dilutions;
    private final List<SubTest> subTests = new ArrayList<>();
    private String unit;
    private boolean requiresCalibration;
    private boolean allInteger = true;
    private boolean isDiagnostic;
    private boolean mIsDirty;
    private int monthsValid = 12;
    private boolean isGroup;
    @StringRes
    private int groupName;
    private String batchNumber;
    private long calibrationDate;
    private long expiryDate;
    private boolean useGrayScale;
    private String shortCode;
    private int hueTrend;
    private double[] rangeValues;
    private String deviceId;

    public TestInfo(HashMap names, TestType testType, String[] swatchArray,
                    String[] defaultColorsArray, String[] dilutionsArray,
                    List<String> uuids, JSONArray resultsArray) {
        this.names = names == null ? null : (HashMap) names.clone();
        this.testType = testType;
        this.uuid = uuids;
        swatches = new ArrayList<>();
        dilutions = new ArrayList<>();

        rangeValues = new double[swatchArray.length];

        for (int i = 0; i < swatchArray.length; i++) {

            String range = swatchArray[i];
            int defaultColor = Color.TRANSPARENT;
            if (defaultColorsArray.length > i) {
                String hexColor = defaultColorsArray[i].trim();
                if (!hexColor.contains("#")) {
                    hexColor = "#" + hexColor;
                }
                defaultColor = Color.parseColor(hexColor);
            }

            Swatch swatch = new Swatch((Double.valueOf(range) * 10) / 10f,
                    Color.TRANSPARENT, defaultColor);

            if (allInteger && swatch.getValue() % 1 != 0) {
                allInteger = false;
            }
            addSwatch(swatch);

            rangeValues[i] = Double.valueOf(range);
        }

        if (swatches.size() > 0) {
            Swatch previousSwatch = swatches.get(0);
            Swatch swatch;
            for (int i = 1; i < swatches.size(); i++) {
                swatch = swatches.get(i);
                int redDifference = Color.red(swatch.getDefaultColor()) - Color.red(previousSwatch.getDefaultColor());
                int greenDifference = Color.green(swatch.getDefaultColor()) - Color.green(previousSwatch.getDefaultColor());
                int blueDifference = Color.blue(swatch.getDefaultColor()) - Color.blue(previousSwatch.getDefaultColor());

                swatch.setRedDifference(redDifference);
                swatch.setGreenDifference(greenDifference);
                swatch.setBlueDifference(blueDifference);

                previousSwatch = swatch;
            }
        }

        for (String dilution : dilutionsArray) {
            addDilution(Integer.parseInt(dilution));
        }

        if (resultsArray != null) {
            for (int ii = 0; ii < resultsArray.length(); ii++) {
                try {
                    JSONObject patchObj = resultsArray.getJSONObject(ii);
                    subTests.add(new SubTest(patchObj.getInt("id"), patchObj.getString("description"), patchObj.getString("unit")));
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        if (subTests.size() > 0) {
            this.unit = subTests.get(0).getUnit();
        }
    }

    public TestInfo() {
        names = null;
        testType = TestType.COLORIMETRIC_LIQUID;
        this.uuid = new ArrayList<>();
        this.unit = "";
        swatches = new ArrayList<>();
        dilutions = new ArrayList<>();
        this.requiresCalibration = false;
    }

    /**
     * Sort the swatches for this test by their result values
     */
    private void sort() {
        Collections.sort(swatches, new Comparator<Swatch>() {
            public int compare(Swatch c1, Swatch c2) {
                return Double.compare(c1.getValue(), (c2.getValue()));
            }
        });
    }

    public String getName() {
        return getName("en");
    }

    public String getName(String languageCode) {
        if (names != null) {
            if (names.containsKey(languageCode)) {
                return names.get(languageCode).toString();
            } else if (names.containsKey("en")) {
                return names.get("en").toString();
            }
        }
        return "";
    }

    public TestType getType() {
        return testType;
    }

    public String getCode() {
        return uuid.size() > 0 ? uuid.get(0) : "";
    }

    public List<String> getUuid() {
        return uuid;
    }

    public String getUnit() {
        return unit;
    }

    public List<Swatch> getSwatches() {
        //ensure that swatches is always sorted
        if (mIsDirty) {
            mIsDirty = false;
            sort();
        }
        return swatches;
    }

    public double getDilutionRequiredLevel() {
        Swatch swatch = swatches.get(swatches.size() - 1);
        return swatch.getValue() - RESULT_ERROR_MARGIN;
    }

    public void addSwatch(Swatch value) {
        swatches.add(value);
        mIsDirty = true;
    }

    public Swatch getSwatch(int position) {
        return swatches.get(position);
    }

    private void addDilution(int dilution) {
        dilutions.add(dilution);
    }

    public boolean getCanUseDilution() {
        return dilutions.size() > 1;
    }

    public boolean getIsDiagnostic() {
        return isDiagnostic;
    }

    @SuppressWarnings("SameParameterValue")
    public void setIsDiagnostic(boolean value) {
        isDiagnostic = value;
    }

    /**
     * Gets if this test type requires calibration
     *
     * @return true if calibration required
     */
    public boolean getRequiresCalibration() {
        return requiresCalibration;
    }

    @SuppressWarnings("SameParameterValue")
    public void setRequiresCalibration(boolean value) {
        requiresCalibration = value;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean requiresCameraFlash() {
        return testType == TestType.COLORIMETRIC_LIQUID;
    }

    public int getMonthsValid() {
        return monthsValid;
    }

    public void setMonthsValid(int monthsValid) {
        this.monthsValid = monthsValid;
    }

    public boolean hasDecimalPlace() {
        return !allInteger;
    }

    public List<SubTest> getSubTests() {
        return subTests;
    }

    public boolean isGroup() {
        return isGroup;
    }

    @SuppressWarnings("SameParameterValue")
    public void setGroup(boolean group) {
        isGroup = group;
    }

    public int getGroupName() {
        return groupName;
    }

    public void setGroupName(@StringRes int groupName) {
        this.groupName = groupName;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public long getCalibrationDate() {
        return calibrationDate;
    }

    public void setCalibrationDate(long calibrationDate) {
        this.calibrationDate = calibrationDate;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCalibrationDateString() {
        return new SimpleDateFormat(SensorConstants.DATE_TIME_FORMAT, Locale.US).format(calibrationDate);
    }

    public String getExpiryDateString() {
        return new SimpleDateFormat(SensorConstants.DATE_FORMAT, Locale.US)
                .format(Calendar.getInstance().getTime());
    }

    public boolean isUseGrayScale() {
        return useGrayScale;
    }

    public void setUseGrayScale(boolean useGrayScale) {
        this.useGrayScale = useGrayScale;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public int getHueTrend() {
        return hueTrend;
    }

    public void setHueTrend(int hueTrend) {
        this.hueTrend = hueTrend;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double[] getRangeValues() {
        return rangeValues;
    }

    public void setRangeValues(double[] rangeValues) {
        this.rangeValues = rangeValues;
    }

    public static class SubTest {
        private final int id;
        private final String desc;
        private final String unit;

        SubTest(int id, String desc, String unit) {
            this.id = id;
            this.desc = desc;
            this.unit = unit;
        }

        public int getId() {
            return id;
        }

        public String getDesc() {
            return desc;
        }

        public String getUnit() {
            return unit;
        }

    }
}
