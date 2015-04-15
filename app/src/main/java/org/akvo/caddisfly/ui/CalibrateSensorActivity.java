package org.akvo.caddisfly.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewAnimator;

import com.ftdi.j2xx.D2xxManager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ApiUtils;
import org.akvo.caddisfly.util.FtdiSerial;

public class CalibrateSensorActivity extends ActionBarActivity {

    private static final String ACTION_USB_PERMISSION = "org.akvo.caddisfly.USB_PERMISSION";
    private static final int DEFAULT_BAUD_RATE = 9600;
    //private static final int DEFAULT_BUFFER_SIZE = 1028;
    //private static final int REQUEST_DELAY = 2000;
    //private final Handler mHandler = new Handler();
    private FtdiSerial mConnection;
    //http://developer.android.com/guide/topics/connectivity/usb/host.html
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    if (!mConnection.isOpen()) {
                        Connect();
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    mConnection.close();
                    break;
                case ACTION_USB_PERMISSION:
                    if (!mConnection.isOpen()) {
                        Connect();
                    }
                    break;
            }
        }
    };

//    private void read() {
//        byte[] dataBuffer = new byte[DEFAULT_BUFFER_SIZE];
//        final StringBuilder mReadData = new StringBuilder();
//
//        int length = mConnection.read(dataBuffer);
//        if (length > 0) {
//            for (int i = 0; i < length; ++i) {
//                mReadData.append((char) dataBuffer[i]);
//            }
//
//            mHandler.post(new Runnable() {
//                public void run() {
//                    Toast.makeText(getBaseContext(), mReadData.toString(), Toast.LENGTH_LONG).show();
//                    mReadData.setLength(0);
//                }
//            });
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_sensor);

        ApiUtils.lockScreenOrientation(this);

        final ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);

        Button nextButton = (Button) findViewById(R.id.startButton);
        Button lowConductivityButton = (Button) findViewById(R.id.lowButton);
        Button highConductivityButton = (Button) findViewById(R.id.highButton);
        final EditText lowValueEditText = (EditText) findViewById(R.id.lowValueEditText);
        final EditText highValueEditText = (EditText) findViewById(R.id.highValueEditText);

        final Context context = this;

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mConnection.isOpen()) {
                    Connect();
                }
                if (mConnection.isOpen()) {

                    final ProgressDialog dialog = ProgressDialog.show(context, getString(R.string.deviceConnecting),
                            getString(R.string.pleaseWait), true);
                    dialog.setCancelable(false);
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            dialog.dismiss();
                            viewAnimator.showNext();
                        }
                    }, 3000);

                } else {
                    AlertUtils.showMessage(context, R.string.notConnected, R.string.deviceConnectSensor);
                }
            }
        });

        lowConductivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validInput(lowValueEditText.getText().toString())) {
                    closeKeyboard(lowValueEditText);
                    String requestCommand = "L," + lowValueEditText.getText();
                    //String requestCommand = "R";
                    mConnection.write(requestCommand.getBytes(), requestCommand.length());

                    final ProgressDialog dialog = ProgressDialog.show(context, getString(R.string.calibrating),
                            getString(R.string.pleaseWait), true);
                    dialog.setCancelable(false);
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            //read();
                            dialog.dismiss();
                            viewAnimator.showNext();
                        }
                    }, 3000);
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
                    String requestCommand = "H," + highValueEditText.getText();
                    //String requestCommand = "R";
                    mConnection.write(requestCommand.getBytes(), requestCommand.length());
                    final ProgressDialog dialog = ProgressDialog.show(context, getString(R.string.calibrating),
                            getString(R.string.pleaseWait), true);
                    dialog.setCancelable(false);
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            //read();
                            //String requestCommand = "R";
                            String requestCommand = "C";
                            mConnection.write(requestCommand.getBytes(), requestCommand.length());
                            (new Handler()).postDelayed(new Runnable() {
                                public void run() {
                                    //read();
                                    dialog.dismiss();
                                    AlertUtils.showAlert(context, R.string.calibrated, R.string.calibrationSucceeded, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }, null);
                                }
                            }, 3000);
                        }
                    }, 3000);

                } else {
                    highValueEditText.setError(getString(R.string.pleaseEnterValue));
                }
            }
        });

        mConnection = new FtdiSerial(this);

        //http://developer.android.com/guide/topics/connectivity/usb/host.html
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        Connect();
    }

    private void Connect() {
        if (mConnection != null) {
            mConnection.initialize(DEFAULT_BAUD_RATE, D2xxManager.FT_DATA_BITS_8,
                    D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
        }
    }

    private boolean validInput(String input) {
        return input.trim().length() > 0;
    }

    void closeKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    @Override
    public void onDestroy() {
        mConnection.close();
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

}
