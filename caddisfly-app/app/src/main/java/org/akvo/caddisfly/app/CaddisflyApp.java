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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class CaddisflyApp extends Application {

    private static CaddisflyApp app; // Singleton
    private TestInfo mCurrentTestInfo = new TestInfo();

    /**
     * Gets the singleton app object
     *
     * @return the singleton app
     */
    public static CaddisflyApp getApp() {
        return app;
    }

    public static String getAppLanguage() {
        Configuration config = getApp().getResources().getConfiguration();
        return config.locale.getLanguage().substring(0, 2);
    }

    /**
     * Gets the app version
     *
     * @return The version name and number
     */
    public static String getAppVersion() {
        String version = "";
        try {
            Context context = getApp();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            if (AppPreferences.isDiagnosticMode()) {
                version = String.format("%s %s (Build %s)", context.getString(R.string.version),
                        packageInfo.versionName, packageInfo.versionCode);
            } else {
                version = String.format("%s %s", context.getString(R.string.version),
                        packageInfo.versionName);
            }

        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return version;
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
        if (mCurrentTestInfo == null || mCurrentTestInfo.getUnit().isEmpty()) {
            setDefaultTest();
        } else {
            loadTestConfigurationByUuid(mCurrentTestInfo.getUuid().get(0));
        }
    }

    /**
     * Select the first test type in the configuration file as the current test
     */
    public void setDefaultTest() {

        ArrayList<TestInfo> tests;
        tests = TestConfigHelper.loadTestsList();
        if (tests.size() > 0) {
            mCurrentTestInfo = tests.get(0);
            if (mCurrentTestInfo.getType() == TestType.COLORIMETRIC_LIQUID) {
                loadCalibratedSwatches(mCurrentTestInfo);
            }
        }
    }

    /**
     * Load the test configuration for the given uuid
     *
     * @param uuid the uuid of the test
     */
    public void loadTestConfigurationByUuid(String uuid) {

        mCurrentTestInfo = TestConfigHelper.loadTestByUuid(uuid);

        if (mCurrentTestInfo != null && mCurrentTestInfo.getType() == TestType.COLORIMETRIC_LIQUID) {
            loadCalibratedSwatches(mCurrentTestInfo);

            if (SwatchHelper.getCalibratedSwatchCount(mCurrentTestInfo.getSwatches()) == 0) {
                try {
                    SwatchHelper.loadCalibrationFromFile(getBaseContext(), "_AutoBackup");
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Load any user calibrated swatches
     *
     * @param testInfo The type of test
     */
    public void loadCalibratedSwatches(TestInfo testInfo) {

        final Context context = getApplicationContext();
        for (Swatch swatch : testInfo.getSwatches()) {
            String key = String.format(Locale.US, "%s-%.2f", testInfo.getCode(), swatch.getValue());
            swatch.setColor(PreferencesUtil.getInt(context, key, 0));
        }
    }

    /**
     * Sets the language of the app on start. The language can be one of system language, language
     * set in the app preferences or language requested via the languageCode parameter
     *
     * @param languageCode If null uses language from app preferences else uses this value
     */
    public void setAppLanguage(String languageCode, boolean isExternal, Handler handler) {

        Locale locale;

        //the languages supported by the app
        String[] supportedLanguages = getResources().getStringArray(R.array.language_codes);

        //the current system language set in the device settings
        String currentSystemLanguage = Locale.getDefault().getLanguage().substring(0, 2);

        //the language the system was set to the last time the app was run
        String previousSystemLanguage = PreferencesUtil.getString(this, R.string.systemLanguageKey, "");

        //if the system language was changed in the device settings then set that as the app language
        if (!previousSystemLanguage.equals(currentSystemLanguage)
                && Arrays.asList(supportedLanguages).contains(currentSystemLanguage)) {
            PreferencesUtil.setString(this, R.string.systemLanguageKey, currentSystemLanguage);
            PreferencesUtil.setString(this, R.string.languageKey, currentSystemLanguage);
        }

        if (languageCode == null || !Arrays.asList(supportedLanguages).contains(languageCode)) {
            //if requested language code is not supported then use language from preferences
            languageCode = PreferencesUtil.getString(this, R.string.languageKey, "");
            if (!Arrays.asList(supportedLanguages).contains(languageCode)) {
                //no language was selected in the app settings so use the system language
                String currentLanguage = getResources().getConfiguration().locale.getLanguage();
                if (currentLanguage.equals(currentSystemLanguage)) {
                    //app is already set to correct language
                    return;
                } else if (Arrays.asList(supportedLanguages).contains(currentSystemLanguage)) {
                    //set to system language
                    languageCode = currentSystemLanguage;
                } else {
                    //no supported languages found just default to English
                    languageCode = "en";
                }
            }
        }

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();

        locale = new Locale(languageCode, Locale.getDefault().getCountry());

        //if the app language is not already set to languageCode then set it now
        if (!config.locale.getLanguage().substring(0, 2).equalsIgnoreCase(languageCode)
                || !config.locale.getCountry().equalsIgnoreCase(Locale.getDefault().getCountry())) {

            config.locale = locale;
            config.setLayoutDirection(locale);
            res.updateConfiguration(config, dm);

            //if this session was launched from an external app then do not restart this app
            if (!isExternal) {
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        }
    }
}
