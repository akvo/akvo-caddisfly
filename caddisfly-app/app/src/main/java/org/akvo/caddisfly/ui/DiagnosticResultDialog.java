/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;


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
import android.widget.TableLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultInfo;
import org.akvo.caddisfly.util.ColorUtils;

import java.util.ArrayList;

/**
 * A Dialog to display detailed result information of the analysis
 * <p/>
 * Only displayed in diagnostic mode
 */
public class DiagnosticResultDialog extends DialogFragment {

    private ArrayList<Result> mResults;
    private boolean mIsCalibration;

    public DiagnosticResultDialog() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of this dialog
     *
     * @param results       array list of results
     * @param allowRetry    whether to display retry button
     * @param result        the result value
     * @param color         the color value
     * @param isCalibration is this a calibration result
     * @return the dialog
     */
    public static DiagnosticResultDialog newInstance(ArrayList<Result> results, boolean allowRetry,
                                                     double result, int color, boolean isCalibration) {
        DiagnosticResultDialog fragment = new DiagnosticResultDialog();
        Bundle args = new Bundle();
        args.putBoolean("retry", allowRetry);
        args.putDouble("result", result);
        args.putInt("color", color);
        args.putBoolean("calibration", isCalibration);
        fragment.mResults = results;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.dialog_diagnostic_result, container, false);

        ListView resultList = (ListView) view.findViewById(R.id.listResults);
        resultList.setAdapter(new ResultListAdapter());

        boolean allowRetry = getArguments().getBoolean("retry");
        int mColor = getArguments().getInt("color");

        mIsCalibration = getArguments().getBoolean("calibration");

        Button cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        Button retryButton = (Button) view.findViewById(R.id.buttonRetry);
        Button okButton = (Button) view.findViewById(R.id.buttonOk);

        //if allowRetry is true then this is an error show retry button
        if (allowRetry) {
            getDialog().setTitle(R.string.error);
            cancelButton.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
            okButton.setVisibility(View.GONE);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DiagnosticResultDialogListener listener = (DiagnosticResultDialogListener) getActivity();
                    listener.onFinishDiagnosticResultDialog(false, true, mIsCalibration);

                }
            });

            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DiagnosticResultDialogListener listener = (DiagnosticResultDialogListener) getActivity();
                    listener.onFinishDiagnosticResultDialog(true, false, mIsCalibration);

                }
            });
        } else {
            double result = getArguments().getDouble("result");
            if (mIsCalibration) {
                getDialog().setTitle(String.format("%s: %s", getString(R.string.result), ColorUtils.getColorRgbString(mColor)));
            } else {
                getDialog().setTitle(String.format("%s: %.2f", getString(R.string.result), result));
            }

            cancelButton.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
            okButton.setVisibility(View.VISIBLE);

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DiagnosticResultDialogListener listener = (DiagnosticResultDialogListener) getActivity();
                    listener.onFinishDiagnosticResultDialog(false, false, mIsCalibration);

                }
            });
        }
        return view;
    }

    public interface DiagnosticResultDialogListener {
        void onFinishDiagnosticResultDialog(boolean retry, boolean cancelled, boolean isCalibration);
    }

    private class ResultListAdapter extends BaseAdapter {

        public int getCount() {
            return mResults.size();
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

            CaddisflyApp caddisflyApp = ((CaddisflyApp) getActivity().getApplicationContext());

            if (caddisflyApp != null && rowView != null) {
                TextView rgbText = (TextView) rowView.findViewById(R.id.textRgb);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
                Button button = (Button) rowView.findViewById(R.id.buttonColor);

                Result result = mResults.get(position);

                imageView.setImageBitmap(result.getBitmap());
                int color = result.getResults().get(0).getColor();

                button.setBackgroundColor(color);

                if (mIsCalibration) {
                    TableLayout colorModelHeading = (TableLayout) rowView.findViewById(R.id.colorModelHeading);
                    colorModelHeading.setVisibility(View.GONE);
                }

                //display rgb value
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                rgbText.setText(String.format("%d  %d  %d", r, g, b));

                ListView resultList = (ListView) rowView.findViewById(R.id.listResults);

                if (mIsCalibration) {
                    // Hide the results table and show only the color
                    resultList.setVisibility(View.GONE);
                } else {
                    // Show the results table
                    resultList.setAdapter(new ResultsDetailsAdapter(result.getResults()));
                }
            }
            return rowView;
        }
    }


    public class ResultsDetailsAdapter extends BaseAdapter {

        final ArrayList<ResultInfo> mResults;

        public ResultsDetailsAdapter(ArrayList<ResultInfo> results) {
            mResults = results;
        }

        public int getCount() {
            return 3;
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
            View rowView = inflater.inflate(R.layout.row_result, parent, false);

            int calibrationSteps = 2;
            switch (position) {
                case 0:
                    calibrationSteps = 2;
                    break;
                case 1:
                    calibrationSteps = 3;
                    break;
                case 2:
                    calibrationSteps = 5;
                    break;
            }

            TextView textCalibrationSteps = (TextView) rowView.findViewById(R.id.textCalibrationSteps);
            textCalibrationSteps.setText(String.format("%s step", calibrationSteps));

            TextView resultText;

            for (ResultInfo resultInfo : mResults) {
                if (resultInfo.getCalibrationSteps() == calibrationSteps) {

                    switch (resultInfo.getColorModel()) {
                        case LAB:
                            resultText = (TextView) rowView.findViewById(R.id.textLabResult);
                            resultText.setText(String.format("%.2f", resultInfo.getResult()));
                            break;
                        case RGB:
                            resultText = (TextView) rowView.findViewById(R.id.textRgbResult);
                            resultText.setText(String.format("%.2f", resultInfo.getResult()));
                            break;
                        case HSV:
                            resultText = (TextView) rowView.findViewById(R.id.textHsvResult);
                            resultText.setText(String.format("%.2f", resultInfo.getResult()));
                            break;
                    }
                }
            }
            return rowView;
        }
    }
}
