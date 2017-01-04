package org.akvo.caddisfly.sensor;

/**
 * Class to hold all public constants used by sensors
 */

public final class SensorConstants {

    /**
     * Serialization constants
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TYPE_NAME = "caddisfly";
    public static final String RESOURCE_ID = "caddisflyResourceUuid";
    public static final String RESPONSE = "response";
    public static final String IS_EXTERNAL_ACTION = "external_action";
    @Deprecated
    public static final String RESPONSE_COMPAT = "response_compat";
    public static final String RESULT = "result";
    public static final String IMAGE = "image";
    public static final String DEVICE = "device";
    public static final String APP = "app";
    public static final String VALUE = "value";
    public static final String LAB = "lab";
    public static final String NAME = "name";
    public static final String UNIT = "unit";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String UUID = "uuid";
    public static final String COLOR = "color";
    public static final String USER = "user";
    public static final String SHORT_CODE = "shortCode";
    public static final String FREE_CHLORINE_ID = "c3535e72-ff77-4225-9f4a-41d3288780c6";
    public static final String FLUORIDE_ID = "f0f3c1dd-89af-49f1-83e7-bcc31c3006cf";

    public static final int DEGREES_90 = 90;
    public static final int DEGREES_270 = 270;
    public static final int DEGREES_180 = 180;
    public static final String LANGUAGE = "language";
    public static final String QUESTION_TITLE = "questionTitle";

    private SensorConstants() {
    }
}
