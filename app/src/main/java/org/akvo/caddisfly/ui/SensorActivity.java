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
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.akvo.caddisfly.R;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SensorActivity extends ActionBarActivity {

    private static UsbSerialPort sPort = null;
    private final String TAG = SensorActivity.class.getSimpleName();
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped. " + e.getMessage());
                }

                @Override
                public void onNewData(final byte[] data) {
                    SensorActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SensorActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    String finalResult = "";
    String ec25Value = "";
    String ecValue = "";
    String temperature = "";
    private TextView mResultTextView;
    private Button mOkButton;
    private UsbManager mUsbManager;
    private TextView mTitleTextView;
    private SerialInputOutputManager mSerialIoManager;
    private RelativeLayout mResultLayout;
    private RelativeLayout mConnectionLayout;
    private TextView mTemperatureTextView;

    public static byte[] stringToBytesASCII(String str) {
        char[] buffer = str.toCharArray();
        byte[] b = new byte[buffer.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) buffer[i];
        }
        return b;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_logo);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mResultTextView = (TextView) findViewById(R.id.resultTextView);
        mTemperatureTextView = (TextView) findViewById(R.id.temperatureTextView);

        mOkButton = (Button) findViewById(R.id.okButton);
        mOkButton.setVisibility(View.INVISIBLE);

        mConnectionLayout = (RelativeLayout) findViewById(R.id.connectionLayout);
        mResultLayout = (RelativeLayout) findViewById(R.id.resultLayout);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getIntent());
                //intent.putExtra("result", finalResult);
                //intent.putExtra("questionId", mQuestionId);
                intent.putExtra("response", ec25Value);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }

    private void refreshDeviceList() {

        new AsyncTask<Void, Void, UsbSerialPort>() {
            @Override
            protected UsbSerialPort doInBackground(Void... params) {
                Log.d(TAG, "Refreshing device list ...");
                SystemClock.sleep(100);

                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

                for (final UsbSerialDriver driver : drivers) {
                    return driver.getPorts().get(0);
//                    Log.d(TAG, String.format("+ %s: %s port%s",
//                            driver, ports.size(), ports.size() == 1 ? "" : "s"));
                }
                return null;
            }

            @Override
            protected void onPostExecute(UsbSerialPort result) {
                sPort = result;
                openConnection();
            }

        }.execute((Void) null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDeviceList();
    }

    private void openConnection() {
        Log.d(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            mResultLayout.setVisibility(View.GONE);
            mConnectionLayout.setVisibility(View.VISIBLE);
            Log.e(TAG, "No serial device");
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 2000);

            //mTitleTextView.setText("No serial device.");
        } else {

            mResultLayout.setVisibility(View.VISIBLE);
            mConnectionLayout.setVisibility(View.GONE);
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                //mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setDTR(true);
                sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                byte[] r = stringToBytesASCII("r");
                try {
                    sPort.write(r, 2000);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                //mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            //mResultTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        String result = new String(data);
        finalResult += result;
        final String message = "Read " + data.length + " bytes: \n"
                + finalResult + "\n\n";
        Log.i(TAG, message);
        if (!finalResult.equals("")) {
            String[] resultArray = finalResult.trim().split(",");
            Log.d(TAG, finalResult);
            Log.d(TAG, resultArray.length + "");

            if (resultArray.length == 3) {
                temperature = resultArray[0];
                ecValue = resultArray[1];
                ec25Value = resultArray[2];
                mResultTextView.setText(ec25Value);
                mTemperatureTextView.setText("EC at " + temperature + " centigrade is " + ecValue);
                Log.d(TAG, ec25Value);
                mOkButton.setVisibility(View.VISIBLE);
            }
        }

    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(getIntent());
//        this.setResult(Activity.RESULT_CANCELED, intent);
//        finish();
//    }


}

//                    if (!data.equals("")) {
//
//                        String[] result = data.trim().split(",");
//
//                        resultReceived = true;
//                        usb.stop();
//                        Message msg2 = new Message();
//                        msg2.obj = data;
//                        mHandler.sendMessage(msg2);
//
//                        // long crc;
//                        try {
//                            if (result.length == 3) {
//                                String temperature = result[0];
//                                String ecValue = result[1];
//                                String ec25Value = result[2];
//                                //crc = Long.parseLong(result[3]);
//
//                                //CRC32 crc32 = new CRC32();
//                                //crc32.update((temperature + "," + ecValue + "," + ec25Value).getBytes());
//
//                                //if (crc == crc32.getValue()) {
//                                //resultReceived = true;
//                                //usb.stop();
//                                Message msg = new Message();
//                                ec25Value = ec25Value.replaceAll("[^\\d.]", "");
//                                msg.obj = String.valueOf(ec25Value);
//                                mHandler.sendMessage(msg);
//                                //}
//                            }
//
//
//
//    void sendResult(String result) {
//        currentMsg = new Message();
//        double resultDouble = Double.parseDouble(result);
//        currentMsg.getData().putDouble(Config.RESULT_VALUE_KEY, resultDouble);
//    }


