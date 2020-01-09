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

package org.akvo.caddisfly.common;

import org.akvo.caddisfly.BuildConfig;

/**
 * Global Configuration settings for the app.
 */
public final class AppConfig {


    /**
     * Takes screenshots during instrumented tests {see ScreenshotSuite}
     * NOTE: Storage permission is required
     */
    public static final boolean INSTRUMENTED_TEST_TAKE_SCREENSHOTS = false;


    public static final String INSTRUMENTED_TEST_LANGUAGE = "en";


    /**
     * For testing
     */
    public static final boolean IS_TEST_MODE = false;

    /**
     * Do not run sensor tests if no sensors connected
     */
    public static final boolean SENSOR_NOT_CONNECTED = false;

    /**
     * Stop animations only for running Espresso UI tests
     */
    public static final boolean STOP_ANIMATIONS = false;

    /**
     * Stop analytics only for testing
     */
    public static final boolean STOP_ANALYTICS = false;

    /**
     * Stop analytics only for testing
     */
    public static final boolean SKIP_BLUETOOTH_SCAN = false;

    /**
     * Stop result validation only for testing
     */
    public static final boolean SKIP_RESULT_VALIDATION = false;

    /**
     * Stop result validation only for testing
     */
    public static final boolean SKIP_PHOTO_VALIDATION = false;

    /**
     * Date on which the app version will expire.
     * This is to ensure that installs from apk meant for testing only cannot be used for too long.
     */
    public static final boolean APP_EXPIRY = false;
    public static final int APP_EXPIRY_DAY = 1;
    public static final int APP_EXPIRY_MONTH = 11;
    public static final int APP_EXPIRY_YEAR = 2019;

    /**
     * Url to policies and terms
     */
    public static final String TERMS_OF_USE_URL = "https://akvo.org/help/akvo-policies-and-terms-2/";

    /**
     * The intent action string used by the caddisfly question type.
     */
    public static final String EXTERNAL_APP_ACTION = "org.akvo.flow.action.caddisfly";

    /**
     * Uri for photos from built in camera.
     */
    public static final String FILE_PROVIDER_AUTHORITY_URI = BuildConfig.APPLICATION_ID + ".fileprovider";

    /**
     * The sound volume for the beeps and other sound effects.
     */
    public static final float SOUND_EFFECTS_VOLUME = 0.99f;

    /**
     * To launch Flow app.
     */
    public static final String FLOW_SURVEY_PACKAGE_NAME = "org.akvo.flow";

    private AppConfig() {
    }

}
