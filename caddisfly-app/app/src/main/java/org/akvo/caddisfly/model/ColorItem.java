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

import java.util.ArrayList;
import java.util.List;

public class ColorItem implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<ColorItem> CREATOR = new Creator<ColorItem>() {
        @Override
        public ColorItem createFromParcel(Parcel in) {
            return new ColorItem(in);
        }

        @Override
        public ColorItem[] newArray(int size) {
            return new ColorItem[size];
        }
    };
    @SerializedName("value")
    @Expose
    private Double value;
    @SerializedName("lab")
    @Expose
    private List<Double> lab = null;
    private int rgb;

    private ColorItem(Parcel in) {
        value = in.readByte() == 0x00 ? null : in.readDouble();
        if (in.readByte() == 0x01) {
            lab = new ArrayList<>();
            in.readList(lab, Double.class.getClassLoader());
        } else {
            lab = null;
        }
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public List<Double> getLab() {
        return lab;
    }

    public void setLab(List<Double> lab) {
        this.lab = lab;
    }

    public int getRgb() {
        return rgb;
    }

    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (value == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(value);
        }
        if (lab == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(lab);
        }
    }
}