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

public class Reagent implements Parcelable {

    public static final Creator<Reagent> CREATOR = new Creator<Reagent>() {

        @SuppressWarnings({"unchecked"})
        public Reagent createFromParcel(Parcel in) {
            return new Reagent(in);
        }

        public Reagent[] newArray(int size) {
            return (new Reagent[size]);
        }

    };
    @SerializedName("name")
    @Expose
    public String name = "";
    @SerializedName("code")
    @Expose
    public String code = "";
    @SerializedName("reactionTime")
    @Expose
    public Integer reactionTime;

    private Reagent(Parcel in) {
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.code = ((String) in.readValue((String.class.getClassLoader())));
        this.reactionTime = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    public Reagent() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(code);
        dest.writeValue(reactionTime);
    }

    public int describeContents() {
        return 0;
    }

}