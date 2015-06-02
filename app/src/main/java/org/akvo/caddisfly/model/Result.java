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

import android.graphics.Bitmap;
import android.util.Pair;

import java.util.ArrayList;

public class Result {
    private final double mValue;
    private final int mColor;
    private final Bitmap mBitmap;
    private final ArrayList<Pair<String, Double>> mResults;

    public Result(double value, int color, Bitmap bitmap, ArrayList<Pair<String, Double>> results) {
        mValue = value;
        mColor = color;
        mBitmap = bitmap;
        mResults = results;
    }

    public double getValue() {
        return mValue;
    }

    public int getColor() {
        return mColor;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public ArrayList<Pair<String, Double>> getResults() {
        return mResults;
    }
}
