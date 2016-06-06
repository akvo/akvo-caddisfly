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
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
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
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.CameraDialogFragment;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.ec.UsbService;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class ColorimetryLiquidActivity extends BaseActivity
        implements ResultDialogFragment.ResultDialogListener,
        HighLevelsDialogFragment.MessageDialogListener,
        DiagnosticResultDialog.DiagnosticResultDialogListener,
        CameraDialog.Cancelled {
    public static final int RESULT_RESTART_TEST = 3;
    private final Handler delayHandler = new Handler();
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private final Handler handler = new Handler();
    boolean mStatus = false;
    boolean mDebug;
    // Notifications from UsbService will be received here.
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
//            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB PERMISSION GRANTED
//            {
//                Toast.makeText(arg0, "USB Ready", Toast.LENGTH_SHORT).show();
//            } else
            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)) // USB PERMISSION NOT GRANTED
            {
                if (mDebug) {
                    Toast.makeText(arg0, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                }
            } else if (arg1.getAction().equals(UsbService.ACTION_NO_USB)) // NO USB CONNECTED
            {
                if (mDebug) {
                    Toast.makeText(arg0, "No USB connected", Toast.LENGTH_SHORT).show();
                }
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_DISCONNECTED)) // USB DISCONNECTED
            {
                if (mDebug) {
                    Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
                }
                //releaseResources();
                //finish();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_NOT_SUPPORTED)) // USB NOT SUPPORTED
            {
                if (mDebug) {
                    Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
                }
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB NOT SUPPORTED
            {
                if (mDebug) {
                    Toast.makeText(arg0, "USB device connected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
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
    private Runnable delayRunnable;
    private PowerManager.WakeLock wakeLock;
    private ArrayList<Result> mResults;
    private boolean mTestCompleted;
    private boolean mHighLevelsFound;
    private boolean mIgnoreShake;
    //pointer to last dialog opened so it can be dismissed on activity getting destroyed
    private AlertDialog alertDialogToBeDestroyed;
    private boolean mIsFirstResult;
    private USBMonitor mUSBMonitor;
    private String mReceivedData = "";
    private UsbService usbService;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestResult("STATUS\r\n");
            //handler.postDelayed(this, 2000);
        }
    };
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

    private final Runnable initializeRunnable = new Runnable() {
        @Override
        public void run() {
            final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getBaseContext(), R.xml.camera_device_filter);
            List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
            if (usbDeviceList.size() > 0 && usbDeviceList.get(0).getVendorId() != AppConfig.ARDUINO_VENDOR_ID) {
                startExternalTest();
            } else {
                Toast.makeText(getBaseContext(), "Camera not found", Toast.LENGTH_SHORT).show();
                releaseResources();
                finish();
            }
        }
    };

    private void displayResult(final String value) {
        if (mDebug) {
            Toast.makeText(this, value.trim(), Toast.LENGTH_SHORT).show();
        }

        final ColorimetryLiquidActivity context = this;
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                if (!mStatus && value.trim().equalsIgnoreCase("OK")) {
                    mStatus = true;
                    //LocalBroadcastManager.getInstance(context).unregisterReceiver(context.mReceiver);

//                    //Check Device Type
//                    requestResult("DEVICE\r\n");
//                } else if (value.trim().toLowerCase(Locale.US).contains("akvo caddisfly")) {
                    //Turn Camera On
                    requestResult("SET CAMERA ON\r\n");
                    if (mDebug) {
                        Toast.makeText(context, "SET CAMERA ON", Toast.LENGTH_SHORT).show();
                    }
                } else if (value.trim().equalsIgnoreCase("ON")) {
                    String brightness = PreferencesUtil.getString(context,
                            CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                            R.string.ledBrightnessKey, "150");
                    //Set LED brightness
                    requestResult(String.format("SET LED %s\r\n", brightness));
                    if (mDebug) {
                        Toast.makeText(context, String.format("SET LED %s", brightness), Toast.LENGTH_SHORT).show();
                    }
                } else if (value.trim().contains("OK")) {
                    if (mDebug) {
                        Toast.makeText(context, "Starting Preview", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Initializing", Toast.LENGTH_SHORT).show();
                    }

                    initializeHandler.postDelayed(initializeRunnable, 17000);

                } else {
                    mStatus = false;
                    Toast.makeText(context, "Error initializing camera", Toast.LENGTH_SHORT).show();
                    releaseResources();
                    finish();
                }
            }
        }, 1000);
    }

    private void requestResult(String command) {
        //Log.d(DEBUG_TAG, "Request Result");
        if (usbService != null && usbService.isUsbConnected()) {
            // if UsbService was correctly bound, Send data
            usbService.write(command.getBytes());
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
        //Log.d(DEBUG_TAG, "Start Service");

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

        if (mDebug) {
            Toast.makeText(this, "STATUS", Toast.LENGTH_SHORT).show();
        }

        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
//        handler.removeCallbacks(runnable);
//        unregisterReceiver(mUsbReceiver);
//        unbindService(usbConnection);
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        setFilters();  // Start listening notifications from UsbService

        // Start UsbService(if it was not started before) and Bind it
        startService(UsbService.class, usbConnection, null);

//        IntentFilter intentFilter = new IntentFilter("my-event");
//        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            if (mIsCalibration) {
                String subTitle = String.format(Locale.getDefault(), "%s %.2f %s",
                        getResources().getString(R.string.calibrate),
                        mSwatchValue, CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
                textDilution.setText(subTitle);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_colorimetry_liquid);

        setTitle("Analysis");

        mDebug = AppPreferences.getShowDebugMessages();

        mHandler = new MyHandler(this);
        initializeHandler = new MyHandler(this);

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

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (isDestroyed()) {
                        return;
                    }
                }

                mWaitingForStillness = true;

                showError(getString(R.string.errorTestInterrupted), null);
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

        mSensorManager.unregisterListener(mShakeDetector);

        textSubtitle.setText(R.string.placeDevice);

    }

    private void InitializeTest() {

        mSensorManager.unregisterListener(mShakeDetector);

        mIgnoreShake = false;
        mTestCompleted = false;
        mHighLevelsFound = false;

        mWaitingForStillness = true;

        if (ApiUtil.isCameraInUse(this, null)) {
            releaseResources();
            finish();
        }

        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.camera_device_filter);
        List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));

        if (usbDeviceList.size() > 0 && usbDeviceList.get(0).getVendorId() != AppConfig.ARDUINO_VENDOR_ID) {
            startExternalTest();
        } else {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI);
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
     * Start the test by displaying the progress bar
     */
    private void dismissShakeAndStartTest() {
        mSensorManager.unregisterListener(mShakeDetector);

        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.camera_device_filter);
        List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
        if (usbDeviceList.size() > 0 && usbDeviceList.get(0).getVendorId() != AppConfig.ARDUINO_VENDOR_ID) {
            startExternalTest();
        } else {
            startTest();
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
                        InitializeTest();
//                        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
//                                SensorManager.SENSOR_DELAY_UI);

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
            case 0:
                textDilution.setText(R.string.noDilution);
                break;
            case 1:
                textDilution.setText(String.format(getString(R.string.timesDilution), 2));
                break;
            case 2:
                textDilution.setText(String.format(getString(R.string.timesDilution), 5));
                break;
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
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        Configuration conf = getResources().getConfiguration();

        //set the title to the test contaminant name
        ((TextView) findViewById(R.id.textTitle)).setText(testInfo.getName(conf.locale.getLanguage()));

        if (testInfo.getCode().isEmpty()) {
            alertCouldNotLoadConfig();
        } else if (!mTestCompleted) {
            InitializeTest();
        }
    }

    /**
     * Display error message for configuration not loading correctly
     */
    private void alertCouldNotLoadConfig() {
        String message = String.format("%s\r\n\r\n%s",
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
            swatches.add(testInfo.getSwatches().get(0));
            SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
            results.add(SwatchHelper.analyzeColor(1, photoColor, swatches, ColorUtil.ColorModel.LAB));
            results.add(SwatchHelper.analyzeColor(1, photoColor, swatches, ColorUtil.ColorModel.RGB));

            swatches.clear();

            //add only the first and last swatch for a 2 step analysis
            swatches.add(testInfo.getSwatches().get(0));
            swatches.add(testInfo.getSwatches().get(testInfo.getSwatches().size() - 1));
            SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
            results.add(SwatchHelper.analyzeColor(2, photoColor, swatches, ColorUtil.ColorModel.LAB));
            results.add(SwatchHelper.analyzeColor(2, photoColor, swatches, ColorUtil.ColorModel.RGB));

            swatches.clear();

            //add the middle swatch for a 3 step analysis
            swatches.add(testInfo.getSwatches().get(0));
            swatches.add(testInfo.getSwatches().get(testInfo.getSwatches().size() - 1));
            swatches.add(1, testInfo.getSwatches().get((testInfo.getSwatches().size() / 2)));
            SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
            results.add(SwatchHelper.analyzeColor(3, photoColor, swatches, ColorUtil.ColorModel.LAB));
            results.add(SwatchHelper.analyzeColor(3, photoColor, swatches, ColorUtil.ColorModel.RGB));

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

        mSensorManager.unregisterListener(mShakeDetector);
        mWaitingForStillness = false;

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
                //if (!((ExternalCameraFragment) mCameraFragment).hasTestCompleted()) {

                mCameraFragment.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
                    @Override
                    public void onPictureTaken(byte[] bytes, boolean completed) {
                        //Bitmap bitmap = ImageUtil.getBitmap(bytes);

                        //Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                        //getAnalyzedResult(croppedBitmap);
                        //bitmap.recycle();


                        Bitmap bitmap = ImageUtil.getBitmap(bytes);

                        Display display = getWindowManager().getDefaultDisplay();
                        int rotation = 0;
                        switch (display.getRotation()) {
                            case Surface.ROTATION_0:
                                rotation = 90;
                                break;
                            case Surface.ROTATION_90:
                                rotation = 0;
                                break;
                            case Surface.ROTATION_180:
                                rotation = 270;
                                break;
                            case Surface.ROTATION_270:
                                rotation = 180;
                                break;
                        }

                        bitmap = ImageUtil.rotateImage(bitmap, rotation);

                        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

                        Bitmap croppedBitmap;
                        //todo: fix this hardcoding
                        if (testInfo.getCode().equalsIgnoreCase("turbi")) {
                            croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                                    ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, false);

                            croppedBitmap = ImageUtil.getGrayscale(croppedBitmap);
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
                            AnalyzeFinalResult(bytes, croppedBitmap);
                            mCameraFragment.dismiss();
                        } else {
                            sound.playShortResource(R.raw.beep);
                        }


//                        if (completed) {
//                            ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
//                            if (croppedBitmap != null) {
//                                croppedBitmap.copyPixelsToBuffer(buffer);
//                            }
//                            byte[] data = buffer.array();
//                            AnalyzeFinalResult(data);
//                            mCameraFragment.dismiss();
//                        }
                    }
                });
                //}

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

        mWaitingForStillness = false;
        mIsFirstResult = true;

        sound.playShortResource(R.raw.beep);
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

                mCameraFragment = CameraDialogFragment.newInstance();

                mCameraFragment.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
                    @Override
                    public void onPictureTaken(byte[] bytes, boolean completed) {
                        Bitmap bitmap = ImageUtil.getBitmap(bytes);

                        Display display = getWindowManager().getDefaultDisplay();
                        int rotation = 0;
                        switch (display.getRotation()) {
                            case Surface.ROTATION_0:
                                rotation = 90;
                                break;
                            case Surface.ROTATION_90:
                                rotation = 0;
                                break;
                            case Surface.ROTATION_180:
                                rotation = 270;
                                break;
                            case Surface.ROTATION_270:
                                rotation = 180;
                                break;
                        }

                        bitmap = ImageUtil.rotateImage(bitmap, rotation);

                        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

                        Bitmap croppedBitmap;
                        //todo: fix this hardcoding
                        if (testInfo.getCode().equalsIgnoreCase("turbi")) {
                            croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                                    ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, false);

                            croppedBitmap = ImageUtil.getGrayscale(croppedBitmap);
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
                            AnalyzeFinalResult(bytes, croppedBitmap);
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
                            e.printStackTrace();
                            finish();
                        }
                    }
                };

                delayHandler.postDelayed(delayRunnable, ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING);

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
    private void AnalyzeFinalResult(byte[] data, Bitmap croppedBitmap) {

        releaseResources();

        boolean isCalibration = getIntent().getBooleanExtra("isCalibration", false);

        String message = "";

        if (mResults.size() == 0) {
            showError(getString(R.string.chamberNotFound), ImageUtil.getBitmap(data));
        } else {

            double result = SwatchHelper.getAverageResult(mResults);

            if (result >= CaddisflyApp.getApp().getCurrentTestInfo().getDilutionRequiredLevel() &&
                    CaddisflyApp.getApp().getCurrentTestInfo().getCanUseDilution()) {
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

            String resultText = String.format(Locale.getDefault(), "%.2f", result);
            if (mHighLevelsFound) {
                resultText = "> " + resultText;
            }

            int color = SwatchHelper.getAverageColor(mResults);

            Intent intent = getIntent();
            String cadUuid = intent.getExtras().getString("caddisflyResourceUuid");

            TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
            JSONObject resultJsonObj = new JSONObject();
            JSONArray resultJsonArr = new JSONArray();

            Intent resultIntent = new Intent(intent);
            resultIntent.putExtra("result", resultText);
            resultIntent.putExtra("color", color);

            if (cadUuid != null) {
                try {
                    TestInfo.SubTest subTest = testInfo.getSubTests().get(0);
                    JSONObject object = new JSONObject();

                    object.put("name", subTest.getDesc());
                    object.put("value", resultText);
                    object.put("unit", subTest.getUnit());
                    object.put("id", subTest.getId());

                    //TextUtils.join(",",testInfo.getSwatches());
                    JSONArray calibrationJsonArr = new JSONArray();
                    for (Swatch swatch : testInfo.getSwatches()) {
                        calibrationJsonArr.put(Integer.toHexString(swatch.getColor() & 0x00FFFFFF));
                    }

                    object.put("result-color", Integer.toHexString(color & 0x00FFFFFF));
                    object.put("calibration", calibrationJsonArr);

                    resultJsonObj.put("type", "caddisfly");
                    resultJsonObj.put("name", testInfo.getName("en"));
                    resultJsonObj.put("uuid", testInfo.getUuid());

                    resultJsonArr.put(object);
                    resultJsonObj.put("result", resultJsonArr);

                    String resultImageUrl = UUID.randomUUID().toString() + ".png";
                    String path = FileStorage.writeBitmapToExternalStorage(croppedBitmap, "/result-images", resultImageUrl);
                    if (path.length() > 0) {
                        resultJsonObj.put("image", resultImageUrl);
                        resultIntent.putExtra("image", path);
                    }

                    resultJsonObj.put("test_date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                            .format(Calendar.getInstance().getTime()));
                    resultJsonObj.put("calibrated_date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                            .format(Calendar.getInstance().getTime()));
                    resultJsonObj.put("reagent_expiry", new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            .format(Calendar.getInstance().getTime()));
                    resultJsonObj.put("reagent_batch", "OIUNPOIUN553443");

                    resultJsonObj.put("app_version", CaddisflyApp.getAppVersion(this));

                    resultJsonObj.put("device_model", Build.MODEL);
                    resultJsonObj.put("device_product", Build.PRODUCT);
                    resultJsonObj.put("device_manufacturer", Build.MANUFACTURER);
                    resultJsonObj.put("device_os", "Android - " + android.os.Build.VERSION.RELEASE + " (" +
                            android.os.Build.VERSION.SDK_INT + ")");
                    resultJsonObj.put("device_country", Locale.getDefault().getCountry());
                    resultJsonObj.put("device_language", Locale.getDefault().getLanguage());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                resultIntent.putExtra("finish", true);
                resultIntent.putExtra("response", resultJsonObj.toString());
            } else {
                resultIntent.putExtra("response", String.format(Locale.US, "%.2f", result));
            }

            setResult(Activity.RESULT_OK, resultIntent);
            mTestCompleted = true;

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
                    }, 500);
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
                            }
                        } else {
                            sound.playShortResource(R.raw.done);
                        }

                        PreferencesUtil.setInt(this, R.string.totalSuccessfulTestsKey,
                                PreferencesUtil.getInt(this, R.string.totalSuccessfulTestsKey, 0) + 1);

                        ResultDialogFragment mResultDialogFragment = ResultDialogFragment.newInstance(title,
                                resultText, mDilutionLevel, message, CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();

                        Fragment fragment = getFragmentManager().findFragmentByTag("resultDialog");
                        if (fragment != null) {
                            ft.remove(fragment);
                        }

                        mResultDialogFragment.setCancelable(false);
                        mResultDialogFragment.show(ft, "resultDialog");
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
                + "_" + batteryPercent + "_" + ApiUtil.getEquipmentId(this));
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

        try {
            handler.removeCallbacks(runnable);
            unregisterReceiver(mUsbReceiver);
            unbindService(usbConnection);
        } catch (Exception ignored) {

        }

        initializeHandler.removeCallbacks(initializeRunnable);

        //LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
//        releaseResources();
//        if (alertDialogToBeDestroyed != null) {
//            alertDialogToBeDestroyed.dismiss();
//        }
//        setResult(Activity.RESULT_CANCELED);
//        finish();
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

            String message = "";
            //todo: remove hard coding of dilution levels
            switch (mDilutionLevel) {
                case 0:
                    message = String.format(getString(R.string.tryWithDilutedSample), 2);
                    break;
                case 1:
                    message = String.format(getString(R.string.tryWithDilutedSample), 5);
                    break;
            }

            ResultDialogFragment mResultDialogFragment = ResultDialogFragment.newInstance(title, result,
                    mDilutionLevel, message, CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
            final FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment fragment = getFragmentManager().findFragmentByTag("resultDialog");
            if (fragment != null) {
                ft.remove(fragment);
            }

            mResultDialogFragment.setCancelable(false);
            mResultDialogFragment.show(ft, "resultDialog");

        } else if (retry) {
            mCameraFragment.dismiss();
            InitializeTest();
        } else {
            releaseResources();
            if (cancelled) {
                setResult(Activity.RESULT_CANCELED);
            }
            finish();
        }
    }

    @Override
    public void onFinishDialog() {
        Intent intent = new Intent(getIntent());
        intent.putExtra("response", String.valueOf(""));
        setResult(Activity.RESULT_OK, intent);
        releaseResources();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sound.release();
    }

    @Override
    public void dialogCancelled() {
        Intent intent = new Intent(getIntent());
        intent.putExtra("response", String.valueOf(""));
        setResult(Activity.RESULT_CANCELED, intent);
        releaseResources();
        finish();

    }

    /*
 * This handler will be passed to UsbService.
 * Data received from serial port is displayed through this handler
 */
    private static class MyHandler extends Handler {
        private final WeakReference<ColorimetryLiquidActivity> mActivity;

        public MyHandler(ColorimetryLiquidActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    ColorimetryLiquidActivity colorimetryLiquidActivity = mActivity.get();
                    if (colorimetryLiquidActivity != null) {

                        colorimetryLiquidActivity.mReceivedData += data;
                        if (colorimetryLiquidActivity.mReceivedData.contains("\r\n")) {
                            colorimetryLiquidActivity.displayResult(colorimetryLiquidActivity.mReceivedData);
                            colorimetryLiquidActivity.mReceivedData = "";
                        }


//                        if (data.contains("\r\n") || colorimetryLiquidActivity.mReceivedData.contains("\r\n")) {
//                            //Log.d(DEBUG_TAG, "result: " + colorimetryLiquidActivity.mReceivedData);
//                            colorimetryLiquidActivity.mReceivedData += data;
//                            colorimetryLiquidActivity.displayResult(colorimetryLiquidActivity.mReceivedData);
//                            colorimetryLiquidActivity.mReceivedData = "";
//                        } else {
//                            //Log.d(DEBUG_TAG, "serial: *" + data + "*");
//
//                            //Log.d(DEBUG_TAG, "serial result: " + colorimetryLiquidActivity.mReceivedData);
//                        }
                    }
                    break;
            }
        }
    }
}