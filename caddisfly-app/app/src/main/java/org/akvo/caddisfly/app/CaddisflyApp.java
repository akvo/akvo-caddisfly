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

package org.akvo.caddisfly.app;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.StringRes;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.Locale;

public class CaddisflyApp extends Application {

    private static boolean hasCameraFlash;
    private static CaddisflyApp app;// Singleton
    private TestInfo mCurrentTestInfo = new TestInfo();

    /**
     * Gets the singleton app object
     *
     * @return the singleton app
     */
    public static CaddisflyApp getApp() {
        return app;
    }

    /**
     * Check if the camera is available
     *
     * @param context         the context
     * @param onClickListener positive button listener
     * @return true if camera flash exists otherwise false
     */
    @SuppressWarnings("deprecation")
    public static Camera getCamera(Context context,
                                   DialogInterface.OnClickListener onClickListener) {

        Camera camera = ApiUtil.getCameraInstance();
        if (hasFeatureBackCamera(context, onClickListener) && camera == null) {
            String message = String.format("%s\r\n\r\n%s",
                    context.getString(R.string.cannotUseCamera),
                    context.getString(R.string.tryRestarting));

            AlertUtil.showError(context, R.string.cameraBusy,
                    message, null, R.string.ok, onClickListener, null);
            return null;
        }

        return camera;
    }

    private static boolean hasFeatureBackCamera(Context context,
                                                DialogInterface.OnClickListener onClickListener) {
        PackageManager packageManager = context.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            AlertUtil.showAlert(context, R.string.cameraNotAvailable,
                    R.string.cameraRequired,
                    R.string.ok, onClickListener, null);
            return false;
        }
        return true;
    }

    /**
     * Check if the device has a camera flash
     *
     * @param context         the context
     * @param onClickListener positive button listener
     * @return true if camera flash exists otherwise false
     */
    public static boolean hasFeatureCameraFlash(Context context, @StringRes int errorTitle,
                                                @StringRes int buttonText,
                                                DialogInterface.OnClickListener onClickListener) {
        if (PreferencesUtil.containsKey(context, R.string.hasCameraFlashKey)) {
            hasCameraFlash = PreferencesUtil.getBoolean(context, R.string.hasCameraFlashKey, false);
        } else {

            @SuppressWarnings("deprecation")
            Camera camera = getCamera(context, onClickListener);
            try {
                if (camera != null) {
                    hasCameraFlash = ApiUtil.hasCameraFlash(context, camera);
                    PreferencesUtil.setBoolean(context, R.string.hasCameraFlashKey, hasCameraFlash);
                }
            } finally {
                if (camera != null) {
                    camera.release();
                }

            }
        }

        if (!hasCameraFlash) {
            AlertUtil.showAlert(context, errorTitle,
                    R.string.errorCameraFlashRequired,
                    buttonText, onClickListener, null);

        }
        return hasCameraFlash;
    }

    /**
     * Gets the app version
     *
     * @param context The context
     * @return The version name and number
     */
    public static String getAppVersion(Context context) {
        try {
            String version = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            String[] words = version.split("\\s");
            String versionString = "";
            for (String word : words) {
                try {
                    Double versionNumber = Double.parseDouble(word);
                    versionString += String.format(Locale.US, "%.2f", versionNumber);
                } catch (NumberFormatException e) {
                    int id = context.getResources()
                            .getIdentifier(word.toLowerCase(), "string", context.getPackageName());
                    if (id > 0) {
                        versionString += context.getString(id);
                    } else {
                        versionString += word;
                    }
                }
                versionString += " ";
            }
            return versionString.trim();

        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    /**
     * Gets the current TestInfo
     *
     * @return the current test info
     */
    public TestInfo getCurrentTestInfo() {
        return mCurrentTestInfo;
    }

    public void setCurrentTestInfo(TestInfo testInfo) {
        mCurrentTestInfo = testInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    /**
     * Initialize the current test by loading the configuration and calibration information
     */
    public void initializeCurrentTest() {
        if (mCurrentTestInfo == null || mCurrentTestInfo.getCode().isEmpty()) {
            setDefaultTest();
        } else {
            loadTestConfiguration(mCurrentTestInfo.getCode());
        }
    }

    /**
     * Select the first test type in the configuration file as the current test
     */
    public void setDefaultTest() {

        ArrayList<TestInfo> tests;
        tests = TestConfigHelper.loadConfigurationsForAllTests(FileHelper.getConfigJson());
        if (tests.size() > 0) {
            mCurrentTestInfo = tests.get(0);
            if (mCurrentTestInfo.getType() == TestType.COLORIMETRIC_LIQUID) {
                loadCalibratedSwatches(mCurrentTestInfo);
            }
        }
    }

    /**
     * Load the test configuration for the given test code
     *
     * @param testCode the test code
     */
    public void loadTestConfiguration(String testCode) {

        mCurrentTestInfo = TestConfigHelper.loadTestConfigurationByCode(
                FileHelper.getConfigJson(), testCode.toUpperCase());

        if (mCurrentTestInfo != null) {
            if (mCurrentTestInfo.getType() == TestType.COLORIMETRIC_LIQUID) {
                loadCalibratedSwatches(mCurrentTestInfo);
            }
        }
    }

    /**
     * Load any user calibrated swatches
     *
     * @param testInfo The type of test
     */
    public void loadCalibratedSwatches(TestInfo testInfo) {

        for (Swatch swatch : testInfo.getSwatches()) {
            String key = String.format(Locale.US, "%s-%.2f", testInfo.getCode(), swatch.getValue());
            swatch.setColor(PreferencesUtil.getInt(this.getApplicationContext(), key, 0));
        }
    }

    /**
     * The different types of testing methods
     */
    public enum TestType {
        /**
         * Liquid reagent is mixed with sample and color is analysed from the resulting
         * color change in the solution
         */
        COLORIMETRIC_LIQUID,

        /**
         * Strip paper is dipped into the sample and color is analysed from the resulting
         * color change on the strip paper
         */
        COLORIMETRIC_STRIP,

        /**
         * External sensors connected to the phone/device
         */
        SENSOR,

        /**
         * Measure of turbidity in the liquid
         */
        TURBIDITY_COLIFORMS
    }

}
