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

/**
 * Global Configuration settings for the app
 */
public class AppConfig {

    /**
     * The intent action string used to connect to external app
     */
    public static final String FLOW_ACTION_EXTERNAL_SOURCE = "org.akvo.flow.action.externalsource";

    /**
     * The url to check for update version
     */
    public static final String UPDATE_CHECK_URL
            = "http://caddisfly.ternup.com/app/deviceapprest?action=getLatestVersion&deviceType=androidPhone&appCode=caddisflyapp";

    /**
     * The expected size of the next update file to enable display of the progress bar.
     * Used only if the update process cannot determine the size of the file to download
     */
    public static final int UPDATE_FILE_TYPICAL_SIZE = 910000;

    /**
     * Width and height of cropped image
     */
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;
    /**
     * The delay between each photo taken by the camera during the analysis
     */
    public static final int DELAY_BETWEEN_SAMPLING = 6000;
    /**
     * The number of photos to take during analysis
     */
    public static final int SAMPLING_COUNT_DEFAULT = 5;
    /**
     * The sound volume for the beeps and success/fail sounds
     */
    public static final float SOUND_VOLUME = 0.1f;

}
