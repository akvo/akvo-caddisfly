/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
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
import org.akvo.caddisfly.model.ColorInfo;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SwatchesAdapter extends ArrayAdapter<Integer> {

    private final Activity activity;

    private final DecimalFormat doubleFormat = new DecimalFormat("0.0");

    public SwatchesAdapter(Activity activity, Integer[] colorArray) {
        super(activity, R.layout.row_swatch, colorArray);
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.row_swatch, parent, false);

        MainApp mainApp = ((MainApp) activity.getApplicationContext());

        if (mainApp != null && rowView != null) {
            ArrayList<ColorInfo> colorRange = mainApp.colorList;

            TextView ppmText = (TextView) rowView.findViewById(R.id.ppmText);
            TextView rgbText = (TextView) rowView.findViewById(R.id.rgbText);
            Button button = (Button) rowView.findViewById(R.id.button);

            int color = colorRange.get(position).getColor();

            //paint the button with the color
            button.setBackgroundColor(color);

            //display ppm value
            ppmText.setText(doubleFormat
                    .format(mainApp.rangeStart + (position
                            * mainApp.rangeIncrementValue)));

            //display rgb value
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            rgbText.setText(
                    String.format("d:%.0f  %s: %d  %d  %d", colorRange.get(position).getDistance(), mainApp.getString(R.string.rgb), r, g, b));
        }
        return rowView;
    }
}
