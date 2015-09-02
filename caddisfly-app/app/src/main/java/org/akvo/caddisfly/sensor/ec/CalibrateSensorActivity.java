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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;

public class CalibrateSensorActivity extends BaseActivity {

    private D2xxManager ftManager;
    private FT_Device ftDev = null;
    private int DevCount = -1;
    private int currentIndex = -1;

    private int baudRate; /*baud rate*/
    private byte stopBit; /*1:1stop bits, 2:2 stop bits*/
    private byte dataBit; /*8:8bit, 7: 7bit*/
    private byte parity;  /* 0: none, 1: odd, 2: even, 3: mark, 4: space*/
    private byte flowControl; /*0:none, 1: flow control(CTS,RTS)*/
    private Context mContext;

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
        super.onDestroy();
    }

    private void createDeviceList() {
        int tempDevCount = ftManager.createDeviceInfoList(this);
        if (tempDevCount > 0) {
            if (DevCount != tempDevCount) {
                DevCount = tempDevCount;
            }
        } else {
            DevCount = -1;
            currentIndex = -1;
        }
    }

    private void disconnect() {
        DevCount = -1;
        currentIndex = -1;
        //bReadThreadGoing = false;
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

        createDeviceList();

        if (DevCount < 1) {
            return;
        }

        int openIndex = 0;
        if (currentIndex != openIndex) {
            if (null == ftDev) {
                ftDev = ftManager.openByIndex(this, openIndex);
            } else {
                synchronized (this) {
                    ftDev = ftManager.openByIndex(this, openIndex);
                }
            }
        } else return;

        if (ftDev == null) {
            return;
        }

        if (ftDev.isOpen()) {
            currentIndex = openIndex;
        }
        SetConfig(baudRate, dataBit, stopBit, parity, flowControl);

    }

    private void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (ftDev == null || !ftDev.isOpen()) {
            //device not open
            return;
        }

        // configure our port
        // reset to mode for 232 devices
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
    }

    private void SendMessage(String data) {
        if (ftDev == null || !ftDev.isOpen()) {
            //device not open
            return;
        }

        ftDev.setLatencyTimer((byte) 16);
//		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

        byte[] OutData = data.getBytes();
        ftDev.write(OutData, data.length());
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
            SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_sensor);

        mContext = this;

        try {
            ftManager = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }

        final ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);

        Button nextButton = (Button) findViewById(R.id.buttonStartCalibrate);
        Button lowConductivityButton = (Button) findViewById(R.id.buttonCalibrateLow);
        Button highConductivityButton = (Button) findViewById(R.id.buttonCalibrateHigh);
        final EditText lowValueEditText = (EditText) findViewById(R.id.editLowValue);
        final EditText highValueEditText = (EditText) findViewById(R.id.editHighValue);

        Configuration conf = getResources().getConfiguration();
        if (!CaddisflyApp.getApp().getCurrentTestInfo().getName(conf.locale.getLanguage()).isEmpty()) {
            ((TextView) findViewById(R.id.textTitle)).setText(
                    CaddisflyApp.getApp().getCurrentTestInfo().getName(conf.locale.getLanguage()));
        }

        baudRate = 9600;

        stopBit = 1;

        dataBit = 8;

        parity = 0;

        flowControl = 0;

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ftDev == null || !ftDev.isOpen()) {
                    connect();
                }
                if (ftDev != null && ftDev.isOpen()) {

                    final ProgressDialog dialog = ProgressDialog.show(mContext,
                            getString(R.string.pleaseWait), getString(R.string.deviceConnecting), true);
                    dialog.setCancelable(false);
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            viewAnimator.showNext();
                        }
                    }, 2000);

                } else {
                    AlertUtil.showMessage(mContext, R.string.sensorNotFound, R.string.deviceConnectSensor);
                }
            }
        });

        lowConductivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validInput(lowValueEditText.getText().toString())) {
                    closeKeyboard(lowValueEditText);
                    final ProgressDialog dialog = ProgressDialog.show(mContext,
                            getString(R.string.pleaseWait), getString(R.string.calibrating), true);
                    dialog.setCancelable(false);
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            String requestCommand = "L," + lowValueEditText.getText();
                            SendMessage(requestCommand);
                            (new Handler()).postDelayed(new Runnable() {
                                public void run() {
                                    dialog.dismiss();
                                    viewAnimator.showNext();
                                }
                            }, 1000);
                        }
                    }, 4000);
                } else {
                    lowValueEditText.setError(getString(R.string.pleaseEnterValue));
                }
            }
        });

        highConductivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validInput(highValueEditText.getText().toString())) {
                    closeKeyboard(highValueEditText);
                    final ProgressDialog dialog = ProgressDialog.show(mContext,
                            getString(R.string.pleaseWait),
                            getString(R.string.calibrating), true);
                    dialog.setCancelable(false);
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            String requestCommand = "H," + highValueEditText.getText();
                            SendMessage(requestCommand);

                            requestCommand = "C";
                            SendMessage(requestCommand);
                            (new Handler()).postDelayed(new Runnable() {
                                public void run() {
                                    dialog.dismiss();
                                    AlertUtil.showAlert(mContext, R.string.calibrationSuccessful,
                                            R.string.sensorCalibrated, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            }, null);
                                }
                            }, 1000);
                        }
                    }, 4000);

                } else {
                    highValueEditText.setError(getString(R.string.pleaseEnterValue));
                }
            }
        });
    }

    private boolean validInput(String input) {
        return input.trim().length() > 0;
    }

    private void closeKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

}
