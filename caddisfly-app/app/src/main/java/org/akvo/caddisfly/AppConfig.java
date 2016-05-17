/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly;

/**
 * Global Configuration settings for the app
 */
public class AppConfig {

    /**
     * To launch Flow app
     */
    public static final String FLOW_SURVEY_PACKAGE_NAME = "org.akvo.flow";

    /**
     * The intent action string used to connect to external app
     *
     * @deprecated use {@link #FLOW_ACTION_CADDISFLY} instead
     */
    @Deprecated
    public static final String FLOW_ACTION_EXTERNAL_SOURCE = "org.akvo.flow.action.externalsource";

    /**
     * The intent action string used by the caddisfly question type
     */
    public static final String FLOW_ACTION_CADDISFLY = "org.akvo.flow.action.caddisfly";

    /**
     * Vendor Id for Sensor devices
     */
    public static final int ARDUINO_VENDOR_ID = 9025;

    /**
     * The sound volume for the beeps and other sound effects
     */
    public static final float SOUND_EFFECTS_VOLUME = 0.99f;

    /**
     * The url to check for update version
     */
    public static final String UPDATE_CHECK_URL
            = "http://caddisfly.ternup.com/app/deviceapprest?action=getLatestVersion&deviceType=androidPhone&appCode=caddisflyapp";

    /**
     * Update check interval
     */
    public static final long UPDATE_CHECK_INTERVAL = 86400000; // 24 hours

}
