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

package org.akvo.caddisfly.sensor.liquid;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.databinding.FragmentItemListBinding;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;

import java.text.DateFormat;
import java.util.Date;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnCalibrationSelectedListener}
 * interface.
 */
public class CalibrationItemFragment extends Fragment {

    private static final String ARG_TEST_INFO = "testInfo";
    private FragmentItemListBinding binding;
    private TestInfo testInfo;
    private OnCalibrationSelectedListener mListener;

    public CalibrationItemFragment() {
    }

    public static CalibrationItemFragment newInstance(TestInfo testInfo) {
        CalibrationItemFragment fragment = new CalibrationItemFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @BindingAdapter("checkValidity")
    public static void validateCalibration(TextView view, TestInfo testInfo) {

        Context context = view.getContext();

        CalibrationDetail calibrationDetail = CaddisflyApp.getApp().getDb()
                .calibrationDao().getCalibrationDetails(testInfo.getUuid());

        if (calibrationDetail != null) {
            long milliseconds = calibrationDetail.expiry;
            if (milliseconds != -1 && milliseconds <= new Date().getTime()) {
                view.setText(String.format("%s. %s", context.getString(R.string.expired),
                        context.getString(R.string.calibrateWithNewReagent)));

                view.setVisibility(View.VISIBLE);
                return;
            }
        }

        if (SwatchHelper.isCalibrationComplete(testInfo.getSwatches())
                && !SwatchHelper.isSwatchListValid(testInfo)) {
            //Display error if calibration is completed but invalid
            view.setText(String.format("%s. %s",
                    context.getString(R.string.calibrationIsInvalid),
                    context.getString(R.string.tryRecalibrating)));
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            testInfo = getArguments().getParcelable(ARG_TEST_INFO);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_item_list, container, false);

        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        model.setTest(testInfo);

        binding.setTestInfoViewModel(model);

        if (getContext() != null) {
            binding.calibrationList.addItemDecoration(new DividerItemDecoration(getContext(), 1));
        }

        setAdapter(testInfo);

        loadDetails();

        return binding.getRoot();
    }

    public void loadDetails() {

        CalibrationDetail calibrationDetail = CaddisflyApp.getApp().getDb()
                .calibrationDao().getCalibrationDetails(testInfo.getUuid());

        if (calibrationDetail != null) {
            binding.textSubtitle.setText(calibrationDetail.batchNumber);

            binding.textSubtitle1.setText(DateFormat
                    .getDateInstance(DateFormat.MEDIUM).format(new Date(calibrationDetail.date)));

            binding.textSubtitle2.setText(String.format("%s: %s", getString(R.string.expires),
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(calibrationDetail.expiry))));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCalibrationSelectedListener) {
            mListener = (OnCalibrationSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalibrationSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setAdapter(TestInfo testInfo) {
        binding.calibrationList.setAdapter(new CalibrationViewAdapter(testInfo, mListener));
        binding.invalidateAll();
    }


    public interface OnCalibrationSelectedListener {
        void onCalibrationSelected(Calibration item);
    }
}
