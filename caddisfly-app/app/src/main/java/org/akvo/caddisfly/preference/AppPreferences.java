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

package org.akvo.caddisfly.preference;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.akvo.caddisfly.common.AppConfig.INSTRUMENTED_TEST_TAKE_SCREENSHOTS;
import static org.akvo.caddisfly.common.AppConfig.IS_TEST_MODE;

/**
 * Static functions to get or set values of various preferences.
 */
public final class AppPreferences {

    private AppPreferences() {
    }

    public static boolean isDiagnosticMode() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, false);
    }

    public static void enableDiagnosticMode() {
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, true);
    }

    public static void disableDiagnosticMode() {
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, false);
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.testModeOnKey, false);
    }

    public static boolean isSoundOn() {
        return !isDiagnosticMode() || PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.soundOnKey, true);
    }

    public static boolean getShowDebugInfo() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.showDebugMessagesKey, false);
    }

    public static boolean isTestMode() {
        return IS_TEST_MODE || (isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.testModeOnKey, false));
    }

    public static boolean ignoreTimeDelays() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.ignoreTimeDelaysKey, false);
    }

    public static void saveLastAppUpdateCheck() {
        PreferencesUtil.setLong(CaddisflyApp.getApp(), "lastUpdateCheck",
                Calendar.getInstance().getTimeInMillis());
    }

    public static boolean isAppUpdateCheckRequired() {
        if (INSTRUMENTED_TEST_TAKE_SCREENSHOTS) {
            return true;
        }
        long lastCheck = PreferencesUtil.getLong(CaddisflyApp.getApp(), "lastUpdateCheck");
        return TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis() - lastCheck) > 0;
    }
}
