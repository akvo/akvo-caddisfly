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

package org.akvo.caddisfly.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.FileUtils;
import org.akvo.caddisfly.util.JsonUtils;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

/**
 * An activity representing a list of Calibrate items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CalibrateDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CalibrateListFragment} and the item details
 * (if present) is a {@link CalibrateDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link CalibrateListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CalibrateListActivity extends ActionBarActivity
        implements CalibrateListFragment.Callbacks {

    private final ExploreSpinnerAdapter mTopLevelSpinnerAdapter = new ExploreSpinnerAdapter();
    private final int REQUEST_CALIBRATE = 100;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calibrate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_swatches) {

            final Intent intent = new Intent(this, SwatchActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_list);

        if (findViewById(R.id.calibrate_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((CalibrateListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.calibrate_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
        setupActionBarSpinner();

    }

    /**
     * Callback method from {@link CalibrateListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(CalibrateDetailFragment.ARG_ITEM_ID, id);
            CalibrateDetailFragment fragment = new CalibrateDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.calibrate_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, CalibrateDetailActivity.class);
            detailIntent.putExtra(CalibrateDetailFragment.ARG_ITEM_ID, id);
            startActivityForResult(detailIntent, REQUEST_CALIBRATE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CALIBRATE:
                if (resultCode == Activity.RESULT_OK) {
                    ((CalibrateListFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.calibrate_list)).setAdapter();
                }
                break;
        }
    }

    private void setupActionBarSpinner() {
        ActionBar ab = getSupportActionBar();

        ArrayList<TestInfo> tests = null;
        try {
            //final String path = getExternalFilesDir(null) + Config.CONFIG_FILE_PATH;
            final String path = Environment.getExternalStorageDirectory() + Config.CONFIG_FOLDER + Config.CONFIG_FILE;

            File file = new File(path);
            String text;

            //Look for external json config file otherwise use the internal default one
            if (file.exists()) {
                text = FileUtils.loadTextFromFile(path);
            } else {
                text = FileUtils.readRawTextFile(this, R.raw.tests_json);
            }
            tests = JsonUtils.loadTests(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mTopLevelSpinnerAdapter.clear();
        MainApp mainApp = (MainApp) getApplicationContext();

        int selectedIndex = 0;
        int index = 0;
        assert tests != null;

        Resources res = getResources();
        Configuration conf = res.getConfiguration();

        for (TestInfo test : tests) {
            if (test.getType() == 0) {
                mTopLevelSpinnerAdapter.addItem(test.getCode(), test.getName(conf.locale.getLanguage()));
                if (test.getCode().equalsIgnoreCase(mainApp.currentTestInfo.getCode())) {
                    selectedIndex = index;
                }
                index++;
            }
        }

        @SuppressLint("InflateParams") View spinnerContainer = LayoutInflater.from(this)
                .inflate(R.layout.actionbar_spinner, null);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        assert ab != null;
        ab.setCustomView(spinnerContainer, lp);

        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(mTopLevelSpinnerAdapter);

        spinner.setSelection(selectedIndex);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                MainApp mainApp = (MainApp) getApplicationContext();
                String testType = mTopLevelSpinnerAdapter.getTag(position);
                mainApp.setSwatches(testType);
                ((CalibrateListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.calibrate_list)).setAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        updateActionBarNavigation();
    }

    private void updateActionBarNavigation() {
//        if (index == Config.CALIBRATE_SCREEN_INDEX) {
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayUseLogoEnabled(false);
//        } else {
//            ActionBar ab = getActionBar();
//            assert ab != null;
//            ab.setDisplayShowCustomEnabled(false);
//            ab.setDisplayShowTitleEnabled(true);
//            ab.setDisplayUseLogoEnabled(true);
//        }

//        switch (index) {
//            case Config.SETTINGS_SCREEN_INDEX:
//                setTitle(R.string.settings);
//                break;
//            case Config.HOME_SCREEN_INDEX:
//                setTitle(R.string.appName);
//                break;
//            case Config.SWATCH_SCREEN_INDEX:
//                setTitle(R.string.swatches);
//                break;
//        }
    }

    /**
     * Adapter that provides views for our top-level Action Bar spinner.
     */
    private class ExploreSpinnerAdapter extends BaseAdapter {
        private final ArrayList<ExploreSpinnerItem> mItems = new ArrayList<>();

        private ExploreSpinnerAdapter() {
        }

        public void clear() {
            mItems.clear();
        }

        public void addItem(String tag, String title) {
            mItems.add(new ExploreSpinnerItem(tag, title));
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
                view = getLayoutInflater().inflate(R.layout.actionbar_spinner_item_dropdown,
                        parent, false);
                view.setTag("DROPDOWN");
            }

            TextView normalTextView = (TextView) view.findViewById(R.id.normal_text);

            normalTextView.setVisibility(View.VISIBLE);
            setUpNormalDropdownView(position, normalTextView);

            return view;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
                view = getLayoutInflater().inflate(R.layout.actionbar_spinner_item,
                        parent, false);
                view.setTag("NON_DROPDOWN");
            }
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getTitle(position));
            return view;
        }

        private String getTitle(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position).title : "";
        }

        private String getTag(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position).tag : "";
        }

        private void setUpNormalDropdownView(int position, TextView textView) {
            textView.setText(getTitle(position));
            ShapeDrawable colorDrawable = (ShapeDrawable) textView.getCompoundDrawables()[2];
            if (colorDrawable != null) {
                textView.setCompoundDrawables(null, null, null, null);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
    }

    private class ExploreSpinnerItem {
        final String tag;
        final String title;

        ExploreSpinnerItem(String tag, String title) {
            this.tag = tag;
            this.title = title;
        }
    }
}
