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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.akvo.caddisfly.R;
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

    private CameraViewListener mListener;
    private WeakReference<PercentageMeterView> wrExposureView;
    private WeakReference<PercentageMeterView> wrContrastView;

    public CameraPrepareFragment() {
        // Required empty public constructor
    }

    public static CameraPrepareFragment newInstance() {

        return new CameraPrepareFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_prepare, container, false);

        PercentageMeterView exposureView = (PercentageMeterView) rootView.findViewById(R.id.quality_brightness);
        PercentageMeterView contrastView = (PercentageMeterView) rootView.findViewById(R.id.quality_shadows);
        countQualityView = (TextView) rootView.findViewById(R.id.textMessage);

        wrExposureView = new WeakReference<>(exposureView);
        wrContrastView = new WeakReference<>(contrastView);

        //use brightness view as a button to switch on and off the flash
        if (exposureView != null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.toggleFlashMode(true);
                }
            });
        }
        return rootView;
    }

    @Override
    protected void showBrightness(double value) {

        if (wrExposureView != null) {
            wrExposureView.get().setPercentage((float) (100 - value));
        }
    }

    @Override
    protected void showShadow(double value) {

        if (wrContrastView != null) {
            wrContrastView.get().setPercentage((float) value);
        }
    }

    @Override
    public void goNext() {

        if (mListener != null) {
            if (mListener.isTorchModeOn()) {
                mListener.toggleFlashMode(false);
            }

            mListener.stopPreview();

            mListener.nextFragment();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CameraViewListener) activity;
            mListener.setQualityCheckCountZero();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CameraViewListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            setHeightOfOverlay(0);

            if (mListener != null) {
                mListener.startNextPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
