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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.updater.UpdateCheckReceiver;
import org.akvo.caddisfly.updater.UpdateHelper;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity {

    private final WeakRefHandler handler = new WeakRefHandler(this);
    private UpdateCheckReceiver updateCheckReceiver;
    private boolean mShouldClose = false;

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

                boolean hasOtg = getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                if (hasOtg) {
                    CaddisflyApp.getApp().loadTestConfiguration("ECOND");
                    final Intent intent = new Intent(getBaseContext(), SensorActivity.class);
                    intent.putExtra("internal", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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

        updateCheckReceiver = UpdateHelper.checkUpdate(this, true);

    }

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s\r\n\r\n%s", getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notSupported, message);
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
            }, 4000);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }

    private void alertDependantAppNotFound() {
        String message = String.format("%s\r\n\r\n%s", getString(R.string.errorAkvoFlowRequired),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notFound, message);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(updateCheckReceiver);
        } catch (Exception ignored) {
        }
        super.onDestroy();
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

