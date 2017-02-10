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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.Display;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.CameraDialogFragment;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_180;
import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_270;
import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_90;

/**
 * The activity where the test chamber type test is conducted.
 */
@SuppressWarnings("deprecation")
public class ColorimetryLiquidActivity extends BaseActivity
        implements ResultDialogFragment.ResultDialogListener,
        DiagnosticResultDialog.DiagnosticResultDialogListener,
        CameraDialog.Cancelled {

    private static final String EMPTY_STRING = "";
    private static final String TWO_SENTENCE_FORMAT = "%s%n%n%s";
    private static final String RESULT_DIALOG_TAG = "resultDialog";
    private static final int RESULT_RESTART_TEST = 3;
    private static final int MAX_SHAKE_DURATION = 2000;
    private static final int MAX_SHAKE_DURATION_2 = 3000;
    private static final int DELAY_MILLIS = 500;
    private final Handler delayHandler = new Handler();
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    private double mResult;
    private Bitmap mCroppedBitmap;
    private boolean mIsCalibration;
    private double mSwatchValue;
    private int mDilutionLevel = 0;
    private DiagnosticResultDialog mResultFragment;
    private TextView textDilution;
    private SoundPoolPlayer sound;
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean mWaitingForStillness = false;
    private CameraDialog mCameraFragment;
    @Nullable
    private Runnable delayRunnable;
    private PowerManager.WakeLock wakeLock;
    private List<Result> mResults;
    private boolean mTestCompleted;
    private boolean mHighLevelsFound;
    private boolean mIgnoreShake;
    //reference to last dialog opened so it can be dismissed on activity getting destroyed
    private AlertDialog alertDialogToBeDestroyed;
    private boolean mIsFirstResult;

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mIsCalibration && getSupportActionBar() != null) {
            String subTitle = String.format(Locale.getDefault(), "%s %.2f %s",
                    getString(R.string.calibrate),
                    mSwatchValue, CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
            textDilution.setText(subTitle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_colorimetry_liquid);

        setTitle("Analysis");

        Intent intent = getIntent();
        mIsCalibration = intent.getBooleanExtra("isCalibration", false);
        mSwatchValue = intent.getDoubleExtra("swatchValue", 0);

        sound = new SoundPoolPlayer(this);

        textDilution = (TextView) findViewById(R.id.textDilution);
        TextView textSubtitle = (TextView) findViewById(R.id.textSubtitle);

        //Set up the shake detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake() {
                if ((mIgnoreShake) || mWaitingForStillness || mCameraFragment == null) {
                    return;
                }

                if (isDestroyed()) {
                    return;
                }

                mWaitingForStillness = true;

                showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestInterrupted),
                        getString(R.string.doNotMoveDeviceDuringTest)), null);
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

        mSensorManager.unregisterListener(mShakeDetector);

        mShakeDetector.setMinShakeAcceleration(5);
        mShakeDetector.setMaxShakeDuration(MAX_SHAKE_DURATION);

        textSubtitle.setText(R.string.placeDevice);

    }

    private void initializeTest() {

        mSensorManager.unregisterListener(mShakeDetector);

        mIgnoreShake = false;
        mTestCompleted = false;
        mHighLevelsFound = false;

        mWaitingForStillness = true;

        if (ApiUtil.isCameraInUse(this, null)) {
            releaseResources();
            finish();
        }

        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Acquire a wake lock to prevent the screen from turning off during the analysis process.
     */
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
     * Start the test by displaying the partial_progress bar.
     */
    private void dismissShakeAndStartTest() {
        mSensorManager.unregisterListener(mShakeDetector);

        startTest();
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    private void showError(String message, final Bitmap bitmap) {

        releaseResources();

        sound.playShortResource(R.raw.err);

        alertDialogToBeDestroyed = AlertUtil.showError(this, R.string.error, message, bitmap, R.string.retry,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        initializeTest();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        releaseResources();
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                }, null
        );
    }

    /**
     * In diagnostic mode show the diagnostic results dialog.
     *
     * @param testFailed    if test has failed then dialog knows to show the retry button
     * @param result        the result shown to the user
     * @param color         the color that was used to get above result
     * @param isCalibration is it in calibration mode, then show only colors, no results
     */
    private void showDiagnosticResultDialog(boolean testFailed, String result, int color, boolean isCalibration) {
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
        mSwatchValue = getIntent().getDoubleExtra("swatchValue", 0);
        mDilutionLevel = getIntent().getIntExtra("dilution", 0);

        switch (mDilutionLevel) {
            case 1:
                textDilution.setText(String.format(getString(R.string.timesDilution), 2));
                break;
            case 2:
                textDilution.setText(String.format(getString(R.string.timesDilution), 5));
                break;
            default:
                textDilution.setText(R.string.noDilution);
        }

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

//        TextView textResult = ((TextView) findViewById(R.id.textResult));
        if (!mIsCalibration) {
            //      textResult.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.appName);
            }

            // disable the key guard when device wakes up and shake alert is displayed
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        //set the title to the test contaminant name
        ((TextView) findViewById(R.id.textTitle)).setText(testInfo.getName());

        if (testInfo.getId().isEmpty()) {
            alertCouldNotLoadConfig();
        } else if (!mTestCompleted) {
            initializeTest();
        }
    }

    /**
     * Display error message for configuration not loading correctly.
     */
    private void alertCouldNotLoadConfig() {
        String message = String.format(TWO_SENTENCE_FORMAT,
                getString(R.string.errorLoadingConfiguration),
                getString(R.string.pleaseContactSupport));
        AlertUtil.showError(this, R.string.error, message, null, R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }, null, null);
    }

    /**
     * Get the test result by analyzing the bitmap.
     *
     * @param bitmap the bitmap of the photo taken during analysis
     */
    private void getAnalyzedResult(@NonNull Bitmap bitmap) {

        //Extract the color from the photo which will be used for comparison
        ColorInfo photoColor = ColorUtil.getColorFromBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        //Quality too low reject this result
//        if (photoColor.getQuality() < 20) {
//            return;
//        }

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        ArrayList<ResultDetail> results = new ArrayList<>();

        //In diagnostic mode show results based on other color models / number of calibration steps
        if (AppPreferences.isDiagnosticMode()) {
            ArrayList<Swatch> swatches = new ArrayList<>();

            //1 step analysis
            try {
                swatches.add((Swatch) testInfo.getSwatch(0).clone());
                SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
                results.add(SwatchHelper.analyzeColor(1, photoColor, swatches, ColorUtil.ColorModel.LAB));
                results.add(SwatchHelper.analyzeColor(1, photoColor, swatches, ColorUtil.ColorModel.RGB));

                swatches.clear();

                //add only the first and last swatch for a 2 step analysis
                swatches.add((Swatch) testInfo.getSwatch(0).clone());
                swatches.add((Swatch) testInfo.getSwatch(testInfo.getSwatches().size() - 1).clone());
                SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
                results.add(SwatchHelper.analyzeColor(2, photoColor, swatches, ColorUtil.ColorModel.LAB));
                results.add(SwatchHelper.analyzeColor(2, photoColor, swatches, ColorUtil.ColorModel.RGB));

                swatches.clear();

                //add the middle swatch for a 3 step analysis
                swatches.add((Swatch) testInfo.getSwatch(0).clone());
                swatches.add((Swatch) testInfo.getSwatch(testInfo.getSwatches().size() - 1).clone());
                swatches.add(1, (Swatch) testInfo.getSwatch(testInfo.getSwatches().size() / 2).clone());
                SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
                results.add(SwatchHelper.analyzeColor(3, photoColor, swatches, ColorUtil.ColorModel.LAB));
                results.add(SwatchHelper.analyzeColor(3, photoColor, swatches, ColorUtil.ColorModel.RGB));

            } catch (CloneNotSupportedException e) {
                Timber.e(e);
            }

            //use all the swatches for an all steps analysis
            results.add(SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
                    CaddisflyApp.getApp().getCurrentTestInfo().getSwatches(), ColorUtil.ColorModel.RGB));

            results.add(SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
                    CaddisflyApp.getApp().getCurrentTestInfo().getSwatches(), ColorUtil.ColorModel.LAB));
        }

        results.add(0, SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
                CaddisflyApp.getApp().getCurrentTestInfo().getSwatches(), ColorUtil.DEFAULT_COLOR_MODEL));

        Result result = new Result(bitmap, results);

        mResults.add(result);
    }

    private void startTest() {

        mResults = new ArrayList<>();

        mWaitingForStillness = false;
        mIsFirstResult = true;

        mShakeDetector.setMinShakeAcceleration(1);
        mShakeDetector.setMaxShakeDuration(MAX_SHAKE_DURATION_2);
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);

        (new AsyncTask<Void, Void, Void>() {

            @Nullable
            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                mCameraFragment = CameraDialogFragment.newInstance();

                mCameraFragment.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
                    @Override
                    public void onPictureTaken(@NonNull byte[] bytes, boolean completed) {
                        Bitmap bitmap = ImageUtil.getBitmap(bytes);

                        Display display = getWindowManager().getDefaultDisplay();
                        int rotation;
                        switch (display.getRotation()) {
                            case Surface.ROTATION_0:
                                rotation = DEGREES_90;
                                break;
                            case Surface.ROTATION_180:
                                rotation = DEGREES_270;
                                break;
                            case Surface.ROTATION_270:
                                rotation = DEGREES_180;
                                break;
                            case Surface.ROTATION_90:
                            default:
                                rotation = 0;
                                break;
                        }

                        bitmap = ImageUtil.rotateImage(bitmap, rotation);

                        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

                        Bitmap croppedBitmap;

                        if (testInfo.isUseGrayScale()) {
                            croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                                    ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, false);

                            if (croppedBitmap != null) {
                                croppedBitmap = ImageUtil.getGrayscale(croppedBitmap);
                            }
                        } else {
                            croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                                    ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, true);
                        }

                        //Ignore the first result as camera may not have focused correctly
                        if (!mIsFirstResult) {
                            if (croppedBitmap != null) {
                                getAnalyzedResult(croppedBitmap);
                            } else {
                                showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.chamberNotFound),
                                        getString(R.string.checkChamberPlacement)), ImageUtil.getBitmap(bytes));
                                mCameraFragment.stopCamera();
                                mCameraFragment.dismiss();
                                return;
                            }
                        }
                        mIsFirstResult = false;

                        if (completed) {
                            analyzeFinalResult(bytes, croppedBitmap);
                            mCameraFragment.dismiss();
                        } else {
                            sound.playShortResource(R.raw.beep);
                        }
                    }
                });

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
                            mCameraFragment.takePictures(AppPreferences.getSamplingTimes(),
                                    ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING);
                        } catch (Exception e) {
                            Timber.e(e);
                            finish();
                        }
                    }
                };

                delayHandler.postDelayed(delayRunnable, ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING);

            }
        }).execute();
    }

    /**
     * Analyze the result and display the appropriate success/fail message.
     * <p/>
     * If the result value is too high then display the high contamination level message
     *
     * @param data          image data to be displayed if error in analysis
     * @param croppedBitmap cropped image used for analysis
     */
    private void analyzeFinalResult(@NonNull byte[] data, Bitmap croppedBitmap) {

        releaseResources();

        if (mResults.size() == 0) {
            // Analysis failed. Display error
            showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.chamberNotFound),
                    getString(R.string.checkChamberPlacement)), ImageUtil.getBitmap(data));
        } else {

            mTestCompleted = true;

            // Get the result
            mResult = SwatchHelper.getAverageResult(mResults);

            TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

            // Check if contamination level is too high
            if (mResult >= testInfo.getDilutionRequiredLevel() && testInfo.getCanUseDilution()) {
                mHighLevelsFound = true;
            }

            // Calculate final result based on dilution
            switch (mDilutionLevel) {
                case 1:
                    mResult = mResult * 2;
                    break;
                case 2:
                    mResult = mResult * 5;
                    break;
                default:
                    break;
            }

            // Format the result
            String resultText = String.format(Locale.getDefault(), "%.2f", mResult);

            // Add 'greater than' symbol if result could be an unknown high value
            if (mHighLevelsFound) {
                resultText = "> " + resultText;
            }
            mCroppedBitmap = croppedBitmap;
            // Show the result dialog
            showResult(data, mResult, resultText);
        }
    }

    private void showResult(@NonNull byte[] data, double result, String resultText) {

        // Get the average color across the results
        int color = SwatchHelper.getAverageColor(mResults);

        // If calibrating then finish if successful
        if (mIsCalibration && color != Color.TRANSPARENT) {

            Intent resultIntent = new Intent(getIntent());
            setResult(Activity.RESULT_OK, resultIntent);
            resultIntent.putExtra(SensorConstants.COLOR, color);

            sound.playShortResource(R.raw.done);
            PreferencesUtil.setInt(this, R.string.totalSuccessfulCalibrationsKey,
                    PreferencesUtil.getInt(this, R.string.totalSuccessfulCalibrationsKey, 0) + 1);

            if (AppPreferences.isSaveImagesOn()) {
                saveImageForDiagnostics(data, resultText);
            }

            if (AppPreferences.isDiagnosticMode()) {
                showDiagnosticResultDialog(false, resultText, color, true);
            } else {
                (new Handler()).postDelayed(new Runnable() {
                    public void run() {
                        finish();
                    }
                }, DELAY_MILLIS);
            }
        } else {

            // If no result then display error message
            if (result < 0 || color == Color.TRANSPARENT) {
                if (AppPreferences.isSaveImagesOn()) {
                    saveImageForDiagnostics(data, resultText);
                }

                if (AppPreferences.isDiagnosticMode()) {
                    sound.playShortResource(R.raw.err);

                    PreferencesUtil.setInt(this, R.string.totalFailedTestsKey,
                            PreferencesUtil.getInt(this, R.string.totalFailedTestsKey, 0) + 1);

                    showDiagnosticResultDialog(true, EMPTY_STRING, color, mIsCalibration);
                } else {
                    if (mIsCalibration) {
                        showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.chamberNotFound),
                                getString(R.string.checkChamberPlacement)), ImageUtil.getBitmap(data));
                        PreferencesUtil.setInt(this, R.string.totalFailedCalibrationsKey,
                                PreferencesUtil.getInt(this, R.string.totalFailedCalibrationsKey, 0) + 1);
                    } else {
                        PreferencesUtil.setInt(this, R.string.totalFailedTestsKey,
                                PreferencesUtil.getInt(this, R.string.totalFailedTestsKey, 0) + 1);

                        showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestFailed),
                                getString(R.string.checkChamberPlacement)), ImageUtil.getBitmap(data));
                    }
                }
            } else {

                if (AppPreferences.isSaveImagesOn()) {
                    saveImageForDiagnostics(data, resultText);
                }

                if (AppPreferences.isDiagnosticMode()) {
                    sound.playShortResource(R.raw.done);
                    showDiagnosticResultDialog(false, resultText, color, false);

                    PreferencesUtil.setInt(this, R.string.totalSuccessfulTestsKey,
                            PreferencesUtil.getInt(this, R.string.totalSuccessfulTestsKey, 0) + 1);

                } else {
                    String title = CaddisflyApp.getApp().getCurrentTestInfo().getName();

                    String message = EMPTY_STRING;
                    if (mHighLevelsFound && mDilutionLevel < 2) {
                        sound.playShortResource(R.raw.beep_long);
                        switch (mDilutionLevel) {
                            case 0:
                                message = String.format(getString(R.string.tryWithDilutedSample), 2);
                                break;
                            case 1:
                                message = String.format(getString(R.string.tryWithDilutedSample), 5);
                                break;
                            default:
                                message = EMPTY_STRING;
                        }
                    } else {
                        sound.playShortResource(R.raw.done);
                    }

                    PreferencesUtil.setInt(this, R.string.totalSuccessfulTestsKey,
                            PreferencesUtil.getInt(this, R.string.totalSuccessfulTestsKey, 0) + 1);

                    ResultDialogFragment mResultDialogFragment = ResultDialogFragment.newInstance(title,
                            resultText, mDilutionLevel, message, CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();

                    Fragment fragment = getFragmentManager().findFragmentByTag(RESULT_DIALOG_TAG);
                    if (fragment != null) {
                        ft.remove(fragment);
                    }

                    mResultDialogFragment.setCancelable(false);
                    mResultDialogFragment.show(ft, RESULT_DIALOG_TAG);
                }
            }
        }
    }

    private void saveImageForDiagnostics(@NonNull byte[] data, String result) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, intentFilter);
        int batteryPercent = -1;

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPercent = (int) ((level / (float) scale) * 100);
        }

        if (mIsCalibration) {
            result = String.format(Locale.US, "%.2f", mSwatchValue);
        }

        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date());
        ImageUtil.saveImage(data,
                CaddisflyApp.getApp().getCurrentTestInfo().getId(),
                String.format(Locale.US, "%s_%s_%s_%d_%s", date, (mIsCalibration ? "C" : "T"),
                        result, batteryPercent, ApiUtil.getInstallationId(this)));
    }

    @Override
    public void onSuccessFinishDialog(boolean resultOk) {
        if (resultOk) {
            setResultIntent();
        } else {
            setResult(RESULT_RESTART_TEST);
        }

        finish();

    }

    private void setResultIntent() {
        // Get the average color across the results
        int color = SwatchHelper.getAverageColor(mResults);

        // If this is a test and it was successful then build the result to return
        if (!mIsCalibration && (mResult > -1 || color != Color.TRANSPARENT)) {
            Intent intent = getIntent();

            String resultText = String.format(Locale.getDefault(), "%.2f", mResult);

            // Add 'greater than' symbol if result could be an unknown high value
            if (mHighLevelsFound) {
                resultText = "> " + resultText;
            }

            Intent resultIntent = new Intent(intent);
            resultIntent.putExtra(SensorConstants.RESULT, resultText);
            resultIntent.putExtra(SensorConstants.COLOR, color);

            String resultImageUrl = EMPTY_STRING;
            if (getIntent().getBooleanExtra(Constant.SEND_IMAGE_IN_RESULT, false)) {
                // Save photo taken during the test
                resultImageUrl = UUID.randomUUID().toString() + ".png";
                String path = FileUtil.writeBitmapToExternalStorage(mCroppedBitmap, "/result-images", resultImageUrl);
                resultIntent.putExtra(SensorConstants.IMAGE, path);
            }

            TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

            SparseArray<String> results = new SparseArray<>();
            results.put(testInfo.getSubTests().get(0).getId(), resultText);

            JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, color,
                    resultImageUrl, null);

            resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

            // TODO: Remove this when obsolete
            // Backward compatibility. Return plain text result
            resultIntent.putExtra(SensorConstants.RESPONSE_COMPAT, resultText);

            setResult(Activity.RESULT_OK, resultIntent);

        }
    }

    private void releaseResources() {
        mSensorManager.unregisterListener(mShakeDetector);
        delayHandler.removeCallbacks(delayRunnable);
        if (mCameraFragment != null) {
            try {
                mCameraFragment.dismiss();
            } catch (Exception ignored) {

            }
            mCameraFragment.stopCamera();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        releaseResources();
        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed.dismiss();
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseResources();
    }

    @Override
    public void onFinishDiagnosticResultDialog(boolean retry, boolean cancelled, String result, boolean isCalibration) {
        mResultFragment.dismiss();
        if (mHighLevelsFound && !isCalibration) {
            mCameraFragment.dismiss();
            sound.playShortResource(R.raw.beep_long);
            String title = CaddisflyApp.getApp().getCurrentTestInfo().getName();

            String message;
            //todo: remove hard coding of dilution levels
            switch (mDilutionLevel) {
                case 0:
                    message = String.format(getString(R.string.tryWithDilutedSample), 2);
                    break;
                case 1:
                    message = String.format(getString(R.string.tryWithDilutedSample), 5);
                    break;
                default:
                    message = EMPTY_STRING;
            }

            ResultDialogFragment mResultDialogFragment = ResultDialogFragment.newInstance(title, result,
                    mDilutionLevel, message, CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
            final FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment fragment = getFragmentManager().findFragmentByTag(RESULT_DIALOG_TAG);
            if (fragment != null) {
                ft.remove(fragment);
            }

            mResultDialogFragment.setCancelable(false);
            mResultDialogFragment.show(ft, RESULT_DIALOG_TAG);

        } else if (retry) {
            mCameraFragment.dismiss();
            initializeTest();
        } else {
            releaseResources();
            if (cancelled) {
                setResult(Activity.RESULT_CANCELED);
            } else {
                setResultIntent();
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sound.release();
    }

    @Override
    public void dialogCancelled() {
        Intent intent = new Intent(getIntent());
        intent.putExtra(SensorConstants.RESPONSE, String.valueOf(EMPTY_STRING));
        this.setResult(Activity.RESULT_CANCELED, intent);
        releaseResources();
        finish();

    }
}
