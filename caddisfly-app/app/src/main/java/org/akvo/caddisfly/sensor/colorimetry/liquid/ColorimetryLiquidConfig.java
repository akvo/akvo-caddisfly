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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

public final class ColorimetryLiquidConfig {

    /**
     * Width and height of cropped image
     */
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;
    /**
     * The delay between each photo taken by the camera during the analysis
     */
    public static final int DELAY_BETWEEN_SAMPLING = 5000;
    /**
     * The number of photos to take during analysis
     */
    public static final int SAMPLING_COUNT_DEFAULT = 5;
    /**
     * Tolerance at which a calibrated color is valid when compared to expected color
     */
    public static final int MAX_VALID_CALIBRATION_TOLERANCE = 250;

    /**
     * Max distance between colors at which the colors are considered to be similar
     */
    public static final int MAX_COLOR_DISTANCE_RGB = 40;

    /**
     * The number of photo samples to be skipped during analysis
     */
    public static final int SKIP_SAMPLING_COUNT = 2;

    private ColorimetryLiquidConfig() {
    }

}
