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
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.adapter.CalibrateListAdapter;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ColorUtils;
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
        switch (id) {
            case R.id.action_swatches:
                final Intent intent = new Intent(this, SwatchActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_load:
                Handler.Callback callback = new Handler.Callback() {
                    public boolean handleMessage(Message msg) {
                        CalibrateListAdapter adapter = (CalibrateListAdapter) ((CalibrateListFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R.id.calibrate_list))
                                .getListAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        return true;
                    }
                };
                loadCalibration(callback);
                return true;
            case R.id.menu_save:
                saveCalibration();
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

        try {
            setupActionBarSpinner();
        } catch (Exception ex) {
            AlertUtils.showError(this, R.string.error, getString(R.string.errorLoadingTestTypes), null, R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }, null);

        }
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
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayUseLogoEnabled(false);
    }

    void closeKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    public void saveCalibration() {
        final Context context = this;
        final MainApp mainApp = (MainApp) this.getApplicationContext();

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});

        alertDialogBuilder.setView(input);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setTitle(R.string.saveCalibration);
        alertDialogBuilder.setMessage(R.string.saveProvideFileName);


        alertDialogBuilder.setPositiveButton(R.string.ok, null);
        alertDialogBuilder
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        closeKeyboard(input);
                        dialog.cancel();
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create(); //create the box

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (!input.getText().toString().trim().isEmpty()) {
                            final StringBuilder exportList = new StringBuilder();

                            for (ResultRange range : mainApp.currentTestInfo.getRanges()) {
                                exportList.append(range.getValue() + "=" + ColorUtils.getColorRgbString(range.getColor()));
                                exportList.append('\n');
                            }

                            File external = Environment.getExternalStorageDirectory();
                            final String path = external.getPath() + Config.CALIBRATE_FOLDER_NAME;

                            File file = new File(path + input.getText());
                            if (file.exists()) {
                                AlertUtils.askQuestion(context, R.string.saveConfirmOverwriteFile,
                                        R.string.saveNameAlreadyExists, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                FileUtils.saveToFile(path, input.getText().toString(),
                                                        exportList.toString());
                                            }
                                        }
                                );
                            } else {
                                FileUtils.saveToFile(path, input.getText().toString(),
                                        exportList.toString());
                            }

                            closeKeyboard(input);
                            alertDialog.dismiss();
                        } else {
                            input.setError(getString(R.string.saveInvalidFileName));
                        }
                    }
                });
            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
//                        == EditorInfo.IME_ACTION_DONE)) {
//
//                }
                return false;
            }
        });

        alertDialog.show();
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    public void loadCalibration(final Handler.Callback callback) {
        final Context context = this;
        final MainApp mainApp = (MainApp) this.getApplicationContext();

        try {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
            builderSingle.setIcon(R.mipmap.ic_launcher);
            builderSingle.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                    android.R.layout.select_dialog_singlechoice);

            File external = Environment.getExternalStorageDirectory();
            final String folderName = Config.CALIBRATE_FOLDER_NAME;
            String path = external.getPath() + folderName;
            File folder = new File(path);
            if (folder.exists()) {
                final File[] listFiles = folder.listFiles();
                for (File listFile : listFiles) {
                    arrayAdapter.add(listFile.getName());
                }

                builderSingle.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

                builderSingle.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = listFiles[which].getName();
                                final ArrayList<ResultRange> swatchList = new ArrayList<>();

                                final ArrayList<String> rgbList = FileUtils.loadFromFile(mainApp.currentTestInfo, fileName);
                                if (rgbList != null) {
                                    for (String rgb : rgbList) {
                                        String[] values = rgb.split("=");
                                        ResultRange range = new ResultRange(Double.valueOf(values[0]), ColorUtils.getColorFromRgb(values[1]));
                                        swatchList.add(range);
                                    }
                                    (new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            mainApp.saveCalibratedSwatches(swatchList);
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void result) {
                                            super.onPostExecute(result);
                                            callback.handleMessage(null);
                                        }
                                    }).execute();
                                }
                            }
                        }
                );

                final AlertDialog alert = builderSingle.create();
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final ListView listView = alert.getListView();
                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                final int position = i;

                                AlertUtils.askQuestion(context, R.string.delete, R.string.deleteConfirm, new DialogInterface.OnClickListener() {
                                    @SuppressWarnings("unchecked")
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String fileName = listFiles[position].getName();
                                        FileUtils.deleteFile(folderName, fileName);
                                        ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                        listAdapter.remove(listAdapter.getItem(position));
                                    }
                                });
                                return true;
                            }
                        });

                    }
                });
                alert.show();
            } else {
                AlertUtils.showMessage(context, R.string.notFound, R.string.loadFilesNotAvailable);
            }
        } catch (ActivityNotFoundException ignored) {
        }

        callback.handleMessage(null);

    }

