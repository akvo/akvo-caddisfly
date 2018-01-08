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

/**
 * Global Configuration settings for the app.
 */
public final class AppConstants {

    /**
     * Date on which the app version will expire.
     * This is to ensure that installs from apk meant for testing only cannot be used for too long.
     */
    public static final int APP_EXPIRY_DAY = 15;
    public static final int APP_EXPIRY_MONTH = 12;
    public static final int APP_EXPIRY_YEAR = 2017;

    public static final String APP_FOLDER = "Akvo Caddisfly";
    public static final String FFEM_FOLDER = "";

    private AppConstants() {
    }

}
