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

package org.akvo.caddisfly.sensor.turbidity;

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
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.CameraDialogFragment;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ExternalCameraFragment;
import org.akvo.caddisfly.sensor.ec.UsbService;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("deprecation")
public class ExternalCameraActivity extends BaseActivity {
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
    private boolean mDebug;
    // Notifications from UsbService will be received here.
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB NOT SUPPORTED
            {
                if (mDebug) {
                    Toast.makeText(arg0, "USB device connected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    private boolean mIsCalibration;
    private double mSwatchValue;
    private TextView textDilution;
    private SoundPoolPlayer sound;
    private CameraDialog mCameraFragment;
    private Runnable delayRunnable;
    private PowerManager.WakeLock wakeLock;
    //private ArrayList<Result> mResults;
    //pointer to last dialog opened so it can be dismissed on activity getting destroyed
    //private boolean mIsFirstResult;
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
    private ArrayList<String> requestQueue;
    private int mCommandIndex = 0;
    private boolean requestsDone;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!requestsDone) {
                requestResult(requestQueue.get(mCommandIndex));

                mCommandIndex++;

                handler.postDelayed(this, 2000);

                if (mCommandIndex > requestQueue.size() - 1) {
                    requestsDone = true;
                }

                if (!mDebug) {
                    Toast.makeText(getBaseContext(), "Connecting", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    private String mSavePath;
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

        //LocalBroadcastManager.getInstance(context).unregisterReceiver(context.mReceiver);
        //if (value.trim().toUpperCase().contains("CAMERA ON")) {
        if (requestsDone && mCommandIndex < 99) {
            Toast.makeText(this, "Initializing", Toast.LENGTH_LONG).show();
            if (AppPreferences.getExternalCameraMultiDeviceMode()) {
                initializeHandler.postDelayed(initializeRunnable, 5000);
            } else {
                initializeHandler.postDelayed(initializeRunnable, 12000);
            }
        }
    }

    private void requestResult(String command) {

        if (usbService != null && usbService.isUsbConnected()) {
            if (mDebug) {
                Toast.makeText(this, command.trim(), Toast.LENGTH_SHORT).show();
            }

            try {
                // if UsbService was correctly bound, Send data
                usbService.write(command.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
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

        //String uuid = getIntent().getStringExtra("uuid");

        String rgb = PreferencesUtil.getString(this,
                CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                R.string.ledRgbKey, "255,255,255");

        if (rgb.length() > 0) {
            rgb = rgb.trim().replace("-", ",").replace(" ", ",").replace(".", ",").replace(" ", ",");

            requestQueue.add(String.format("SET RGB %s\r\n", rgb));
        }

        if (AppPreferences.getExternalCameraMultiDeviceMode()) {
            requestQueue.add("SET CAMERA ON\r\n");
        } else {
            int timeout = 14 + (AppPreferences.getSamplingTimes() * ((ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING / 1000) + 1));
            requestQueue.add("SET CAMERATIME " + timeout + "\r\n");
            requestQueue.add("USB\r\n");
        }

        mCommandIndex = 0;
        handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        acquireWakeLock();
        //Intent intent = new Intent("custom-event-name");
        //LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        setFilters();  // Start listening notifications from UsbService

        // Start UsbService(if it was not started before) and Bind it
        if (usbService == null || !usbService.isUsbConnected()) {
            startService(UsbService.class, usbConnection, null);
        }

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                if (usbService == null || !usbService.isUsbConnected()) {
                    Toast.makeText(getBaseContext(), "Caddisfly sensor not found", Toast.LENGTH_LONG).show();
                    releaseResources();
                    finish();
                }
            }
        }, 1000);

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

        setContentView(R.layout.activity_external_colorimetry_liquid);

        setTitle("Analysis");

        mDebug = AppPreferences.getShowDebugMessages();

        mHandler = new MyHandler(this);
        initializeHandler = new Handler();

        mUSBMonitor = new USBMonitor(this, null);

        Intent intent = getIntent();
        mIsCalibration = intent.getBooleanExtra("isCalibration", false);
        mSwatchValue = intent.getDoubleExtra("swatchValue", 0);
        mSavePath = intent.getStringExtra("savePath");
        String uuid = intent.getStringExtra("uuid");

        CaddisflyApp.getApp().loadTestConfigurationByUuid(uuid);

        sound = new SoundPoolPlayer(this);

        textDilution = (TextView) findViewById(R.id.textDilution);
        TextView textSubtitle = (TextView) findViewById(R.id.textSubtitle);

        textSubtitle.setText("");
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

    @Override
    protected void onStart() {
        super.onStart();
        mIsCalibration = getIntent().getBooleanExtra("isCalibration", false);
        mSwatchValue = getIntent().getDoubleExtra("swatchValue", 0);
        int mDilutionLevel = getIntent().getIntExtra("dilution", 0);

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

        String uuid = getIntent().getStringExtra("uuid");
        CaddisflyApp.getApp().loadTestConfigurationByUuid(uuid);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        if (!mIsCalibration) {
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
        }

        acquireWakeLock();
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

//    /**
//     * Get the test result by analyzing the bitmap
//     *
//     * @param bitmap the bitmap of the photo taken during analysis
//     */
    //private void getAnalyzedResult(Bitmap bitmap) {

        //Extract the color from the photo which will be used for comparison
        //ColorInfo photoColor = ColorUtil.getColorFromBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        //Quality too low reject this result
//        if (photoColor.getQuality() < 20) {
//            return;
//        }

        //TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

       //ArrayList<ResultDetail> results = new ArrayList<>();

        //In diagnostic mode show results based on other color models / number of calibration steps
//        if (AppPreferences.isDiagnosticMode()) {
//            ArrayList<Swatch> swatches = new ArrayList<>();
//
//            //1 step analysis
//            swatches.add((Swatch) testInfo.getSwatch(0).clone());
//            SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
//            results.add(SwatchHelper.analyzeColor(1, photoColor, swatches, ColorUtil.ColorModel.LAB));
//            results.add(SwatchHelper.analyzeColor(1, photoColor, swatches, ColorUtil.ColorModel.RGB));
//
//            swatches.clear();
//
//            //add only the first and last swatch for a 2 step analysis
//            swatches.add((Swatch) testInfo.getSwatch(0).clone());
//            swatches.add((Swatch) testInfo.getSwatch(testInfo.getSwatches().size() - 1).clone());
//            SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
//            results.add(SwatchHelper.analyzeColor(2, photoColor, swatches, ColorUtil.ColorModel.LAB));
//            results.add(SwatchHelper.analyzeColor(2, photoColor, swatches, ColorUtil.ColorModel.RGB));
//
//            swatches.clear();
//
//            //add the middle swatch for a 3 step analysis
//            swatches.add((Swatch) testInfo.getSwatch(0).clone());
//            swatches.add((Swatch) testInfo.getSwatch(testInfo.getSwatches().size() - 1).clone());
//            swatches.add(1, (Swatch) testInfo.getSwatch(testInfo.getSwatches().size() / 2).clone());
//            SwatchHelper.generateSwatches(swatches, testInfo.getSwatches());
//            results.add(SwatchHelper.analyzeColor(3, photoColor, swatches, ColorUtil.ColorModel.LAB));
//            results.add(SwatchHelper.analyzeColor(3, photoColor, swatches, ColorUtil.ColorModel.RGB));
//
//            //use all the swatches for an all steps analysis
//            results.add(SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
//                    CaddisflyApp.getApp().getCurrentTestInfo().getSwatches(), ColorUtil.ColorModel.RGB));
//
//            results.add(SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
//                    CaddisflyApp.getApp().getCurrentTestInfo().getSwatches(), ColorUtil.ColorModel.LAB));
//        }
//
//        results.add(0, SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
//                CaddisflyApp.getApp().getCurrentTestInfo().getSwatches(), ColorUtil.DEFAULT_COLOR_MODEL));

        //Result result = new Result(bitmap, results);

        //mResults.add(result);
   // }

    private void startExternalTest() {

        findViewById(R.id.layoutWait).setVisibility(View.INVISIBLE);

        //mIsFirstResult = true;

        //mResults = new ArrayList<>();
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
                    public void onPictureTaken(final byte[] bytes, boolean completed) {
                        //Bitmap bitmap = ImageUtil.getBitmap(bytes);
                        //Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);
                        //getAnalyzedResult(croppedBitmap);
                        //bitmap.recycle();

//                        Bitmap bitmap = ImageUtil.getBitmap(bytes);
//
//                        Display display = getWindowManager().getDefaultDisplay();
//                        int rotation = 0;
//                        switch (display.getRotation()) {
//                            case Surface.ROTATION_0:
//                                rotation = 90;
//                                break;
//                            case Surface.ROTATION_90:
//                                rotation = 0;
//                                break;
//                            case Surface.ROTATION_180:
//                                rotation = 270;
//                                break;
//                            case Surface.ROTATION_270:
//                                rotation = 180;
//                                break;
//                        }

//                        bitmap = ImageUtil.rotateImage(bitmap, rotation);
//                        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

//                        Bitmap croppedBitmap;
//
//                        if (testInfo.isUseGrayScale()) {
//                            croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
//                                    ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, false);
//
//                            croppedBitmap = ImageUtil.getGrayscale(croppedBitmap);
//                        } else {
//                            croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
//                                    ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, true);
//                        }

                        //Ignore the first result as camera may not have focused correctly
//                        if (!mIsFirstResult) {
//                            if (croppedBitmap != null) {
//                                getAnalyzedResult(croppedBitmap);
//                            }
//                        }
                        //mIsFirstResult = false;

                        if (completed) {

                            mCameraFragment.dismiss();
                            releaseResources();

                            AnalyzeFinalResult(bytes);

                        }

                    }
                });

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
     * @param data image data to be displayed if error in analysis
     */
    private void AnalyzeFinalResult(byte[] data) {

        releaseResources();
        String uuid = getIntent().getStringExtra("uuid");
        CaddisflyApp.getApp().loadTestConfigurationByUuid(uuid);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        saveImage(data, testInfo);
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 500);
    }

    private void releaseResources() {

        mCommandIndex = 99;
        handler.removeCallbacks(runnable);

        UsbService.SERVICE_CONNECTED = false;

        if (usbService != null) {
            if (AppPreferences.getExternalCameraMultiDeviceMode()) {
                requestResult("SET CAMERA OFF\r\n");
                requestResult("SET LED OFF\r\n");
            }

            try {
                usbService.stopSelf();
                unbindService(usbConnection);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResources();
        sound.release();
    }


    @SuppressWarnings("SameParameterValue")
    private void saveImage(byte[] data, TestInfo testInfo) {

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, intentFilter);
        int batteryPercent = -1;

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPercent = (int) ((level / (float) scale) * 100);
        }

        String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date());

        String fileName;
        File folder;

        switch (testInfo.getCode().toLowerCase(Locale.US)) {
            case SensorConstants.FLUORIDE_ID:

                Bitmap bitmap = ImageUtil.getBitmap(data);
                bitmap = ImageUtil.rotateImage(bitmap, 90);
                Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                        ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, true);

                //Extract the color from the photo which will be used for comparison
                ColorInfo photoColor = ColorUtil.getColorFromBitmap(croppedBitmap,
                        ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                CaddisflyApp.getApp().setDefaultTest();
                ResultDetail resultDetail = SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
                        testInfo.getSwatches(), ColorUtil.DEFAULT_COLOR_MODEL);
                fileName = date + "_" + resultDetail.getResult() + "_" + batteryPercent;

                folder = FileHelper.getFilesDir(FileHelper.FileType.IMAGE, mSavePath);

                break;

            default:

                bitmap = ImageUtil.getBitmap(data);
                bitmap = ImageUtil.rotateImage(bitmap, 90);
                bitmap = ImageUtil.getCroppedBitmap(bitmap, 180, false);

                Bitmap croppedGrayBitmap = getGrayscale(bitmap);
                int[] brightnessValues = getContrast(croppedGrayBitmap);

                int blackIndex = 0;
                for (int i = 0; i < brightnessValues.length; i++) {
                    if (brightnessValues[blackIndex] > brightnessValues[i]) {
                        blackIndex = i;
                    }
                }

                int whiteIndex = 0;
                for (int i = 0; i < brightnessValues.length; i++) {
                    if (brightnessValues[whiteIndex] < brightnessValues[i]) {
                        whiteIndex = i;
                    }
                }

                fileName = date + "_" + blackIndex + "_" + whiteIndex + "_" + batteryPercent;

                folder = FileHelper.getFilesDir(FileHelper.FileType.IMAGE, mSavePath);

                break;
        }


        File photo = new File(folder, fileName + ".jpg");

        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());

            fos.write(data);
            fos.close();
        } catch (Exception ignored) {

        }

        Intent intent = new Intent("custom-event-name");
        intent.putExtra("savePath", mSavePath);
        intent.putExtra("uuid", testInfo.getUuid().get(0));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private Bitmap getGrayscale(Bitmap src) {

        //Custom color matrix to convert to GrayScale
        float[] matrix = new float[]{
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0,};

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(),
                src.getHeight(),
                src.getConfig());

        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);