//        public void loadCalibration(final Handler.Callback callback) {
//        final Context context = this;
//        final MainApp mainApp = (MainApp) this.getApplicationContext();
//
//        try {
//            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
//            builderSingle.setIcon(R.mipmap.ic_launcher);
//            builderSingle.setTitle(R.string.loadCalibration);
//
//            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
//                    android.R.layout.select_dialog_singlechoice);
//
//            File external = Environment.getExternalStorageDirectory();
//            final String folderName = Config.CALIBRATE_FOLDER_NAME;
//            String path = external.getPath() + folderName;
//            File folder = new File(path);
//            if (folder.exists()) {
//                final File[] listFiles = folder.listFiles();
//                for (File listFile : listFiles) {
//                    arrayAdapter.add(listFile.getName());
//                }
//
//                builderSingle.setNegativeButton(R.string.cancel,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }
//                );
//
//                builderSingle.setAdapter(arrayAdapter,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                String fileName = listFiles[which].getName();
//                                final ArrayList<Integer> swatchList = new ArrayList<>();
//
//                                final ArrayList<String> rgbList = FileUtils.loadFromFile(fileName);
//                                if (rgbList != null) {
//
//                                    for (String rgb : rgbList) {
//                                        swatchList.add(ColorUtils.getColorFromRgb(rgb));
//                                    }
//                                    (new AsyncTask<Void, Void, Void>() {
//                                        @Override
//                                        protected Void doInBackground(Void... params) {
//                                            mainApp.saveCalibratedSwatches(mainApp.currentTestInfo.getCode(), swatchList);
//
//                                            mainApp.setSwatches(mainApp.currentTestInfo.getCode());
//
//                                            SharedPreferences sharedPreferences = PreferenceManager
//                                                    .getDefaultSharedPreferences(context);
//                                            SharedPreferences.Editor editor = sharedPreferences.edit();
//
//                                            for (int i = 0; i < mainApp.currentTestInfo.getRanges().size(); i++) {
//                                                ColorUtils.autoGenerateColors(mainApp.currentTestInfo, editor);
//                                            }
//                                            editor.apply();
//                                            return null;
//                                        }
//
//                                        @Override
//                                        protected void onPostExecute(Void result) {
//                                            super.onPostExecute(result);
//                                            callback.handleMessage(null);
//                                        }
//                                    }).execute();
//                                }
//                            }
//                        }
//                );
//
//                final AlertDialog alert = builderSingle.create();
//                alert.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialogInterface) {
//                        final ListView listView = alert.getListView();
//                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                            @Override
//                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                                final int position = i;
//
//                                AlertUtils.askQuestion(context, R.string.delete, R.string.deleteConfirm, new DialogInterface.OnClickListener() {
//                                    @SuppressWarnings("unchecked")
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        String fileName = listFiles[position].getName();
//                                        FileUtils.deleteFile(folderName, fileName);
//                                        ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
//                                        listAdapter.remove(listAdapter.getItem(position));
//                                    }
//                                });
//                                return true;
//                            }
//                        });
//
//                    }
//                });
//                alert.show();
//            } else {
//                AlertUtils.showMessage(context, R.string.notFound, R.string.loadFilesNotAvailable);
//            }
//        } catch (ActivityNotFoundException ignored) {
//        }
//
//        callback.handleMessage(null);
//    }

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
