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

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.akvo.caddisfly.R;

public class DilutionFragment extends DialogFragment {

    public static DilutionFragment newInstance() {
        return new DilutionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_dilution, container, false);
        getDialog().setTitle(R.string.selectDilution);

        Button noDilutionButton = (Button) view.findViewById(R.id.noDilutionButton);
        Button percentButton1 = (Button) view.findViewById(R.id.percentButton1);
        Button percentButton2 = (Button) view.findViewById(R.id.percentButton2);

        // todo: remove hardcoding of dilution times
        percentButton1.setText(String.format(getString(R.string.timesDilution), 2));
        percentButton2.setText(String.format(getString(R.string.timesDilution), 5));

        noDilutionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DilutionDialogListener) DilutionFragment.this.getActivity()).onFinishDilutionDialog(0);
            }
        });

        percentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DilutionDialogListener) DilutionFragment.this.getActivity()).onFinishDilutionDialog(1);
            }
        });

        percentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DilutionDialogListener) DilutionFragment.this.getActivity()).onFinishDilutionDialog(2);
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                ((DilutionDialogListener) DilutionFragment.this.getActivity()).onFinishDilutionDialog(-1);
            }
        };
    }

    public interface DilutionDialogListener {
        void onFinishDilutionDialog(int index);
    }

}
