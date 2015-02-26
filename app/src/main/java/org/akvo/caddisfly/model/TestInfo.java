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

import java.util.ArrayList;
import java.util.Hashtable;

public class TestInfo {
    private final Hashtable mNames;
    private final String mCode;
    private final String mUnit;
    private final ArrayList<ResultRange> mRanges;
    private final ArrayList<ResultRange> mSwatches;
    private final int mType;
    private int mIncrement;

    public TestInfo(Hashtable names, String code, String unit, int type) {
        mNames = names;
        mType = type;
        mCode = code;
        mUnit = unit;
        mRanges = new ArrayList<>();
        mSwatches = new ArrayList<>();
    }

    public String getName(String languageCode) {
        if (mNames.containsKey(languageCode)) {
            return mNames.get(languageCode).toString();
        } else if (mNames.containsKey("en")) {
            return mNames.get("en").toString();
        }
        return "";
    }

    public int getType() {
        return mType;
    }

    public String getCode() {
        return mCode;
    }

    public String getUnit() {
        return mUnit;
    }

    public ArrayList<ResultRange> getRanges() {
        return mRanges;
    }

    public void addRange(ResultRange value) {
        mRanges.add(value);
    }

    public ResultRange getRange(int position) {
        return mRanges.get(position);
    }

    public void setColor(ResultRange range, int resultColor) {
        range.setColor(resultColor);
    }

    public ArrayList<ResultRange> getSwatches() {
        return mSwatches;
    }
}
