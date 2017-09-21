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

package org.akvo.caddisfly.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.ec.CalibrateSensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TypeListActivity extends BaseActivity implements TypeListFragment.OnFragmentInteractionListener {

    private static final int PERMISSION_ALL = 1;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;

    private View coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_list);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(R.string.selectTest);
    }

    @Override
    public void onFragmentInteraction(TestInfo testInfo) {
        CaddisflyApp.getApp().loadTestConfigurationByUuid(testInfo.getId());

        switch (testInfo.getType()) {
            case COLORIMETRIC_LIQUID:

                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (AppPreferences.useExternalCamera()) {
                    permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                }

                if (!ApiUtil.hasPermissions(this, permissions)) {
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
                } else {
                    startCalibration();
                }

                break;
            case SENSOR:
                //Only start the sensor activity if the device supports 'On The Go'(OTG) feature
                boolean hasOtg = getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                if (hasOtg) {
                    AlertUtil.askQuestion(this, R.string.warning, R.string.incorrectCalibrationCanAffect,
                            R.string.calibrate, R.string.cancel, true,
                            (dialogInterface, i) -> {
                                final Intent intent = new Intent(getBaseContext(), CalibrateSensorActivity.class);
                                startActivity(intent);
                            }, null);
                } else {
                    alertFeatureNotSupported();
                }
                break;
            default:
                break;
        }
    }

    private void startCalibration() {
        //Only start the colorimetry calibration if the device has a camera flash
        if (AppPreferences.useExternalCamera()
                || CameraHelper.hasFeatureCameraFlash(this, R.string.cannotCalibrate, R.string.ok, null)) {

            final Intent intent;
            if (getIntent().getBooleanExtra("runTest", false)) {
                intent = new Intent(this, ColorimetryLiquidActivity.class);
            } else {
                intent = new Intent(this, CalibrateListActivity.class);
            }
            startActivity(intent);
        }
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
            if (granted) {
                startCalibration();
            } else {
                String message = getString(R.string.cameraAndStoragePermissions);
                if (AppPreferences.useExternalCamera()) {
                    message = getString(R.string.storagePermission);
                }
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s%n%n%s",
                getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notSupported, message);
    }
}
