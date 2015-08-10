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
import android.widget.ImageView;
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
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.row_calibrate, parent, false);

        final CaddisflyApp caddisflyApp = ((CaddisflyApp) activity.getApplicationContext());
        ArrayList<Swatch> ranges = caddisflyApp.currentTestInfo.getRanges();
        Swatch range = ranges.get(position);

        TextView textUnit = (TextView) rowView.findViewById(R.id.textUnit);
        TextView rgbText = (TextView) rowView.findViewById(R.id.textRgb);
        TextView hsvText = (TextView) rowView.findViewById(R.id.textHsv);
        TextView brightnessText = (TextView) rowView.findViewById(R.id.textBrightness);
        ImageView moreArrow = (ImageView) rowView.findViewById(R.id.imageArrow);
        //ImageView errorImage = (ImageView) rowView.findViewById(R.id.error);
        Button button = (Button) rowView.findViewById(R.id.buttonColor);

        int color = range.getColor();

        // display ppm value
        Spannable word = new SpannableString(String.format("%.2f ", range.getValue()));
        textUnit.setText(word);

        Spannable wordTwo = new SpannableString(caddisflyApp.currentTestInfo.getUnit());

        wordTwo.setSpan(new ForegroundColorSpan(Color.argb(255, 80, 80, 80)), 0, wordTwo.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        wordTwo.setSpan(new RelativeSizeSpan(.6f), 0, wordTwo.length(), 0);

        textUnit.append(wordTwo);

        if (color != 0 && color != -16777216) {
            button.setBackgroundColor(color);
            button.setText("");

            //Display additional information if we are in diagnostic mode
            if (AppPreferences.isDiagnosticMode(getContext())) {
                double distance = 0;
                if (position > 0) {
                    int previousColor = ranges.get(position - 1).getColor();
                    distance = ColorUtils.getColorDistance(previousColor, color);
                }
                rgbText.setText(String.format("r: %s", ColorUtils.getColorRgbString(color)));

                float[] colorHSV = new float[3];
                Color.colorToHSV(color, colorHSV);
                hsvText.setText(String.format("h: %.0f  %.2f  %.2f", colorHSV[0], colorHSV[1], colorHSV[1]));
                brightnessText.setText(String.format("d:%.0f  b: %d", distance, ColorUtils.getBrightness(color)));
                rgbText.setVisibility(View.VISIBLE);
                hsvText.setVisibility(View.VISIBLE);
                brightnessText.setVisibility(View.VISIBLE);
                moreArrow.setVisibility(View.GONE);
            }
        } else {
            button.setBackgroundColor(Color.argb(0, 10, 10, 10));
            button.setText("?");
            rgbText.setText("");
            hsvText.setText("");
        }


        return rowView;
    }
}
