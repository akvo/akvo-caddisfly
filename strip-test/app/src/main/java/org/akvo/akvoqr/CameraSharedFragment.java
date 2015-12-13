package org.akvo.akvoqr;

import android.app.Activity;
import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.util.Constant;

import java.util.Map;

/**
 * Created by linda on 11/25/15.
 * Contains methods that are shared by child classes
 * So the activity that inflates an instance of this fragment has access to them
 * in a simple way
 */
public abstract class CameraSharedFragment extends Fragment {

    protected StripTest stripTest;
    protected CameraViewListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        stripTest = new StripTest(activity);
    }

    public void setFocusAreas(Camera.Size previewSize){};

    public void countQuality(Map<String, Integer> countArray){};

    protected void showExposure(double value){};

    protected void showShadow(double value){};

    public void showStartButton(){};

    protected void setHeightOfOverlay(int shrinkOrEnlarge) {
        try {
            final RelativeLayout parentView = (RelativeLayout) getActivity().findViewById(R.id.activity_cameraMainRelativeLayout);

            final FrameLayout placeholderView = (FrameLayout) getActivity().findViewById(((View) getView().getParent()).getId());

            //find the overlay that hides part of the preview
            final RelativeLayout overlay = (RelativeLayout) getView().findViewById(R.id.overlay);
            final Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);

            final ViewGroup.LayoutParams paramsP = placeholderView.getLayoutParams();
            final ViewGroup.LayoutParams params = overlay.getLayoutParams();


            //shrinkOrEnlarge the overlay view based on a factor of its parent height
            switch (shrinkOrEnlarge) {
                case 0: //shrink
                    params.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERAVIEW_FACTOR);
                    paramsP.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERAVIEW_FACTOR);
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
                    placeholderView.startAnimation(slideUp);
                    overlay.startAnimation(slideUp);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
