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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
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

public class MainActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private final WeakRefHandler handler = new WeakRefHandler(this);
    //tracks whether this app was launched by an external app
    private Boolean mIsExternalAppCall = false;
    //tracks if the app should automatically close (after launching an external app)
    //the language requested by the external app
    private String mExternalAppLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fabDisableDiagnostics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                        Toast.LENGTH_SHORT).show();

                AppPreferences.disableDiagnosticMode();

                switchLayoutForDiagnosticOrUserMode();

                changeActionBarStyleBasedOnCurrentMode();
            }
        });

        findViewById(R.id.buttonCalibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getBaseContext(), TypeListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        findViewById(R.id.buttonEcSensor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CaddisflyApp.getApp().loadTestConfiguration("ECOND");
                final Intent intent = new Intent(getBaseContext(), SensorActivity.class);
                intent.putExtra("internal", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        Button startSurveyButton = (Button) findViewById(R.id.buttonGotoSurvey);
        startSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSurvey();
            }
        });

//        final Context context = this;
//        findViewById(R.id.layoutOpenApp).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertUtil.showAlert(context, R.string.closing, R.string.appWillClose, R.string.ok,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                finish();
//                            }
//                        }, null);
//            }
//        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        switchLayoutForDiagnosticOrUserMode();

        setAppLanguage(mExternalAppLanguageCode);
    }

    /**
     * Show the diagnostic mode layout
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode()) {
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutSlogan).setVisibility(View.GONE);
            findViewById(R.id.mainLayout).setBackgroundResource(R.drawable.diagnostic_gradient);
        } else {
            if (findViewById(R.id.layoutDiagnostics).getVisibility() == View.VISIBLE) {
                findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
                findViewById(R.id.layoutSlogan).setVisibility(View.VISIBLE);
                findViewById(R.id.mainLayout).setBackgroundResource(R.drawable.gradient);
            }
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.actionSettings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String type = intent.getType();
        String mQuestionTitle;

        if (AppConfig.FLOW_ACTION_EXTERNAL_SOURCE.equals(intent.getAction()) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                mIsExternalAppCall = true;
                mQuestionTitle = intent.getStringExtra("questionTitle");

                //todo: fix FLOW to return language code
                mExternalAppLanguageCode = intent.getStringExtra("language").substring(0, 2).toLowerCase();

                setAppLanguage(mExternalAppLanguageCode);

                //Extract the 5 letter code in the question and load the test config
                CaddisflyApp.getApp().loadTestConfiguration(
                        mQuestionTitle.substring(Math.max(0, mQuestionTitle.length() - 5))
                );

                if (CaddisflyApp.getApp().getCurrentTestInfo() == null) {
                    alertTestTypeNotSupported(mQuestionTitle);
                } else {
                    if (CaddisflyApp.hasFeatureCameraFlash(this, R.string.cannotStartTest,
                            R.string.backToSurvey,
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

    private void startSurvey() {
        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME);
        if (LaunchIntent == null) {
            alertDependantAppNotFound();
        } else {
            startActivity(LaunchIntent);
//            mShouldFinish = true;
//
//            (new Handler()).postDelayed(new Runnable() {
//                public void run() {
//                    if (mShouldFinish) {
//                        finish();
//                    }
//                }
//            }, 6000);
        }
    }

    private void alertDependantAppNotFound() {
        String message = String.format("%s\r\n\r\n%s", getString(R.string.errorAkvoFlowRequired),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notFound, message);
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

    private void alertCalibrationExpired() {
        String message = getString(R.string.errorCalibrationExpired,
                CaddisflyApp.getApp().getCurrentTestInfo().getName(
                        getResources().getConfiguration().locale.getLanguage()));
        message = String.format("%s\r\n\r\n%s", message,
                getString(R.string.orderFreshBatch));

        AlertUtil.showAlert(this, R.string.cannotStartTest,
                message, R.string.backToSurvey,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }, null
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
                    this.setResult(Activity.RESULT_OK, intent);
                }
                finish();
                break;
            default:
        }
    }

//    @Override
//    protected void onUserLeaveHint() {
//        super.onUserLeaveHint();
//        if (mShouldFinish) {
//            finish();
//        }
//    }

    /**
     * Alert message for calibration incomplete or invalid
     */
    private void alertCalibrationIncomplete() {
        String message = getString(R.string.errorCalibrationIncomplete,
                CaddisflyApp.getApp().getCurrentTestInfo().getName(
                        getResources().getConfiguration().locale.getLanguage()));
        message = String.format("%s\r\n\r\n%s", message,
                getString(R.string.doYouWantToCalibrate));

        AlertUtil.showAlert(this, R.string.cannotStartTest,
                message, R.string.calibrate,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialogInterface,
                            int i) {
                        final Intent intent = new Intent(getBaseContext(), CalibrateListActivity.class);
                        startActivity(intent);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialogInterface,
                            int i) {
                        finish();
                    }
                }
        );
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
                R.string.backToSurvey,
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
