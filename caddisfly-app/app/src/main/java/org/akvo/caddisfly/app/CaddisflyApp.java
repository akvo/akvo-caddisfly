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

package org.akvo.caddisfly.app;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.Arrays;
import java.util.Locale;

import timber.log.Timber;

public class CaddisflyApp extends Application {

    private static final String DATABASE_NAME = "calibration";
    private static CalibrationDatabase database;
    private static CaddisflyApp app; // Singleton

    /**
     * Gets the singleton app object.
     *
     * @return the singleton app
     */
    public static CaddisflyApp getApp() {
        return app;
    }

    private static void setApp(CaddisflyApp value) {
        app = value;
    }

    public static String getAppLanguage() {
        Configuration config = getApp().getResources().getConfiguration();
        return config.locale.getLanguage().substring(0, 2);
    }

    /**
     * Gets the app version.
     *
     * @return The version name and number
     */
    public static String getAppVersion(boolean isDiagnostic) {
        String version = "";
        try {
            Context context = getApp();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            if (isDiagnostic) {
                version = String.format("%s (Build %s)", packageInfo.versionName, packageInfo.versionCode);
            } else {
                version = String.format("%s %s", context.getString(R.string.version),
                        packageInfo.versionName);
            }

        } catch (PackageManager.NameNotFoundException ignored) {
            // do nothing
        }
        return version;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setApp(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        app = this;

        database = Room.databaseBuilder(getApplicationContext(),
                CalibrationDatabase.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public CalibrationDatabase getDb() {
        return database;
    }

    /**
     * Sets the language of the app on start. The language can be one of system language, language
     * set in the app preferences or language requested via the languageCode parameter
     *
     * @param languageCode If null uses language from app preferences else uses this value
     */
    public void setAppLanguage(String languageCode, boolean isExternal, Handler handler) {

        try {
            Locale locale;

            String code = languageCode;

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

            if (code == null || !Arrays.asList(supportedLanguages).contains(code)) {
                //if requested language code is not supported then use language from preferences
                code = PreferencesUtil.getString(this, R.string.languageKey, "");
                if (!Arrays.asList(supportedLanguages).contains(code)) {
                    //no language was selected in the app settings so use the system language
                    String currentLanguage = getResources().getConfiguration().locale.getLanguage();
                    if (currentLanguage.equals(currentSystemLanguage)) {
                        //app is already set to correct language
                        return;
                    } else if (Arrays.asList(supportedLanguages).contains(currentSystemLanguage)) {
                        //set to system language
                        code = currentSystemLanguage;
                    } else {
                        //no supported languages found just default to English
                        code = "en";
                    }
                }
            }

            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration config = res.getConfiguration();

            locale = new Locale(code, Locale.getDefault().getCountry());

            //if the app language is not already set to languageCode then set it now
            if (!config.locale.getLanguage().substring(0, 2).equalsIgnoreCase(code)
                    || !config.locale.getCountry().equalsIgnoreCase(Locale.getDefault().getCountry())) {

                config.locale = locale;
                config.setLayoutDirection(locale);
                res.updateConfiguration(config, dm);

                //if this session was launched from an external app then do not restart this app
                if (!isExternal && handler != null) {
                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
            }
        } catch (Exception ignored) {
            // do nothing
        }
    }
}
