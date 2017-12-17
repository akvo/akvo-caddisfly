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
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.util.PreferencesUtil;

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
    }

    /**
     * The number of photos to take during the test.
     *
     * @return number of samples to take
     */
    public static int getSamplingTimes() {
        int samplingTimes;
        if (isDiagnosticMode()) {
            samplingTimes = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.samplingsTimeKey, String.valueOf(ChamberTestConfig.SAMPLING_COUNT_DEFAULT)));
        } else {
            samplingTimes = ChamberTestConfig.SAMPLING_COUNT_DEFAULT;
        }
        //Add skip count as the first few samples may not be valid
        return samplingTimes + ChamberTestConfig.SKIP_SAMPLING_COUNT;
    }

    /**
     * The color distance tolerance for when matching colors.
     *
     * @return the tolerance value
     */
    public static int getColorDistanceTolerance() {
        if (isDiagnosticMode()) {
            return Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.colorDistanceToleranceKey,
                    String.valueOf(ChamberTestConfig.MAX_COLOR_DISTANCE_RGB)));
        } else {
            return ChamberTestConfig.MAX_COLOR_DISTANCE_RGB;
        }
    }

    public static boolean isSoundOn() {
        return !isDiagnosticMode() || PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.soundOnKey, true);
    }

    public static boolean getShowDebugMessages() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.showDebugMessagesKey, false);
    }

    //Diagnostic in user mode
    public static boolean isSaveImagesOn() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.saveImagesDataKey, false);
    }

    public static boolean useExternalCamera() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.useExternalCameraKey, false);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean useFlashMode() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.useFlashModeKey, false);
    }

    public static boolean ignoreTimeDelays() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.ignoreTimeDelaysKey, false);
    }

    /**
     * Preference whether to show bluetooth raw data for debugging purposes.
     *
     * @return true if bluetooth raw data should be displayed
     */
    public static boolean showBluetoothData() {
        return isDiagnosticMode()
                && PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.showBlueToothDataKey, false);

    }
}
