package org.akvo.caddisfly.sensor.bluetooth;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothResultFragment extends Fragment {

    private static final int ANIMATION_DURATION = 400;
    private final SparseArray<String> results = new SparseArray<>();
    private TextView textName1;
    private TextView textResult1;
    private TextView textUnit1;

    private TextView textName2;
    private TextView textResult2;
    private TextView textUnit2;

    private TextView textName3;
    private TextView textResult3;
    private TextView textUnit3;

    private LinearLayout layoutWaiting;
    private LinearLayout layoutResult;
    private OnCurrentModeListener mListener;
    private AlertDialog errorDialog;
    private LinearLayout layoutResult1;
    private LinearLayout layoutResult2;
    private LinearLayout layoutResult3;
    private Button mAcceptButton;
    private TestInfo testInfo;

    /**
     * Creates test fragment for specific uuid.
     */
    public static BluetoothResultFragment getInstance(TestInfo testInfo) {
        BluetoothResultFragment fragment = new BluetoothResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bluetooth_result, container, false);

        testInfo = getArguments().getParcelable(ConstantKey.TEST_INFO);

        Button buttonInstructions = view.findViewById(R.id.button_instructions);
        buttonInstructions.setOnClickListener(view1 -> {
            if (mListener != null) {
                mListener.onCurrentMode(0);
            }
        });

        if (testInfo.getInstructions() == null || testInfo.getInstructions().size() < 1) {
            buttonInstructions.setVisibility(View.GONE);
        }

        layoutResult1 = view.findViewById(R.id.layoutResult1);
        textResult1 = view.findViewById(R.id.textResult1);
        textUnit1 = view.findViewById(R.id.textUnit1);
        textName1 = view.findViewById(R.id.textName1);

        layoutResult2 = view.findViewById(R.id.layoutResult2);
        textResult2 = view.findViewById(R.id.textResult2);
        textUnit2 = view.findViewById(R.id.textUnit2);
        textName2 = view.findViewById(R.id.textName2);

        layoutResult3 = view.findViewById(R.id.layoutResult3);
        textResult3 = view.findViewById(R.id.textResult3);
        textUnit3 = view.findViewById(R.id.textUnit3);
        textName3 = view.findViewById(R.id.textName3);

        layoutWaiting = view.findViewById(R.id.layoutWaiting);
        layoutResult = view.findViewById(R.id.layoutResult);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        if (AppPreferences.isTestMode()) {
            progressBar.setVisibility(View.GONE);
        }

        mAcceptButton = view.findViewById(R.id.button_accept_result);

        mAcceptButton.setOnClickListener(view12 -> {
            // Build the result json to be returned

            Intent resultIntent = new Intent();

            JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo,
                    results, null, -1, "");
            resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();

        });

        TextView textPerformTest = view.findViewById(R.id.textPerformTest);
        textPerformTest.setText(StringUtil.toInstruction((AppCompatActivity) getActivity(), testInfo,
                String.format(getString(R.string.perform_test), testInfo.getName())));

        if (AppPreferences.isDiagnosticMode()) {
            LinearLayout layoutTitle = view.findViewById(R.id.layoutTitleBar);
            if (layoutTitle != null) {
                layoutTitle.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.diagnostic));
            }
        }

        return view;
    }

    private void showError(Activity activity) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(R.string.incorrect_test_selected);

        alertDialog.setMessage(TextUtils.concat(
                StringUtil.toInstruction((AppCompatActivity) getActivity(),
                        null, getString(R.string.data_does_not_match) + "<br /><br />"),

                StringUtil.toInstruction((AppCompatActivity) getActivity(),
                        null, getString(R.string.select_correct_test))
        ));

        alertDialog.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.setCancelable(false);
        errorDialog = alertDialog.create();
        errorDialog.show();
    }

    /**
     * Display the waiting screen.
     */
    public void displayWaiting() {

        layoutResult.setVisibility(View.GONE);
        layoutWaiting.setVisibility(View.VISIBLE);
        layoutWaiting.setAlpha(1f);

    }

    /**
     * Display the result data.
     */
    public boolean displayData(String data) {

        if (errorDialog != null) {
            errorDialog.dismiss();
        }

        // Display data received for diagnostic purposes
        showDebugInfo(data);

        String resultTitles = ",,Version,Version,Id,Test,Range,,,,Date,Time,,,,,,,Result,Unit";
        String[] titles = resultTitles.split(",");
        String testId = "";
        String[] ranges = new String[2];
        String unit = "";
        boolean dataOk = false;

        String[] dataArray = data.split(";");
        for (int i = 0; i < dataArray.length; i++) {
            if (titles.length > i && !titles[i].isEmpty()) {
                if (titles[i].equals("Version")) {
                    String version = dataArray[i].trim();
                    if (version.startsWith("V")) {
                        dataOk = true;
                    } else {
                        return false;
                    }
                }
                if (titles[i].equals("Id")) {
                    testId = dataArray[i].trim();
                }
                if (titles[i].equals("Range")) {
                    Matcher m =
                            Pattern.compile("([-+]?[0-9]*\\.?[0-9]+)-([-+]?[0-9]*\\.?[0-9]+)\\s+(.+)\\s+(.+)")
                                    .matcher(dataArray[i].trim());
                    if (m.find()) {
                        ranges[0] = m.group(1);
                        ranges[1] = m.group(2);
                        ranges[0] = "null";
                        unit = m.group(3);
                        //String formula = m.group(4);
                    }
                }

                if (titles[i].equals("Result")) {

                    int resultCount = 0;
                    for (int j = 0; j + i < dataArray.length; j = j + 4) {

                        resultCount++;

                        String result = dataArray[j + i].trim();
                        String md610Id = dataArray[j + i + 1].trim().replace(unit, "").trim();

                        boolean isText = false;
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            Double.parseDouble(result);
                        } catch (Exception e) {
                            isText = true;
                        }

                        if (isText) {
                            if (ranges.length > 1) {
                                result = handleOutOfRangeResult(ranges, result);
                            } else {
                                return false;
                            }
                        }

                        showResults(result, md610Id);
                    }

                    if (results.size() < 1 || resultCount != testInfo.getResults().size()) {
                        dataOk = false;
                    }
                    break;
                }
            }
        }

        if (dataOk && testId.equals(testInfo.getMd610Id())) {
            mAcceptButton.setVisibility(View.VISIBLE);
            crossFade();
            return true;
        } else {

            showError(getActivity());
            layoutResult.setVisibility(View.GONE);
            layoutWaiting.setVisibility(View.VISIBLE);
            layoutWaiting.setAlpha(1);

            if (mListener != null) {
                mListener.onCurrentMode(1);
            }

            return false;
        }
    }

    private String handleOutOfRangeResult(String[] ranges, String data) {

        DecimalFormat df = new DecimalFormat("#.###");

        if (data.equalsIgnoreCase("underrange")) {
            try {
                //noinspection ResultOfMethodCallIgnored
                Double.parseDouble(ranges[0]);
            } catch (Exception e) {
                ranges[0] = df.format(testInfo.getMinRangeValue());
            }
            if (ranges[0].equals("0")) {
                return "0";
            } else {
                return "<" + ranges[0];
            }

        } else if (data.equalsIgnoreCase("overrange")) {
            try {
                //noinspection ResultOfMethodCallIgnored
                Double.parseDouble(ranges[1]);
            } catch (Exception e) {
                ranges[1] = df.format(testInfo.getMaxRangeValue());
            }
            return ">" + ranges[1];
        }
        return "";
    }

    private void showResults(String result, String md610Id) {
        if (testInfo.getResults().size() > 1) {

            for (Result subTest : testInfo.getResults()) {
                if (subTest.getMd610Id().equalsIgnoreCase(md610Id)) {

                    if (subTest.getId() == 1) {
                        layoutResult1.setVisibility(View.VISIBLE);
                        textName1.setText(subTest.getName());
                        textUnit1.setText(subTest.getUnit());
                        textResult1.setText(result);
                    } else if (subTest.getId() == 2) {
                        layoutResult2.setVisibility(View.VISIBLE);
                        textName2.setText(subTest.getName());
                        textUnit2.setText(subTest.getUnit());
                        textResult2.setText(result);
                    } else if (subTest.getId() == 3) {
                        layoutResult3.setVisibility(View.VISIBLE);
                        textName3.setText(subTest.getName());
                        textUnit3.setText(subTest.getUnit());
                        textResult3.setText(result);
                    }

                    results.put(subTest.getId(), result);
                }
            }
        } else {
            layoutResult1.setVisibility(View.VISIBLE);
            textName1.setText(testInfo.getResults().get(0).getName());
            textResult1.setText(result);
            textUnit1.setText(testInfo.getResults().get(0).getUnit());
            results.put(1, result);
        }
    }

    private void showDebugInfo(String data) {
        if (AppPreferences.getShowDebugInfo()) {
            AlertDialog.Builder builder;
            final TextView showText = new TextView(getActivity());
            showText.setText(String.format("%s = %s", testInfo.getName(), data));

            showText.setPadding(50, 20, 40, 30);

            builder = new AlertDialog.Builder(getActivity());
            builder.setView(showText);

            builder.setPositiveButton("Copy", (dialog1, which) -> {

                if (getActivity() != null) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", showText.getText());
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }

                    Toast.makeText(getActivity(), "Data copied to clipboard",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });

            builder.setNegativeButton(R.string.cancel, (dialog12, which) -> dialog12.dismiss());

            AlertDialog dialog;
            dialog = builder.create();
            dialog.setTitle("Received Data");
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCurrentModeListener) {
            mListener = (OnCurrentModeListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnCurrentModeListener");
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

    public interface OnCurrentModeListener {
        void onCurrentMode(int mode);
    }

}

