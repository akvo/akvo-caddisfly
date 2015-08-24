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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.AppPreferences;
import org.akvo.caddisfly.helper.DataHelper;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.UpdateCheckTask;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ColorimetryStripActivity;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.sensor.turbidity.TurbidityActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.DateUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.NetworkUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private final WeakRefHandler handler = new WeakRefHandler(this);
    //tracks whether this app was launched by an external app
    private Boolean mIsExternalAppCall = false;
    //tracks if the app should automatically close (after launching an external app)
    private boolean mShouldFinish = false;
    //the language requested by the external app
    private String mExternalAppLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonStartSurvey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSurvey();
            }
        });

        findViewById(R.id.buttonDisableDiagnostics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled), Toast.LENGTH_LONG).show();

                AppPreferences.disableDiagnosticMode(getBaseContext());

                switchLayoutForDiagnosticOrUserMode();

                changeActionBarStyleBasedOnCurrentMode();
            }
        });

        upgradeOldFolderPath();

        checkForUpdate();
    }

    /**
     * Opens Akvo FLOW app
     */
    private void startSurvey() {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME);

        if (intent == null) {
            //external app is not installed
            alertDependantAppNotFound();
        } else {
            startActivity(intent);
            //external app was launched so get ready to close this one
            mShouldFinish = true;

            //After external app is launched wait a short while and then close this app
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    if (mShouldFinish) {
                        //close this app as external app is currently active
                        finish();
                        ExitActivity.exitApplication(getApplicationContext());
                    }
                }
            }, 2000);
        }
    }

    /**
     * Upgrade folder names and paths created by previous version to new folder structure
     */
    //todo: upgrade stuff. To be removed eventually...
    private void upgradeOldFolderPath() {

        //todo: remove when upgrade process no more required
        final String OLD_CALIBRATE_FOLDER_NAME = "calibrate";
        final String OLD_FILES_FOLDER_NAME = "/com.ternup.caddisfly";
        final String OLD_APP_EXTERNAL_PATH = "/org.akvo.caddisfly";

        File oldFolder = new File(Environment.getExternalStorageDirectory().getPath() +
                OLD_FILES_FOLDER_NAME + File.separator + OLD_CALIBRATE_FOLDER_NAME);

        boolean folderFixed = true;
        File newPath = new File(Environment.getExternalStorageDirectory().getPath() +
                OLD_APP_EXTERNAL_PATH);

        if (oldFolder.exists()) {

            if (!newPath.exists()) {
                //noinspection ResultOfMethodCallIgnored
                newPath.mkdirs();
            }

            newPath = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION);
            if (!newPath.exists()) {
                folderFixed = oldFolder.renameTo(newPath);
            }
        }

        oldFolder = new File(Environment.getExternalStorageDirectory().getPath() +
                OLD_FILES_FOLDER_NAME);
        if (oldFolder.exists() && folderFixed) {
            FileUtil.deleteFiles(Environment.getExternalStorageDirectory().getPath()
                    + OLD_FILES_FOLDER_NAME);
        }

        if (newPath.exists()) {
            //noinspection ResultOfMethodCallIgnored
            newPath.renameTo(new File(Environment.getExternalStorageDirectory().getPath(), "Akvo Caddisfly"));
        }

    }

    /**
     * Check for update, but not more than once an hour
     */
    private void checkForUpdate() {

        final Context mContext = this;

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                if (NetworkUtil.checkInternetConnection(mContext, false)) {
                    long updateLastCheck = PreferencesUtil.getLong(mContext, R.string.lastUpdateCheckKey);

                    // last update check date
                    Calendar lastCheckDate = Calendar.getInstance();
                    lastCheckDate.setTimeInMillis(updateLastCheck);

                    Calendar currentDate = Calendar.getInstance();
                    if (DateUtil.getHoursDifference(lastCheckDate, currentDate) > 0) {
                        UpdateCheckTask updateCheckTask = new UpdateCheckTask(mContext, true);
                        updateCheckTask.execute();
                    }
                }
            }
        }, 2000);

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
        if (AppPreferences.isDiagnosticMode(this)) {
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
        } else {
            if (findViewById(R.id.layoutDiagnostics).getVisibility() == View.VISIBLE) {
                findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
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

        if (id == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Do not close this app as user manually opened it
        mShouldFinish = false;

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

                if (CaddisflyApp.getApp().currentTestInfo == null) {
                    alertTestTypeNotSupported(mQuestionTitle);
                } else {
                    if (!CaddisflyApp.hasFeatureCameraFlash(this)) {
                        alertCameraFlashNotAvailable();
                    } else {
                        startTest();
                    }
                }
            }
        }
    }

    /**
     * Start the appropriate test based on the current test type
     */
    private void startTest() {
        Context context = this;
        CaddisflyApp caddisflyApp = CaddisflyApp.getApp();
        switch (caddisflyApp.currentTestInfo.getType()) {
            case COLORIMETRIC_LIQUID:

                if (!DataHelper.isSwatchListValid(caddisflyApp.currentTestInfo.getSwatches())) {
                    alertCalibrationIncomplete();
                    return;
                }

                final Intent colorimetricLiquidIntent = new Intent(context, ColorimetryLiquidActivity.class);
                startActivityForResult(colorimetricLiquidIntent, REQUEST_TEST);

                break;
            case COLORIMETRIC_STRIP:

                final Intent colorimetricStripIntent = new Intent(context, ColorimetryStripActivity.class);
                startActivityForResult(colorimetricStripIntent, REQUEST_TEST);

                break;
            case TURBIDITY_COLIFORMS:

                final Intent turbidityIntent = new Intent(context, TurbidityActivity.class);
                startActivityForResult(turbidityIntent, REQUEST_TEST);

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
     * Alert message for external app not found
     */
    private void alertDependantAppNotFound() {
        String message = String.format("%s\r\n\r\n%s", getString(R.string.errorAkvoFlowRequired),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.akvoFlowNotFound, message);
    }

    /**
     * Alert message for camera flash not found
     */
    private void alertCameraFlashNotAvailable() {
        AlertUtil.showError(this, R.string.cannotStartTest,
                getString(R.string.errorCameraFlashRequired),
                null,
                R.string.backToSurvey,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                },
                null);
    }

    /**
     * Alert message for calibration incomplete or invalid
     */
    private void alertCalibrationIncomplete() {
        String message = getString(R.string.errorCalibrationIncomplete,
                CaddisflyApp.getApp().currentTestInfo.getName(
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
