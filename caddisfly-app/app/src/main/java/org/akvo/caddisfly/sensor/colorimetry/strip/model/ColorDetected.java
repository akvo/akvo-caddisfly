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

package org.akvo.caddisfly.sensor.colorimetry.strip.model;

import org.opencv.core.Scalar;

/**
 * Created by linda on 8/26/15
 */
public class ColorDetected {

    private int color;
    private Scalar lab;

    public ColorDetected() {
    }

    @SuppressWarnings("unused")
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Scalar getLab() {
        return lab;
    }

    public void setLab(Scalar lab) {
        this.lab = lab;
    }
}
