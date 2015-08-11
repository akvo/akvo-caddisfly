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

import org.akvo.caddisfly.util.FileUtils;

import java.io.File;

/*
Global Configuration
 */
public class AppConfig {

    // For external app connection
    public static final String FLOW_ACTION_EXTERNAL_SOURCE = "org.akvo.flow.action.externalsource";

    // Used to check if flow app is installed
    public static final String FLOW_SURVEY_PACKAGE_NAME = "org.akvo.flow";

    // Caddisfly update file name
    public static final String UPDATE_FILE_NAME = "akvo_caddisfly_update.apk";

    // Caddisfly update check path
    public static final String UPDATE_CHECK_URL
            = "http://caddisfly.ternup.com/akvoapp/v1.txt";
    // Caddisfly update path
    public static final String UPDATE_URL
            = "http://caddisfly.ternup.com/akvoapp/akvo_caddisfly_update.apk";
    //todo: remove this temporary file size
    public static final int UPDATE_FILE_TYPICAL_SIZE = 1500000;
    public static final String OLD_CALIBRATE_FOLDER_NAME = "calibrate";
    public static final String OLD_FILES_FOLDER_NAME = "/com.ternup.caddisfly";
    public static final String APP_EXTERNAL_PATH = "/org.akvo.caddisfly";
    public static final String CONFIG_FILE = "tests.json";

    // Tag for debug log filtering
    public static final String DEBUG_TAG = "Caddisfly";

    // Width and height of cropped image
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;
    public static final int DELAY_BETWEEN_SAMPLING = 6000;
    //The maximum color distance before the color is considered out of range
    public static final int MAX_COLOR_DISTANCE = 4;
    //The minimum color distance allowed before the colors are considered equivalent
    public static final double MIN_VALID_COLOR_DISTANCE = 1.2;
    public static final int SAMPLING_COUNT_DEFAULT = 5;
    public static final float SOUND_VOLUME = 1f;

    // Folders
    private static final String DIR_APK = "apk"; // App upgrades
    private static final String DIR_CALIBRATION = "Akvo Caddisfly/calibration"; // Calibration files
    private static final String DIR_CONFIG = "Akvo Caddisfly/config"; // Calibration files

    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type FileType to determine the type of resource attempting to use.
     * @return File representing the root directory for the given FileType.
     */
    public static File getFilesDir(FileType type) {
        String path = null;
        switch (type) {
            case APK:
                path = FileUtils.getFilesStorageDir(true) + File.separator + DIR_APK;
                break;
            case CALIBRATION:
                path = FileUtils.getFilesStorageDir(false) + File.separator + DIR_CALIBRATION;
                break;
            case CONFIG:
                path = FileUtils.getFilesStorageDir(false) + File.separator + DIR_CONFIG;
                break;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public enum TestType {COLORIMETRIC_LIQUID, COLORIMETRIC_STRIP, SENSOR}

    public enum ColorModel {RGB, LAB, HSV}

    public enum FileType {APK, CALIBRATION, CONFIG}
}
