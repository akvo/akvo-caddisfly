package org.akvo.caddisfly.common;

public class Constants {
    public static final String BRAND_IMAGE_PATH = "images/brand/";
    public static final String ILLUSTRATION_PATH = "images/instructions/";
    public static final String TESTS_META_FILENAME = "tests.json";

    /**
     * Max distance between colors at which the colors are considered to be similar
     */
    public static final int MAX_COLOR_DISTANCE_RGB = 40;

    /**
     * The number of photos to take during analysis
     */
    public static final int SAMPLING_COUNT_DEFAULT = 5;

    /**
     * The number of photo samples to be skipped during analysis
     */
    public static final int SKIP_SAMPLING_COUNT = 2;
    public static final int GET_READY_SECONDS = 12;


    public static final float MAX_TILT_DIFF = 0.03f;

    public static final int MIN_CAMERA_MEGA_PIXELS = 5;
}
