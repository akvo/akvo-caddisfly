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

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.preference.AppPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity to display info about the app.
 */
public class AboutActivity extends BaseActivity {

    private static final int CHANGE_MODE_MIN_CLICKS = 10;

    /**
     * To display version number.
     */
    @BindView(R.id.textVersion)
    TextView textVersion;

    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        textVersion.setText(CaddisflyApp.getAppVersion());

        setTitle(R.string.about);
    }

    /**
     * Displays legal information.
     */
    @OnClick(R.id.textLinkSoftwareNotices)
    public void showSoftwareNotices() {
        NoticesDialogFragment dialog = NoticesDialogFragment.newInstance();
        dialog.show(getFragmentManager(), "NoticesDialog");
    }

    /**
     * Disables diagnostic mode.
     */
    @OnClick(R.id.fabDisableDiagnostics)
    public void disableDiagnosticsMode() {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode();

        switchLayoutForDiagnosticOrUserMode();

        changeActionBarStyleBasedOnCurrentMode();
    }

    /**
     * Turn on diagnostic mode if user clicks on version text CHANGE_MODE_MIN_CLICKS times.
     */
    @OnClick(R.id.textVersion)
    public void switchToDiagnosticMode() {
        if (!AppPreferences.isDiagnosticMode()) {
            clickCount++;

            if (clickCount >= CHANGE_MODE_MIN_CLICKS) {
                clickCount = 0;
                Toast.makeText(getBaseContext(), getString(
                        R.string.diagnosticModeEnabled), Toast.LENGTH_SHORT).show();
                AppPreferences.enableDiagnosticMode();

                changeActionBarStyleBasedOnCurrentMode();

                switchLayoutForDiagnosticOrUserMode();
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
