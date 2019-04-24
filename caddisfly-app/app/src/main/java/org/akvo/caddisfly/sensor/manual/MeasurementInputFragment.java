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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseFragment;

import static org.akvo.caddisfly.common.AppConfig.SKIP_RESULT_VALIDATION;

public class MeasurementInputFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private Float resultFloat;
    private EditText editResult;
    private OnSubmitResultListener mListener;
    private EditText radioValidation;
    private RadioGroup unitRadioGroup;
    private Float minValue;
    private Float maxValue;

    /**
     * Get the instance.
     */
    public static MeasurementInputFragment newInstance(TestInfo testInfo) {
        MeasurementInputFragment fragment = new MeasurementInputFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, testInfo);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manual_input, container, false);

        editResult = view.findViewById(R.id.editResult);
        radioValidation = view.findViewById(R.id.editRadioValidation);
        TextView textRange = view.findViewById(R.id.textRange);
        unitRadioGroup = view.findViewById(R.id.unitChoice);

        if (getArguments() != null) {

            TestInfo testInfo = getArguments().getParcelable(ARG_PARAM1);
            Result testResult;
            if (testInfo != null) {
                testResult = testInfo.getResults().get(0);

                if (testResult.getUnit().isEmpty()) {
                    textRange.setText(String.format("(%s)", testInfo.getMinMaxRange()));
                } else {
                    textRange.setText(String.format("(%s %s)", testInfo.getMinMaxRange(), testResult.getUnit()));
                }

                String range = testInfo.getRanges();
                String[] ranges = range.split(",");
                minValue = Float.parseFloat(ranges[0].trim());
                maxValue = Float.parseFloat(ranges[1].trim());

                String unitChoice = testResult.getUnitChoice();

                if (unitChoice == null || unitChoice.isEmpty()) {
                    unitRadioGroup.setVisibility(View.GONE);
                } else {
                    unitRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                        radioValidation.setError(null);
                        editResult.setActivated(true);
                        editResult.requestFocus();
                    });
                }

                final Button buttonSubmitResult = view.findViewById(R.id.buttonSubmitResult);

                buttonSubmitResult.setOnClickListener(view1 -> {
                    if (mListener != null) {
                        resultFloat = isValidResult();
                        if (resultFloat != -1f) {
                            mListener.onSubmitResult(String.valueOf(resultFloat));
                        }
                    }
                });
            }
        }

        return view;
    }

    private Float isValidResult() {
        boolean okToSubmit = true;
        Float resultFloat = -1f;

        if (editResult == null) {
            return resultFloat;
        }

        String result = editResult.getText().toString();
        if (result.isEmpty()) {
            editResult.setError("Enter result");
            resultFloat = -1f;
        } else {

            resultFloat = Float.parseFloat(result);

            if (unitRadioGroup.getVisibility() == View.VISIBLE) {
                int radioButtonId = unitRadioGroup.getCheckedRadioButtonId();

                if (radioButtonId == -1) {
                    radioValidation.setActivated(true);
                    radioValidation.requestFocus();
                    radioValidation.setError("Select unit");
                    resultFloat = -1f;
                    okToSubmit = false;
                } else {
                    RadioButton selectedRadioButton = unitRadioGroup.findViewById(radioButtonId);
                    int index = unitRadioGroup.indexOfChild(selectedRadioButton);

                    if (index == 1) {
                        resultFloat = resultFloat * 1000;
                    }
                }
            }

            if (okToSubmit) {
                if (resultFloat < minValue || resultFloat > maxValue) {
                    editResult.setError("Invalid result");
                    resultFloat = -1f;
                } else {
                    hideSoftKeyboard(editResult);
                }
            }
        }

        return resultFloat;
    }

    void showSoftKeyboard() {
        showSoftKeyboard(editResult);
    }

    private void showSoftKeyboard(View view) {
        if (getActivity() != null && view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    void hideSoftKeyboard() {
        hideSoftKeyboard(editResult);
    }

    private void hideSoftKeyboard(View view) {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                view.setActivated(true);
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSubmitResultListener) {
            mListener = (OnSubmitResultListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnSubmitResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    boolean isValid() {
        return SKIP_RESULT_VALIDATION || isValidResult() != -1f;
    }

    public String getResult() {
        return String.valueOf(resultFloat);
    }

    public interface OnSubmitResultListener {
        void onSubmitResult(String key);
    }
}
