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

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.FileUtils;

import java.io.File;

/**
 * Global Configuration settings for the app
 */
public class AppConfig {

    /**
     * The intent action string used to connect to external app
     */
    public static final String FLOW_ACTION_EXTERNAL_SOURCE = "org.akvo.flow.action.externalsource";

    /**
     * FLOW package name to check if FLOW app is installed
     */
    public static final String FLOW_SURVEY_PACKAGE_NAME = "org.akvo.flow";

    /**
     * The file name for the downloaded update apk file
     */
    public static final String UPDATE_FILE_NAME = "akvo_caddisfly_update.apk";

    /**
     * The url to check for update version
     */
    public static final String UPDATE_CHECK_URL = "http://caddisfly.ternup.com/akvoapp/v.txt";

    /**
     * The url to download the update apk file from
     */
    public static final String UPDATE_URL
            = "http://caddisfly.ternup.com/akvoapp/akvo_caddisfly_update.apk";

    /**
     * The expected size of the next update file to enable display of the progress bar.
     * Used only if the update process cannot determine the size of the file to download
     */
    public static final int UPDATE_FILE_TYPICAL_SIZE = 1500000;

    //todo: remove when upgrade process no more required
    @Deprecated
    public static final String OLD_CALIBRATE_FOLDER_NAME = "calibrate";
    @Deprecated
    public static final String OLD_FILES_FOLDER_NAME = "/com.ternup.caddisfly";
    @Deprecated
    public static final String OLD_APP_EXTERNAL_PATH = "/org.akvo.caddisfly";

    /**
     * The user created configuration file name
     */
    public static final String CONFIG_FILE = "tests.json";

    /**
     * Tag for debug log filtering
     */
    public static final String DEBUG_TAG = "Caddisfly";

    /**
     * Width and height of cropped image
     */
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;

    /**
     * The delay between each photo taken by the camera during the analysis
     */
    public static final int DELAY_BETWEEN_SAMPLING = 6000;

    /**
     * The maximum color distance before the color is considered out of range
     */
    public static final int MAX_COLOR_DISTANCE = 4;

    /**
     * The minimum color distance allowed before the colors are considered equivalent
     */
    public static final double MIN_VALID_COLOR_DISTANCE = 1.2;

    /**
     * The number of photos to take during analysis
     */
    public static final int SAMPLING_COUNT_DEFAULT = 5;

    /**
     * The sound volume for the beeps and success/fail sounds
     */
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

    /**
     * Loads the tests from the json config file.
     * <p/>
     * Looks for the user created json file. If not found loads the internal json config file
     *
     * @return json configuration text
     */
    public static String getConfigJson() {

        File file = new File(AppConfig.getFilesDir(AppConfig.FileType.CONFIG), AppConfig.CONFIG_FILE);
        String text;

        //Look for external json config file otherwise use the internal default one
        if (file.exists()) {
            text = FileUtils.loadTextFromFile(file);
        } else {
            text = FileUtils.readRawTextFile(CaddisflyApp.getApp(), R.raw.tests_config);
        }

        return text;
    }

    /**
     * The different types of testing methods
     */
    public enum TestType {
        COLORIMETRIC_LIQUID, COLORIMETRIC_STRIP, SENSOR
    }

    /**
     * The different types of color models
     */
    public enum ColorModel {
        RGB, LAB, HSV
    }

    /**
     * The different types of files
     */
    public enum FileType {
        APK, CALIBRATION, CONFIG
    }
}
