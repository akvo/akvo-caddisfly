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

package org.akvo.caddisfly.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import org.akvo.caddisfly.helper.CameraHelper;

import java.util.UUID;

/**
 * Utility functions for api related actions
 */
@SuppressWarnings("deprecation")
public final class ApiUtil {

    private ApiUtil() {
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * Checks if the device has a camera flash
     *
     * @param context the context
     * @return true if camera flash is available
     */
    public static boolean hasCameraFlash(Context context, @NonNull Camera camera) {
        boolean hasFlash = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        try {
            Camera.Parameters p;

            if (hasFlash) {
                p = camera.getParameters();
                if (p.getSupportedFlashModes() == null) {
                    hasFlash = false;
                } else {
                    if (p.getSupportedFlashModes().size() == 1) {
                        if (p.getSupportedFlashModes().get(0).equals("off")) {
                            hasFlash = false;
                        }
                    }
                }
            }
        } finally {
            camera.release();
        }
        return hasFlash;
    }

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    /**
     * Gets an unique id for installation
     *
     * @return the unique id
     */
    public synchronized static String getInstallationId(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }

        return uniqueID;
    }

    public static boolean isCameraInUse(Context context, final Activity activity) {
        Camera camera = null;
        try {
            camera = CameraHelper.getCamera(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            });

        } catch (Exception ignored) {
        }

        if (camera != null) {
            camera.release();
            return false;
        }

        return true;
    }

    public static boolean isStoreVersion(Context context) {
        boolean result = false;

        try {
            String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());
            result = !TextUtils.isEmpty(installer);
        } catch (Throwable ignored) {
        }

        return result;
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}