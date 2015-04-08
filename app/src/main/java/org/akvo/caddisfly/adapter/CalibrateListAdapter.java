/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.PreferencesUtils;

import java.util.ArrayList;

public class CalibrateListAdapter extends ArrayAdapter<ResultRange> {

    private final Activity activity;

    public CalibrateListAdapter(Activity activity, ResultRange[] rangeArray) {
        super(activity, R.layout.row_calibrate, rangeArray);
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.row_calibrate, parent, false);

        final MainApp mainApp = ((MainApp) activity.getApplicationContext());

        rowView.setBackgroundResource(position % 2 == 0 ?
                R.drawable.listitem_row_2 : R.drawable.listitem_row_1);

        ArrayList<ResultRange> ranges = mainApp.currentTestInfo.getRanges();
        ResultRange range = ranges.get(position);

        TextView ppmText = (TextView) rowView.findViewById(R.id.ppmText);
        TextView rgbText = (TextView) rowView.findViewById(R.id.rgbText);
        TextView brightnessText = (TextView) rowView.findViewById(R.id.brightnessText);
        //ImageView errorImage = (ImageView) rowView.findViewById(R.id.error);
        Button button = (Button) rowView.findViewById(R.id.button);

        int color = range.getColor();

        // display ppm value
        ppmText.setText(mainApp.doubleFormat.format(range.getValue()));

//        if (ranges.get(index).getErrorCode() > 0) {
//            errorImage.setVisibility(View.VISIBLE);
//        } else {
//            errorImage.setVisibility(View.GONE);
//        }

        if (color != 0 && color != -16777216) {
            button.setBackgroundColor(color);
            button.setText("");
            boolean developerMode = PreferencesUtils.getBoolean(getContext(), R.string.developerModeKey, false);
            if (developerMode) {
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                double distance = 0;
                if (position > 0) {
                    int previousColor = ranges.get(position - 1).getColor();
                    distance = ColorUtils.getDistance(previousColor, color);
                }
                rgbText.setText(String.format("c: %d  %d  %d", r, g, b));
                brightnessText.setText(String.format("d:%.0f  b: %d", distance, ColorUtils.getBrightness(color)));
                rgbText.setVisibility(View.VISIBLE);
                brightnessText.setVisibility(View.VISIBLE);
            }
        } else {
            button.setBackgroundColor(Color.argb(0, 10, 10, 10));
            button.setText("?");
            rgbText.setText("");
        }


        return rowView;
    }
}
