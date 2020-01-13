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

/**
 * Global Configuration settings for the app.
 */
object AppConfig {
    /**
     * INSTRUMENTED TESTS
     * Stops animations during running Espresso UI tests
     * Ignores tests that require externals sensors to be connected
     * Skips scanning or connecting to bluetooth devices
     * Takes screenshots during instrumented tests {see ScreenshotSuite}
     * NOTE: Storage permission is required for saving screenshots
     * The language to run the instrumented tests in
     */
    const val INSTRUMENTED_TEST_RUNNING = false
    const val INSTRUMENTED_TEST_TAKE_SCREENSHOTS = false
    const val INSTRUMENTED_TEST_LANGUAGE = "en"

    /**
     * Date on which the app version will expire.
     * This is to ensure that installs from apk meant for testing only cannot be used for too long.
     */
    const val APP_EXPIRY = false
    const val APP_EXPIRY_DAY = 1
    const val APP_EXPIRY_MONTH = 2
    const val APP_EXPIRY_YEAR = 2020

    /**
     * The sound volume for the beeps and other sound effects.
     */
    const val SOUND_EFFECTS_VOLUME = 1f
}