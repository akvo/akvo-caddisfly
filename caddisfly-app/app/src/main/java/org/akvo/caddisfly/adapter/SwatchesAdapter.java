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

import java.text.DecimalFormat;

public class SwatchesAdapter extends ArrayAdapter<ResultRange> {

    private final Activity activity;
    private final ResultRange[] colorArray;

    private final DecimalFormat doubleFormat = new DecimalFormat("0.0");

    public SwatchesAdapter(Activity activity, ResultRange[] colorArray) {
        super(activity, R.layout.row_swatch, colorArray);
        this.activity = activity;
        this.colorArray = colorArray;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.row_swatch, parent, false);

        MainApp mainApp = ((MainApp) activity.getApplicationContext());

        if (mainApp != null && rowView != null) {
            TextView ppmText = (TextView) rowView.findViewById(R.id.ppmText);
            TextView rgbText = (TextView) rowView.findViewById(R.id.rgbText);
            TextView hsvText = (TextView) rowView.findViewById(R.id.hsvText);
            Button button = (Button) rowView.findViewById(R.id.button);

            int color = colorArray[position].getColor();

            button.setBackgroundColor(color);

            //display ppm value
            ppmText.setText(doubleFormat.format(colorArray[position].getValue()));

            double distance = 0;
            if (position > 0) {
                int previousColor = colorArray[position - 1].getColor();
                distance = ColorUtils.getColorDistance(previousColor, color);
            }

            float[] colorHSV = new float[3];
            Color.colorToHSV(color, colorHSV);

            rgbText.setText(String.format("d:%.0f  %s: %s", distance, "rgb", ColorUtils.getColorRgbString(color)));
            hsvText.setText(String.format("d:%.0f  %s: %.0f %.0f %.0f", distance, "hsv", colorHSV[0], colorHSV[1], colorHSV[1]));
        }
        return rowView;
    }
}
