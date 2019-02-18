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
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.widget.PageIndicatorView;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import timber.log.Timber;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends BaseActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final long RESULT_DISPLAY_DELAY = 2000;
    private SelectTestFragment selectTestFragment;
    private WaitingFragment waitingFragment;
    private ViewPager viewPager;
    private FrameLayout resultLayout;
    private FrameLayout pagerLayout;
    private RelativeLayout footerLayout;
    private TestInfo testInfo;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    // to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
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
    private Handler debugTestHandler;
    private ProgressBar progressCircle;
    private boolean showSkipMenu = false;
    private BluetoothResultFragment mBluetoothResultFragment;
    private String mData;
    private PageIndicatorView pagerIndicator;
    private boolean registered;
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (Objects.requireNonNull(action)) {
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    if (AppPreferences.getShowDebugInfo()) {
                        Toast.makeText(DeviceControlActivity.this,
                                "Device connected", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    Toast.makeText(DeviceControlActivity.this,
                            "Device disconnected. Check bluetooth settings.", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    setGattServices(mBluetoothLeService.getSupportedGattServices());
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    break;
                default:
                    break;
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

    private void registerReceiver() {
        if (!registered) {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            registered = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_result);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO);

        hookBluetooth();

        waitingFragment = WaitingFragment.getInstance();
        mBluetoothResultFragment = BluetoothResultFragment.getInstance(testInfo);
        selectTestFragment = SelectTestFragment.getInstance(testInfo);
        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        resultLayout = findViewById(R.id.resultLayout);
        pagerLayout = findViewById(R.id.pagerLayout);
        footerLayout = findViewById(R.id.layout_footer);
        progressCircle = findViewById(R.id.progressCircle);

        progressCircle.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.resultLayout, mBluetoothResultFragment);
        ft.commit();

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);

        pagerIndicator.showDots(true);
        pagerIndicator.setPageCount(mSectionsPagerAdapter.getCount() - 2);

        ImageView imagePageRight = findViewById(R.id.image_pageRight);
        imagePageRight.setOnClickListener(view ->
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));

        ImageView imagePageLeft = findViewById(R.id.image_pageLeft);
        imagePageLeft.setOnClickListener(view -> pageBack());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Nothing to do here
            }

            @Override
            public void onPageSelected(int position) {
                pagerIndicator.setActiveIndex(position - 1);

                if (position == 0) {
                    showSelectTestView();
                } else if (position == 1) {
                    showInstructionsView();
                } else if (position == testInfo.getInstructions().size() + 1) {
                    showWaitingView();
                } else {
                    showInstructionsView();
                    onInstructionFinish(testInfo.getInstructions().size() - position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Nothing to do here
            }
        });

        showSelectTestView();
    }

    private void pageBack() {
        viewPager.setCurrentItem(Math.max(0, viewPager.getCurrentItem() - 1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            if (!result) {
                finish();
            }
        }
    }

    private boolean waitingForResult() {
        return viewPager.getCurrentItem() == testInfo.getInstructions().size() + 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showSkipMenu && testInfo.getSubtype() == TestType.BLUETOOTH) {
            getMenuInflater().inflate(R.menu.menu_instructions, menu);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unHookBluetooth();
    }

    private void hookBluetooth() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void unHookBluetooth() {
        unregisterReceiver();
        if (debugTestHandler != null) {
            debugTestHandler.removeCallbacksAndMessages(null);
        }
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(mGattUpdateReceiver);
            registered = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindServices();
        mBluetoothLeService = null;
    }

    private void unbindServices() {
        try {
            unbindService(mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (viewPager.getCurrentItem() == 0) {
                onBackPressed();
            } else {
                showSelectTestView();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (debugTestHandler != null) {
            debugTestHandler.removeCallbacksAndMessages(null);
        }
        if (resultLayout.getVisibility() == View.VISIBLE) {
            viewPager.setCurrentItem(testInfo.getInstructions().size() + 1);
            showWaitingView();
        } else if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            pageBack();
        }
    }

    private void displayData(String data) {

        if (!waitingForResult()) {
            return;
        }

        mData += data;

        Matcher m = Pattern.compile("DT01;((?!DT01;).)*?;;;;").matcher(mData);

        if (m.find()) {

            unregisterReceiver();

            final String fullData = m.group();
            mData = "";

            final ProgressDialog dlg = new ProgressDialog(this);
            dlg.setMessage("Receiving data");
            dlg.setCancelable(false);
            dlg.show();
            new Handler().postDelayed(() -> {
                try {
                    if (mBluetoothResultFragment.displayData(fullData)) {
                        setTitle(R.string.result);
                        showResultView();
                    } else {
                        registerReceiver();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.invalid_data_received), Toast.LENGTH_LONG).show();
                    Timber.e("Bluetooth data error: %s", fullData);
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

    private void showInstructionsView() {
        progressCircle.setVisibility(View.GONE);
        footerLayout.setVisibility(View.VISIBLE);
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        setTitle(testInfo.getMd610Id() + ". " + testInfo.getName());
        showSkipMenu = true;
        invalidateOptionsMenu();
    }

    private void showSelectTestView() {
        progressCircle.setVisibility(View.GONE);
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        footerLayout.setVisibility(View.GONE);
        viewPager.setCurrentItem(0);
        showSkipMenu = false;
        setTitle(R.string.selectTest);
        invalidateOptionsMenu();
    }

    private void showWaitingView() {
        registerReceiver();

        if (!AppConfig.STOP_ANIMATIONS) {
            progressCircle.setVisibility(View.VISIBLE);
        }
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        footerLayout.setVisibility(View.GONE);
        showSkipMenu = false;
        setTitle(R.string.awaitingResult);
        invalidateOptionsMenu();
        if (AppPreferences.isTestMode()) {
            if (debugTestHandler != null) {
                debugTestHandler.removeCallbacksAndMessages(null);
            }

            debugTestHandler = new Handler();
            debugTestHandler.postDelayed(() -> displayData(Constants.BLUETOOTH_TEST_DATA), 6000);
        }
    }

    private void showResultView() {
        progressCircle.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        pagerLayout.setVisibility(View.GONE);
    }

    public void onInstructionFinish(int page) {
        if (page > 1) {
            showSkipMenu = true;
            invalidateOptionsMenu();
        } else if (page == 0) {
            showSkipMenu = false;
            invalidateOptionsMenu();
        }
    }

    public void onSkipClick(MenuItem item) {
        viewPager.setCurrentItem(testInfo.getInstructions().size() + 1);
        showWaitingView();
    }

    public void onSelectTestClick(View view) {
        viewPager.setCurrentItem(1);
        showInstructionsView();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        FragmentInstructionBinding fragmentInstructionBinding;
        Instruction instruction;

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param instruction The information to to display
         * @return The instance
         */
        static PlaceholderFragment newInstance(Instruction instruction) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putParcelable(ARG_SECTION_NUMBER, instruction);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            fragmentInstructionBinding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_instruction, container, false);

            if (getArguments() != null) {
                instruction = getArguments().getParcelable(ARG_SECTION_NUMBER);
                fragmentInstructionBinding.setInstruction(instruction);
            }

            return fragmentInstructionBinding.getRoot();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return selectTestFragment;
            } else if (position == testInfo.getInstructions().size() + 1) {
                return waitingFragment;
            } else {
                return PlaceholderFragment.newInstance(testInfo.getInstructions().get(position - 1));
            }
        }

        @Override
        public int getCount() {
            return testInfo.getInstructions().size() + 2;
        }
    }
}
