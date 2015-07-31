package org.akvo.caddisfly.app;

import android.content.Context;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.PreferencesUtils;

public class AppPreferences {

    public static int getSamplingTimes(Context context) {

        int samplingTimes = Config.SAMPLING_COUNT_DEFAULT;

        if (PreferencesUtils.getBoolean(context, R.string.diagnosticModeKey, false)) {
            samplingTimes = Integer.parseInt(PreferencesUtils.getString(context,
                    R.string.samplingsTimeKey, String.valueOf(Config.SAMPLING_COUNT_DEFAULT)));
        }

        return samplingTimes;
    }
}
