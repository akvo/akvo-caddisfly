package org.akvo.caddisfly.sensor.colorimetry.bluetooth;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothResultFragment extends Fragment {


    private final SparseArray<String> results = new SparseArray<>();
    private String mResult;
    private TextView textResult;
    private TextView textUnit;
    private Button buttonOk;

    private LinearLayout layoutWaiting;
    private LinearLayout layoutResult;
    private LinearLayout layoutError;

    private OnFragmentInteractionListener mListener;


    public BluetoothResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bluetooth_result, container, false);

        Button instructionsButton = (Button) view.findViewById(R.id.button_instructions);
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onFragmentInteraction();
                }
            }
        });

        textResult = (TextView) view.findViewById(R.id.textResult);
        textUnit = (TextView) view.findViewById(R.id.textUnit);
        textUnit = (Button) view.findViewById(R.id.buttonOk);

        layoutWaiting = (LinearLayout) view.findViewById(R.id.layoutWaiting);
        layoutResult = (LinearLayout) view.findViewById(R.id.layoutResult);
        layoutError = (LinearLayout) view.findViewById(R.id.layoutError);

        Button mAcceptButton = (Button) view.findViewById(R.id.button_accept_result);

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Build the result json to be returned
                TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

                Intent resultIntent = new Intent(getActivity().getIntent());

                results.clear();

                try {
                    double result = Double.parseDouble(mResult);
                    results.put(1, String.valueOf(result));
                } catch (Exception e) {
                    Timber.e(e);
                    return;
                }

                JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, "", null);
                resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();

            }
        });

        return view;
    }

    public void displayData(String data) {

        String resultTitles = ",,,,Id,Test,,,,,Date,Time,,,,,,,Result,Unit";
        String[] titles = resultTitles.split(",");
        String testId = "";

        String[] result = data.split(";");
        for (int i = 0; i < result.length; i++) {
            if (titles.length > i && !titles[i].isEmpty()) {
                if (titles[i].equals("Id")) {
                    testId = result[i].trim();
                }
                if (titles[i].equals("Result")) {
                    mResult = result[i];
                    textResult.setText(result[i].trim());
                }
                if (titles[i].equals("Unit")) {
                    textUnit.setText(result[i].trim());
                }
            }
        }

        if (testId.equals(CaddisflyApp.getApp().getCurrentTestInfo().getTintometerId())) {
            layoutResult.setVisibility(View.VISIBLE);
            layoutWaiting.setVisibility(View.GONE);
        } else {
            layoutError.setVisibility(View.VISIBLE);
            layoutResult.setVisibility(View.GONE);
            layoutWaiting.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }


}

