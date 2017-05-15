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

package org.akvo.caddisfly.model;

import android.graphics.Color;
import android.support.annotation.StringRes;

import org.akvo.caddisfly.sensor.SensorConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Model to hold test configuration information.
 */
public class TestInfo {

    private static final double RESULT_ERROR_MARGIN = 0.2;
    private final String name;
    private final String uuid;
    private final List<Swatch> swatches;
    private final TestType testType;
    private final List<Integer> dilutions;
    private final List<SubTest> subTests = new ArrayList<>();
    private String unit;
    private boolean requiresCalibration;
    private boolean allInteger = true;
    private boolean mIsDirty;
    private int monthsValid = 12;
    private boolean isGroup;
    @StringRes
    private int groupName;
    private String batchNumber;
    private long calibrationDate;
    private long expiryDate;
    private boolean useGrayScale;
    private int hueTrend;
    private double[] rangeValues;
    private String deviceId;
    private String responseFormat;
    private boolean deprecated;
    private JSONArray instructions;

    public TestInfo(String name, TestType testType, String[] swatchArray,
                    String[] defaultColorsArray, String[] dilutionsArray,
                    String uuid, JSONArray resultsArray, JSONArray instructionsArray) {
        this.name = name;
        this.testType = testType;
        this.uuid = uuid;
        swatches = new ArrayList<>();
        dilutions = new ArrayList<>();

        instructions = instructionsArray;

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
                    subTests.add(new SubTest(patchObj.getInt("id"), patchObj.getString("name"),
                            patchObj.getString("unit"),
                            patchObj.has("timeDelay") ? patchObj.getInt("timeDelay") : 0));
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }

        if (subTests.size() > 0) {
            this.unit = subTests.get(0).getUnit();
        }
    }

    public TestInfo() {
        name = null;
        testType = TestType.COLORIMETRIC_LIQUID;
        this.uuid = "";
        this.unit = "";
        swatches = new ArrayList<>();
        dilutions = new ArrayList<>();
        this.requiresCalibration = false;
    }

    /**
     * Sort the swatches for this test by their result values.
     */
    private void sort() {
        Collections.sort(swatches, new Comparator<Swatch>() {
            public int compare(Swatch c1, Swatch c2) {
                return Double.compare(c1.getValue(), (c2.getValue()));
            }
        });
    }

    public String getName() {
        return name;
    }

    public TestType getType() {
        return testType;
    }

    public String getId() {
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

    /**
     * Gets if this test type requires calibration.
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
        return rangeValues.clone();
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public boolean getIsDeprecated() {
        return deprecated;
    }

    public void setIsDeprecated(boolean value) {
        this.deprecated = value;
    }

    public JSONArray getInstructions() {
        return instructions;
    }

    public static class SubTest {
        private final int id;
        private final String desc;
        private final String unit;
        private final int timeDelay;

        SubTest(int id, String desc, String unit, int timeDelay) {
            this.id = id;
            this.desc = desc;
            this.unit = unit;
            this.timeDelay = timeDelay * 1000;
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

        public int getTimeDelay() {
            return timeDelay;
        }

    }
}
