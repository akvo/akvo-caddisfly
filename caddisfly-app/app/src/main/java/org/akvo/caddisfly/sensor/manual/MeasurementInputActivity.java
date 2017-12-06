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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.ui.BaseActivity;

public class MeasurementInputActivity extends BaseActivity {
    EditText editResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_meter_input);

        editResult = findViewById(R.id.editResult);

        EditText radioValidation = findViewById(R.id.editRadioValidation);

        TextView textRange = findViewById(R.id.textRange);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        setTitle(testInfo.getTitle());

        TestInfo.SubTest testResult;
        testResult = testInfo.getSubTests().get(0);

        double[] ranges = testInfo.getRangeValues();

        double minValue = ranges[0];
        double maxValue = ranges[1];

        if (testResult.getUnit().isEmpty()) {
            textRange.setText(String.format("(%s - %s)", minValue, maxValue));
        } else {
            textRange.setText(String.format("(%s - %s %s)", minValue, maxValue, testResult.getUnit()));
        }

        String unitChoice = testResult.getUnitChoice();

        RadioGroup unitRadioGroup = findViewById(R.id.unitChoice);

        if (unitChoice == null || unitChoice.isEmpty()) {
            unitRadioGroup.setVisibility(View.GONE);
        } else {
            unitRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                radioValidation.setError(null);
                editResult.setError(null);
                editResult.setActivated(true);
                editResult.requestFocus();
            });
        }

        final Button buttonSubmitResult = findViewById(R.id.buttonSubmitResult);

        buttonSubmitResult.setOnClickListener(view1 -> {

            boolean okToSubmit = true;

            String result = editResult.getText().toString();

            if (result.isEmpty()) {
                editResult.setError("Enter result");
            } else {

                Float resultFloat = Float.parseFloat(result);

                if (unitRadioGroup.getVisibility() == View.VISIBLE) {
                    int radioButtonID = unitRadioGroup.getCheckedRadioButtonId();

                    if (radioButtonID == -1) {
                        radioValidation.setActivated(true);
                        radioValidation.requestFocus();
                        radioValidation.setError("Select unit");
                        okToSubmit = false;
                    } else {
                        RadioButton selectedRadioButton = unitRadioGroup.findViewById(radioButtonID);
                        int index = unitRadioGroup.indexOfChild(selectedRadioButton);

                        if (index == 1) {
                            resultFloat = resultFloat * 1000;
                        }
                    }
                }

                if (okToSubmit) {
                    if (resultFloat < minValue || resultFloat > maxValue) {
                        editResult.setError("Invalid result");
                    } else {
                        hideSoftKeyboard(editResult);
                        Intent intent = new Intent(getIntent());

                        intent.putExtra(SensorConstants.RESPONSE, String.valueOf(resultFloat));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }

        });

        (new Handler()).postDelayed(() -> showSoftKeyboard(editResult), 200);

    }


    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
