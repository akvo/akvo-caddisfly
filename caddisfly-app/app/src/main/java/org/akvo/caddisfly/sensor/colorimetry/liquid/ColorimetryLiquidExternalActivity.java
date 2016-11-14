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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
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
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.usb.UsbService;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_180;
import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_270;
import static org.akvo.caddisfly.sensor.SensorConstants.DEGREES_90;

@SuppressWarnings("deprecation")
public class ColorimetryLiquidExternalActivity extends BaseActivity
        implements ResultDialogFragment.ResultDialogListener,
        DiagnosticResultDialog.DiagnosticResultDialogListener,
        CameraDialog.Cancelled {

    private static final String TAG = "ColorLiquidExtActivity";

    private static final int REQUEST_DELAY_MILLIS = 2000;
    private static final int DELAY_MILLIS = 500;
    private static final int MAX_COMMAND_INDEX = 99;
    private static final int DELAY_MULTI_DEVICE_START = 5000;
    private static final int DELAY_START = 12000;
    private static final int TIMEOUT_DURATION_SECONDS_EXTRA = 14;
    private static final int RESULT_RESTART_TEST = 3;
    private static final String RESULT_DIALOG_TAG = "resultDialog";
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
    private final Handler handler = new Handler();
    /*
       * Notifications from UsbService will be received here.
       */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED:
                    //Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED:
                    //Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB:
                    //Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED:
                    //Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED:
                    //Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private boolean mDebug;
    private int timeout = 0;
    private long testStartTime = 0;
    private boolean mIsCalibration;
    private double mSwatchValue;
    private int mDilutionLevel = 0;
    private DiagnosticResultDialog mResultFragment;
    private TextView textDilution;
    private SoundPoolPlayer sound;
    private CameraDialog mCameraFragment;
    private Runnable delayRunnable;
    private PowerManager.WakeLock wakeLock;
    private List<Result> mResults;
    private boolean mTestCompleted;
    private boolean mHighLevelsFound;
    //reference to last dialog opened so it can be dismissed on activity getting destroyed
    private AlertDialog alertDialogToBeDestroyed;
    private boolean mIsFirstResult;
    private USBMonitor mUSBMonitor;
    private String mReceivedData = "";
    private UsbService usbService;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    private Handler initializeHandler;
    private List<String> requestQueue;
    private int mCommandIndex = 0;
    private boolean requestsDone;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!requestsDone) {
                requestResult(requestQueue.get(mCommandIndex));

                mCommandIndex++;

                handler.postDelayed(this, REQUEST_DELAY_MILLIS);

                if (mCommandIndex > requestQueue.size() - 1) {
                    requestsDone = true;
                }

                if (!mDebug) {
                    Toast.makeText(getBaseContext(), "Connecting", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    private final Runnable initializeRunnable = new Runnable() {
        @Override
        public void run() {

            List<UsbDevice> usbDeviceList = getUsbDevices();

//            for (int i = 0; i < 30; i++) {
//                if (getCameraDevice(usbDeviceList) == null) {
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    usbDeviceList = getUsbDevices();
//                } else {
//                    break;
//                }
//            }

            if (getCameraDevice(usbDeviceList) != null) {
                startExternalTest();
            } else {
                Toast.makeText(getBaseContext(), "Camera not found", Toast.LENGTH_SHORT).show();
                //releaseResources();
                //finish();
            }
        }
    };

    private UsbDevice getCameraDevice(List<UsbDevice> usbDeviceList) {

        for (int i = 0; i < usbDeviceList.size(); i++) {
            if (usbDeviceList.get(i).getVendorId() == AppConfig.CAMERA_VENDOR_ID) {
                return usbDeviceList.get(i);
            }
        }

        return null;
    }

    private List<UsbDevice> getUsbDevices() {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.camera_device_filter);
        return mUSBMonitor.getDeviceList(filter.get(0));
    }

    private void displayResult(final String value) {

        if (mDebug) {
            Toast.makeText(this, value.trim(), Toast.LENGTH_SHORT).show();
        }

        if (requestsDone && mCommandIndex < MAX_COMMAND_INDEX) {
            Toast.makeText(this, "Initializing", Toast.LENGTH_LONG).show();
            if (AppPreferences.getExternalCameraMultiDeviceMode()) {
                initializeHandler.postDelayed(initializeRunnable, DELAY_MULTI_DEVICE_START);
            } else {
                initializeHandler.postDelayed(initializeRunnable, DELAY_START);
                testStartTime = System.currentTimeMillis() + DELAY_START;
            }
        }
    }

    private void requestResult(String command) {

        if (usbService != null) {
            if (mDebug) {
                Toast.makeText(this, command.trim(), Toast.LENGTH_SHORT).show();
            }

            try {
                // if UsbService was correctly bound, Send data
                usbService.write(command.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @SuppressWarnings("SameParameterValue")
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {

        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        requestQueue = new ArrayList<>();

        requestQueue.add("STATUS\r\n");

        String rgb = PreferencesUtil.getString(this,
                CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                R.string.ledRgbKey, "255,255,255");

        if (rgb.length() > 0) {
            rgb = rgb.trim().replace("-", ",").replace(" ", ",").replace(".", ",").replace(" ", ",");

            requestQueue.add(String.format("SET RGB %s", rgb) + "\r\n");
        }

        if (AppPreferences.getExternalCameraMultiDeviceMode()) {
            requestQueue.add("SET CAMERA ON\r\n");
        } else {
            timeout = TIMEOUT_DURATION_SECONDS_EXTRA
                    + (AppPreferences.getSamplingTimes() * ((ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING / 1000) + 1));
            requestQueue.add("SET CAMERATIME " + timeout + "\r\n");
            requestQueue.add("USB\r\n");
        }

        mCommandIndex = 0;
        handler.postDelayed(runnable, REQUEST_DELAY_MILLIS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        acquireWakeLock();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        setFilters();  // Start listening notifications from UsbService

        // Start UsbService(if it was not started before) and Bind it
        startService(UsbService.class, usbConnection, null);

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                if (usbService == null) {
                    Toast.makeText(getBaseContext(), "Caddisfly sensor not found", Toast.LENGTH_LONG).show();
                    releaseResources();
                    finish();
                }
            }
        }, 1000);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mIsCalibration && getSupportActionBar() != null) {
            String subTitle = String.format(Locale.getDefault(), "%s %.2f %s",
                    getResources().getString(R.string.calibrate),
                    mSwatchValue, CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
            textDilution.setText(subTitle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_external_colorimetry_liquid);

        setTitle("Analysis");

        mDebug = AppPreferences.getShowDebugMessages();

        mHandler = new MyHandler(this);
        initializeHandler = new Handler();

        mUSBMonitor = new USBMonitor(this, null);

        Intent intent = getIntent();
        mIsCalibration = intent.getBooleanExtra("isCalibration", false);
        mSwatchValue = intent.getDoubleExtra("swatchValue", 0);

        sound = new SoundPoolPlayer(this);

        textDilution = (TextView) findViewById(R.id.textDilution);
        TextView textSubtitle = (TextView) findViewById(R.id.textSubtitle);


        textSubtitle.setText("");
    }

    private void initializeTest() {

        mTestCompleted = false;
        mHighLevelsFound = false;

        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.camera_device_filter);
        if (mUSBMonitor != null) {
            List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));

            if (usbDeviceList.size() > 0 && getCameraDevice(usbDeviceList) != null) {
                startExternalTest();
            }
        }
    }

    /**
     * Acquire a wake lock to prevent the screen from turning off during the analysis process
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
     * Show an error message dialog
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
                        releaseResources();
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                }, null
        );
    }

    /**
     * In diagnostic mode show the diagnostic results dialog
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
                break;
        }

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        if (!mIsCalibration) {
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

        acquireWakeLock();

        Configuration conf = getResources().getConfiguration();

        //set the title to the test contaminant name
        ((TextView) findViewById(R.id.textTitle)).setText(testInfo.getName(conf.locale.getLanguage()));

        if (testInfo.getUuid().isEmpty()) {
            alertCouldNotLoadConfig();
        } else if (!mTestCompleted) {
            initializeTest();
        }
    }

    /**
     * Display error message for configuration not loading correctly
     */
    private void alertCouldNotLoadConfig() {
        String message = String.format("%s%n%n%s",
                getString(R.string.errorLoadingConfiguration),
                getString(R.string.pleaseContactSupport));
        AlertUtil.showError(this, R.string.error, message, null, R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null, null);
    }

    /**
     * Get the test result by analyzing the bitmap
     *
     * @param bitmap the bitmap of the photo taken during analysis
     */
    private void getAnalyzedResult(Bitmap bitmap) {

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
                Log.e(TAG, e.getMessage(), e);
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

    private void startExternalTest() {

        findViewById(R.id.layoutWait).setVisibility(View.INVISIBLE);

        mIsFirstResult = true;
        mResults = new ArrayList<>();
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                mCameraFragment = ExternalCameraFragment.newInstance();
                mCameraFragment.setCancelable(true);


                mCameraFragment.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
                    @Override
                    public void onPictureTaken(final byte[] bytes, boolean completed) {

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
                                showError(getString(R.string.chamberNotFound), ImageUtil.getBitmap(bytes));
                                mCameraFragment.stopCamera();
                                mCameraFragment.dismiss();
                                return;
                            }
                        }
                        mIsFirstResult = false;

                        if (completed) {

                            mCameraFragment.dismiss();
                            releaseResources();

                            analyzeFinalResult(bytes, croppedBitmap);

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
                        Fragment prev = getFragmentManager().findFragmentByTag("externalCameraDialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);
                        try {
                            mCameraFragment.show(ft, "externalCameraDialog");
                            mCameraFragment.takePictures(AppPreferences.getSamplingTimes(),
                                    ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                            finish();
                        }
                    }
                };

                delayHandler.postDelayed(delayRunnable, 0);
            }
        }).execute();
    }

    /**
     * Analyze the result and display the appropriate success/fail message
     * <p/>
     * If the result value is too high then display the high contamination level message
     *
     * @param data          image data to be displayed if error in analysis
     * @param croppedBitmap cropped image used for analysis
     */
    private void analyzeFinalResult(byte[] data, Bitmap croppedBitmap) {

        releaseResources();

        boolean isCalibration = getIntent().getBooleanExtra("isCalibration", false);

        String message = "";

        if (mResults.size() == 0) {
            // Analysis failed. Display error
            showError(getString(R.string.chamberNotFound), ImageUtil.getBitmap(data));
        } else {

            mTestCompleted = true;

            // Get the result
            double result = SwatchHelper.getAverageResult(mResults);

            // Check if contamination level is too high
            if (result >= CaddisflyApp.getApp().getCurrentTestInfo().getDilutionRequiredLevel()
                    && CaddisflyApp.getApp().getCurrentTestInfo().getCanUseDilution()) {
                mHighLevelsFound = true;
            }

            // Calculate final result based on dilution
            switch (mDilutionLevel) {
                case 1:
                    result = result * 2;
                    break;
                case 2:
                    result = result * 5;
                    break;
                default:
                    break;
            }

            // Format the result
            String resultText = String.format(Locale.getDefault(), "%.2f", result);

            // Add 'greater than' symbol if result could be an unknown high value
            if (mHighLevelsFound) {
                resultText = "> " + resultText;
            }

            // Get the average color across the results
            int color = SwatchHelper.getAverageColor(mResults);

            Intent intent = getIntent();

            TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

            Intent resultIntent = new Intent(intent);
            resultIntent.putExtra(SensorConstants.RESULT, resultText);
            resultIntent.putExtra(SensorConstants.COLOR, color);


            // Save photo taken during the test
            String resultImageUrl = UUID.randomUUID().toString() + ".png";
            String path = FileStorage.writeBitmapToExternalStorage(croppedBitmap, "/result-images", resultImageUrl);

            ArrayList<String> results = new ArrayList<>();
            results.add(resultText);

            JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, color, resultImageUrl);

            resultIntent.putExtra(SensorConstants.IMAGE, path);
            resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

            // TODO: Remove this when obsolete
            // Backward compatibility. Return plain text result
            resultIntent.putExtra(SensorConstants.RESPONSE_COMPAT, resultText);
            setResult(Activity.RESULT_OK, resultIntent);

            if (isCalibration && color != Color.TRANSPARENT) {
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
                if (result < 0 || color == Color.TRANSPARENT) {
                    if (AppPreferences.isSaveImagesOn()) {
                        saveImageForDiagnostics(data, resultText);
                    }

                    if (AppPreferences.isDiagnosticMode()) {
                        sound.playShortResource(R.raw.err);

                        PreferencesUtil.setInt(this, R.string.totalFailedTestsKey,
                                PreferencesUtil.getInt(this, R.string.totalFailedTestsKey, 0) + 1);

                        showDiagnosticResultDialog(true, "", color, isCalibration);
                    } else {
                        if (mIsCalibration) {
                            showError(getString(R.string.chamberNotFound), ImageUtil.getBitmap(data));
                            PreferencesUtil.setInt(this, R.string.totalFailedCalibrationsKey,
                                    PreferencesUtil.getInt(this, R.string.totalFailedCalibrationsKey, 0) + 1);
                        } else {
                            PreferencesUtil.setInt(this, R.string.totalFailedTestsKey,
                                    PreferencesUtil.getInt(this, R.string.totalFailedTestsKey, 0) + 1);

                            showError(getString(R.string.errorTestFailed), ImageUtil.getBitmap(data));
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
                        String title = CaddisflyApp.getApp().getCurrentTestInfo().
                                getName(getResources().getConfiguration().locale.getLanguage());

                        if (mHighLevelsFound && mDilutionLevel < 2) {
                            sound.playShortResource(R.raw.beep_long);
                            //todo: remove hard coding of dilution levels
                            switch (mDilutionLevel) {
                                case 0:
                                    message = String.format(getString(R.string.tryWithDilutedSample), 2);
                                    break;
                                case 1:
                                    message = String.format(getString(R.string.tryWithDilutedSample), 5);
                                    break;
                                default:
                                    message = "";
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
    }

    private void saveImageForDiagnostics(byte[] data, String result) {
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
        ImageUtil.saveImage(data, CaddisflyApp.getApp().getCurrentTestInfo().getCode(), date + "_"
                + (mIsCalibration ? "C" : "T") + "_" + result
                + "_" + batteryPercent + "_" + ApiUtil.getInstallationId(this));
    }

    @Override
    public void onSuccessFinishDialog(boolean resultOk) {
        if (resultOk) {
            finish();
        } else {
            setResult(RESULT_RESTART_TEST);
            finish();
        }
    }

    private void releaseResources() {

        mCommandIndex = MAX_COMMAND_INDEX;
        handler.removeCallbacks(runnable);

        UsbService.SERVICE_CONNECTED = false;

        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed.dismiss();
        }

        try {
            unbindService(usbConnection);
        } catch (Exception ignored) {

        }

        if (usbService != null) {
            if (AppPreferences.getExternalCameraMultiDeviceMode()) {
                requestResult("SET CAMERA OFF\r\n");
                requestResult("SET LED OFF\r\n");
            }

            try {
                usbService.stopSelf();
                usbService = null;
            } catch (Exception ignored) {

            }
        }

        try {
            unregisterReceiver(mUsbReceiver);
        } catch (Exception ignored) {

        }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseResources();
        overridePendingTransition(R.anim.slide_back_out, R.anim.slide_back_in);
    }

    @Override
    public void onFinishDiagnosticResultDialog(boolean retry, boolean cancelled, String result, boolean isCalibration) {
        mResultFragment.dismiss();
        if (mHighLevelsFound && !isCalibration) {
            mCameraFragment.dismiss();
            sound.playShortResource(R.raw.beep_long);
            String title = CaddisflyApp.getApp().getCurrentTestInfo().getName(getResources().getConfiguration().locale.getLanguage());

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
                    message = "";
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
            releaseResources();
            finish();
        } else {
            releaseResources();
            if (cancelled) {
                setResult(Activity.RESULT_CANCELED);
            }

            if (!AppPreferences.getExternalCameraMultiDeviceMode()) {

                long endTime = System.currentTimeMillis();
                long elapsedSeconds = endTime - testStartTime;

                final ProgressDialog progressDialog = ProgressDialog.show(this,
                        getString(R.string.pleaseWait), "Completing...", true, false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        finish();
                    }
                }, (timeout * 1000) - elapsedSeconds);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResources();
        sound.release();
    }

    @Override
    public void dialogCancelled() {
        Intent intent = new Intent(getIntent());
        intent.putExtra(SensorConstants.RESPONSE, String.valueOf(""));
        setResult(Activity.RESULT_CANCELED, intent);
        releaseResources();
        finish();

    }

    /*
    * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
    */
    private static class MyHandler extends Handler {
        private final WeakReference<ColorimetryLiquidExternalActivity> mActivity;

        MyHandler(ColorimetryLiquidExternalActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    ColorimetryLiquidExternalActivity colorimetryLiquidActivity = mActivity.get();
                    if (colorimetryLiquidActivity != null) {

                        colorimetryLiquidActivity.mReceivedData += data;
                        if (colorimetryLiquidActivity.mReceivedData.contains("\r\n")) {
                            colorimetryLiquidActivity.displayResult(colorimetryLiquidActivity.mReceivedData);
                            colorimetryLiquidActivity.mReceivedData = "";
                        }
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }
}
