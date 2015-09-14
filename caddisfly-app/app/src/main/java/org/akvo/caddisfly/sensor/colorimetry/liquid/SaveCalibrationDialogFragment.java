/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.DateUtil;
import org.akvo.caddisfly.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SaveCalibrationDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SaveCalibrationDialogFragment extends DialogFragment {

    private final Calendar calendar = Calendar.getInstance();
    private EditText editName = null;
    private EditText editBatchNumber = null;
    private EditText editExpiryDate;

    public SaveCalibrationDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SaveCalibrationDialogFragment.
     */
    public static SaveCalibrationDialogFragment newInstance() {
        return new SaveCalibrationDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = getActivity();

        LayoutInflater i = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View v = i.inflate(R.layout.fragment_save_calibration, null);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String date = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(calendar.getTime());
                editExpiryDate.setText(String.format("%s (Reagent expiry)", date));
            }
        };

        final DatePickerDialog datePickerDialog = new DatePickerDialog(context, date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        //datePickerDialog.setTitle(R.string.reagentExpiryDate);

        editExpiryDate = (EditText) v.findViewById(R.id.editExpiryDate);

        editExpiryDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    datePickerDialog.show();
                }
            }
        });

        editExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        editName = (EditText) v.findViewById(R.id.editName);
        if (AppPreferences.isDiagnosticMode()) {
            editName.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            editName.setVisibility(View.GONE);
        }
        editBatchNumber = (EditText) v.findViewById(R.id.editBatchCode);

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.calibrationDetails)
                .setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                closeKeyboard(context, editName);
                                dismiss();
                            }
                        }
                );

        b.setView(v);
        return b.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final Context context = getActivity();

        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (formEntryValid()) {
                        final StringBuilder calibrationDetails = new StringBuilder();

                        for (Swatch swatch : CaddisflyApp.getApp().getCurrentTestInfo().getSwatches()) {
                            calibrationDetails.append(String.format("%.2f", swatch.getValue()))
                                    .append("=")
                                    .append(ColorUtil.getColorRgbString(swatch.getColor()));
                            calibrationDetails.append('\n');
                        }

                        calibrationDetails.append("Type: ");
                        calibrationDetails.append(CaddisflyApp.getApp().getCurrentTestInfo().getCode());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Date: ");
                        calibrationDetails.append(DateUtil.getDateTimeString());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Calibrated: ");
                        calibrationDetails.append(DateUtil.getDateTimeString());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("ReagentExpiry: ");
                        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime()));
                        calibrationDetails.append("\n");
                        calibrationDetails.append("ReagentBatch: ");
                        calibrationDetails.append(editBatchNumber.getText().toString());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Version: ");
                        calibrationDetails.append(CaddisflyApp.getAppVersion(context));
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Model: ");
                        calibrationDetails.append(android.os.Build.MODEL).append(" (")
                                .append(android.os.Build.PRODUCT).append(")");
                        calibrationDetails.append("\n");
                        calibrationDetails.append("OS: ");
                        calibrationDetails.append(android.os.Build.VERSION.RELEASE).append(" (")
                                .append(android.os.Build.VERSION.SDK_INT).append(")");
                        calibrationDetails.append("\n");
                        calibrationDetails.append("DeviceId: ");
                        calibrationDetails.append(ApiUtil.getEquipmentId(context));

                        if (!editName.getText().toString().trim().isEmpty()) {
                            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                                    CaddisflyApp.getApp().getCurrentTestInfo().getCode());
                            File file = new File(path, editName.getText().toString());

                            if (file.exists()) {
                                AlertUtil.askQuestion(context, R.string.fileAlreadyExists,
                                        R.string.doYouWantToOverwrite, R.string.overwrite, R.string.cancel, true,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                FileUtil.saveToFile(path, editName.getText().toString(),
                                                        calibrationDetails.toString());
                                                Toast.makeText(context, R.string.fileSaved, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );
                            } else {
                                FileUtil.saveToFile(path, editName.getText().toString(),
                                        calibrationDetails.toString());
                                Toast.makeText(context, R.string.fileSaved, Toast.LENGTH_SHORT).show();
                            }
                        }

                        closeKeyboard(context, editName);
                        dismiss();
                    }
                }

                private boolean formEntryValid() {

                    if (AppPreferences.isDiagnosticMode() &&
                            editName.getText().toString().trim().isEmpty()) {
                        editName.setError(getString(R.string.saveInvalidFileName));
                        return false;
                    }
                    if (editBatchNumber.getText().toString().trim().isEmpty()) {
                        editBatchNumber.setError(getString(R.string.required));
                        return false;
                    }

                    if (editExpiryDate.getText().toString().trim().isEmpty()) {
                        editExpiryDate.setError(getString(R.string.required));
                        return false;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * Hides the keyboard
     *
     * @param input the EditText for which the keyboard is open
     */
    private void closeKeyboard(Context context, EditText input) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

}
