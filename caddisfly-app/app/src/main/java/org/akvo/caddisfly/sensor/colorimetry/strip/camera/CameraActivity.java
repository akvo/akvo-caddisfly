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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.model.TestStatus;
import org.akvo.caddisfly.sensor.colorimetry.strip.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.ResultActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.FinderPatternIndicatorView;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.LevelView;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.detector.FinderPattern;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;
import org.akvo.caddisfly.widget.TimerView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * The activity that displays the camera preview.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends BaseActivity implements CameraViewListener {

    private static final long CAMERA_PREVIEW_DELAY = 500;
    private static final int PROGRESS_FADE_DURATION_MILLIS = 4000;
    private static final int LONG_TIME = 35;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
    private final MyHandler handler = new MyHandler();
    private final Map<String, Integer> qualityCountMap = new LinkedHashMap<>(3); // <Type, count>
    private boolean torchModeOn = false;
    private WeakReference<CameraActivity> mActivity;
    private Camera mCamera;
    private SoundPoolPlayer sound;
    @Nullable
    private WeakReference<Camera> wrCamera;
    private FrameLayout previewLayout;
    @Nullable
    private CameraPreview cameraPreview;
    private FinderPatternIndicatorView finderPatternIndicatorView;
    private LevelView levelView;
    private String uuid;
    private CameraSharedFragmentBase currentFragment;
    //OpenCV Manager
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    init();
                    break;
                default:
                    super.onManagerConnected(status);
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
    @Nullable
    private final Runnable startNextPreviewRunnable = new Runnable() {
        @Override
        public void run() {
            if (wrCamera != null) {
                Camera camera = wrCamera.get();
                if (camera != null) {
                    if (cameraCallbackOneShotPreview == null) {
                        cameraCallbackOneShotPreview = new CameraCallbackOneShotPreview(mActivity.get(),
                                camera.getParameters());
                    }

                    if (currentFragment != null && (currentFragment instanceof CameraPrepareFragment
                            || currentFragment instanceof CameraStartTestFragment)) {
                        camera.setOneShotPreviewCallback(cameraCallbackOneShotPreview);
                    }
                }
            }
        }
    };
    private CameraCallbackTakePicture cameraCallbackTakePicture;
    //set takePicture to true in CameraPreviewCallback and start oneShotPreviewCallback with that.
    @Nullable
    private final Runnable takeNextPictureRunnable = new Runnable() {
        @Override
        public void run() {
            if (wrCamera != null) {

                Camera camera = wrCamera.get();
                if (camera != null) {
                    if (cameraCallbackTakePicture == null) {
                        cameraCallbackTakePicture = new CameraCallbackTakePicture(mActivity.get(),
                                camera.getParameters());
                    }

                    camera.setOneShotPreviewCallback(cameraCallbackTakePicture);
                }
            }
        }
    };
    private TimerView timerCountdown;
    private boolean mCameraPaused;
    private InstructionFragment instructionFragment;
    private LinearLayout parentLayout;
    private Snackbar snackbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_view);

        parentLayout = (LinearLayout) findViewById(R.id.activity_cameraMainLayout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sound = new SoundPoolPlayer(this);

        finderPatternIndicatorView =
                (FinderPatternIndicatorView) findViewById(R.id.finder_indicator);

        levelView = (LevelView) findViewById(R.id.level_cameraLevel);

        timerCountdown = (TimerView) findViewById(R.id.countdownTimer);

        showFinderPatternRunnable = new ShowFinderPatternRunnable();
        showLevelRunnable = new ShowLevelRunnable();

        //Initialize count quality map
        qualityCountMap.put("B", 0);
        qualityCountMap.put("S", 0);
        qualityCountMap.put("L", 0);

        mActivity = new WeakReference<>(this);

        cameraScheduledExecutorService = new CameraScheduledExecutorService();

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                startCameraPreview();
            }
        }, 0);
    }


    private void startCameraPreview() {

        cameraPreview = new CameraPreview(this);

        mCamera = cameraPreview.getCamera();

        previewLayout = (FrameLayout) findViewById(R.id.camera_preview);

        if (mCamera == null) {
            Toast.makeText(this.getApplicationContext(), "Could not instantiate the camera",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            try {
                wrCamera = new WeakReference<>(mCamera);

                previewLayout.removeAllViews();
                if (cameraPreview != null) {
                    previewLayout.addView(cameraPreview);
                } else {
                    finish();
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void init() {
        try {
            if (currentFragment == null) {
                currentFragment = CameraPrepareFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.layout_cameraPlaceholder, currentFragment
                ).commit();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onPause() {
        releaseResources();
        if (!isFinishing()) {
            finish();
        }
        super.onPause();
    }

    private void releaseResources() {
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
    }

    @Override
    public void onResume() {

        uuid = getIntent().getStringExtra(Constant.UUID);

        if (uuid != null) {
            StripTest stripTest = new StripTest();
            setTitle(stripTest.getBrand(uuid).getName());

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

            //Delete all finder pattern info and image data from internal storage
            new DeleteTask().execute();
        } else {
            finish();
        }

        super.onResume();
    }

    /**
     * Store previewLayout info in global properties for later use.
     */
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
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    public void startPreview() {
        mCamera.startPreview();
    }

    @Override
    public void stopCallback() {
        if (cameraCallbackOneShotPreview != null) {
            cameraCallbackOneShotPreview.stop();
        }
        if (cameraCallbackTakePicture != null) {
            cameraCallbackTakePicture.stop();
        }
    }

    @Override
    public void takeNextPicture(long delay) {

        if (cameraScheduledExecutorService != null) {
            cameraScheduledExecutorService.cancelTasks(delay);
            cameraScheduledExecutorService.scheduleRunnable(takeNextPictureRunnable, delay);
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
    public void addCountToQualityCheckCount(@NonNull int[] countArray) {

        if (!CalibrationCard.hasError()) {
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
    }

    @Override
    public void nextFragment() {

        if (currentFragment instanceof CameraPrepareFragment) {
            // Display instructions
            currentFragment = InstructionFragment.newInstance(uuid);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout_instructionLayout, currentFragment)
                    .commit();
        } else if (currentFragment instanceof InstructionFragment) {
            getSupportFragmentManager().beginTransaction()
                    .remove(currentFragment)
                    .commit();

            currentFragment = CameraStartTestFragment.newInstance(uuid);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout_cameraPlaceholder, currentFragment)
                    .commit();
        } else if (currentFragment instanceof CameraStartTestFragment) {

            if (instructionFragment == null) {

                instructionFragment = InstructionFragment.newInstance(uuid, 2);

                getSupportFragmentManager().beginTransaction()
                        .hide(currentFragment)
                        .add(R.id.layout_instructionLayout, instructionFragment)
                        .commit();
            } else {

                getSupportFragmentManager().beginTransaction()
                        .remove(instructionFragment)
                        .show(currentFragment)
                        .commit();

                currentFragment.onResume();
            }
        }
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

        if (currentFragment != null) {
            currentFragment.showBrightness(value);
        }
    }

    @Override
    public void showShadow(final double value) {

        if (currentFragment != null) {
            currentFragment.showShadow(value);
        }
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
    public void sendData(final byte[] data, long timeMillis, final FinderPatternInfo info) {

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

        if (currentFragment instanceof CameraStartTestFragment
                && previewFormat > 0 && previewWidth > 0 && previewHeight > 0
                && ((CameraStartTestFragment) currentFragment).dataSent()) {
            showResults();
        }
    }

    @Override
    public void playSound() {
        sound.playShortResource(R.raw.futurebeep2);
    }

    @Override
    public void showError(final String message) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                currentFragment.showError(message);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void timeOut(TestStatus status) {

        releaseResources();

        finderPatternIndicatorView.clearPatterns();
        finderPatternIndicatorView.invalidate();

        int title;
        switch (status) {
            case DETECTING_COLOR_CARD:
                title = R.string.color_card_not_found;
                break;
            case CHECKING_QUALITY:
                if (qualityCountMap.get("B") < 5) {
                    title = R.string.better_light_required;
                } else {
                    title = R.string.shadows_detected;
                }
                break;
            default:
                title = R.string.qualityCheckFailed;
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AlertDialog alertDialog = AlertUtil.showAlert(this, title,
                R.string.tryTestingInAWellLitArea, R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }, null, null);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
    }

    @Override
    public void showCountdownTimer(final int value, final double max) {

        // don't show progressbar if it is not showing and only a few seconds left
        if (timerCountdown.getVisibility() == View.INVISIBLE && value < 8) {
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                timerCountdown.setProgress(value, (int) max);

                if (value > 0) {
                    timerCountdown.setVisibility(View.VISIBLE);

                    // if long time left turn off the camera preview
                    if (value > LONG_TIME && !mCameraPaused) {
                        mCameraPaused = true;
                        if (cameraPreview != null) {
                            cameraPreview.setVisibility(View.INVISIBLE);
                        }
                        stopPreview();

                        (new Handler()).postDelayed(new Runnable() {
                            public void run() {
                                finderPatternIndicatorView.clearPatterns();
                                finderPatternIndicatorView.invalidate();
                            }
                        }, 100);

                        snackbar = Snackbar
                                .make(parentLayout, getString(R.string.you_can_set_phone_aside),
                                        Snackbar.LENGTH_INDEFINITE);

                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                        snackbar.setActionTextColor(typedValue.data);
                        View snackView = snackbar.getView();
                        TextView textView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
                        textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        textView.setTextColor(Color.WHITE);
                        snackbar.show();
                    }

                    // start the camera preview again in last few seconds
                    if (value <= Constant.GET_READY_SECONDS && mCameraPaused) {
                        mCameraPaused = false;
                        if (cameraPreview != null) {
                            finderPatternIndicatorView.setVisibility(View.VISIBLE);
                            cameraPreview.setVisibility(View.VISIBLE);
                        }
                        mCamera.startPreview();

                        playReadySound();

                        snackbar.dismiss();
                    }
                }

                if (timerCountdown.getAnimation() == null && value <= 3 && value > 0) {
                    AlphaAnimation animation = new AlphaAnimation(1f, 0);
                    animation.setDuration(PROGRESS_FADE_DURATION_MILLIS);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            timerCountdown.setAnimation(null);
                            timerCountdown.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    timerCountdown.startAnimation(animation);
                }
            }
        };
        handler.post(runnable);
    }

    private void playReadySound() {
        sound.playShortResource(R.raw.beep);
    }

    private void showResults() {
        Intent resultIntent = new Intent(getIntent());
        resultIntent.setClass(this, ResultActivity.class);
        resultIntent.putExtra(Constant.FORMAT, previewFormat);
        resultIntent.putExtra(Constant.WIDTH, previewWidth);
        resultIntent.putExtra(Constant.HEIGHT, previewHeight);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class MyHandler extends Handler {
    }

    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        @Nullable
        @Override
        protected Void doInBackground(Void... params) {
            try {
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.INFO);
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.DATA);
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.STRIP);
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.IMAGE_PATCH);
            } catch (IOException e) {
                showError(e.getMessage());
            }

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

            FinderPatternIndicatorView indicatorView = wrFinderPatternIndicatorView.get();
            if (indicatorView != null) {
                indicatorView.setColor(color);
                indicatorView.showPatterns(patterns, size);
            }
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
        @Nullable
        private float[] tilts;

        @Override
        public void run() {
            if (levelView != null) {
                levelView.setAngles(tilts);
            }
        }

        void setAngles(@Nullable float[] tiltValues) {
            this.tilts = tiltValues == null ? null : tiltValues.clone();
        }
    }
}
