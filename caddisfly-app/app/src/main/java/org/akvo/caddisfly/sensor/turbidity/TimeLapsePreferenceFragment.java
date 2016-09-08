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

package org.akvo.caddisfly.sensor.turbidity;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimeLapsePreferenceFragment extends PreferenceFragment {

    private ListView list;

    public TimeLapsePreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_turbidity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        final EditTextPreference sampleIntervalPreference =
                (EditTextPreference) findPreference(getString(R.string.colif_IntervalMinutesKey));
        if (sampleIntervalPreference != null) {

            sampleIntervalPreference.setSummary(String.format("Every %s minutes", sampleIntervalPreference.getText()));

            sampleIntervalPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        if (Integer.parseInt(String.valueOf(newValue)) > 360) {
                            newValue = 360;
                        }

                        if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                            newValue = 1;
                        }
                    } catch (Exception e) {
                        newValue = 2;
                    }

                    sampleIntervalPreference.setText(String.valueOf(newValue));
                    sampleIntervalPreference.setSummary(String.format("Every %s minutes", newValue));
                    return false;
                }
            });
        }

        final EditTextPreference samplesPreference =
                (EditTextPreference) findPreference(getString(R.string.colif_NumberOfSamplesKey));
        if (samplesPreference != null) {
            samplesPreference.setSummary(samplesPreference.getText());

            samplesPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        if (Integer.parseInt(String.valueOf(newValue)) > 50) {
                            newValue = 50;
                        }

                        if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                            newValue = 1;
                        }
                    } catch (Exception e) {
                        newValue = 1;
                    }
                    samplesPreference.setText(String.valueOf(newValue));
                    samplesPreference.setSummary(String.valueOf(newValue));
                    return false;
                }
            });
        }

        final EditTextPreference rgbPreference =
                (EditTextPreference) findPreference(getString(R.string.turbidityLedRgbKey));
        if (rgbPreference != null) {
            rgbPreference.setSummary(rgbPreference.getText());

            rgbPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    PreferencesUtil.setString(getActivity(),
                            CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                            R.string.ledRgbKey, String.valueOf(newValue));

                    rgbPreference.setText(String.valueOf(newValue));
                    rgbPreference.setSummary(String.valueOf(newValue));
                    return false;
                }
            });

        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = (ListView) view.findViewById(android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0);
    }
}