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

package org.akvo.caddisfly.preference;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

public class SettingsActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ScrollView mScrollView;
    private int mScrollPosition;

    private void removeAllFragments() {
        findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
        findViewById(R.id.layoutDiagnosticsOptions).setVisibility(View.GONE);
        findViewById(R.id.layoutUserDiagnostics).setVisibility(View.GONE);
        findViewById(R.id.layoutDebugging).setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        setupActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    private void setupActivity() {

        setTitle(R.string.settings);

        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutGeneral, new GeneralPreferenceFragment())
                .commit();

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutOther, new OtherPreferenceFragment())
                .commit();

        if (AppPreferences.isDiagnosticMode()) {

            getFragmentManager().beginTransaction()
                    .add(R.id.layoutDiagnostics, new DiagnosticPreferenceFragment())
                    .commit();

            getFragmentManager().beginTransaction()
                    .add(R.id.layoutDiagnosticsOptions, new DiagnosticOptionsPreferenceFragment())
                    .commit();

            if (!BuildConfig.isExperimentFlavor) {
                getFragmentManager().beginTransaction()
                        .add(R.id.layoutUserDiagnostics, new DiagnosticUserPreferenceFragment())
                        .commit();
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.layoutDebugging, new DebuggingPreferenceFragment())
                    .commit();

            findViewById(R.id.layoutDiagnosticsOptions).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutUserDiagnostics).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutDebugging).setVisibility(View.VISIBLE);
        }

        mScrollView = findViewById(R.id.scrollViewSettings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        try {
            setSupportActionBar(toolbar);
        } catch (Exception ignored) {
            //Ignore crash in Samsung
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode()) {
            getMenuInflater().inflate(R.menu.menu_settings, menu);
        }
        return true;
    }

    public void onDisableDiagnostics(MenuItem item) {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode();

        changeActionBarStyleBasedOnCurrentMode();

        invalidateOptionsMenu();

        clearTests();

        removeAllFragments();
    }

    private void clearTests() {
        final TestListViewModel viewModel =
                ViewModelProviders.of(this).get(TestListViewModel.class);

        viewModel.clearTests();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (getApplicationContext().getString(R.string.languageKey).equals(s)) {
            CaddisflyApp.getApp().setAppLanguage(null, false, null);
            Intent resultIntent = new Intent(getIntent());
            resultIntent.getBooleanExtra("refresh", true);
            setResult(RESULT_OK, resultIntent);
            PreferencesUtil.setBoolean(this, R.string.refreshKey, true);
            recreate();
        }
    }

    @Override
    public void onPause() {
        int scrollbarPosition = mScrollView.getScrollY();

        PreferencesUtil.setInt(this, "settingsScrollPosition", scrollbarPosition);

        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        mScrollPosition = PreferencesUtil.getInt(this, "settingsScrollPosition", 0);

        mScrollView.post(() -> mScrollView.scrollTo(0, mScrollPosition));
    }

}
