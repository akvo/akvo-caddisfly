package org.akvo.akvoqr;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.akvoqr.ui.QualityCheckView;
import org.akvo.akvoqr.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraPrepareFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * This fragment is used to show the quality checks done in CameraPreviewCallback
 */
public class CameraPrepareFragment extends CameraSharedFragment {

    private CameraViewListener mListener;
    private Button startButton;
    private TextView messageView;
    private TextView countQualityView;

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
        messageView = (TextView) rootView.findViewById(R.id.activity_cameraPrepareTextView);
        countQualityView = (TextView) rootView.findViewById(R.id.activity_cameraPrepareCountQualityView);

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

    @Override
    public void showStartButton() {

        if(startButton==null)
            return;

        startButton.setVisibility(View.VISIBLE);
        startButton.setBackgroundResource(android.R.drawable.btn_default);
        startButton.setBackgroundColor(getResources().getColor(R.color.springgreen));
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

        try {
            setHeightOfOverlay(0);

            if (mListener != null) {
                mListener.startNextPreview(0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void countQuality(Map<String, Integer> countMap)
    {

        if(countQualityView!=null)
        {
            try {

                int count = 0;

                for(int i: countMap.values()){
                    if(i > Constant.COUNT_QUALITY_CHECK_LIMIT / countMap.size())
                        count += Constant.COUNT_QUALITY_CHECK_LIMIT / countMap.size();
                }

                count = Math.max(0, Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT, count));
                countQualityView.setText("Quality checks: " + String.valueOf(count) + " out of " + Constant.COUNT_QUALITY_CHECK_LIMIT);

                countQualityView.append("\n\n");
                for(Map.Entry<String, Integer> entry: countMap.entrySet()) {
                    countQualityView.append(entry.getKey() + ": " + entry.getValue() + " " );
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setFocusAreas(Camera.Size previewSize) {
        //set focus area to upper third of preview
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {


            List<Camera.Area> areas = new ArrayList<>();

            int ratioW = Math.round(1000f / previewSize.width);
            int ratioH = Math.round(1000f / previewSize.height);

            Rect focusArea = new Rect(
                    -1000 + ratioW * (int) 1,
                    -1000 + ratioH * (int) 1,
                    -1000 + ratioW * (int) (previewSize.width * Constant.CROP_CAMERAVIEW_FACTOR),
                    -1000 + ratioH * (int) (previewSize.height )
            );

            areas.add(new Camera.Area(focusArea, 1));

            if(mListener!=null)
                mListener.setFocusAreas(areas);

        }
    }
}
