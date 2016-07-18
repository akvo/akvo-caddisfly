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
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.QualityCheckView;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

import java.lang.ref.WeakReference;
import java.util.Map;


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
public class CameraPrepareFragment extends CameraSharedFragmentAbstract {

    private CameraViewListener mListener;
    private TextView buttonNext;
    private TextView messageView;
    private WeakReference<TextView> wrCountQualityView;
    private WeakReference<QualityCheckView> wrExposureView;
    private WeakReference<QualityCheckView> wrContrastView;

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

        QualityCheckView exposureView = (QualityCheckView) rootView.findViewById(R.id.quality_brightness);
        QualityCheckView contrastView = (QualityCheckView) rootView.findViewById(R.id.quality_shadows);
        buttonNext = (TextView) rootView.findViewById(R.id.button_next);
        buttonNext.setVisibility(View.INVISIBLE);
        messageView = (TextView) rootView.findViewById(R.id.text_prepareInfo);
        TextView countQualityView = (TextView) rootView.findViewById(R.id.text_qualityCount);

        wrCountQualityView = new WeakReference<>(countQualityView);
        wrExposureView = new WeakReference<>(exposureView);
        wrContrastView = new WeakReference<>(contrastView);

        //use brightness view as a button to switch on and off the flash
        if (exposureView != null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mListener.switchFlash();
                }
            });
        }
        return rootView;
    }

    @Override
    protected void showBrightness(double value) {

        if (wrExposureView != null)
            wrExposureView.get().setPercentage((float) value);
    }

    @Override
    protected void showShadow(double value) {

        if (wrContrastView != null)
            wrContrastView.get().setPercentage((float) value);
    }

    @Override
    public void showStartButton() {

        if (buttonNext == null)
            return;

        if (buttonNext.getVisibility() == View.INVISIBLE) {
            buttonNext.setVisibility(View.VISIBLE);
            buttonNext.setText(R.string.next);
            buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.nextFragment();
                }
            });

            if (messageView != null) {
                messageView.setText(R.string.confirm_quality_checks_ok);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CameraViewListener) activity;
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

    @Override
    public void countQuality(Map<String, Integer> countMap) {
        if (wrCountQualityView != null) {
            try {
                int count = 0;

                // each parameter counts for 1/3 towards the final count shown.
                for (int i : countMap.values()) {
                    count += Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT / countMap.size(), i);
                }

                count = Math.max(0, Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT, count));

                if (!wrCountQualityView.get().getText().toString().contains("15 out of")) {
                    String text = getResources().getString(R.string.quality_checks_counter, count, Constant.COUNT_QUALITY_CHECK_LIMIT);
                    wrCountQualityView.get().setText(text);

                    // only for development purposes. It shows the count per quality parameter
                    if (AppPreferences.isDiagnosticMode()) {
                        wrCountQualityView.get().append("\n\n");
                        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                            wrCountQualityView.get().append(entry.getKey() + ": " + entry.getValue() + " ");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
