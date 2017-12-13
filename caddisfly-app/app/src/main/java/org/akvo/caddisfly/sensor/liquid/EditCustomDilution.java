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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.akvo.caddisfly.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCustomDilutionListener} interface
 * to handle interaction events.
 * Use the {@link EditCustomDilution#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCustomDilution extends DialogFragment {

    private OnCustomDilutionListener mListener;
    private EditText editDilutionFactor;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditSensorIdentity.
     */
    public static EditCustomDilution newInstance() {
        EditCustomDilution fragment = new EditCustomDilution();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity activity = getActivity();

        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.edit_custom_dilution, null);

        editDilutionFactor = view.findViewById(R.id.editDilutionFactor);
        editDilutionFactor.requestFocus();

        AlertDialog.Builder b = new AlertDialog.Builder(activity)
                .setTitle(R.string.customDilution)
                .setPositiveButton(R.string.ok,
                        (dialog, whichButton) -> {
                            closeKeyboard(activity, editDilutionFactor);
                            dismiss();
                        }
                )
                .setNegativeButton(R.string.cancel,
                        (dialog, whichButton) -> {
                            closeKeyboard(activity, editDilutionFactor);
                            dismiss();
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
                    if (formEntryValid() && !editDilutionFactor.getText().toString().trim().isEmpty()) {
                        if (mListener != null) {
                            mListener.onCustomDilution(Integer.parseInt(editDilutionFactor.getText().toString()));
                        }
                        closeKeyboard(context, editDilutionFactor);
                        dismiss();
                    }
                }

                private boolean formEntryValid() {
                    if (editDilutionFactor.getText().toString().trim().isEmpty()) {
                        editDilutionFactor.setError(getString(R.string.required));
                        return false;
                    }

                    return true;
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCustomDilutionListener) {
            mListener = (OnCustomDilutionListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        editDilutionFactor.post(() -> {
            editDilutionFactor.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) editDilutionFactor.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.showSoftInput(editDilutionFactor, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    /**
     * Hides the keyboard
     *
     * @param input the EditText for which the keyboard is open
     */
    private void closeKeyboard(Context context, EditText input) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }

    public interface OnCustomDilutionListener {
        void onCustomDilution(Integer value);
    }

}
