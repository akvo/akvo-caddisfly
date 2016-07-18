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

package org.akvo.caddisfly.sensor.colorimetry.strip.model;

import org.opencv.core.Scalar;

/**
 * Created by linda on 8/26/15
 */
public class ColorDetected {

    private int color;
    private Scalar rgb;
    private Scalar lab;

    public ColorDetected() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Scalar getRgb() {
        return rgb;
    }

    public void setRgb(Scalar rgb) {
        this.rgb = rgb;
    }

    public Scalar getLab() {
        return lab;
    }

    public void setLab(Scalar lab) {
        this.lab = lab;
    }
}
