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

package org.akvo.caddisfly.sensor.ec;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Set;

public class SensorActivity extends BaseActivity {

    //private static final String DEBUG_TAG = "SensorActivity";
    private final StringBuilder mReadData = new StringBuilder();
    private final Handler handler = new Handler();
    private TestInfo mCurrentTestInfo;
    private Toast debugToast;
    private String mEc25Value = "";
    private String mTemperature = "";
    private boolean mIsInternal = false;
    private LinearLayout layoutResult;
    private ProgressBar progressWait;
    private TextView textResult;
    private TextView textTemperature;
    private TextView textUnit;
    private Button buttonAcceptResult;
    private TextView textSubtitle;
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
    private ImageView imageUsbConnection;
    // Notifications from UsbService will be received here.
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)) // USB PERMISSION NOT GRANTED
            {
                Toast.makeText(arg0, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                displayNotConnectedView();
            } else if (arg1.getAction().equals(UsbService.ACTION_NO_USB)) // NO USB CONNECTED
            {
                displayNotConnectedView();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_DISCONNECTED)) // USB DISCONNECTED
            {
                Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
                displayNotConnectedView();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_NOT_SUPPORTED)) // USB NOT SUPPORTED
            {
                Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
                displayNotConnectedView();
            }
        }
    };

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestResult();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService

        // Start UsbService(if it was not started before) and Bind it
        startService(UsbService.class, usbConnection, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle("Sensor");
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
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

        handler.postDelayed(runnable, 100);
    }

    private void requestResult() {
        String data = "r\r\n";
        if (usbService != null && usbService.isUsbConnected()) {
            // if UsbService was correctly bound, Send data
            usbService.write(data.getBytes());
        } else {
            displayNotConnectedView();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensor);

        final Intent intent = getIntent();
        final String cadUuid = intent.getExtras().getString(SensorConstants.RESOURCE_ID);
        mIsInternal = intent.getBooleanExtra("internal", false);
        mHandler = new MyHandler(this);

        textResult = (TextView) findViewById(R.id.textResult);
        textTemperature = (TextView) findViewById(R.id.textTemperature);
        progressWait = (ProgressBar) findViewById(R.id.progressWait);
        textUnit = (TextView) findViewById(R.id.textName);
        TextView textUnit2 = (TextView) findViewById(R.id.textUnit2);
        textSubtitle = (TextView) findViewById(R.id.textSubtitle);
        imageUsbConnection = (ImageView) findViewById(R.id.imageUsbConnection);

        mCurrentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        buttonAcceptResult = (Button) findViewById(R.id.buttonAcceptResult);
        buttonAcceptResult.setVisibility(View.INVISIBLE);
        buttonAcceptResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Build the result json to be returned
                TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
                JSONObject resultJson = new JSONObject();
                JSONArray resultsJsonArray = new JSONArray();

                Intent resultIntent = new Intent(intent);

                if (cadUuid != null) {
                    try {
                        for (TestInfo.SubTest subTest : testInfo.getSubTests()) {
                            JSONObject subTestJson = new JSONObject();
                            subTestJson.put(SensorConstants.NAME, subTest.getDesc());
                            subTestJson.put(SensorConstants.UNIT, subTest.getUnit());
                            subTestJson.put(SensorConstants.ID, subTest.getId());
                            switch (subTest.getId()) {
                                case 1:
                                    subTestJson.put(SensorConstants.VALUE, mEc25Value);
                                    break;
                                case 2:
                                    subTestJson.put(SensorConstants.VALUE, mTemperature);
                                    break;
                            }
                            resultsJsonArray.put(subTestJson);
                        }

                        resultJson.put(SensorConstants.RESULT, resultsJsonArray);
                        resultJson.put(SensorConstants.TYPE, SensorConstants.TYPE_NAME);
                        resultJson.put(SensorConstants.NAME, testInfo.getName());
                        resultJson.put(SensorConstants.UUID, testInfo.getUuid());

                        // Add app details to the result
                        resultJson.put(SensorConstants.APP, TestConfigHelper.getAppDetails());

                        // Add standard diagnostic details to the result
                        resultJson.put(SensorConstants.DEVICE, TestConfigHelper.getDeviceDetails());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Add standard diagnostic details to the result
                    resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
                } else {
                    // TODO: Remove this when obsolete
                    // Backward compatibility. Return plain text result
                    if (mCurrentTestInfo != null && mCurrentTestInfo.getCode().equals("TEMPE")) {
                        resultIntent.putExtra(SensorConstants.RESPONSE, mTemperature);
                    } else {
                        resultIntent.putExtra(SensorConstants.RESPONSE, mEc25Value);
                    }
                }

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        layoutResult = (LinearLayout) findViewById(R.id.layoutResult);

        Configuration config = getResources().getConfiguration();
        if (mCurrentTestInfo != null && !mCurrentTestInfo.getName(config.locale.getLanguage()).isEmpty()) {
            ((TextView) findViewById(R.id.textTitle)).setText(
                    mCurrentTestInfo.getName(config.locale.getLanguage()));
        }

        if (mIsInternal) {
            textTemperature.setVisibility(View.VISIBLE);
            textUnit2.setVisibility(View.VISIBLE);
        } else if (cadUuid == null) {
            textTemperature.setVisibility(View.GONE);
            textUnit2.setVisibility(View.GONE);
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

    private void displayNotConnectedView() {
        mReadData.setLength(0);
        progressWait.setVisibility(View.GONE);
        layoutResult.animate().alpha(0f).setDuration(500);
        imageUsbConnection.animate().alpha(0.9f).setDuration(1000);
        buttonAcceptResult.setVisibility(View.GONE);
        textSubtitle.setText(R.string.deviceConnectSensor);

        if (!mIsInternal) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 3000);
        }
    }

    private void displayResult(String value) {

        if (AppPreferences.getShowDebugMessages()) {
            Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        }

        if (value.startsWith(".") || value.startsWith(",")) {
            return;
        }

        Configuration config = getResources().getConfiguration();

        value = value.trim();
        if (value.contains("\r\n")) {
            String[] values = value.split("\r\n");
            if (values.length > 0) {
                value = values[1];
            }
        }

        final String result = value;
        if (!result.isEmpty()) {

            String[] resultArray = result.split(",");

            if (AppPreferences.getShowDebugMessages()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (debugToast == null) {
                            debugToast = Toast.makeText(getBaseContext(), result.trim(), Toast.LENGTH_LONG);
                        }
                        debugToast.setText(result.trim());
                        debugToast.show();
                    }
                });
            }

            if (resultArray.length > 1) {

                for (int i = 0; i < resultArray.length; i++) {
                    resultArray[i] = resultArray[i].trim();
                }

                double temperature;
                long ec25Value;

                try {
                    temperature = Double.parseDouble(resultArray[0]);
                    mTemperature = String.format(Locale.US, "%.1f", temperature);
                } catch (NumberFormatException e) {
                    mTemperature = "";
                }

                try {
                    ec25Value = Math.round(Double.parseDouble(resultArray[1]));
                } catch (NumberFormatException e) {
                    ec25Value = -1;
                }

                if (ec25Value > -1) {
                    mEc25Value = Long.toString(ec25Value);
                } else {
                    mEc25Value = "";
                }

                if (mCurrentTestInfo != null && mCurrentTestInfo.getCode().equals("TEMPE")) {
                    textSubtitle.setText(R.string.sensorConnected);
                    textResult.setText(mTemperature);
                    if (mCurrentTestInfo != null && !mCurrentTestInfo.getName(config.locale.getLanguage()).isEmpty()) {
                        textUnit.setText(mCurrentTestInfo.getUnit());
                    }
                    buttonAcceptResult.setVisibility(View.VISIBLE);
                    progressWait.setVisibility(View.GONE);
                } else {
                    if (mEc25Value.isEmpty()) {
                        textResult.setText("");
                        progressWait.setVisibility(View.VISIBLE);
                        if (mCurrentTestInfo != null && !mCurrentTestInfo.getName(config.locale.getLanguage()).isEmpty()) {
                            textUnit.setText(mCurrentTestInfo.getUnit());
                        }
                        buttonAcceptResult.setVisibility(View.GONE);
                        textSubtitle.setText(R.string.dipSensorInSample);
                    } else {
                        textResult.setText(String.format(Locale.US, "%d", ec25Value));
                        progressWait.setVisibility(View.GONE);
                        buttonAcceptResult.setVisibility(View.VISIBLE);
                        textSubtitle.setText(R.string.sensorConnected);
                        if (mCurrentTestInfo != null && !mCurrentTestInfo.getName(config.locale.getLanguage()).isEmpty()) {
                            textUnit.setText(mCurrentTestInfo.getUnit());
                        }
                    }
                }

                textTemperature.setText(mTemperature);

                if (mIsInternal) {
                    buttonAcceptResult.setVisibility(View.GONE);
                }

                layoutResult.animate().alpha(1f).setDuration(500);
                imageUsbConnection.animate().alpha(0f).setDuration(500);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_back_out, R.anim.slide_back_in);
    }

    /*
     * This handler will be passed to UsbService.
     * Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<SensorActivity> mActivity;

        public MyHandler(SensorActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    SensorActivity sensorActivity = mActivity.get();
                    if (sensorActivity != null) {

                        sensorActivity.mReceivedData += data;
                        if (sensorActivity.mReceivedData.contains("\r\n")) {
                            sensorActivity.displayResult(sensorActivity.mReceivedData);
                            sensorActivity.mReceivedData = "";
                        }
                    }
                    break;
            }
        }
    }
}