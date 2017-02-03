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

package org.akvo.caddisfly.sensor.colorimetry.strip.util;

/**
 * Various constants used for configuration and keys.
 */
public final class Constant {
    public static final double MAX_LUM_LOWER = 200;
    public static final double MAX_LUM_UPPER = 254;
    public static final double MAX_SHADOW_PERCENTAGE = 10;
    public static final double PERCENT_ILLUMINATION = 1.05;
    public static final double CROP_FINDER_PATTERN_FACTOR = 0.75;
    public static final float MAX_TILT_DIFF = 0.03f;
    public static final int COUNT_QUALITY_CHECK_LIMIT = 15;
    public static final int PIXEL_MARGIN_STRIP_AREA_WIDTH = 6;
    public static final int PIXEL_MARGIN_STRIP_AREA_HEIGHT = 4;
    public static final String UUID = "org.akvo.caddisfly.uuid";
    public static final String FORMAT = "org.akvo.caddisfly.format";
    public static final String WIDTH = "org.akvo.caddisfly.width";
    public static final String HEIGHT = "org.akvo.caddisfly.height";
    public static final String TOP_LEFT = "org.akvo.caddisfly.top_left";
    public static final String TOP_RIGHT = "org.akvo.caddisfly.top_right";
    public static final String BOTTOM_LEFT = "org.akvo.caddisfly.bottom_left";
    public static final String BOTTOM_RIGHT = "org.akvo.caddisfly.bottom_right";
    public static final String INFO = "org.akvo.caddisfly.finder_pattern_info";
    public static final String DATA = "org.akvo.caddisfly.data";
    public static final String STRIP = "org.akvo.caddisfly.strip";
    public static final String IMAGE_PATCH = "org.akvo.caddisfly.image_patch";
    public static final String ERROR = "org.akvo.caddisfly.error";
    public static final String DIAGNOSTIC_INFO = "diagnostic_info_key";
    public static final String CALIBRATION_INFO = "calibration_info_key";
    public static final String DISTANCE_INFO = "distance_info_key";
    public static final int MIN_CAMERA_MEGA_PIXELS = 5;
    public static final int TIMEOUT_PREPARE = 20000;
    public static final int TIMEOUT_PREPARE_EXTEND = 10000;
    public static final String SEND_IMAGE_IN_RESULT = "send_image";
    public static final int GET_READY_SECONDS = 12;
    public static final String PHASE = "org.akvo.caddisfly.phase";
    static final int MAX_COLOR_DISTANCE = 15;
    static final double CONTRAST_DEVIATION_FRACTION = 0.05;
    static final double CONTRAST_MAX_DEVIATION_FRACTION = 0.20;

    private Constant() {
    }
}
