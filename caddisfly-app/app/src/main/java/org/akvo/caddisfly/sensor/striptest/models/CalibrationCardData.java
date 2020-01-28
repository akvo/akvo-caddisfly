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

package org.akvo.caddisfly.sensor.striptest.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalibrationCardData {
    @NonNull
    private final Map<String, Location> locations;
    @NonNull
    private final Map<String, CalValue> calValues;
    @NonNull
    private final List<WhiteLine> whiteLines;
    @NonNull
    private final float[] stripArea;
    public float hSize;
    public float vSize;
    public int version;
    private float patchSize;

    public CalibrationCardData() {
        this.locations = new HashMap<>();
        this.calValues = new HashMap<>();
        this.whiteLines = new ArrayList<>();
        this.stripArea = new float[4];
        this.version = 0;
    }

    public void addLocation(String label, Float x, Float y, Boolean grayPatch) {
        Location loc = new Location(x, y, grayPatch);
        this.locations.put(label, loc);
    }

    public void addCal(String label, float l, float a, float b) {
        CalValue calVal = new CalValue(l, a, b);
        this.calValues.put(label, calVal);
    }

    public void addWhiteLine(Float x1, Float y1, Float x2, Float y2, Float width) {
        WhiteLine line = new WhiteLine(x1, y1, x2, y2, width);
        this.whiteLines.add(line);
    }

    public float getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(float patchSize) {
        this.patchSize = patchSize;
    }

    public float[] getStripArea() {
        return stripArea.clone();
    }

    public void setStripArea(float x1, float y1, float x2, float y2) {
        stripArea[0] = x1;
        stripArea[1] = y1;
        stripArea[2] = x2;
        stripArea[3] = y2;
    }

    @NonNull
    public List<WhiteLine> getWhiteLines() {
        return whiteLines;
    }

    @NonNull
    public Map<String, Location> getLocations() {
        return locations;
    }

    @NonNull
    public Map<String, CalValue> getCalValues() {
        return calValues;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public static class Location {
        public final Float x;
        public final Float y;
        private final Boolean grayPatch;

        Location(Float x, Float y, Boolean grayPatch) {
            this.x = x;
            this.y = y;
            this.grayPatch = grayPatch;
        }
    }

    public static class CalValue {
        private final float X;
        private final float Y;
        private final float Z;

        CalValue(float X, float Y, float Z) {
            this.X = X;
            this.Y = Y;
            this.Z = Z;
        }

        public float getX() {
            return X;
        }

        public float getY() {
            return Y;
        }

        public float getZ() {
            return Z;
        }
    }

    public static class WhiteLine {
        @NonNull
        private final Float[] p;
        private final Float width;

        WhiteLine(Float x1, Float y1, Float x2, Float y2, Float width) {
            Float[] pArray = new Float[4];
            pArray[0] = x1;
            pArray[1] = y1;
            pArray[2] = x2;
            pArray[3] = y2;
            this.p = pArray;
            this.width = width;
        }

        public Float[] getPosition() {
            return p.clone();
        }

        public Float getWidth() {
            return width;
        }
    }
}
