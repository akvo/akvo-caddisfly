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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link CameraDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("deprecation")
public class CameraDialogFragment extends DialogFragment
        implements DiagnosticResultFragment.ResultDialogListener {
    private static final String ARG_DIAGNOSTIC_MODE = "diagnostics";
    public Camera.PictureCallback pictureCallback;
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private int samplingCount;
    private int picturesTaken;
    private boolean mDiagnosticMode;
    private SoundPoolPlayer sound;
    private boolean mCancelled = false;
    private AlertDialog progressDialog;
    private Camera mCamera;
    private Fragment mFragment;
    // View to display the camera output.
    private CameraPreview mPreview;

    public CameraDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param diagnosticMode if true will display preview only, otherwise start taking pictures
     * @return A new instance of fragment CameraDialogFragment.
     */
    @SuppressWarnings("SameParameterValue")
    public static CameraDialogFragment newInstance(boolean diagnosticMode) {
        CameraDialogFragment fragment = new CameraDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DIAGNOSTIC_MODE, diagnosticMode);
        fragment.setArguments(args);
        return fragment;
    }

    public static CameraDialogFragment newInstance() {
        return new CameraDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDiagnosticMode = getArguments().getBoolean(ARG_DIAGNOSTIC_MODE);
        }
        mFragment = this;

        sound = new SoundPoolPlayer(getActivity());
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
        if (mCamera != null && !mDiagnosticMode) {
            startTakingPictures();
        }
    }

    private void startTakingPictures() {

        samplingCount = 0;
        picturesTaken = 0;
        Context context = getActivity();
        progressDialog = new AlertDialog.Builder(context).create();
        progressDialog.setMessage(getString(R.string.analyzingWait));
        progressDialog.setCancelable(false);

        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.show();

        takePicture(mDiagnosticMode);
    }

    private void takePicture(boolean isPreview) {

        mPreview.setCamera(mCamera);
        if (getActivity() != null) {
            mPreview.startCameraPreview();
            try {

                if (isPreview) {
                    PictureCallback localCallback = new PictureCallback();
                    mCamera.takePicture(null, null, localCallback);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PictureCallback localCallback = new PictureCallback();
                            try {
                                mCamera.takePicture(null, null, localCallback);
                            } catch (Exception ignored) {

                            }
                        }
                    }, AppConfig.DELAY_BETWEEN_SAMPLING);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void stopCamera() {
        mCancelled = true;
        releaseCameraAndPreview();
    }

    /**
     * Checks if requested number of pictures were taken
     *
     * @return true if completed, false if not
     */
    public boolean hasTestCompleted() {
        return (getActivity() != null && samplingCount >
                AppPreferences.getSamplingTimes(getActivity())) || picturesTaken > 10;
    }

    private boolean safeCameraOpenInView(View view) {
        boolean qOpened;
        releaseCameraAndPreview();
        mCamera = ApiUtil.getCameraInstance();
        qOpened = (mCamera != null);

        if (qOpened) {
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.layoutCameraPreview);
            preview.addView(mPreview);

            //in diagnostic mode allow user to long press or place device face down to to run a quick test
            if (mDiagnosticMode) {

                //Set up the shake detector
                mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
                final Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
                    @Override
                    public void onShake() {
                    }
                }, new ShakeDetector.OnNoShakeListener() {
                    @Override
                    public void onNoShake() {
                        unregisterShakeDetector();
                        takePicture(mDiagnosticMode);
                    }
                });

                mShakeDetector.minShakeAcceleration = 5;
                mShakeDetector.maxShakeDuration = 2000;

                mSensorManager.registerListener(mShakeDetector, accelerometer,
                        SensorManager.SENSOR_DELAY_UI);

                preview.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        unregisterShakeDetector();
                        takePicture(mDiagnosticMode);
                        return false;
                    }
                });
            }

            mPreview.startCameraPreview();
        }
        return qOpened;
    }

    private void unregisterShakeDetector() {
        try {
            mSensorManager.unregisterListener(mShakeDetector);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterShakeDetector();
        sound.release();
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

            Camera.Size mSize;
            for (Camera.Size size : sizes) {
                if (size.width > 400 && size.width < 1000) {
                    mSize = size;
                    parameters.setPictureSize(mSize.width, mSize.height);
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

            requestLayout();
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

                Camera.Size mSize;
                for (Camera.Size size : sizes) {
                    if (size.width > 400 && size.width < 1000) {
                        mSize = size;
                        parameters.setPictureSize(mSize.width, mSize.height);
                        break;
                    }
                }

                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            samplingCount++;
            picturesTaken++;
            if (!mCancelled) {
                if (!hasTestCompleted()) {
                    sound.playShortResource(R.raw.beep);
                    if (pictureCallback == null) {
                        stopCamera();
                        Bitmap bitmap = ImageUtil.getBitmap(bytes);

                        DiagnosticResultFragment diagnosticResultFragment =
                                DiagnosticResultFragment.newInstance(
                                        ImageUtil.getCroppedBitmap(bitmap,
                                                AppConfig.SAMPLE_CROP_LENGTH_DEFAULT),
                                        bitmap, bitmap.getWidth() + " x " + bitmap.getHeight(), mFragment);

                        final FragmentTransaction ft = getFragmentManager().beginTransaction();

                        Fragment prev = getFragmentManager().findFragmentByTag("resultDialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }

                        diagnosticResultFragment.setCancelable(true);
                        diagnosticResultFragment.show(ft, "resultDialog");

                    } else {
                        pictureCallback.onPictureTaken(bytes, camera);
                        takePicture(mDiagnosticMode);
                    }
                } else {
                    pictureCallback.onPictureTaken(bytes, camera);
                }
            }
        }
    }
}
