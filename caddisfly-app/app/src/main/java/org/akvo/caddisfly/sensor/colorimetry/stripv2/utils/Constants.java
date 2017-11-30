package org.akvo.caddisfly.sensor.colorimetry.stripv2.utils;

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
    public static final int CALIB_PERCENTAGE_LIMIT = 95;
    public static final int MEASURE_TIME_COMPENSATION_MILLIS = 3000;
    public static final float STRIP_WIDTH_FRACTION = 0.5f;
    public static final String UUID = "org.akvo.caddisfly.uuid";
    //    public static final String FORMAT = "org.akvo.caddisfly.format";
//    public static final String WIDTH = "org.akvo.caddisfly.width";
//    public static final String HEIGHT = "org.akvo.caddisfly.height";
    public static final String TOP_LEFT = "org.akvo.caddisfly.top_left";
    public static final String TOP_RIGHT = "org.akvo.caddisfly.top_right";
    public static final String BOTTOM_LEFT = "org.akvo.caddisfly.bottom_left";
    public static final String BOTTOM_RIGHT = "org.akvo.caddisfly.bottom_right";
    //    public static final String INFO = "org.akvo.caddisfly.finder_pattern_info";
//    public static final String DATA = "org.akvo.caddisfly.data";
//    public static final String STRIP = "org.akvo.caddisfly.strip";
//    public static final String IMAGE_PATCH = "org.akvo.caddisfly.image_patch";
//    public static final String ERROR = "org.akvo.caddisfly.error";
//    public static final String DIAGNOSTIC_INFO = "diagnostic_info_key";
//    public static final String CALIBRATION_INFO = "calibration_info_key";
//    public static final String DISTANCE_INFO = "distance_info_key";
//    public static final int MIN_CAMERA_MEGA_PIXELS = 5;
//    public static final int TIMEOUT_PREPARE = 20000;
//    public static final int TIMEOUT_PREPARE_EXTEND = 10000;
//    public static final String ERROR = "org.akvo.caddisfly.error";
//    public static final String DIAGNOSTIC_INFO = "diagnostic_info_key";
//    public static final String CALIBRATION_INFO = "calibration_info_key";
//    public static final int MIN_CAMERA_MEGA_PIXELS = 5;
    public static final String SEND_IMAGE_IN_RESULT = "send_image";
    public static final int GET_READY_SECONDS = 12;
    public static final int MIN_SHOW_TIMER_SECONDS = 5;
    public static final String PHASE = "org.akvo.caddisfly.phase";

    //    public static final String PHASE = "org.akvo.caddisfly.phase";
    public static final int MAX_COLOR_DISTANCE = 15;

    private Constants() {
    }
}
