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

package org.akvo.caddisfly.common;

public final class ChamberTestConfig {

    /**
     * The delay before starting the test
     */
    public static final int DELAY_INITIAL = 4;

    /**
     * The delay seconds between each photo taken by the camera during the analysis.
     */
    public static final int DELAY_BETWEEN_SAMPLING = 4;

    /**
     * The number of photos to take during analysis.
     */
    public static final int SAMPLING_COUNT_DEFAULT = 5;

    /**
     * The number of photo samples to be skipped during analysis.
     */
    public static final int SKIP_SAMPLING_COUNT = 2;

    /**
     * Width and height of cropped image.
     */
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;

    /**
     * Max distance between colors at which the colors are considered to be similar.
     */
    public static final int MAX_COLOR_DISTANCE_RGB = 50;

    /**
     * Max distance between colors for calibration.
     */
    public static final int MAX_COLOR_DISTANCE_CALIBRATION = 20;

    /**
     * The number of interpolations to generate between range values.
     */
    public static final double INTERPOLATION_COUNT = 250;

    private ChamberTestConfig() {
    }

}
