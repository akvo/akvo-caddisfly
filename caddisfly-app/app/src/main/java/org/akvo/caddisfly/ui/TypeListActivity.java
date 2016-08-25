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

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidExternalActivity;
import org.akvo.caddisfly.sensor.ec.CalibrateSensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;

public class TypeListActivity extends BaseActivity implements TypeListFragment.OnFragmentInteractionListener {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
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
        CaddisflyApp.getApp().loadTestConfiguration(testInfo.getCode());

        switch (testInfo.getType()) {
            case COLORIMETRIC_LIQUID:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (!AppPreferences.useExternalCamera() && ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        startCalibration();
                    }

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
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final Intent intent = new Intent(getBaseContext(), CalibrateSensorActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                }
                            }, null);
                } else {
                    alertFeatureNotSupported();
                }
                break;
        }
    }

    private void startCalibration() {
        //Only start the colorimetry calibration if the device has a camera flash
        if (CaddisflyApp.hasFeatureCameraFlash(this, R.string.cannotCalibrate,
                R.string.ok, null)) {

            final Intent intent;
            if (getIntent().getBooleanExtra("runTest", false)) {
                if (AppPreferences.useExternalCamera()) {
                    intent = new Intent(this, ColorimetryLiquidExternalActivity.class);
                } else {
                    intent = new Intent(this, ColorimetryLiquidActivity.class);
                }
            } else {
                intent = new Intent(this, CalibrateListActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        final Activity activity = this;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCalibration();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Akvo Caddisfly requires camera permission to run",
                                    Snackbar.LENGTH_INDEFINITE)
                            .setAction("SETTINGS", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ApiUtil.startInstalledAppDetailsActivity(activity);
                                }
                            });

                    TypedValue typedValue = new TypedValue();
                    getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                    snackbar.setActionTextColor(typedValue.data);
                    View snackView = snackbar.getView();
                    TextView textView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setHeight(200);
                    textView.setLineSpacing(1.2f, 1.2f);
                    textView.setTextColor(Color.WHITE);
                    snackbar.show();
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_back_out, R.anim.slide_back_in);
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

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s\r\n\r\n%s",
                getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notSupported, message);
    }

}
