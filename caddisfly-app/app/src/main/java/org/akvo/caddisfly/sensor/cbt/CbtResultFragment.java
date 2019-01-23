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

package org.akvo.caddisfly.sensor.cbt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.util.StringUtil;

import java.util.Objects;

public class CbtResultFragment extends BaseFragment {
    private static final String ARG_RESULT = "result";
    private static final String ARG_SAMPLE_QTY = "sample_quantity";

    private String mResult;
    private String mSampleQuantity;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CbtResultFragment.
     */
    public static CbtResultFragment newInstance(String result, String sampleQuantity) {
        CbtResultFragment fragment = new CbtResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESULT, result);
        args.putString(ARG_SAMPLE_QTY, sampleQuantity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResult = getArguments().getString(ARG_RESULT);
            mSampleQuantity = getArguments().getString(ARG_SAMPLE_QTY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cbt_result, container, false);

        TextView textResult = view.findViewById(R.id.textResult);
        TextView textResult1 = view.findViewById(R.id.textResult1);
        LinearLayout layoutResult = view.findViewById(R.id.layoutResult);
        LinearLayout layoutResult1 = view.findViewById(R.id.layoutResult1);
        LinearLayout layoutResult2 = view.findViewById(R.id.layoutResult2);

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(mResult, mSampleQuantity);

        String[] results = StringUtil.getStringResourceByName(Objects.requireNonNull(getActivity()),
                mpnValue.getRiskCategory()).toString().split("/");

        textResult.setText(results[0].trim());
        if (results.length > 1) {
            textResult1.setText(results[1].trim());
        }

        layoutResult.setBackgroundColor(mpnValue.getBackgroundColor1());
        layoutResult1.setBackgroundColor(mpnValue.getBackgroundColor2());
        layoutResult2.setBackgroundColor(mpnValue.getBackgroundColor1());

        TextView textResult2 = view.findViewById(R.id.textResult2);
        TextView textResult3 = view.findViewById(R.id.textResult3);
        textResult2.setText(mpnValue.getMpn());
        textResult3.setText(String.valueOf(mpnValue.getConfidence()));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.result);
        }
    }
}
