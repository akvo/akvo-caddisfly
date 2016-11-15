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

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.PercentageMeterView;

import java.lang.ref.WeakReference;

/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraPrepareFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * This fragment is used to show the quality checks done in CameraPreviewCallback
 */
@SuppressWarnings("deprecation")
public class CameraPrepareFragment extends CameraSharedFragmentBase {

    private static final String TAG = "CameraPrepareFragment";
    private CountDownTimer countDownTimer;
    @Nullable
    private CameraViewListener mListener;
    private WeakReference<PercentageMeterView> wrExposureView;
    private WeakReference<PercentageMeterView> wrContrastView;

    public CameraPrepareFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static CameraPrepareFragment newInstance() {

        return new CameraPrepareFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_prepare, container, false);

        PercentageMeterView exposureView = (PercentageMeterView) rootView.findViewById(R.id.quality_brightness);
        PercentageMeterView contrastView = (PercentageMeterView) rootView.findViewById(R.id.quality_shadows);
        setCountQualityView((TextView) rootView.findViewById(R.id.textMessage));

        wrExposureView = new WeakReference<>(exposureView);
        wrContrastView = new WeakReference<>(contrastView);

        //use brightness view as a button to switch on and off the flash
        if (exposureView != null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.toggleFlashMode(true);
                    }
                }
            });
        }
        return rootView;
    }

    @Override
    protected void showBrightness(double value) {

        if (wrExposureView != null) {
            PercentageMeterView meterView = wrExposureView.get();
            if (meterView != null) {
                meterView.setPercentage((float) (100 - value));
            }
        }
    }

    @Override
    protected void showShadow(double value) {

        if (wrContrastView != null) {
            PercentageMeterView meterView = wrContrastView.get();
            if (meterView != null) {
                meterView.setPercentage((float) value);
            }
        }
    }

    @Override
    public void goNext() {

        countDownTimer.cancel();

        if (mListener != null) {
            if (mListener.isTorchModeOn()) {
                mListener.toggleFlashMode(false);
            }

            mListener.stopPreview();

            mListener.nextFragment();
        }
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CameraViewListener) activity;
            mListener.setQualityCheckCountZero();

            final long startTime = System.currentTimeMillis();

            // timer to stop quality checks if it is taking too long
            countDownTimer = new CountDownTimer(Constant.TIMEOUT_PREPARE_MAX, 1000) {

                @Override
                public void onTick(long l) {

                    long currentTime = System.currentTimeMillis();

                    // if the minimum time has passed for quality checks...
                    if (currentTime - startTime > Constant.TIMEOUT_PREPARE_MIN) {

                        // if a quality check was not successful in the last few seconds then timeout
                        if (currentTime - getLastQualityIncrementTime() > Constant.TIMEOUT_PREPARE_EXTEND) {
                            this.cancel();
                            mListener.stopPreview();
                            mListener.timeOut();
                        }
                    }
                }

                public void onFinish() {

                    // timeout because quality checks are taking too long
                    if (getActivity() != null && mListener != null) {
                        mListener.stopPreview();
                        mListener.timeOut();
                    }
                }
            }.start();


        } catch (ClassCastException e) {
            throw new IllegalArgumentException(activity.toString()
                    + " must implement CameraViewListener", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        countDownTimer.cancel();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            if (mListener != null) {
                mListener.startNextPreview();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
