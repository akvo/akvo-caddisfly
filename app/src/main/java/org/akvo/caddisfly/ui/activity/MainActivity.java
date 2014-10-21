/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.ui.fragment.AboutFragment;
import org.akvo.caddisfly.ui.fragment.CalibrateFragment;
import org.akvo.caddisfly.ui.fragment.CalibrateItemFragment;
import org.akvo.caddisfly.ui.fragment.SettingsFragment;
import org.akvo.caddisfly.ui.fragment.StartFragment;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.DataHelper;
import org.akvo.caddisfly.util.DateUtils;
import org.akvo.caddisfly.util.FileUtils;
import org.akvo.caddisfly.util.NetworkUtils;
import org.akvo.caddisfly.util.PreferencesHelper;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.akvo.caddisfly.util.UpdateCheckTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends MainActivityBase implements
        SettingsFragment.OnCheckUpdateListener,
        SettingsFragment.OnCalibrateListener,
        SettingsFragment.OnAboutListener,
        StartFragment.OnBackListener,
        StartFragment.OnStartTestListener,
        StartFragment.OnVideoListener,
        StartFragment.OnStartSurveyListener,
        CalibrateFragment.OnLoadCalibrationListener,
        CalibrateItemFragment.OnLoadCalibrationListener {

    private static final int REQUEST_TEST = 1;
    private int mTestType = Config.FLUORIDE_2_INDEX;
    private boolean mShouldFinish = false;
    private SettingsFragment mSettingsFragment = null;
    //private HelpFragment helpFragment = null;

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

            loadSavedPreferences();

            if (savedInstanceState == null) {
                displayView(Config.HOME_SCREEN_INDEX, false);
            }

            long updateLastCheck = PreferencesUtils.getLong(this, R.string.lastUpdateCheck);

            // last update check date
            Calendar lastCheckDate = Calendar.getInstance();
            lastCheckDate.setTimeInMillis(updateLastCheck);

            Calendar currentDate = Calendar.getInstance();

            if (!PreferencesUtils.getBoolean(this, R.string.revertVersionKey, false)) {
                if (DateUtils.getDaysDifference(lastCheckDate, currentDate) > 0) {
                    checkUpdate(true);
                }
            }
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
     * Load user preferences
     */
    private void loadSavedPreferences() {
        assert getApplicationContext() != null;

        MainApp mainApp = ((MainApp) getApplicationContext());

        if (PreferencesUtils.getBoolean(this, R.string.oneStepCalibrationKey, false)) {
            mainApp.setFluorideSwatches();
        } else {
            mainApp.setFluoride2Swatches();
        }
        // Set the locale according to preference
        Locale myLocale = new Locale(
                PreferencesUtils.getString(this, R.string.languageKey, Config.DEFAULT_LOCALE));
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLayoutDirection(myLocale);
        }
        res.updateConfiguration(conf, dm);
    }


    /**
     * @param background true: check for update silently, false: show messages to user
     */
    void checkUpdate(boolean background) {
        UpdateCheckTask updateCheckTask = new UpdateCheckTask(this, background, false);
        updateCheckTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        mShouldFinish = false;
    }

    void displayView(int position, boolean addToBackStack) {
        int index = getCurrentFragmentIndex();

        if (index == position) {
            // requested fragment is already showing
            return;
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Boolean external = false;

        if (Config.FLOW_ACTION_EXTERNAL_SOURCE.equals(action) && type != null) {
            if ("text/plain".equals(type)) { //NON-NLS
                external = true;
                //mQuestionId = getIntent().getStringExtra("questionId");
                String questionTitle = getIntent().getStringExtra("questionTitle");

                String code = questionTitle.substring(Math.max(0, questionTitle.length() - 5));
                mTestType = DataHelper.getTestTypeFromCode(code);
            }
        }

        Fragment fragment;

        switch (position) {
            case Config.HOME_SCREEN_INDEX:
                if (external && !PreferencesUtils.getBoolean(this, R.string.showStartPageKey, true)) {
                    onStartTest();
                    return;
                }

                fragment = StartFragment.newInstance(external, mTestType);
                break;
            case Config.CALIBRATE_SCREEN_INDEX:
                //if (mCalibrateFragment == null) {
                if (PreferencesUtils.getBoolean(this, R.string.oneStepCalibrationKey, false)) {
                    CalibrateItemFragment mCalibrateFragment = new CalibrateItemFragment();

                    Bundle args = new Bundle();
                    args.putInt(getString(R.string.swatchIndex), 0);
                    MainApp mainApp = (MainApp) this.getApplicationContext();
                    args.putInt(getString(R.string.currentTestTypeId), mainApp.currentTestType);
                    mCalibrateFragment.setArguments(args);
                    fragment = mCalibrateFragment;

                } else {
                    fragment = new CalibrateFragment();

                }
                break;
            case Config.SETTINGS_SCREEN_INDEX:
                if (mSettingsFragment == null) {
                    mSettingsFragment = new SettingsFragment();
                }
                fragment = mSettingsFragment;
                break;
            default:
                return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, fragment, String.valueOf(position));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();

        invalidateOptionsMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getCurrentFragmentIndex() == Config.HOME_SCREEN_INDEX) {
            getMenuInflater().inflate(R.menu.home, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                displayView(Config.SETTINGS_SCREEN_INDEX, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * @return index of fragment currently showing
     */
    int getCurrentFragmentIndex() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            String positionString = fragment.getTag();
            if (positionString != null) {
                try {
                    return Integer.parseInt(positionString);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public void onBack() {
        finish();
    }

    public void onStartSurvey() {
        Context context = this;

        MainApp mainApp = (MainApp) this.getApplicationContext();


        if (mainApp.getCalibrationErrorCount() > 0) {
            AlertUtils.showAlert(context, R.string.error,
                    R.string.calibrate_error,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialogInterface,
                                int i) {
                            dialogInterface.dismiss();
                            displayView(Config.CALIBRATE_SCREEN_INDEX, true);
                        }
                    }, null
            );
            return;
        }

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

    @Override
    public void onStartTest() {

        Context context = this;

        MainApp mainApp = (MainApp) context.getApplicationContext();

        if (mainApp.getCalibrationErrorCount() > 0) {
            AlertUtils.showAlert(context, R.string.error,
                    R.string.calibrate_error,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialogInterface,
                                int i) {
                            displayView(Config.CALIBRATE_SCREEN_INDEX, true);

                        }
                    }, null
            );
            return;
        }

        //final Intent intent = new Intent(getIntent());
        final Intent intent = new Intent(context, ProgressActivity.class);
        intent.setClass(context, ProgressActivity.class);
        intent.putExtra(PreferencesHelper.CURRENT_LOCATION_ID_KEY, (long) 0);
        intent.putExtra("isCalibration", false);
        //intent.getBooleanExtra("skipInfo", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_TEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getIntent());
                    intent.putExtra("result", data.getDoubleExtra("result", -1));
                    //intent.putExtra("questionId", mQuestionId);
                    intent.putExtra("response", String.valueOf(data.getDoubleExtra("result", -1)));
                    this.setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    if (!PreferencesUtils.getBoolean(this, R.string.showStartPageKey, true)) {
                        onBack();
                        return;
                    }
                    //displayView(Config.CHECKLIST_SCREEN_INDEX, true);
                }
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            //showCheckUpdateOption = getCurrentFragmentIndex() == Config.SETTINGS_SCREEN_INDEX;
            invalidateOptionsMenu();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onAbout() {
        AboutFragment aboutFragment = AboutFragment.newInstance();
        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment prev = getFragmentManager().findFragmentByTag("aboutDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        aboutFragment.show(ft, "aboutDialog");

    }

    public void onCheckUpdate() {
        checkUpdate(false);
    }

    public void onVideo() {
        playVideo();
    }

    private void playVideo() {
        File sdDir = getExternalFilesDir(null);
        final File videoFile = new File(sdDir, "training.mp4");

        if (!videoFile.exists()) {
            if (NetworkUtils.checkInternetConnection(this)) {
                final Intent intent = new Intent(this, VideoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        } else {
            final Intent intent = new Intent(this, VideoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onCalibrate() {
        displayView(Config.CALIBRATE_SCREEN_INDEX, true);
    }

    @Override
    public void onLoadCalibration(final Handler.Callback callback) {
        final Context context = this;
        final MainApp mainApp = (MainApp) this.getApplicationContext();

        try {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.select_dialog_singlechoice);

            File external = Environment.getExternalStorageDirectory();
            final String folderName = Config.CALIBRATE_FOLDER_NAME;
            String path = external.getPath() + folderName;
            File folder = new File(path);
            if (folder.exists()) {
                final File[] listFiles = folder.listFiles();
                for (File listFile : listFiles) {
                    arrayAdapter.add(listFile.getName());
                }

                builderSingle.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

                builderSingle.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = listFiles[which].getName();
                                final ArrayList<Integer> swatchList = new ArrayList<Integer>();

                                final ArrayList<String> rgbList = FileUtils.loadFromFile(fileName);
                                if (rgbList != null) {

                                    for (String rgb : rgbList) {
                                        swatchList.add(ColorUtils.getColorFromRgb(rgb));
                                    }
                                    (new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            mainApp.saveCalibratedSwatches(mainApp.currentTestType, swatchList);

                                            mainApp.setSwatches(mainApp.currentTestType);

                                            SharedPreferences sharedPreferences = PreferenceManager
                                                    .getDefaultSharedPreferences(context);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            for (int i = 0; i < mainApp.rangeIntervals.size(); i++) {
                                                int index = i * mainApp.rangeIncrementStep;

                                                ColorUtils.autoGenerateColors(
                                                        index,
                                                        mainApp.currentTestType,
                                                        mainApp.colorList,
                                                        mainApp.rangeIncrementStep, editor);
                                            }
                                            editor.apply();
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void result) {
                                            super.onPostExecute(result);
                                            callback.handleMessage(null);
                                        }
                                    }).execute();

                                }
                            }
                        }
                );

                final AlertDialog alert = builderSingle.create();
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final ListView listView = alert.getListView();
                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                final int position = i;

                                AlertUtils.askQuestion(context, R.string.delete, R.string.selectedWillBeDeleted, new DialogInterface.OnClickListener() {
                                    @SuppressWarnings("unchecked")
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String fileName = listFiles[position].getName();
                                        FileUtils.deleteFile(folderName, fileName);
                                        ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                        listAdapter.remove(listAdapter.getItem(position));
                                    }
                                }, null);
                                return true;
                            }
                        });

                    }
                });
                alert.show();
            } else {
                AlertUtils.showMessage(context, R.string.notFound, R.string.noSavedCalibrations);
            }
        } catch (ActivityNotFoundException e) {
            AlertUtils.showMessage(context, R.string.error,
                    R.string.updateRequired);
        }

        callback.handleMessage(null);
    }

}
