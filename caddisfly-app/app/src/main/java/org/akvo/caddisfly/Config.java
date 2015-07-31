/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly;

/*
Global Configuration
 */
public class Config {

    // For external app connection
    public static final String FLOW_ACTION_EXTERNAL_SOURCE = "org.akvo.flow.action.externalsource";

    // Used to check if flow app is installed
    public static final String FLOW_SURVEY_PACKAGE_NAME = "org.akvo.flow";

    // Caddisfly update file name
    public static final String UPDATE_FILE_NAME = "akvo_caddisfly_update.apk";

    // Caddisfly update check path
    public static final String UPDATE_CHECK_URL
            = "http://caddisfly.ternup.com/akvoapp/v.txt";

    // Caddisfly update path
    public static final String UPDATE_URL
            = "http://caddisfly.ternup.com/akvoapp/akvo_caddisfly_update.apk";

    public static final String OLD_CALIBRATE_FOLDER_NAME = "calibrate";
    public static final String OLD_FILES_FOLDER_NAME = "/com.ternup.caddisfly";
    public static final String CALIBRATE_FOLDER_NAME = "calibration";

    public static final String APP_EXTERNAL_PATH = "/org.akvo.caddisfly";

    // Tag for debug log filtering
    public static final String DEBUG_TAG = "Caddisfly";

    public static final String RESULT_VALUE_KEY = "resultValue";
    public static final String RESULT_COLOR_KEY = "resultColor";

    public static final String CONFIG_FOLDER = "/org.akvo.caddisfly/config/";
    public static final String CONFIG_FILE = "tests.json";

    // width and height of cropped image
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;
    public static final int SAMPLING_COUNT_DEFAULT = 5;
    public static final float SOUND_VOLUME = 1f;
    public static final int INITIAL_DELAY = 6000;
    public static final int MAX_COLOR_DISTANCE = 30;


}
