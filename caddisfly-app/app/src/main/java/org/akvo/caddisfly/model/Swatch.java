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

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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
