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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Spanned;
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
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import static android.graphics.Typeface.BOLD;

/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link InstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * This fragment shows instructions for a particular strip test.
 */
public class InstructionFragment extends CameraSharedFragmentBase {

    private static final int BUTTON_ENABLE_DELAY = 4000;
    private static final int ANIMATION_DURATION_MILLIS = 2000;
    private static final float BUTTON_START_ALPHA = 0.2f;
    private CameraViewListener mListener;

    public InstructionFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static InstructionFragment newInstance(String uuid, int phase) {
        InstructionFragment fragment = new InstructionFragment();
        Bundle args = new Bundle();
        args.putString(Constant.UUID, uuid);
        args.putInt(Constant.PHASE, phase);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    public static InstructionFragment newInstance(String uuid) {
        return newInstance(uuid, 1);
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
            int phase = getArguments().getInt(Constant.PHASE);

            StripTest stripTest = new StripTest();
            JSONArray instructions = stripTest.getBrand(uuid).getInstructions();

            if (phase == 1) {
                showInstruction(linearLayout, getString(R.string.success_quality_checks), BOLD);
            }

            if (instructions != null) {
                try {
                    for (int i = 0; i < instructions.length(); i++) {

                        JSONObject object = instructions.getJSONObject(i);
                        Object item = instructions.getJSONObject(i).get("text");

                        if ((object.has("phase") ? object.getInt("phase") : 1) == phase) {
                            JSONArray jsonArray;

                            if (item instanceof JSONArray) {
                                jsonArray = (JSONArray) item;
                            } else {
                                String text = (String) item;
                                jsonArray = new JSONArray();
                                jsonArray.put(text);
                            }

                            for (int j = 0; j < jsonArray.length(); j++) {
                                showInstruction(linearLayout, jsonArray.getString(j), Typeface.NORMAL);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Timber.e(e);
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

        textView.setPadding(
                (int) getResources().getDimension(R.dimen.activity_vertical_margin),
                0,
                (int) getResources().getDimension(R.dimen.activity_vertical_margin),
                (int) getResources().getDimension(R.dimen.activity_vertical_margin));

        String text = instruction;
        if (instruction.contains("<!>")) {
            text = instruction.replaceAll("<!>", "");
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.DKGRAY);
        }

        if (instruction.contains("<b>") || style == BOLD) {
            text = text.replaceAll("<b>", "").replaceAll("</b>", "");
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.titleTextSize));
        } else {
            textView.setTextColor(Color.DKGRAY);
        }

        textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,
                getResources().getDisplayMetrics()), 1.0f);

        Spanned spanned = StringUtil.getStringResourceByName(getContext(), text);
        if (!text.isEmpty()) {
            textView.append(spanned);
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

}
