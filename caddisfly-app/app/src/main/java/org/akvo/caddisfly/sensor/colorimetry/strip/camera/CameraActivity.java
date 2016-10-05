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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.sensor.colorimetry.strip.detect.DetectStripListener;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.ResultActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.FinderPatternIndicatorView;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.LevelView;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.detector.FinderPattern;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linda on 7/7/15
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends BaseActivity implements CameraViewListener, DetectStripListener {

    private static final long CAMERA_PREVIEW_DELAY = 500;
    private final MyHandler handler = new MyHandler();
    private final Map<String, Integer> qualityCountMap = new LinkedHashMap<>(3); // <Type, count>
    private boolean torchModeOn = false;
    private WeakReference<CameraActivity> mActivity;
    private Camera mCamera;
    private SoundPoolPlayer sound;
    private WeakReference<Camera> wrCamera;
    private FrameLayout previewLayout;
    private CameraPreview cameraPreview;
    private FinderPatternIndicatorView finderPatternIndicatorView;
    private LevelView levelView;
    private String brandName;
    private CameraSharedFragmentBase currentFragment;
    //OpenCV Manager
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("", "OpenCV loaded successfully");
                    init();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }

                break;
            }
        }
    };
    private int previewFormat = -1;
    private int previewWidth = 0;
    private int previewHeight = 0;
    private ShowFinderPatternRunnable showFinderPatternRunnable;
    private ShowLevelRunnable showLevelRunnable;
    private CameraScheduledExecutorService cameraScheduledExecutorService;
    private CameraCallbackOneShotPreview cameraCallbackOneShotPreview;
    //get instance of CameraPreviewCallback
    //and do a oneShotPreviewCallback.
    //do not do this if currentFragment is instructions, only for prepare and start test fragments
    private final Runnable startNextPreviewRunnable = new Runnable() {
        @Override
        public void run() {
            if (wrCamera != null) {
                if (cameraCallbackOneShotPreview == null)
                    cameraCallbackOneShotPreview = new CameraCallbackOneShotPreview(mActivity.get(),
                            wrCamera.get().getParameters());

                if (currentFragment != null && (currentFragment instanceof CameraPrepareFragment ||
                        currentFragment instanceof CameraStartTestFragment)) {
                    wrCamera.get().setOneShotPreviewCallback(cameraCallbackOneShotPreview);
                }
            }
        }
    };
    private CameraCallbackTakePicture cameraCallbackTakePicture;
    //set takePicture to true in CameraPreviewCallback and start oneShotPreviewCallback with that.
    private final Runnable takeNextPictureRunnable = new Runnable() {
        @Override
        public void run() {
            if (wrCamera != null) {

                if (cameraCallbackTakePicture == null)
                    cameraCallbackTakePicture = new CameraCallbackTakePicture(mActivity.get(),
                            wrCamera.get().getParameters());

                wrCamera.get().setOneShotPreviewCallback(cameraCallbackTakePicture);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        sound = new SoundPoolPlayer(this);

        finderPatternIndicatorView =
                (FinderPatternIndicatorView) findViewById(R.id.finder_indicator);
        levelView = (LevelView) findViewById(R.id.level_cameraLevel);

        showFinderPatternRunnable = new ShowFinderPatternRunnable();
        showLevelRunnable = new ShowLevelRunnable();

        //Initialize count quality map
        qualityCountMap.put("B", 0);
        qualityCountMap.put("S", 0);
        qualityCountMap.put("L", 0);

        mActivity = new WeakReference<>(this);

        cameraScheduledExecutorService = new CameraScheduledExecutorService();

        cameraPreview = new CameraPreview(this);
        mCamera = cameraPreview.getCamera();

        if (mCamera == null) {
            Toast.makeText(this.getApplicationContext(), "Could not instantiate the camera",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            try {
                wrCamera = new WeakReference<>(mCamera);

                // Create our Preview view and set it as the content of our activity.
                previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
                previewLayout.removeAllViews();
                previewLayout.addView(cameraPreview);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {
        try {
            currentFragment = CameraPrepareFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.layout_cameraPlaceholder, currentFragment
            ).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        cameraScheduledExecutorService.shutdown();

        if (mCamera != null) {

            mCamera.setOneShotPreviewCallback(null);

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
        }
        if (wrCamera != null) {
            wrCamera.clear();
            wrCamera = null;
        }

        if (cameraPreview != null && previewLayout != null) {
            previewLayout.removeView(cameraPreview);
            cameraPreview = null;
        }
        super.onPause();
    }

    public void onResume() {

        if (getIntent().getStringExtra(Constant.BRAND) != null) {
            this.brandName = getIntent().getStringExtra(Constant.BRAND);
        } else {
            throw new NullPointerException("Cannot proceed without brand.");
        }

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        //Delete all finder pattern info and image data from internal storage
        new DeleteTask().execute();

        super.onResume();
    }

    // Store previewLayout info in global properties for later use
    public void setPreviewProperties() {
        if (mCamera != null && cameraPreview != null) {
            previewFormat = mCamera.getParameters().getPreviewFormat();
            previewWidth = mCamera.getParameters().getPreviewSize().width;
            previewHeight = mCamera.getParameters().getPreviewSize().height;
        }
    }

    @Override
    public void toggleFlashMode(boolean userSelect) {
        if (cameraPreview != null) {
            if (userSelect) {
                torchModeOn = cameraPreview.toggleFlashMode();
            } else {
                cameraPreview.toggleFlashMode();
            }
        }
    }

    @Override
    public void stopPreview() {
        mCamera.stopPreview();
    }

    @Override
    public void startPreview() {
        mCamera.startPreview();
    }

    @Override
    public void stopCallback() {
        if (cameraCallbackOneShotPreview != null)
            cameraCallbackOneShotPreview.stop();
        if (cameraCallbackTakePicture != null)
            cameraCallbackTakePicture.stop();
    }

    @Override
    public void takeNextPicture(long timeMillis) {

        if (cameraScheduledExecutorService != null) {
            cameraScheduledExecutorService.cancelTasks(timeMillis);
            cameraScheduledExecutorService.scheduleRunnable(takeNextPictureRunnable, timeMillis);
        }
    }

    @Override
    public void startNextPreview() {

        if (cameraScheduledExecutorService != null) {
            cameraScheduledExecutorService.scheduleRunnableWithFixedDelay(
                    startNextPreviewRunnable, 0, CAMERA_PREVIEW_DELAY);
        }
    }

    @Override
    public void setQualityCheckCountZero() {

        for (Map.Entry<String, Integer> entry : qualityCountMap.entrySet()) {
            entry.setValue(0);
        }
    }

    @Override
    public void addCountToQualityCheckCount(int[] countArray) {

        if (countArray == null) {
            throw new NullPointerException("quality checks array is NULL");
        }

        int ci = 0;
        for (Map.Entry<String, Integer> entry : qualityCountMap.entrySet()) {
            entry.setValue(entry.getValue() + countArray[ci]);
            ci++;
        }

        if (currentFragment != null) {
            currentFragment.displayCountQuality(qualityCountMap);

            // Show start button only if enough quality checks are positive
            if (qualityChecksOK()) {
                currentFragment.goNext();
            }
        }
    }

    @Override
    public void nextFragment() {

        if (currentFragment instanceof CameraPrepareFragment) {
            // Display instructions
            currentFragment = InstructionFragment.newInstance(brandName);
        } else if (currentFragment instanceof InstructionFragment) {
            currentFragment = CameraStartTestFragment.newInstance(brandName);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_cameraPlaceholder, currentFragment)
                .commit();
    }

    @Override
    public boolean qualityChecksOK() {
        for (int i : qualityCountMap.values()) {
            if (i < Constant.COUNT_QUALITY_CHECK_LIMIT / qualityCountMap.size()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void showFinderPatterns(final List<FinderPattern> patterns, final Camera.Size size, final int color) {
        showFinderPatternRunnable.setPatterns(patterns);
        showFinderPatternRunnable.setSize(size);
        showFinderPatternRunnable.setColor(color);
        handler.post(showFinderPatternRunnable);
    }

    @Override
    public void showBrightness(final double value) {

        if (currentFragment != null)
            currentFragment.showBrightness(value);
    }

    @Override
    public void showShadow(final double value) {

        if (currentFragment != null)
            currentFragment.showShadow(value);
    }

    @Override
    public void showLevel(final float[] tilts) {
        showLevelRunnable.setAngles(tilts);
        handler.post(showLevelRunnable);
    }

    @Override
    public boolean isTorchModeOn() {
        return torchModeOn;
    }

    @Override
    public void adjustExposureCompensation(int delta) {
        if (cameraPreview != null) {
            cameraPreview.adjustExposure(delta);
        }
    }

    @Override
    public void sendData(final byte[] data, long timeMillis,
                         final FinderPatternInfo info) {

        if (currentFragment instanceof CameraStartTestFragment) {
            ((CameraStartTestFragment) currentFragment).sendData(data, timeMillis, info);
        }

        //clear the finder pattern view after one second and qualityChecksOK the previewLayout again
        showFinderPatterns(null, null, 1);

        //clear level indicator
        showLevel(null);
    }

    @Override
    public void dataSent() {

        if (currentFragment instanceof CameraStartTestFragment) {
            if (previewFormat > 0 && previewWidth > 0 && previewHeight > 0) {
                ((CameraStartTestFragment) currentFragment).dataSent(previewFormat,
                        previewWidth,
                        previewHeight);
            }
        }
    }

    @Override
    public void playSound() {
        sound.playShortResource(R.raw.futurebeep2);
    }

    @Override
    public void showSpinner() {
        if (currentFragment instanceof CameraStartTestFragment) {
            ((CameraStartTestFragment) currentFragment).showSpinner();
        }
    }

    @Override
    public void showMessage(final int what) {

//        //For debugging
//        final String[] messages = new String[]
//                {
//                        getString(R.string.reading_data), //0
//                        getString(R.string.calibrating), //1
//                        getString(R.string.cut_out_strip), //2
//                        "\n\n" + getString(R.string.finished) //3
//
//                };
//        finish.append(messages[what] + "\n");

        final TextView finish = (TextView) findViewById(R.id.activity_cameraFinishText);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (finish != null) {
                    finish.setText(R.string.analysing);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void showMessage(final String message) {
        final TextView finish = (TextView) findViewById(R.id.activity_cameraFinishText);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (finish != null)
                    finish.setText(R.string.analysing);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void showError(final int what) {

        final String[] messages = new String[]
                {
                        getString(R.string.error_conversion), //0
                        getString(R.string.error_no_finder_pattern_info), //1
                        getString(R.string.error_warp), //2
                        getString(R.string.error_detection), //3
                        getString(R.string.error_calibrating), //4
                        getString(R.string.error_cut_out_strip), //5
                        getString(R.string.error_unknown) //6
                };

        final TextView finish = (TextView) findViewById(R.id.activity_cameraFinishText);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (finish != null) {
                    try {
                        int mesNo = what < messages.length ? what : messages.length - 1;
                        finish.setText(messages[mesNo]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void showResults() {
        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra(Constant.BRAND, brandName);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(resultIntent);
        finish();
    }

    private static class MyHandler extends Handler {
    }

    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final FileStorage fileStorage = new FileStorage(CameraActivity.this);

            fileStorage.deleteFromInternalStorage(Constant.INFO);
            fileStorage.deleteFromInternalStorage(Constant.DATA);
            fileStorage.deleteFromInternalStorage(Constant.STRIP);

            return null;
        }
    }

    private class ShowFinderPatternRunnable implements Runnable {
        private final WeakReference<FinderPatternIndicatorView> wrFinderPatternIndicatorView =
                new WeakReference<>(finderPatternIndicatorView);
        private int color;
        private List<FinderPattern> patterns;
        private Camera.Size size;

        @Override
        public void run() {

            wrFinderPatternIndicatorView.get().setColor(color);
            wrFinderPatternIndicatorView.get().showPatterns(patterns, size == null ? 0 : size.width,
                    size == null ? 0 : size.height);
        }

        public void setColor(int color) {
            this.color = color;
        }

        public void setPatterns(List<FinderPattern> patterns) {
            this.patterns = patterns;
        }

        public void setSize(Camera.Size size) {
            this.size = size;
        }
    }

    private class ShowLevelRunnable implements Runnable {
        private float[] tilts;

        @Override
        public void run() {
            if (levelView != null)
                levelView.setAngles(tilts);
        }

        void setAngles(float[] tilts) {
            this.tilts = tilts;
        }
    }
}
