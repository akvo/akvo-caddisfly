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

package org.akvo.caddisfly.sensor.usb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.ActivitySensorBinding;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.usb.UsbService;
import org.akvo.caddisfly.util.StringUtil;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * The activity that displays the results for the connected sensor.
 */
public class SensorActivity extends BaseActivity {

    private static final int REQUEST_DELAY_MILLIS = 1500;
    private static final int IDENTIFY_DELAY_MILLIS = 300;
    private static final int ANIMATION_DURATION = 500;
    private static final int ANIMATION_DURATION_LONG = 1500;
    private static final String LINE_FEED = "\r\n";
    private static final String EMPTY_STRING = "";

    private final Handler handler = new Handler();
    private final SparseArray<String> results = new SparseArray<>();
    private ActivitySensorBinding b;
    // Notifications from UsbService will be received here.
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (UsbService.ACTION_USB_PERMISSION_NOT_GRANTED.equals(arg1.getAction())) {
                Toast.makeText(arg0, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                displayNotConnectedView();
            } else if (UsbService.ACTION_NO_USB.equals(arg1.getAction())) {
                displayNotConnectedView();
            } else if (UsbService.ACTION_USB_DISCONNECTED.equals(arg1.getAction())) {
                Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
                displayNotConnectedView();
            } else if (UsbService.ACTION_USB_NOT_SUPPORTED.equals(arg1.getAction())) {
                Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
                displayNotConnectedView();
            }
        }
    };
    private AlertDialog alertDialog;
    private TestInfo testInfo;
    private Toast debugToast;
    private boolean mIsInternal = false;
    private String mReceivedData = EMPTY_STRING;
    private UsbService usbService;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
            if (usbService.isUsbConnected()) {
                b.imageUsbConnection.animate().alpha(0f).setDuration(ANIMATION_DURATION);
                b.progressWait.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    private int identityCheck = 0;
    private int deviceStatus = 0;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (deviceStatus == 1) {
                requestResult();
                handler.postDelayed(this, REQUEST_DELAY_MILLIS);
            } else {
                handler.postDelayed(validateDeviceRunnable, IDENTIFY_DELAY_MILLIS * 2);
            }
        }
    };
    private final Runnable validateDeviceRunnable = new Runnable() {
        @Override
        public void run() {
            String data = "device" + LINE_FEED;
            if (usbService != null && usbService.isUsbConnected()) {
                // if UsbService was correctly bound, Send data
                usbService.write(data.getBytes(StandardCharsets.UTF_8));
            } else {
                displayNotConnectedView();
            }

            switch (deviceStatus) {

                case 0:
                    handler.postDelayed(this, IDENTIFY_DELAY_MILLIS);
                    break;
                case 1:
                    handler.postDelayed(runnable, IDENTIFY_DELAY_MILLIS);
                    alertDialog.dismiss();
                    break;
                default:
                    b.progressWait.setVisibility(View.GONE);
                    if (!alertDialog.isShowing()) {
                        alertDialog.show();
                    }
                    handler.postDelayed(runnable, IDENTIFY_DELAY_MILLIS);
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        deviceStatus = 0;
        identityCheck = 0;

        setFilters();  // Start listening notifications from UsbService

        // Start UsbService(if it was not started before) and Bind it
        startService(UsbService.class, usbConnection, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.sensor);
    }

    @Override
    public void onPause() {
        super.onPause();
        deviceStatus = 0;
        identityCheck = 0;
        handler.removeCallbacks(runnable);
        handler.removeCallbacks(validateDeviceRunnable);
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

        alertDialog.dismiss();

        handler.postDelayed(validateDeviceRunnable, IDENTIFY_DELAY_MILLIS * 2);
    }

    private void requestResult() {
        String data = "r" + LINE_FEED;
        if (usbService != null && usbService.isUsbConnected()) {
            // if UsbService was correctly bound, Send data
            usbService.write(data.getBytes(StandardCharsets.UTF_8));
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

        b = DataBindingUtil.setContentView(this, R.layout.activity_sensor);

        final TestInfoViewModel viewModel =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        //        model.setTest(testInfo);

        b.setTestInfoViewModel(viewModel);

        final Intent intent = getIntent();

        mIsInternal = intent.getBooleanExtra("internal", false);
        mHandler = new MyHandler(this);

        b.textSubtitle.setText(R.string.deviceConnectSensor);

        b.buttonAcceptResult.setVisibility(View.INVISIBLE);

        if (testInfo != null && testInfo.getUuid() != null) {
            ((TextView) findViewById(R.id.textTitle)).setText(
                    testInfo.getName());

            String message = String.format("%s<br/><br/>%s", getString(R.string.expectedDeviceNotFound),
                    getString(R.string.connectCorrectSensor, testInfo.getName()));
            Spanned spanned = StringUtil.fromHtml(message);

            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.incorrectDevice)
                    .setMessage(spanned)
                    .setCancelable(false);

            builder.setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                dialogInterface.dismiss();
                finish();
            });

            alertDialog = builder.create();
        }
        b.progressWait.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayNotConnectedView() {
        if (!isFinishing()) {
            b.progressWait.setVisibility(View.GONE);
            b.layoutResult.animate().alpha(0f).setDuration(ANIMATION_DURATION);
            b.imageUsbConnection.animate().alpha(1f).setDuration(ANIMATION_DURATION_LONG);
            b.buttonAcceptResult.setVisibility(View.GONE);
            b.textSubtitle.setText(R.string.deviceConnectSensor);
            b.textSubtitle2.setText("");
        }
    }

    @SuppressLint("ShowToast")
    private void displayResult(String value) {

        String tempValue = value;

        if (AppPreferences.getShowDebugInfo()) {
            Toast.makeText(this, tempValue, Toast.LENGTH_SHORT).show();
        }

        // reject value if corrupt
        if (tempValue.startsWith(".") || tempValue.startsWith(",")) {
            return;
        }

        // clean up data
        tempValue = tempValue.trim();
        if (tempValue.contains(LINE_FEED)) {
            String[] values = tempValue.split(LINE_FEED);
            if (values.length > 0) {
                tempValue = values[1];
            }
        }

        tempValue = tempValue.trim();
        if (!tempValue.isEmpty()) {

            // if device not yet validated then check if device id is ok
            if (deviceStatus == 0) {
                if (tempValue.contains(" ")) {
                    if (tempValue.startsWith(testInfo.getDeviceId())) {

                        Pattern p = Pattern.compile(".*\\s(\\d+)");
                        deviceStatus = 1;
                        Matcher m = p.matcher(tempValue);
                        if (m.matches()) {
                            b.textSubtitle.setText(String.format("Sensor ID: %s", m.group(1)));
                        } else {
                            b.textSubtitle.setText(tempValue);
                        }

                        b.progressWait.setVisibility(View.VISIBLE);
                        hideNotConnectedView();
                        deviceStatus = 1;
                    } else {
                        if (identityCheck > 1) {
                            deviceStatus = 2;
                        }
                        identityCheck++;
                    }
                }
                return;
            }

            if (deviceStatus == 2) {
                return;
            } else {
                alertDialog.dismiss();
            }

            String[] resultArray = tempValue.split(",");

            showDebugInfo(tempValue);

            if (resultArray.length == testInfo.getResponseFormat().split(",").length) {

                // use the response format to display the results in test id order
                String responseFormat = testInfo.getResponseFormat().replace("$", EMPTY_STRING)
                        .replace(" ", EMPTY_STRING).replace(",", EMPTY_STRING).trim();
                results.clear();

                for (int i = 0; i < resultArray.length; i++) {
                    resultArray[i] = resultArray[i].trim();
                    try {

                        double result = Double.parseDouble(resultArray[i]);

                        results.put(Integer.parseInt(responseFormat.substring(i, i + 1)), String.valueOf(result));

                    } catch (Exception e) {
                        Timber.e(e);
                        return;
                    }
                }

                // display the results
                showResults();

                b.layoutResult.animate().alpha(1f).setDuration(ANIMATION_DURATION);
                hideNotConnectedView();
            }
        }
    }

    private void showResults() {
        if (testInfo.getResults().size() > 0 && results.size() > 0
                && !results.get(1).equals(EMPTY_STRING)) {
            b.textResult.setText(results.get(1));
            b.textUnit.setText(testInfo.getResults().get(0).getUnit());
            b.textResult.setVisibility(View.VISIBLE);
            b.textUnit.setVisibility(View.VISIBLE);
            b.progressWait.setVisibility(View.GONE);
            b.buttonAcceptResult.setVisibility(View.VISIBLE);
        } else {
            b.textResult.setText(EMPTY_STRING);
            b.textUnit.setText(EMPTY_STRING);
            b.textResult.setVisibility(View.INVISIBLE);
            b.textUnit.setVisibility(View.INVISIBLE);
            b.progressWait.setVisibility(View.VISIBLE);
            b.buttonAcceptResult.setVisibility(View.GONE);
            b.textSubtitle2.setText(R.string.dipSensorInSample);
        }

        if (testInfo.getResults().size() > 1 && results.size() > 1) {
            b.textResult2.setText(results.get(2));
            b.textUnit2.setText(testInfo.getResults().get(1).getUnit());
            b.textResult2.setVisibility(View.VISIBLE);
            b.textUnit2.setVisibility(View.VISIBLE);
        } else {
            b.textResult2.setVisibility(View.GONE);
            b.textUnit2.setVisibility(View.GONE);
        }

        // if test is not via survey then do not show the accept button
        if (mIsInternal) {
            b.buttonAcceptResult.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ShowToast")
    private void showDebugInfo(String tempValue) {
        if (AppPreferences.getShowDebugInfo()) {
            final String finalValue = tempValue;
            runOnUiThread(() -> {
                if (debugToast == null) {
                    debugToast = Toast.makeText(getBaseContext(), finalValue, Toast.LENGTH_LONG);
                }
                debugToast.setText(finalValue);
                debugToast.show();
            });
        }
    }

    private void hideNotConnectedView() {
        b.imageUsbConnection.animate().alpha(0f).setDuration(ANIMATION_DURATION);
    }

    public void onClickAcceptResult(View view) {
        // Build the result json to be returned

        Intent resultIntent = new Intent(getIntent());

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, -1, EMPTY_STRING);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    /*
     * This handler will be passed to UsbService.
     * Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<SensorActivity> mActivity;

        MyHandler(SensorActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UsbService.MESSAGE_FROM_SERIAL_PORT) {
                String data = (String) msg.obj;
                SensorActivity sensorActivity = mActivity.get();
                if (sensorActivity != null) {
                    sensorActivity.mReceivedData += data;
                    if (sensorActivity.mReceivedData.contains(LINE_FEED)) {
                        sensorActivity.displayResult(sensorActivity.mReceivedData);
                        sensorActivity.mReceivedData = EMPTY_STRING;
                    }
                }
            }
        }
    }
}
