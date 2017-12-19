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
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseFragment;

public class CompartmentBagFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final int BUTTON_ENABLE_DELAY = 1500;
    private static final int ANIMATION_DURATION_MILLIS = 500;
    private static final float BUTTON_START_ALPHA = 0.1f;
    private OnCompartmentBagSelectListener mListener;
    private String mKey = "00000";

    public static CompartmentBagFragment newInstance(String key) {
        CompartmentBagFragment fragment = new CompartmentBagFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKey = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compartment_bag, container, false);

        final CustomShapeButton customShapeButton = view.findViewById(R.id.compartments);

        customShapeButton.setKey(mKey);

        customShapeButton.setOnClickListener(v -> {
            mKey = customShapeButton.getKey();

            if (mListener != null) {
                mListener.onCompartmentBagSelect(mKey);
            }
        });

        final Button buttonNext = view.findViewById(R.id.buttonNext);
        (new Handler()).postDelayed(() -> {
            buttonNext.setEnabled(true);
            AlphaAnimation animation = new AlphaAnimation(BUTTON_START_ALPHA, 1f);
            buttonNext.setAlpha(1f);
            animation.setDuration(ANIMATION_DURATION_MILLIS);
            buttonNext.startAnimation(animation);
        }, BUTTON_ENABLE_DELAY);

        buttonNext.setEnabled(false);
        buttonNext.setAlpha(BUTTON_START_ALPHA);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.setCompartmentColors);
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

    public interface OnCompartmentBagSelectListener {
        void onCompartmentBagSelect(String key);
    }
}
