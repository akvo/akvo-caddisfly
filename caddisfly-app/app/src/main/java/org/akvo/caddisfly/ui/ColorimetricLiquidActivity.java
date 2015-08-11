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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.AppPreferences;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultInfo;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.DataHelper;
import org.akvo.caddisfly.util.ImageUtils;
import org.akvo.caddisfly.util.ShakeDetector;
import org.akvo.caddisfly.util.SoundPoolPlayer;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class ColorimetricLiquidActivity extends BaseActivity
        implements ResultFragment.ResultDialogListener,
        MessageFragment.MessageDialogListener, DiagnosticResultDialog.ErrorListDialogListener {
    private final Handler delayHandler = new Handler();
    private boolean mIsCalibration;
    private int mDilutionLevel = 0;
    private DiagnosticResultDialog mResultFragment;
    private TextView mDilutionTextView;
    private TextView mDilutionTextView1;
    private SoundPoolPlayer sound;
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    //private boolean mWaitingForFirstShake;
    //private boolean mWaitingForShake = true;
    private boolean mWaitingForStillness = false;
    private ViewAnimator mViewAnimator;
    private DialogFragment mCameraFragment;
    private Runnable delayRunnable;
    private PowerManager.WakeLock wakeLock;
    private ArrayList<Result> mResults;
    private boolean mTestCompleted;
    private boolean mHighLevelsFound;
    private boolean mIgnoreShake;

//    @SuppressWarnings("SameParameterValue")
//    private static void setAnimatorDisplayedChild(ViewAnimator viewAnimator, int whichChild) {
//        Animation inAnimation = viewAnimator.getInAnimation();
//        Animation outAnimation = viewAnimator.getOutAnimation();
//        viewAnimator.setInAnimation(null);
//        viewAnimator.setOutAnimation(null);
//        viewAnimator.setDisplayedChild(whichChild);
//        viewAnimator.setInAnimation(inAnimation);
//        viewAnimator.setOutAnimation(outAnimation);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_sensor);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            mIsCalibration = getIntent().getBooleanExtra("isCalibration", false);
            double rangeValue = getIntent().getDoubleExtra("rangeValue", 0);
            if (mIsCalibration) {
                getSupportActionBar().setDisplayUseLogoEnabled(false);
                getSupportActionBar().setTitle(String.format("%s %.2f %s",
                        getResources().getString(R.string.calibrate),
                        rangeValue, CaddisflyApp.getApp().currentTestInfo.getUnit()));
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            }
        }

        sound = new SoundPoolPlayer(this);

        mDilutionTextView = (TextView) findViewById(R.id.textDilution);
        mDilutionTextView1 = (TextView) findViewById(R.id.textDilution2);

        if (!mIsCalibration && CaddisflyApp.getApp().currentTestInfo.hasDilution()) {
            mDilutionTextView.setVisibility(View.VISIBLE);
            mDilutionTextView1.setVisibility(View.VISIBLE);
        } else {
            mDilutionTextView.setVisibility(View.GONE);
            mDilutionTextView1.setVisibility(View.GONE);
        }

        mViewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);

        //Set up the shake detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                if ((mIgnoreShake) || mWaitingForStillness || mCameraFragment == null
                        || isDestroyed()) {
                    return;
                }
                mWaitingForStillness = true;
                mViewAnimator.showNext();
                if (mCameraFragment != null) {
                    try {
                        ((CameraFragment) mCameraFragment).stopCamera();
                        mCameraFragment.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showError(getString(R.string.errorTestInterrupted), null);
                }
            }
        }, new ShakeDetector.OnNoShakeListener() {
            @Override
            public void onNoShake() {
                if (mWaitingForStillness) {
                    mWaitingForStillness = false;
                    dismissShakeAndStartTest();
                }
            }
        });
        mShakeDetector.minShakeAcceleration = 5;
        mShakeDetector.maxShakeDuration = 2000;

        //setTitle(R.string.selectDilution);

        Button noDilutionButton = (Button) findViewById(R.id.buttonNoDilution);
        Button percentButton1 = (Button) findViewById(R.id.buttonDilution1);
        Button percentButton2 = (Button) findViewById(R.id.buttonDilution2);

        // todo: remove hardcoding of dilution times
        percentButton1.setText(String.format(getString(R.string.timesDilution), 2));
        percentButton2.setText(String.format(getString(R.string.timesDilution), 5));

        noDilutionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDilutionLevel = 0;
                mDilutionTextView.setText(R.string.noDilution);
                mDilutionTextView1.setText(R.string.noDilution);
                mViewAnimator.showNext();
            }
        });

        percentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDilutionLevel = 1;
                String dilutionLabel = String.format(getString(R.string.timesDilution), 2);
                mDilutionTextView.setText(dilutionLabel);
                mDilutionTextView1.setText(dilutionLabel);
                mViewAnimator.showNext();
            }
        });

        percentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDilutionLevel = 2;
                String dilutionLabel = String.format(getString(R.string.timesDilution), 5);
                mDilutionTextView.setText(dilutionLabel);
                mDilutionTextView1.setText(dilutionLabel);
                mViewAnimator.showNext();
            }
        });


        findViewById(R.id.buttonStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mViewAnimator.showNext();

                acquireWakeLock();

                // disable the key guard when device wakes up and shake alert is displayed
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                );


                InitializeTest();

                mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                        SensorManager.SENSOR_DELAY_UI);
            }
        });
    }

    private void InitializeTest() {

        mSensorManager.unregisterListener(mShakeDetector);

        mIgnoreShake = AppPreferences.getIgnoreShake(this);
        mTestCompleted = false;
        mHighLevelsFound = false;

        mWaitingForStillness = true;
        //mWaitingForShake = false;
        //mWaitingForFirstShake = false;
        //setAnimatorDisplayedChild(mViewAnimator, 0);


    }

    private void acquireWakeLock() {
        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "CameraSensorWakeLock");
            wakeLock.acquire();
        }
    }

    /**
     * Start the test by displaying the progress bar
     */
    private void dismissShakeAndStartTest() {
        /*if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }*/

        mSensorManager.unregisterListener(mShakeDetector);

        startTest();
    }

    private void showError(String message, final Bitmap bitmap) {
        releaseResources();
        sound.playShortResource(this, R.raw.err);

        AlertUtils.showError(this, R.string.error, message, bitmap, R.string.retry,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCameraFragment.dismiss();
                        InitializeTest();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getIntent());
                        setResult(Activity.RESULT_CANCELED, intent);
                        releaseResources();
                        finish();
                    }
                }
        );
    }

    private void ShowVerboseError(boolean testFailed, double result, int color, boolean isCalibration) {
        mResultFragment = DiagnosticResultDialog.newInstance(mResults, testFailed, result, color, isCalibration);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment prev = getFragmentManager().findFragmentByTag("gridDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        mResultFragment.setCancelable(false);
        mResultFragment.show(ft, "gridDialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsCalibration = getIntent().getBooleanExtra("isCalibration", false);
        double rangeValue = getIntent().getDoubleExtra("rangeValue", 0);

        TextView ppmTextView = ((TextView) findViewById(R.id.ppmTextView));
        if (mIsCalibration) {
            ppmTextView.setText(String.format("%.2f %s", rangeValue, CaddisflyApp.getApp().currentTestInfo.getUnit()));
            ppmTextView.setVisibility(View.VISIBLE);
        } else {
            ppmTextView.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.appName);
            }

//            if (getSupportActionBar() != null) {
//                getSupportActionBar().hide();
//            }
            // disable the key guard when device wakes up and shake alert is displayed
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        Resources res = getResources();
        Configuration conf = res.getConfiguration();

        ((TextView) findViewById(R.id.textTitle)).setText(CaddisflyApp.getApp().currentTestInfo.getName(conf.locale.getLanguage()));
        ((TextView) findViewById(R.id.textTitle2)).setText(CaddisflyApp.getApp().currentTestInfo.getName(conf.locale.getLanguage()));
        ((TextView) findViewById(R.id.textTitle3)).setText(CaddisflyApp.getApp().currentTestInfo.getName(conf.locale.getLanguage()));

        if (CaddisflyApp.getApp().currentTestInfo.getCode().isEmpty()) {
            alertCouldNotLoadConfig();
        } else if (mIsCalibration || !CaddisflyApp.getApp().currentTestInfo.hasDilution()) {
            //setAnimatorDisplayedChild(mViewAnimator, 0);
            mViewAnimator.showNext();
        } else if (!mTestCompleted) {
            InitializeTest();
        }
    }

    private void alertCouldNotLoadConfig() {
        String message = String.format("%s\r\n\r\n%s",
                getString(R.string.errorLoadingConfiguration),
                getString(R.string.pleaseContactSupport));
        AlertUtils.showError(this, R.string.error, message, null, R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null);
    }

    private void getResult(Bitmap bitmap) {

        int maxDistance = AppPreferences.getColorDistanceTolerance(this);

        ColorInfo photoColor = ColorUtils.getColorFromBitmap(bitmap, AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        TestInfo testInfo = CaddisflyApp.getApp().currentTestInfo;

        ArrayList<ResultInfo> results = new ArrayList<>();

        ResultInfo resultInfo;
        if (AppPreferences.isDiagnosticMode(this)) {
            ArrayList<Swatch> tempColorRange = new ArrayList<>();
            tempColorRange.add(testInfo.getRanges().get(0));
            tempColorRange.add(testInfo.getRanges().get(testInfo.getRanges().size() - 1));

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    tempColorRange,
                    maxDistance,
                    AppConfig.ColorModel.LAB);

            results.add(resultInfo);

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    tempColorRange,
                    maxDistance,
                    AppConfig.ColorModel.RGB);

            results.add(resultInfo);

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    tempColorRange,
                    maxDistance,
                    AppConfig.ColorModel.HSV);

            results.add(resultInfo);

            tempColorRange.add(1, testInfo.getRanges().get((testInfo.getRanges().size() / 2) - 1));

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    tempColorRange,
                    maxDistance,
                    AppConfig.ColorModel.LAB);

            results.add(resultInfo);

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    tempColorRange,
                    maxDistance,
                    AppConfig.ColorModel.RGB);

            results.add(resultInfo);

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    tempColorRange,
                    maxDistance,
                    AppConfig.ColorModel.HSV);

            results.add(resultInfo);

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    CaddisflyApp.getApp().currentTestInfo.getRanges(),
                    maxDistance,
                    AppConfig.ColorModel.RGB);

            results.add(resultInfo);

            resultInfo = ColorUtils.analyzeColor(photoColor,
                    CaddisflyApp.getApp().currentTestInfo.getRanges(),
                    maxDistance,
                    AppConfig.ColorModel.HSV);

            results.add(resultInfo);
        }
        resultInfo = ColorUtils.analyzeColor(photoColor,
                CaddisflyApp.getApp().currentTestInfo.getRanges(),
                maxDistance,
                AppConfig.ColorModel.LAB);

        results.add(0, resultInfo);

        Result result = new Result(bitmap, results);

        mResults.add(result);
    }

    private void startTest() {

        mResults = new ArrayList<>();

        //mWaitingForShake = false;
        //mWaitingForFirstShake = false;
        mWaitingForStillness = false;

        sound.playShortResource(getBaseContext(), R.raw.beep);
        mShakeDetector.minShakeAcceleration = 1;
        mShakeDetector.maxShakeDuration = 3000;
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                mCameraFragment = CameraFragment.newInstance();
                if (!((CameraFragment) mCameraFragment).hasTestCompleted()) {
                    ((CameraFragment) mCameraFragment).pictureCallback = new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                            Bitmap bitmap = ImageUtils.getBitmap(data);
                            Bitmap croppedBitmap = ImageUtils.getCroppedBitmap(bitmap, AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                            getResult(croppedBitmap);

                            if (((CameraFragment) mCameraFragment).hasTestCompleted()) {

                                AnalyzeResult(data);
                                mCameraFragment.dismiss();
                            }
                        }
                    };

                    acquireWakeLock();

                    delayRunnable = new Runnable() {
                        @Override
                        public void run() {
                            final FragmentTransaction ft = getFragmentManager().beginTransaction();
                            Fragment prev = getFragmentManager().findFragmentByTag("cameraDialog");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);
                            try {
                                mCameraFragment.show(ft, "cameraDialog");
                            } catch (Exception e) {
                                e.printStackTrace();
                                finish();
                            }
                        }
                    };

                    delayHandler.postDelayed(delayRunnable, AppConfig.DELAY_BETWEEN_SAMPLING);
                }
            }
        }).execute();
    }

    private void AnalyzeResult(byte[] data) {

        releaseResources();

        String message = getString(R.string.errorTestFailed);
        double result = DataHelper.getAverageResult(mResults, AppPreferences.getSamplingTimes(this));

        if (mDilutionLevel < 2 && result >= CaddisflyApp.getApp().currentTestInfo.getDilutionRequiredLevel() &&
                CaddisflyApp.getApp().currentTestInfo.hasDilution()) {
            mHighLevelsFound = true;
        }

        //todo: remove hard coding of dilution levels
        switch (mDilutionLevel) {
            case 1:
                result = result * 2;
                break;
            case 2:
                result = result * 5;
                break;
        }

        int color = DataHelper.getAverageColor(mResults, AppPreferences.getSamplingTimes(this));
        boolean isCalibration = getIntent().getBooleanExtra("isCalibration", false);
        Intent intent = new Intent(getIntent());
        intent.putExtra("result", result);
        intent.putExtra("color", color);
        //intent.putExtra("questionId", mQuestionId);
        intent.putExtra("response", String.format("%.2f", result));
        setResult(Activity.RESULT_OK, intent);
        mTestCompleted = true;

        if (isCalibration && color != 0) {
            sound.playShortResource(this, R.raw.done);
            if (AppPreferences.isDiagnosticMode(getBaseContext())) {
                ShowVerboseError(false, result, color, true);
            } else {
                finish();
            }
        } else {
            if (result < 0 || color == 0) {
                if (AppPreferences.isDiagnosticMode(getBaseContext())) {
                    sound.playShortResource(this, R.raw.err);
                    ShowVerboseError(true, 0, color, isCalibration);
                } else {
                    showError(message, ImageUtils.getBitmap(data));
                }
            } else {

                if (AppPreferences.isDiagnosticMode(getBaseContext())) {
                    sound.playShortResource(this, R.raw.done);
                    ShowVerboseError(false, result, color, false);
                } else {
                    String title = CaddisflyApp.getApp().currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage());

                    if (mHighLevelsFound && mDilutionLevel < 2) {
                        sound.playShortResource(this, R.raw.beep_long);
                        //todo: remove hard coding of dilution levels
                        switch (mDilutionLevel) {
                            case 0:
                                message = String.format(getString(R.string.tryWithDilutedSample), 2);
                                break;
                            case 1:
                                message = String.format(getString(R.string.tryWithDilutedSample), 5);
                                break;
                        }

                        MessageFragment mMessageFragment = MessageFragment.newInstance(title, message, mDilutionLevel);
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();

                        Fragment prev = getFragmentManager().findFragmentByTag("resultDialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        mMessageFragment.setCancelable(false);
                        mMessageFragment.show(ft, "resultDialog");

                    } else {
                        sound.playShortResource(this, R.raw.done);
                        ResultFragment mResultFragment = ResultFragment.newInstance(title, result,
                                mDilutionLevel, CaddisflyApp.getApp().currentTestInfo.getUnit());
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();

                        Fragment prev = getFragmentManager().findFragmentByTag("resultDialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        mResultFragment.setCancelable(false);
                        mResultFragment.show(ft, "resultDialog");
                    }
                }
            }
        }
    }

    @Override
    public void onSuccessFinishDialog() {
        finish();
    }

    private void releaseResources() {
        mSensorManager.unregisterListener(mShakeDetector);

        delayHandler.removeCallbacks(delayRunnable);
        if (mCameraFragment != null && mCameraFragment instanceof CameraFragment) {
            ((CameraFragment) mCameraFragment).stopCamera();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onBackPressed() {
        //if (mWaitingForStillness) {
        releaseResources();
        Intent intent = new Intent(getIntent());
        this.setResult(Activity.RESULT_CANCELED, intent);
        finish();
        //}
    }

    @Override
    public void onFinishErrorListDialog(boolean retry, boolean cancelled, boolean isCalibration) {
        mResultFragment.dismiss();
        if (mHighLevelsFound && !isCalibration) {
            mCameraFragment.dismiss();
            sound.playShortResource(this, R.raw.beep_long);
            String title = CaddisflyApp.getApp().currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage());

            //todo: remove hard coding of dilution levels
            String message = "";
            switch (mDilutionLevel) {
                case 0:
                    message = String.format(getString(R.string.tryWithDilutedSample), 2);
                    break;
                case 1:
                    message = String.format(getString(R.string.tryWithDilutedSample), 5);
                    break;
            }

            MessageFragment mMessageFragment = MessageFragment.newInstance(title, message, mDilutionLevel);
            final FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment prev = getFragmentManager().findFragmentByTag("resultDialog");
            if (prev != null) {
                ft.remove(prev);
            }
            mMessageFragment.setCancelable(false);
            mMessageFragment.show(ft, "resultDialog");

        } else if (retry) {
            mCameraFragment.dismiss();
            InitializeTest();
        } else {
            if (cancelled) {
                Intent intent = new Intent(getIntent());
                this.setResult(Activity.RESULT_CANCELED, intent);
            }
            releaseResources();
            finish();
        }
    }

    @Override
    public void onFinishDialog() {
        Intent intent = new Intent(getIntent());
        intent.putExtra("response", String.valueOf(""));
        this.setResult(Activity.RESULT_OK, intent);
        releaseResources();
        finish();
    }
}