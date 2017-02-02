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

package org.akvo.caddisfly.sensor.colorimetry.strip.instructions;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import timber.log.Timber;

public class InstructionDetailFragment extends Fragment {
    /**
     * The fragment arguments for the contents to be displayed.
     */
    private static final String ARG_ITEM_TEXT = "text";
    private static final String ARG_ITEM_IMAGE = "image";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InstructionDetailFragment() {
    }

    public static InstructionDetailFragment newInstance(JSONArray text, String imageName) {
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
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_instruction_detail, container, false);

        Drawable instructionDrawable = AssetsManager.getImage(getActivity(),
                getArguments().getString(ARG_ITEM_IMAGE));
        if (instructionDrawable != null) {
            ((ImageView) rootView.findViewById(R.id.image_illustration)).
                    setImageDrawable(instructionDrawable);
        }

        ArrayList<String> instructionText = getArguments().getStringArrayList(ARG_ITEM_TEXT);
        if (instructionText != null) {

            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.layout_instructions);
            for (String instruction : instructionText) {
                TextView textView = new TextView(getActivity());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.mediumTextSize));

                textView.setPadding(0, 0, 0,
                        (int) getResources().getDimension(R.dimen.activity_vertical_margin));

                textView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,
                        getResources().getDisplayMetrics()), 1.0f);

                String text = instruction;
                if (instruction.contains("<!>")) {
                    text = instruction.replaceAll("<!>", "");
                    textView.setTextColor(Color.RED);
                } else {
                    textView.setTextColor(Color.DKGRAY);
                }

                if (instruction.contains("<b>")) {
                    text = text.replaceAll("<b>", "").replaceAll("</b>", "");
                    textView.setTypeface(null, Typeface.BOLD);
                } else {
                    textView.setTextColor(Color.DKGRAY);
                }

                Spanned spanned = StringUtil.getStringResourceByName(getContext(), text);
                if (!text.isEmpty()) {
                    textView.append(spanned);
                    linearLayout.addView(textView);
                }
            }
        }

        return rootView;
    }
}
