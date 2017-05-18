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

package org.akvo.caddisfly.sensor.colorimetry.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;

import java.util.List;

import timber.log.Timber;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends BaseActivity implements BluetoothResultFragment.OnFragmentInteractionListener {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private int numPages = 2;
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
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //updateConnectionState(R.string.disconnected);
                Toast.makeText(DeviceControlActivity.this, "Device disconnected. Check bluetooth settings.", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                finish();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                setGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private Fragment mInstructionsFragment;

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

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        setTitle(testInfo.getName());

        if (testInfo.getInstructions() == null || testInfo.getInstructions().length() < 1) {
            numPages = 1;
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mBluetoothResultFragment = new BluetoothResultFragment();
        mInstructionsFragment = new InstructionFragment();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                if (position == 0) {
//                    setTitle("Result");
//                } else {
//                    setTitle("Test Instructions");
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Timber.d("Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void updateConnectionState() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //mConnectionState.setText(resourceId);
//            }
//        });
//    }

    private void displayData(String data) {


        if (data.contains("DT01")) {
            mData = "";
        }

        mData += data;

        int beginIndex = mData.indexOf("DT01");
        int endIndex = mData.indexOf(";;;");

        if (endIndex > beginIndex) {
            mData = mData.substring(beginIndex, endIndex);

            if (mBluetoothResultFragment.displayData(mData)) {
                numPages = 1;
                mPagerAdapter.notifyDataSetChanged();
                setTitle("Result");
            }

            mPager.setCurrentItem(0);
        }

//        String resultTitles = ",,,,,Test,,,,,Date,Time,,,,,,,Result,Unit";
//        String[] titles = resultTitles.split(",");
//
//        if (data.contains("DT01")) {
//            mData = "";
//            mDataField.setText("");
////            mAcceptButton.setVisibility(View.GONE);
//        }
//
//        mData += data;
//
//        int beginIndex = mData.indexOf("DT01");
//        int endIndex = mData.indexOf(";;;;;");
//
//        if (endIndex > beginIndex) {
//            mData = mData.substring(beginIndex, endIndex);
//
//            String[] result = mData.split(";");
//            for (int i = 0; i < result.length; i++) {
//                if (titles.length > i && !titles[i].isEmpty()) {
//                    if (titles[i].equals("Result")) {
//                        //mResult = result[i];
//                    }
//                    mDataField.append(String.format("%s: %s %n", titles[i], result[i]));
//                }
//            }

//            mAcceptButton.setVisibility(View.VISIBLE);
//        }
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
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onFragmentInteraction() {
        mPager.setCurrentItem(1);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mBluetoothResultFragment;
            } else {
                return mInstructionsFragment;
            }
        }

        @Override
        public int getCount() {
            return numPages;
        }
    }

}
