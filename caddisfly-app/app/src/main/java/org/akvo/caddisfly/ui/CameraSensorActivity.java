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
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ApiUtils;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.DataHelper;
import org.akvo.caddisfly.util.ImageUtils;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.akvo.caddisfly.util.ShakeDetector;
import org.akvo.caddisfly.util.SoundPoolPlayer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraSensorActivity extends AppCompatActivity
        implements ResultFragment.ResultDialogListener, DilutionFragment.DilutionDialogListener,
        MessageFragment.MessageDialogListener, DialogGridError.ErrorListDialogListener {
    private final Handler delayHandler = new Handler();
    private boolean mIsCalibration;
    private DilutionFragment mDilutionFragment;
    private int mDilutionLevel = 0;
    private DialogGridError mResultFragment;
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
    //private ArrayList<Integer> mColors;
    //private ArrayList<Bitmap> mBitmaps;
    private boolean mTestCompleted;
    private boolean mHighLevelsFound;
    private boolean mIgnoreShake;
    private USBMonitor mUSBMonitor;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.calibrate));
        }

        sound = new SoundPoolPlayer(this);

        mDilutionTextView = (TextView) findViewById(R.id.dilutionTextView);
        mDilutionTextView1 = (TextView) findViewById(R.id.dilution1TextView);

        mDilutionTextView.setVisibility(View.GONE);
        mDilutionTextView1.setVisibility(View.GONE);

        mViewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);
        //Animation mSlideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        //Animation mSlideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        //mViewAnimator.setInAnimation(mSlideInRight);
        //mViewAnimator.setOutAnimation(mSlideOutLeft);

        //Set up the shake detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final boolean diagnosticMode = PreferencesUtils.getBoolean(this, R.string.diagnosticModeKey, false);

        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                if ((diagnosticMode && mIgnoreShake) || mWaitingForStillness || mCameraFragment == null) {
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

        mUSBMonitor = new USBMonitor(this, null);

    }

    private void InitializeTest() {

        mSensorManager.unregisterListener(mShakeDetector);

        mIgnoreShake = PreferencesUtils.getBoolean(this, R.string.ignoreShakeKey, false);
        mTestCompleted = false;
        mHighLevelsFound = false;

        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }

        mWaitingForStillness = true;
        //mWaitingForShake = false;
        //mWaitingForFirstShake = false;
        setAnimatorDisplayedChild(mViewAnimator, 0);

        //mViewAnimator.setInAnimation(null);
        //mViewAnimator.setOutAnimation(null);
        //mViewAnimator.setInAnimation(mSlideInRight);
        //mViewAnimator.setOutAnimation(mSlideOutLeft);

        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewAnimator.showNext();

                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                // disable the key guard when device wakes up and shake alert is displayed
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                );

                final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getBaseContext(), R.xml.camera_device_filter);
                List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
                if (usbDeviceList.size() > 0) {
                    startExternalTest();
                } else {
                    mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                            SensorManager.SENSOR_DELAY_UI);
                }
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

        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.camera_device_filter);
        List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
        if (usbDeviceList.size() > 0) {
            startExternalTest();
        } else {
            startTest();
        }
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
    protected void onStart() {
        super.onStart();
        MainApp mainApp = (MainApp) getApplicationContext();
        mIsCalibration = getIntent().getBooleanExtra("isCalibration", false);
        double rangeValue = getIntent().getDoubleExtra("rangeValue", 0);

        TextView ppmTextView = ((TextView) findViewById(R.id.ppmTextView));
        if (mIsCalibration) {
            ppmTextView.setText(String.format("%.2f %s", rangeValue, mainApp.currentTestInfo.getUnit()));
            ppmTextView.setVisibility(View.VISIBLE);
        } else {
            ppmTextView.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            // disable the key guard when device wakes up and shake alert is displayed
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
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

        ((TextView) findViewById(R.id.testTitleTextView)).setText(mainApp.currentTestInfo.getName(conf.locale.getLanguage()));
        ((TextView) findViewById(R.id.testTypeTextView)).setText(mainApp.currentTestInfo.getName(conf.locale.getLanguage()));

        if (mainApp.currentTestInfo.getCode().isEmpty()) {
            AlertUtils.showError(this, R.string.error, getString(R.string.errorLoadingTestTypes), null, R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }, null);

        } else if (!mIsCalibration && mainApp.currentTestInfo.hasDilution()
                && !mTestCompleted) {
            mDilutionFragment = DilutionFragment.newInstance();
            final FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment prev = getFragmentManager().findFragmentByTag("dilutionFragment");
            if (prev != null) {
                ft.remove(prev);
            }
            mDilutionFragment.setCancelable(false);
            mDilutionFragment.show(ft, "dilutionFragment");

        } else if (!mTestCompleted) {
            InitializeTest();
        }
    }

    private void getResult(Bitmap bitmap) {

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
//        byte[] croppedData;
//
//        croppedData = bos.toByteArray();
//
//        Bitmap croppedBitmap = BitmapFactory.decodeByteArray(croppedData, 0, croppedData.length);

        Bundle bundle = ColorUtils.getPpmValue(bitmap,
                ((MainApp) getApplicationContext()).currentTestInfo, Config.SAMPLE_CROP_LENGTH_DEFAULT);
//        bitmap.recycle();

        double result = bundle.getDouble(Config.RESULT_VALUE_KEY, -1);
        int color = bundle.getInt(Config.RESULT_COLOR_KEY, 0);

        ArrayList<Pair<String, Double>> results = new ArrayList<>();
        results.add(new Pair<>("HSV 1 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_1", -1)));
        results.add(new Pair<>("HSV 2 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_2", -1)));
        results.add(new Pair<>("HSV 3 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_3", -1)));
        results.add(new Pair<>("HSV 5 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY + "_5", -1)));

        results.add(new Pair<>("RGB 5 Calibration", bundle.getDouble(Config.RESULT_VALUE_KEY, -1)));

        Result resultInfo = new Result(result, color, bitmap, results);

        mResults.add(resultInfo);
    }

    private void startExternalTest() {
        mResults = new ArrayList<>();
        sound.playShortResource(this, R.raw.beep);
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                mCameraFragment = ExternalCameraFragment.newInstance();
                if (!((ExternalCameraFragment) mCameraFragment).hasTestCompleted()) {

                    ((ExternalCameraFragment) mCameraFragment).pictureCallback = new ExternalCameraFragment.PictureCallback() {
                        @Override
                        public void onPictureTaken(Bitmap bitmap) {

                            Bitmap croppedBitmap = ImageUtils.getCroppedBitmap(bitmap, Config.SAMPLE_CROP_LENGTH_DEFAULT);

                            getResult(croppedBitmap);
                            //bitmap.recycle();

                            if (((ExternalCameraFragment) mCameraFragment).hasTestCompleted()) {
                                ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
                                croppedBitmap.copyPixelsToBuffer(buffer);
                                byte[] data = buffer.array();
                                AnalyzeResult(data);
                                mCameraFragment.dismiss();
                            }
                        }
                    };
                }

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
                        Fragment prev = getFragmentManager().findFragmentByTag("externalCameraDialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);
                        try {
                            mCameraFragment.show(ft, "externalCameraDialog");
                        } catch (Exception e) {
                            e.printStackTrace();
                            finish();
                        }
                    }
                };

                delayHandler.postDelayed(delayRunnable, 0);
            }
        }).execute();


    }

    private void startTest() {

        mResults = new ArrayList<>();

//        try {
//            Thread.sleep(Config.INITIAL_DELAY, 0);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

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
                            Bitmap croppedBitmap = ImageUtils.getCroppedBitmap(bitmap, Config.SAMPLE_CROP_LENGTH_DEFAULT);

                            getResult(croppedBitmap);

                            if (((CameraFragment) mCameraFragment).hasTestCompleted()) {

                                AnalyzeResult(data);
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

    private void AnalyzeResult(byte[] data) {
        String message = getString(R.string.errorTestFailed);
        double result = DataHelper.getAverageResult(mResults);

        MainApp mainApp = (MainApp) getApplicationContext();
        if (result >= mainApp.currentTestInfo.getDilutionRequiredLevel() && mainApp.currentTestInfo.hasDilution()) {
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

        int color = DataHelper.getAverageColor(mResults);
        boolean isCalibration = getIntent().getBooleanExtra("isCalibration", false);
        releaseResources();
        Intent intent = new Intent(getIntent());
        intent.putExtra("result", result);
        intent.putExtra("color", color);
        //intent.putExtra("questionId", mQuestionId);
        intent.putExtra("response", String.format("%.2f", result));
        setResult(Activity.RESULT_OK, intent);
        mTestCompleted = true;
        boolean diagnosticMode = PreferencesUtils.getBoolean(getBaseContext(), R.string.diagnosticModeKey, false);

        if (isCalibration && color != 0) {
            sound.playShortResource(this, R.raw.done);
            if (diagnosticMode) {
                ShowVerboseError(false, result, color, true);
            } else {
                finish();
            }
        } else {
            if (result < 0 || color == 0) {
                if (diagnosticMode) {
                    sound.playShortResource(this, R.raw.err);
                    ShowVerboseError(true, 0, color, isCalibration);
                } else {
                    showError(message, ImageUtils.getBitmap(data));
                }
            } else {

                if (diagnosticMode) {
                    sound.playShortResource(this, R.raw.done);
                    ShowVerboseError(false, result, color, false);
                } else {
                    String title = mainApp.currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage());

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
    public void onFinishDilutionDialog(int index) {
        if (index == -1) {
            releaseResources();
            Intent intent = new Intent(getIntent());
            this.setResult(Activity.RESULT_CANCELED, intent);
            finish();
        } else {
            mDilutionLevel = index;
            mDilutionFragment.dismiss();

            //todo: remove hard coding of dilution levels
            String dilutionLabel;
            switch (mDilutionLevel) {
                case 0:
                    mDilutionTextView.setText(R.string.noDilution);
                    mDilutionTextView1.setText(R.string.noDilution);
                    break;
                case 1:
                    dilutionLabel = String.format(getString(R.string.timesDilution), 2);
                    mDilutionTextView.setText(dilutionLabel);
                    mDilutionTextView1.setText(dilutionLabel);
                    break;
                case 2:
                    dilutionLabel = String.format(getString(R.string.timesDilution), 5);
                    mDilutionTextView.setText(dilutionLabel);
                    mDilutionTextView1.setText(dilutionLabel);
                    break;
            }

            if (!mIsCalibration) {
                mDilutionTextView.setVisibility(View.VISIBLE);
                mDilutionTextView1.setVisibility(View.VISIBLE);
            }
            InitializeTest();
        }
    }

    @Override
    public void onFinishErrorListDialog(boolean retry, boolean cancelled, boolean isCalibration) {
        mResultFragment.dismiss();
        if (mHighLevelsFound && !isCalibration) {
            mCameraFragment.dismiss();
            sound.playShortResource(this, R.raw.beep_long);
            MainApp mainApp = (MainApp) getApplicationContext();
            String title = mainApp.currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage());

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