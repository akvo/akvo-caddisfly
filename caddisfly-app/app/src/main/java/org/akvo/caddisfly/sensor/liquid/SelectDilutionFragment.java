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

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.FragmentSelectDilutionBinding;
import org.akvo.caddisfly.model.TestInfo;

import java.util.Locale;

public class SelectDilutionFragment extends Fragment {
    private static final String ARG_PARAM_TEST_INFO = "testInfo";

    private TestInfo testInfo;
    private OnDilutionSelectedListener mListener;
    private FragmentSelectDilutionBinding b;

    public SelectDilutionFragment() {
        // Required empty public constructor
    }

    public static SelectDilutionFragment newInstance(TestInfo testInfo) {
        SelectDilutionFragment fragment = new SelectDilutionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM_TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            testInfo = getArguments().getParcelable(ARG_PARAM_TEST_INFO);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_select_dilution,
                container, false);

        //todo: remove hardcoding of dilution times
        b.buttonDilution1.setText(String.format(Locale.getDefault(), getString(R.string.timesDilution), 2));
        b.buttonDilution2.setText(String.format(Locale.getDefault(), getString(R.string.timesDilution), 5));

        b.buttonNoDilution.setOnClickListener(view1 -> mListener.onDilutionSelected(1));

        b.buttonDilution1.setOnClickListener(view1 -> mListener.onDilutionSelected(2));

        b.buttonDilution2.setOnClickListener(view1 -> mListener.onDilutionSelected(5));

        b.buttonCustomDilution.setOnClickListener(view1 -> showCustomDilutionDialog());

        ((TextView) b.getRoot().findViewById(R.id.textTitle)).setText(testInfo.getName());

        return b.getRoot();
    }

    private void showCustomDilutionDialog() {
        if (getActivity() != null) {
            final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            EditCustomDilution editCustomDilution = EditCustomDilution.newInstance();
            editCustomDilution.show(ft, "editCustomDilution");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDilutionSelectedListener) {
            mListener = (OnDilutionSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDilutionSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnDilutionSelectedListener {
        void onDilutionSelected(int dilution);
    }
}
