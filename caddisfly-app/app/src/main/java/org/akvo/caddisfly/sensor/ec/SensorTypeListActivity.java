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

package org.akvo.caddisfly.sensor.ec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;

import java.util.List;

public class SensorTypeListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_type_list);

        setTitle(R.string.selectTest);

        ListView listTypes = (ListView) findViewById(R.id.list_types);

        final List<TestInfo> mTests = TestConfigHelper.loadTestsList();

        //Only sensor items to be displayed in the list
        for (int i = mTests.size() - 1; i >= 0; i--) {

            if (mTests.get(i).getType() != TestType.SENSOR) {
                mTests.remove(i);
                continue;
            }

            //Remove deprecated items
            if (mTests.get(i).getIsDeprecated()) {
                mTests.remove(i);
            }
        }

        //set the adapter with tests list
        TestTypesAdapter testTypesAdapter = new TestTypesAdapter(this,
                mTests.toArray(new TestInfo[mTests.size()]));

        listTypes.setAdapter(testTypesAdapter);

        listTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startDetailActivity(mTests.get(i).getId());
            }
        });
    }

    private void startDetailActivity(String uuid) {
        final Intent intent = new Intent(getBaseContext(), SensorActivity.class);
        intent.putExtra(Constant.UUID, uuid);
        intent.putExtra("internal", true);
        startActivity(intent);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static class TestTypesAdapter extends ArrayAdapter<TestInfo> {

        private final TestInfo[] mTestInfoArray;
        @NonNull
        private final Context context;

        TestTypesAdapter(@NonNull Activity activity, TestInfo[] testInfoArray) {
            super(activity, R.layout.row_calibrate, testInfoArray);
            context = activity;
            mTestInfoArray = testInfoArray.clone();
        }


        @Override
        public int getCount() {
            return mTestInfoArray.length;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = convertView;
            ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item_type, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            TestInfo testInfo = mTestInfoArray[position];

            if (testInfo != null) {
                holder.textView.setText(testInfo.getName());
            }

            return view;
        }

        private static class ViewHolder {

            @NonNull
            private final TextView textView;

            ViewHolder(@NonNull View v) {
                textView = (TextView) v.findViewById(R.id.text_title);
            }
        }
    }
}
