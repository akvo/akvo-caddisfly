/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.adapter.CalibrateListAdapter;
import org.akvo.caddisfly.app.MainApp;

import java.io.File;
import java.util.ArrayList;

public class CalibrateFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private CalibrateItemFragment mCalibrateItemFragment;
    private OnLoadCalibrationListener mOnLoadCalibrationListener;

    public CalibrateFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Intent LaunchIntent = getActivity().getPackageManager()
                .getLaunchIntentForPackage(Config.CADDISFLY_PACKAGE_NAME);
        if (LaunchIntent != null) {
            File external = Environment.getExternalStorageDirectory();
            String path = external.getPath() + Config.CALIBRATE_FOLDER_NAME;
            File folder = new File(path);
            if (folder.exists()) {
                inflater.inflate(R.menu.calibrate, menu);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter();
        getActivity().setTitle(R.string.calibrate);
        getListView().setOnItemClickListener(this);
    }

    void displayCalibrateItem(int index) {

        if (mCalibrateItemFragment == null) {
            mCalibrateItemFragment = new CalibrateItemFragment();
        } else {
            try {
                mCalibrateItemFragment.setArguments(null);
            } catch (Exception e) {
                mCalibrateItemFragment = new CalibrateItemFragment();
            }
        }
        //mCalibrateItemFragment = CalibrateItemFragment.newInstance();

        FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        fragmentManager.executePendingTransactions();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle args = new Bundle();
        args.putInt(getString(R.string.swatchIndex), index);
        MainApp mainApp = (MainApp) getActivity().getApplicationContext();
        args.putString(getString(R.string.currentTestTypeId), mainApp.currentTestInfo.getCode());
        mCalibrateItemFragment.setArguments(args);
        ft.replace(R.id.container, mCalibrateItemFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
        fragmentManager.executePendingTransactions();
    }


    @SuppressWarnings("NullableProblems")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calibrate, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    void setAdapter() {

        Activity activity = getActivity();
        MainApp mainApp = (MainApp) activity.getApplicationContext();

        assert mainApp != null;
        ArrayList<Double> rangeIntervals = mainApp.rangeIntervals;
        Double[] rangeArray = rangeIntervals.toArray(new Double[rangeIntervals.size()]);

        CalibrateListAdapter customList = new CalibrateListAdapter(getActivity(), rangeArray);
        setListAdapter(customList);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        displayCalibrateItem(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_swatches:
                SwatchFragment fragment = new SwatchFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, fragment, String.valueOf(Config.SWATCH_SCREEN_INDEX));
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
                return true;
            case R.id.menu_load:
                Handler.Callback callback = new Handler.Callback() {
                    public boolean handleMessage(Message msg) {
                        CalibrateListAdapter adapter = (CalibrateListAdapter) getListAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        return true;
                    }
                };
                mOnLoadCalibrationListener.onLoadCalibration(callback);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnLoadCalibrationListener = (OnLoadCalibrationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoadCalibrationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnLoadCalibrationListener = null;
    }

    public void dataChanged() {
        setAdapter();
    }

    public interface OnLoadCalibrationListener {
        public void onLoadCalibration(Handler.Callback callback);
    }

}
