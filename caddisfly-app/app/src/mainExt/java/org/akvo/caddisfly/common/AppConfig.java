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
     * Date on which the app version will expire.
     * This is to ensure that installs from apk meant for testing only cannot be used for too long.
     */
    public static final boolean APP_EXPIRY = true;
    public static final int APP_EXPIRY_DAY = 15;
    public static final int APP_EXPIRY_MONTH = 3;
    public static final int APP_EXPIRY_YEAR = 2018;

    /**
     * The intent action string used to connect to external app.
     *
     * @deprecated use {@link #FLOW_ACTION_CADDISFLY} instead
     */
    @Deprecated
    public static final String FLOW_ACTION_EXTERNAL_SOURCE = "org.akvo.flow.action.externalsource";
    /**
     * The intent action string used by the caddisfly question type.
     */
    public static final String FLOW_ACTION_CADDISFLY = "org.akvo.flow.action.caddisfly";

    /**
     * Uri for photos from built in camera.
     */
    public static final String FILE_PROVIDER_AUTHORITY_URI = BuildConfig.APPLICATION_ID + ".fileprovider";

    /**
     * The sound volume for the beeps and other sound effects.
     */
    public static final float SOUND_EFFECTS_VOLUME = 0.99f;

    private AppConfig() {
    }

}
