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

import android.Manifest;
import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.diagnostic.ConfigTask;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.util.NetUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.util.Calendar;
import java.util.Date;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExperimentPreferenceFragment extends PreferenceFragment {

    private static final int PERMISSION_SYNC = 2;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
    private ListView list;
    private View coordinatorLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_experiment);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        coordinatorLayout = rootView;

        setupSyncConfigPreference();

        return rootView;
    }

    private void setupSyncConfigPreference() {
        final Preference syncTestsPreference = findPreference("syncTestList");
        if (syncTestsPreference != null) {
            syncTestsPreference.setOnPreferenceClickListener(preference -> {

                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ApiUtil.hasPermissions(getActivity(), permissions)) {
                    requestPermissions(permissions, PERMISSION_SYNC);
                } else {
                    startTestSync();
                }

                return true;
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0);
    }

    private void startTestSync() {
        if (NetUtil.isNetworkAvailable(getActivity())) {
            Date todayDate = Calendar.getInstance().getTime();
            ConfigTask configTask = new ConfigTask(getActivity());
            configTask.execute("https://raw.githubusercontent.com/foundation-for-enviromental-monitoring/experimental-tests/version-beta-10/experimental_tests.json?" + todayDate.getTime());

            final TestListViewModel viewModel =
                    ViewModelProviders.of((FragmentActivity) getActivity()).get(TestListViewModel.class);

            viewModel.clearTests();

//            configTask.execute("https://raw.githubusercontent.com/foundation-for-enviromental-monitoring/experimental-tests/master/experimental_tests.json?" + todayDate.getTime());
        } else {
            Toast.makeText(getActivity(), "No data connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_SYNC) {
            // If request is cancelled, the result arrays are empty.
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            if (granted) {
                startTestSync();
            } else {
                String message = getString(R.string.storagePermission);
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(getActivity()));

                TypedValue typedValue = new TypedValue();
                getActivity().getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                snackbar.setActionTextColor(typedValue.data);
                View snackView = snackbar.getView();
                TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
                textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }
}
