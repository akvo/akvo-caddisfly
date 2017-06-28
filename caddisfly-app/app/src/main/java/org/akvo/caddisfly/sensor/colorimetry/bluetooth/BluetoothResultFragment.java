package org.akvo.caddisfly.sensor.colorimetry.bluetooth;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothResultFragment extends Fragment {

    private static final int ANIMATION_DURATION = 400;
    private final SparseArray<String> results = new SparseArray<>();
    private String mResult;
    private TextView textResult;
    private TextView textUnit;
    private LinearLayout layoutWaiting;
    private LinearLayout layoutResult;
    private OnFragmentInteractionListener mListener;
    private TextView textName;
    private AlertDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bluetooth_result, container, false);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        Button buttonInstructions = (Button) view.findViewById(R.id.button_instructions);
        buttonInstructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(0);
                }
            }
        });

        if (testInfo.getInstructions() == null || testInfo.getInstructions().length() < 1) {
            buttonInstructions.setVisibility(View.GONE);
        }

        textResult = (TextView) view.findViewById(R.id.textResult);
        textUnit = (TextView) view.findViewById(R.id.textUnit);
        textName = (TextView) view.findViewById(R.id.textName);
        TextView textPerformTest = (TextView) view.findViewById(R.id.textPerformTest);

        layoutWaiting = (LinearLayout) view.findViewById(R.id.layoutWaiting);
        layoutResult = (LinearLayout) view.findViewById(R.id.layoutResult);

        Button mAcceptButton = (Button) view.findViewById(R.id.button_accept_result);

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Build the result json to be returned
                TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

                Intent resultIntent = new Intent(getActivity().getIntent());

                results.clear();

                results.put(1, String.valueOf(mResult));

                JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, "", null);
                resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();

            }
        });

        textPerformTest.setText(StringUtil.toInstruction(getActivity(), String.format(getString(R.string.perform_test),
                testInfo.getName())));

        return view;
    }

    private AlertDialog showError(Activity activity) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(R.string.incorrect_test_selected);

        alertDialog.setMessage(TextUtils.concat(
                StringUtil.toInstruction(getActivity(), getString(R.string.data_does_not_match) + "<br /><br />"),
                StringUtil.toInstruction(getActivity(), getString(R.string.select_correct_test))
        ));

        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setCancelable(false);
        dialog = alertDialog.create();
        dialog.show();
        return dialog;
    }

    public void displayWaiting() {

        layoutResult.setVisibility(View.GONE);
        layoutWaiting.setVisibility(View.VISIBLE);
        layoutWaiting.setAlpha(1f);

    }

    public boolean displayData(String data) {

        if (dialog != null) {
            dialog.dismiss();
        }

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        String resultTitles = ",,,,Id,Test,Range,,,,Date,Time,,,,,,,Result,Unit";
        String[] titles = resultTitles.split(",");
        String testId = "";
        String[] ranges = null;

        String[] result = data.split(";");
        for (int i = 0; i < result.length; i++) {
            if (titles.length > i && !titles[i].isEmpty()) {
                if (titles[i].equals("Id")) {
                    testId = result[i].trim();
                }
                if (titles[i].equals("Range")) {
                    ranges = result[i].substring(0, result[i].indexOf(" ")).trim().split("-");
                }

                if (titles[i].equals("Result")) {
                    mResult = result[i].trim();

                    boolean isText = false;
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        Double.parseDouble(mResult);
                    } catch (Exception e) {
                        isText = true;
                    }

                    if (isText) {
                        if (ranges != null && ranges.length > 1) {
                            if (mResult.equalsIgnoreCase("underrange")) {
                                mResult = "<" + ranges[0];
                            } else if (mResult.equalsIgnoreCase("overrange")) {
                                mResult = ">" + ranges[1];
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    textResult.setText(mResult);
                }
            }
        }

        textName.setText(testInfo.getName());
        textUnit.setText(testInfo.getUnit());

        if (testId.equals(CaddisflyApp.getApp().getCurrentTestInfo().getTintometerId())) {
            crossFade();
            return true;
        } else {

            showError(getActivity());
            layoutResult.setVisibility(View.GONE);
            layoutWaiting.setVisibility(View.VISIBLE);
            layoutWaiting.setAlpha(1);

            if (mListener != null) {
                mListener.onFragmentInteraction(1);
            }

            return false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void crossFade() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        layoutResult.setAlpha(0f);
        layoutResult.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        layoutResult.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        layoutWaiting.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        layoutWaiting.setVisibility(View.GONE);
                    }
                });
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
        void onFragmentInteraction(int mode);
    }

}
