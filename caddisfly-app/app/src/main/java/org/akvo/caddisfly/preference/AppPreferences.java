package org.akvo.caddisfly.preference;

import android.content.Context;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

/**
 * Static functions to get or set values of various preferences
 */
public class AppPreferences {

    public static boolean isDiagnosticMode(Context context) {
        return PreferencesUtil.getBoolean(context, R.string.diagnosticModeKey, false);
    }

    public static void enableDiagnosticMode(Context context) {
        PreferencesUtil.setBoolean(context, R.string.diagnosticModeKey, true);
    }

    public static void disableDiagnosticMode(Context context) {
        PreferencesUtil.setBoolean(context, R.string.diagnosticModeKey, false);
    }

    public static int getSamplingTimes(Context context) {
        if (isDiagnosticMode(context)) {
            return Integer.parseInt(PreferencesUtil.getString(context,
                    R.string.samplingsTimeKey, String.valueOf(AppConfig.SAMPLING_COUNT_DEFAULT)));
        } else {
            return AppConfig.SAMPLING_COUNT_DEFAULT;
        }
    }

    public static int getColorDistanceTolerance(Context context) {
        if (isDiagnosticMode(context)) {
            return Integer.parseInt(PreferencesUtil.getString(context,
                    R.string.colorDistanceToleranceKey, String.valueOf(ColorUtil.MAX_COLOR_DISTANCE_RGB)));
        } else {
            return ColorUtil.MAX_COLOR_DISTANCE_RGB;
        }
    }

    public static boolean isSoundOff(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtil.getBoolean(context, R.string.noSoundKey, false);
    }

    public static boolean getAutoFocus(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtil.getBoolean(context, R.string.autoFocusKey, false);
    }

    public static boolean getUseFlashMode(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtil.getBoolean(context, R.string.useFlashModeKey, false);
    }

    public static boolean getIgnoreShake(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtil.getBoolean(context, R.string.ignoreShakeKey, false);
    }

    public static boolean getShowDebugMessages(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtil.getBoolean(context, R.string.showDebugMessagesKey, false);
    }
}
