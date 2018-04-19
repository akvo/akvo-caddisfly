/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.chamber;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.List;
import java.util.Locale;

public class CalibrationViewAdapter extends RecyclerView.Adapter<CalibrationViewAdapter.ViewHolder> {

    private final TestInfo testInfo;
    private final CalibrationItemFragment.OnCalibrationSelectedListener mListener;

    CalibrationViewAdapter(TestInfo items, CalibrationItemFragment.OnCalibrationSelectedListener listener) {
        testInfo = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calibration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = testInfo.getCalibrations().get(position);

        String format = "%." + testInfo.getDecimalPlaces() + "f";
        holder.textValue.setText(String.valueOf(String.format(Locale.getDefault(), format,
                holder.mItem.value)));

        Result result = testInfo.getResults().get(0);
        List<ColorItem> colors = result.getColors();
        if (position < colors.size()) {
            int color = colors.get(position).getRgb();
            holder.mIdView.setBackground(new ColorDrawable(color));
            holder.textUnit.setText(String.valueOf(result.getUnit()));

            //display additional information if we are in diagnostic mode
            if (AppPreferences.getShowDebugInfo()) {

                holder.textUnit.setVisibility(View.GONE);

                holder.textRgb.setText(String.format("r: %s", ColorUtil.getColorRgbString(color)));
                holder.textRgb.setVisibility(View.VISIBLE);

                float[] colorHsv = new float[3];
                Color.colorToHSV(color, colorHsv);
                holder.textHsv.setText(String.format(Locale.getDefault(),
                        "h: %.0f  %.2f  %.2f", colorHsv[0], colorHsv[1], colorHsv[2]));
                holder.textHsv.setVisibility(View.VISIBLE);

                double distance = 0;
                if (position > 0) {
                    int previousColor = colors.get(position - 1).getRgb();
                    distance = ColorUtil.getColorDistance(previousColor, color);
                }

                holder.textBrightness.setText(String.format(Locale.getDefault(),
                        "d:%.2f  b: %d", distance, ColorUtil.getBrightness(color)));
                holder.textBrightness.setVisibility(View.VISIBLE);
            }
        }

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onCalibrationSelected(holder.mItem);
            }
        });

    }

    @Override
    public int getItemCount() {
        return testInfo.getCalibrations().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final Button mIdView;
        final TextView textValue;
        final TextView textUnit;
        private final TextView textRgb;
        private final TextView textHsv;
        private final TextView textBrightness;
        Calibration mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.buttonColor);
            textValue = view.findViewById(R.id.textValue);
            textUnit = view.findViewById(R.id.textUnit);
            textRgb = view.findViewById(R.id.textRgb);
            textHsv = view.findViewById(R.id.textHsv);
            textBrightness = view.findViewById(R.id.textBrightness);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textValue.getText() + "'";
        }
    }
}
