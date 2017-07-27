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

package org.akvo.caddisfly.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class ShowNotificationJob extends Job {

    static final String TAG = "show_notification";
    private static final long DAYS_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int CALIBRATION_NOTIFICATION_ID = 4233;
    private static final int VERSION_EXPIRY_NOTIFICATION_ID = 5676;

    public static void schedulePeriodic() {
        new JobRequest.Builder(ShowNotificationJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                .setUpdateCurrent(true)
                .setPersisted(true)
                .build()
                .schedule();
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        List<TestInfo> tests = TestConfigHelper.getTestsByType(TestType.COLORIMETRIC_LIQUID);

        for (TestInfo testInfo : tests) {

            String uuid = testInfo.getId();

            long milliseconds = PreferencesUtil.getLong(getContext(), uuid, R.string.calibrationExpiryDateKey);

            long difference = milliseconds - new Date().getTime();

            int remainingDays = (int) (difference / DAYS_IN_MILLIS);

            if (remainingDays > -1 && remainingDays < 16) {

                //String message = getContext().getResources().getQuantityString(R.plurals.calibrationWillExpire, remainingDays);

                //message = String.format(message, remainingDays, testInfo.getName());

                Intent i = new Intent(getContext(), CalibrateListActivity.class);
                i.putExtra("uuid", uuid);

                Bundle bundle = new Bundle();
                bundle.putString("uuid", uuid);
                PendingIntent pi = PendingIntent.getActivity(getContext(), 0, i, 0);

                Notification notification = new NotificationCompat.Builder(getContext())
                        .setContentTitle(getContext().getString(R.string.appName))
                        .setContentText(getContext().getString(R.string.calibrationExpireSoon))
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(pi)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setShowWhen(true)
                        .setColor(ContextCompat.getColor(getContext(), R.color.akvo_orange))
                        .setLocalOnly(true)
                        .setExtras(new Bundle())
                        .build();

                NotificationManagerCompat.from(getContext())
                        .notify(CALIBRATION_NOTIFICATION_ID, notification);

                break;
            }
        }

        if (!ApkHelper.isStoreVersion(getContext())) {

            final GregorianCalendar appExpiryDate = new GregorianCalendar(AppConfig.APP_EXPIRY_YEAR,
                    AppConfig.APP_EXPIRY_MONTH - 1, AppConfig.APP_EXPIRY_DAY);

            GregorianCalendar now = new GregorianCalendar();
            long difference = appExpiryDate.getTimeInMillis() - now.getTimeInMillis();

            int remainingDays = (int) (difference / DAYS_IN_MILLIS);
            if (remainingDays > -1 && remainingDays < 5) {

                Intent i = new Intent(getContext(), MainActivity.class);
                i.putExtra("appExpiryNotification", true);
                i.addFlags(FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent pi = PendingIntent.getActivity(getContext(), 0, i, 0);

                Notification appExpiryNotification = new NotificationCompat.Builder(getContext())
                        .setContentTitle(getContext().getString(R.string.appName))
                        .setContentText(getContext().getString(R.string.appVersionWillExpire))
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(pi)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setShowWhen(true)
                        .setColor(ContextCompat.getColor(getContext(), R.color.akvo_orange))
                        .setLocalOnly(true)
                        .build();

                NotificationManagerCompat.from(getContext())
                        .notify(VERSION_EXPIRY_NOTIFICATION_ID, appExpiryNotification);
            }
        }

        return Result.SUCCESS;
    }

}