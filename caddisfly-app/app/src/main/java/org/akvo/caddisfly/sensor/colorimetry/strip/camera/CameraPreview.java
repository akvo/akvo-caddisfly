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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.util.detector.CameraConfigurationUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by linda on 7/7/15
 */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";

    private static final int MIN_CAMERA_WIDTH = 1300;
    private final Camera mCamera;
    private final CameraActivity activity;
    private Camera.Parameters parameters;

    public CameraPreview(Context context) {
        super(context);
        // Create an instance of Camera
        mCamera = TheCamera.getCameraInstance();

        try {
            activity = (CameraActivity) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("must have CameraActivity as Context.", e);
        }
        // SurfaceHolder callback to track when underlying surface is created and destroyed.
        SurfaceHolder mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            Log.d("", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        if (mCamera == null) {
            //Camera was released
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            return;

        }
        if (parameters == null) {
            return;
        }


        Camera.Size bestSize = null;
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        int maxWidth = 0;
        for (Camera.Size size : sizes) {
            if (size.width > MIN_CAMERA_WIDTH) {
                continue;
            }
            if (size.width > maxWidth) {
                bestSize = size;
                maxWidth = size.width;
            }
        }

        //portrait mode
        mCamera.setDisplayOrientation(SensorConstants.DEGREES_90);

        //preview size
        assert bestSize != null;
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        boolean canAutoFocus = false;
        boolean disableContinuousFocus = true;
        List<String> modes = mCamera.getParameters().getSupportedFocusModes();
        for (String s : modes) {

            if (s.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                canAutoFocus = true;

            }
            if (s.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                disableContinuousFocus = false;
            }
        }

        try {
            CameraConfigurationUtils.setFocus(parameters, canAutoFocus, disableContinuousFocus, false);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        //white balance
        if (parameters.getWhiteBalance() != null) {
            //Check if this optimise the code
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Log.e(TAG, "Error setting camera parameters: " + e.getMessage(), e);
        }

        try {
            mCamera.setPreviewDisplay(holder);
            activity.setPreviewProperties();
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    public boolean toggleFlashMode() {
        if (mCamera == null) {
            return false;
        }
        parameters = mCamera.getParameters();

        String flashMode = mCamera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)
                ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF;
        parameters.setFlashMode(flashMode);

        mCamera.setParameters(parameters);

        return flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH);
    }

    //exposure compensation
    public void adjustExposure(int delta) throws RuntimeException {
        if (mCamera == null) {
            return;
        }

        mCamera.cancelAutoFocus();

        if (!parameters.getAutoExposureLock()) {
            parameters.setAutoExposureLock(true);
            mCamera.setParameters(parameters);
        }

        if (delta > 0) {
            parameters.setExposureCompensation(Math.min(parameters.getMaxExposureCompensation(),
                    Math.round(parameters.getExposureCompensation() + 1f)));
        } else if (delta < 0) {
            parameters.setExposureCompensation(Math.max(parameters.getMinExposureCompensation(),
                    Math.round(parameters.getExposureCompensation() - 1f)));
        } else if (delta == 0) {
            parameters.setExposureCompensation(0);
        }

        if (parameters.getAutoExposureLock()) {
            parameters.setAutoExposureLock(false);
            mCamera.setParameters(parameters);
        }
    }

    public Camera getCamera() {
        return mCamera;
    }
}
