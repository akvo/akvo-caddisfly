package org.akvo.akvoqr;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.akvoqr.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;


/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraInstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * This fragment shows instructions for a particular strip test. They are read from strips.json in assets
 */
public class CameraInstructionFragment extends CameraSharedFragment {

    private CameraViewListener mListener;
    private Button startButton;
    private String brandName;

    public static CameraInstructionFragment newInstance(String brandName) {
        CameraInstructionFragment fragment = new CameraInstructionFragment();
        Bundle args = new Bundle();
        args.putString(Constant.BRAND, brandName);
        fragment.setArguments(args);
        return fragment;
    }

    public CameraInstructionFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_camera_instructions, container, false);
        startButton = (Button) rootView.findViewById(R.id.activity_cameraStartButton);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_camera_instructionsLinearLayout);

        if(getArguments()!=null) {

            brandName = getArguments().getString(Constant.BRAND);


            JSONArray instructions = stripTest.getBrand(brandName).getInstructions();
            TextView textView;
            try {
                for (int i = 0; i < instructions.length(); i++) {

                    String instr = instructions.getJSONObject(i).getString("text");

                    String[] instrArray = instr.split("<!");

                    for(int ii=0; ii < instrArray.length;ii++) {
                        textView = new TextView(getActivity());
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.mediumTextSize));

                        int padBottom = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
                        textView.setPadding(0,0,0, padBottom);
                        textView.setText(" - ");

                        int indexImp = instrArray[ii].indexOf(">");
                        if(indexImp >= 0)
                        {
                            textView.setTextColor(Color.RED);
                        }
                        else
                        {
                            textView.setTextColor(Color.GRAY);
                        }
                        String text = instrArray[ii].replaceAll(">", "");
                        textView.append(text);
                        linearLayout.addView(textView);

                    }

                }
            }
            catch (JSONException e)
            {
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

        final FrameLayout parentView = (FrameLayout) getActivity().findViewById(((View) getView().getParent()).getId());
        ViewGroup.LayoutParams params = parentView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        parentView.setLayoutParams(params);

    }


}
