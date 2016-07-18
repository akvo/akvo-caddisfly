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

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

import java.util.Map;

/**
 * Created by linda on 11/25/15.
 * Contains methods that are shared by child classes
 * So the activity that inflates an instance of this fragment has access to them
 * in a simple way
 */
@SuppressWarnings("deprecation")
public abstract class CameraSharedFragmentAbstract extends Fragment {

    //public abstract void setFocusAreas(Camera.Size previewSize);

    public void countQuality(Map<String, Integer> countArray) {
    }

    void showBrightness(double value) {
    }

    void showShadow(double value) {
    }

    public void showStartButton() {
    }

    @SuppressWarnings("SameParameterValue")
    void setHeightOfOverlay(int shrinkOrEnlarge) {
        try {
            final RelativeLayout parentView = (RelativeLayout) getActivity().findViewById(R.id.activity_cameraMainRelativeLayout);

            if (getView() != null) {
                final FrameLayout placeholderView = (FrameLayout) getActivity().findViewById(((View) getView().getParent()).getId());

                //find the overlay that hides part of the preview
                final RelativeLayout overlay = (RelativeLayout) getView().findViewById(R.id.overlay);
                //final Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);

                final ViewGroup.LayoutParams paramsP = placeholderView.getLayoutParams();
                final ViewGroup.LayoutParams params = overlay.getLayoutParams();

                //shrinkOrEnlarge the overlay view based on a factor of its parent height
                switch (shrinkOrEnlarge) {
                    case 0: //shrink
                        params.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERA_VIEW_FACTOR);
                        paramsP.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERA_VIEW_FACTOR);
                        break;
                    case 1: //enlarge
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        paramsP.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        break;
                }

                overlay.post(new Runnable() {

                    @Override
                    public void run() {

                        placeholderView.setLayoutParams(paramsP);
                        overlay.setLayoutParams(params);

                        //make view slide up
                        //placeholderView.startAnimation(slideUp);
                        //overlay.startAnimation(slideUp);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
