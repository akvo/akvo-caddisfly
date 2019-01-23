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

package org.akvo.caddisfly.sensor.manual;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseFragment;
import org.json.JSONObject;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueUnitString;

public class SwatchSelectTestResultFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";

    private TestInfo testInfo;
    private LinearLayout layout;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param testInfo the test info
     * @return A new instance of fragment CbtResultFragment.
     */
    public static SwatchSelectTestResultFragment newInstance(TestInfo testInfo) {
        SwatchSelectTestResultFragment fragment = new SwatchSelectTestResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            testInfo = getArguments().getParcelable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swatch_select_result, container, false);

        layout = view.findViewById(R.id.layout_results);
        layout.removeAllViews();

        for (Result result : testInfo.getResults()) {
            String valueString = createValueUnitString(result.getResultValue(), result.getUnit(),
                    getString(R.string.no_result));
            inflateView(result.getName(), valueString);
        }

        Button buttonSave = view.findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {
            Intent intent = new Intent();

            SparseArray<String> results = new SparseArray<>();

            results.put(1, String.valueOf(testInfo.getResults().get(0).getResultValue()));
            results.put(2, String.valueOf(testInfo.getResults().get(1).getResultValue()));

            JSONObject resultJsonObj = TestConfigHelper.getJsonResult(getActivity(), testInfo,
                    results, null, null);

            if (getActivity() != null) {
                intent.putExtra(SensorConstants.RESPONSE, resultJsonObj.toString());
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            getActivity().setTitle(R.string.result);
        }
    }

    @SuppressLint("InflateParams")
    private void inflateView(String patchDescription, String valueString) {
        LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity())
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemResult;
        if (inflater != null) {
            itemResult = (LinearLayout) inflater.inflate(R.layout.item_result, null, false);
            TextView textTitle = itemResult.findViewById(R.id.text_title);
            textTitle.setText(patchDescription);

            TextView textResult = itemResult.findViewById(R.id.text_result);
            textResult.setText(valueString);
            layout.addView(itemResult);
        }
    }

}
