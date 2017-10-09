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

package org.akvo.caddisfly.sensor.instructions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.colorimetry.bluetooth.ReagentLabel;
import org.akvo.caddisfly.util.AssetsManager;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class InstructionDetailFragment extends Fragment {

    private static final String ILLUSTRATION_PATH = "images/instructions/";
    private static final String ARG_ITEM_TEXT = "text";
    private static final String ARG_ITEM_IMAGE = "image";
    private static final String ARG_ITEM_INFO = "testInfo";
    @BindView(R.id.image_illustration)
    ImageView imageIllustration;
    @BindView(R.id.layout_instructions)
    LinearLayout layoutInstructions;

    public static InstructionDetailFragment newInstance(TestInfo testInfo,
                                                        JSONArray text, String imageName) {
        InstructionDetailFragment fragment = new InstructionDetailFragment();
        Bundle args = new Bundle();

        ArrayList<String> arrayList = new ArrayList<>();
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                try {
                    arrayList.add(text.getString(i));
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }

        args.putStringArrayList(ARG_ITEM_TEXT, arrayList);
        args.putString(ARG_ITEM_IMAGE, imageName);
        args.putParcelable(ARG_ITEM_INFO, testInfo);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_instruction_detail, container, false);

        Context context = getContext();

        ButterKnife.bind(this, rootView);

        Drawable instructionDrawable = AssetsManager.getImage(getActivity(),
                getArguments().getString(ARG_ITEM_IMAGE));

        TestInfo testInfo = getArguments().getParcelable(ARG_ITEM_INFO);

        if (instructionDrawable != null) {
            imageIllustration.setImageDrawable(instructionDrawable);
        }

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getRealSize(size);
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        double divisor = 3;
        if (displayMetrics.densityDpi > 250) {
            divisor = 2.5;
        }

        if (size.y > displayMetrics.heightPixels) {
            divisor += 0.3;
        }

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (displayMetrics.heightPixels / divisor));

        llp.setMargins(0, 0, 0, 20);

        ArrayList<String> instructionText = getArguments().getStringArrayList(ARG_ITEM_TEXT);
        if (instructionText != null) {

            for (String instruction : instructionText) {

                if (instruction.contains("image:")) {

                    String imageName = instruction.substring(instruction.indexOf(":") + 1, instruction.length());

                    int resourceId = context.getResources().getIdentifier("drawable/in_" + imageName,
                            "id", "org.akvo.caddisfly");

                    if (resourceId > 0) {

                        final AppCompatImageView imageView = new AppCompatImageView(context);
                        imageView.setImageResource(resourceId);
                        imageView.setLayoutParams(llp);
                        imageView.setContentDescription(imageName);

                        layoutInstructions.addView(imageView);

                    } else {

                        InputStream ims = null;
                        try {
                            ims = context.getAssets().open(ILLUSTRATION_PATH + imageName + ".webp");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (ims != null) {

                            ImageView imageView = new ImageView(getContext());
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            imageView.setImageDrawable(Drawable.createFromStream(ims, null));

                            imageView.setLayoutParams(llp);

                            layoutInstructions.addView(imageView);

                        }
                    }

                } else {

                    TextView textView = new TextView(getActivity());
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimension(R.dimen.mediumTextSize));

                    textView.setPadding(0, 0, 0,
                            (int) getResources().getDimension(R.dimen.activity_vertical_margin));

                    textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,
                            getResources().getDisplayMetrics()), 1.0f);

                    String text = instruction;

                    textView.setTextColor(Color.DKGRAY);

                    if (instruction.contains("<!>")) {
                        text = instruction.replaceAll("<!>", "");
                        textView.setTextColor(Color.RED);
                    }

                    if (instruction.contains("<b>")) {
                        text = text.replaceAll("<b>", "").replaceAll("</b>", "");
                        textView.setTypeface(null, Typeface.BOLD);
                    }

                    Spanned spanned = StringUtil.toInstruction((AppCompatActivity) getActivity(), testInfo, text);

                    if (!text.isEmpty()) {
                        textView.append(spanned);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());

                        layoutInstructions.addView(textView);

                        SpannableStringBuilder builder = new SpannableStringBuilder();

                        Spanned spanned2 = StringUtil.getStringResourceByName(context, text);
                        builder.append(spanned2);

                        // Set reagent in the string
                        for (int i = 1; i < 5; i++) {
                            Matcher m1 = Pattern.compile("%reagent" + i).matcher(builder);
                            while (m1.find()) {
                                ReagentLabel reagentLabel = new ReagentLabel(context, null);
                                if (testInfo != null) {
                                    reagentLabel.setReagentName(testInfo.getReagent(i - 1));
                                }

                                reagentLabel.setLayoutParams(new FrameLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        250));
                                layoutInstructions.addView(reagentLabel);
                            }
                        }
                    }
                }
            }
        }

        return rootView;
    }
}
