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

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.databinding.ActivityAboutBinding;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

/**
 * Activity to display info about the app.
 */
public class AboutActivity extends BaseActivity {

    private static final int CHANGE_MODE_MIN_CLICKS = 10;

    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAboutBinding b =
                DataBindingUtil.setContentView(this, R.layout.activity_about);

        b.textVersion.setText(CaddisflyApp.getAppVersion(AppPreferences.isDiagnosticMode()));

        setTitle(R.string.about);
    }

    /**
     * Displays legal information.
     */
    public void onSoftwareNoticesClick(View view) {
        NoticesDialogFragment dialog = NoticesDialogFragment.newInstance();
        dialog.show(getFragmentManager(), "NoticesDialog");
    }

    /**
     * Disables diagnostic mode.
     */
    public void disableDiagnosticsMode(View view) {
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
    public void switchToDiagnosticMode(View view) {
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
        switchLayoutForDiagnosticOrUserMode();
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

}
