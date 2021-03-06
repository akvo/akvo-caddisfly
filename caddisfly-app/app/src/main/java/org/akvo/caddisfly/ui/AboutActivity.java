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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.databinding.ActivityAboutBinding;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import static org.akvo.caddisfly.common.AppConstants.TERMS_OF_USE_URL;

/**
 * Activity to display info about the app.
 */
public class AboutActivity extends BaseActivity {

    private static final int CHANGE_MODE_MIN_CLICKS = 10;

    private int clickCount = 0;
    private ActivityAboutBinding b;
    private NoticesDialogFragment dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = DataBindingUtil.setContentView(this, R.layout.activity_about);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(R.string.about);
    }

    /**
     * Displays legal information.
     */
    public void onSoftwareNoticesClick(@SuppressWarnings("unused") View view) {
        dialog = NoticesDialogFragment.newInstance();
        dialog.show(getFragmentManager(), "NoticesDialog");
    }

    /**
     * Disables diagnostic mode.
     */
    public void disableDiagnosticsMode(@SuppressWarnings("unused") View view) {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode();

        switchLayoutForDiagnosticOrUserMode();

        changeActionBarStyleBasedOnCurrentMode();

        clearTests();
    }

    private void clearTests() {
        final TestListViewModel viewModel =
                ViewModelProviders.of(this).get(TestListViewModel.class);

        viewModel.clearTests();
    }

    /**
     * Turn on diagnostic mode if user clicks on version section CHANGE_MODE_MIN_CLICKS times.
     */
    public void switchToDiagnosticMode(@SuppressWarnings("unused") View view) {
        if (!AppPreferences.isDiagnosticMode()) {
            clickCount++;

            if (clickCount >= CHANGE_MODE_MIN_CLICKS) {
                clickCount = 0;
                Toast.makeText(getBaseContext(), getString(
                        R.string.diagnosticModeEnabled), Toast.LENGTH_SHORT).show();
                AppPreferences.enableDiagnosticMode();

                changeActionBarStyleBasedOnCurrentMode();

                switchLayoutForDiagnosticOrUserMode();

                // clear and reload all the tests as diagnostic mode includes experimental tests
                clearTests();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferencesUtil.getBoolean(this, R.string.refreshAboutKey, false)) {
            PreferencesUtil.removeKey(this, R.string.refreshAboutKey);
            this.recreate();
            return;
        }

        switchLayoutForDiagnosticOrUserMode();
        b.textVersion.setText(CaddisflyApp.getAppVersion(AppPreferences.isDiagnosticMode()));
    }

    /**
     * Show the diagnostic mode layout.
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        invalidateOptionsMenu();
        if (AppPreferences.isDiagnosticMode()) {
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
        } else {
            if (findViewById(R.id.layoutDiagnostics).getVisibility() == View.VISIBLE) {
                findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
            }
        }
        b.textVersion.setText(CaddisflyApp.getAppVersion(AppPreferences.isDiagnosticMode()));
    }

    public void onSettingsClick(@SuppressWarnings("unused") MenuItem item) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode()) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    public void onHomeClick(View view) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void onTermsOfServicesClick(View view) {
        openUrl(this, TERMS_OF_USE_URL);
    }

    @SuppressWarnings("SameParameterValue")
    private void openUrl(@NonNull Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
