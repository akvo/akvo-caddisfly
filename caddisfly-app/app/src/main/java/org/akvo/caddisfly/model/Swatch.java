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

public class Swatch implements Cloneable, Parcelable {
    @SuppressWarnings("unused")
    public static final Creator<Swatch> CREATOR = new Creator<Swatch>() {
        @Override
        public Swatch createFromParcel(Parcel in) {
            return new Swatch(in);
        }

        @Override
        public Swatch[] newArray(int size) {
            return new Swatch[size];
        }
    };
    private final double value;
    private final int defaultColor;
    private int color;

    public Swatch(double value, int color, int defaultColor) {
        this.value = value;
        this.color = color;
        this.defaultColor = defaultColor;
    }

    protected Swatch(Parcel in) {
        value = in.readDouble();
        defaultColor = in.readInt();
        color = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(value);
        dest.writeInt(defaultColor);
        dest.writeInt(color);
    }

    public double getValue() {
        return value;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

