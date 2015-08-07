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
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.util.ColorUtils;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DialogGridError extends DialogFragment {

    //ArrayList<Integer> mColors;
    private ArrayList<Result> mResults;
    private boolean mIsCalibration;

    public DialogGridError() {
        // Required empty public constructor
    }

    public static DialogGridError newInstance(ArrayList<Result> results, boolean allowRetry,
                                              double result, int color, boolean isCalibration) {
        DialogGridError fragment = new DialogGridError();
        Bundle args = new Bundle();
        args.putBoolean("retry", allowRetry);
        //args.putIntegerArrayList("colors", colors);
        //args.putDoubleArray("results", convertDoubles(results));
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

        final View view = inflater.inflate(R.layout.dialog_grid_error, container, false);

        ListView resultList = (ListView) view.findViewById(R.id.listResults);
        resultList.setAdapter(new ImageAdapter());

        //mColors = getArguments().getIntegerArrayList("colors");
        //mResults = getArguments().getDoubleArray("results");
        boolean allowRetry = getArguments().getBoolean("retry");
        int mColor = getArguments().getInt("color");

        mIsCalibration = getArguments().getBoolean("calibration");

        Button cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        Button retryButton = (Button) view.findViewById(R.id.buttonRetry);
        Button okButton = (Button) view.findViewById(R.id.buttonOk);

        if (allowRetry) {
            getDialog().setTitle(R.string.error);
            cancelButton.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
            okButton.setVisibility(View.GONE);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ErrorListDialogListener listener = (ErrorListDialogListener) getActivity();
                    listener.onFinishErrorListDialog(false, true, mIsCalibration);

                }
            });

            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ErrorListDialogListener listener = (ErrorListDialogListener) getActivity();
                    listener.onFinishErrorListDialog(true, false, mIsCalibration);

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

                    ErrorListDialogListener listener = (ErrorListDialogListener) getActivity();
                    listener.onFinishErrorListDialog(false, false, mIsCalibration);

                }
            });
        }
        return view;
    }

    public interface ErrorListDialogListener {
        void onFinishErrorListDialog(boolean retry, boolean cancelled, boolean isCalibration);
    }

    private class ImageAdapter extends BaseAdapter {
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

            MainApp mainApp = ((MainApp) getActivity().getApplicationContext());

            if (mainApp != null && rowView != null) {
                //TextView ppmText = (TextView) rowView.findViewById(R.id.ppmText);
                TextView rgbText = (TextView) rowView.findViewById(R.id.textRgb);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
                Button button = (Button) rowView.findViewById(R.id.buttonColor);

                Result result = mResults.get(position);

                imageView.setImageBitmap(result.getBitmap());
                int color = result.getColor();

                button.setBackgroundColor(color);

//                if (mIsCalibration) {
//                    ppmText.setVisibility(View.INVISIBLE);
//                } else if (mResults[position] > -1) {
//                    ppmText.setText(doubleFormat.format(mResults[position]));
//                }

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
                    resultList.setAdapter(new ResultsAdapter(result.getResults()));
                }
            }
            return rowView;
        }
    }

    public class ResultsAdapter extends BaseAdapter {

        final ArrayList<Pair<String, Double>> mResults;

        public ResultsAdapter(ArrayList<Pair<String, Double>> results) {
            mResults = results;
        }

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
            View rowView = inflater.inflate(R.layout.row_result, parent, false);

            MainApp mainApp = ((MainApp) getActivity().getApplicationContext());

            if (mainApp != null && rowView != null) {
                TextView descriptionText = (TextView) rowView.findViewById(R.id.textDescription);
                TextView resultText = (TextView) rowView.findViewById(R.id.textResult);
                descriptionText.setText(mResults.get(position).first);
                resultText.setText(String.format("%.2f", mResults.get(position).second));
            }
            return rowView;
        }
    }
}
