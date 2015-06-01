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

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.util.ApiUtils;
import org.akvo.caddisfly.util.NetworkUtils;
import org.akvo.caddisfly.util.PreferencesUtils;

public class AboutActivity extends AppCompatActivity {

    private boolean mDeveloperMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ApiUtils.lockScreenOrientation(this);

        mDeveloperMode = PreferencesUtils.getBoolean(this, R.string.developerModeKey, false);

        TextView productView = (TextView) findViewById(R.id.textVersion);
        productView.setText(MainApp.getVersion(this));
        ImageView organizationView = (ImageView) findViewById(R.id.organizationImage);
        ImageView logoView = (ImageView) findViewById(R.id.logoImageView);
        final Button disableDeveloperButton = (Button) findViewById(R.id.disableDeveloperButton);

        if (mDeveloperMode) {
            disableDeveloperButton.setVisibility(View.VISIBLE);
        }

        disableDeveloperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableDeveloperButton.setVisibility(View.GONE);
                Toast.makeText(getBaseContext(), getString(R.string.developerModeDisabled), Toast.LENGTH_LONG).show();
                PreferencesUtils.setBoolean(getBaseContext(), R.string.developerModeKey, false);
                mDeveloperMode = false;
            }
        });

        logoView.setOnClickListener(new View.OnClickListener() {
            int clickCount = 0;

            @Override
            public void onClick(View view) {
                if (!mDeveloperMode) {
                    clickCount++;
                    if (clickCount > 9) {
                        clickCount = 0;
                        Toast.makeText(getBaseContext(), getString(R.string.developerModeEnabled), Toast.LENGTH_LONG).show();
                        disableDeveloperButton.setVisibility(View.VISIBLE);
                        PreferencesUtils.setBoolean(getBaseContext(), R.string.developerModeKey, true);
                        mDeveloperMode = true;
                    }
                }
            }
        });

        final Context context = this;
        productView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                NetworkUtils.openWebBrowser(context, Config.PRODUCT_WEBSITE);
            }
        });

        organizationView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                NetworkUtils.openWebBrowser(context, Config.ORG_WEBSITE);
            }
        });

        setTitle(R.string.about);
    }
}
