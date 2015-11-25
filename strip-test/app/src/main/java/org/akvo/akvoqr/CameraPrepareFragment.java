package org.akvo.akvoqr;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.akvo.akvoqr.ui.QualityCheckView;
import org.akvo.akvoqr.util.Constant;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraPrepareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraPrepareFragment extends Fragment {

    private CameraViewListener mListener;
    private Button startButton;
    private TextView messageView;

    public static CameraPrepareFragment newInstance() {
        CameraPrepareFragment fragment = new CameraPrepareFragment();

        return fragment;
    }

    public CameraPrepareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_prepare, container, false);
        startButton = (Button) rootView.findViewById(R.id.activity_cameraStartButton);
        messageView = (TextView) rootView.findViewById(R.id.activity_cameraInstructionsTextView);

        //use brightness view as a button to switch on and off the flash
        QualityCheckView exposureView = (QualityCheckView) rootView.findViewById(R.id.activity_cameraImageViewExposure);
        if(exposureView!=null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mListener.switchFlash();
                }
            });
        }
        return rootView;
    }

    public void showStartButton() {

        if(startButton==null)
            return;

        startButton.setVisibility(View.VISIBLE);
        //startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checked_box, 0, 0, 0);
        startButton.setBackgroundResource(android.R.drawable.btn_default);
        startButton.setText(R.string.next);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.nextFragment();
            }
        });

        if(messageView!=null)
        {
            messageView.setTextSize(getResources().getDimension(R.dimen.mediumTextSize));
            messageView.setText("Excellent! \nPlease go to the next step.");
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
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        setHeightOfOverlay(0);

        if(mListener!=null)
        {
            mListener.startNextPreview(0);
        }
    }

    private void setHeightOfOverlay(int shrinkOrEnlarge)
    {
        final RelativeLayout parentView = (RelativeLayout) getActivity().findViewById(R.id.activity_cameraMainRelativeLayout);

        final FrameLayout placeholderView = (FrameLayout) getActivity().findViewById(((View) getView().getParent()).getId());

        //find the overlay that hides part of the preview
        final RelativeLayout overlay = (RelativeLayout) getView().findViewById(R.id.overlay);
        final Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);

        System.out.println("***parentView.getHeight() " + parentView.getHeight());

        final ViewGroup.LayoutParams paramsP = placeholderView.getLayoutParams();
        final ViewGroup.LayoutParams params = overlay.getLayoutParams();

        //shrinkOrEnlarge the overlay view based on a factor of its parent height
        switch (shrinkOrEnlarge)
        {
            case 0: //shrink
                params.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERAVIEW_FACTOR);
                paramsP.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERAVIEW_FACTOR);
                break;
            case 1: //enlarge
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                paramsP.height = ViewGroup.LayoutParams.MATCH_PARENT;
                break;
        }

        try {

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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private int iter = 0;
    public void countQuality(int count)
    {
        if(messageView!=null)
        {
            try {
                iter++;
                messageView.setText("\n\n" + String.valueOf(iter) + " : " + String.valueOf(count));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
