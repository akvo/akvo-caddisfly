/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;

import at.markushi.ui.CircleButton;

@SuppressWarnings("WeakerAccess")
public class StartFragment extends Fragment {

    private static final String EXTERNAL_PARAM = "external";

    private static final String TEST_TYPE_PARAM = "testType";

    private OnStartSurveyListener mOnStartSurveyListener;
    private OnStartTestListener mOnStartTestListener;
    private OnVideoListener mOnVideoListener;
    private OnBackListener mOnBackListener;

    // private boolean mIsExternal = false;
    private int mTestType;
    private boolean mIsExternal;

    public static StartFragment newInstance(boolean external, int testType) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTERNAL_PARAM, external);
        args.putInt(TEST_TYPE_PARAM, testType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        getActivity().setTitle(R.string.appName);

        MainApp mainApp = (MainApp) getActivity().getApplicationContext();

        if (getArguments() != null) {
            mIsExternal = getArguments().getBoolean(EXTERNAL_PARAM);
            mTestType = getArguments().getInt(TEST_TYPE_PARAM);
        }


        final CircleButton videoButton = (CircleButton) view.findViewById(R.id.videoButton);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnVideoListener != null) {
                    mOnVideoListener.onVideo();
                }
            }
        });

        Button backButton = (Button) view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnBackListener != null) {
                    mOnBackListener.onBack();
                }
            }
        });

        Button startSurveyButton = (Button) view.findViewById(R.id.surveyButton);
        startSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnStartSurveyListener != null) {
                    mOnStartSurveyListener.onStartSurvey();
                }
            }
        });

        Button startButton = (Button) view.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnStartTestListener != null) {
                    mOnStartTestListener.onStartTest();
                }
            }
        });

        TextView testTypeTextView = (TextView) view.findViewById(R.id.testTypeTextView);

        if (mIsExternal) {
            testTypeTextView.setText(mainApp.currentTestInfo.getName());
            testTypeTextView.setVisibility(View.VISIBLE);

            backButton.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.VISIBLE);
            startSurveyButton.setVisibility(View.GONE);
        } else {
            testTypeTextView.setVisibility(View.GONE);
            startSurveyButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnStartTestListener = (OnStartTestListener) activity;
            mOnStartSurveyListener = (OnStartSurveyListener) activity;
            mOnVideoListener = (OnVideoListener) activity;
            mOnBackListener = (OnBackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnStartSurveyListener = null;
        mOnStartTestListener = null;
        mOnVideoListener = null;
        mOnBackListener = null;
    }


    public interface OnStartSurveyListener {
        public void onStartSurvey();
    }

    public interface OnStartTestListener {
        public void onStartTest();
    }

    public interface OnVideoListener {
        public void onVideo();
    }

    public interface OnBackListener {
        public void onBack();
    }
}
