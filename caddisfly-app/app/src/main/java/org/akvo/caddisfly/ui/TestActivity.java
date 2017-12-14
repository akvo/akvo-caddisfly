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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.ActivityTestBinding;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.ErrorMessages;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.sensor.bluetooth.DeviceScanActivity;
import org.akvo.caddisfly.sensor.cbt.CbtResultFragment;
import org.akvo.caddisfly.sensor.cbt.CompartmentBagFragment;
import org.akvo.caddisfly.sensor.liquid.ChamberTestActivity;
import org.akvo.caddisfly.sensor.manual.MeasurementInputFragment;
import org.akvo.caddisfly.sensor.striptest.ui.StripMeasureActivity;
import org.akvo.caddisfly.sensor.usb.SensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.util.StringUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

import static org.akvo.caddisfly.common.AppConfig.FILE_PROVIDER_AUTHORITY_URI;

@SuppressWarnings("deprecation")
public class TestActivity extends BaseActivity implements
        CompartmentBagFragment.OnCompartmentBagSelectListener,
        MeasurementInputFragment.OnSubmitResultListener {

    private static final int CBT_TEST = 1;
    private static final int MANUAL_TEST = 2;
    private static final int SENSOR_TEST = 3;
    private static final int BLUETOOTH_TEST = 4;
    private static final int CHAMBER_TEST = 5;
    private static final String MESSAGE_TWO_LINE_FORMAT = "%s%n%n%s";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final WeakRefHandler handler = new WeakRefHandler(this);
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);

    private final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final String[] bluetoothPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION};

    private String mCurrentPhotoPath;
    private String imageFileName = "";
    private ActivityTestBinding b;
    // track if the call was made internally or from an external app
    private boolean mIsExternalAppCall = false;
    // old versions of the survey app does not expect image in result
    private boolean mCallerExpectsImageInResult = true;
    // the language requested by the external app
    private String mExternalAppLanguageCode;
    private TestInfo testInfo;
    private String mResult = "00000";
    private FragmentManager fragmentManager;
    private boolean cameraIsOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = DataBindingUtil.setContentView(this, R.layout.activity_test);

        fragmentManager = getSupportFragmentManager();

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

            String uuid = intent.getStringExtra(SensorConstants.RESOURCE_ID);

            mIsExternalAppCall = true;
            mExternalAppLanguageCode = intent.getStringExtra(SensorConstants.LANGUAGE);
            CaddisflyApp.getApp().setAppLanguage(mExternalAppLanguageCode, mIsExternalAppCall, handler);
            String questionTitle = intent.getStringExtra(SensorConstants.QUESTION_TITLE);

            if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction())) {

                // old version of survey does not expect image in result
                mCallerExpectsImageInResult = false;
            }

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

        if (testInfo != null && testInfo.getSubtype() == TestType.SENSOR) {
            if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
                ErrorMessages.alertFeatureNotSupported(this, true);
            }
        }

        if (testInfo != null && testInfo.getSubtype() == TestType.COLORIMETRIC_LIQUID) {
            List<Calibration> calibrations = CaddisflyApp.getApp().getDB()
                    .calibrationDao().getAll(testInfo.getUuid());

            if (calibrations.size() < 1) {
                TestConfigRepository testConfigRepository = new TestConfigRepository();
                testConfigRepository.addCalibration(testInfo);
                calibrations = CaddisflyApp.getApp().getDB()
                        .calibrationDao().getAll(testInfo.getUuid());
            }

            testInfo.setCalibrations(calibrations);

            if (!SwatchHelper.isSwatchListValid(testInfo)) {
                ErrorMessages.alertCalibrationIncomplete(this, testInfo);
                return;
            }

            CalibrationDetail calibrationDetail = CaddisflyApp.getApp().getDB()
                    .calibrationDao().getCalibrationDetails(testInfo.getUuid());

            if (calibrationDetail != null) {
                long milliseconds = calibrationDetail.expiry;
                if (milliseconds != -1 && milliseconds <= new Date().getTime()) {
                    ErrorMessages.alertCalibrationExpired(this);
                }
            }
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            startTest();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        b.getRoot().invalidate();
        b.getRoot().requestLayout();
    }

    public void onInstructionsClick(View view) {

        InstructionFragment instructionFragment = InstructionFragment.getInstance(testInfo);

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("instructions")
                .replace(R.id.fragment_container,
                        instructionFragment, null).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickIncubationTimes(View view) {
        DialogFragment newFragment = new IncubationTimesDialogFragment();
        newFragment.show(getSupportFragmentManager(), "incubationTimes");
    }

    public void onSiteLinkClick(View view) {
        String url = testInfo.getBrandUrl();
        if (!url.contains("http://")) {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public void onStartTestClick(View view) {

        String[] checkPermissions = permissions;
        if (testInfo.getSubtype() == TestType.BLUETOOTH) {
            checkPermissions = bluetoothPermissions;
        }

        if (permissionsDelegate.hasPermissions(checkPermissions)) {
            startTest();
        } else {
            permissionsDelegate.requestPermissions(checkPermissions);
        }

    }

    private void startBluetoothTest() {
        final Intent intent = new Intent(this, DeviceScanActivity.class);
        intent.putExtra("internal", true);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, BLUETOOTH_TEST);
    }

    private void startTest() {
        switch (testInfo.getSubtype()) {
            case BLUETOOTH:
                startBluetoothTest();
                break;
            case CBT:
                startCbtTest();
                break;
            case COLORIMETRIC_LIQUID:
                startChamberTest();
                break;
            case COLORIMETRIC_STRIP:
                if (cameraIsOk) {
                    startStripTest();
                } else {
                    checkCameraMegaPixel();
                }
                break;
            case MANUAL:
                startManualTest();
                break;
            case SENSOR:
                startSensorTest();
                break;
        }
    }

    private void startSensorTest() {
        //Only start the sensor activity if the device supports 'On The Go'(OTG) feature
        boolean hasOtg = getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
        if (hasOtg) {
            final Intent sensorIntent = new Intent(this, SensorActivity.class);
            sensorIntent.putExtra(ConstantKey.TEST_INFO, testInfo);
            startActivityForResult(sensorIntent, SENSOR_TEST);
        } else {
            ErrorMessages.alertFeatureNotSupported(this, true);
        }
    }

    private void startStripTest() {
        Intent intent;
        intent = new Intent(this, StripMeasureActivity.class);
        intent.putExtra("internal", true);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivity(intent);
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
            startActivityForResult(intent, CHAMBER_TEST);
        }
    }

    private void startManualTest() {
        if (testInfo.getHasImage()) {

            (new Handler()).postDelayed(() -> {
                Toast toast = Toast.makeText(this, R.string.take_photo_meter_result, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 200);
                toast.show();
            }, 400);

            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {

                    Uri photoURI;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        photoURI = Uri.fromFile(photoFile);
                    } else {
                        photoURI = FileProvider.getUriForFile(this,
                                FILE_PROVIDER_AUTHORITY_URI,
                                photoFile);
                    }
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(pictureIntent, MANUAL_TEST);
                }
            }
        } else {
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.fragment_container,
                    MeasurementInputFragment.newInstance(testInfo), "tubeFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void startCbtTest() {
        (new Handler()).postDelayed(() -> {
            Toast toast = Toast.makeText(this, R.string.take_photo_compartments, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.show();
        }, 400);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {

                Uri photoURI;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    photoURI = Uri.fromFile(photoFile);
                } else {
                    photoURI = FileProvider.getUriForFile(this,
                            FILE_PROVIDER_AUTHORITY_URI,
                            photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CBT_TEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            switch (requestCode) {
                case CBT_TEST:
                    fragmentTransaction.replace(R.id.fragment_container,
                            CompartmentBagFragment.newInstance(mResult), "compartmentFragment")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .addToBackStack(null)
                            .commit();
                    break;
                case BLUETOOTH_TEST:
                    //return the test result to the external app
                    Intent intent1 = new Intent(getIntent());

                    intent1.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE));

                    this.setResult(Activity.RESULT_OK, intent1);
                    finish();
                    break;
                case CHAMBER_TEST:
                    //return the test result to the external app
                    Intent intent2 = new Intent(getIntent());

                    //todo: remove when obsolete
                    if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent2.getAction())
                            && data.hasExtra(SensorConstants.RESPONSE_COMPAT)) {
                        //if survey from old version server then don't send json response
                        intent2.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE_COMPAT));
                    } else {
                        intent2.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE));
                        if (mCallerExpectsImageInResult) {
                            intent2.putExtra(SensorConstants.IMAGE, data.getStringExtra(SensorConstants.IMAGE));
                        }
                    }

                    this.setResult(Activity.RESULT_OK, intent2);
                    finish();

                    break;
                case MANUAL_TEST:
                    fragmentTransaction.replace(R.id.fragment_container,
                            MeasurementInputFragment.newInstance(testInfo), "manualFragment")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .addToBackStack(null)
                            .commit();
                    break;
                case SENSOR_TEST:
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
                    finish();
                    break;
                default:
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        imageFileName = UUID.randomUUID().toString();

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFileName += ".jpg";
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onClickAcceptResult(View view) {

        Intent resultIntent = new Intent(getIntent());

        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        ImageUtil.resizeImage(mCurrentPhotoPath, resultImagePath);

        File imageFile = new File(mCurrentPhotoPath);
        if (imageFile.exists()) {
            if (!new File(mCurrentPhotoPath).delete()) {
                Toast.makeText(this, "Could not delete file", Toast.LENGTH_SHORT).show();
            }
        }

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(mResult);

        results.put(1, StringUtil.getStringResourceByName(this, mpnValue.getRiskCategory()).toString());
        results.put(2, mpnValue.getMpn());
        results.put(3, mpnValue.getConfidence());

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, -1, imageFileName);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        resultIntent.putExtra(SensorConstants.IMAGE, resultImagePath);

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    @Override
    public void onCompartmentBagSelect(String key) {
        mResult = key;
    }

    public void onClickMatchedButton(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, CbtResultFragment.newInstance(mResult), "resultFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSubmitResult(String result) {
        Intent resultIntent = new Intent(getIntent());

        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        if (mCurrentPhotoPath != null) {
            ImageUtil.resizeImage(mCurrentPhotoPath, resultImagePath);

            File imageFile = new File(mCurrentPhotoPath);
            if (imageFile.exists()) {
                if (!new File(mCurrentPhotoPath).delete()) {
                    Toast.makeText(this, "Could not delete file", Toast.LENGTH_SHORT).show();
                }
            }
        }

        results.put(1, result);

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, -1, imageFileName);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        resultIntent.putExtra(SensorConstants.IMAGE, resultImagePath);

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
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
                            -> PreferencesUtil.setBoolean(getBaseContext(), R.string.showMinMegaPixelDialogKey, !isChecked));

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

    public static class IncubationTimesDialogFragment extends DialogFragment {
        @NonNull
        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_incubation_times, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
            return builder.create();
        }
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
