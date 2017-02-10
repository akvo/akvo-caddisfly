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
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SaveCalibrationDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SaveCalibrationDialogFragment extends DialogFragment {

    private final Calendar calendar = Calendar.getInstance();
    private EditText editName = null;
    private EditText editBatchCode = null;
    private EditText editExpiryDate;
    private EditText editRgb;
    private boolean isEditing = false;

    public SaveCalibrationDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SaveCalibrationDialogFragment.
     */
    public static SaveCalibrationDialogFragment newInstance(boolean isEdit) {
        SaveCalibrationDialogFragment saveCalibrationDialogFragment = new SaveCalibrationDialogFragment();
        saveCalibrationDialogFragment.isEditing = isEdit;
        return saveCalibrationDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String testCode = CaddisflyApp.getApp().getCurrentTestInfo().getId();
        final Context context = getActivity();

        LayoutInflater i = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = i.inflate(R.layout.fragment_save_calibration, null);

        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String date = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(calendar.getTime());
                editExpiryDate.setText(date);
            }
        };

        editExpiryDate = (EditText) view.findViewById(R.id.editExpiryDate);

        editBatchCode = (EditText) view.findViewById(R.id.editBatchCode);

        editRgb = (EditText) view.findViewById(R.id.editRgb);

        long milliseconds = PreferencesUtil.getLong(getActivity(),
                CaddisflyApp.getApp().getCurrentTestInfo().getId(),
                R.string.calibrationExpiryDateKey);
        if (milliseconds > new Date().getTime()) {

            editBatchCode.setText(PreferencesUtil.getString(context, testCode, R.string.batchNumberKey, "").trim());

            long expiryDate = PreferencesUtil.getLong(getContext(), testCode, R.string.calibrationExpiryDateKey);
            if (expiryDate >= 0) {
                calendar.setTimeInMillis(expiryDate);

                editExpiryDate.setText(new SimpleDateFormat("dd-MMM-yyyy", Locale.US)
                        .format(new Date(expiryDate)));
            }
        }

        editRgb.setText(PreferencesUtil.getString(context, testCode, R.string.ledRgbKey, "").trim());

        if (!AppPreferences.useExternalCamera()) {
            editRgb.setVisibility(View.GONE);
            view.findViewById(R.id.textRgb).setVisibility(View.GONE);
        }

        final DatePickerDialog datePickerDialog = new DatePickerDialog(context, onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 1);
        date.set(Calendar.HOUR_OF_DAY, date.getMinimum(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, date.getMinimum(Calendar.MINUTE));
        date.set(Calendar.SECOND, date.getMinimum(Calendar.SECOND));
        date.set(Calendar.MILLISECOND, date.getMinimum(Calendar.MILLISECOND));
        datePickerDialog.getDatePicker().setMinDate(date.getTimeInMillis());
        date.add(Calendar.MONTH, CaddisflyApp.getApp().getCurrentTestInfo().getMonthsValid());
        date.set(Calendar.HOUR_OF_DAY, date.getMaximum(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, date.getMaximum(Calendar.MINUTE));
        date.set(Calendar.SECOND, date.getMaximum(Calendar.SECOND));
        date.set(Calendar.MILLISECOND, date.getMaximum(Calendar.MILLISECOND));
        datePickerDialog.getDatePicker().setMaxDate(date.getTimeInMillis());

        editExpiryDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    closeKeyboard(getContext(), editBatchCode);
                    datePickerDialog.show();
                }
            }
        });

        editExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeKeyboard(getContext(), editBatchCode);
                datePickerDialog.show();
            }
        });

        editName = (EditText) view.findViewById(R.id.editName);
        if (!isEditing && AppPreferences.isDiagnosticMode()) {
            editName.requestFocus();
        } else {
            editName.setVisibility(View.GONE);
            editBatchCode.requestFocus();
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

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

        b.setView(view);
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

                        final String testCode = CaddisflyApp.getApp().getCurrentTestInfo().getId();

                        if (!editName.getText().toString().trim().isEmpty()) {

                            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, testCode);

                            File file = new File(path, editName.getText().toString());

                            if (file.exists()) {
                                AlertUtil.askQuestion(context, R.string.fileAlreadyExists,
                                        R.string.doYouWantToOverwrite, R.string.overwrite, R.string.cancel, true,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                saveCalibrationDetails(path);
                                                saveDetails(testCode);
                                                closeKeyboard(context, editName);
                                                dismiss();
                                            }
                                        }, null
                                );
                            } else {
                                saveCalibrationDetails(path);
                                saveDetails(testCode);
                                closeKeyboard(context, editName);
                                dismiss();
                            }
                        } else {
                            saveDetails(testCode);
                            closeKeyboard(context, editExpiryDate);
                            dismiss();
                        }
                    }
                }

                void saveDetails(String testCode) {
                    PreferencesUtil.setLong(context, testCode,
                            R.string.calibrationExpiryDateKey, calendar.getTimeInMillis());

                    PreferencesUtil.setString(context, testCode,
                            R.string.batchNumberKey, editBatchCode.getText().toString().trim());

                    PreferencesUtil.setString(context, testCode,
                            R.string.ledRgbKey, editRgb.getText().toString().trim());

                    ((CalibrationDetailsSavedListener) getActivity()).onCalibrationDetailsSaved();
                }

                private boolean formEntryValid() {

                    if (!isEditing && AppPreferences.isDiagnosticMode()
                            && editName.getText().toString().trim().isEmpty()) {
                        editName.setError(getString(R.string.saveInvalidFileName));
                        return false;
                    }
                    if (editBatchCode.getText().toString().trim().isEmpty()) {
                        editBatchCode.setError(getString(R.string.required));
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

    private void saveCalibrationDetails(File path) {
        final Context context = getContext();

        final String testCode = CaddisflyApp.getApp().getCurrentTestInfo().getId();

        final String calibrationDetails = SwatchHelper.generateCalibrationFile(context,
                testCode, editBatchCode.getText().toString().trim(),
                Calendar.getInstance().getTimeInMillis(),
                calendar.getTimeInMillis(), editRgb.getText().toString(),
                !PreferencesUtil.getBoolean(context, R.string.noBackdropDetectionKey, false));

        FileUtil.saveToFile(path, editName.getText().toString().trim(), calibrationDetails);

        Toast.makeText(context, R.string.fileSaved, Toast.LENGTH_SHORT).show();

    }

    /**
     * Hides the keyboard.
     *
     * @param input the EditText for which the keyboard is open
     */
    private void closeKeyboard(Context context, EditText input) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

            View view = getActivity().getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        closeKeyboard(getActivity(), editBatchCode);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeKeyboard(getActivity(), editBatchCode);
    }

    public interface CalibrationDetailsSavedListener {
        void onCalibrationDetailsSaved();
    }
}
