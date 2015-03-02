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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.util.FtdiSerial;

public class SensorActivity extends ActionBarActivity {

    private static final String ACTION_USB_PERMISSION = "org.akvo.caddisfly.USB_PERMISSION";

    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int DEFAULT_BUFFER_SIZE = 1028;
    private static final int REQUEST_DELAY = 2000;
    private final Handler mHandler = new Handler();
    private final StringBuilder mReadData = new StringBuilder();
    private FtdiSerial mConnection;
    private String ec25Value = "";
    private boolean mRunLoop = false;

    private final Runnable mCommunicate = new Runnable() {
        @Override
        public void run() {
            byte[] dataBuffer = new byte[DEFAULT_BUFFER_SIZE];
            while (mRunLoop) {
                try {
                    Thread.sleep(REQUEST_DELAY);
                    String requestCommand = "r";
                    mConnection.write(requestCommand.getBytes(), requestCommand.length());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int length = mConnection.read(dataBuffer);
                if (length > 0) {
                    for (int i = 0; i < length; ++i) {
                        mReadData.append((char) dataBuffer[i]);
                    }

                    mHandler.post(new Runnable() {
                        public void run() {
                            displayResult(mReadData.toString());
                            mReadData.setLength(0);
                        }
                    });
                }
            }
        }
    };
    //http://developer.android.com/guide/topics/connectivity/usb/host.html
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    if (!mConnection.isOpen()) {
                        Connect();
                    }
                    if (!mRunLoop) {
                        startCommunication();
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    mRunLoop = false;
                    mHandler.post(new Runnable() {
                        public void run() {
                            mResultLayout.setVisibility(View.GONE);
                            mConnectionLayout.setVisibility(View.VISIBLE);
                        }
                    });

                    mConnection.close();
                    break;
                case ACTION_USB_PERMISSION:
                    if (!mConnection.isOpen()) {
                        Connect();
                    }
                    if (!mRunLoop) {
                        startCommunication();
                    }
                    break;
            }
        }
    };
    private TextView mResultTextView;
    //private TextView mTemperatureTextView;
    private Button mOkButton;
    private LinearLayout mConnectionLayout;
    private RelativeLayout mResultLayout;
    private ProgressWheel mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensor);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_logo);

        mResultTextView = (TextView) findViewById(R.id.resultTextView);
        //mTemperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        mProgressBar = (ProgressWheel) findViewById(R.id.progress_wheel);

        MainApp mainApp = (MainApp) getApplicationContext();
        Configuration conf = getResources().getConfiguration();

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        mOkButton = (Button) findViewById(R.id.okButton);
        mOkButton.setVisibility(View.INVISIBLE);
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

        mConnectionLayout = (LinearLayout) findViewById(R.id.connectionLayout);
        mResultLayout = (RelativeLayout) findViewById(R.id.resultLayout);

        ((TextView) findViewById(R.id.titleTextView)).setText(
                mainApp.currentTestInfo.getName(conf.locale.getLanguage()));


        mConnection = new FtdiSerial(this);

        //http://developer.android.com/guide/topics/connectivity/usb/host.html
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        Connect();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Connect();
    }

    private void Connect() {
        if (mConnection != null) {
            mConnection.initialize(DEFAULT_BAUD_RATE, D2xxManager.FT_DATA_BITS_8,
                    D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
            if (mConnection.isOpen() && !mRunLoop) {
                startCommunication();
            } else {
                mResultLayout.setVisibility(View.GONE);
                mConnectionLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startCommunication() {
        mResultLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mConnectionLayout.setVisibility(View.GONE);
        mRunLoop = true;
        new Thread(mCommunicate).start();
    }

    private void displayResult(String result) {

        if (!result.equals("")) {
            String[] resultArray = result.trim().split(",");

            if (resultArray.length == 3) {
                //String temperature = resultArray[0];
                //String ecValue = resultArray[1];
                ec25Value = resultArray[2];

//                CRC32 crc32 = new CRC32();
//                crc32.update((temperature + "," + ecValue + "," + ec25Value).getBytes());
//                crc32.getValue();
//                result += "," + Long.toHexString(crc32.getValue());

                mResultTextView.setText(ec25Value);
                //mTemperatureTextView.setText("EC at " + temperature + " centigrade is " + ecValue);

                mProgressBar.setVisibility(View.GONE);
                mResultLayout.setVisibility(View.VISIBLE);
                mConnectionLayout.setVisibility(View.GONE);
                mOkButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroy() {
        mConnection.close();
        mRunLoop = false;
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }
}
