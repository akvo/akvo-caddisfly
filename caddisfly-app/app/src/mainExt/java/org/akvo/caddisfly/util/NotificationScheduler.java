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

package org.akvo.caddisfly.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.updater.NotificationCancelReceiver;
import org.akvo.caddisfly.updater.UpdateAppReceiver;

public class NotificationScheduler {
    private static final String EXTRA_NOTIFICATION_ID = "android.intent.extra.NOTIFICATION_ID";
    private static final int DAILY_REMINDER_REQUEST_CODE = 100;

    public static void showNotification(Context context, String title, String content) {

        Intent updateIntent = new Intent(context, UpdateAppReceiver.class);
        updateIntent.setAction(Integer.toString(DAILY_REMINDER_REQUEST_CODE));
        updateIntent.putExtra(EXTRA_NOTIFICATION_ID, DAILY_REMINDER_REQUEST_CODE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, updateIntent, 0);

        Intent snoozeIntent = new Intent(context, NotificationCancelReceiver.class);
        snoozeIntent.setAction(Integer.toString(DAILY_REMINDER_REQUEST_CODE));
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, DAILY_REMINDER_REQUEST_CODE);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = builder.setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setSmallIcon(R.mipmap.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .addAction(0, "Later", snoozePendingIntent)
                .addAction(0, "Update", pendingIntent)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(DAILY_REMINDER_REQUEST_CODE, notification);
        }

    }

}