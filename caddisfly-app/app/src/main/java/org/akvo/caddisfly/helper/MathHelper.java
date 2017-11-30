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

package org.akvo.caddisfly.helper;

import org.akvo.caddisfly.sensor.striptest.utils.Constants;

import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_180;
import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_90;

/**
 * Math helper functions
 */
public final class MathHelper {

    private MathHelper() {
    }

    public static float getTiltAngle(float[] tiltValues) {
        float degrees = 0f;

        // if the horizontal tilt is too large, indicate it
        if (Math.abs(tiltValues[0] - 1) > Constants.MAX_TILT_DIFF) {
            degrees = tiltValues[0] - 1 < 0 ? -DEGREES_90 : DEGREES_90;
        }

        // if the vertical tilt is too large, indicate it
        if (Math.abs(tiltValues[1] - 1) > Constants.MAX_TILT_DIFF) {
            degrees = tiltValues[1] - 1 < 0 ? DEGREES_180 : 1;
        }
        return degrees;
    }
}
