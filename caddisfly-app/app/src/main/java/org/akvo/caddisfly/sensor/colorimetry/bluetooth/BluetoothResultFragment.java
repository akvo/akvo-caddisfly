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

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothResultFragment extends Fragment {

    private static final int ANIMATION_DURATION = 400;
    private final SparseArray<String> results = new SparseArray<>();
    private AlertDialog alertDialog;
    private String mResult;
    private TextView textResult;
    private TextView textUnit;
    private LinearLayout layoutWaiting;
    private LinearLayout layoutResult;
    private OnFragmentInteractionListener mListener;
    private TextView textName;

    public BluetoothResultFragment() {
        // Required empty public constructor
    }

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
                    mListener.onFragmentInteraction();
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

        textPerformTest.setText(StringUtil.fromHtml(String.format(getString(R.string.perform_test),
                testInfo.getName(), testInfo.getTintometerId())));

        alertDialog = showInstructionDialog(getActivity());

        return view;
    }

    private AlertDialog showInstructionDialog(Activity activity) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(R.string.selectTest);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        alertDialog.setMessage(TextUtils.concat(
                StringUtil.fromHtml(String.format(getString(R.string.select_test_instruction),
                        testInfo.getTintometerId(), testInfo.getName()))
        ));

        alertDialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setCancelable(false);
        AlertDialog dialog = alertDialog.create();
        dialog.show();
        return dialog;
    }

    private AlertDialog showError(Activity activity) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(R.string.incorrect_test_selected);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        alertDialog.setMessage(TextUtils.concat(
                StringUtil.fromHtml(getString(R.string.data_does_not_match) + "<br /><br />"),
                StringUtil.fromHtml(getString(R.string.select_correct_test) + "<br /><br />"),
                StringUtil.fromHtml(String.format(getString(R.string.select_test_instruction),
                        testInfo.getTintometerId(), testInfo.getName()))
        ));

        alertDialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setCancelable(false);
        AlertDialog dialog = alertDialog.create();
        dialog.show();
        return dialog;
    }

    public boolean displayData(String data) {

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

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
            }
        }

        textName.setText(testInfo.getName());
        textUnit.setText(testInfo.getUnit());

        alertDialog.dismiss();

        if (testId.equals(CaddisflyApp.getApp().getCurrentTestInfo().getTintometerId())) {
            crossFade();
            return true;
        } else {

            showError(getActivity());
            layoutResult.setVisibility(View.GONE);
            layoutWaiting.setVisibility(View.VISIBLE);
            return false;
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
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }


}

