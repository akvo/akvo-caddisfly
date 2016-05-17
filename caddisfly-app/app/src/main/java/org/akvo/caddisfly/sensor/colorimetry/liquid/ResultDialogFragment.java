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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.akvo.caddisfly.R;

import java.util.Locale;

/**
 * Displays the results of an test analysis
 */
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

        //set the dialog title
        getDialog().setTitle(R.string.result);

        final View view = inflater.inflate(R.layout.fragment_result, container, false);

        view.findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultDialogListener listener = (ResultDialogListener) getActivity();
                listener.onSuccessFinishDialog();
            }
        });

        //display the title
        ((TextView) view.findViewById(R.id.textTitle)).setText(getArguments().getString("title", ""));

        double result = getArguments().getDouble("result", -1);

        //determine whether to display decimal places
        ((TextView) view.findViewById(R.id.textResult)).setText(result > 999 ?
                String.format(Locale.getDefault(), "%.0f", result) :
                String.format(Locale.getDefault(), "%.2f", result));

        //display dilution information
        TextView textDilution = (TextView) view.findViewById(R.id.textDilution);
        int dilutionLevel = getArguments().getInt("dilution", -1);

        textDilution.setVisibility(View.VISIBLE);

        //todo: remove hard coding of dilution levels
        switch (dilutionLevel) {
            case 0:
                textDilution.setText(R.string.noDilution);
                break;
            case 1:
                textDilution.setText(String.format(getString(R.string.timesDilution), 2));
                break;
            case 2:
                textDilution.setText(String.format(getString(R.string.timesDilution), 5));
                break;
            default:
                textDilution.setVisibility(View.GONE);
                break;
        }

        //display the unit
        ((TextView) view.findViewById(R.id.textUnit)).setText(getArguments().getString("unit", ""));

        return view;
    }

    public interface ResultDialogListener {
        void onSuccessFinishDialog();
    }
}
