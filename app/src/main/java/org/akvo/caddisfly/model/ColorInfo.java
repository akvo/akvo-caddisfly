/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.model;

public class ColorInfo {

    private final int mColor;

    private final int mQuality;

    private double mDistance;

    private double mIncrementDistance;

    private int mErrorCode = 0;

    public ColorInfo(int color, int quality) {
        mColor = color;
        //int mCount = count;
        //int mDominantCount = dominantCount;
        mQuality = quality;

    }

    public int getColor() {
        return mColor;
    }

 /*   public int getCount() {
        return mCount;
    }

    public int getDominantCount() {
        return mDominantCount;
    }*/

    public int getQuality() {
        return mQuality;
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double value) {
        mDistance = value;
    }

    public double getIncrementDistance() {
        return mIncrementDistance;
    }

    public void setIncrementDistance(double value) {
        mIncrementDistance = value;
    }


    public int getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(int value) {
        mErrorCode = value;
    }

}