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

package org.akvo.caddisfly.sensor.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends BaseActivity implements DeviceConnectDialog.InterfaceCommunicator {

    private static final int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION};

    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after scan period
    private static final long SCAN_PERIOD = 6000;
    private static final int REQUEST_CODE = 100;
    private static final long CONNECTING_DELAY = 2000;
    private CoordinatorLayout coordinatorLayout;
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
    private Snackbar snackbar;
    private TestInfo testInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        setTitle("Connection");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mScanCallback = new ScanCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
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

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                private void processResult(ScanResult result) {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                }
            };
        } else {
            mLeScanCallback =
                    (device, rssi, scanRecord) -> runOnUiThread(() -> mLeDeviceListAdapter.addDevice(device));
        }

        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        layoutDevices = findViewById(R.id.layoutDevices);

        deviceList = findViewById(R.id.device_list);

        progressBar = findViewById(R.id.progressBar);

        layoutInfo = findViewById(R.id.layout_bluetooth_info);

        textTitle = findViewById(R.id.textTitle);
        textTitle.setText(R.string.scanning);

        textSubtitle = findViewById(R.id.textSubtitle);
        textSubtitle.setText(R.string.searching_for_device);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    private void showInstructionDialog() {

        if (deviceConnectDialog == null || !deviceConnectDialog.isVisible()) {
            deviceConnectDialog = DeviceConnectDialog.newInstance();

            final FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment fragment = getFragmentManager().findFragmentByTag("connectionInfoDialog");
            if (fragment != null) {
                ft.remove(fragment);
            }

            deviceConnectDialog.setCancelable(false);
            deviceConnectDialog.show(ft, "connectionInfoDialog");
        }
    }

    private void connectToDevice(int position) {

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) {
            return;
        }
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);

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
        new Handler().postDelayed(() -> {
            startActivityForResult(intent, REQUEST_CODE);
            dlg.dismiss();
        }, CONNECTING_DELAY);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        final Activity activity = this;
        if (requestCode == PERMISSION_ALL) {
            // If request is cancelled, the result arrays are empty.
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            if (!granted) {
                snackbar = Snackbar
                        .make(coordinatorLayout, getString(R.string.location_permission),
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(activity));

                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                snackbar.setActionTextColor(typedValue.data);
                View snackView = snackbar.getView();
                TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
                textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        layoutDevices.setVisibility(View.GONE);
        layoutInfo.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            if (snackbar == null || !snackbar.isShownOrQueued()) {
                requestPermissions(PERMISSIONS, PERMISSION_ALL);
            }
        } else {
            scanLeDevice(true);
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        deviceList.setAdapter(mLeDeviceListAdapter);

        mHandler.postDelayed(() -> {
            if (mLeDeviceListAdapter.getCount() < 1) {
                layoutInfo.setVisibility(View.VISIBLE);
            }
        }, 1000);
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

        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(data);
                this.setResult(Activity.RESULT_OK, intent);
                finish();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                scanLeDevice(true);
            }
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

        progressBar.setVisibility(View.VISIBLE);

        if (enable) {

            if (mScanning) {
                return;
            }

            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothLeScanner == null) {
                return;
            }

            runnable = () -> {
                mScanning = false;
                if (mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothLeScanner.stopScan(mScanCallback);
                    } else {
                        //noinspection deprecation
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }

                if (!isDestroyed() && !isFinishing() && mLeDeviceListAdapter.getCount() < 1) {
                    deviceList.setVisibility(View.GONE);
                    layoutInfo.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    showInstructionDialog();
                }
            };

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            mScanning = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid.fromString(GattAttributes.LOVIBOND_SERVICE)).build();

                List<ScanFilter> scanFilters = new ArrayList<>();
                scanFilters.add(filter);

                ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothLeScanner.startScan(scanFilters, settings, mScanCallback);
                }

                progressBar.setVisibility(View.VISIBLE);
            } else {
                //noinspection deprecation
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

                textTitle.setText(R.string.nearby_devices);
                textSubtitle.setText(R.string.connect_device);
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
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);

                Button buttonConnect = view.findViewById(R.id.button_connect);

                buttonConnect.setOnClickListener(v -> {
                    connectToDevice(position);
                });

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(R.string.md610_photometer);
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }

            return view;
        }
    }
}
