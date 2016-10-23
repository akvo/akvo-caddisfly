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

public class Swatch implements Cloneable {
    private final double value;
    private final int defaultColor;
    private int color;
    private int redDifference;
    private int greenDifference;
    private int blueDifference;

    public Swatch(double value, int color, int defaultColor) {
        this.value = value;
        this.color = color;
        this.defaultColor = defaultColor;
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

    public int getDefaultColor() {
        return defaultColor;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int getRedDifference() {
        return redDifference;
    }

    void setRedDifference(int redDifference) {
        this.redDifference = redDifference;
    }

    public int getGreenDifference() {
        return greenDifference;
    }

    void setGreenDifference(int greenDifference) {
        this.greenDifference = greenDifference;
    }

    public int getBlueDifference() {
        return blueDifference;
    }

    void setBlueDifference(int blueDifference) {
        this.blueDifference = blueDifference;
    }
}
