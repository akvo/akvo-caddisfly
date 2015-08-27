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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

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
public class CalibrateListActivity extends BaseActivity
        implements CalibrateListFragment.Callbacks {

    private final int REQUEST_CALIBRATE = 100;
    private int mPosition;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode(this)) {
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
                final Intent intent = new Intent(this, DiagnosticSwatchActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_load:
                Handler.Callback callback = new Handler.Callback() {
                    public boolean handleMessage(Message msg) {
                        CalibrateListFragment fragment = (CalibrateListFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R.id.fragmentCalibrateList);
                        fragment.setAdapter();
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

        setTitle(CaddisflyApp.getApp().currentTestInfo.getName(
                getResources().getConfiguration().locale.getLanguage()));

    }

    /**
     * Callback method from {@link CalibrateListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
        mPosition = id;
        Swatch swatch = CaddisflyApp.getApp().currentTestInfo.getSwatch(mPosition);

        final Intent intent = new Intent();
        intent.setClass(this, ColorimetryLiquidActivity.class);
        intent.putExtra("isCalibration", true);
        intent.putExtra("swatchValue", swatch.getValue());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_CALIBRATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CALIBRATE:

                Swatch swatch = CaddisflyApp.getApp().currentTestInfo.getSwatch(mPosition);

                if (resultCode == Activity.RESULT_OK) {
                    saveCalibratedData(swatch, data.getIntExtra("color", 0));
                }

                ((CalibrateListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentCalibrateList)).setAdapter();
                break;
        }
    }

    /**
     * Save a single calibrated color
     *
     * @param swatch      The swatch object
     * @param resultColor The color value
     */
    private void saveCalibratedData(Swatch swatch, final int resultColor) {
        String colorKey = String.format(Locale.US, "%s-%.2f",
                CaddisflyApp.getApp().currentTestInfo.getCode(), swatch.getValue());

        if (resultColor == 0) {
            PreferencesUtil.removeKey(getApplicationContext(), colorKey);
        } else {
            swatch.setColor(resultColor);
            PreferencesUtil.setInt(getApplicationContext(), colorKey, resultColor);
        }
    }

    /**
     * Save the current calibration to a text file
     */
    private void saveCalibration() {

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        SaveCalibrationDialogFragment saveCalibrationDialogFragment = SaveCalibrationDialogFragment.newInstance();

        saveCalibrationDialogFragment.show(ft, "saveCalibrationDialog");
    }

    /**
     * Convert a string number into a double value
     *
     * @param text the text to be converted to number
     * @return the double value
     */
    private double stringToDouble(String text) {

        text = text.replaceAll(",", ".");
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        try {
            return nf.parse(text).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Save a list of calibrated colors
     *
     * @param swatches List of swatch colors to be saved
     */
    private void saveCalibratedSwatches(ArrayList<Swatch> swatches) {

        CaddisflyApp caddisflyApp = CaddisflyApp.getApp();
        for (Swatch swatch : swatches) {
            String key = String.format(Locale.US, "%s-%.2f",
                    caddisflyApp.currentTestInfo.getCode(), swatch.getValue());

            PreferencesUtil.setInt(this, key, swatch.getColor());
        }

        caddisflyApp.loadCalibratedSwatches(caddisflyApp.currentTestInfo);
    }

    /**
     * Load the calibrated swatches from the calibration text file
     *
     * @param callback callback to be initiated once the loading is complete
     */
    private void loadCalibration(final Handler.Callback callback) {
        final Context context = this;
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                    R.layout.row_text);

            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION);

            final File subPath = new File(path, CaddisflyApp.getApp().currentTestInfo.getCode());

            if (subPath.exists() && subPath.isDirectory()) {
                final File[] listFiles = subPath.listFiles();
                Arrays.sort(listFiles);

                for (File listFile : listFiles) {
                    arrayAdapter.add(listFile.getName());
                }

                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

                builder.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = listFiles[which].getName();
                                final ArrayList<Swatch> swatchList = new ArrayList<>();

                                ArrayList<String> calibrationDetails;
                                try {
                                    calibrationDetails = FileUtil.loadFromFile(subPath, fileName);

                                    if (calibrationDetails != null) {

                                        for (int i = calibrationDetails.size() - 1; i >= 0; i--) {
                                            if (!calibrationDetails.get(i).contains("=")) {
                                                calibrationDetails.remove(i);
                                            }
                                        }

                                        for (String rgb : calibrationDetails) {
                                            String[] values = rgb.split("=");
                                            Swatch swatch = new Swatch(stringToDouble(values[0]),
                                                    ColorUtil.getColorFromRgb(values[1]));
                                            swatchList.add(swatch);
                                        }

                                        Toast.makeText(getBaseContext(),
                                                String.format(getString(R.string.calibrationLoaded), fileName),
                                                Toast.LENGTH_SHORT).show();

                                        (new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                saveCalibratedSwatches(swatchList);
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
                                    AlertUtil.showError(context, R.string.error, getString(R.string.errorLoadingFile),
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

                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final ListView listView = alertDialog.getListView();
                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                final int position = i;

                                AlertUtil.askQuestion(context, R.string.delete,
                                        R.string.deleteConfirm, R.string.delete, R.string.cancel, true,
                                        new DialogInterface.OnClickListener() {
                                            @SuppressWarnings("unchecked")
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String fileName = listFiles[position].getName();
                                                FileUtil.deleteFile(subPath, fileName);
                                                ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                                listAdapter.remove(listAdapter.getItem(position));
                                                alertDialog.dismiss();
                                                Toast.makeText(getBaseContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                return true;
                            }
                        });

                    }
                });
                alertDialog.show();
            } else {
                AlertUtil.showMessage(context, R.string.notFound, R.string.loadFilesNotAvailable);
            }
        } catch (ActivityNotFoundException ignored) {
        }

        callback.handleMessage(null);
    }

}
