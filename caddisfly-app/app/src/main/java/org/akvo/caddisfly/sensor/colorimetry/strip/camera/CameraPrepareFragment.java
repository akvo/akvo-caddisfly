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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.PercentageMeterView;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import timber.log.Timber;

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

    private static final int SHADOW_UNKNOWN_VALUE = 101;
    private final Handler handler = new Handler();
    @Nullable
    private CameraViewListener mListener;
    private WeakReference<PercentageMeterView> wrExposureView;
    private WeakReference<PercentageMeterView> wrContrastView;
    private long startTime;
    @Nullable
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();

            // if the minimum time has passed for quality checks and
            // if a quality check was not successful in the last few seconds then timeout
            if (currentTime - startTime > Constant.TIMEOUT_PREPARE
                    && currentTime - getLastQualityIncrementTime() > Constant.TIMEOUT_PREPARE_EXTEND) {
                if (mListener != null) {
                    mListener.stopPreview();
                    mListener.timeOut(getStatus());
                }
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @NonNull
    public static CameraPrepareFragment newInstance() {

        return new CameraPrepareFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_prepare, container, false);

        ButterKnife.bind(this, rootView);

        PercentageMeterView exposureView = rootView.findViewById(R.id.quality_brightness);
        PercentageMeterView contrastView = rootView.findViewById(R.id.quality_shadows);

        wrExposureView = new WeakReference<>(exposureView);
        wrContrastView = new WeakReference<>(contrastView);

        //use brightness view as a button to switch on and off the flash
        if (AppPreferences.isDiagnosticMode() && exposureView != null) {
            exposureView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.toggleFlashMode(true);
                }
            });
        }
        return rootView;
    }

    @Override
    public void showBrightness(double value) {

        if (wrExposureView != null) {
            PercentageMeterView meterView = wrExposureView.get();
            if (meterView != null) {
                meterView.setPercentage((float) (100 - value));
            }
        }
    }

    @Override
    public void showShadow(double value) {

        if (wrContrastView != null) {
            PercentageMeterView meterView = wrContrastView.get();
            if (meterView != null) {
                meterView.setPercentage((float) value);
            }
        }
    }

    @Override
    public void goNext() {

        handler.removeCallbacks(runnable);

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

            startTime = System.currentTimeMillis();
            handler.postDelayed(runnable, 1000);

        } catch (ClassCastException e) {
            throw new IllegalArgumentException(activity.toString()
                    + " must implement CameraViewListener", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handler.removeCallbacks(runnable);
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            if (mListener != null) {
                mListener.startNextPreview();
            }

            showBrightness(-1);
            showShadow(SHADOW_UNKNOWN_VALUE);

        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
