package org.akvo.caddisfly.app;

import android.content.Context;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.PreferencesUtils;

/**
 * Static functions to get or set values of various preferences
 */
public class AppPreferences {

    public static boolean isDiagnosticMode(Context context) {
        return PreferencesUtils.getBoolean(context, R.string.diagnosticModeKey, false);
    }

    public static void enableDiagnosticMode(Context context) {
        PreferencesUtils.setBoolean(context, R.string.diagnosticModeKey, true);
    }

    public static void disableDiagnosticMode(Context context) {
        PreferencesUtils.setBoolean(context, R.string.diagnosticModeKey, false);
    }

    public static int getSamplingTimes(Context context) {
        if (isDiagnosticMode(context)) {
            return Integer.parseInt(PreferencesUtils.getString(context,
                    R.string.samplingsTimeKey, String.valueOf(AppConfig.SAMPLING_COUNT_DEFAULT)));
        } else {
            return AppConfig.SAMPLING_COUNT_DEFAULT;
        }
    }

    public static boolean isSoundOff(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtils.getBoolean(context, R.string.noSoundKey, false);
    }

    public static boolean getAutoFocus(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtils.getBoolean(context, R.string.autoFocusKey, false);
    }

    public static boolean getUseFlashMode(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtils.getBoolean(context, R.string.useFlashModeKey, false);
    }

    public static boolean getIgnoreShake(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtils.getBoolean(context, R.string.ignoreShakeKey, false);
    }

    public static boolean getShowDebugMessages(Context context) {
        return isDiagnosticMode(context) &&
                PreferencesUtils.getBoolean(context, R.string.showDebugMessagesKey, false);
    }
}