        return dest;
    }

    private int[] getContrast(Bitmap image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int posX1 = width / 4;
        int posX2 = posX1 * 3;

        int posY1 = height / 4;
        int posY2 = posY1 * 3;


        int pixel1 = ColorUtil.getBrightness(image.getPixel(posX1, posY1));
        int pixel2 = ColorUtil.getBrightness(image.getPixel(posX2, posY1));
        int pixel3 = ColorUtil.getBrightness(image.getPixel(posX1, posY2));
        int pixel4 = ColorUtil.getBrightness(image.getPixel(posX2, posY2));

        return new int[]{pixel1, pixel2, pixel3, pixel4};

    }


    /*
 * This handler will be passed to UsbService.
 * Data received from serial port is displayed through this handler
 */
    private static class MyHandler extends Handler {
        private final WeakReference<ExternalCameraActivity> mActivity;

        MyHandler(ExternalCameraActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    ExternalCameraActivity colorimetryLiquidActivity = mActivity.get();
                    if (colorimetryLiquidActivity != null) {

                        colorimetryLiquidActivity.mReceivedData += data;
                        if (colorimetryLiquidActivity.mReceivedData.contains("\r\n")) {
                            colorimetryLiquidActivity.displayResult(colorimetryLiquidActivity.mReceivedData);
                            colorimetryLiquidActivity.mReceivedData = "";
                        }

                    }
                    break;
            }
        }
    }
}