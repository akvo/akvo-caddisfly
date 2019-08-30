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

package org.akvo.caddisfly.sensor.cbt;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseFragment;

public class CompartmentBagFragment extends BaseFragment {
    private static final String ARG_RESULT_VALUES = "result_key";
    private static final String ARG_USE_BLUE = "use_blue_selection";
    private OnCompartmentBagSelectListener mListener;
    private String resultValues = "";
    private Boolean useBlue;

    public static CompartmentBagFragment newInstance(String key, int id, boolean useBlue) {
        CompartmentBagFragment fragment = new CompartmentBagFragment();
        fragment.setFragmentId(id);
        Bundle args = new Bundle();
        args.putString(ARG_RESULT_VALUES, key);
        args.putBoolean(ARG_USE_BLUE, useBlue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && resultValues.isEmpty()) {
            resultValues = getArguments().getString(ARG_RESULT_VALUES);
            useBlue = getArguments().getBoolean(ARG_USE_BLUE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compartment_bag, container, false);

        final CustomShapeButton customShapeButton = view.findViewById(R.id.compartments);

        customShapeButton.setKey(resultValues);
        customShapeButton.useBlueSelection(useBlue);

        customShapeButton.setOnClickListener(v -> {
            resultValues = customShapeButton.getKey();

            if (mListener != null) {
                mListener.onCompartmentBagSelect(resultValues, getFragmentId());
            }
        });

        return view;
    }

    public String getKey() {
        return resultValues;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCompartmentBagSelectListener) {
            mListener = (OnCompartmentBagSelectListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnCompartmentBagSelectListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setKey(String key) {
        resultValues = key;
    }

    public interface OnCompartmentBagSelectListener {
        void onCompartmentBagSelect(String key, int fragmentId);
    }
}
