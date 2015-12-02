package org.akvo.akvoqr.util;

/**
 * Created by linda on 9/3/15.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Constant {
    public static final String BRAND = "org.akvo.akvoqr.brand";
    public static final String MAT = "org.akvo.akvoqr.mat";
    public static final String DATA = "org.akvo.akvoqr.data";
    public static final String FORMAT = "org.akvo.akvoqr.format";
    public static final String WIDTH = "org.akvo.akvoqr.width";
    public static final String HEIGHT = "org.akvo.akvoqr.height";
    public static final String TOPLEFT = "org.akvo.akvoqr.topleft";
    public static final String TOPRIGHT = "org.akvo.akvoqr.topright";
    public static final String BOTTOMLEFT = "org.akvo.akvoqr.bottomleft";
    public static final String BOTTOMRIGHT = "org.akvo.akvoqr.bottomright";
    public static final String INFO = "org.akvo.akvoqr.finderpatterninfo";
    public static final String IMAGE_PATCH = "org.akvo.akvoqr.imagepatch";
    public static final double MAX_LUM_LOWER = 150;
    public static final double MAX_LUM_UPPER = 240;
    public static final double MAX_LUM_PERCENTAGE = (MAX_LUM_UPPER / 255d) * 100;
    public static final double MAX_SHADOW_PERCENTAGE = 10;
    public static final double MIN_FOCUS_PERCENTAGE = 70;
    public static final double CONTRAST_DEVIATION_FRACTION = 0.05;
    public static final double CONTRAST_MAX_DEVIATION_FRACTION = 0.20;
    public static final int COUNT_QUALITY_CHECK_LIMIT = 5;
    public static final double CROP_CAMERAVIEW_FACTOR = 0.6;
    public static final double CROP_FINDERPATTERN_FACTOR = 0.75;
    public static final float MAX_LEVEL_DIFF = 2f;

    public static final String ERROR = "org.akvo.akvoqr.error";
}
