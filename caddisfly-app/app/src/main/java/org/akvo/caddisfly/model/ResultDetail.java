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

import android.graphics.Bitmap;

public class ResultDetail {
    private final int color;
    private double result;
    private int matchedColor;
    private double distance;
    private int dilution = 1;
    private int calibrationSteps;
    private Bitmap bitmap;

    public ResultDetail(double result, int color) {
        this.result = result;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setDilution(int dilution) {
        this.dilution = dilution;
    }

    public int getMatchedColor() {
        return matchedColor;
    }

    public void setMatchedColor(int matchedColor) {
        this.matchedColor = matchedColor;
    }

    public int getCalibrationSteps() {
        return calibrationSteps;
    }

    public void setCalibrationSteps(int calibrationSteps) {
        this.calibrationSteps = calibrationSteps;
    }

    public int getDilution() {
        return dilution;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
