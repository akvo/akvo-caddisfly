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

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class DiagnosticInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_info);

        setTitle("Diagnostic Information");

        ListView mListView = (ListView) findViewById(R.id.listInformation);

        ArrayList<String> infoList = new ArrayList<>();

        infoList.add(String.format("Number of calibrations\ns:%d  e:%d",
                PreferencesUtil.getInt(this, R.string.totalSuccessfulCalibrationsKey, 0),
                PreferencesUtil.getInt(this, R.string.totalFailedCalibrationsKey, 0)));

        infoList.add(String.format("Number of tests\ns:%d  e:%d",
                PreferencesUtil.getInt(this, R.string.totalSuccessfulTestsKey, 0),
                PreferencesUtil.getInt(this, R.string.totalFailedTestsKey, 0)));

        infoList.add(String.format("Installed from\n%s", ApiUtil.isStoreVersion(this) ? "Play store" : "apk file"));

        long updateLastCheck = PreferencesUtil.getLong(this, R.string.lastUpdateCheckKey);
        if (updateLastCheck > -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(updateLastCheck);
            infoList.add(String.format("Last check for update\n%s", calendar.getTime()));

            calendar.setTimeInMillis(calendar.getTimeInMillis() + AppConfig.UPDATE_CHECK_INTERVAL);
            infoList.add(String.format("Next check for update\n%s", calendar.getTime()));
        }
        ArrayAdapter<String> infoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, infoList);

        mListView.setAdapter(infoAdapter);
    }
}
