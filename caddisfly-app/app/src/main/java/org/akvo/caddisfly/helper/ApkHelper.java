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

package org.akvo.caddisfly.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Installation related utility methods.
 */
public final class ApkHelper {

    private ApkHelper() {
    }

    /**
     * Checks if app version has expired and if so displays an expiry message and closes activity.
     *
     * @param activity The activity
     * @return True if the app has expired
     */
    public static boolean isAppVersionExpired(@NonNull final Activity activity) {
        //noinspection ConstantConditions
        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("release") &&
                isNonStoreVersion(activity)) {
            final Uri marketUrl = Uri.parse("https://play.google.com/store/apps/details?id=" +
                    activity.getPackageName());

            final Calendar appExpiryDate = GregorianCalendar.getInstance();
            appExpiryDate.setTime(BuildConfig.BUILD_TIME);
            appExpiryDate.add(Calendar.DAY_OF_YEAR, 15);

            if ((new GregorianCalendar()).after(appExpiryDate)) {

                String message = String.format("%s%n%n%s", activity.getString(R.string.thisVersionHasExpired),
                        activity.getString(R.string.uninstallAndInstallFromStore));

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(activity);

                builder.setTitle(R.string.versionExpired)
                        .setMessage(message)
                        .setCancelable(false);

                builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(marketUrl);
                        intent.setPackage("com.android.vending");
                        activity.startActivity(intent);
                    } catch (Exception ignore) {
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        activity.finishAndRemoveTask();
                    } else {
                        activity.finish();
                    }
                });

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the app was installed from the app store or from an install file.
     * source: http://stackoverflow.com/questions/37539949/detect-if-an-app-is-installed-from-play-store
     *
     * @param context The context
     * @return True if app was not installed from the store
     */
    public static boolean isNonStoreVersion(@NonNull Context context) {

        // Valid installer package names
        List<String> validInstallers = new ArrayList<>(
                Arrays.asList("com.android.vending", "com.google.android.feedback"));

        try {
            // The package name of the app that has installed the app
            final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

            // true if the app has been downloaded from Play Store
            return installer == null || !validInstallers.contains(installer);

        } catch (Exception ignored) {
            // do nothing
        }

        return true;
    }
}
