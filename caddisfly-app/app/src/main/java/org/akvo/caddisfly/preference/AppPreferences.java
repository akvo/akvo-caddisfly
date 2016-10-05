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

package org.akvo.caddisfly.preference;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

/**
 * Static functions to get or set values of various preferences
 */
public class AppPreferences {

    public static boolean isDiagnosticMode() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, false);
    }

    public static void enableDiagnosticMode() {
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, true);
    }

    public static void disableDiagnosticMode() {
        PreferencesUtil.setBoolean(CaddisflyApp.getApp(), R.string.diagnosticModeKey, false);
    }

    public static int getSamplingTimes() {
        int samplingTimes;
        if (isDiagnosticMode()) {
            samplingTimes = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.samplingsTimeKey, String.valueOf(ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT)));
        } else {
            samplingTimes = ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT;
        }
        //Add 1 because first sample is always discarded during analysis
        return samplingTimes + 1;
    }

    public static int getColorDistanceTolerance() {
        if (isDiagnosticMode()) {
            return Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    R.string.colorDistanceToleranceKey, String.valueOf(ColorUtil.MAX_COLOR_DISTANCE_RGB)));
        } else {
            return ColorUtil.MAX_COLOR_DISTANCE_RGB;
        }
    }

    public static boolean isSoundOff() {
        return isDiagnosticMode() &&
                PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.noSoundKey, false);
    }

    public static boolean getShowDebugMessages() {
        return isDiagnosticMode() &&
                PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.showDebugMessagesKey, false);
    }

    //Diagnostic in user mode
    public static boolean getNoBackdropDetection() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.noBackdropDetectionKey, false);
    }

    //Diagnostic in user mode
    public static boolean getExternalCameraMultiDeviceMode() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.externalCameraMultiDeviceModeKey, false);
    }

    //Diagnostic in user mode
    public static boolean isSaveImagesOn() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.saveImagesKey, false);
    }

    public static boolean useExternalCamera() {
        return PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.useExternalCameraKey, false);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean useFlashMode() {
        return isDiagnosticMode() &&
            PreferencesUtil.getBoolean(CaddisflyApp.getApp(), R.string.useFlashModeKey, false);
    }
}
