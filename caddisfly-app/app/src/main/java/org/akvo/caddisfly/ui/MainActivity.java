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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.TestTypeListActivity;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.lang.ref.WeakReference;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends BaseActivity {

    private static final int AUTO_FINISH_DELAY_MILLIS = 4000;
    private static final int PERMISSION_ALL = 1;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;

    private final WeakRefHandler handler = new WeakRefHandler(this);
    private boolean mShouldClose = false;
    private View coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

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
            }
        });

        findViewById(R.id.buttonEcSensor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean hasOtg = getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                if (hasOtg) {
                    CaddisflyApp.getApp().loadTestConfigurationByUuid(SensorConstants.ELECTRICAL_CONDUCTIVITY_ID);
                    final Intent intent = new Intent(getBaseContext(), SensorActivity.class);
                    intent.putExtra("internal", true);
                    startActivity(intent);
                } else {
                    alertFeatureNotSupported();
                }
            }
        });

        findViewById(R.id.buttonSurvey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSurvey();
            }
        });

        final Activity activity = this;

        findViewById(R.id.buttonStripTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG), "strip-tests.json");
                if (file.exists()) {

                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                    if (!ApiUtil.hasPermissions(getBaseContext(), permissions)) {
                        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_ALL);
                    } else {
                        startStripTest();
                    }
                } else {
                    startStripTest();
                }

            }
        });

        // TODO: remove upgrade code when obsolete
        upgradeFolder("FLUOR", SensorConstants.FLUORIDE_ID);
        upgradeFolder("CHLOR", SensorConstants.FREE_CHLORINE_ID);

        // change the old name of custom config folder from config to custom-config
        final File oldFolder = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                + FileHelper.ROOT_DIRECTORY + File.separator + "config");
        final File newFolder = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                + FileHelper.ROOT_DIRECTORY + File.separator + "custom-config");

        if (oldFolder.exists() && oldFolder.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            oldFolder.renameTo(newFolder);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        final Activity activity = this;
        if (requestCode == PERMISSION_ALL) {
            // If request is cancelled, the result arrays are empty.
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            if (granted) {
                startStripTest();
            } else {
                String message = getString(R.string.storagePermission);
                if (AppPreferences.useExternalCamera()) {
                    message = getString(R.string.storagePermission);
                }
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction("SETTINGS", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ApiUtil.startInstalledAppDetailsActivity(activity);
                            }
                        });

                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                snackbar.setActionTextColor(typedValue.data);
                View snackView = snackbar.getView();
                TextView textView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
                textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }

    private void startStripTest() {
        final Intent intent = new Intent(getBaseContext(), TestTypeListActivity.class);
        intent.putExtra("internal", true);
        startActivity(intent);
    }

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s%n%n%s", getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notSupported, message);
    }

    // TODO: remove upgrade code when obsolete
    private void upgradeFolder(String code, String uuid) {

        // change calibration folder names to have uuid instead of the old 5 letter codes
        final File sourcePath = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, code);
        final File destinationPath = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, uuid);
        if (sourcePath.exists() && sourcePath.isDirectory()) {
            File[] sourceFiles = sourcePath.listFiles();
            if (sourceFiles != null) {
                for (File file : sourceFiles) {
                    File destinationFile = new File(destinationPath + File.separator + file.getName());
                    //noinspection ResultOfMethodCallIgnored
                    file.renameTo(destinationFile);
                }

                sourceFiles = sourcePath.listFiles();
                if (sourceFiles != null && sourceFiles.length == 0) {
                    //noinspection ResultOfMethodCallIgnored
                    sourcePath.delete();
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setTitle(R.string.appName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShouldClose = false;
        switchLayoutForDiagnosticOrUserMode();

        CaddisflyApp.getApp().setAppLanguage(null, false, handler);

        if (PreferencesUtil.getBoolean(this, R.string.themeChangedKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.themeChangedKey, false);
            handler.sendEmptyMessage(0);
        }
    }

    /**
     * Show the diagnostic mode layout
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode()) {
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
        } else {
            if (findViewById(R.id.layoutDiagnostics).getVisibility() == View.VISIBLE) {
                findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
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

    private void startSurvey() {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME);
        if (intent == null) {
            alertDependantAppNotFound();
        } else {
            mShouldClose = true;
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    if (mShouldClose) {
                        finish();
                    }
                }
            }, AUTO_FINISH_DELAY_MILLIS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void alertDependantAppNotFound() {
        String message = String.format("%s%n%n%s", getString(R.string.errorAkvoFlowRequired),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notFound, message);
    }

    /**
     * Handler to restart the app after language has been changed
     */
    private static class WeakRefHandler extends Handler {
        private final WeakReference<Activity> ref;

        WeakRefHandler(Activity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity f = ref.get();
            if (f != null) {
                f.recreate();
            }
        }
    }

}

