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

package org.akvo.caddisfly.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ApiUtils;


public class UsbConnectionActivity extends ActionBarActivity {
    private TextView mConnectedTextView;
    private LinearLayout mProgressLayout;

//    private static final String ACTION_USB_PERMISSION = "org.akvo.caddisfly.USB_PERMISSION";
//    IntentFilter filter;
//
//    //http://developer.android.com/guide/topics/connectivity/usb/host.html
//    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            switch (action) {
//                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
//                    Toast.makeText(getBaseContext(), "Device Connected", Toast.LENGTH_LONG).show();
//                    break;
//                case UsbManager.ACTION_USB_DEVICE_DETACHED:
//                    Toast.makeText(getBaseContext(), "Device Disconnected", Toast.LENGTH_LONG).show();
//                    break;
//                case ACTION_USB_PERMISSION:
//                    Toast.makeText(getBaseContext(), "Device Connected", Toast.LENGTH_LONG).show();
//                    break;
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_connection);

        ApiUtils.lockScreenOrientation(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_logo);

        mConnectedTextView = (TextView) findViewById(R.id.connectedTextView);
        mProgressLayout = (LinearLayout) findViewById(R.id.progressLayout);

        //http://developer.android.com/guide/topics/connectivity/usb/host.html
//        filter = new IntentFilter();
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(mUsbReceiver, filter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                mConnectedTextView.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);
            }
        }, 3000);

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                //Toast.makeText(getBaseContext(), "EC Sensor Device Connected", Toast.LENGTH_LONG).show();
                finish();
            }
        }, 4000);
    }
}
