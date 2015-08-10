/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.AppPreferences;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.PreferencesUtils;

import java.util.Locale;

public class AboutActivity extends BaseActivity {

    private boolean mDiagnosticMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mDiagnosticMode = AppPreferences.isDiagnosticMode(this);

        TextView productView = (TextView) findViewById(R.id.textVersion);
        productView.setText(CaddisflyApp.getVersion(this));

        productView.setOnClickListener(new View.OnClickListener() {
            int clickCount = 0;

            @Override
            public void onClick(View view) {
                if (!mDiagnosticMode) {
                    clickCount++;
                    if (clickCount > 9) {
                        clickCount = 0;
                        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeEnabled), Toast.LENGTH_LONG).show();
                        AppPreferences.enableDiagnosticMode(getBaseContext());

                        //set the language preference to the current language
                        Locale currentLocale = getResources().getConfiguration().locale;
                        PreferencesUtils.setString(getBaseContext(), R.string.languageKey, currentLocale.getLanguage());

                        mDiagnosticMode = true;

                        checkDiagnosticActionBar();
                    }
                }
            }
        });

        setTitle(R.string.about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        }
    }
}
