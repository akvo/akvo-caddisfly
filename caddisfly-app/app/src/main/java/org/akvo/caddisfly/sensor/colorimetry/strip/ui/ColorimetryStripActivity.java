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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;

import java.util.Collections;
import java.util.List;

public class ColorimetryStripActivity extends BaseActivity implements BaseActivity.ResultListener {

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
}
