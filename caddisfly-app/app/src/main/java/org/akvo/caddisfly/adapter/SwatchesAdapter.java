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
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.util.ColorUtils;

import java.util.ArrayList;

public class SwatchesAdapter extends ArrayAdapter<Swatch> {

    private final Activity activity;
    private final ArrayList<Swatch> colorArray;

    public SwatchesAdapter(Activity activity, ArrayList<Swatch> colorArray) {
        super(activity, R.layout.row_swatch, colorArray);
        this.activity = activity;
        this.colorArray = colorArray;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();

        @SuppressLint("ViewHolder")
        View rowView = inflater.inflate(R.layout.row_swatch, parent, false);

        if (rowView != null) {
            TextView ppmText = (TextView) rowView.findViewById(R.id.textUnit);
            TextView rgbText = (TextView) rowView.findViewById(R.id.textRgb);
            TextView hsvText = (TextView) rowView.findViewById(R.id.textHsv);
            Button button = (Button) rowView.findViewById(R.id.buttonColor);

            int color = colorArray.get(position).getColor();

            button.setBackgroundColor(color);

            //display ppm value
            ppmText.setText(String.format("%.2f", colorArray.get(position).getValue()));

            double distance = 0;
            double distanceRgb = 0;
            if (position > 0) {
                int previousColor = colorArray.get(position - 1).getColor();
                distance = ColorUtils.getColorDistanceLab(ColorUtils.colorToLab(previousColor),
                        ColorUtils.colorToLab(color));
                distanceRgb = ColorUtils.getColorDistance(previousColor, color);

            }

            float[] colorHSV = new float[3];
            Color.colorToHSV(color, colorHSV);

            rgbText.setText(String.format("d:%.2f  %s: %s", distanceRgb, "rgb", ColorUtils.getColorRgbString(color)));
            hsvText.setText(String.format("d:%.2f  %s: %.0f  %.2f  %.2f", distance, "hsv", colorHSV[0], colorHSV[1], colorHSV[1]));
        }
        return rowView;
    }
}
