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
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.AppPreferences;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ApiUtils;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.DateUtils;
import org.akvo.caddisfly.util.FileUtils;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.akvo.caddisfly.util.UpdateCheckTask;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TEST = 1;
    private static final int REQUEST_LANGUAGE = 2;
    private final WeakRefHandler handler = new WeakRefHandler(this);
    private Boolean external = false;
    private boolean mShouldFinish = false;
    private String mLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiUtils.lockScreenOrientation(this);

        MainApp.hasCameraFlash = ApiUtils.checkCameraFlash(this);

        long updateLastCheck = PreferencesUtils.getLong(this, R.string.lastUpdateCheckKey);

        // last update check date
        Calendar lastCheckDate = Calendar.getInstance();
        lastCheckDate.setTimeInMillis(updateLastCheck);

        Calendar currentDate = Calendar.getInstance();
        if (DateUtils.getDaysDifference(lastCheckDate, currentDate) > 0) {
            UpdateCheckTask updateCheckTask = new UpdateCheckTask(this, true, MainApp.getVersion(this));
            updateCheckTask.execute();

        }

        Button startSurveyButton = (Button) findViewById(R.id.surveyButton);
        startSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSurvey();
            }
        });

        final Button disableDeveloperButton = (Button) findViewById(R.id.disableDiagnosticButton);

        disableDeveloperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled), Toast.LENGTH_LONG).show();
                AppPreferences.disableDiagnosticMode(getBaseContext());

                checkDiagnosticMode();
            }
        });


        //todo: upgrade stuff. To be removed...
        File oldFolder = new File(Environment.getExternalStorageDirectory().getPath() +
                Config.OLD_FILES_FOLDER_NAME + File.separator + Config.OLD_CALIBRATE_FOLDER_NAME);

        boolean folderFixed = true;

        if (oldFolder.exists()) {
            File newPath = new File(Environment.getExternalStorageDirectory().getPath() +
                    Config.APP_EXTERNAL_PATH);

            if (!newPath.exists()) {
                //noinspection ResultOfMethodCallIgnored
                newPath.mkdirs();
            }

            newPath = new File(Environment.getExternalStorageDirectory().getPath() +
                    Config.APP_EXTERNAL_PATH + File.separator + Config.CALIBRATE_FOLDER_NAME);
            if (!newPath.exists()) {
                folderFixed = oldFolder.renameTo(newPath);
            }
        }

        oldFolder = new File(Environment.getExternalStorageDirectory().getPath() +
                Config.OLD_FILES_FOLDER_NAME);
        if (oldFolder.exists() && folderFixed) {
            FileUtils.deleteFiles(Environment.getExternalStorageDirectory().getPath()
                    + Config.OLD_FILES_FOLDER_NAME);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkDiagnosticMode();

        CheckLocale(mLanguageCode);
    }

    private void checkDiagnosticMode() {
        if (AppPreferences.isDiagnosticMode(this)) {
            findViewById(R.id.diagnosticModeLayout).setVisibility(View.VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.diagnostic)));
            }

        } else {
            if (findViewById(R.id.diagnosticModeLayout).getVisibility() == View.VISIBLE) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar)));
                    findViewById(R.id.diagnosticModeLayout).setVisibility(View.GONE);
                }
            }
        }
    }

    private void CheckLocale(String languageCode) {
        assert getApplicationContext() != null;

        Locale locale;
        String[] supportedLanguages = getResources().getStringArray(R.array.language_codes);

        if (languageCode != null && !languageCode.isEmpty() &&
                Arrays.asList(supportedLanguages).contains(languageCode)) {
            locale = new Locale(languageCode);
        } else {

            String previousSystemLanguage = PreferencesUtils.getString(this, R.string.systemLanguageKey, "");
            Locale currentSystemLocale = Locale.getDefault();

            if (!previousSystemLanguage.equals(currentSystemLocale.getLanguage())
                    && Arrays.asList(supportedLanguages).contains(currentSystemLocale.getLanguage())) {
                locale = Locale.getDefault();
                PreferencesUtils.setString(this, R.string.systemLanguageKey, locale.getLanguage());
                PreferencesUtils.setString(this, R.string.languageKey, locale.getLanguage());
            } else {
                languageCode = PreferencesUtils.getString(this, R.string.languageKey, "");
                if (languageCode.isEmpty()) {
                    //no language was selected in the settings so use the device language
                    locale = Locale.getDefault();
                    Locale currentLocale = getResources().getConfiguration().locale;
                    if (currentLocale.getLanguage().equals(locale.getLanguage())) {
                        return;
                    }

                } else {
                    locale = new Locale(languageCode);
                }
            }
        }

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();

        if (!config.locale.equals(locale)) {
            config.locale = locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLayoutDirection(locale);
            }
            res.updateConfiguration(config, dm);

            if (!external) {
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        }
    }

    private void startSurvey() {
        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage(Config.FLOW_SURVEY_PACKAGE_NAME);
        if (LaunchIntent == null) {

            String message = String.format("%s\r\n\r\n%s", getString(R.string.errorAkvoFlowRequired),
                    getString(R.string.pleaseContactSupport));

            AlertUtils.showMessage(this, R.string.akvoFlowNotFound, message);
        } else {
            startActivity(LaunchIntent);
            mShouldFinish = true;

            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    if (mShouldFinish) {
                        finish();
                    }
                }
            }, 6000);
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
            startActivityForResult(intent, REQUEST_LANGUAGE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mShouldFinish = false;

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String mQuestionTitle;
        MainApp mainApp = (MainApp) getApplicationContext();

        if (Config.FLOW_ACTION_EXTERNAL_SOURCE.equals(action) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                external = true;
                mQuestionTitle = intent.getStringExtra("questionTitle");
                mLanguageCode = intent.getStringExtra("language");

                String code = mQuestionTitle.substring(Math.max(0, mQuestionTitle.length() - 5));
                mainApp.setSwatches(code);

                if (mainApp.currentTestInfo == null) {

                    String itemName;
                    if (mQuestionTitle.length() > 0) {
                        if (mQuestionTitle.length() > 30) {
                            mQuestionTitle = mQuestionTitle.substring(0, 30);
                        }
                        itemName = mQuestionTitle.substring(0, Math.max(0, mQuestionTitle.length() - 7)).trim();
                    } else {
                        itemName = getString(R.string.error);
                    }

                    String message = getString(R.string.errorTestNotAvailable, itemName);
                    String alertMessage = String.format("%s\r\n\r\n%s", message,
                            getString(R.string.pleaseContactSupport));

                    AlertUtils.showAlert(this, R.string.cannotStartTest, alertMessage,
                            R.string.backToSurvey,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialogInterface,
                                        int i) {
                                    finish();
                                }
                            }, null
                    );
                } else {
                    if (!MainApp.hasCameraFlash) {
                        AlertUtils.showError(this, R.string.cannotStartTest,
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
                    } else {
                        startTest();
                    }
                }
            }
        }

        CheckLocale(mLanguageCode);
    }

    private void startTest() {
        Context context = this;
        MainApp mainApp = (MainApp) context.getApplicationContext();
        if (mainApp.currentTestInfo.getType() == MainApp.TestType.COLORIMETRIC) {

            if (!ColorUtils.validateColorRange(mainApp.currentTestInfo.getRanges())) {
                Configuration conf = getResources().getConfiguration();

                String message = getString(R.string.errorCalibrationIncomplete,
                        mainApp.currentTestInfo.getName(conf.locale.getLanguage()));

                String alertMessage = String.format("%s\r\n\r\n%s", message,
                        getString(R.string.doYouWantToCalibrate));

                AlertUtils.showAlert(context, R.string.cannotStartTest,
                        alertMessage, R.string.calibrate,
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
                return;
            }

            final Intent intent = new Intent(context, CameraSensorActivity.class);
            startActivityForResult(intent, REQUEST_TEST);

        } else if (mainApp.currentTestInfo.getType() == MainApp.TestType.SENSOR) {
            final Intent intent = new Intent(context, SensorActivity.class);
            startActivityForResult(intent, REQUEST_TEST);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_LANGUAGE:
                if (resultCode == Activity.RESULT_OK) {
                    this.recreate();
                }
                break;
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getIntent());
                    //intent.putExtra("result", data.getDoubleExtra("result", -1));
                    //intent.putExtra("questionId", mQuestionId);
                    intent.putExtra("response", data.getStringExtra("response"));
                    this.setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferencesUtils.setString(this, R.string.systemLanguageKey, Locale.getDefault().getLanguage());
    }

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
