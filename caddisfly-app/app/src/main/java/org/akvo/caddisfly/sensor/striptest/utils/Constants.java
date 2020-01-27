package org.akvo.caddisfly.sensor.striptest.utils;

/**
 * Various constants used for configuration and keys.
 */
public final class Constants {
    public static final double MAX_LUM_LOWER = 210;
    public static final double MAX_LUM_UPPER = 240;
    public static final double SHADOW_PERCENTAGE_LIMIT = 90;
    //    public static final double PERCENT_ILLUMINATION = 1.05;
    public static final double CONTRAST_DEVIATION_FRACTION = 0.1;
    public static final double CONTRAST_MAX_DEVIATION_FRACTION = 0.20;
    public static final double CROP_FINDER_PATTERN_FACTOR = 0.75;
    //    public static final double FINDER_PATTERN_ASPECT = 0.5662;
    public static final float MAX_TILT_DIFF = 0.05f;
    public static final float MAX_CLOSER_DIFF = 0.15f;
    public static final int COUNT_QUALITY_CHECK_LIMIT = 15;
    public static final int PIXEL_PER_MM = 5;
    public static final int SKIP_MM_EDGE = 1;
    public static final int CALIBRATION_PERCENTAGE_LIMIT = 95;
    public static final int MEASURE_TIME_COMPENSATION_MILLIS = 3000;
    public static final float STRIP_WIDTH_FRACTION = 0.5f;
    public static final int GET_READY_SECONDS = 12;
    public static final int MIN_SHOW_TIMER_SECONDS = 5;

    public static final int MAX_COLOR_DISTANCE = 23;

    private Constants() {
    }
}
