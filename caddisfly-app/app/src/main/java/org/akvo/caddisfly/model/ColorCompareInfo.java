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

public class ColorCompareInfo {

    private final int resultColor;
    private final int matchedColor;
    private final double distance;
    private final double result;

    public ColorCompareInfo(double result, int resultColor, int matchedColor, double distance) {
        this.result = result;
        this.resultColor = resultColor;
        this.matchedColor = matchedColor;
        this.distance = distance;
    }

    public double getResult() {
        return result;
    }

    @SuppressWarnings("unused")
    public int getResultColor() {
        return resultColor;
    }

    public int getMatchedColor() {
        return matchedColor;
    }

    public double getDistance() {
        return distance;
    }

}
