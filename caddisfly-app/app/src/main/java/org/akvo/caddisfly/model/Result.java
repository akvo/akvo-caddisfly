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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.akvo.caddisfly.preference.AppPreferences;

import java.util.ArrayList;
import java.util.List;

public class Result implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
    @SerializedName("id")
    @Expose
    private final Integer id;
    @SerializedName("md610_id")
    @Expose
    private final String md610Id;
    @SerializedName("name")
    @Expose
    private final String name;
    @SerializedName("unit")
    @Expose
    private final String unit;
    @SerializedName("formula")
    @Expose
    private final String formula;
    @SerializedName("unitChoice")
    @Expose
    private final String unitChoice;
    @SerializedName("patchPos")
    @Expose
    private final Double patchPos;
    @SerializedName("patchWidth")
    @Expose
    private final Double patchWidth;
    @SerializedName("timeDelay")
    @Expose
    private final Integer timeDelay;
    @SerializedName("testStage")
    @Expose
    private final Integer testStage;
    @SerializedName("colors")
    @Expose
    private final List<ColorItem> colorItems;
    @SerializedName("grayScale")
    @Expose
    private final Boolean grayScale;
    private Float resultValue;

    private Result(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        md610Id = in.readString();
        name = in.readString();
        unit = in.readString();
        formula = in.readString();
        unitChoice = in.readString();
        patchPos = in.readByte() == 0x00 ? null : in.readDouble();
        patchWidth = in.readByte() == 0x00 ? null : in.readDouble();
        timeDelay = in.readByte() == 0x00 ? null : in.readInt();
        testStage = in.readByte() == 0x00 ? null : in.readInt();
        if (in.readByte() == 0x01) {
            colorItems = new ArrayList<>();
            in.readList(colorItems, ColorItem.class.getClassLoader());
        } else {
            colorItems = null;
        }
        byte tmpGrayScale = in.readByte();
        grayScale = tmpGrayScale == 1;
        resultValue = in.readByte() == 0x00 ? null : in.readFloat();
    }

    public Integer getId() {
        return id;
    }

    public String getMd610Id() {
        return md610Id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit == null ? "" : unit;
    }

    public String getFormula() {
        return formula == null ? "" : formula;
    }

    public String getUnitChoice() {
        return unitChoice;
    }

    public Double getPatchPos() {
        return patchPos;
    }

    public Double getPatchWidth() {
        return patchWidth;
    }

    /**
     * Time to wait before analyzing.
     *
     * @return the time delay milli seconds
     */
    public Integer getTimeDelay() {
        if (AppPreferences.ignoreTimeDelays()) {
            // use the id as seconds when ignoring actual timeDelay
            return id;
        } else {
            return timeDelay == null ? 0 : timeDelay;
        }
    }

    public Integer getTestStage() {
        return testStage == null ? 1 : testStage;
    }

    public List<ColorItem> getColors() {
        return colorItems == null ? new ArrayList<>() : colorItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(id);
        }
        dest.writeString(md610Id);
        dest.writeString(name);
        dest.writeString(unit);
        dest.writeString(formula);
        dest.writeString(unitChoice);
        if (patchPos == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(patchPos);
        }
        if (patchWidth == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(patchWidth);
        }
        if (timeDelay == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(timeDelay);
        }
        if (testStage == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(testStage);
        }
        if (colorItems == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(colorItems);
        }
        dest.writeByte((byte) (grayScale == null ? 0 : grayScale ? 1 : 2));
        if (resultValue == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(resultValue);
        }

    }

    public float getResultValue() {
        return resultValue;
    }

    public void setResultValue(float resultValue) {
        this.resultValue = resultValue;
    }
}