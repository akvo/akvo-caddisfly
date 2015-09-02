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

package org.akvo.caddisfly.sensor.ec;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.zip.CRC32;

public class SensorActivity extends BaseActivity {

    private static final int readLength = 512;
    private static final String ACTION_USB_PERMISSION = "org.akvo.caddisfly.USB_PERMISSION";
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int REQUEST_DELAY = 4000;
    private static final int INITIAL_DELAY = 1000;
    private final StringBuilder mReadData = new StringBuilder();
    private final WeakRefHandler handler = new WeakRefHandler(this);
    private int byteCount = 0;
    private boolean bReadThreadGoing = false;
    private Toast debugToast;
    private D2xxManager ftManager;
    private FT_Device ftDev = null;
    private int DevCount = -1;
    private int currentIndex = -1;
    private byte stopBit; /*1:1stop bits, 2:2 stop bits*/
    private byte dataBit; /*8:8bit, 7: 7bit*/
    private byte parity;  /* 0: none, 1: odd, 2: even, 3: mark, 4: space*/
    private byte flowControl; /*0:none, 1: flow control(CTS,RTS)*/
    private byte[] readData;
    private char[] readDataToText;
    private String mEc25Value = "";
    private String mTemperature = "";
    private TextView mResultTextView;
    private TextView mTemperatureTextView;
    private TextView mEcValueTextView;
    private Button mOkButton;
    private LinearLayout mConnectionLayout;
    private LinearLayout mResultLayout;
    private ProgressBar mProgressBar;
    private ImageView mTemperatureImageView;
    private TextView mUnitsTextView;

