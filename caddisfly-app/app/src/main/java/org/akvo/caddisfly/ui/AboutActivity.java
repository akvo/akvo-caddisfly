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
import org.akvo.caddisfly.util.ApiUtil;

public class AboutActivity extends BaseActivity {

    private static final int CHANGE_MODE_MIN_CLICKS = 10;
    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

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

        TextView buttonSoftwareNotices = (TextView) findViewById(R.id.buttonSoftwareNotices);
        buttonSoftwareNotices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoticesDialogFragment dialog = NoticesDialogFragment.newInstance();
                dialog.show(getFragmentManager(), "NoticesDialog");
            }
        });

        TextView textVersion = (TextView) findViewById(R.id.textVersion);
        textVersion.setText(CaddisflyApp.getAppVersion());

        textVersion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!AppPreferences.isDiagnosticMode()) {
                    clickCount++;

                    //Turn on diagnostic mode if the user clicks on the version text 10 times
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
        });

        //A indication whether the app was installed via Store or manually
        View imageStoreIcon = findViewById(R.id.viewInstallType);
        if (ApiUtil.isStoreVersion(this)) {
            imageStoreIcon.setVisibility(View.GONE);
        } else {
            imageStoreIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(R.string.about);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchLayoutForDiagnosticOrUserMode();
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
}
