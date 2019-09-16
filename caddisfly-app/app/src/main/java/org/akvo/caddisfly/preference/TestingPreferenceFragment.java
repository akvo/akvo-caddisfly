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

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.Calendar;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingPreferenceFragment extends PreferenceFragment {

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_testing);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_row, container, false);
        setBackgroundColor(view);

        Preference testModeOnPreference = findPreference(getString(R.string.testModeOnKey));
        if (testModeOnPreference != null) {
            testModeOnPreference.setOnPreferenceClickListener(preference -> {
                setBackgroundColor(view);
                return true;
            });
        }

        Preference simulateCrashPreference = findPreference(getString(R.string.simulateCrashKey));
        if (simulateCrashPreference != null) {
            simulateCrashPreference.setOnPreferenceClickListener(preference -> {
                if (Calendar.getInstance().getTimeInMillis() - PreferencesUtil.getLong(getActivity(),
                        "lastCrashReportSentKey") > 60000) {
                    AlertUtil.askQuestion(getActivity(), R.string.simulateCrash, R.string.simulateCrash,
                            R.string.ok, R.string.cancel, true, (dialogInterface, i) -> {
                                PreferencesUtil.setLong(getActivity(), "lastCrashReportSentKey",
                                        Calendar.getInstance().getTimeInMillis());
                                try {
                                    throw new Exception("Simulated crash test");
                                } catch (Exception e) {
                                    Timber.e(e);
                                }
                            }, null);
                } else {
                    Toast.makeText(getActivity(), "Wait 1 minute before sending again",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        return view;
    }

    private void setBackgroundColor(View view) {
        if (AppPreferences.isTestMode()) {
            view.setBackgroundColor(Color.rgb(255, 165, 0));
        } else {
            view.setBackgroundColor(Color.rgb(255, 255, 255));
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
        ListViewUtil.setListViewHeightBasedOnChildren(list, 40);
    }
}
