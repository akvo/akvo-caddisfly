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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.SoundPoolPlayer;


/**
 * A fragment representing a single Calibrate detail screen.
 * This fragment is either contained in a {@link CalibrateListActivity}
 * in two-pane mode (on tablets) or a {@link CalibrateDetailActivity}
 * on handsets.
 */
public class CalibrateDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    ResultRange mRange;
    private PowerManager.WakeLock wakeLock;
    private SoundPoolPlayer sound;
    private TextView mValueTextView;
    private Button mColorButton;
    private LinearLayout mErrorLayout;
    private TextView mErrorTextView;
    private TextView mErrorSummaryTextView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CalibrateDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sound = new SoundPoolPlayer(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_calibrate_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mColorButton = (Button) view.findViewById(R.id.colorButton);
        mValueTextView = (TextView) view.findViewById(R.id.valueTextView);
        Button startButton = (Button) view.findViewById(R.id.startButton);

        mErrorLayout = (LinearLayout) view.findViewById(R.id.errorLayout);
        mErrorTextView = (TextView) view.findViewById(R.id.errorTextView);
        mErrorSummaryTextView = (TextView) view.findViewById(R.id.errorSummaryTextView);

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                AlertUtils.askQuestion(getActivity(), R.string.calibrate,
                        R.string.startTestConfirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface,
                                    int i) {
                                calibrate(position);
                            }
                        }
                );
            }
        });
        displayInfo();
    }

    private void calibrate(int position) {
        //PreferencesUtils.setInt(this, R.string.currentSamplingCountKey, 0);

        //deleteCalibration(position);
        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getActivity()
                    .getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }
        startCalibration();
    }

    public void startCalibration() {

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            final int position = getArguments().getInt(ARG_ITEM_ID);
            final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());
            mRange = mainApp.currentTestInfo.getRange(position);

            final Intent intent = new Intent();
            intent.setClass(getActivity(), CameraSensorActivity.class);
            intent.putExtra("isCalibration", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, 200);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());
        if (requestCode == 200 && data != null) {

            if (mRange == null) {
                final int position = getArguments().getInt(ARG_ITEM_ID);
                mRange = mainApp.currentTestInfo.getRange(position);
            }

            if (resultCode == Activity.RESULT_OK) {
                mainApp.storeCalibratedData(mRange, data.getIntExtra("color", -1));
                displayInfo();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            } else {
                mainApp.storeCalibratedData(mRange, -1);
                displayInfo();
            }
        }
    }

    void displayInfo() {

        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());
        //mainApp.setSwatches(mainApp.currentTestType);
        final int position = getArguments().getInt(ARG_ITEM_ID);
        int color = mainApp.currentTestInfo.getRanges().get(position).getColor();

//        int error = mainApp.colorList.get(position).getErrorCode();
//        if (error > 0) {
//            mErrorLayout.setVisibility(View.VISIBLE);
//            if (error == Config.ERROR_NOT_YET_CALIBRATED) {
//                mErrorSummaryTextView.setVisibility(View.GONE);
//                mErrorTextView.setVisibility(View.GONE);
//            } else {
//                mErrorSummaryTextView.setVisibility(View.VISIBLE);
//                mErrorTextView.setVisibility(View.VISIBLE);
//            }
//            mErrorTextView.setText(DataHelper.getSwatchError(getActivity(), error));
//        } else {
//            mErrorLayout.setVisibility(View.GONE);
//        }

        if (color != -1) {
            mColorButton.setBackgroundColor(color);
            mColorButton.setText("");
        } else {
            mColorButton.setBackgroundColor(Color.argb(0, 10, 10, 10));
            mColorButton.setText("?");
        }

        mValueTextView.setText(mainApp.doubleFormat.format(mainApp.currentTestInfo.getRanges().get(position).getValue()));
    }

    private void releaseResources() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseResources();
    }

}
