/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.adapter.CalibrateListAdapter;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ApiUtils;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.FileUtils;
import org.akvo.caddisfly.util.PreferencesUtils;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * An activity representing a list of Calibrate items.
 * <p/>
 * This activity also implements the required
 * {@link CalibrateListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CalibrateListActivity extends AppCompatActivity
        implements CalibrateListFragment.Callbacks {

    private final int REQUEST_CALIBRATE = 100;
    private int mPosition;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean diagnosticMode = PreferencesUtils.getBoolean(this, R.string.diagnosticModeKey, false);
        if (diagnosticMode) {
            getMenuInflater().inflate(R.menu.menu_calibrate_dev, menu);
        }
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

        ApiUtils.lockScreenOrientation(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        }

        final MainApp mainApp = ((MainApp) getApplicationContext());
        setTitle(mainApp.currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage()));

        if (findViewById(R.id.calibrate_detail_container) != null) {
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((CalibrateListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.calibrate_list))
                    .setActivateOnItemClick(true);
        }

    }

    /**
     * Callback method from {@link CalibrateListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
        mPosition = id;
        final MainApp mainApp = ((MainApp) getApplicationContext());
        ResultRange mRange = mainApp.currentTestInfo.getRange(mPosition);

        final Intent intent = new Intent();
        intent.setClass(this, CameraSensorActivity.class);
        intent.putExtra("isCalibration", true);
        intent.putExtra("rangeValue", mRange.getValue());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_CALIBRATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CALIBRATE:

                final MainApp mainApp = ((MainApp) getApplicationContext());
                ResultRange mRange = mainApp.currentTestInfo.getRange(mPosition);

                if (resultCode == Activity.RESULT_OK) {
                    mainApp.storeCalibratedData(mRange, data.getIntExtra("color", 0));
                }

                ((CalibrateListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.calibrate_list)).setAdapter();
                break;
        }
    }

    private void closeKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    private void saveCalibration() {
        final Context context = this;
        final MainApp mainApp = (MainApp) this.getApplicationContext();

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});

        alertDialogBuilder.setView(input);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setTitle(R.string.saveCalibration);
        alertDialogBuilder.setMessage(R.string.saveProvideFileName);


        alertDialogBuilder.setPositiveButton(R.string.save, null);
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
                                exportList.append(String.format("%.2f", range.getValue()))
                                        .append("=")
                                        .append(ColorUtils.getColorRgbString(range.getColor()));
                                exportList.append('\n');
                            }

                            File external = Environment.getExternalStorageDirectory();
                            final String path = external.getPath() + Config.APP_EXTERNAL_PATH +
                                    File.separator + Config.CALIBRATE_FOLDER_NAME;

                            File file = new File(path, input.getText().toString());
                            if (file.exists()) {
                                AlertUtils.askQuestion(context, R.string.saveConfirmOverwriteFile,
                                        R.string.saveNameAlreadyExists, R.string.overwrite, R.string.cancel,
                                        new DialogInterface.OnClickListener() {
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

        alertDialog.show();
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private double stringToDouble(String s) {

        s = s.replaceAll(",", ".");
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        try {
            return nf.parse(s).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private void loadCalibration(final Handler.Callback callback) {
        final Context context = this;
        final MainApp mainApp = (MainApp) this.getApplicationContext();

        try {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
            builderSingle.setIcon(R.mipmap.ic_launcher);
            builderSingle.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                    android.R.layout.select_dialog_singlechoice);

            File external = Environment.getExternalStorageDirectory();
            final String path = external.getPath() + Config.APP_EXTERNAL_PATH +
                    File.separator + Config.CALIBRATE_FOLDER_NAME;

            File folder = new File(path);


            if (folder.exists()) {
                final File[] listFiles = folder.listFiles();
                Arrays.sort(listFiles);

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

                                ArrayList<String> rgbList;
                                try {
                                    rgbList = FileUtils.loadFromFile(mainApp.currentTestInfo, fileName);

                                    if (rgbList != null) {

                                        for (String rgb : rgbList) {
                                            String[] values = rgb.split("=");
                                            ResultRange range = new ResultRange(stringToDouble(values[0]), ColorUtils.getColorFromRgb(values[1]));
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
                                } catch (Exception ex) {
                                    AlertUtils.showError(context, R.string.error, getString(R.string.errorLoadingFile),
                                            null, R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }, null);

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

                                AlertUtils.askQuestion(context, R.string.delete,
                                        R.string.deleteConfirm, R.string.delete, R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                    @SuppressWarnings("unchecked")
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String fileName = listFiles[position].getName();
                                        FileUtils.deleteFile(path, fileName);
                                        ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                        listAdapter.remove(listAdapter.getItem(position));
                                        alert.dismiss();
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

}
