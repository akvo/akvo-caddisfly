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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ColorimetryStripActivity;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ExternalActionActivity extends Activity {

    private static final int REQUEST_TEST = 1;
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
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String type = intent.getType();
        String mQuestionTitle;

//        ActivityManager activityManager = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
//        List<ActivityManager.RunningAppProcessInfo> processInfo = activityManager.getRunningAppProcesses();

        if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction()) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                mIsExternalAppCall = true;
                mQuestionTitle = intent.getStringExtra("questionTitle");

                //todo: fix FLOW to return language code
                mExternalAppLanguageCode = intent.getStringExtra("language").substring(0, 2).toLowerCase();

                //setAppLanguage(mExternalAppLanguageCode);

                //Extract the 5 letter code in the question and load the test config
                CaddisflyApp.getApp().loadTestConfiguration(
                        mQuestionTitle.substring(Math.max(0, mQuestionTitle.length() - 5))
                );

                if (CaddisflyApp.getApp().getCurrentTestInfo() == null) {
                    alertTestTypeNotSupported(mQuestionTitle);
                } else {
                    if (!CaddisflyApp.getApp().getCurrentTestInfo().requiresCameraFlash() ||
                            CaddisflyApp.hasFeatureCameraFlash(this, R.string.cannotStartTest,
                                    R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    }
                            )) {
                        startTest();
                    }
                }
            }
        }
    }

    private void alertCalibrationExpired() {
        String message = getString(R.string.errorCalibrationExpired,
                CaddisflyApp.getApp().getCurrentTestInfo().getName(
                        getResources().getConfiguration().locale.getLanguage()));
        message = String.format("%s\r\n\r\n%s", message,
                getString(R.string.orderFreshBatch));

        AlertUtil.showAlert(this, R.string.cannotStartTest,
                message, R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null
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
        message = String.format("%s\r\n\r\n%s", message,
                getString(R.string.doYouWantToCalibrate));

        AlertUtil.showAlert(this, R.string.cannotStartTest,
                message, R.string.calibrate,
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
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        setAppLanguage(mExternalAppLanguageCode);
    }

    /**
     * Sets the language of the app on start. The language can be one of system language, language
     * set in the app preferences or language requested via the languageCode parameter
     *
     * @param languageCode If null uses language from app preferences else uses this value
     */
    private void setAppLanguage(String languageCode) {
        assert getApplicationContext() != null;

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

        locale = new Locale(languageCode);

        //if the app language is not already set to languageCode then set it now
        if (!config.locale.getLanguage().substring(0, 2).equals(languageCode)) {

            config.locale = locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLayoutDirection(locale);
            }
            res.updateConfiguration(config, dm);

            //if this session was launched from an external app then do not restart this app
            if (!mIsExternalAppCall) {
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        }
    }

    /**
     * Start the appropriate test based on the current test type
     */
    private void startTest() {
        Context context = this;
        CaddisflyApp caddisflyApp = CaddisflyApp.getApp();
        switch (caddisflyApp.getCurrentTestInfo().getType()) {
            case COLORIMETRIC_LIQUID:

                if (!SwatchHelper.isSwatchListValid(caddisflyApp.getCurrentTestInfo().getSwatches())) {
                    alertCalibrationIncomplete();
                    return;
                }

                String key = String.format("%s_%s", CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                        R.string.calibrationExpiryDateKey);

                long milliseconds = PreferencesUtil.getLong(this, key);
                if (milliseconds != -1 && milliseconds <= new Date().getTime()) {
                    alertCalibrationExpired();
                    return;
                }

                final Intent colorimetricLiquidIntent = new Intent(context, ColorimetryLiquidActivity.class);
                startActivityForResult(colorimetricLiquidIntent, REQUEST_TEST);

                break;
            case COLORIMETRIC_STRIP:

                final Intent colorimetricStripIntent = new Intent(context, ColorimetryStripActivity.class);
                startActivityForResult(colorimetricStripIntent, REQUEST_TEST);

                break;
            case SENSOR:

                final Intent sensorIntent = new Intent(context, SensorActivity.class);
                startActivityForResult(sensorIntent, REQUEST_TEST);

                break;
        }
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
                    this.setResult(Activity.RESULT_OK, intent);
                }
                finish();
                break;
            default:
        }
    }

    /**
     * Alert displayed when an unsupported contaminant test type was requested
     *
     * @param title the name of the test contaminant
     */
    private void alertTestTypeNotSupported(String title) {

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

        String message = getString(R.string.errorTestNotAvailable, itemName);
        message = String.format("%s\r\n\r\n%s", message, getString(R.string.pleaseContactSupport));

        AlertUtil.showAlert(this, R.string.cannotStartTest, message,
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null
        );
    }

    /**
     * Handler to restart the app after language has been changed
     */
    private static class WeakRefHandler extends Handler {
        private final WeakReference<Activity> ref;

        public WeakRefHandler(Activity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity f = ref.get();
            f.recreate();
        }
    }
}
