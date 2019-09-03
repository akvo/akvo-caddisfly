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

package org.akvo.caddisfly.sensor.striptest.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.sensor.striptest.ui.StripMeasureActivity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by linda on 7/7/15
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final int MIN_CAMERA_WIDTH = 1300;
    private final Camera mCamera;
    private final StripMeasureActivity activity;
    private int mPreviewWidth;
    private int mPreviewHeight;

    public CameraPreview(Context context) {
        // create surfaceView
        super(context);

        // Create an instance of Camera
        mCamera = TheCamera.getCameraInstance();

        try {
            activity = (StripMeasureActivity) context;
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
            Timber.d("Error setting camera preview: %s", e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
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

        // set preview size and make any resize, rotate or reformatting changes here
        Camera.Size bestSize = setupCamera();

        if (bestSize == null) return;

        try {
            mCamera.setPreviewDisplay(holder);
            mPreviewWidth = bestSize.width;
            mPreviewHeight = bestSize.height;

            activity.setPreviewProperties(w, h, mPreviewWidth, mPreviewHeight);
            activity.initPreviewFragment();
            mCamera.startPreview();
            try {
                // if we are in FOCUS_MODE_AUTO, we have to start the autofocus here.
                if (mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    mCamera.autoFocus((success, camera) -> {
                        // do nothing
                    });
                }
            } catch (Exception ignore) {
            }
        } catch (IOException e) {
            Timber.e(e, mPreviewWidth + "x" + mPreviewHeight);
        }
    }

    @Nullable
    private Camera.Size setupCamera() {
        Camera.Parameters parameters;
        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            return null;

        }
        if (parameters == null) {
            return null;
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
        mCamera.setDisplayOrientation(Constants.DEGREES_90);

        //preview size
        assert bestSize != null;
        parameters.setPreviewSize(bestSize.width, bestSize.height);
        Timber.d("Preview size set to:" + bestSize.width + "," + bestSize.height);

        // default focus mode
        String focusMode = Camera.Parameters.FOCUS_MODE_AUTO;
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();

        // Select FOCUS_MODE_CONTINUOUS_PICTURE if available
        // fall back on FOCUS_MODE_CONTINUOUS_VIDEO if the previous is not available
        // fall back on FOCUS_MODE_AUTO if none are available
        if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
        }

        parameters.setFocusMode(focusMode);

        Camera.Area cardArea = new Camera.Area(new Rect(-1000, -1000, -167, 1000), 1);
        List<Camera.Area> cardAreaList = Collections.singletonList(cardArea);
        if (parameters.getMaxNumFocusAreas() > 0) {
            parameters.setFocusAreas(cardAreaList);
        }

        if (parameters.getWhiteBalance() != null) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            parameters.setMeteringAreas(cardAreaList);
        }

        if (parameters.getFlashMode() != null) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Timber.e(e);
        }
        return bestSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (0 == mPreviewWidth || 0 == mPreviewHeight) {
            setMeasuredDimension(width, height);
        } else {
            setMeasuredDimension(width, width * mPreviewWidth / mPreviewHeight);
        }
    }

    public Camera getCamera() {
        return mCamera;
    }
}
