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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Surface;
import android.view.WindowManager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;

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

    /**
     * Lock the screen orientation based on the natural position of the device
     *
     * @param activity the activity
     */
    public static void lockScreenOrientation(Activity activity) {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        boolean isTablet = activity.getResources().getBoolean(R.bool.isTablet);

        // Search for the natural position of the device
        if (isTablet) {
            // Natural position is Landscape
            switch (rotation) {
                case Surface.ROTATION_0:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case Surface.ROTATION_90:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case Surface.ROTATION_180:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    break;
                case Surface.ROTATION_270:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
            }
        }
    }

    /**
     * Gets the device's Equipment Id
     *
     * @return the international mobile equipment id
     */
    public static String getEquipmentId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String number = null;
        if (telephonyManager != null) {
            try {
                number = telephonyManager.getDeviceId();
            } catch (Exception ignored) {
            }
        }
        if (number == null) {
            number = "No equipment Id";
        }
        return number;
    }


    public static boolean isCameraInUse(Context context, final Activity activity) {
        Camera camera = null;
        try {
            camera = CaddisflyApp.getCamera(context, new DialogInterface.OnClickListener() {
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
}