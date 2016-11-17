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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link InstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * This fragment shows instructions for a particular strip test. They are read from strips.json in assets
 */
public class InstructionFragment extends CameraSharedFragmentBase {

    private static final String TAG = "InstructionFragment";

    private static final int BUTTON_ENABLE_DELAY = 4000;
    private static final int ANIMATION_DURATION_MILLIS = 2000;
    private static final float BUTTON_START_ALPHA = 0.2f;
    private CameraViewListener mListener;

    public InstructionFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static InstructionFragment newInstance(String uuid) {
        InstructionFragment fragment = new InstructionFragment();
        Bundle args = new Bundle();
        args.putString(Constant.UUID, uuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instruction, container, false);
        final Button buttonStart = (Button) rootView.findViewById(R.id.button_start);
        buttonStart.setEnabled(false);
        buttonStart.setAlpha(BUTTON_START_ALPHA);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.layout_information);

        TextView textTitle = (TextView) rootView.findViewById(R.id.textToolbarTitle);
        if (textTitle != null) {
            textTitle.setText(R.string.instructions);
        }

        if (getArguments() != null) {

            String uuid = getArguments().getString(Constant.UUID);

            StripTest stripTest = new StripTest();
            JSONArray instructions = stripTest.getBrand(getContext(), uuid).getInstructions();

            showInstruction(linearLayout, getString(R.string.success_quality_checks), Typeface.BOLD);

            if (instructions != null) {
                try {
                    for (int i = 0; i < instructions.length(); i++) {
                        for (String instruction : instructions.getJSONObject(i).getString("text").split("<!")) {
                            showInstruction(linearLayout, instruction, Typeface.NORMAL);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

        }

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.nextFragment();
            }
        });

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                buttonStart.setEnabled(true);
                AlphaAnimation animation = new AlphaAnimation(BUTTON_START_ALPHA, 1f);
                buttonStart.setAlpha(1f);
                animation.setDuration(ANIMATION_DURATION_MILLIS);
                buttonStart.startAnimation(animation);
            }
        }, BUTTON_ENABLE_DELAY);


        return rootView;
    }

    private void showInstruction(@NonNull LinearLayout linearLayout, @NonNull String instruction, int style) {
        TextView textView = new TextView(getActivity());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.mediumTextSize));

        textView.setPadding(0, 0, 0,
                (int) getResources().getDimension(R.dimen.activity_vertical_margin));

        if (instruction.contains(">")) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.DKGRAY);
        }

        textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,
                getResources().getDisplayMetrics()), 1.0f);

        textView.setTypeface(null, style);

        String text = instruction.replaceAll(">", "");
        if (!text.isEmpty()) {
            textView.append(text);
            linearLayout.addView(textView);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(context.toString()
                    + " must implement CameraViewListener", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected void hideProgressBar() {

    }
}
