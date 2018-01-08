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
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantJsonKey;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.ErrorMessages;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.bluetooth.DeviceControlActivity;
import org.akvo.caddisfly.sensor.bluetooth.DeviceScanActivity;
import org.akvo.caddisfly.sensor.cbt.CbtActivity;
import org.akvo.caddisfly.sensor.chamber.ChamberTestActivity;
import org.akvo.caddisfly.sensor.manual.ManualTestActivity;
import org.akvo.caddisfly.sensor.striptest.ui.StripMeasureActivity;
import org.akvo.caddisfly.sensor.usb.SensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.lang.ref.WeakReference;
import java.util.Date;

import timber.log.Timber;

@SuppressWarnings("deprecation")
public class TestActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private static final String MESSAGE_TWO_LINE_FORMAT = "%s%n%n%s";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final WeakRefHandler handler = new WeakRefHandler(this);
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);

    private final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final String[] bluetoothPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private final String[] noPermissions = {};

    // track if the call was made internally or from an external app
    private boolean isExternalAppCall = false;
    // the language requested by the external app
    private String mExternalAppLanguageCode;
    // old versions of the survey app does not expect image in result
    private boolean mCallerExpectsImageInResult = true;
    private TestInfo testInfo;
    private boolean cameraIsOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        FragmentManager fragmentManager = getSupportFragmentManager();

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

            if (testInfo != null) {
                TestInfoFragment fragment = TestInfoFragment.getInstance(testInfo);

                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment, TestActivity.class.getSimpleName()).commit();
            }
        }

        Intent intent = getIntent();
        String type = intent.getType();

        if (type != null && "text/plain".equals(type)
                && AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction())
                || AppConfig.FLOW_ACTION_CADDISFLY.equals(intent.getAction())) {

            getTestSelectedByExternalApp(fragmentManager, intent);
        }

        if (testInfo != null && testInfo.getSubtype() == TestType.SENSOR
                && !this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            ErrorMessages.alertFeatureNotSupported(this, true);
        }

        if (testInfo != null && testInfo.getSubtype() == TestType.CHAMBER_TEST) {

            if (!SwatchHelper.isSwatchListValid(testInfo)) {
                ErrorMessages.alertCalibrationIncomplete(this, testInfo);
                return;
            }

            CalibrationDetail calibrationDetail = CaddisflyApp.getApp().getDb()
                    .calibrationDao().getCalibrationDetails(testInfo.getUuid());

            if (calibrationDetail != null) {
                long milliseconds = calibrationDetail.expiry;
                if (milliseconds > 0 && milliseconds <= new Date().getTime()) {
                    ErrorMessages.alertCalibrationExpired(this);
                }
            }
        }
    }

    private void getTestSelectedByExternalApp(FragmentManager fragmentManager, Intent intent) {
        isExternalAppCall = true;
        mExternalAppLanguageCode = intent.getStringExtra(SensorConstants.LANGUAGE);
        CaddisflyApp.getApp().setAppLanguage(mExternalAppLanguageCode, isExternalAppCall, handler);
        String questionTitle = intent.getStringExtra(SensorConstants.QUESTION_TITLE);

        if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction())) {

            // old version of survey does not expect image in result
            mCallerExpectsImageInResult = false;
        }

        String uuid = intent.getStringExtra(SensorConstants.RESOURCE_ID);
        if (uuid == null) {

            //todo: remove when obsolete
            //UUID was not found so it must be old version survey, look for 5 letter code
            String code = questionTitle.trim().substring(Math.max(0, questionTitle.length() - 5)).toLowerCase();

            uuid = TestConfigHelper.getUuidFromShortCode(code);
        }

        if (uuid != null) {
            //Get the test config by uuid
            final TestListViewModel viewModel =
                    ViewModelProviders.of(this).get(TestListViewModel.class);
            testInfo = viewModel.getTestInfo(uuid);
        }

        if (testInfo == null) {
            setTitle(getTestName(questionTitle));
            alertTestTypeNotSupported();
        } else {

            TestInfoFragment fragment = TestInfoFragment.getInstance(testInfo);

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, TestActivity.class.getSimpleName()).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Stop if the app version has expired
        if (ApkHelper.isAppVersionExpired(this)) {
            return;
        }

        if (testInfo != null) {
            if (testInfo.getSubtype() == TestType.BLUETOOTH) {
                setTitle(String.format("%s. %s", testInfo.getMd610Id(), testInfo.getName()));

            } else {
                setTitle(testInfo.getName());
            }
        }
    }

    /**
     * Start the test.
     *
     * @param view the View
     */
    public void onStartTestClick(View view) {

        String[] checkPermissions = permissions;

        switch (testInfo.getSubtype()) {
            case SENSOR:
                checkPermissions = noPermissions;
                break;
            case MANUAL:
                if (!testInfo.getHasImage()) {
                    checkPermissions = noPermissions;
                }
                break;
            case BLUETOOTH:
                checkPermissions = bluetoothPermissions;
                break;
            default:
        }

        if (permissionsDelegate.hasPermissions(checkPermissions)) {
            startTest();
        } else {
            permissionsDelegate.requestPermissions(checkPermissions);
        }
    }

    private void startTest() {
        switch (testInfo.getSubtype()) {
            case BLUETOOTH:
                startBluetoothTest();
                break;
            case CBT:
                startCbtTest();
                break;
            case CHAMBER_TEST:
                startChamberTest();
                break;
            case MANUAL:
                startManualTest();
                break;
            case SENSOR:
                startSensorTest();
                break;
            case STRIP_TEST:
                if (cameraIsOk) {
                    startStripTest();
                } else {
                    checkCameraMegaPixel();
                }
                break;
            default:
        }
    }

    private void startBluetoothTest() {
        Intent intent;
        // skip scanning for device in testing mode
        if (AppPreferences.isTestMode()) {
            intent = new Intent(this, DeviceControlActivity.class);
        } else {
            intent = new Intent(this, DeviceScanActivity.class);
        }
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    private void startCbtTest() {
        Intent intent;
        intent = new Intent(this, CbtActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    private void startManualTest() {
        Intent intent;
        intent = new Intent(this, ManualTestActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    private void startChamberTest() {

        //Only start the colorimetry calibration if the device has a camera flash
        if (AppPreferences.useExternalCamera()
                || CameraHelper.hasFeatureCameraFlash(this,
                R.string.cannotStartTest, R.string.ok, null)) {

            if (!SwatchHelper.isSwatchListValid(testInfo)) {
                ErrorMessages.alertCalibrationIncomplete(this, testInfo);
                return;
            }

            Intent intent = new Intent(this, ChamberTestActivity.class);
            intent.putExtra(ConstantKey.RUN_TEST, true);
            intent.putExtra(ConstantKey.TEST_INFO, testInfo);
            startActivityForResult(intent, REQUEST_TEST);
        }
    }

    private void startSensorTest() {
        //Only start the sensor activity if the device supports 'On The Go'(OTG) feature
        boolean hasOtg = getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
        if (hasOtg) {
            final Intent sensorIntent = new Intent(this, SensorActivity.class);
            sensorIntent.putExtra(ConstantKey.TEST_INFO, testInfo);
            startActivityForResult(sensorIntent, REQUEST_TEST);
        } else {
            ErrorMessages.alertFeatureNotSupported(this, true);
        }
    }

    private void startStripTest() {
        Intent intent;
        intent = new Intent(this, StripMeasureActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TEST && resultCode == Activity.RESULT_OK) {
            //return the test result to the external app
            Intent intent = new Intent(getIntent());

            //todo: remove when obsolete
            if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction())
                    && data.hasExtra(SensorConstants.RESPONSE_COMPAT)) {
                //if survey from old version server then don't send json response
                intent.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE_COMPAT));
                intent.putExtra(SensorConstants.VALUE, data.getStringExtra(SensorConstants.RESPONSE_COMPAT));
            } else {
                intent.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE));
                if (testInfo.getHasImage() && mCallerExpectsImageInResult) {
                    intent.putExtra(ConstantJsonKey.IMAGE, data.getStringExtra(ConstantKey.IMAGE));
                }
            }

            this.setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Show Instructions for the test.
     *
     * @param view the View
     */
    public void onInstructionsClick(View view) {

        InstructionFragment instructionFragment = InstructionFragment.getInstance(testInfo);

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("instructions")
                .replace(R.id.fragment_container,
                        instructionFragment, null).commit();
    }

    /**
     * Navigate to clicked link.
     *
     * @param view the View
     */
    public void onSiteLinkClick(View view) {
        String url = testInfo.getBrandUrl();
        if (!url.contains("http://")) {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
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

    private void checkCameraMegaPixel() {

        cameraIsOk = true;
        if (PreferencesUtil.getBoolean(this, R.string.showMinMegaPixelDialogKey, true)) {
            try {

                if (CameraHelper.getMaxSupportedMegaPixelsByCamera(this) < Constants.MIN_CAMERA_MEGA_PIXELS) {

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    View checkBoxView = View.inflate(this, R.layout.dialog_message, null);
                    CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                            -> PreferencesUtil.setBoolean(getBaseContext(),
                            R.string.showMinMegaPixelDialogKey, !isChecked));

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.warning);
                    builder.setMessage(R.string.camera_not_good)
                            .setView(checkBoxView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_anyway, (dialog, id) -> startTest())
                            .setNegativeButton(R.string.stop_test, (dialog, id) -> {
                                dialog.dismiss();
                                cameraIsOk = false;
                                finish();
                            }).show();

                } else {
                    startTest();
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        } else {
            startTest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, grantResults)) {
            startTest();
        }
    }

    /**
     * Alert displayed when an unsupported contaminant test type was requested.
     */
    private void alertTestTypeNotSupported() {

        String message = getString(R.string.errorTestNotAvailable);
        message = String.format(MESSAGE_TWO_LINE_FORMAT, message, getString(R.string.pleaseContactSupport));

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

    /**
     * Show CBT incubation times instructions in a dialog.
     *
     * @param view the view
     */
    public void onClickIncubationTimes(View view) {
        DialogFragment newFragment = new CbtActivity.IncubationTimesDialogFragment();
        newFragment.show(getSupportFragmentManager(), "incubationTimes");
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