    //http://developer.android.com/guide/topics/connectivity/usb/host.html
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    notifyUSBDeviceAttach();
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    notifyUSBDeviceDetach();
                    break;
                case ACTION_USB_PERMISSION:
                    break;
            }
        }
    };
    private String mResult = "";
    private int delay = INITIAL_DELAY;
    private boolean mRunLoop;
    private final Runnable mCommunicate = new Runnable() {
        @Override
        public void run() {
            while (mRunLoop) {
                try {
                    Thread.sleep(delay);
                } catch (final Exception ignored) {
                }

                SendMessage();
                delay = REQUEST_DELAY;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ftManager = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }

        setContentView(R.layout.activity_sensor);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.ic_actionbar_logo);
        }

        mResultTextView = (TextView) findViewById(R.id.textResult);
        mTemperatureTextView = (TextView) findViewById(R.id.textTemperature);
        mEcValueTextView = (TextView) findViewById(R.id.textEcValue);
        mProgressBar = (ProgressBar) findViewById(R.id.progressWait);
        mTemperatureImageView = (ImageView) findViewById(R.id.imageTemperature);
        mUnitsTextView = (TextView) findViewById(R.id.textUnit);

        final TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        Configuration conf = getResources().getConfiguration();

        Button backButton = (Button) findViewById(R.id.buttonOk);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        mTemperatureImageView.setVisibility(View.GONE);
        mUnitsTextView.setVisibility(View.GONE);

        mOkButton = (Button) findViewById(R.id.buttonAcceptResult);
        mOkButton.setVisibility(View.INVISIBLE);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getIntent());
                if (currentTestInfo.getCode().equals("TEMPE")) {
                    intent.putExtra("response", mTemperature);
                } else {
                    intent.putExtra("response", mEc25Value);
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        mConnectionLayout = (LinearLayout) findViewById(R.id.layoutConnection);
        mResultLayout = (LinearLayout) findViewById(R.id.layoutResult);

        if (!currentTestInfo.getName(conf.locale.getLanguage()).isEmpty()) {
            ((TextView) findViewById(R.id.textTitle)).setText(
                    currentTestInfo.getName(conf.locale.getLanguage()));

            mUnitsTextView.setText(currentTestInfo.getUnit());
        }

        //http://developer.android.com/guide/topics/connectivity/usb/host.html
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        readData = new byte[readLength];
        readDataToText = new char[readLength];

        stopBit = 1;
        dataBit = 8;
        parity = 0;
        flowControl = 0;
    }

    @Override
    public void onStart() {
        super.onStart();
        createDeviceList();
    }

    @Override
    public void onStop() {
        disconnect();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mRunLoop = false;
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private void notifyUSBDeviceAttach() {
        createDeviceList();
    }

    private void notifyUSBDeviceDetach() {
        disconnect();
        displayNotConnectedView();
    }

    private void createDeviceList() {
        int tempDevCount = ftManager.createDeviceInfoList(this);
        if (tempDevCount > 0) {
            displayNotConnectedView();
            if (DevCount != tempDevCount) {
                DevCount = tempDevCount;
                displayConnectedView();
            }
        } else {
            displayNotConnectedView();
            DevCount = -1;
            currentIndex = -1;
        }
    }

    private void disconnect() {
        DevCount = -1;
        currentIndex = -1;
        bReadThreadGoing = false;
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ftDev != null) {
            synchronized (this) {
                if (ftDev.isOpen()) {
                    ftDev.close();
                }
            }
        }
    }

    private void connect() {

        int openIndex = 0;
        if (currentIndex != openIndex) {
            if (null == ftDev) {
                ftDev = ftManager.openByIndex(this, openIndex);
            } else {
                synchronized (this) {
                    ftDev = ftManager.openByIndex(this, openIndex);
                }
            }
        } else {
            return;
        }

        if (ftDev == null) {
            return;
        }

        if (ftDev.isOpen()) {
            currentIndex = openIndex;

            if (!bReadThreadGoing) {
                readThread read_thread = new readThread(handler);
                read_thread.start();
                bReadThreadGoing = true;
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (ftDev == null || !ftDev.isOpen()) {
            //device not open
            return;
        }

        // configure our port
        // reset mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

        //Toast.makeText(this, "AppConfig done", Toast.LENGTH_SHORT).show();
    }

    private void SendMessage() {
        if (ftDev == null || !ftDev.isOpen()) {
            //device not open
            return;
        }

        ftDev.setLatencyTimer((byte) 16);
//		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

        String writeData = "r";
        byte[] OutData = writeData.getBytes();
        ftDev.write(OutData, writeData.length());
    }

    /**
     * Hot plug for plug in solution
     * This is workaround before android 4.2 . Because BroadcastReceiver can not
     * receive ACTION_USB_DEVICE_ATTACHED broadcast
     */
    @Override
    public void onResume() {
        super.onResume();
        DevCount = 0;
        createDeviceList();
        if (DevCount > 0) {
            connect();
            SetConfig(DEFAULT_BAUD_RATE, dataBit, stopBit, parity, flowControl);
        }
        mRunLoop = true;
        new Thread(mCommunicate).start();
    }

    private void displayNotConnectedView() {
        mReadData.setLength(0);
        mResultLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mTemperatureImageView.setVisibility(View.GONE);
        mUnitsTextView.setVisibility(View.GONE);
        mConnectionLayout.setVisibility(View.VISIBLE);
        mOkButton.setVisibility(View.GONE);
    }

    private void displayConnectedView() {

        mTemperatureImageView.setVisibility(View.GONE);
        mUnitsTextView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mConnectionLayout.setVisibility(View.GONE);
    }

    private void displayResult(String value) {

        //Toast.makeText(getBaseContext(), result.trim(), Toast.LENGTH_LONG).show();
        String newline = System.getProperty("line.separator");

        value = value.trim();
        if (value.contains(newline)) {
            String[] values = value.split(newline);
            if (values.length > 0) {
                value = values[1];
            }
        }

        final String result = value;
        if (!result.isEmpty()) {

            String[] resultArray = result.split(",");

            if (AppPreferences.getShowDebugMessages(this)) {
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

            if (resultArray.length > 3) {

                for (int i = 0; i < resultArray.length; i++) {
                    resultArray[i] = resultArray[i].trim();
                }

                mTemperature = resultArray[0];
                String mEcValue = resultArray[1];
                mEc25Value = resultArray[2];
                String crc = resultArray[3].trim();

                CRC32 crc32 = new CRC32();
                crc32.update((mTemperature + "," + mEcValue + "," + mEc25Value).getBytes());
                String crcValue = Long.toHexString(crc32.getValue());

                if (crc.equals(crcValue)) {


                    double temperature = Double.parseDouble(mTemperature);
                    mTemperature = String.format("%.1f\u00B0C", temperature);
                    mTemperature = mTemperature.replace(".0", "");
                    long ec25Value = Math.round(Double.parseDouble(mEc25Value));
                    if (ec25Value > -1) {
                        mEc25Value = Long.toString(ec25Value);
                    } else {
                        mEc25Value = "";
                    }
                    double ecValue = Math.round(Double.parseDouble(mEcValue));

                    mTemperatureTextView.setText(mTemperature);
                    if (mEc25Value.isEmpty()) {
                        mEcValueTextView.setText("");
                        mResultTextView.setText("");
                        mProgressBar.setVisibility(View.VISIBLE);
                    } else {
                        mResultTextView.setText(String.format("%d", ec25Value));
                        mEcValueTextView.setText(String.format(getString(R.string.electricalConductivityResult), String.format("%.0f", ecValue)));
                        mProgressBar.setVisibility(View.GONE);
                    }

                    mResultLayout.setVisibility(View.VISIBLE);
                    mConnectionLayout.setVisibility(View.GONE);
                    mOkButton.setVisibility(View.VISIBLE);
                    mTemperatureImageView.setVisibility(View.VISIBLE);
                    mUnitsTextView.setVisibility(View.VISIBLE);
                }

            }
        }
        mResult = "";
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

    private static class WeakRefHandler extends Handler {
        private final WeakReference<SensorActivity> ref;

        public WeakRefHandler(SensorActivity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            SensorActivity f = ref.get();
            if (f.byteCount > 0) {
                f.mResult += String.copyValueOf(f.readDataToText, 0, f.byteCount);
                if (f.mResult.split(",").length > 3) {
                    f.displayResult(f.mResult);
                }
            }
        }
    }

    private class readThread extends Thread {
        final Handler mHandler;

        readThread(Handler h) {
            mHandler = h;
            this.setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            int i;

            while (bReadThreadGoing) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }

                synchronized (this) {
                    byteCount = ftDev.getQueueStatus();
                    if (byteCount > 0) {

                        if (byteCount > readLength) {
                            byteCount = readLength;
                        }

                        ftDev.read(readData, byteCount);
                        for (i = 0; i < byteCount; i++) {
                            readDataToText[i] = (char) readData[i];
                        }
                        Message msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }
    }
}