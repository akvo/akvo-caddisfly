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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.databinding.ActivityMainBinding;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private final WeakRefHandler refreshHandler = new WeakRefHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CaddisflyApp.getApp().setAppLanguage(null, false, null);

        ActivityMainBinding b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setTitle(R.string.appName);

        try {
            final GregorianCalendar appExpiryDate = new GregorianCalendar(AppConfig.APP_EXPIRY_YEAR,
                    AppConfig.APP_EXPIRY_MONTH - 1, AppConfig.APP_EXPIRY_DAY);

            DateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            b.textVersionExpiry.setText(String.format("Version expiry: %s", df.format(appExpiryDate.getTime())));

//            if (AppConfig.APP_EXPIRY && ApkHelper.isNonStoreVersion(this)) {
//                b.textVersionExpiry.setVisibility(View.VISIBLE);
//            }

            // If app has expired then close this activity
            ApkHelper.isAppVersionExpired(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the diagnostic mode layout.
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

        CaddisflyApp.getApp().setAppLanguage(null, false, refreshHandler);

        if (PreferencesUtil.getBoolean(this, R.string.themeChangedKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.themeChangedKey, false);
            refreshHandler.sendEmptyMessage(0);
        }
    }

    public void onDisableDiagnosticsClick(@SuppressWarnings("unused") View view) {

        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode();

        switchLayoutForDiagnosticOrUserMode();

        changeActionBarStyleBasedOnCurrentMode();

        final TestListViewModel viewModel =
                ViewModelProviders.of(this).get(TestListViewModel.class);

        viewModel.clearTests();
    }

    public void onSettingsClick(@SuppressWarnings("unused") MenuItem item) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && PreferencesUtil.getBoolean(this, R.string.refreshKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.refreshKey, false);
            this.recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode()) {
            getMenuInflater().inflate(R.menu.menu_main_diagnostic, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    public void onAboutClick(MenuItem item) {
        final Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onLaunchAppClick(View view) {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        (new Handler()).postDelayed(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        }, 1000);
    }

    public void onUserGuideClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(AppConfig.PRODUCT_SUPPORT_URL));
        startActivity(browserIntent);
    }

    /**
     * Handler to restart the app after language has been changed.
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

