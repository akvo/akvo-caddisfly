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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

public class ColorimetryLiquidConfig {

    /**
     * Width and height of cropped image
     */
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;
    /**
     * The delay between each photo taken by the camera during the analysis
     */
    public static final int DELAY_BETWEEN_SAMPLING = 6000;
    /**
     * The number of photos to take during analysis
     */
    public static final int SAMPLING_COUNT_DEFAULT = 5;
    /**
     * Tolerance at which a calibrated color is valid when compared to expected color
     */
    public static final int MAX_VALID_CALIBRATION_TOLERANCE = 250;

}
