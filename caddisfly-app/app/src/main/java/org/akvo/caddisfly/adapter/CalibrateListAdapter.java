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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.AppPreferences;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.util.ColorUtils;

import java.util.ArrayList;

public class CalibrateListAdapter extends ArrayAdapter<Swatch> {

    private final Activity activity;

    public CalibrateListAdapter(Activity activity, Swatch[] rangeArray) {
        super(activity, R.layout.row_calibrate, rangeArray);
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();

        @SuppressLint("ViewHolder")
        View rowView = inflater.inflate(R.layout.row_calibrate, parent, false);

        final CaddisflyApp caddisflyApp = ((CaddisflyApp) activity.getApplicationContext());
        ArrayList<Swatch> ranges = caddisflyApp.currentTestInfo.getSwatches();
        Swatch range = ranges.get(position);

        TextView textUnit = (TextView) rowView.findViewById(R.id.textUnit);
        TextView textRgb = (TextView) rowView.findViewById(R.id.textRgb);
        TextView textHsv = (TextView) rowView.findViewById(R.id.textHsv);
        TextView textBrightness = (TextView) rowView.findViewById(R.id.textBrightness);
        Button buttonColor = (Button) rowView.findViewById(R.id.buttonColor);

        int color = range.getColor();

        // display ppm value
        Spannable word = new SpannableString(String.format("%.2f ", range.getValue()));
        textUnit.setText(word);

        Spannable wordTwo = new SpannableString(caddisflyApp.currentTestInfo.getUnit());

        wordTwo.setSpan(new ForegroundColorSpan(Color.argb(255, 80, 80, 80)), 0, wordTwo.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        wordTwo.setSpan(new RelativeSizeSpan(.6f), 0, wordTwo.length(), 0);

        textUnit.append(wordTwo);

        if (color == Color.TRANSPARENT || color == Color.BLACK) {
            buttonColor.setBackgroundColor(Color.argb(0, 10, 10, 10));
            buttonColor.setText("?");
        } else {
            buttonColor.setBackgroundColor(color);

            //Display additional information if we are in diagnostic mode
            if (AppPreferences.isDiagnosticMode(getContext())) {
                double distance = 0;
                if (position > 0) {
                    int previousColor = ranges.get(position - 1).getColor();
                    distance = ColorUtils.getColorDistanceLab(ColorUtils.colorToLab(previousColor),
                            ColorUtils.colorToLab(color));
                }
                textRgb.setText(String.format("r: %s", ColorUtils.getColorRgbString(color)));

                float[] colorHSV = new float[3];
                Color.colorToHSV(color, colorHSV);
                textHsv.setText(String.format("h: %.0f  %.2f  %.2f", colorHSV[0], colorHSV[1], colorHSV[1]));
                textBrightness.setText(String.format("d:%.2f  b: %d", distance, ColorUtils.getBrightness(color)));
                textRgb.setVisibility(View.VISIBLE);
                textHsv.setVisibility(View.VISIBLE);
                textBrightness.setVisibility(View.VISIBLE);
                rowView.findViewById(R.id.imageArrow).setVisibility(View.GONE);
            }
        }

        return rowView;
    }
}
