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

package org.akvo.caddisfly.sensor.liquid;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.model.TestInfo;

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
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = testInfo.getCalibrations().get(position);
        holder.mIdView.setBackground(new ColorDrawable(holder.mItem.color));
        holder.textValue.setText(String.valueOf(holder.mItem.value));
        holder.textUnit.setText(String.valueOf(testInfo.Results().get(0).getUnit()));

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
        Calibration mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.buttonColor);
            textValue = view.findViewById(R.id.textValue);
            textUnit = view.findViewById(R.id.textUnit);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textValue.getText() + "'";
        }
    }
}
