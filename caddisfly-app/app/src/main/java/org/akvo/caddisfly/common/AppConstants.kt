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
package org.akvo.caddisfly.common

import org.akvo.caddisfly.BuildConfig

/**
 * Global Configuration settings for the app.
 */
object AppConstants {
    /**
     * Url to policies and terms
     */
    const val TERMS_OF_USE_URL = "https://akvo.org/help/akvo-policies-and-terms-2/"

    /**
     * The intent action string used by the caddisfly question type.
     */
    const val EXTERNAL_APP_ACTION = "org.akvo.flow.action.caddisfly"
    /**
     * Uri for photos from built in camera.
     */
    const val FILE_PROVIDER_AUTHORITY_URI = BuildConfig.APPLICATION_ID + ".fileprovider"
    /**
     * To launch Flow app.
     */
    const val FLOW_SURVEY_PACKAGE_NAME = "org.akvo.flow"
}