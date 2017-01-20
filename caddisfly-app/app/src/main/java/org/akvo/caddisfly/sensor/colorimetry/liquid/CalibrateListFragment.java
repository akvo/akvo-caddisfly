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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.Date;

/**
 * A list fragment representing a list of Calibrate items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link CalibrateListFragment.Callbacks}
 * interface.
 */
public class CalibrateListFragment extends ListFragment {

    /**
     * A dummy implementation of the interface that does nothing.
     * Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks DUMMY_CALLBACKS = new Callbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };
    private static final long MIN_DELAY_MILLIS = 1000;
    /**
     * The fragment's current callback object, which is notified of list item clicks
     */
    private Callbacks mCallbacks = DUMMY_CALLBACKS;
    private TextView textCalibrationError;
    private long mLastClickTime;

    public CalibrateListFragment() {
    }

    void setAdapter() {
        TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        CalibrationsAdapter calibrationsAdapter = new CalibrationsAdapter(getActivity(),
                currentTestInfo.getSwatches().toArray(
                        new Swatch[currentTestInfo.getSwatches().size()]), currentTestInfo.hasDecimalPlace());

        setListAdapter(calibrationsAdapter);

        validateCalibration();
    }

    private void validateCalibration() {
        TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        long milliseconds = PreferencesUtil.getLong(getActivity(), currentTestInfo.getId(),
                R.string.calibrationExpiryDateKey);
        if (milliseconds != -1 && milliseconds <= new Date().getTime()) {
            textCalibrationError.setText(String.format("%s. %s", getString(R.string.expired),
                    getString(R.string.calibrateWithNewReagent)));
            textCalibrationError.setVisibility(View.VISIBLE);
        } else if (SwatchHelper.isCalibrationComplete(currentTestInfo.getSwatches())
                && !SwatchHelper.isSwatchListValid(currentTestInfo)) {
            //Display error if calibration is completed but invalid
            textCalibrationError.setText(String.format("%s. %s",
                    getString(R.string.calibrationIsInvalid), getString(R.string.tryRecalibrating)));
            textCalibrationError.setVisibility(View.VISIBLE);
        } else {
            textCalibrationError.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calibrate_list, container, false);
        textCalibrationError = (TextView) view.findViewById(R.id.textCalibrationError);
        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Activities containing this fragment must implement its callbacks.
        if (!(context instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = DUMMY_CALLBACKS;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        long lastClickTime = mLastClickTime;
        long now = System.currentTimeMillis();
        mLastClickTime = now;
        if (now - lastClickTime >= MIN_DELAY_MILLIS) {
            mCallbacks.onItemSelected(position);
        }
    }

    public void refresh() {
        validateCalibration();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         *
         * @param id selected item id
         */
        void onItemSelected(int id);
    }
}
