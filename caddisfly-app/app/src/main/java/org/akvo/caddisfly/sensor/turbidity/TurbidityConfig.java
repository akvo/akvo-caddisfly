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

package org.akvo.caddisfly.sensor.turbidity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.PreferencesUtil;

class TurbidityConfig {
    public static final String ACTION_ALARM_RECEIVER = "ACTION_ALARM_RECEIVER";
    private static final int INTENT_REQUEST_CODE = 1000;

    public static void setRepeatingAlarm(Context context, int initialDelay, String uuid) {

        int mDelayMinute = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                uuid + "_IntervalMinutes", "1"));
        PendingIntent pendingIntent = getPendingIntent(context, PendingIntent.FLAG_CANCEL_CURRENT, uuid);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long delay = 6 + (mDelayMinute * 60000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (initialDelay > 0) {
                delay = initialDelay;
            }
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + initialDelay, delay, pendingIntent);
        }
    }

    private static PendingIntent getPendingIntent(Context context, int flag, String uuid) {
        Intent intent = new Intent(context, TurbidityStartReceiver.class);
        intent.setAction(TurbidityConfig.ACTION_ALARM_RECEIVER);
        String savePath = PreferencesUtil.getString(context, R.string.turbiditySavePathKey, "");
        intent.putExtra("savePath", savePath);
        intent.putExtra("uuid", uuid);

        return PendingIntent.getBroadcast(context, TurbidityConfig.INTENT_REQUEST_CODE, intent, flag);
    }

    public static boolean isAlarmRunning(Context context, String uuid) {
        return getPendingIntent(context, PendingIntent.FLAG_NO_CREATE, uuid) != null;
    }

    public static void stopRepeatingAlarm(Context context, String uuid) {
        PendingIntent pendingIntent = getPendingIntent(context, PendingIntent.FLAG_NO_CREATE, uuid);
        if (pendingIntent != null) {
            pendingIntent.cancel();
        }
    }
}
