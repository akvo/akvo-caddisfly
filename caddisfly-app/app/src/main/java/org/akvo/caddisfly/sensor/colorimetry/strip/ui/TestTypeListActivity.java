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

import java.util.Collections;
import java.util.List;

public class TestTypeListActivity extends BaseActivity implements BaseActivity.ResultListener {

    private StripAdapter adapter;
    private StripTest stripTest;
    private List<String> brandNames;

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

        brandNames = stripTest.getBrandsAsList();

        if (brandNames != null) {
            //order alpha-numeric on brand
            Collections.sort(brandNames);

            if (adapter == null) {
                adapter = new StripAdapter(this,
                        R.layout.item_test_type, brandNames);
            }
            listTypes.setAdapter(adapter);

            listTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    startDetailActivity(brandNames.get(i));
                }
            });
        }
    }

    private void startDetailActivity(String brandName) {
        Intent detailIntent = new Intent(getBaseContext(), BrandInfoActivity.class);
        detailIntent.putExtra(Constant.BRAND, brandName);
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

            // find brand which goes with this test uuid
            // and if found, go there immediately.
            StripTest stripTest = new StripTest();
            String brand = stripTest.matchUuidToBrand(cadUuid);
            if (brand != null && brand.length() > 0)
                startDetailActivity(brand);
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

    static class StripAdapter extends ArrayAdapter<String> {

        private final List<String> brandNames;
        private final int resource;
        private final Context context;
        private final StripTest stripTest;

        @SuppressWarnings("SameParameterValue")
        StripAdapter(Context context, int resource, List<String> brandNames) {
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

            ViewHolder(View v) {

                textView = (TextView) v.findViewById(R.id.text_title);
                subtextView = (TextView) v.findViewById(R.id.text_subtitle);

            }
        }
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
}
