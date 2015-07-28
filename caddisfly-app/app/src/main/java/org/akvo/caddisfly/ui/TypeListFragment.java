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

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.adapter.TypeListAdapter;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.FileUtils;
import org.akvo.caddisfly.util.JsonUtils;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class TypeListFragment extends ListFragment {

    private ArrayList<TestInfo> mTests;

    private OnFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TypeListFragment() {
    }

//    public static TypeListFragment newInstance() {
//        return new TypeListFragment();
//    }

    private void setAdapter() {

        Activity activity = getActivity();
        MainApp mainApp = (MainApp) activity.getApplicationContext();
        assert mainApp != null;

        try {
            final String path = Environment.getExternalStorageDirectory() + Config.CONFIG_FOLDER + Config.CONFIG_FILE;

            File file = new File(path);
            String text;

            //Look for external json config file otherwise use the internal default one
            if (file.exists()) {
                text = FileUtils.loadTextFromFile(path);
                //ignore file if it is old version
                if (!text.contains("ranges")) {
                    text = FileUtils.readRawTextFile(getActivity(), R.raw.tests_json);
                }
            } else {
                text = FileUtils.readRawTextFile(getActivity(), R.raw.tests_json);
            }
            mTests = JsonUtils.loadTests(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert mTests != null;
        TypeListAdapter customList = new TypeListAdapter(getActivity(), mTests.toArray(new TestInfo[mTests.size()]));
        setListAdapter(customList);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAdapter();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(mTests.get(position));
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(TestInfo testInfo);
    }

}
