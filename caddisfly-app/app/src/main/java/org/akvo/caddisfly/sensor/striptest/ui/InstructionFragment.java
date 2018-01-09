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

package org.akvo.caddisfly.sensor.striptest.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.StringUtil;

import java.util.List;

import static android.graphics.Typeface.BOLD;

public class InstructionFragment extends Fragment {

    private static final int BUTTON_ENABLE_DELAY = 4000;
    private static final int ANIMATION_DURATION_MILLIS = 2000;
    private static final float BUTTON_START_ALPHA = 0.2f;
    private StripMeasureListener mListener;

    @NonNull
    public static InstructionFragment newInstance(TestInfo testInfo, int testStage) {
        InstructionFragment fragment = new InstructionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        args.putInt(ConstantKey.TEST_STAGE, testStage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_strip_instruction, container, false);
        final Button buttonStart = rootView.findViewById(R.id.button_start);
        buttonStart.setEnabled(false);
        buttonStart.setAlpha(BUTTON_START_ALPHA);
        LinearLayout linearLayout = rootView.findViewById(R.id.layout_information);

        TextView textTitle = rootView.findViewById(R.id.textToolbarTitle);
        if (textTitle != null) {
            textTitle.setText(R.string.instructions);
        }

        if (getArguments() != null) {

            TestInfo testInfo = getArguments().getParcelable(ConstantKey.TEST_INFO);
            int testStage = getArguments().getInt(ConstantKey.TEST_STAGE);

            if (testInfo != null) {
                List<Instruction> instructions = testInfo.getInstructions();

                // If quality check was finished and this is the start of the first stage of the test
                if (testStage == 1) {
                    showInstruction(linearLayout, getString(R.string.success_quality_checks), BOLD);
                }

                if (instructions != null) {
                    for (int i = 0; i < instructions.size(); i++) {
                        Instruction instruction = instructions.get(i);
                        List<String> section = instruction.section;

                        if (Math.max(instruction.testStage, 1) == testStage) {
                            for (int j = 0; j < section.size(); j++) {
                                if (!section.get(j).startsWith("image:")) {

                                    Spanned spanned = StringUtil.toInstruction((AppCompatActivity) getActivity(), testInfo, section.get(j));
                                    TextView textView = new TextView(getActivity());
                                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            getResources().getDimension(R.dimen.mediumTextSize));

                                    textView.setPadding(
                                            (int) getResources().getDimension(R.dimen.activity_vertical_margin),
                                            0,
                                            (int) getResources().getDimension(R.dimen.activity_vertical_margin),
                                            (int) getResources().getDimension(R.dimen.activity_vertical_margin));

                                    textView.setMovementMethod(LinkMovementMethod.getInstance());

                                    textView.append(spanned);
                                    linearLayout.addView(textView);
                                }
                            }
                        }
                    }
                }
            }
        }

        buttonStart.setOnClickListener(v -> mListener.moveToStripMeasurement());

        (new Handler()).postDelayed(() -> {
            buttonStart.setEnabled(true);
            AlphaAnimation animation = new AlphaAnimation(BUTTON_START_ALPHA, 1f);
            buttonStart.setAlpha(1f);
            animation.setDuration(ANIMATION_DURATION_MILLIS);
            buttonStart.startAnimation(animation);
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

        textView.setTextColor(Color.DKGRAY);

        if (style == BOLD) {
            textView.setTypeface(null, BOLD);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.titleTextSize));
        } else {
            textView.setTextColor(Color.DKGRAY);
        }

        textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,
                getResources().getDisplayMetrics()), 1.0f);

        Spanned spanned = StringUtil.getStringResourceByName(getContext(), instruction);
        if (!instruction.isEmpty()) {
            textView.append(spanned);
            linearLayout.addView(textView);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (StripMeasureListener) context;
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
