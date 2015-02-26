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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.DateUtils;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.akvo.caddisfly.util.UpdateCheckTask;

import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_TEST = 1;
    private static final int REQUEST_LANGUAGE = 2;
    private boolean mShouldFinish = false;

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkCameraFlash()) {
            AlertUtils.showError(this, R.string.error,
                    getString(R.string.cameraFlashRequired),
                    null,
                    R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    },
                    null);
        } else {

            //loadSavedPreferences();

            long updateLastCheck = PreferencesUtils.getLong(this, R.string.lastUpdateCheckKey);

            // last update check date
            Calendar lastCheckDate = Calendar.getInstance();
            lastCheckDate.setTimeInMillis(updateLastCheck);

            Calendar currentDate = Calendar.getInstance();
            if (DateUtils.getDaysDifference(lastCheckDate, currentDate) > 0) {
                checkUpdate(true);
            }
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Button startSurveyButton = (Button) findViewById(R.id.surveyButton);
        startSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSurvey();
            }
        });

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_logo);

    }

    /**
     * @param background true: check for update silently, false: show messages to user
     */
    void checkUpdate(boolean background) {
        UpdateCheckTask updateCheckTask = new UpdateCheckTask(this, background, MainApp.getVersion(this));
        updateCheckTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedPreferences();
    }

    public void startSurvey() {
        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage(Config.FLOW_SURVEY_PACKAGE_NAME);
        if (LaunchIntent == null) {
            AlertUtils.showMessage(this, R.string.error, R.string.installAkvoFlow);
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

    /**
     * Load user preferences
     */
    private void loadSavedPreferences() {
        assert getApplicationContext() != null;

        // Set the locale according to preference
        Locale myLocale = new Locale(
                PreferencesUtils.getString(this, R.string.languageKey, Config.DEFAULT_LOCALE));
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (!conf.locale.equals(myLocale)) {
            conf.locale = myLocale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                conf.setLayoutDirection(myLocale);
            }
            res.updateConfiguration(conf, dm);
            this.recreate();
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

        //noinspection SimplifiableIfStatement
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
        //Boolean external = false;

        if (Config.FLOW_ACTION_EXTERNAL_SOURCE.equals(action) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                //external = true;
                mQuestionTitle = getIntent().getStringExtra("questionTitle");
                String code = mQuestionTitle.substring(Math.max(0, mQuestionTitle.length() - 5));
                mainApp.setSwatches(code);

                if (mainApp.currentTestInfo == null) {

                    String errorTitle;
                    if (mQuestionTitle.length() > 0) {
                        if (mQuestionTitle.length() > 30) {
                            mQuestionTitle = mQuestionTitle.substring(0, 30);
                        }
                        errorTitle = mQuestionTitle;
                    } else {
                        errorTitle = getString(R.string.error);
                    }

                    AlertUtils.showAlert(this, errorTitle,
                            R.string.testNotAvailable,
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
                    startTest();
                }
            }
        }
    }

    public void startTest() {
        Context context = this;
        MainApp mainApp = (MainApp) context.getApplicationContext();
        if (mainApp.currentTestInfo.getType() == 0) {
            final Intent intent = new Intent(context, CameraSensorActivity.class);
            //intent.setClass(context, CameraSensorActivity.class);
            startActivityForResult(intent, REQUEST_TEST);
            //finish();
        } else if (mainApp.currentTestInfo.getType() == 1) {
            final Intent intent = new Intent(context, SensorActivity.class);
            //intent.setClass(context, SensorActivity.class);
            startActivityForResult(intent, REQUEST_TEST);
            //finish();
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
                    //displayView(Config.CHECKLIST_SCREEN_INDEX, true);
                }
                break;
            default:
        }
    }

    private boolean checkCameraFlash() {
        boolean hasFlash = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        Camera camera = getCameraInstance();
        try {
            Camera.Parameters p;

            if (hasFlash) {
                p = camera.getParameters();
                if (p.getSupportedFlashModes() == null) {
                    hasFlash = false;
                } else {
                    if (p.getSupportedFlashModes().size() == 1) {
                        if (p.getSupportedFlashModes().get(0).equals("off")) {
                            hasFlash = false;
                        }
                    }
                }
            }
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
        return hasFlash;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }


}
