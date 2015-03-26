package org.akvo.caddisfly.ui;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.util.UpdateCheckTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesOtherFragment extends PreferenceFragment {


    public PreferencesOtherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_other);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        Preference updatePreference = findPreference("checkUpdate");
        if (updatePreference != null) {
            updatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    checkUpdate(false);
                    return true;
                }
            });
        }

        return rootView;
    }

    void checkUpdate(boolean background) {
        UpdateCheckTask updateCheckTask = new UpdateCheckTask(getActivity(), background, MainApp.getVersion(getActivity()));
        updateCheckTask.execute();
    }

}
