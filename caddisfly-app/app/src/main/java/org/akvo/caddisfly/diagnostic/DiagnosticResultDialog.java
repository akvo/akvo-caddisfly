package org.akvo.caddisfly.diagnostic;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.ArrayList;
import java.util.Locale;

public class DiagnosticResultDialog extends DialogFragment {

    private ArrayList<ResultDetail> resultDetails;
    private Result result;

    /**
     * Instance of dialog.
     *
     * @param testFailed    did test fail
     * @param result        the result
     * @param resultDetails the result details
     * @param isCalibration is this in calibration mode
     * @param color         the color
     * @return the dialog
     */
    public static DialogFragment newInstance(boolean testFailed, Result result,
                                             ArrayList<ResultDetail> resultDetails,
                                             boolean isCalibration, int color) {
        DiagnosticResultDialog fragment = new DiagnosticResultDialog();
        Bundle args = new Bundle();
        fragment.result = result;
        fragment.resultDetails = resultDetails;
        args.putInt("color", color);
        args.putBoolean("testFailed", testFailed);
        args.putBoolean("isCalibration", isCalibration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.dialog_diagnostic_result, container, false);

        ListView listResults = view.findViewById(R.id.listResults);
        listResults.setAdapter(new ResultListAdapter());

        boolean testFailed = getArguments().getBoolean("testFailed");
        int color = getArguments().getInt("color");

        boolean isCalibration = getArguments().getBoolean("isCalibration");

        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonRetry = view.findViewById(R.id.buttonRetry);

        if (testFailed) {
            getDialog().setTitle(R.string.error);
        } else {
            if (isCalibration) {
                if (color == Color.TRANSPARENT) {
                    getDialog().setTitle(R.string.error);
                } else {
                    getDialog().setTitle(String.format("%s: %s", getString(R.string.result),
                            ColorUtil.getColorRgbString(color)));
                }
            } else {
                final String resultString = result.getResult();
                getDialog().setTitle(resultString);
            }
        }

        buttonCancel.setVisibility(View.GONE);
        buttonRetry.setVisibility(View.GONE);

        Button buttonOk = view.findViewById(R.id.buttonOk);
        buttonOk.setVisibility(View.VISIBLE);
        buttonOk.setOnClickListener(view1 -> this.dismiss());

        return view;
    }

    private class ResultListAdapter extends BaseAdapter {

        public int getCount() {
            return resultDetails.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            @SuppressLint("ViewHolder")
            View rowView = inflater.inflate(R.layout.row_info, parent, false);

            if (rowView != null) {
                ImageView imageView = rowView.findViewById(R.id.imageView);
                TextView textRgb = rowView.findViewById(R.id.textRgb);
                TextView textSwatch = rowView.findViewById(R.id.textSwatch);

                ResultDetail result = resultDetails.get(position);

                imageView.setImageBitmap(result.getBitmap());
                int color = result.getColor();

                textSwatch.setBackgroundColor(color);

                //display rgb value
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                textRgb.setText(String.format(Locale.getDefault(), "%d  %d  %d", r, g, b));
            }
            return rowView;
        }
    }

}
