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

import org.akvo.caddisfly.util.ColorUtil;

public class ResultDetail {
    private final int color;
    private double result;
    private int matchedColor;
    private double distance;
    private int calibrationSteps;
    private ColorUtil.ColorModel colorModel;

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

    public int getMatchedColor() {
        return matchedColor;
    }

    public void setMatchedColor(int matchedColor) {
        this.matchedColor = matchedColor;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getCalibrationSteps() {
        return calibrationSteps;
    }

    public void setCalibrationSteps(int calibrationSteps) {
        this.calibrationSteps = calibrationSteps;
    }

    public ColorUtil.ColorModel getColorModel() {
        return colorModel;
    }

    public void setColorModel(ColorUtil.ColorModel colorModel) {
        this.colorModel = colorModel;
    }
}
