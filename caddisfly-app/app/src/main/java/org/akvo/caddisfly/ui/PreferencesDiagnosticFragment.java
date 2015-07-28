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
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
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

        Preference cameraPreviewPreference = findPreference("cameraPreview");
        if (cameraPreviewPreference != null) {
            cameraPreviewPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
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

                    final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getActivity(), R.xml.camera_device_filter);
                    List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
                    if (usbDeviceList.size() > 0) {
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ExternalCameraFragment cameraFragment = ExternalCameraFragment.newInstance(true);
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
                    MainApp mainApp = (MainApp) getActivity().getApplicationContext();
                    //todo: fix hardcoding of econd
                    mainApp.setSwatches("ECOND");

                    final Intent intent = new Intent(getActivity(), SensorActivity.class);
                    startActivity(intent);
                    return true;
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
