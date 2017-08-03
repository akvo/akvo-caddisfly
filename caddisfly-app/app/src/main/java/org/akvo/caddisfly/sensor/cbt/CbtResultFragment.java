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

import android.graphics.Color;
import android.os.Bundle;
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

public class CbtResultFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";

    private String mResult;

    public CbtResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CbtResultFragment.
     */
    public static CbtResultFragment newInstance(String result) {
        CbtResultFragment fragment = new CbtResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResult = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cbt_result, container, false);

        TextView textResult = view.findViewById(R.id.textResult);
        TextView textResult1 = view.findViewById(R.id.textResult1);
        TextView textResult2 = view.findViewById(R.id.textResult2);
        TextView textResult3 = view.findViewById(R.id.textResult3);
        LinearLayout layoutResult = view.findViewById(R.id.layoutResult);
        LinearLayout layoutResult1 = view.findViewById(R.id.layoutResult1);

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(mResult);

        String[] results = StringUtil.getStringResourceByName(getActivity(), mpnValue.getRiskCategory()).toString().split("/");

        textResult.setText(results[0].trim());
        if (results.length > 1) {
            textResult1.setText(results[1].trim());
        }else{
            textResult1.setVisibility(View.GONE);
        }

        if (Double.parseDouble(mpnValue.getConfidence()) > 9000) {
            layoutResult1.setBackgroundColor(Color.rgb(210, 23, 23));
            layoutResult.setBackgroundColor(Color.rgb(191, 3, 3));
            textResult.setTextColor(Color.WHITE);
        } else if (Double.parseDouble(mpnValue.getConfidence()) > 100) {
            layoutResult.setBackgroundColor(Color.rgb(190, 70, 6));
            layoutResult1.setBackgroundColor(Color.rgb(180, 63, 30));
        } else if (Double.parseDouble(mpnValue.getConfidence()) > 50) {
            layoutResult.setBackgroundColor(Color.rgb(186, 133, 16));
            layoutResult1.setBackgroundColor(Color.rgb(196, 143, 10));
        } else if (Double.parseDouble(mpnValue.getConfidence()) > 10) {
            layoutResult.setBackgroundColor(Color.rgb(176, 173, 30));
            layoutResult1.setBackgroundColor(Color.rgb(186, 163, 20));
        } else if (Double.parseDouble(mpnValue.getConfidence()) > 3) {
            layoutResult.setBackgroundColor(Color.rgb(142, 163, 20));
            layoutResult1.setBackgroundColor(Color.rgb(150, 153, 20));
        } else {
            layoutResult.setBackgroundColor(Color.rgb(84, 183, 30));
            layoutResult1.setBackgroundColor(Color.rgb(94, 173, 20));
        }

        textResult2.setText(mpnValue.getMpn());

        textResult3.setText(mpnValue.getConfidence());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.result);
    }

}
