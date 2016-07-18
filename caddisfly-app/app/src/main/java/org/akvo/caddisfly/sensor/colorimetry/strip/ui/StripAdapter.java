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

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;

import java.util.List;

/**
 * Created by linda on 9/5/15
 */
public class StripAdapter extends ArrayAdapter<String> {

    private final List<String> brandNames;
    private final int resource;
    private final Context context;
    private final StripTest stripTest;

    @SuppressWarnings("SameParameterValue")
    public StripAdapter(Context context, int resource, List<String> brandNames) {
        super(context, resource);

        this.context = context;
        this.resource = resource;
        this.brandNames = brandNames;
        this.stripTest = new StripTest();
    }

    @Override
    public int getCount() {
        return brandNames.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (brandNames != null) {
            if (stripTest != null) {
                StripTest.Brand brand = stripTest.getBrand(brandNames.get(position));

                if (brand != null) {

                    List<StripTest.Brand.Patch> patches = brand.getPatches();

                    if (patches != null && patches.size() > 0) {
                        String subtext = "";
                        for (int i = 0; i < patches.size(); i++) {
                            subtext += patches.get(i).getDesc() + ", ";
                        }
                        int indexLastSep = subtext.lastIndexOf(",");
                        subtext = subtext.substring(0, indexLastSep);
                        holder.textView.setText(brand.getName());

                        holder.subtextView.setText(subtext);
                    }
                }
            } else holder.textView.setText(brandNames.get(position));
        }
        return view;

    }

    private static class ViewHolder {

        private final TextView textView;
        private final TextView subtextView;

        public ViewHolder(View v) {

            textView = (TextView) v.findViewById(R.id.text_title);
            subtextView = (TextView) v.findViewById(R.id.text_subtitle);

        }
    }
}
