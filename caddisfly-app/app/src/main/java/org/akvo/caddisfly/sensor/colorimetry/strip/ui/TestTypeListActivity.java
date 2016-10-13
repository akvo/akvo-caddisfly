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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TestTypeListActivity extends BaseActivity implements BaseActivity.ResultListener {

    private StripAdapter adapter;
    private StripTest stripTest;
    private List<StripTest.Brand> brands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_type_list);

        setTitle(R.string.selectTest);

        //set result listener
        BaseActivity.setResultListener(this);

        ListView listTypes = (ListView) findViewById(R.id.list_types);

        if (stripTest == null)
            stripTest = new StripTest();

        brands = stripTest.getBrandsAsList();

        if (brands != null) {
            //order alpha-numeric on brand
            Collections.sort(brands, new Comparator<StripTest.Brand>() {
                @Override
                public int compare(final StripTest.Brand object1, final StripTest.Brand object2) {
                    return object1.getName().compareTo(object2.getName());
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
        Intent detailIntent = new Intent(getBaseContext(), BrandInfoActivity.class);
        detailIntent.putExtra(Constant.UUID, uuid);
        detailIntent.putExtra("internal", getIntent().getBooleanExtra("internal", false));
        startActivity(detailIntent);
    }

    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String cadUuid = null;
        if (intent.hasExtra("caddisflyResourceUuid")) {
            cadUuid = intent.getStringExtra("caddisflyResourceUuid");
        }
        if (intent.getBooleanExtra(SensorConstants.FINISH, false)) {
            finish();
        } else if (cadUuid != null && cadUuid.length() > 0) {
            // when we get back here, we want to go straight back to the FLOW app
            intent.putExtra(SensorConstants.FINISH, true);
            startDetailActivity(cadUuid);
        }
    }

    @Override
    public void onResult(String result, String imagePath) {

        Intent intent = new Intent(getIntent());
        if (result.isEmpty()) {
            setResult(RESULT_CANCELED, intent);
        } else {
            intent.putExtra("response", result);
            intent.putExtra("image", imagePath);
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static class StripAdapter extends ArrayAdapter<StripTest.Brand> {

        private final List<StripTest.Brand> brandList;
        private final int resource;
        private final Context context;
        private final StripTest stripTest;

        @SuppressWarnings("SameParameterValue")
        StripAdapter(Context context, int resource, List<StripTest.Brand> brandNames) {
            super(context, resource);

            this.context = context;
            this.resource = resource;
            this.brandList = brandNames;
            this.stripTest = new StripTest();
        }

        @Override
        public int getCount() {
            return brandList.size();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

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

            if (stripTest != null) {

                if (brand != null) {

                    List<StripTest.Brand.Patch> patches = brand.getPatches();

                    if (patches != null && patches.size() > 0) {
//                            String subtext = "";
//                            for (int i = 0; i < patches.size(); i++) {
//                                subtext += patches.get(i).getDesc() + ", ";
//                            }
//                        int indexLastSep = subtext.lastIndexOf(",");
//                            subtext = subtext.substring(0, indexLastSep);

                        String ranges = "";
                        for (StripTest.Brand.Patch patch : patches) {
                            try {
                                int valueCount = patch.getColours().length();
                                if (!ranges.isEmpty()) {
                                    ranges += ", ";
                                }
                                ranges += String.format(Locale.US, "%.0f - %.0f",
                                        patch.getColours().getJSONObject(0).getDouble("value"),
                                        patch.getColours().getJSONObject(valueCount - 1).getDouble("value"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (brand.getGroupingType() == StripTest.GroupType.GROUP) {
                                break;
                            }
                        }


                        holder.textView.setText(brand.getName());
                        holder.textSubtitle.setText(brand.getBrandDescription() + ", " + ranges);
                    }
                }
            } else holder.textView.setText(brand.getName());
            return view;

        }

        private static class ViewHolder {

            private final TextView textView;
            private final TextView textSubtitle;

            ViewHolder(View v) {

                textView = (TextView) v.findViewById(R.id.text_title);
                textSubtitle = (TextView) v.findViewById(R.id.text_subtitle);
            }
        }
    }
}
