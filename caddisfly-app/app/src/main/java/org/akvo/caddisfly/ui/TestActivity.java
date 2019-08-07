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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.ErrorMessages;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.bluetooth.DeviceControlActivity;
import org.akvo.caddisfly.sensor.bluetooth.DeviceScanActivity;
import org.akvo.caddisfly.sensor.cbt.CbtActivity;
import org.akvo.caddisfly.sensor.manual.ManualTestActivity;
import org.akvo.caddisfly.sensor.manual.SwatchSelectTestActivity;
import org.akvo.caddisfly.sensor.striptest.ui.StripTestActivity;
import org.akvo.caddisfly.sensor.usb.SensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.lang.ref.WeakReference;

import timber.log.Timber;

import static org.akvo.caddisfly.helper.FileHelper.cleanResultImagesFolder;
import static org.akvo.caddisfly.model.TestType.CBT;

public class TestActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private static final String MESSAGE_TWO_LINE_FORMAT = "%s%n%n%s";
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final WeakRefHandler handler = new WeakRefHandler(this);
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);

    private final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final String[] bluetoothPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION};

    private TestInfo testInfo;
    private boolean cameraIsOk = false;
    private LinearLayout mainLayout;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        Intent intent = getIntent();

        // Stop if the app version has expired
        if (ApkHelper.isAppVersionExpired(this)) {
            return;
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FragmentManager fragmentManager = getSupportFragmentManager();

        mainLayout = findViewById(R.id.mainLayout);

        if (savedInstanceState != null) {
            testInfo = savedInstanceState.getParcelable(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO);
        }

        setTitle(R.string.appName);

        if (testInfo == null) {
            String type = intent.getType();
            if (("text/plain".equals(type))
                    && AppConfig.EXTERNAL_APP_ACTION.equals(intent.getAction())) {

                getTestSelectedByExternalApp(fragmentManager, intent);
            }
        }

        if (testInfo == null) {
            return;
        }

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TestInfoFragment.getInstance(testInfo),
                            TestActivity.class.getSimpleName()).commit();
        }

        if (testInfo != null && testInfo.getSubtype() == TestType.SENSOR
                && !this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            ErrorMessages.alertFeatureNotSupported(this, true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ConstantKey.TEST_INFO, testInfo);
        super.onSaveInstanceState(outState);
    }

    private void getTestSelectedByExternalApp(FragmentManager fragmentManager, Intent intent) {

        CaddisflyApp.getApp().setAppLanguage(this,
                intent.getStringExtra(SensorConstants.LANGUAGE), true, handler);

        if (AppPreferences.getShowDebugInfo()) {
            Toast.makeText(this, "Language: " + intent.getStringExtra(SensorConstants.LANGUAGE),
                    Toast.LENGTH_LONG).show();
        }

        String questionTitle = intent.getStringExtra(SensorConstants.QUESTION_TITLE);

        String uuid = intent.getStringExtra(SensorConstants.RESOURCE_ID);

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
    public void onStartTestClick(@SuppressWarnings("unused") View view) {

        String[] checkPermissions = permissions;

        switch (testInfo.getSubtype()) {
            case SENSOR:
                startTest();
                return;
            case MANUAL:
            case MANUAL_COLOR_SELECT:
                if (!testInfo.getHasImage()) {
                    startTest();
                    return;
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
            case MANUAL:
                startManualTest();
                break;
            case MANUAL_COLOR_SELECT:
                startSwatchSelectTest();
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

    private void startSwatchSelectTest() {
        Intent intent;
        intent = new Intent(this, SwatchSelectTestActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    private void startBluetoothTest() {
        Intent intent;
        // skip scanning for device in testing mode
        if (AppPreferences.isTestMode() || AppConfig.SKIP_BLUETOOTH_SCAN) {
            intent = new Intent(this, DeviceControlActivity.class);
        } else {
            intent = new Intent(this, DeviceScanActivity.class);
        }
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    private void startCbtTest() {

        cleanResultImagesFolder();

        Intent intent = new Intent(this, CbtActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    private void startManualTest() {

        cleanResultImagesFolder();

        Intent intent = new Intent(this, ManualTestActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
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
        Intent intent = new Intent(this, StripTestActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TEST && resultCode == Activity.RESULT_OK) {
            //return the test result to the external app
            Intent intent = new Intent(data);

            if (!BuildConfig.DEBUG && !AppConfig.STOP_ANALYTICS) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, testInfo.getUuid());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, testInfo.getName());
                bundle.putString("Brand", testInfo.getBrand());
                bundle.putString("Type", testInfo.getSubtype().toString().toLowerCase());
                bundle.putString("Range", testInfo.getRanges());

                String instanceName = getIntent().getStringExtra(SensorConstants.FLOW_INSTANCE_NAME);

                if (instanceName != null && !instanceName.isEmpty()) {
                    bundle.putString("Instance", instanceName);
                    bundle.putString("InstanceTest", instanceName + "," + testInfo.getName() + "," + testInfo.getUuid());
                }

                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "test");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
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
    public void onInstructionsClick(@SuppressWarnings("unused") View view) {

        if (testInfo.getSubtype() == CBT) {
            String[] checkPermissions = permissions;
            if (permissionsDelegate.hasPermissions(checkPermissions)) {
                startTest();
            } else {
                permissionsDelegate.requestPermissions(checkPermissions);
            }
        } else {
            InstructionFragment instructionFragment = InstructionFragment.getInstance(testInfo);

            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack("instructions")
                    .replace(R.id.fragment_container,
                            instructionFragment, null).commit();
        }
    }

    /**
     * Navigate to clicked link.
     *
     * @param view the View
     */
    public void onSiteLinkClick(@SuppressWarnings("unused") View view) {
        String url = testInfo.getBrandUrl();
        if (url != null) {
            if (!url.contains("http://")) {
                url = "http://" + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    }

    @NonNull
    private String getTestName(String title) {

        String tempTitle = title;
        //ensure we have short name to display as title
        if (title != null && title.length() > 0) {
            if (title.length() > 30) {
                tempTitle = title.substring(0, 30);
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
        } else {

            String message;
            if (testInfo.getSubtype() == TestType.BLUETOOTH) {
                message = getString(R.string.location_permission);
            } else {
                message = getString(R.string.cameraAndStoragePermissions);
            }

            Snackbar snackbar = Snackbar
                    .make(mainLayout, message,
                            Snackbar.LENGTH_LONG)
                    .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(this));

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

            snackbar.setActionTextColor(typedValue.data);
            View snackView = snackbar.getView();
            TextView textView = snackView.findViewById(R.id.snackbar_text);
            textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
            textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
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
