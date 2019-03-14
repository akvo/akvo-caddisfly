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

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseFragment;

import androidx.annotation.NonNull;

public class ResultPhotoFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private OnSubmitResultListener mListener;

    /**
     * Get the instance.
     */
    public static ResultPhotoFragment newInstance(TestInfo testInfo) {
        ResultPhotoFragment fragment = new ResultPhotoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, testInfo);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result_photo, container, false);

        if (getArguments() != null) {

            TestInfo testInfo = getArguments().getParcelable(ARG_PARAM1);
            if (testInfo != null) {

//                final Button buttonSubmitResult = view.findViewById(R.id.buttonSubmitResult);
//
//                buttonSubmitResult.setOnClickListener(view1 -> {
//                    if (mListener != null) {
//
//                    }
//                });
            }
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSubmitResultListener) {
            mListener = (OnSubmitResultListener) context;
        } else {
//            throw new IllegalArgumentException(context.toString()
//                    + " must implement OnSubmitResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSubmitResultListener {
        void onSubmitResult(String key);
    }
}
