package org.akvo.akvoqr.camera_strip;

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

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.ui.QualityCheckView;
import org.akvo.akvoqr.util.Constant;

import java.lang.ref.WeakReference;
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
@SuppressWarnings("deprecation")
public class CameraPrepareFragment extends CameraSharedFragmentAbstract {

    private CameraViewListener mListener;
    private Button startButton;
    private TextView messageView;
    private WeakReference<TextView> wrCountQualityView;
    private WeakReference<QualityCheckView> wrExposureView;
    private WeakReference<QualityCheckView> wrContrastView;

    public static CameraPrepareFragment newInstance() {

        return new CameraPrepareFragment();
    }

    public CameraPrepareFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_prepare, container, false);

        QualityCheckView exposureView = (QualityCheckView) rootView.findViewById(R.id.activity_cameraImageViewExposure);
        QualityCheckView contrastView = (QualityCheckView) rootView.findViewById(R.id.activity_cameraImageViewContrast);
        startButton = (Button) rootView.findViewById(R.id.activity_cameraStartButton);
        startButton.setVisibility(View.GONE);
        messageView = (TextView) rootView.findViewById(R.id.activity_cameraPrepareTextView);
        TextView countQualityView = (TextView) rootView.findViewById(R.id.activity_cameraPrepareCountQualityView);

        wrCountQualityView = new WeakReference<TextView>(countQualityView);
        wrExposureView = new WeakReference<QualityCheckView>(exposureView);
        wrContrastView = new WeakReference<QualityCheckView>(contrastView);

        //use brightness view as a button to switch on and off the flash
        if(exposureView !=null) {
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

        if(wrExposureView!=null)
            wrExposureView.get().setPercentage((float) value);
    }

    @Override
    protected void showShadow(double value) {

        if(wrContrastView!=null)
            wrContrastView.get().setPercentage((float) value);
    }

    @Override
    public void showStartButton() {

        if(startButton==null)
            return;

        if(startButton.getVisibility()==View.GONE) {
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
    }

    @Override
    public void onPause()
    {
        super.onPause();
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
        if(wrCountQualityView!=null)
        {
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

                    if (1 == 1) {
                    }
                // next part is only for develop purposes. It shows the count per quality parameter
                  wrCountQualityView.get().append("\n\n");
                  for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                    wrCountQualityView.get().append(entry.getKey() + ": " + entry.getValue() + " ");
                  }
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
                    -1000 + ratioW,
                    -1000 + ratioH,
                    -1000 + ratioW * (int) (previewSize.width * Constant.CROP_CAMERAVIEW_FACTOR),
                    -1000 + ratioH * previewSize.height
            );

            areas.add(new Camera.Area(focusArea, 1));

            if(mListener!=null)
                mListener.setFocusAreas(areas);

        }
    }
}
