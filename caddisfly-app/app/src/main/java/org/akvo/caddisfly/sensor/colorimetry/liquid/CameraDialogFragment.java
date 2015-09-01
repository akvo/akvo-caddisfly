/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ApiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link CameraDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("deprecation")
public class CameraDialogFragment extends CameraDialog
        implements DiagnosticDetailsFragment.ResultDialogListener {
    private int mNumberOfPhotosToTake;
    private int mPhotoCurrentCount = 0;

    private boolean mCancelled = false;
    private Camera mCamera;

    // View to display the camera output.
    private CameraPreview mPreview;

    public CameraDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraDialogFragment.
     */
    public static CameraDialogFragment newInstance() {
        return new CameraDialogFragment();
    }

    private static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Create preview and set it as the content
        if (!safeCameraOpenInView(view)) {
            return null;
        }
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPreview.setCamera(mCamera);
        mPreview.startCameraPreview();
    }

    @Override
    public void takePictureSingle() {
        mNumberOfPhotosToTake = 1;
        mPhotoCurrentCount = 0;
        takePicture();
    }

    @Override
    public void takePictures(int count, long delay) {
        mNumberOfPhotosToTake = count;
        mPhotoCurrentCount = 0;

        Timer timer = new Timer();
        TimeLapseTask task = new TimeLapseTask();
        timer.schedule(task, delay, delay);
    }

    private void takePicture() {
        PictureCallback localCallback = new PictureCallback();
        try {
            mCamera.takePicture(null, null, localCallback);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void stopCamera() {
        mCancelled = true;
        releaseCameraAndPreview();
    }

    /**
     * Checks if requested number of pictures were taken
     *
     * @return true if completed, false if not
     */
    private boolean hasCompleted() {
        return mPhotoCurrentCount >= mNumberOfPhotosToTake;
    }

    private boolean safeCameraOpenInView(View view) {
        boolean opened;
        releaseCameraAndPreview();
        mCamera = ApiUtil.getCameraInstance();
        opened = (mCamera != null);

        if (opened) {
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.layoutCameraPreview);
            preview.addView(mPreview);

            mPreview.startCameraPreview();
        }
        return opened;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCancelled = true;
        releaseCameraAndPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getActivity() != null && getActivity() instanceof Cancelled) {
            ((Cancelled) getActivity()).dialogCancelled();
        }
        super.onCancel(dialog);
    }

    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }

    @Override
    public void onSuccessFinishDialog() {
        this.dismiss();
    }

    public interface Cancelled {
        void dialogCancelled();
    }

    static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private final SurfaceHolder mHolder;

        private Camera mCamera;

        @SuppressWarnings("unused")
        private List<String> mSupportedFlashModes;

        public CameraPreview(Context context) {
            super(context);
            mHolder = getHolder();
        }

        public CameraPreview(Context context, Camera camera) {
            super(context);
            setCamera(camera);
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
        }

        public void startCameraPreview() {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setCamera(Camera camera) {
            mCamera = camera;
            //mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
            Camera.Parameters parameters = mCamera.getParameters();

            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);

            List<String> supportedSceneModes = mCamera.getParameters().getSupportedSceneModes();
            if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO)) {
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            }

            parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
            parameters.setZoom(0);

            List<String> focusModes = parameters.getSupportedFocusModes();

            if (AppPreferences.getAutoFocus(getContext())) {
                // Force auto focus as per preference
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            } else {

                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                } else
                    // Attempt to set focus to infinity if supported
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                    }
            }

            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<>();
                Rect areaRect1 = new Rect(-100, -100, 100, 100);
                meteringAreas.add(new Camera.Area(areaRect1, 1000));
                parameters.setMeteringAreas(meteringAreas);
            }

            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                Rect areaRect1 = new Rect(-100, -100, 100, 100);
                focusAreas.add(new Camera.Area(areaRect1, 1000));
                parameters.setFocusAreas(focusAreas);
            }

            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();

            for (Camera.Size size : sizes) {
                if (size.width > 400 && size.width < 1000) {
                    parameters.setPictureSize(size.width, size.height);
                    break;
                }
            }
            if (mSupportedFlashModes != null) {
                if (AppPreferences.getUseFlashMode(getContext())) {
                    if (mSupportedFlashModes.contains((Camera.Parameters.FLASH_MODE_ON))) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    }
                } else {
                    if (mSupportedFlashModes.contains((Camera.Parameters.FLASH_MODE_TORCH))) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    }
                }
            }

            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

            if (mHolder.getSurface() == null) {
                return;
            }

            try {
                Camera.Parameters parameters = mCamera.getParameters();

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize(sizes, w, h);
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);

//                for (Camera.Size size : sizes) {
//                    if (size.width > 400 && size.width < 1000) {
//                        parameters.setPreviewSize(size.width, size.height);
//                        break;
//                    }
//                }
                requestLayout();

                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Timer task for continuous triggering of preview callbacks
    private class TimeLapseTask extends TimerTask {
        @Override
        public void run() {
            try {
                takePicture();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            mPhotoCurrentCount++;
            if (!mCancelled) {
                for (PictureTaken pictureTakenObserver : pictureTakenObservers) {
                    pictureTakenObserver.onPictureTaken(bytes, hasCompleted());
                }
                if (!hasCompleted()) {
                    try {
                        camera.startPreview();
                    } catch (Exception ignored) {

                    }
                }
            }
        }
    }
}
