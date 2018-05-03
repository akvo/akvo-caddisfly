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

package org.akvo.caddisfly.sensor.chamber;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.preference.AppPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.akvo.caddisfly.util.ApiUtil.getCameraInstance;

public class ChamberCameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final int METERING_AREA_SIZE = 100;
    private static final int EXPOSURE_COMPENSATION = -2;
    private static final int MIN_PICTURE_WIDTH = 640;
    private static final int MIN_PICTURE_HEIGHT = 480;
    private static final int MIN_SUPPORTED_WIDTH = 400;
    private final SurfaceHolder mHolder;
    private Camera mCamera;

    /**
     * Camera preview.
     *
     * @param context the context
     */
    public ChamberCameraPreview(Context context) {
        super(context);
        mCamera = getCameraInstance();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    /**
     * Surface created.
     *
     * @param holder the holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Timber.d("Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    /**
     * Surface changed.
     *
     * @param holder the holder
     * @param format the format
     * @param w      the width
     * @param h      the height
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Timber.d("Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * Camera setup.
     *
     * @param camera the camera
     */
    public void setupCamera(Camera camera) {
        mCamera = camera;
        Camera.Parameters parameters = mCamera.getParameters();

        List<String> supportedWhiteBalance = mCamera.getParameters().getSupportedWhiteBalance();
        if (supportedWhiteBalance != null && supportedWhiteBalance.contains(
                Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT)) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
        }

        List<String> supportedSceneModes = mCamera.getParameters().getSupportedSceneModes();
        if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO)) {
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }

        List<String> supportedColorEffects = mCamera.getParameters().getSupportedColorEffects();
        if (supportedColorEffects != null && supportedColorEffects.contains(Camera.Parameters.EFFECT_NONE)) {
            parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
        }

        List<Integer> supportedPictureFormats = mCamera.getParameters().getSupportedPictureFormats();
        if (supportedPictureFormats != null && supportedPictureFormats.contains(ImageFormat.JPEG)) {
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
        }

        List<String> focusModes = parameters.getSupportedFocusModes();

        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            // Attempt to set focus to infinity if supported
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            Rect areaRect1 = new Rect(-METERING_AREA_SIZE, -METERING_AREA_SIZE,
                    METERING_AREA_SIZE, METERING_AREA_SIZE);
            meteringAreas.add(new Camera.Area(areaRect1, 1000));
            parameters.setMeteringAreas(meteringAreas);
        }

        parameters.setExposureCompensation(EXPOSURE_COMPENSATION);

        if (parameters.isZoomSupported()) {
            if (AppPreferences.useMaxZoom()) {
                parameters.setZoom(parameters.getMaxZoom());
            } else {
                parameters.setZoom(0);
            }
        }

        mCamera.setDisplayOrientation(Constants.DEGREES_90);

        parameters.setPictureSize(MIN_PICTURE_WIDTH, MIN_PICTURE_HEIGHT);

        try {
            mCamera.setParameters(parameters);
        } catch (Exception ex) {
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();

            parameters.setPictureSize(
                    supportedPictureSizes.get(supportedPictureSizes.size() - 1).width,
                    supportedPictureSizes.get(supportedPictureSizes.size() - 1).height);

            for (Camera.Size size : supportedPictureSizes) {
                if (size.width > MIN_SUPPORTED_WIDTH && size.width < 1000) {
                    parameters.setPictureSize(size.width, size.height);
                    break;
                }
            }

            mCamera.setParameters(parameters);
        }
    }

    public Camera getCamera() {
        return mCamera;
    }
}