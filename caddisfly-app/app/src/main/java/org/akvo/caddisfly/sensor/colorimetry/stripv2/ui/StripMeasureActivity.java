package org.akvo.caddisfly.sensor.colorimetry.stripv2.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.camera.CameraOperationsManager;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.camera.CameraPreview;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.Constants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.MessageUtils;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.widget.FinderPatternIndicatorView;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.widget.TimerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by markwestra on 19/07/2017
 */
@SuppressWarnings("deprecation")
public class StripMeasureActivity extends BaseActivity implements StripMeasureListener {

    public static final boolean DEBUG = false;
    // a handler to handle the state machine of the preview, capture, decode, fullCapture cycle
    public static StriptestHandler mStriptestHandler;
    private FinderPatternIndicatorView mFinderPatternIndicatorView;
    private TimerView mTimerCountdown;
    @Nullable
    private WeakReference<Camera> wrCamera;
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private FrameLayout previewLayout;
    private SoundPoolPlayer sound;
    private WeakReference<StripMeasureActivity> mActivity;
    //    private int previewFormat = -1;
//    private int previewWidth = 0;
//    private int previewHeight = 0;
    private String uuid;
    private StripMeasureFragment stripMeasureFragment;
    private InstructionFragment instructionsFragment;
    private List<StripTest.Brand.Patch> patches;
    // CameraOperationsManager wraps the camera API
    private CameraOperationsManager mCameraOpsManager;

    public CameraOperationsManager getCameraOpsManager() {
        return mCameraOpsManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sound = new SoundPoolPlayer(this);

        setContentView(R.layout.v2activity_strip_measure);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mFinderPatternIndicatorView =
                findViewById(R.id.finder_indicator);

        mTimerCountdown = findViewById(R.id.countdownTimer);

        mActivity = new WeakReference<>(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        uuid = getIntent().getStringExtra(Constants.UUID);

        StripTest stripTest = new StripTest();
        if (uuid != null && stripTest.getBrand(uuid) != null) {
            setTitle(stripTest.getBrand(uuid).getName());
            patches = stripTest.getBrand(uuid).getPatches();
        } else {
            finish();
        }

        if (mCameraOpsManager == null) {
            mCameraOpsManager = new CameraOperationsManager();
        }

        // create striptestHandler
        // as this handler is created on the current thread, it is part of the UI thread.
        // So we don't want to do actual work on it - just coordinate.
        // The camera and the decoder get their own thread.
        if (mStriptestHandler == null) {
            mStriptestHandler = new StriptestHandler(this, getApplicationContext(),
                    mCameraOpsManager, mFinderPatternIndicatorView, stripTest.getBrand(uuid));
        }

        mCameraOpsManager.setStriptestHandler(mStriptestHandler);

        mStriptestHandler.setStatus(StriptestHandler.State.PREPARE);

        // we use a set to remove duplicates
        Set<Integer> timeLapseSet = new HashSet<>();
        double currentPatch = -1;
        for (int i = 0; i < patches.size(); i++) {
            double nextPatch = patches.get(i).getTimeLapse();
            if (nextPatch - currentPatch > 0.001) {
                timeLapseSet.add((int) Math.round(nextPatch));
                currentPatch = nextPatch;
            }
        }
        List<Integer> timeLapses = new ArrayList<>();
        timeLapses.addAll(timeLapseSet);
        Collections.sort(timeLapses);
        mStriptestHandler.setTestData(timeLapses);

        // initialize camera and start camera preview
        startCameraPreview();
    }

    private void startCameraPreview() {
        previewLayout = findViewById(R.id.camera_preview);
        mCameraPreview = mCameraOpsManager.initCamera(this);
        mCamera = mCameraPreview.getCamera();
        if (mCamera == null) {
            Toast.makeText(this.getApplicationContext(), "Could not instantiate the camera",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            try {
                wrCamera = new WeakReference<>(mCamera);
                previewLayout.removeAllViews();
                if (mCameraPreview != null) {
                    previewLayout.addView(mCameraPreview);
                } else {
                    finish();
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    // started from within camera preview
    public void initPreviewFragment() {
        try {
            if (stripMeasureFragment == null) {
                stripMeasureFragment = StripMeasureFragment.newInstance(mStriptestHandler);
                mStriptestHandler.setFragment(stripMeasureFragment);
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.layout_cameraPlaceholder, stripMeasureFragment
                ).commit();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void moveToInstructions() {
        instructionsFragment = InstructionFragment.newInstance(uuid);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_instructionLayout, instructionsFragment)
                .commit();
    }

    @Override
    public void moveToStripMeasurement() {
        getSupportFragmentManager().beginTransaction()
                .remove(instructionsFragment)
                .commit();

        if (stripMeasureFragment == null) {
            stripMeasureFragment = StripMeasureFragment.newInstance(mStriptestHandler);
        }

        getSupportFragmentManager().beginTransaction().replace(
                R.id.layout_cameraPlaceholder, stripMeasureFragment).commit();
        mStriptestHandler.setStatus(StriptestHandler.State.MEASURE);
        stripMeasureFragment.clearProgress();
        stripMeasureFragment.setMeasureText();

        // hand over to state machine
        MessageUtils.sendMessage(mStriptestHandler, StriptestHandler.START_PREVIEW_MESSAGE, 0);
    }

    @Override
    public void moveToResults() {
        // move to results activity
        Intent resultIntent = new Intent(getIntent());
        resultIntent.setClass(this, ResultActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(resultIntent);
        ResultActivity.setDecodeData(StriptestHandler.getDecodeData());
    }

    @Override
    public void showTimer() {
        if (mTimerCountdown.getVisibility() != View.VISIBLE)
            mTimerCountdown.setVisibility(View.VISIBLE);
    }

    @Override
    public void playSound() {
        sound.playShortResource(R.raw.futurebeep2);
    }

    @Override
    public void updateTimer(int value) {
        mTimerCountdown.setProgress(value, 60);
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
        if (mCamera != null) {
            mCameraOpsManager.stopAutoFocus();
            mCameraOpsManager.stopCamera();
            mCamera.setOneShotPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (mStriptestHandler != null) {
            mStriptestHandler.quitSynchronously();
            mStriptestHandler = null;
        }

        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
        }
        if (wrCamera != null) {
            wrCamera.clear();
            wrCamera = null;
        }

        if (mCameraPreview != null && previewLayout != null) {
            previewLayout.removeView(mCameraPreview);
            mCameraPreview = null;
        }
    }

    /**
     * Store previewLayout info in global properties for later use.
     */
    public void setPreviewProperties(int w, int h, int previewImageWidth, int previewImageHeight) {
        if (mCamera != null && mCameraPreview != null) {
            StriptestHandler.mDecodeData.setPreviewWidth(w);
            StriptestHandler.mDecodeData.setPreviewHeight(h);
            StriptestHandler.mDecodeData.setDecodeWidth(previewImageWidth);
            StriptestHandler.mDecodeData.setDecodeHeight(previewImageHeight);

            mFinderPatternIndicatorView.setMeasure(w, (int) Math.round(w * Constants.CROP_FINDER_PATTERN_FACTOR));
        }
    }
}
