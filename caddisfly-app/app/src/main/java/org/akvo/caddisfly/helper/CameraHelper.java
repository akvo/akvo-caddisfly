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

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.StringRes;
import android.util.Log;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

public final class CameraHelper {

    private static final String TAG = "CameraHelper";

    private static final float ONE_MILLION = 1000000f;
    private static boolean hasCameraFlash;

    private CameraHelper() {
    }

    /**
     * Check if the camera is available
     *
     * @param context         the context
     * @param onClickListener positive button listener
     * @return true if camera flash exists otherwise false
     */
    @SuppressWarnings("deprecation")
    public static Camera getCamera(Context context,
                                   DialogInterface.OnClickListener onClickListener) {

        Camera camera = ApiUtil.getCameraInstance();
        if (hasFeatureBackCamera(context, onClickListener) && camera == null) {
            String message = String.format("%s%n%n%s",
                    context.getString(R.string.cannotUseCamera),
                    context.getString(R.string.tryRestarting));

            AlertUtil.showError(context, R.string.cameraBusy,
                    message, null, R.string.ok, onClickListener, null, null);
            return null;
        }

        return camera;
    }

    private static boolean hasFeatureBackCamera(Context context,
                                                DialogInterface.OnClickListener onClickListener) {
        PackageManager packageManager = context.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            AlertUtil.showAlert(context, R.string.cameraNotAvailable,
                    R.string.cameraRequired,
                    R.string.ok, onClickListener, null, null);
            return false;
        }
        return true;
    }

    /**
     * Check if the device has a camera flash
     *
     * @param context         the context
     * @param onClickListener positive button listener
     * @return true if camera flash exists otherwise false
     */
    @SuppressWarnings("SameParameterValue")
    public static boolean hasFeatureCameraFlash(Context context, @StringRes int errorTitle,
                                                @StringRes int buttonText,
                                                DialogInterface.OnClickListener onClickListener) {

        if (PreferencesUtil.containsKey(context, R.string.hasCameraFlashKey)) {
            hasCameraFlash = PreferencesUtil.getBoolean(context, R.string.hasCameraFlashKey, false);
        } else {

            @SuppressWarnings("deprecation")
            Camera camera = getCamera(context, onClickListener);
            try {
                if (camera != null) {
                    hasCameraFlash = ApiUtil.hasCameraFlash(context, camera);
                    PreferencesUtil.setBoolean(context, R.string.hasCameraFlashKey, hasCameraFlash);
                }
            } finally {
                if (camera != null) {
                    camera.release();
                }

            }
        }

        if (!hasCameraFlash) {
            AlertUtil.showAlert(context, errorTitle,
                    R.string.errorCameraFlashRequired,
                    buttonText, onClickListener, null, null);

        }
        return hasCameraFlash;
    }

    public static int getMaxSupportedMegaPixelsByCamera(Context context) {

        int cameraMegaPixels = 0;

        if (PreferencesUtil.containsKey(context, R.string.cameraMegaPixelsKey)) {
            cameraMegaPixels = PreferencesUtil.getInt(context, R.string.cameraMegaPixelsKey, 0);
        } else {

            Camera camera = ApiUtil.getCameraInstance();
            try {

                // make sure the camera is not in use
                if (camera != null) {
                    Camera.Parameters allParams = camera.getParameters();
                    for (Camera.Size pictureSize : allParams.getSupportedPictureSizes()) {
                        int sizeInMegaPixel = (int) Math.ceil((pictureSize.width * pictureSize.height) / ONE_MILLION);
                        if (sizeInMegaPixel > cameraMegaPixels) {
                            cameraMegaPixels = sizeInMegaPixel;
                        }
                    }
                }

                PreferencesUtil.setInt(context, R.string.cameraMegaPixelsKey, cameraMegaPixels);

            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                if (camera != null) {
                    camera.release();
                }
            }
        }
        return cameraMegaPixels;
    }

}
