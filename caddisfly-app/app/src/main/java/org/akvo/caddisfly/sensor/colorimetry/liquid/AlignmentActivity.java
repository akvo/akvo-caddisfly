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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.CameraDialogFragment;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;

import java.util.List;

public class AlignmentActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private CameraDialog mCameraDialog;
    private boolean mTestStarted = false;
    private USBMonitor mUSBMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alignment);

        setTitle("Check Alignment");

        findViewById(R.id.buttonStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCameraDialog.stopCamera();

                final Intent intent = new Intent(getIntent());
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                intent.setClass(getBaseContext(), ColorimetryLiquidActivity.class);
                mTestStarted = true;
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        Configuration conf = getResources().getConfiguration();
        ((TextView) findViewById(R.id.textTitle)).setText(
                CaddisflyApp.getApp().getCurrentTestInfo().getName(conf.locale.getLanguage()));

        if (getIntent().getBooleanExtra("isCalibration", false)) {
            String subTitle = String.format("%s %.2f %s",
                    getResources().getString(R.string.calibrate),
                    getIntent().getDoubleExtra("swatchValue", 0),
                    CaddisflyApp.getApp().getCurrentTestInfo().getUnit());
            ((TextView) findViewById(R.id.textDilution)).setText(subTitle);
        } else {
            int dilutionLevel = getIntent().getIntExtra("dilution", 0);
            TextView textDilution = (TextView) findViewById(R.id.textDilution);
            switch (dilutionLevel) {
                case 0:
                    textDilution.setText(R.string.noDilution);
                    break;
                case 1:
                    textDilution.setText(String.format(getString(R.string.timesDilution), 2));
                    break;
                case 2:
                    textDilution.setText(String.format(getString(R.string.timesDilution), 5));
                    break;
            }
        }

        mUSBMonitor = new USBMonitor(this, null);

        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.camera_device_filter);
        List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
        if (usbDeviceList.size() > 0) {
            mCameraDialog = ExternalCameraFragment.newInstance();
        } else {
            mCameraDialog = CameraDialogFragment.newInstance();
        }

        TextView textSubtitle = (TextView) findViewById(R.id.textSubtitle);

        textSubtitle.setText(R.string.alignChamber);

        getFragmentManager().beginTransaction()
                .add(R.id.layoutCameraPreview, mCameraDialog)
                .commit();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(data);
                    this.setResult(Activity.RESULT_OK, intent);
                }
                onBackPressed();
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_back_out, R.anim.slide_back_in);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!mTestStarted) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraDialog != null) {
            mCameraDialog.stopCamera();
        }
    }
}