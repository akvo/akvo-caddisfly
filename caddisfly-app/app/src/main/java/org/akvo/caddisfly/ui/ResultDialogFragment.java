/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;

public class ResultDialogFragment extends DialogFragment {

    public static ResultDialogFragment newInstance(String title, double result, int dilutionLevel, String unit) {
        ResultDialogFragment fragment = new ResultDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putDouble("result", result);
        args.putInt("dilution", dilutionLevel);
        args.putString("unit", unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.result);

        final View view = inflater.inflate(R.layout.fragment_result, container, false);

        TextView resultView = (TextView) view.findViewById(R.id.textResult);
        TextView titleView = (TextView) view.findViewById(R.id.textTitle);
        TextView unitTextView = (TextView) view.findViewById(R.id.textUnit);
        Button button = (Button) view.findViewById(R.id.buttonOk);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ResultDialogListener listener = (ResultDialogListener) getActivity();
                listener.onSuccessFinishDialog();

            }
        });

        titleView.setText(getArguments().getString("title", ""));

        double result = getArguments().getDouble("result", -1);

        if (result > 999) {
            resultView.setText(String.format("%.0f", result));
        } else {
            resultView.setText(String.format("%.2f", result));
        }

        TextView dilutionTextView = (TextView) view.findViewById(R.id.textDilution);
        int dilutionLevel = getArguments().getInt("dilution", -1);

        //todo: remove hard coding of dilution levels
        String dilutionLabel;
        switch (dilutionLevel) {
            case 0:
                dilutionTextView.setVisibility(View.VISIBLE);
                dilutionTextView.setText(R.string.noDilution);
                break;
            case 1:
                dilutionLabel = String.format(getString(R.string.timesDilution), 2);
                dilutionTextView.setText(dilutionLabel);
                dilutionTextView.setVisibility(View.VISIBLE);
                break;
            case 2:
                dilutionLabel = String.format(getString(R.string.timesDilution), 5);
                dilutionTextView.setText(dilutionLabel);
                dilutionTextView.setVisibility(View.VISIBLE);
                break;
        }

        unitTextView.setText(getArguments().getString("unit", ""));

        return view;
    }

    public interface ResultDialogListener {
        void onSuccessFinishDialog();
    }
}
