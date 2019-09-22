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

package org.akvo.caddisfly.sensor.manual;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseFragment;

public class SwatchSelectFragment extends BaseFragment {
    private static final String ARG_RESULTS = "results";
    private static final String ARG_RANGE = "range";
    private static final String RESULT_ARRAY = "result_array";
    private OnSwatchSelectListener mListener;
    private float[] mResults;
    private String range;

    public static SwatchSelectFragment newInstance(float[] results, String range) {
        SwatchSelectFragment fragment = new SwatchSelectFragment();
        Bundle args = new Bundle();
        args.putFloatArray(ARG_RESULTS, results);
        args.putString(ARG_RANGE, range);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResults = getArguments().getFloatArray(ARG_RESULTS);
            range = getArguments().getString(ARG_RANGE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putFloatArray(RESULT_ARRAY, mResults);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_swatch_select, container, false);

        final SwatchSelectWidget swatchSelect = view.findViewById(R.id.swatch_select);

        if (range.contains("6.0")) {
            swatchSelect.setRange(2);
        }

        if (savedInstanceState != null) {
            mResults = savedInstanceState.getFloatArray(RESULT_ARRAY);
        }

        swatchSelect.setResults(mResults);

        swatchSelect.setOnClickListener(v -> {
            mResults = swatchSelect.getResult();

            if (mListener != null) {
                mListener.onSwatchSelect(mResults);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSwatchSelectListener) {
            mListener = (OnSwatchSelectListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnSwatchSelectListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    boolean isValid() {
        return mResults != null && mResults[0] != 0 && mResults[1] != 0;
    }

    public interface OnSwatchSelectListener {
        void onSwatchSelect(float[] key);
    }
}
