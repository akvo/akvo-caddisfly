package org.akvo.caddisfly.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.StringRes;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

public class CameraHelper {

    private static boolean hasCameraFlash;

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
            String message = String.format("%s\r\n\r\n%s",
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
}
