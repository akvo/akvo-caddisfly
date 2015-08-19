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

package org.akvo.caddisfly.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import org.akvo.caddisfly.helper.ConfigHelper;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.Locale;

public class CaddisflyApp extends Application {

    private static boolean hasCameraFlash;
    private static boolean checkedForFlash;
    private static CaddisflyApp app;// Singleton
    public TestInfo currentTestInfo = new TestInfo();

    /**
     * Check if the device has a camera flash
     *
     * @param context the context
     * @return true if camera flash exists otherwise false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasFeatureCameraFlash(Context context) {
        //check only once for flash
        if (!checkedForFlash) {
            hasCameraFlash = ApiUtil.hasCameraFlash(context);
            checkedForFlash = true;
        }
        return hasCameraFlash;
    }

    /**
     * Gets the singleton app object
     *
     * @return the singleton app
     */
    public static CaddisflyApp getApp() {
        return app;
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

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    /**
     * Initialize the current test by loading the configuration and calibration information
     */
    public void initializeCurrentTest() {
        if (currentTestInfo.getCode().isEmpty()) {
            setDefaultTest();
        } else {
            loadTestConfiguration(currentTestInfo.getCode());
        }
    }

    /**
     * Select the first test type in the configuration file as the current test
     */
    public void setDefaultTest() {

        ArrayList<TestInfo> tests;
        tests = ConfigHelper.loadConfigurationsForAllTests(FileHelper.getConfigJson());
        if (tests.size() > 0) {
            currentTestInfo = tests.get(0);
            if (currentTestInfo.getType() == TestType.COLORIMETRIC_LIQUID) {
                loadCalibratedSwatches(currentTestInfo);
            }
        }
    }

    /**
     * Load the test configuration for the given test code
     *
     * @param testCode the test code
     */
    public void loadTestConfiguration(String testCode) {

        currentTestInfo = ConfigHelper.loadTestConfigurationByCode(
                FileHelper.getConfigJson(), testCode.toUpperCase());

        if (currentTestInfo != null) {
            if (currentTestInfo.getType() == TestType.COLORIMETRIC_LIQUID) {
                loadCalibratedSwatches(currentTestInfo);
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
        COLORIMETRIC_LIQUID, COLORIMETRIC_STRIP, SENSOR, TURBIDITY_COLIFORMS
    }

}
