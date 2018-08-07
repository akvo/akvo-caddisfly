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

public class MpnValue {
    private final String mpn;
    private final double confidence;
    private final String riskCategory;
    private int backgroundColor1;
    private int backgroundColor2;

    public MpnValue(String mpn, double confidence, String riskCategory) {

        this.mpn = mpn;
        this.confidence = confidence;
        this.riskCategory = riskCategory;
    }

    public String getMpn() {
        return mpn;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public int getBackgroundColor1() {
        return backgroundColor1;
    }

    public void setBackgroundColor1(int backgroundColor1) {
        this.backgroundColor1 = backgroundColor1;
    }

    public int getBackgroundColor2() {
        return backgroundColor2;
    }

    public void setBackgroundColor2(int backgroundColor2) {
        this.backgroundColor2 = backgroundColor2;
    }
}
