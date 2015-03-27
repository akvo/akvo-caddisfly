package org.akvo.caddisfly.ui;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ListViewUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesDeveloperFragment extends PreferenceFragment {

    ListView list;

    public PreferencesDeveloperFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_developer);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.card_row, container, false);

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
