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

package org.akvo.caddisfly.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;

public class FtdiSerial {

    public static final int readLength = 512;
    private static final String ACTION_USB_PERMISSION = "org.akvo.caddisfly.USB_PERMISSION";
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int DEFAULT_BUFFER_SIZE = 1028;
    private static final int REQUEST_DELAY = 4000;
    private static final int INITIAL_DELAY = 1000;
    // original ///////////////////////////////
    private final StringBuilder mReadData = new StringBuilder();
    public int iavailable = 0;
    public boolean bReadThreadGoing = false;
    public readThread read_thread;
    Toast debugToast;
    D2xxManager ftdid2xx;
    FT_Device ftDev = null;
    int DevCount = -1;
    int currentIndex = -1;
    int openIndex = 0;
    /*graphical objects*/
    ArrayAdapter<CharSequence> portAdapter;
    /*local variables*/
    int baudRate; /*baud rate*/
    byte stopBit; /*1:1stop bits, 2:2 stop bits*/
    byte dataBit; /*8:8bit, 7: 7bit*/
    byte parity;  /* 0: none, 1: odd, 2: even, 3: mark, 4: space*/
    byte flowControl; /*0:none, 1: flow control(CTS,RTS)*/
    int portNumber; /*port number*/
    ArrayList<CharSequence> portNumberList;
    byte[] readData;
    char[] readDataToText;
    boolean uart_configured = false;
    private String mEc25Value = "";
    private String mTemperature = "";
    private TextView mResultTextView;
    private TextView mTemperatureTextView;
    private TextView mEcValueTextView;
    private Button mOkButton;
    private LinearLayout mConnectionLayout;
    private LinearLayout mResultLayout;
    private ProgressWheel mProgressBar;
    private boolean firstResultIgnored = false;
    private ImageView mTemperatureImageView;
    private TextView mUnitsTextView;
    private Context mContext;
    private String mResult = "";
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (iavailable > 0) {
                mResult += String.copyValueOf(readDataToText, 0, iavailable);
                if (mResult.split(",").length > 3) {

                }
                //Toast.makeext(mContext, String.copyValueOf(readDataToText, 0, iavailable), Toast.LENGTH_LONG).show();
            }
        }
    };
    private int delay = INITIAL_DELAY;
    private boolean mRunLoop;
    private final Runnable mCommunicate = new Runnable() {
        @Override
        public void run() {
            while (mRunLoop) {
                try {
                    Thread.sleep(delay);
                } catch (final Exception e) {
                }

                SendMessage();
                delay = REQUEST_DELAY;
            }
        }
    };

    public FtdiSerial(Context context) {
        mContext = context;

        try {
            ftdid2xx = D2xxManager.getInstance(mContext);
        } catch (D2xxManager.D2xxException ignored) {
        }

        readData = new byte[readLength];
        readDataToText = new char[readLength];

        /* by default it is 9600 */
        baudRate = 9600;

        stopBit = 1;

        dataBit = 8;

        parity = 0;

        flowControl = 0;

        portNumber = 1;

    }


    public void notifyUSBDeviceAttach() {
        createDeviceList();
    }

    public void notifyUSBDeviceDetach() {
        disconnect();
    }

    public void createDeviceList() {
        int tempDevCount = ftdid2xx.createDeviceInfoList(mContext);
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

    public void disconnect() {
        DevCount = -1;
        currentIndex = -1;
        bReadThreadGoing = false;
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ftDev != null) {
            synchronized (ftDev) {
                if (ftDev.isOpen()) {
                    ftDev.close();
                }
            }
        }
    }

    public void connect() {
        int tmpProtNumber = openIndex + 1;

        if (currentIndex != openIndex) {
            if (null == ftDev) {
                ftDev = ftdid2xx.openByIndex(mContext, openIndex);
            } else {
                synchronized (ftDev) {
                    ftDev = ftdid2xx.openByIndex(mContext, openIndex);
                }
            }
            uart_configured = false;
        } else {
            //Toast.makeText(this, "Device port " + tmpProtNumber + " is already opened", Toast.LENGTH_LONG).show();
            return;
        }

        if (ftDev == null) {
            //Toast.makeText(this, "open device port(" + tmpProtNumber + ") NG, ftDev == null", Toast.LENGTH_LONG).show();
            return;
        }

        if (ftDev.isOpen()) {
            currentIndex = openIndex;
            //Toast.makeText(this, "open device port(" + tmpProtNumber + ") OK", Toast.LENGTH_SHORT).show();

            if (!bReadThreadGoing) {
                read_thread = new readThread(handler);
                read_thread.start();
                bReadThreadGoing = true;
            }
        } else {
            //Toast.makeText(this, "open device port(" + tmpProtNumber + ") NG", Toast.LENGTH_LONG).show();
            //Toast.makeText(this, "Need to get permission!", Toast.LENGTH_SHORT).show();
        }
    }

    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (!ftDev.isOpen()) {
            Log.e("j2xx", "SetConfig: device not open");
            return;
        }

        // configure our port
        // reset to UART mode for 232 devices
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

        // TODO : flow ctrl: XOFF/XOM
        // TODO : flow ctrl: XOFF/XOM
        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

        uart_configured = true;
        //Toast.makeText(this, "Config done", Toast.LENGTH_SHORT).show();
    }

    public void SendMessage() {
        if (ftDev == null || !ftDev.isOpen()) {
            Log.e("j2xx", "SendMessage: device not open");
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

        //mResultLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mConnectionLayout.setVisibility(View.GONE);
        //unregisterReceiver(mUsbReceiver);
    }

    private class readThread extends Thread {
        Handler mHandler;

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

                synchronized (ftDev) {
                    iavailable = ftDev.getQueueStatus();
                    if (iavailable > 0) {

                        if (iavailable > readLength) {
                            iavailable = readLength;
                        }

                        ftDev.read(readData, iavailable);
                        for (i = 0; i < iavailable; i++) {
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