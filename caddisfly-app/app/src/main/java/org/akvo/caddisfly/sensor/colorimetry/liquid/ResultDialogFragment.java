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

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.SensorConstants;

/**
 * Displays the results of an test analysis
 */
public class ResultDialogFragment extends DialogFragment {

    public static ResultDialogFragment newInstance(String title, String result, int dilutionLevel,
                                                   String message, String unit) {
        ResultDialogFragment fragment = new ResultDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString(SensorConstants.RESULT, result);
        args.putInt("dilution", dilutionLevel);
        args.putString("message", message);
        args.putString("unit", unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
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
                listener.onSuccessFinishDialog(true);
            }
        });

        view.findViewById(R.id.buttonDilutionTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultDialogListener listener = (ResultDialogListener) getActivity();
                listener.onSuccessFinishDialog(false);
            }
        });

        //display the title
        ((TextView) view.findViewById(R.id.textTitle)).setText(getArguments().getString("title", ""));
        LinearLayout dilutionLayout = (LinearLayout) view.findViewById(R.id.dilutionLayout);

        String result = getArguments().getString(SensorConstants.RESULT);

        String message = getArguments().getString("message");

        if (message != null && !message.isEmpty()) {
            dilutionLayout.setVisibility(View.VISIBLE);

            TextView textMessage1 = (TextView) view.findViewById(R.id.textMessage1);

            //final String title = getArguments().getString("title");
            textMessage1.setText(getString(R.string.highLevelsFound));

            //TextView textMessage2 = (TextView) view.findViewById(R.id.textMessage2);
            //textMessage2.setText(message);

        } else {
            dilutionLayout.setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.textResult)).setText(result);
        //determine whether to display decimal places
//        ((TextView) view.findViewById(R.id.textResult)).setText(result > 999 ?
//                String.format(Locale.getDefault(), "%.0f", result) :
//                String.format(Locale.getDefault(), "%.2f", result));

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
        ((TextView) view.findViewById(R.id.textName)).setText(getArguments().getString("unit", ""));

        return view;
    }

    public interface ResultDialogListener {
        void onSuccessFinishDialog(boolean resultOk);
    }
}
