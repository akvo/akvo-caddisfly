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

package org.akvo.caddisfly.sensor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import org.akvo.caddisfly.R;

/**
 * A partial_progress dialog to the show that the usb external device is connected
 */
public class UsbConnectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_connection);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ProgressDialog progressDialog =
                new ProgressDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.appName);
        progressDialog.setMessage(getString(R.string.deviceConnecting));
        progressDialog.setCancelable(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        progressDialog.show();

        //just a fixed delay so the progress is visible
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                progressDialog.setMessage(getString(R.string.deviceConnected));
                //dismiss after the second message
                (new Handler()).postDelayed(new Runnable() {
                    public void run() {
                        try {
                            progressDialog.dismiss();
//                            Intent intent = new Intent("my-event");
//                            intent.putExtra("message", "data");
//                            LocalBroadcastManager.getInstance(CaddisflyApp.getApp()).sendBroadcast(intent);
                        } catch (Exception ignored) {

                        }
                        finish();
                    }
                }, 1000);
            }
        }, 1000);

    }
}
