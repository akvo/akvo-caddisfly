package org.akvo.caddisfly.ui;


import android.app.Fragment;
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
import org.akvo.caddisfly.util.ListViewUtils;
import org.akvo.caddisfly.util.UpdateCheckTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesOtherFragment extends PreferenceFragment {


    ListView list;

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
