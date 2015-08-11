/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.model;

import org.akvo.caddisfly.AppConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

public class TestInfo {
    private final Hashtable mNames;
    private final String mCode;
    private final String mUnit;
    private final ArrayList<Swatch> mRanges;
    private final AppConfig.TestType mType;
    private final ArrayList<Integer> mDilutions;
    private boolean requiresCalibation;

    public TestInfo(Hashtable names, String code, String unit, AppConfig.TestType type) {
        mNames = names;
        mType = type;
        mCode = code;
        mUnit = unit;
        mRanges = new ArrayList<>();
        mDilutions = new ArrayList<>();
    }

    public TestInfo() {
        mNames = null;
        mType = AppConfig.TestType.COLORIMETRIC_LIQUID;
        mCode = "";
        mUnit = "";
        mRanges = new ArrayList<>();
        mDilutions = new ArrayList<>();
    }

    public void sortRange() {
        Collections.sort(mRanges, new Comparator<Swatch>() {
            public int compare(Swatch c1, Swatch c2) {
                return Double.compare(c1.getValue(), (c2.getValue()));
            }
        });
    }

    public String getName(String languageCode) {
        if (mNames.containsKey(languageCode)) {
            return mNames.get(languageCode).toString();
        } else if (mNames.containsKey("en")) {
            return mNames.get("en").toString();
        }
        return "";
    }

    public AppConfig.TestType getType() {
        return mType;
    }

    public String getCode() {
        return mCode;
    }

    public String getUnit() {
        return mUnit;
    }

    public ArrayList<Swatch> getRanges() {
        return mRanges;
    }

    public double getDilutionRequiredLevel() {
        Swatch swatch = mRanges.get(mRanges.size() - 1);
        return swatch.getValue() - 0.2;
    }

    public void addRange(Swatch value) {
        mRanges.add(value);
    }

    public Swatch getRange(int position) {
        return mRanges.get(position);
    }

    public void addDilution(int dilution) {
        mDilutions.add(dilution);
    }

    public boolean hasDilution() {
        return mDilutions.size() > 1;
    }

    public boolean isRequiresCalibation() {
        return requiresCalibation;
    }

    public void setRequiresCalibation(boolean requiresCalibation) {
        this.requiresCalibation = requiresCalibation;
    }
}
