package org.akvo.caddisfly.ui;


import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ListViewUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesGeneralFragment extends PreferenceFragment {

    ListView list;

    public PreferencesGeneralFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        Preference calibratePreference = findPreference("calibrateSensor");
        if (calibratePreference != null) {
            calibratePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    boolean hasOtg = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                    if (hasOtg) {
                        final Intent intent = new Intent(getActivity(), CalibrateSensorActivity.class);
                        startActivity(intent);
                    } else {
                        AlertUtils.showMessage(getActivity(), R.string.notSupported, R.string.phoneDoesNotSupport);
                    }
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            ListViewUtils.setListViewHeightBasedOnChildren(list, 33);
        } else {
            ListViewUtils.setListViewHeightBasedOnChildren(list, 0);
        }
    }
}
