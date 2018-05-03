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

package org.akvo.caddisfly.sensor.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableStringBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.ui.InstructionFragment;
import org.akvo.caddisfly.util.StringUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends BaseActivity
        implements BluetoothResultFragment.OnCurrentModeListener {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final long RESULT_DISPLAY_DELAY = 2000;
    private boolean resultReceived = false;
    private TestInfo testInfo;

    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    // to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Timber.e("Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private BluetoothResultFragment mBluetoothResultFragment;
    private String mData;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private LinearLayout layoutSelectTest;
    private RelativeLayout layoutInstructions;
    private RelativeLayout layoutWaiting;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //updateConnectionState(R.string.disconnected);
                Toast.makeText(DeviceControlActivity.this,
                        "Device disconnected. Check bluetooth settings.", Toast.LENGTH_SHORT).show();
                finish();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                setGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_result);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);

        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mBluetoothResultFragment = BluetoothResultFragment.getInstance(testInfo);
        InstructionFragment instructionFragment = InstructionFragment.getInstance(testInfo);

        layoutInstructions = findViewById(R.id.layoutInstructions);
        layoutWaiting = findViewById(R.id.layoutWaiting);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.layoutInstructions, instructionFragment, "instructionFragment");
        ft.replace(R.id.layoutWaiting, mBluetoothResultFragment);
        ft.commit();

        layoutSelectTest = findViewById(R.id.selectTestLayout);
        findViewById(R.id.buttonTestSelected).setOnClickListener(v -> showWaitingView());

        SpannableStringBuilder selectionInstruction = StringUtil.toInstruction(this, testInfo,
                String.format(StringUtil.getStringByName(this, testInfo.getSelectInstruction()),
                        StringUtil.convertToTags(testInfo.getMd610Id()), testInfo.getName()));

        ((TextView) findViewById(R.id.textSelectInstruction)).setText(selectionInstruction);

        showSelectTestView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            if (!result) {
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBluetoothLeService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (layoutInstructions.getVisibility() == View.VISIBLE) {
                showWaitingView();
            } else {
                onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (layoutSelectTest.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        } else if (resultReceived && mBluetoothResultFragment.isVisible()) {
            mBluetoothResultFragment.displayWaiting();
            showWaitingView();
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            resultReceived = false;
        } else if (layoutInstructions.getVisibility() == View.VISIBLE) {
            showWaitingView();
        } else if (mBluetoothResultFragment.isVisible()) {
            showSelectTestView();
        } else {
            super.onBackPressed();
        }
    }

    private void displayData(String data) {

        mData += data;

        showWaitingView();

        Matcher m = Pattern.compile("DT01;.*?;;;;").matcher(mData);

        if (m.find()) {

            try {
                unregisterReceiver(mGattUpdateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final String fullData = m.group();
            mData = "";

            final ProgressDialog dlg = new ProgressDialog(this);
            dlg.setMessage("Receiving data");
            dlg.setCancelable(false);
            dlg.show();
            new Handler().postDelayed(() -> {

                if (mBluetoothResultFragment.displayData(fullData)) {

                    resultReceived = true;
                    setTitle("Result");
                } else {
                    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                    showSelectTestView();
                }

                dlg.dismiss();
            }, RESULT_DISPLAY_DELAY);
        }
    }

    private void setGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                // The characteristic 'Data' must be set to "indicate"
                if (gattCharacteristic.getUuid().toString().equals(GattAttributes.LOVIBOND_DATA_CHARACTERISTIC)) {
                    mBluetoothLeService.setCharacteristicIndication(gattCharacteristic, true);
                }
            }
        }
    }

    @Override
    public void onCurrentMode(int mode) {
        if (mode == 1) {
            if (mBluetoothResultFragment.isVisible()) {
                mBluetoothResultFragment.displayWaiting();
            }

            showSelectTestView();

        } else {
            showInstructionsView();
        }
    }

    private void showInstructionsView() {
        layoutInstructions.setVisibility(View.VISIBLE);
        layoutSelectTest.setVisibility(View.GONE);
        layoutWaiting.setVisibility(View.GONE);
    }

    private void showSelectTestView() {
        layoutSelectTest.setVisibility(View.VISIBLE);
        layoutWaiting.setVisibility(View.GONE);
        layoutInstructions.setVisibility(View.GONE);
        setTitle(testInfo.getMd610Id() + ". " + testInfo.getName());
    }

    private void showWaitingView() {
        layoutWaiting.setVisibility(View.VISIBLE);
        layoutSelectTest.setVisibility(View.GONE);
        layoutInstructions.setVisibility(View.GONE);
    }

}
