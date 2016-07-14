package org.akvo.caddisfly.sensor.colorimetry.strip.camera_strip;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;


/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraInstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p>
 * This fragment shows instructions for a particular strip test. They are read from strips.json in assets
 */
public class CameraInstructionFragment extends CameraSharedFragmentAbstract {

    private CameraViewListener mListener;

    public CameraInstructionFragment() {
        // Required empty public constructor
    }

    public static CameraInstructionFragment newInstance(String brandName) {
        CameraInstructionFragment fragment = new CameraInstructionFragment();
        Bundle args = new Bundle();
        args.putString(Constant.BRAND, brandName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_instructions, container, false);
        Button startButton = (Button) rootView.findViewById(R.id.activity_cameraStartButton);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_camera_instructionsLinearLayout);

        if (getArguments() != null) {

            String brandName = getArguments().getString(Constant.BRAND);

            StripTest stripTest = new StripTest();
            JSONArray instructions = stripTest.getBrand(brandName).getInstructions();
            TextView textView;
            try {
                for (int i = 0; i < instructions.length(); i++) {

                    String instr = instructions.getJSONObject(i).getString("text");

                    String[] instrArray = instr.split("<!");

                    for (String anInstrArray : instrArray) {
                        textView = new TextView(getActivity());
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mediumTextSize));

                        int padBottom = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
                        textView.setPadding(0, 0, 0, padBottom);

                        int indexImp = anInstrArray.indexOf(">");
                        if (indexImp >= 0) {
                            textView.setTextColor(Color.RED);
                        } else {
                            textView.setTextColor(Color.GRAY);
                        }
                        String text = anInstrArray.replaceAll(">", "");
                        if (!text.isEmpty()) {
                            textView.setText(" - ");
                            textView.append(text);
                            linearLayout.addView(textView);
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.nextFragment();
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CameraViewListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        final FrameLayout parentView = (FrameLayout) getActivity().findViewById(((View) getView().getParent()).getId());
//        ViewGroup.LayoutParams params = parentView.getLayoutParams();
//        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//        parentView.setLayoutParams(params);
//
//    }


}
