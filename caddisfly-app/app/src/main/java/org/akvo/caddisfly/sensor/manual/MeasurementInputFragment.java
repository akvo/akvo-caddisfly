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
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.FragmentManualInputBinding;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseFragment;

public class MeasurementInputFragment extends BaseFragment {
    private static final String ARG_INSTRUCTION = "resultInstruction";
    private static final String ARG_TEST_INFO = "testInfo";
    private static final String ARG_RESULT_ID = "resultId";
    private Float resultFloat;
    private OnSubmitResultListener listener;
    private Float minValue;
    private Float maxValue;
    private FragmentManualInputBinding b;

    /**
     * Get the instance.
     */
    public static MeasurementInputFragment newInstance(TestInfo testInfo, int resultId,
                                                       Instruction instruction, int id) {
        MeasurementInputFragment fragment = new MeasurementInputFragment();
        fragment.setFragmentId(id);
        Bundle args = new Bundle();
        args.putParcelable(ARG_TEST_INFO, testInfo);
        args.putInt(ARG_RESULT_ID, resultId);
        args.putParcelable(ARG_INSTRUCTION, instruction);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = DataBindingUtil.inflate(inflater,
                R.layout.fragment_manual_input, container, false);

        if (getArguments() != null) {

            Instruction instruction = getArguments().getParcelable(ARG_INSTRUCTION);
            b.setInstruction(instruction);

            TestInfo testInfo = getArguments().getParcelable(ARG_TEST_INFO);
            int resultId = getArguments().getInt(ARG_RESULT_ID);
            Result testResult;
            if (testInfo != null) {
                testResult = testInfo.getResults().get(resultId);

                b.textName.setText(testResult.getName());

                String testResultRange = testResult.getRange();
                String displayedRange = testResultRange.isEmpty() ? testInfo.getMinMaxRange() : testResultRange;
                if (testResult.getUnit().isEmpty()) {
                    b.textRange.setText(String.format("(%s)", displayedRange));
                } else {
                    b.textRange.setText(String.format("(%s %s)", displayedRange, testResult.getUnit()));
                }

                String range = testResultRange.isEmpty() ? testInfo.getRanges(): testResultRange;
                String[] ranges = testResultRange.isEmpty() ? range.split(","): range.split("-");
                minValue = Float.parseFloat(ranges[0].trim());
                maxValue = Float.parseFloat(ranges[1].trim());

                String unitChoice = testResult.getUnitChoice();

                if (unitChoice == null || unitChoice.isEmpty()) {
                    b.unitChoice.setVisibility(View.GONE);
                } else {
                    b.unitChoice.setOnCheckedChangeListener((radioGroup, i) -> {
                        b.editRadioValidation.setError(null);
                        b.editResult.setActivated(true);
                        b.editResult.requestFocus();
                    });
                }

                b.buttonSubmitResult.setOnClickListener(view1 -> {
                    if (listener != null) {
                        resultFloat = isValidResult(true);
                        if (resultFloat != -1f) {
                            listener.onSubmitResult(testResult.getId(), String.valueOf(resultFloat));
                        }
                    }
                });
            }
        }

        return b.getRoot();
    }

    private Float isValidResult(boolean showEmptyError) {
        boolean okToSubmit = true;
        Float resultFloat = -1f;

        if (b.editResult == null) {
            return resultFloat;
        }

        String result = b.editResult.getText().toString();
        if (result.isEmpty()) {
            if (showEmptyError) {
                b.editResult.setError("Enter result");
            }
            resultFloat = -1f;
        } else {

            resultFloat = Float.parseFloat(result);

            if (b.unitChoice.getVisibility() == View.VISIBLE) {
                int radioButtonId = b.unitChoice.getCheckedRadioButtonId();

                if (radioButtonId == -1) {
                    b.editRadioValidation.setActivated(true);
                    b.editRadioValidation.requestFocus();
                    b.editRadioValidation.setError("Select unit");
                    resultFloat = -1f;
                    okToSubmit = false;
                } else {
                    RadioButton selectedRadioButton = b.unitChoice.findViewById(radioButtonId);
                    int index = b.unitChoice.indexOfChild(selectedRadioButton);

                    if (index == 1) {
                        resultFloat = resultFloat * 1000;
                    }
                }
            }

            if (okToSubmit) {
                if (resultFloat < minValue || resultFloat > maxValue) {
                    b.editResult.setError("Invalid result");
                    resultFloat = -1f;
                } else {
                    hideSoftKeyboard(b.editResult);
                }
            }
        }

        return resultFloat;
    }

    void showSoftKeyboard() {
        showSoftKeyboard(b.editResult);
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
        hideSoftKeyboard(b.editResult);
        if (b.editResult != null) {
            b.editResult.setError(null);
        }
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
            listener = (OnSubmitResultListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnSubmitResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @SuppressWarnings("SameParameterValue")
    boolean isValid(boolean showEmptyError) {
        return isValidResult(showEmptyError) != -1f;
    }

    public String getResult() {
        resultFloat = isValidResult(true);
        return String.valueOf(resultFloat);
    }

    public interface OnSubmitResultListener {
        void onSubmitResult(Integer id, String key);
    }
}
