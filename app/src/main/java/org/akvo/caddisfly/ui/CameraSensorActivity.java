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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ApiUtils;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.DataHelper;
import org.akvo.caddisfly.util.ImageUtils;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.akvo.caddisfly.util.ShakeDetector;
import org.akvo.caddisfly.util.SoundPoolPlayer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class CameraSensorActivity extends AppCompatActivity
        implements ResultFragment.ResultDialogListener, DilutionFragment.DilutionDialogListener,
        MessageFragment.MessageDialogListener, DialogGridError.ErrorListDialogListener {
    private final Handler delayHandler = new Handler();
    private DilutionFragment mDilutionFragment;
    private int mDilutionLevel = 0;
    private DialogGridError mResultFragment;
    //private TextView mTitleText;
    private TextView mTestTypeTextView;
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
    private Animation mSlideInRight;
    private Animation mSlideOutLeft;
    private CameraFragment mCameraFragment;
    private Runnable delayRunnable;
    private PowerManager.WakeLock wakeLock;
    private ArrayList<Result> mResults;
    //private ArrayList<Integer> mColors;
    //private ArrayList<Bitmap> mBitmaps;
    private boolean mTestCompleted;
    private boolean mHighLevelsFound;
    private boolean mIgnoreShake;

    @SuppressWarnings("SameParameterValue")
    private static void setAnimatorDisplayedChild(ViewAnimator viewAnimator, int whichChild) {
        Animation inAnimation = viewAnimator.getInAnimation();
        Animation outAnimation = viewAnimator.getOutAnimation();
        viewAnimator.setInAnimation(null);
        viewAnimator.setOutAnimation(null);
        viewAnimator.setDisplayedChild(whichChild);
        viewAnimator.setInAnimation(inAnimation);
        viewAnimator.setOutAnimation(outAnimation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiUtils.lockScreenOrientation(this);

        setContentView(R.layout.activity_camera_sensor);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_logo);

        sound = new SoundPoolPlayer(this);

        //mTitleText = (TextView) findViewById(R.id.titleText);
        mTestTypeTextView = (TextView) findViewById(R.id.testTypeTextView);
        mDilutionTextView = (TextView) findViewById(R.id.dilutionTextView);
        mDilutionTextView1 = (TextView) findViewById(R.id.dilution1TextView);

        mViewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);
        mSlideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        mSlideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        mViewAnimator.setInAnimation(mSlideInRight);
        mViewAnimator.setOutAnimation(mSlideOutLeft);

        //Set up the shake detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                if (!mIgnoreShake && !mWaitingForStillness && mCameraFragment != null) {
                    mWaitingForStillness = true;
                    mViewAnimator.showNext();
                    if (mCameraFragment != null) {
                        try {
                            mCameraFragment.stopCamera();
                            mCameraFragment.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        showError(getString(R.string.errorTestInterrupted), null);
                    }
                }
            }
        }, new ShakeDetector.OnNoShakeListener() {
            @Override
            public void onNoShake() {
                if (mWaitingForStillness) {
                    mWaitingForStillness = false;
                    sound.playShortResource(R.raw.beep);
                    dismissShakeAndStartTest();
                }
            }
        });
        mShakeDetector.minShakeAcceleration = 5;
        mShakeDetector.maxShakeDuration = 2000;
    }

    private void InitializeTest() {

        mIgnoreShake = PreferencesUtils.getBoolean(this, R.string.ignoreShakeKey, false);
        mTestCompleted = false;
        mHighLevelsFound = false;
        MainApp mainApp = (MainApp) getApplicationContext();
        Resources res = getResources();
        Configuration conf = res.getConfiguration();

        mTestTypeTextView.setText(mainApp.currentTestInfo.getName(conf.locale.getLanguage()));
        ((TextView) findViewById(R.id.testTitleTextView)).setText(mainApp.currentTestInfo.getName(conf.locale.getLanguage()));

        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }

        mSensorManager.unregisterListener(mShakeDetector);

        mWaitingForStillness = true;
        //mWaitingForShake = false;
        //mWaitingForFirstShake = false;
        setAnimatorDisplayedChild(mViewAnimator, 0);

        mViewAnimator.setInAnimation(null);
        mViewAnimator.setOutAnimation(null);
        mViewAnimator.setInAnimation(mSlideInRight);
        mViewAnimator.setOutAnimation(mSlideOutLeft);

        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                        SensorManager.SENSOR_DELAY_UI);
                mViewAnimator.showNext();
            }
        });

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
        sound.playShortResource(R.raw.err);

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
        mResultFragment = DialogGridError.newInstance(mResults, testFailed, result, color, isCalibration);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment prev = getFragmentManager().findFragmentByTag("gridDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        mResultFragment.setCancelable(false);
        mResultFragment.show(ft, "gridDialog");
    }

    @Override
    public void onAttachedToWindow() {

        // disable the key guard when device wakes up and shake alert is displayed
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainApp mainApp = (MainApp) getApplicationContext();
        //Configuration conf = getResources().getConfiguration();
        boolean isCalibration = getIntent().getBooleanExtra("isCalibration", false);

        if (mainApp.currentTestInfo.getCode().isEmpty()) {
            AlertUtils.showError(this, R.string.error, getString(R.string.errorLoadingTestTypes), null, R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }, null);

        } else if (!isCalibration && mainApp.currentTestInfo.getCode().equals("FLUOR")
                && !mTestCompleted) {
            mDilutionFragment = DilutionFragment.newInstance();
            final FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment prev = getFragmentManager().findFragmentByTag("dilutionFragment");
            if (prev != null) {
                ft.remove(prev);
            }
            mDilutionFragment.setCancelable(false);
            mDilutionFragment.show(ft, "dilutionFragment");

            mDilutionTextView.setVisibility(View.VISIBLE);
            mDilutionTextView1.setVisibility(View.VISIBLE);

        } else if (!mTestCompleted) {
            InitializeTest();
        }
    }

    private void getResult(byte[] bytes) {

        Bitmap bitmap = getBitmap(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] croppedData;

        croppedData = bos.toByteArray();

//        int sampleLength = PreferencesUtils.getInt(this, R.string.photoSampleDimensionKey,
//                Config.SAMPLE_CROP_LENGTH_DEFAULT);

        //ArrayList<ResultRange> ranges = ((MainApp) getApplicationContext()).currentTestInfo.getSwatches();

        Bitmap croppedBitmap = BitmapFactory.decodeByteArray(croppedData, 0, croppedData.length);
        Bundle bundle = ColorUtils.getPpmValue(croppedBitmap, ((MainApp) getApplicationContext()).currentTestInfo, Config.SAMPLE_CROP_LENGTH_DEFAULT);
        bitmap.recycle();

        double result = bundle.getDouble(Config.RESULT_VALUE_KEY, -1);
        int color = bundle.getInt(Config.RESULT_COLOR_KEY, 0);

        ArrayList<Pair<String, Double>> results = new ArrayList<>();
        results.add(new Pair<>("HSV 1 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_1", -1)));
        results.add(new Pair<>("HSV 2 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_2", -1)));
        results.add(new Pair<>("HSV 3 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_3", -1)));
        results.add(new Pair<>("HSV 5 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_5", -1)));

        results.add(new Pair<>("RGB 5 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY, -1)));

        Result resultInfo = new Result(result, color, croppedBitmap, results);

        //mColors.add(color);
        mResults.add(resultInfo);
        //mBitmaps.add(croppedBitmap);
        //boolean isCalibration = getIntent().getBooleanExtra("isCalibration", false);

//        if (mResults.size() > 3 && !isCalibration) {
//            if (DataHelper.getAverageResult(mResults) == -1) {
//                mCameraFragment.samplingCount--;
//            }
//        }
    }

    private void startTest() {
        mResults = new ArrayList<>();

        //mWaitingForShake = false;
        //mWaitingForFirstShake = false;
        mWaitingForStillness = false;

        mShakeDetector.minShakeAcceleration = 0.5;
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
                if (!mCameraFragment.hasTestCompleted()) {
                    mCameraFragment.pictureCallback = new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                            String message = getString(R.string.errorTestFailed);

                            getResult(data);

                            if (mCameraFragment.hasTestCompleted()) {

                                double result = DataHelper.getAverageResult(mResults);

                                MainApp mainApp = (MainApp) getApplicationContext();
                                if (result >= mainApp.currentTestInfo.getDilutionRequiredLevel() && mainApp.currentTestInfo.getCode().equals("FLUOR")) {
                                    mHighLevelsFound = true;
                                }

                                switch (mDilutionLevel) {
                                    case 1:
                                        result = result * 2;
                                        break;
                                    case 2:
                                        result = result * 4;
                                        break;
                                }

                                int color = DataHelper.getAverageColor(mResults);
                                boolean isCalibration = getIntent().getBooleanExtra("isCalibration", false);
                                releaseResources();
                                Intent intent = new Intent(getIntent());
                                intent.putExtra("result", result);
                                intent.putExtra("color", color);
                                //intent.putExtra("questionId", mQuestionId);
                                intent.putExtra("response", String.valueOf(result));
                                setResult(Activity.RESULT_OK, intent);
                                mTestCompleted = true;
                                boolean developerMode = PreferencesUtils.getBoolean(getBaseContext(), R.string.developerModeKey, false);

                                if (isCalibration && color != 0) {
                                    sound.playShortResource(R.raw.done);
                                    if (developerMode) {
                                        ShowVerboseError(false, result, color, true);
                                    } else {
                                        finish();
                                    }
                                } else {
                                    if (result < 0 || color == 0) {
                                        if (developerMode) {
                                            sound.playShortResource(R.raw.err);
                                            ShowVerboseError(true, 0, color, isCalibration);
                                        } else {
                                            showError(message, getBitmap(data));
                                        }
                                    } else {

                                        if (developerMode) {
                                            sound.playShortResource(R.raw.done);
                                            ShowVerboseError(false, result, color, false);
                                        } else {
                                            String title = mainApp.currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage());

                                            if (mHighLevelsFound && mDilutionLevel < 2) {
                                                sound.playShortResource(R.raw.beep_long);
                                                switch (mDilutionLevel) {
                                                    case 0:
                                                        message = getString(R.string.tryWith50PercentSample);
                                                        break;
                                                    case 1:
                                                        message = getString(R.string.tryWith25PercentSample);
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
                                                sound.playShortResource(R.raw.done);
                                                ResultFragment mResultFragment = ResultFragment.newInstance(title, result,
                                                        mDilutionLevel, mainApp.currentTestInfo.getUnit());
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
                                mCameraFragment.dismiss();
                            }
                        }
                    };

                    if (wakeLock == null || !wakeLock.isHeld()) {
                        PowerManager pm = (PowerManager) getApplicationContext()
                                .getSystemService(Context.POWER_SERVICE);
                        //noinspection deprecation
                        wakeLock = pm
                                .newWakeLock(PowerManager.FULL_WAKE_LOCK
                                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                        | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
                        wakeLock.acquire();
                    }

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

                    delayHandler.postDelayed(delayRunnable, Config.INITIAL_DELAY);
                }
            }
        }).execute();
    }

    private Bitmap getBitmap(byte[] bytes) {
        int sampleLength = Config.SAMPLE_CROP_LENGTH_DEFAULT;
        int[] pixels = new int[sampleLength * sampleLength];
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

//        File path = this.getExternalFilesDir(null);
//        if (!path.exists()) {
//            path.mkdirs();
//        }
        //ImageUtils.saveBitmap(bitmap, path.getAbsolutePath() + "/" + mResults.size() + ".jpg");

        bitmap.getPixels(pixels, 0, sampleLength,
                (bitmap.getWidth() - sampleLength) / 2,
                (bitmap.getHeight() - sampleLength) / 2,
                sampleLength,
                sampleLength);
        bitmap = Bitmap.createBitmap(pixels, 0, sampleLength,
                sampleLength,
                sampleLength,
                Bitmap.Config.ARGB_8888);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                            croppedData = bos.toByteArray();

        bitmap = ImageUtils.getRoundedShape(bitmap, sampleLength);
        bitmap.setHasAlpha(true);
        return bitmap;
    }


    @Override
    public void onFinishDialog(Bundle bundle) {
        finish();
    }

    private void releaseResources() {
        mSensorManager.unregisterListener(mShakeDetector);

        delayHandler.removeCallbacks(delayRunnable);
        if (mCameraFragment != null) {
            mCameraFragment.stopCamera();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onBackPressed() {
        if (mWaitingForStillness) {
            releaseResources();
            Intent intent = new Intent(getIntent());
            this.setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
    }

    @Override
    public void onFinishDilutionDialog(int index) {
        if (index == -1) {
            releaseResources();
            Intent intent = new Intent(getIntent());
            this.setResult(Activity.RESULT_CANCELED, intent);
            finish();
        } else {
            mDilutionLevel = index;
            mDilutionFragment.dismiss();

            switch (mDilutionLevel) {
                case 0:
                    mDilutionTextView.setText(R.string.hundredPercentSampleWater);
                    mDilutionTextView1.setText(R.string.hundredPercentSampleWater);
                    break;
                case 1:
                    mDilutionTextView.setText(R.string.fiftyPercentSampleWater);
                    mDilutionTextView1.setText(R.string.fiftyPercentSampleWater);
                    break;
                case 2:
                    mDilutionTextView.setText(R.string.twentyFivePercentSampleWater);
                    mDilutionTextView1.setText(R.string.twentyFivePercentSampleWater);
                    break;
            }
            InitializeTest();
        }
    }

    @Override
    public void onFinishErrorListDialog(boolean retry, boolean cancelled, boolean isCalibration) {
        mResultFragment.dismiss();
        if (mHighLevelsFound && !isCalibration) {
            mCameraFragment.dismiss();
            sound.playShortResource(R.raw.beep_long);
            MainApp mainApp = (MainApp) getApplicationContext();
            String title = mainApp.currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage());

            String message = "";
            switch (mDilutionLevel) {
                case 0:
                    message = getString(R.string.tryWith50PercentSample);
                    break;
                case 1:
                    message = getString(R.string.tryWith25PercentSample);
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