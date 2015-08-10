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


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ListViewUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesDiagnosticFragment extends PreferenceFragment {

    private ListView list;
    private USBMonitor mUSBMonitor;

    public PreferencesDiagnosticFragment() {
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

        mUSBMonitor = new USBMonitor(getActivity(), null);

        final EditTextPreference sampleTimesPreference =
                (EditTextPreference) findPreference(getString(R.string.samplingsTimeKey));
        if (sampleTimesPreference != null) {

            sampleTimesPreference.setSummary(sampleTimesPreference.getText());

            sampleTimesPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (Integer.parseInt(String.valueOf(newValue)) > AppConfig.SAMPLING_COUNT_DEFAULT) {
                        newValue = AppConfig.SAMPLING_COUNT_DEFAULT;
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
                    CaddisflyApp caddisflyApp = (CaddisflyApp) context.getApplicationContext();
                    caddisflyApp.initializeCurrentTest();
                    if (caddisflyApp.currentTestInfo.getType() != AppConfig.TestType.COLORIMETRIC_LIQUID) {
                        caddisflyApp.setDefaultTest();
                    }

                    final Intent intent = new Intent(context, ColorimetricLiquidActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }

        Preference cameraPreviewPreference = findPreference("cameraPreview");
        if (cameraPreviewPreference != null) {
            cameraPreviewPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CaddisflyApp caddisflyApp = (CaddisflyApp) getActivity().getApplicationContext();
                    caddisflyApp.initializeCurrentTest();
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    CameraFragment cameraFragment = CameraFragment.newInstance(true);
                    cameraFragment.show(ft, "cameraFragment");
                    return true;
                }
            });
        }

        Preference externalCameraPreviewPreference = findPreference("externalCamera");
        if (externalCameraPreviewPreference != null) {
            externalCameraPreviewPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CaddisflyApp caddisflyApp = (CaddisflyApp) getActivity().getApplicationContext();
                    caddisflyApp.initializeCurrentTest();

                    final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getActivity(), R.xml.camera_device_filter);
                    List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
                    if (usbDeviceList.size() > 0) {
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ExternalCameraFragment cameraFragment = ExternalCameraFragment.newInstance(true);
                        cameraFragment.setCancelable(false);
                        cameraFragment.show(ft, "externalCameraFragment");
                    } else {
                        AlertUtils.showMessage(getActivity(), R.string.sensorNotFound, R.string.deviceConnectExternalCamera);
                    }

                    return true;
                }
            });
        }

        Preference sensorPreference = findPreference("ecSensor");
        if (sensorPreference != null) {
            sensorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CaddisflyApp caddisflyApp = (CaddisflyApp) getActivity().getApplicationContext();
                    //todo: fix hardcoding of econd
                    caddisflyApp.setSwatches("ECOND");

                    final Intent intent = new Intent(getActivity(), SensorActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }

        final EditTextPreference distancePreference =
                (EditTextPreference) findPreference(getString(R.string.colorDistanceToleranceKey));
        if (distancePreference != null) {
            distancePreference.setSummary(distancePreference.getText());

            distancePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (Integer.parseInt(String.valueOf(newValue)) > 99) {
                        newValue = 99;
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = (ListView) view.findViewById(android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtils.setListViewHeightBasedOnChildren(list, 0);
    }

}
