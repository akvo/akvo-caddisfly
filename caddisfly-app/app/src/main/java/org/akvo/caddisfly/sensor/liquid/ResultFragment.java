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


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.FragmentResultBinding;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;

import static org.akvo.caddisfly.common.ConstantKey.TEST_INFO;

public class ResultFragment extends Fragment {

    public static ResultFragment newInstance(TestInfo testInfo) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentResultBinding b = DataBindingUtil.inflate(inflater,
                R.layout.fragment_result, container, false);
        View view = b.getRoot();

        if (getActivity() != null) {
            getActivity().setTitle(R.string.result);
        }

        if (getArguments() != null) {
            TestInfo testInfo = getArguments().getParcelable(TEST_INFO);
            if (testInfo != null) {
                Result result = testInfo.Results().get(0);

                b.textResult.setText(result.getResult());
                b.textTitle.setText(testInfo.getName());
                b.textDilution.setText(getResources().getQuantityString(R.plurals.dilutions,
                        testInfo.getDilution(), testInfo.getDilution()));
                b.textUnit.setText(result.getUnit());

                if (testInfo.getDilution() == testInfo.getMaxDilution()) {
                    b.dilutionLayout.setVisibility(View.GONE);
                } else if (result.highLevelsFound()) {
                    b.dilutionLayout.setVisibility(View.VISIBLE);
                } else {
                    b.dilutionLayout.setVisibility(View.GONE);
                }
            }
        }

        return view;
    }
}

