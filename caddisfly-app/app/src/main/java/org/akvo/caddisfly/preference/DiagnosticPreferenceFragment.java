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
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.sensor.colorimetry.liquid.DiagnosticPreviewFragment;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.TestTypeListActivity;
import org.akvo.caddisfly.ui.TypeListActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ListViewUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiagnosticPreferenceFragment extends PreferenceFragment {

    private static final int PERMISSION_ALL = 1;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
    private static final int MAX_TOLERANCE = 399;

    private ListView list;
    private View coordinatorLayout;

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
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        final EditTextPreference sampleTimesPreference =
                (EditTextPreference) findPreference(getString(R.string.samplingsTimeKey));
        if (sampleTimesPreference != null) {

            sampleTimesPreference.setSummary(sampleTimesPreference.getText());

            sampleTimesPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        if (Integer.parseInt(String.valueOf(newValue)) > ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT) {
                            newValue = ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT;
                        }

                        if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                            newValue = 1;
                        }

                    } catch (Exception e) {
                        newValue = ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT;
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
                    final Intent intent = new Intent(getActivity(), TypeListActivity.class);
                    intent.putExtra("runTest", true);
                    startActivity(intent);
                    return true;
                }
            });
        }

        final Preference startStripTestPreference = findPreference("startStripTest");
        if (startStripTestPreference != null) {
            startStripTestPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    final Intent intent = new Intent(getActivity(), TestTypeListActivity.class);
                    intent.putExtra("internal", true);
                    startActivity(intent);
                    return true;
                }
            });
        }

        final Preference cameraPreviewPreference = findPreference("cameraPreview");
        if (cameraPreviewPreference != null) {
            cameraPreviewPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (getFragmentManager().findFragmentByTag("diagnosticPreviewFragment") == null) {

                        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (AppPreferences.useExternalCamera()) {
                            permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ApiUtil.hasPermissions(getActivity(), permissions)) {
                            requestPermissions(permissions, PERMISSION_ALL);
                        } else {
                            startPreview();
                        }
                    }
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
                    try {
                        if (Integer.parseInt(String.valueOf(newValue)) > MAX_TOLERANCE) {
                            newValue = MAX_TOLERANCE;
                        }

                        if (Integer.parseInt(String.valueOf(newValue)) < 1) {
                            newValue = 1;
                        }

                    } catch (Exception e) {
                        newValue = ColorimetryLiquidConfig.MAX_COLOR_DISTANCE_RGB;
                    }
                    distancePreference.setText(String.valueOf(newValue));
                    distancePreference.setSummary(String.valueOf(newValue));
                    return false;
                }
            });
        }

        coordinatorLayout = rootView;
        return rootView;
    }

    private void startPreview() {
        if (isCameraAvailable()) {
            CaddisflyApp.getApp().initializeCurrentTest();
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            DiagnosticPreviewFragment diagnosticPreviewFragment = DiagnosticPreviewFragment.newInstance();
            diagnosticPreviewFragment.show(ft, "diagnosticPreviewFragment");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_ALL) {
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
                startPreview();
            } else {
                String message = getString(R.string.cameraAndStoragePermissions);
                if (AppPreferences.useExternalCamera()) {
                    message = getString(R.string.storagePermission);
                }
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction("SETTINGS", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ApiUtil.startInstalledAppDetailsActivity(getActivity());
                            }
                        });

                TypedValue typedValue = new TypedValue();
                getActivity().getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                snackbar.setActionTextColor(typedValue.data);
                View snackView = snackbar.getView();
                TextView textView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
                textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isCameraAvailable() {
        Camera camera = null;
        try {
            camera = CameraHelper.getCamera(getActivity(), new DialogInterface.OnClickListener() {
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
