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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.ArrayList;
import java.util.Locale;

/**
 * List of swatches including the generated gradient swatches
 */
class DiagnosticSwatchesAdapter extends ArrayAdapter<Swatch> {

    private final Activity activity;
    private final ArrayList<Swatch> colorArray;

    DiagnosticSwatchesAdapter(Activity activity, ArrayList<Swatch> colorArray) {
        super(activity, R.layout.row_swatch, colorArray);
        this.activity = activity;
        this.colorArray = colorArray;
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();

        @SuppressLint("ViewHolder")
        View rowView = inflater.inflate(R.layout.row_swatch, parent, false);

        if (rowView != null) {

            int color = colorArray.get(position).getColor();

            rowView.findViewById(R.id.textSwatch).setBackgroundColor(color);

            //display unit value
            ((TextView) rowView.findViewById(R.id.textName)).setText(
                    String.format(Locale.getDefault(), "%.2f", colorArray.get(position).getValue()));

            double distance = 0;
            double distanceRgb = 0;
            if (position > 0) {
                int previousColor = colorArray.get(position - 1).getColor();
                distance = ColorUtil.getColorDistanceLab(ColorUtil.colorToLab(previousColor),
                        ColorUtil.colorToLab(color));
                distanceRgb = ColorUtil.getColorDistance(previousColor, color);
            }

            float[] colorHSV = new float[3];
            Color.colorToHSV(color, colorHSV);

            ((TextView) rowView.findViewById(R.id.textRgb)).setText(
                    String.format(Locale.getDefault(), "d:%.2f  %s: %s",
                            distanceRgb, "rgb", ColorUtil.getColorRgbString(color)));
            ((TextView) rowView.findViewById(R.id.textHsv)).setText(
                    String.format(Locale.getDefault(), "d:%.2f  %s: %.0f  %.2f  %.2f",
                            distance, "hsv", colorHSV[0], colorHSV[1], colorHSV[1]));
        }else {
            return view;
        }
        return rowView;
    }
}
