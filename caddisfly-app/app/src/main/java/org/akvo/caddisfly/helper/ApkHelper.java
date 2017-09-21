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
import android.support.annotation.NonNull;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;

import java.util.ArrayList;
import java.util.Arrays;
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
        if (isNonStoreVersion(activity)) {
            final Uri marketUrl = Uri.parse("market://details?id=" + activity.getPackageName());

            final GregorianCalendar appExpiryDate = new GregorianCalendar(AppConfig.APP_EXPIRY_YEAR,
                    AppConfig.APP_EXPIRY_MONTH - 1, AppConfig.APP_EXPIRY_DAY);

            GregorianCalendar now = new GregorianCalendar();
            if (now.after(appExpiryDate)) {

                String message = String.format("%s%n%n%s", activity.getString(R.string.thisVersionHasExpired),
                        activity.getString(R.string.uninstallAndInstallFromStore));

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(activity);

                builder.setTitle(R.string.versionExpired)
                        .setMessage(message)
                        .setCancelable(false);

                builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, marketUrl));
                    activity.finish();
                });

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the app was installed from the app store or from an install file
     *
     * @param context The context
     * @return True if app was not installed from the store
     * <p>
     * source: http://stackoverflow.com/questions/37539949/detect-if-an-app-is-installed-from-play-store
     */
    public static boolean isNonStoreVersion(@NonNull Context context) {

        // Valid installer package names
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));

        try {
            // The package name of the app that has installed the app
            final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

            // true if the app has been downloaded from Play Store
            return installer == null || !validInstallers.contains(installer);

        } catch (Exception ignored) {
        }

        return true;
    }
}
