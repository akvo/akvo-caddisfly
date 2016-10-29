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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by markwestra on 01/08/2015
 */
public class CalibrationData {
    public final Map<String, Location> locations;
    public final Map<String, CalValue> calValues;
    public final List<WhiteLine> whiteLines;
    public final double[] stripArea;
    public int hSizePixel;
    public int vSizePixel;
    public double patchSize;
    public double hSize;
    public double vSize;

    public CalibrationData() {
        this.locations = new HashMap<>();
        this.calValues = new HashMap<>();
        this.whiteLines = new ArrayList<>();
        this.stripArea = new double[4];
    }

    public void addLocation(String label, Double x, Double y, Boolean grayPatch) {
        Location loc = new Location(x, y, grayPatch);
        this.locations.put(label, loc);
    }

    public void addCal(String label, double l, double a, double b) {
        CalValue calVal = new CalValue(l, a, b);
        this.calValues.put(label, calVal);
    }

    public void addWhiteLine(Double x1, Double y1, Double x2, Double y2, Double width) {
        WhiteLine line = new WhiteLine(x1, y1, x2, y2, width);
        this.whiteLines.add(line);
    }

    public static class Location {
        public final Double x;
        public final Double y;
        @SuppressWarnings("unused")
        private final Boolean grayPatch;

        Location(Double x, Double y, Boolean grayPatch) {
            this.x = x;
            this.y = y;
            this.grayPatch = grayPatch;
        }
    }

    public static class CalValue {
        private final double l;
        private final double a;
        private final double b;

        CalValue(double l, double a, double b) {
            this.l = l;
            this.a = a;
            this.b = b;
        }

        public double getL() {
            return l;
        }

        public double getA() {
            return a;
        }

        public double getB() {
            return b;
        }
    }

    public static class WhiteLine {
        private final Double[] p;
        private final Double width;

        WhiteLine(Double x1, Double y1, Double x2, Double y2, Double width) {
            Double[] pArray = new Double[4];
            pArray[0] = x1;
            pArray[1] = y1;
            pArray[2] = x2;
            pArray[3] = y2;
            this.p = pArray;
            this.width = width;
        }

        public Double[] getPosition() {
            return p.clone();
        }

        public Double getWidth() {
            return width;
        }
    }
}
