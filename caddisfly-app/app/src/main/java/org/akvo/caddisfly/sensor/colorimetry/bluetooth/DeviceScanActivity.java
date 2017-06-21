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

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.akvo.caddisfly.sensor.colorimetry.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends BaseActivity implements DeviceConnectDialog.InterfaceCommunicator {
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after scan period
    private static final long SCAN_PERIOD = 6000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE = 100;
    private static final long CONNECTING_DELAY = 2000;
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private LinearLayout layoutInfo;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;
    private ProgressBar progressBar;
    private ListView deviceList;
    private RelativeLayout layoutDevices;
    private Runnable runnable;
    private TextView textTitle;
    private TextView textSubtitle;
    private ScanCallback mScanCallback;
    private DeviceConnectDialog deviceConnectDialog;
    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);

        setTitle(CaddisflyApp.getApp().getCurrentTestInfo().getName());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mScanCallback = new ScanCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Timber.d("onScanResult");
                    processResult(result);
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    for (ScanResult result : results) {
                        processResult(result);
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                private void processResult(ScanResult result) {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                }
            };
        } else {
            mLeScanCallback =
                    new BluetoothAdapter.LeScanCallback() {

                        @Override
                        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLeDeviceListAdapter.addDevice(device);
                                }
                            });
                        }
                    };
        }

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                }
            });

            builder.show();
        }

        layoutDevices = (RelativeLayout) findViewById(R.id.layoutDevices);

        deviceList = (ListView) findViewById(R.id.device_list);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                connectToDevice(position);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        layoutInfo = (LinearLayout) findViewById(R.id.layout_bluetooth_info);

        textTitle = (TextView) findViewById(R.id.textTitle);
        textTitle.setText("Scanning...");

        textSubtitle = (TextView) findViewById(R.id.textSubtitle);
        textSubtitle.setText("Searching for bluetooth devices");
    }

    private void showInstructionDialog() {

        if (deviceConnectDialog == null || !deviceConnectDialog.isVisible()) {
            deviceConnectDialog = DeviceConnectDialog.newInstance();

            final FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment fragment = getFragmentManager().findFragmentByTag("resultDialog");
            if (fragment != null) {
                ft.remove(fragment);
            }

            deviceConnectDialog.setCancelable(false);
            deviceConnectDialog.show(ft, "connectionInfoDialog");
        }
    }

    private void connectToDevice(int position) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) {
            return;
        }
        final Intent intent = new Intent(DeviceScanActivity.this, DeviceControlActivity.class);
        intent.putExtra(Constant.UUID, getIntent().getStringExtra(Constant.UUID));

        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothLeScanner.stopScan(mScanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            mScanning = false;
        }

        final ProgressDialog dlg = new ProgressDialog(this);
        dlg.setMessage(getString(R.string.deviceConnecting));
        dlg.setCancelable(false);
        dlg.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivityForResult(intent, REQUEST_CODE);
                dlg.dismiss();
            }
        }, CONNECTING_DELAY);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, app cannot discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                        }
//                    });
                    builder.show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        layoutDevices.setVisibility(View.GONE);
        layoutInfo.setVisibility(View.VISIBLE);

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        deviceList.setAdapter(mLeDeviceListAdapter);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLeDeviceListAdapter.getCount() < 1) {
                    layoutInfo.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);

        scanLeDevice(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mHandler.removeCallbacks(runnable);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(data);
            this.setResult(Activity.RESULT_OK, intent);
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(runnable);
        if (deviceConnectDialog != null) {
            deviceConnectDialog.dismiss();
        }
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    private void scanLeDevice(final boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothLeScanner == null) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (enable) {

            runnable = new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothLeScanner.stopScan(mScanCallback);
                    } else {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }

                    if (!isDestroyed() && !isFinishing()) {
                        if (mLeDeviceListAdapter.getCount() < 1) {
                            deviceList.setVisibility(View.GONE);
                            layoutInfo.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            showInstructionDialog();
                        }
                    }
                }
            };

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            mScanning = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(GattAttributes.LOVIBOND_SERVICE)).build();

                List<ScanFilter> scanFilters = new ArrayList<>();
                scanFilters.add(filter);

                ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

                mBluetoothLeScanner.startScan(scanFilters, settings, mScanCallback);

                progressBar.setVisibility(View.VISIBLE);
            } else {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            mScanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }

        }
    }

    @Override
    public void sendRequestCode(int code) {
        if (code == RESULT_OK) {
            scanLeDevice(true);
        } else {
            finish();
        }
    }

    private static class ViewHolder {
        private TextView deviceName;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private final List<BluetoothDevice> mLeDevices;
        private final LayoutInflater mInflater;

        LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflater = DeviceScanActivity.this.getLayoutInflater();
        }

        private void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)
                    && device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC
                    && device.getType() != BluetoothDevice.DEVICE_TYPE_UNKNOWN
                    ) {
                mLeDevices.add(device);
                mLeDeviceListAdapter.notifyDataSetChanged();

                layoutDevices.setVisibility(View.VISIBLE);
                deviceList.setVisibility(View.VISIBLE);
                layoutInfo.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

                textTitle.setText("Nearby devices");
                textSubtitle.setText("Connect to your device");
            }
        }

        private BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.item_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);

                Button buttonConnect = (Button) view.findViewById(R.id.button_connect);

                buttonConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Timber.e("clicked " + position);
                        connectToDevice(position);
                    }
                });

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText("MD610 Photometer");
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            Timber.e(device.getAddress());

            return view;
        }
    }
}
