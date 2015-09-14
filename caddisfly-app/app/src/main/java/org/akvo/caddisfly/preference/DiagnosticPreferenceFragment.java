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

package org.akvo.caddisfly.preference;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.sensor.colorimetry.liquid.DiagnosticPreviewFragment;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.util.ListViewUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiagnosticPreferenceFragment extends PreferenceFragment {

    private ListView list;

    public DiagnosticPreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_diagnostic);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        final EditTextPreference sampleTimesPreference =
                (EditTextPreference) findPreference(getString(R.string.samplingsTimeKey));
        if (sampleTimesPreference != null) {

            sampleTimesPreference.setSummary(sampleTimesPreference.getText());

            sampleTimesPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (Integer.parseInt(String.valueOf(newValue)) > ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT) {
                        newValue = ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT;
                    }

                    if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                        newValue = 1;
                    }

                    sampleTimesPreference.setText(String.valueOf(newValue));
                    sampleTimesPreference.setSummary(String.valueOf(newValue));
                    return false;
                }
            });
        }

        final Preference startTestPreference = findPreference("startTest");
        if (startTestPreference != null) {
            startTestPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Context context = getActivity();
                    CaddisflyApp caddisflyApp = CaddisflyApp.getApp();
                    caddisflyApp.initializeCurrentTest();
                    if (caddisflyApp.getCurrentTestInfo() == null ||
                            caddisflyApp.getCurrentTestInfo().getType() != CaddisflyApp.TestType.COLORIMETRIC_LIQUID) {
                        caddisflyApp.setDefaultTest();
                    }

                    final Intent intent = new Intent(context, ColorimetryLiquidActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }

        Preference cameraPreviewPreference = findPreference("cameraPreview");
        if (cameraPreviewPreference != null) {
            cameraPreviewPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    if (isCameraAvailable()) {
                        CaddisflyApp.getApp().initializeCurrentTest();
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        DiagnosticPreviewFragment diagnosticPreviewFragment = DiagnosticPreviewFragment.newInstance();
                        diagnosticPreviewFragment.show(ft, "diagnosticPreviewFragment");
                    }
                    return true;
                }
            });
        }

        Preference sensorPreference = findPreference("ecSensor");
        if (sensorPreference != null) {
            sensorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CaddisflyApp.getApp().loadTestConfiguration("ECOND");
                    final Intent intent = new Intent(getActivity(), SensorActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }

        Preference useCamera2 = findPreference(getString(R.string.useCamera2Key));
        if (useCamera2 != null) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                PreferenceCategory preferenceCategory =
                        (PreferenceCategory) findPreference("preferenceCategoryDiagnostics");
                preferenceCategory.removePreference(useCamera2);
            }
        }

        final EditTextPreference distancePreference =
                (EditTextPreference) findPreference(getString(R.string.colorDistanceToleranceKey));
        if (distancePreference != null) {
            distancePreference.setSummary(distancePreference.getText());

            distancePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (Integer.parseInt(String.valueOf(newValue)) > 399) {
                        newValue = 399;
                    }

                    if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                        newValue = 1;
                    }

                    distancePreference.setText(String.valueOf(newValue));
                    distancePreference.setSummary(String.valueOf(newValue));
                    return false;
                }
            });
        }

        return rootView;
    }

    @SuppressWarnings("deprecation")
    private boolean isCameraAvailable() {
        Camera camera = null;
        try {
            camera = CaddisflyApp.getCamera(getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            if (camera != null) {
                return true;
            }

        } finally {
            if (camera != null) {
                camera.release();
            }
        }
        return false;
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