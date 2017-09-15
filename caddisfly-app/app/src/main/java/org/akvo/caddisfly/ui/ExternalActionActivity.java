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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.cbt.TestActivity;
import org.akvo.caddisfly.sensor.colorimetry.bluetooth.BluetoothTypeListActivity;
import org.akvo.caddisfly.sensor.colorimetry.bluetooth.DeviceScanActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.SelectDilutionActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.BrandInfoActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.TestTypeListActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ExternalActionActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private static final int PERMISSION_ALL = 1;
    private static final String MESSAGE_TWO_LINE_FORMAT = "%s%n%n%s";
    private final WeakRefHandler handler = new WeakRefHandler(this);

    // track if the call was made internally or from an external app
    private boolean mIsExternalAppCall = false;

    // old versions of the survey app does not expect image in result
    private boolean mCallerExpectsImageInResult = true;

    // the test type requested
    @Nullable
    private String mTestTypeUuid;

    // the language requested by the external app
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

        // Stop if the app version has expired
        if (ApkHelper.isAppVersionExpired(this)) {
            return;
        }

        Intent intent = getIntent();
        String type = intent.getType();

        if (type != null && "text/plain".equals(type)
                && AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction())
                || AppConfig.FLOW_ACTION_CADDISFLY.equals(intent.getAction())) {

            mTestTypeUuid = intent.getStringExtra(SensorConstants.RESOURCE_ID);

            mIsExternalAppCall = true;
            mExternalAppLanguageCode = intent.getStringExtra(SensorConstants.LANGUAGE);
            CaddisflyApp.getApp().setAppLanguage(mExternalAppLanguageCode, mIsExternalAppCall, handler);
            String questionTitle = intent.getStringExtra(SensorConstants.QUESTION_TITLE);

            if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction())) {

                // old version of survey does not expect image in result
                mCallerExpectsImageInResult = false;
            }

            if (mTestTypeUuid == null) {

                //todo: remove when obsolete
                //UUID was not found so it must be old version survey, look for 5 letter code
                String code = questionTitle.trim().substring(Math.max(0, questionTitle.length() - 5)).toLowerCase();

                if (code.equalsIgnoreCase("strip")) {
                    final Intent colorimetricStripIntent = new Intent(this, TestTypeListActivity.class);
                    colorimetricStripIntent.putExtra(Constant.SEND_IMAGE_IN_RESULT, mCallerExpectsImageInResult);
                    startActivityForResult(colorimetricStripIntent, REQUEST_TEST);
                    return;
                }

                mTestTypeUuid = TestConfigHelper.getUuidFromShortCode(code);

                if (mTestTypeUuid == null) {
                    Matcher m = Pattern.compile("(\\d{3})").matcher(code.toLowerCase());
                    if (m.find()) {
                        final Intent i = new Intent(this, BluetoothTypeListActivity.class);
                        i.putExtra("testCode", m.group(1));
                        startActivityForResult(i, REQUEST_TEST);
                        return;
                    }
                }
            }

            //Get the test config by uuid
            CaddisflyApp.getApp().loadTestConfigurationByUuid(mTestTypeUuid);

            if (CaddisflyApp.getApp().getCurrentTestInfo() == null) {
                ((TextView) findViewById(R.id.textTitle)).setText(getTestName(questionTitle));
                alertTestTypeNotSupported();
            } else {
                ((TextView) findViewById(R.id.textTitle)).setText(
                        CaddisflyApp.getApp().getCurrentTestInfo().getTitle());

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
                    startTest(mTestTypeUuid);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
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
                startTest(mTestTypeUuid);
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

        String message = String.format(MESSAGE_TWO_LINE_FORMAT, getString(R.string.errorCalibrationExpired),
                getString(R.string.orderFreshBatch));

        AlertUtil.showAlert(this, R.string.cannotStartTest,
                message, R.string.ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }, null,
                dialogInterface -> {
                    dialogInterface.dismiss();
                    finish();
                }
        );
    }

    /**
     * Alert message for calibration incomplete or invalid.
     */
    private void alertCalibrationIncomplete() {

        final Activity activity = this;

        String message = getString(R.string.errorCalibrationIncomplete,
                CaddisflyApp.getApp().getCurrentTestInfo().getTitle());
        message = String.format(MESSAGE_TWO_LINE_FORMAT, message,
                getString(R.string.doYouWantToCalibrate));

        AlertUtil.showAlert(this, R.string.cannotStartTest, message, R.string.calibrate,
                (dialogInterface, i) -> {
                    final Intent intent = new Intent(getBaseContext(), CalibrateListActivity.class);
                    startActivity(intent);

                    activity.setResult(Activity.RESULT_CANCELED);
                    dialogInterface.dismiss();
                    finish();
                }, (dialogInterface, i) -> {
                    activity.setResult(Activity.RESULT_CANCELED);
                    dialogInterface.dismiss();
                    finish();
                },
                dialogInterface -> {
                    activity.setResult(Activity.RESULT_CANCELED);
                    dialogInterface.dismiss();
                    finish();
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        CaddisflyApp.getApp().setAppLanguage(mExternalAppLanguageCode, mIsExternalAppCall, handler);
    }

    /**
     * Start the appropriate test based on the current test type.
     */
    private void startTest(String uuid) {
        Context context = this;
        CaddisflyApp caddisflyApp = CaddisflyApp.getApp();

        switch (caddisflyApp.getCurrentTestInfo().getType()) {
            case BLUETOOTH:
                if (this.getPackageManager().hasSystemFeature(FEATURE_BLUETOOTH_LE)) {
                    final Intent bluetoothIntent = new Intent();
                    bluetoothIntent.putExtra(SensorConstants.IS_EXTERNAL_ACTION, mIsExternalAppCall);
                    bluetoothIntent.setClass(getBaseContext(), DeviceScanActivity.class);
                    bluetoothIntent.putExtra(Constant.UUID, uuid);
                    bluetoothIntent.putExtra(Constant.SEND_IMAGE_IN_RESULT, mCallerExpectsImageInResult);
                    startActivityForResult(bluetoothIntent, REQUEST_TEST);
                } else {
                    alertFeatureNotSupported();
                }
                break;

            case COLORIMETRIC_LIQUID:

                if (!AppPreferences.useExternalCamera()
                        && !CameraHelper.hasFeatureCameraFlash(this, R.string.cannotStartTest,
                        R.string.ok, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            finish();
                        }
                )) {
                    return;
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && ApiUtil.isCameraInUse(this, this)) {
                    return;
                }

                if (!SwatchHelper.isSwatchListValid(caddisflyApp.getCurrentTestInfo())) {
                    alertCalibrationIncomplete();
                    return;
                }

                long milliseconds = PreferencesUtil.getLong(this,
                        CaddisflyApp.getApp().getCurrentTestInfo().getId(),
                        R.string.calibrationExpiryDateKey);
                if (milliseconds != -1 && milliseconds <= new Date().getTime()) {
                    alertCalibrationExpired();
                    return;
                }

                final Intent intent = new Intent();
                intent.putExtra(SensorConstants.IS_EXTERNAL_ACTION, mIsExternalAppCall);
                if (caddisflyApp.getCurrentTestInfo().getCanUseDilution()) {
                    intent.setClass(context, SelectDilutionActivity.class);
                } else {
                    intent.setClass(getBaseContext(), ColorimetryLiquidActivity.class);
                }

                intent.putExtra(Constant.UUID, uuid);
                intent.putExtra(Constant.SEND_IMAGE_IN_RESULT, mCallerExpectsImageInResult);
                startActivityForResult(intent, REQUEST_TEST);

                break;
            case COLORIMETRIC_STRIP:

                final Intent colorimetricStripIntent = new Intent(context, BrandInfoActivity.class);
                colorimetricStripIntent.putExtra(Constant.UUID, uuid);
                colorimetricStripIntent.putExtra(Constant.SEND_IMAGE_IN_RESULT, mCallerExpectsImageInResult);
                startActivityForResult(colorimetricStripIntent, REQUEST_TEST);

                break;
            case SENSOR:

                //Only start the sensor activity if the device supports 'On The Go'(OTG) feature
                boolean hasOtg = getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                if (hasOtg) {
                    final Intent sensorIntent = new Intent(context, SensorActivity.class);
                    sensorIntent.putExtra(Constant.UUID, uuid);
                    startActivityForResult(sensorIntent, REQUEST_TEST);
                } else {
                    alertFeatureNotSupported();
                }
                break;
            case CBT:

                TestConfigHelper.loadTestByUuid(SensorConstants.CBT_ID);
                final Intent cbtIntent = new Intent(getBaseContext(), TestActivity.class);
                cbtIntent.putExtra("internal", true);
                startActivityForResult(cbtIntent, REQUEST_TEST);
                break;
            default:
        }
    }

    /**
     * Alert shown when a feature is not supported by the device.
     */
    private void alertFeatureNotSupported() {
        String message = String.format(ExternalActionActivity.MESSAGE_TWO_LINE_FORMAT, getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showAlert(this, R.string.notSupported, message,
                R.string.ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }, null,
                dialogInterface -> {
                    dialogInterface.dismiss();
                    finish();
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    //return the test result to the external app
                    Intent intent = new Intent(getIntent());

                    //todo: remove when obsolete
                    if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction())
                            && data.hasExtra(SensorConstants.RESPONSE_COMPAT)) {
                        //if survey from old version server then don't send json response
                        intent.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE_COMPAT));
                    } else {
                        intent.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE));
                        if (mCallerExpectsImageInResult) {
                            intent.putExtra(SensorConstants.IMAGE, data.getStringExtra(SensorConstants.IMAGE));
                        }
                    }

                    this.setResult(Activity.RESULT_OK, intent);
                }
                finish();
                break;
            default:
        }
    }

    /**
     * Alert displayed when an unsupported contaminant test type was requested.
     */
    private void alertTestTypeNotSupported() {

        String message = getString(R.string.errorTestNotAvailable);
        message = String.format(ExternalActionActivity.MESSAGE_TWO_LINE_FORMAT, message, getString(R.string.pleaseContactSupport));

        AlertUtil.showAlert(this, R.string.cannotStartTest, message,
                R.string.ok,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }, null,
                dialogInterface -> {
                    dialogInterface.dismiss();
                    finish();
                }
        );
    }

    @NonNull
    @Deprecated
    private String getTestName(@NonNull String title) {

        String tempTitle = title;
        //ensure we have short name to display as title
        if (title.length() > 0) {
            if (title.length() > 30) {
                tempTitle = title.substring(0, 30);
            }
            if (title.contains("-")) {
                tempTitle = title.substring(0, title.indexOf("-")).trim();
            }
        } else {
            tempTitle = getString(R.string.error);
        }
        return tempTitle;
    }

    /**
     * Handler to restart the app after language has been changed.
     */
    private static class WeakRefHandler extends Handler {
        @NonNull
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
