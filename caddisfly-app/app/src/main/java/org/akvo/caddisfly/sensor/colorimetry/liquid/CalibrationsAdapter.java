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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.List;
import java.util.Locale;

/**
 * The calibrated swatches and values for each test type
 */
class CalibrationsAdapter extends ArrayAdapter<Swatch> {

    private static final float SMALL_FONT_SIZE = .6f;
    @NonNull
    private final Activity activity;
    private final boolean mDisplayDecimal;

    CalibrationsAdapter(@NonNull Activity activity, @NonNull Swatch[] rangeArray, boolean displayDecimal) {
        super(activity, R.layout.row_calibrate, rangeArray);
        this.activity = activity;
        mDisplayDecimal = displayDecimal;
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();

        @SuppressLint("ViewHolder")
        View rowView = inflater.inflate(R.layout.row_calibrate, parent, false);

        List<Swatch> swatches = CaddisflyApp.getApp().getCurrentTestInfo().getSwatches();
        Swatch swatch = swatches.get(position);

        TextView textName = (TextView) rowView.findViewById(R.id.textName);
        TextView textSwatch = (TextView) rowView.findViewById(R.id.textSwatch);

        int color = swatch.getColor();

        //display unit value
        Spannable word;
        if (mDisplayDecimal) {
            word = new SpannableString(String.format(Locale.getDefault(), "%.2f ", swatch.getValue()));
        } else {
            word = new SpannableString(String.format(Locale.getDefault(), "%.0f ", swatch.getValue()));
        }
        textName.setText(word);

        //append the unit
        Spannable wordTwo = new SpannableString(CaddisflyApp.getApp().getCurrentTestInfo().getUnit());

        wordTwo.setSpan(new ForegroundColorSpan(Color.GRAY), 0, wordTwo.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        wordTwo.setSpan(new RelativeSizeSpan(SMALL_FONT_SIZE), 0, wordTwo.length(), 0);

        textName.append(wordTwo);

        if (color == Color.TRANSPARENT || color == Color.BLACK) {
            //not calibrated so just show a '?' instead of color
            textSwatch.setBackgroundColor(Color.TRANSPARENT);
            textSwatch.setText("?");
        } else {
            //show the calibrated color
            textSwatch.setBackgroundColor(color);

            //display additional information if we are in diagnostic mode
            if (AppPreferences.isDiagnosticMode()) {

                TextView textRgb = (TextView) rowView.findViewById(R.id.textRgb);
                TextView textHsv = (TextView) rowView.findViewById(R.id.textHsv);
                TextView textBrightness = (TextView) rowView.findViewById(R.id.textBrightness);

                double distance = 0;
                if (position > 0) {
                    int previousColor = swatches.get(position - 1).getColor();
                    distance = ColorUtil.getColorDistance(previousColor, color);
                }
                textRgb.setText(String.format("r: %s", ColorUtil.getColorRgbString(color)));

                float[] colorHSV = new float[3];
                Color.colorToHSV(color, colorHSV);
                textHsv.setText(String.format(Locale.getDefault(), "h: %.0f  %.2f  %.2f", colorHSV[0], colorHSV[1], colorHSV[2]));
                textBrightness.setText(String.format(Locale.getDefault(), "d:%.2f  b: %d", distance, ColorUtil.getBrightness(color)));
                textRgb.setVisibility(View.VISIBLE);
                textHsv.setVisibility(View.VISIBLE);
                textBrightness.setVisibility(View.VISIBLE);
            }
        }

        return rowView;
    }
}
