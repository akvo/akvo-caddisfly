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

package org.akvo.caddisfly.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;

public class ResultFragment extends DialogFragment {

    public static ResultFragment newInstance(String title, double result, Message msg, String unit) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putDouble("result", result);
        //args.putBundle("message", msg.getData());
        args.putString("unit", unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.result);

        final View view = inflater.inflate(R.layout.fragment_result, container, false);

        TextView resultView = (TextView) view.findViewById(R.id.result);
        TextView titleView = (TextView) view.findViewById(R.id.titleView);
        TextView unitTextView = (TextView) view.findViewById(R.id.ppmText);
        Button button = (Button) view.findViewById(R.id.endSurveyButton);

        final Bundle bundle = getArguments().getBundle("message");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ResultDialogListener listener = (ResultDialogListener) getActivity();
                listener.onFinishDialog(bundle);

            }
        });

        titleView.setText(getArguments().getString("title", ""));

        double result = getArguments().getDouble("result", -1);

        if (result > 999) {
            resultView.setText(String.format("%.0f", result));
        } else {
            resultView.setText(String.format("%.2f", result));
        }


        unitTextView.setText(getArguments().getString("unit", ""));

        return view;
    }

    public interface ResultDialogListener {
        void onFinishDialog(Bundle bundle);
    }
}
