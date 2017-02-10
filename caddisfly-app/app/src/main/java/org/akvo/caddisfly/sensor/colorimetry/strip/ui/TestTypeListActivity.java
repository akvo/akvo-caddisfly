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

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TestTypeListActivity extends BaseActivity {

    private StripAdapter adapter;
    private StripTest stripTest;
    private List<StripTest.Brand> brands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_type_list);

        setTitle(R.string.selectTest);

        ListView listTypes = (ListView) findViewById(R.id.list_types);

        if (stripTest == null) {
            stripTest = new StripTest();
        }

        brands = stripTest.getBrandsAsList();

        if (brands != null) {
            //order alpha-numeric on brand
            Collections.sort(brands, new Comparator<StripTest.Brand>() {
                @Override
                public int compare(@NonNull final StripTest.Brand object1, @NonNull final StripTest.Brand object2) {
                    return object1.getName().compareToIgnoreCase(object2.getName());
                }
            });

            if (adapter == null) {
                adapter = new StripAdapter(this,
                        R.layout.item_test_type, brands);
            }
            listTypes.setAdapter(adapter);

            listTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    startDetailActivity(brands.get(i).getUuid());
                }
            });
        }
    }

    private void startDetailActivity(String uuid) {
        CaddisflyApp.getApp().loadTestConfigurationByUuid(uuid);
        Intent intent = new Intent(getIntent());
        intent.setClass(this, BrandInfoActivity.class);
        intent.putExtra(Constant.UUID, uuid);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (data != null) {
                Intent intent = new Intent(data);
                setResult(resultCode, intent);
            }

            // If an external activity is expecting the result then finish
            if (!getIntent().getBooleanExtra("internal", false)) {
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static class StripAdapter extends ArrayAdapter<StripTest.Brand> {

        private static final String TAG = "StripAdapter";

        private final List<StripTest.Brand> brandList;
        private final int resource;
        @NonNull
        private final Context context;

        @SuppressWarnings("SameParameterValue")
        StripAdapter(@NonNull Context context, int resource, List<StripTest.Brand> brandNames) {
            super(context, resource);

            this.context = context;
            this.resource = resource;
            this.brandList = brandNames;
        }

        @Override
        public int getCount() {
            return brandList.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

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

            StripTest.Brand brand = brandList.get(position);

            if (brand != null) {

                // for display purposes sort patches by position on the strip
                List<StripTest.Brand.Patch> patches = brand.getPatchesSortedByPosition();

                // display the range in the description
                if (patches != null && patches.size() > 0) {
                    StringBuilder ranges = new StringBuilder();
                    for (StripTest.Brand.Patch patch : patches) {
                        try {
                            int valueCount = patch.getColors().length();
                            if (ranges.length() > 0) {
                                ranges.append(", ");
                            }
                            if (patch.getColors().length() > 0) {
                                ranges.append(String.format(Locale.US, "%.0f - %.0f",
                                        patch.getColors().getJSONObject(0).getDouble(SensorConstants.VALUE),
                                        patch.getColors().getJSONObject(valueCount - 1).getDouble(SensorConstants.VALUE)));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                        if (brand.getGroupingType() == StripTest.GroupType.GROUP) {
                            break;
                        }
                    }

                    holder.textView.setText(brand.getName());
                    holder.textSubtitle.setText(brand.getBrandDescription() + ", " + ranges.toString());
//                    if (brand.isCustomTest()) {
//                        holder.imageIcon.setVisibility(View.VISIBLE);
//                    } else {
//                        holder.imageIcon.setVisibility(View.INVISIBLE);
//                    }
                }
            }
            return view;

        }

        private static class ViewHolder {

            @NonNull
            private final TextView textView;
            @NonNull
            private final TextView textSubtitle;
//            @NonNull
//            private final ImageView imageIcon;

            ViewHolder(@NonNull View v) {

                textView = (TextView) v.findViewById(R.id.text_title);
                textSubtitle = (TextView) v.findViewById(R.id.text_subtitle);
//                imageIcon = (ImageView) v.findViewById(R.id.imageIcon);
            }
        }
    }
}
