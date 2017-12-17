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

package org.akvo.caddisfly.util;


import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import org.akvo.caddisfly.diagnostic.ConfigTask;
import org.akvo.caddisfly.ui.TestListActivity;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.util.Calendar;
import java.util.Date;

public class ConfigDownloader {

    /**
     * Download latest version of the experimental config file.
     *
     * @param activity the activity
     * @param configSyncHandler the callback
     */
    public static void syncExperimentalConfig(Activity activity,
                                              TestListActivity.SyncCallbackInterface configSyncHandler) {
        if (NetUtil.isNetworkAvailable(activity)) {
            Date todayDate = Calendar.getInstance().getTime();
            ConfigTask configTask = new ConfigTask(activity, configSyncHandler);
            configTask.execute("https://raw.githubusercontent.com/foundation-for-enviromental-monitoring/experimental-tests/version-beta-10/experimental_tests.json?" + todayDate.getTime());

            final TestListViewModel viewModel =
                    ViewModelProviders.of((FragmentActivity) activity).get(TestListViewModel.class);

            viewModel.clearTests();

            //configTask.execute("https://raw.githubusercontent.com/foundation-for-enviromental-monitoring/experimental-tests/master/experimental_tests.json?" + todayDate.getTime());
        } else {
            Toast.makeText(activity,
                    "No data connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
        }
    }
}
