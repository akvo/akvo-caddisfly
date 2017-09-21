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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import org.akvo.caddisfly.sensor.SensorConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Model to hold test configuration information.
 */
public class TestInfo implements Parcelable {

    public static final Creator<TestInfo> CREATOR = new Creator<TestInfo>() {
        @Override
        public TestInfo createFromParcel(Parcel in) {
            return new TestInfo(in);
        }

        @Override
        public TestInfo[] newArray(int size) {
            return new TestInfo[size];
        }
    };
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
    private String md610Id;
    private String selectInstruction;
    private ArrayList<String> reagents;
    private Serializable sampleQuantity;
    private ArrayList<String> reactionTimes;
    private String title;
    private String brand;
    private String subtitleExtra;

    public TestInfo(String name, TestType testType, String[] swatchArray,
                    String[] defaultColorsArray, String[] dilutionsArray,
                    String uuid, JSONArray resultsArray, JSONArray instructionsArray) {
        this.name = name;
        this.testType = testType;
        this.uuid = uuid;
        swatches = new ArrayList<>();
        dilutions = new ArrayList<>();
        reagents = new ArrayList<>();
        reactionTimes = new ArrayList<>();

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
                            patchObj.has("unit") ? patchObj.getString("unit") : "",
                            patchObj.has("timeDelay") ? patchObj.getInt("timeDelay") : 0,
                            patchObj.has("md610_id") ? patchObj.getString("md610_id") : ""));
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

    private TestInfo(Parcel in) {
        name = in.readString();
        uuid = in.readString();
        unit = in.readString();
        requiresCalibration = in.readByte() != 0;
        allInteger = in.readByte() != 0;
        mIsDirty = in.readByte() != 0;
        monthsValid = in.readInt();
        isGroup = in.readByte() != 0;
        groupName = in.readInt();
        batchNumber = in.readString();
        calibrationDate = in.readLong();
        expiryDate = in.readLong();
        useGrayScale = in.readByte() != 0;
        hueTrend = in.readInt();
        rangeValues = in.createDoubleArray();
        deviceId = in.readString();
        responseFormat = in.readString();
        deprecated = in.readByte() != 0;
        md610Id = in.readString();
        selectInstruction = in.readString();
        reagents = in.createStringArrayList();
        swatches = null;
        testType = null;
        dilutions = null;
    }

    /**
     * Sort the swatches for this test by their result values.
     */
    private void sort() {
        Collections.sort(swatches, (c1, c2) -> Double.compare(c1.getValue(), (c2.getValue())));
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

    public String getMd610Id() {
        return md610Id;
    }

    public void setMd610Id(String md610Id) {
        this.md610Id = md610Id;
    }

    public String getSelectInstruction() {
        return selectInstruction;
    }

    public void setSelectInstruction(String selectInstruction) {
        this.selectInstruction = selectInstruction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uuid);
        dest.writeString(unit);
        dest.writeByte((byte) (requiresCalibration ? 1 : 0));
        dest.writeByte((byte) (allInteger ? 1 : 0));
        dest.writeByte((byte) (mIsDirty ? 1 : 0));
        dest.writeInt(monthsValid);
        dest.writeByte((byte) (isGroup ? 1 : 0));
        dest.writeInt(groupName);
        dest.writeString(batchNumber);
        dest.writeLong(calibrationDate);
        dest.writeLong(expiryDate);
        dest.writeByte((byte) (useGrayScale ? 1 : 0));
        dest.writeInt(hueTrend);
        dest.writeDoubleArray(rangeValues);
        dest.writeString(deviceId);
        dest.writeString(responseFormat);
        dest.writeByte((byte) (deprecated ? 1 : 0));
        dest.writeString(md610Id);
        dest.writeString(selectInstruction);
        dest.writeStringList(reagents);
    }

    public String getReagent(int index) {
        if (reagents.size() > index) {
            return reagents.get(index);
        } else {
            return "";
        }
    }

    public void setReagent(JSONArray value) {
        for (int i = 0; i < value.length(); i++) {
            try {
                this.reagents.add(value.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Serializable getSampleQuantity() {
        return sampleQuantity;
    }

    public void setSampleQuantity(Serializable sampleQuantity) {
        this.sampleQuantity = sampleQuantity;
    }

    public void setReactionTime(JSONArray value) {
        for (int i = 0; i < value.length(); i++) {
            try {
                this.reactionTimes.add(value.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getReactionTime(int index) {
        if (reactionTimes.size() > index) {
            return reactionTimes.get(index);
        } else {
            return "";
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSubtitleExtra() {
        return subtitleExtra;
    }

    public void setSubtitleExtra(String subtitleExtra) {
        this.subtitleExtra = subtitleExtra;
    }

    public static class SubTest {
        private final int id;
        private final String desc;
        private final String unit;
        private final int timeDelay;
        private final String md610Id;

        SubTest(int id, String desc, String unit, int timeDelay, String md610Id) {
            this.id = id;
            this.desc = desc;
            this.unit = unit;
            this.timeDelay = timeDelay * 1000;
            this.md610Id = md610Id;
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

        public String getMd610Id() {
            return md610Id;
        }
    }
}
