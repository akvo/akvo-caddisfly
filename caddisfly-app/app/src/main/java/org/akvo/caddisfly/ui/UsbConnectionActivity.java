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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ApiUtils;


public class UsbConnectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_connection);

        ApiUtils.lockScreenOrientation(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.appName);
        progressDialog.setMessage(getString(R.string.deviceConnecting));
        progressDialog.setCancelable(false);
        progressDialog.show();

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                progressDialog.setMessage(getString(R.string.deviceConnected));
            }
        }, 2000);

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                progressDialog.dismiss();
                finish();
            }
        }, 4000);
    }
}
