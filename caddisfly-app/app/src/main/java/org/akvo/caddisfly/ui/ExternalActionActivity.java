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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidExternalActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.SelectDilutionActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.BrandInfoActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.TestTypeListActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.sensor.turbidity.TimeLapseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.lang.ref.WeakReference;
import java.util.Date;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ExternalActionActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private static final int PERMISSION_ALL = 1;

    private final WeakRefHandler handler = new WeakRefHandler(this);
    private Boolean mIsExternalAppCall = false;
    //the language requested by the external app
    private String mExternalAppLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_action);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setTitle(R.string.appName);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String type = intent.getType();
        String caddisflyResourceUuid;

        if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction()) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                mIsExternalAppCall = true;
                String questionTitle = intent.getStringExtra("questionTitle");

                mExternalAppLanguageCode = intent.getStringExtra("language");

                CaddisflyApp.getApp().setAppLanguage(mExternalAppLanguageCode, mIsExternalAppCall, handler);

                //Extract the 5 letter code in the question and load the test config
                String code = questionTitle.substring(Math.max(0, questionTitle.length() - 5)).toLowerCase();

                //todo: remove when obsolete
                switch (code) {
                    case "fluor":
                        caddisflyResourceUuid = SensorConstants.FLUORIDE_ID;
                        break;

                    case "chlor":
                        caddisflyResourceUuid = SensorConstants.FREE_CHLORINE_ID;
                        break;

                    case "econd":
                        caddisflyResourceUuid = SensorConstants.ELECTRICAL_CONDUCTIVITY_ID;
                        break;

                    case "strip":
                        final Intent colorimetricStripIntent = new Intent(this, TestTypeListActivity.class);
                        startActivityForResult(colorimetricStripIntent, REQUEST_TEST);
                        return;

                    default:
                        caddisflyResourceUuid = "";
                }

                CaddisflyApp.getApp().loadTestConfigurationByUuid(caddisflyResourceUuid);

                if (CaddisflyApp.getApp().getCurrentTestInfo() == null) {
                    ((TextView) findViewById(R.id.textTitle)).setText(getTestName(questionTitle));
                    alertTestTypeNotSupported();
                } else {
                    Configuration config = getResources().getConfiguration();
                    ((TextView) findViewById(R.id.textTitle)).setText(
                            CaddisflyApp.getApp().getCurrentTestInfo().getName(config.locale.getLanguage()));

                    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (AppPreferences.useExternalCamera()) {
                        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    }

                    if (!ApiUtil.hasPermissions(this, permissions)) {
                        ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
                    } else {
                        initializeTest();
                    }
                }
            }
        } else if (AppConfig.FLOW_ACTION_CADDISFLY.equals(intent.getAction()) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                mIsExternalAppCall = true;
                caddisflyResourceUuid = intent.getStringExtra("caddisflyResourceUuid");

                mExternalAppLanguageCode = intent.getStringExtra("language");

                CaddisflyApp.getApp().setAppLanguage(mExternalAppLanguageCode, mIsExternalAppCall, handler);

                //Get the test config by uuid
                CaddisflyApp.getApp().loadTestConfigurationByUuid(caddisflyResourceUuid);

                if (CaddisflyApp.getApp().getCurrentTestInfo() == null) {
                    alertTestTypeNotSupported();
                } else {
                    Configuration config = getResources().getConfiguration();
                    ((TextView) findViewById(R.id.textTitle)).setText(
                            CaddisflyApp.getApp().getCurrentTestInfo().getName(config.locale.getLanguage()));

                    String[] permissions = {};
                    if (CaddisflyApp.getApp().getCurrentTestInfo().requiresCameraFlash()) {
                        if (AppPreferences.useExternalCamera()) {
                            permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        } else {
                            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        }
                    }
                    if (!ApiUtil.hasPermissions(this, permissions)) {
                        ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
                    } else {
                        if (AppPreferences.useExternalCamera()) {
                            startTest(caddisflyResourceUuid);
                        } else {
                            initializeTest();
                        }
                    }
                }
            }
        }
    }

    private void initializeTest() {

        if (CameraHelper.hasFeatureCameraFlash(this, R.string.cannotStartTest,
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }
        )) {
            startTest(getIntent().getStringExtra("caddisflyResourceUuid"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
                initializeTest();
            } else {
                String message = getString(R.string.cameraAndStoragePermissions);
                if (AppPreferences.useExternalCamera()) {
                    message = getString(R.string.storagePermission);
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                ApiUtil.startInstalledAppDetailsActivity(this);
                finish();
            }
        }

    }

    private void alertCalibrationExpired() {

        String message = String.format("%s%n%n%s", getString(R.string.errorCalibrationExpired),
                getString(R.string.orderFreshBatch));

        AlertUtil.showAlert(this, R.string.cannotStartTest,
                message, R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                }
        );
    }

    /**
     * Alert message for calibration incomplete or invalid
     */
    private void alertCalibrationIncomplete() {

        final Activity activity = this;

        String message = getString(R.string.errorCalibrationIncomplete,
                CaddisflyApp.getApp().getCurrentTestInfo().getName(
                        getResources().getConfiguration().locale.getLanguage()));
        message = String.format("%s%n%n%s", message,
                getString(R.string.doYouWantToCalibrate));

        AlertUtil.showAlert(this, R.string.cannotStartTest, message, R.string.calibrate,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Intent intent = new Intent(getBaseContext(), CalibrateListActivity.class);
                        startActivity(intent);

                        activity.setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                },
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        activity.setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        CaddisflyApp.getApp().setAppLanguage(mExternalAppLanguageCode, mIsExternalAppCall, handler);
    }

    /**
     * Start the appropriate test based on the current test type
     */
    private void startTest(String caddisflyResourceUuid) {
        Context context = this;
        CaddisflyApp caddisflyApp = CaddisflyApp.getApp();
        switch (caddisflyApp.getCurrentTestInfo().getType()) {
            case COLORIMETRIC_LIQUID:

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (ApiUtil.isCameraInUse(this, this)) {
                        return;
                    }
                }

                if (!SwatchHelper.isSwatchListValid(caddisflyApp.getCurrentTestInfo().getSwatches())) {
                    alertCalibrationIncomplete();
                    return;
                }

                long milliseconds = PreferencesUtil.getLong(this,
                        CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                        R.string.calibrationExpiryDateKey);
                if (milliseconds != -1 && milliseconds <= new Date().getTime()) {
                    alertCalibrationExpired();
                    return;
                }

                final Intent intent = new Intent();
                intent.putExtra("isExternal", mIsExternalAppCall);
                if (caddisflyApp.getCurrentTestInfo().getCanUseDilution()) {
                    intent.setClass(context, SelectDilutionActivity.class);
                } else {
                    if (AppPreferences.useExternalCamera()) {
                        intent.setClass(getBaseContext(), ColorimetryLiquidExternalActivity.class);
                    } else {
                        intent.setClass(getBaseContext(), ColorimetryLiquidActivity.class);
                    }
                }

                intent.putExtra("caddisflyResourceUuid", caddisflyResourceUuid);
                startActivityForResult(intent, REQUEST_TEST);

                break;
            case COLORIMETRIC_STRIP:

                final Intent colorimetricStripIntent = new Intent(context, BrandInfoActivity.class);
                colorimetricStripIntent.putExtra(Constant.UUID, caddisflyResourceUuid);
                startActivityForResult(colorimetricStripIntent, REQUEST_TEST);

                break;
            case SENSOR:

                //Only start the sensor activity if the device supports 'On The Go'(OTG) feature
                boolean hasOtg = getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                if (hasOtg) {
                    final Intent sensorIntent = new Intent(context, SensorActivity.class);
                    sensorIntent.putExtra("caddisflyResourceUuid", caddisflyResourceUuid);
                    startActivityForResult(sensorIntent, REQUEST_TEST);
                } else {
                    alertFeatureNotSupported();
                }
                break;
            case TURBIDITY_COLIFORMS:

                final Intent turbidityIntent = new Intent(context, TimeLapseActivity.class);
                startActivityForResult(turbidityIntent, REQUEST_TEST);

                break;
        }
    }

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s%n%n%s", getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showAlert(this, R.string.notSupported, message,
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    //return the test result to the external app
                    Intent intent = new Intent(getIntent());
                    intent.putExtra("response", data.getStringExtra("response"));
                    intent.putExtra("image", data.getStringExtra("image"));
                    this.setResult(Activity.RESULT_OK, intent);
                }
                finish();
                break;
            default:
        }
    }

    /**
     * Alert displayed when an unsupported contaminant test type was requested
     */
    private void alertTestTypeNotSupported() {

        String message = getString(R.string.errorTestNotAvailable);
        message = String.format("%s%n%n%s", message, getString(R.string.pleaseContactSupport));

        AlertUtil.showAlert(this, R.string.cannotStartTest, message,
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                }
        );
    }

    @NonNull
    private String getTestName(String title) {
        //ensure we have short name to display as title
        String itemName;
        if (title.length() > 0) {
            if (title.length() > 30) {
                title = title.substring(0, 30);
            }
            itemName = title.substring(0, Math.max(0, title.length() - 7)).trim();
        } else {
            itemName = getString(R.string.error);
        }
        return itemName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Handler to restart the app after language has been changed
     */
    private static class WeakRefHandler extends Handler {
        private final WeakReference<Activity> ref;

        WeakRefHandler(Activity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity f = ref.get();
            if (f != null) {
                f.recreate();
            }
        }
    }
}