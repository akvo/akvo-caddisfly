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

public class ColorCompareInfo {

    private final int mResultColor;
    private final int mMatchedColor;
    private final double mDistance;
    private final double mResult;

    public ColorCompareInfo(double result, int resultColor, int matchedColor, double distance) {
        mResult = result;
        mResultColor = resultColor;
        mMatchedColor = matchedColor;
        mDistance = distance;
    }

    public double getResult() {
        return mResult;
    }

    @SuppressWarnings("unused")
    public int getResultColor() {
        return mResultColor;
    }

    public int getMatchedColor() {
        return mMatchedColor;
    }

    public double getDistance() {
        return mDistance;
    }

}