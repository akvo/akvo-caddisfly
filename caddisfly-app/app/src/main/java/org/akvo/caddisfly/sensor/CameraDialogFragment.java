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

package org.akvo.caddisfly.sensor;

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
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;

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
public class CameraDialogFragment extends CameraDialog {
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
        if (mCamera != null) {
            mPreview.setCamera(mCamera);
            mPreview.startCameraPreview();
        } else {
            String message = String.format("%s\r\n\r\n%s",
                    getString(R.string.cannotUseCamera),
                    getString(R.string.tryRestarting));

            AlertUtil.showError(getActivity(), R.string.cameraBusy,
                    message, null, R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dismiss();
                        }
                    }, null);
        }
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
            FrameLayout layoutCameraPreview = (FrameLayout) view.findViewById(R.id.layoutCameraPreview);
            layoutCameraPreview.addView(mPreview);

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

    public interface Cancelled {
        void dialogCancelled();
    }

    static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private final SurfaceHolder mHolder;
        private List<Camera.Size> mSupportedPreviewSizes;
        private Camera mCamera;

        @SuppressWarnings("unused")
        private List<String> mSupportedFlashModes;
        private Camera.Size mPreviewSize;

        public CameraPreview(Context context) {
            super(context);
            mHolder = getHolder();
        }

        public CameraPreview(Context context, Camera camera) {
            super(context);
            setCamera(camera);
            mCamera = camera;

            // supported preview sizes
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

            if (AppPreferences.getAutoFocus()) {
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

//            if (parameters.getMaxNumFocusAreas() > 0) {
//                List<Camera.Area> focusAreas = new ArrayList<>();
//                Rect areaRect1 = new Rect(-100, -100, 100, 100);
//                focusAreas.add(new Camera.Area(areaRect1, 1000));
//                parameters.setFocusAreas(focusAreas);
//            }

            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();

            for (Camera.Size size : sizes) {
                if (size.width > 400 && size.width < 1000) {
                    parameters.setPictureSize(size.width, size.height);
                    break;
                }
            }
            if (mSupportedFlashModes != null) {
                if (AppPreferences.getUseFlashMode()) {
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
            // empty. surfaceChanged will take care of stuff
        }


        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

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

            // set preview size and make any resize, rotate or reformatting changes here
            // start preview with new settings
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception ignored) {

            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) h / w;

            if (sizes == null)
                return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.height / size.width;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;

                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }

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
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }

            float ratio;
            if (mPreviewSize.height >= mPreviewSize.width)
                ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
            else
                ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

            // One of these methods should be used, second method squishes preview slightly
            setMeasuredDimension(width, (int) (width * ratio));
//        setMeasuredDimension((int) (width * ratio), height);
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
