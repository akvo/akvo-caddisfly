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

package org.akvo.caddisfly.sensor.ec;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.usb.UsbService;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.StringUtil;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public class CalibrateSensorActivity extends BaseActivity implements EditSensorIdentity.OnFragmentInteractionListener {

    private static final String LINE_FEED = "\r\n";
    private static final int IDENTIFY_DELAY_MILLIS = 500;
    private static final int INITIAL_DELAY_MILLIS = 2000;
    private static final int PROGRESS_MAX = 10;
    private static final int CALIBRATION_DELAY_MILLIS = 8000;
    private static final int SAVING_DELAY_MILLIS = 4000;

    private final Handler handler = new Handler();
    // Notifications from UsbService will be received here.
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, @NonNull Intent arg1) {
//            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB PERMISSION GRANTED
//            {
//                Toast.makeText(arg0, "USB Ready", Toast.LENGTH_SHORT).show();
//            } else
//            else if (arg1.getAction().equals(UsbService.ACTION_NO_USB)) // NO USB CONNECTED
//            {
//                Toast.makeText(arg0, "No USB connected", Toast.LENGTH_SHORT).show();
//            }

            if (arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)) {
                Toast.makeText(arg0, "USB Permission not granted", Toast.LENGTH_SHORT).show();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_DISCONNECTED)) {
                Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
            } else if (arg1.getAction().equals(UsbService.ACTION_USB_NOT_SUPPORTED)) {
                Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private TestInfo mCurrentTestInfo;
    private double[] calibrationPoints;
    private ProgressDialog progressDialog;
    private ViewAnimator viewAnimator;
    private boolean deviceHasId = false;
    private int calibrationIndex = 0;
    private TextView textHeading;
    private TextView textSubtitle;
    private TextView textInformation;
    private Context mContext;
    private UsbService usbService;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestResult();
        }
    };
    private TextView textId;
    private UsbDataHandler mHandler;
    @Nullable
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, @NonNull IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    @NonNull
    private String mReceivedData = "";
    private FloatingActionButton fabEdit;
    private int deviceStatus = 0;
    private final Runnable validateDeviceRunnable = new Runnable() {
        @Override
        public void run() {

            String data = "device\r\n";
            if (usbService != null && usbService.isUsbConnected()) {
                // if UsbService was correctly bound, Send data
                usbService.write(data.getBytes(StandardCharsets.UTF_8));
            } else {
                return;
            }

            switch (deviceStatus) {

                case 0:
                    handler.postDelayed(this, IDENTIFY_DELAY_MILLIS);
                    break;
                case 1:
                    handler.postDelayed(runnable, 100);
                    break;
                default:
                    alertSensorNotFound();
                    break;
            }
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
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        if (usbConnection != null) {
            unbindService(usbConnection);
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
    private void startService(Class<?> service, ServiceConnection serviceConnection, @Nullable Bundle extras) {

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

        deviceStatus = 0;

        handler.postDelayed(validateDeviceRunnable, 100);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_sensor);

        mContext = this;

        //final Intent intent = getIntent();
        //String mUuid = intent.getStringExtra(Constant.UUID);
        //CaddisflyApp.getApp().loadTestConfigurationByUuid(mUuid);
        mCurrentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        mHandler = new UsbDataHandler(this);

        viewAnimator = findViewById(R.id.viewAnimator);

        Button buttonStartCalibrate = findViewById(R.id.buttonStartCalibrate);
        Button buttonFinishCalibrate = findViewById(R.id.buttonFinishCalibrate);
        Button buttonNext = findViewById(R.id.buttonNext);

        fabEdit = findViewById(R.id.fabEdit);

        if (!mCurrentTestInfo.getName().isEmpty()) {
            ((TextView) findViewById(R.id.textTitle)).setText(
                    mCurrentTestInfo.getName());
        }

        calibrationPoints = mCurrentTestInfo.getRangeValues();
        textHeading = findViewById(R.id.textHeading);
        textSubtitle = findViewById(R.id.textSubtitle);
        textInformation = findViewById(R.id.textInformation);
        textId = findViewById(R.id.textId);

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDetailsDialog();
            }
        });

        buttonStartCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usbService.isUsbConnected()) {

                    final ProgressDialog connectingProgressDialog = ProgressDialog.show(mContext,
                            getString(R.string.pleaseWait), getString(R.string.deviceConnecting), true, false);

                    new Handler().postDelayed(runnable, 100);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (deviceHasId) {
                                viewAnimator.showNext();
                                fabEdit.setVisibility(View.VISIBLE);
                                textSubtitle.setText(R.string.verifySensorDetails);

                            } else {
                                showEditDetailsDialog();
                            }
                            connectingProgressDialog.dismiss();
                        }
                    }, INITIAL_DELAY_MILLIS);

                } else {
                    alertSensorNotFound();
                }
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deviceHasId) {
                    viewAnimator.showNext();
                    fabEdit.setVisibility(View.INVISIBLE);

                    displayInformation(calibrationIndex);
                } else {
                    showEditDetailsDialog();
                }

            }
        });

        buttonFinishCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usbService.isUsbConnected()) {
                    calibratePoint(calibrationPoints, calibrationIndex);
                    calibrationIndex++;
                } else {
                    alertSensorNotFound();
                }
            }
        });
    }

    private void alertSensorNotFound() {

        String message = String.format("%s<br/><br/>%s", getString(R.string.expectedDeviceNotFound),
                getString(R.string.connectCorrectSensor, mCurrentTestInfo.getName()));

        Spanned spanned = StringUtil.fromHtml(message);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(mContext);

        builder.setTitle(R.string.sensorNotFound)
                .setMessage(spanned)
                .setCancelable(false);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NonNull DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEditDetailsDialog() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        EditSensorIdentity editSensorIdentity = EditSensorIdentity.newInstance();
        editSensorIdentity.show(ft, "editSensorIdentity");
    }

    private void displayInformation(int index) {

        textHeading.setText(String.format(Locale.US,
                getString(R.string.calibratePoint),
                calibrationPoints[index]));
        textSubtitle.setText(String.format(Locale.US, "Step %d of 6", index + 1));
        textInformation.setText(String.format(Locale.US,
                getString(R.string.getEcSolutionReady),
                calibrationPoints[index]));

    }

    private void requestResult() {
        String data = "GET ID\r\n";
        if (usbService != null) {
            usbService.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void calibratePoint(final double[] calibrations, final int index) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.pleaseWait);
        progressDialog.setMessage(getString(R.string.calibrating));
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(PROGRESS_MAX);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        final Handler progressHandler = new Handler();
        Runnable progressRunnable = new Runnable() {
            public void run() {
                progressDialog.incrementProgressBy(1);
                if (progressDialog.getProgress() == progressDialog.getMax()) {
                    progressDialog.dismiss();
                } else {
                    progressHandler.postDelayed(this, 1000);
                }
            }
        };

        progressHandler.postDelayed(progressRunnable, 100);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                String requestCommand = "SET POINT" + calibrationIndex + " " + calibrations[index] + LINE_FEED;

                usbService.write(requestCommand.getBytes(StandardCharsets.UTF_8));

                (new Handler()).postDelayed(new Runnable() {
                    public void run() {

                        if (calibrationIndex > 5) {

                            AlertUtil.showAlert(mContext, R.string.calibrationSuccessful,
                                    R.string.sensorCalibrated, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            finish();
                                        }
                                    }, null, null);

                        } else {
                            displayInformation(calibrationIndex);
                        }
                    }
                }, 1000);
            }
        }, CALIBRATION_DELAY_MILLIS);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.calibrate);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayId(String id) {
        String displayText = id.trim();
        textId.setText(displayText);
        deviceHasId = (!displayText.isEmpty() && !"0".equals(displayText));
    }

    @Override
    public void onFragmentInteraction(String value) {
        String requestCommand = "SET ID " + value + LINE_FEED;

        usbService.write(requestCommand.getBytes(StandardCharsets.UTF_8));

        final ProgressDialog savingProgressDialog = ProgressDialog.show(mContext,
                getString(R.string.pleaseWait), getString(R.string.saving), true, false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String getIdRequest = "GET ID" + LINE_FEED;
                usbService.write(getIdRequest.getBytes(StandardCharsets.UTF_8));

                displayInformation(calibrationIndex);

                savingProgressDialog.dismiss();

                if (viewAnimator.getDisplayedChild() == 0) {
                    viewAnimator.showNext();
                    fabEdit.setVisibility(View.VISIBLE);
                }
            }
        }, SAVING_DELAY_MILLIS);

    }

    private void displayResult(String result) {

        if (deviceStatus == 0) {
            if (result.contains(" ")) {
                if (result.startsWith(mCurrentTestInfo.getDeviceId())) {
                    deviceStatus = 1;
                } else {
                    deviceStatus = 2;
                }
            }
            return;
        }

        if (!result.contains("OK")) {
            displayId(result);
        }
    }

    /*
    * This handler will be passed to UsbService.
    * Data received from serial port is displayed through this handler
    */
    private static class UsbDataHandler extends Handler {
        @NonNull
        private final WeakReference<CalibrateSensorActivity> mActivity;

        UsbDataHandler(CalibrateSensorActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == UsbService.MESSAGE_FROM_SERIAL_PORT) {
                String data = (String) msg.obj;
                CalibrateSensorActivity sensorActivity = mActivity.get();
                if (sensorActivity != null) {
                    sensorActivity.mReceivedData += data;
                    if (sensorActivity.mReceivedData.contains(LINE_FEED)) {

                        sensorActivity.displayResult(sensorActivity.mReceivedData.trim());

                        sensorActivity.mReceivedData = "";
                    }
                }
            }
        }
    }
}
