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

package org.akvo.caddisfly.sensor.colorimetry.strip.instructions;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;

public class InstructionDetailFragment extends Fragment {
    /**
     * The fragment arguments for the contents to be displayed
     */
    private static final String ARG_ITEM_TEXT = "text";
    private static final String ARG_ITEM_IMAGE = "image";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InstructionDetailFragment() {
    }

    public static InstructionDetailFragment newInstance(String text, String imageName) {
        InstructionDetailFragment fragment = new InstructionDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_TEXT, text);
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
            ImageView imageView = (ImageView) rootView.findViewById(R.id.image_illustration);
            imageView.setImageDrawable(instructionDrawable);
        }

        String instructionText = getArguments().getString(ARG_ITEM_TEXT);
        if (instructionText != null) {
            TextView textView = ((TextView) rootView.findViewById(R.id.text_instruction));
            textView.setText(instructionText);
        }

        return rootView;
    }
}
