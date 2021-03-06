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

public class Instruction implements Parcelable, Cloneable {

    public static final Creator<Instruction> CREATOR = new Creator<Instruction>() {
        @Override
        public Instruction createFromParcel(Parcel in) {
            return new Instruction(in);
        }

        @Override
        public Instruction[] newArray(int size) {
            return new Instruction[size];
        }
    };
    @SerializedName("section")
    @Expose
    public final List<String> section;
    @SerializedName("testStage")
    @Expose
    public final int testStage;
    @SerializedName("image")
    @Expose
    private final String image;
    @SerializedName("layout")
    @Expose
    private final String layout;
    private int index;

    private Instruction(Instruction instruction) {
        index = instruction.index;
        section = new ArrayList<>(instruction.section);
        image = instruction.image;
        layout = instruction.layout;
        testStage = instruction.testStage;
    }

    private Instruction(Parcel in) {
        index = in.readInt();
        section = in.createStringArrayList();
        image = in.readString();
        layout = in.readString();
        testStage = in.readInt();
    }

    public Instruction clone() throws CloneNotSupportedException {
        Instruction clone = (Instruction) super.clone();
        return new Instruction(clone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(index);
        parcel.writeStringList(section);
        parcel.writeString(image);
        parcel.writeString(layout);
        parcel.writeInt(testStage);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int value) {
        index = value;
    }
}

