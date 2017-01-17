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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An activity representing a list of Calibrate items.
 * <p/>
 * This activity also implements the required
 * {@link CalibrateListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CalibrateListActivity extends BaseActivity
        implements CalibrateListFragment.Callbacks,
        SaveCalibrationDialogFragment.CalibrationDetailsSavedListener {

    private static final int REQUEST_CALIBRATE = 100;
    private static final int FREEZE_BUTTON_DELAY_MILLIS = 500;
    private FloatingActionButton fabEditCalibration;
    private TextView textSubtitle;
    private TextView textSubtitle1;
    private TextView textSubtitle2;
    private int mPosition;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode()) {
            getMenuInflater().inflate(R.menu.menu_calibrate_dev, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSwatches:
                final Intent intent = new Intent(this, DiagnosticSwatchActivity.class);
                startActivity(intent);
                return true;
            case R.id.menuLoad:
                Handler.Callback callback = new Handler.Callback() {
                    public boolean handleMessage(Message msg) {
                        CalibrateListFragment fragment = (CalibrateListFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R.id.fragmentCalibrateList);
                        fragment.setAdapter();
                        loadDetails();
                        return true;
                    }
                };
                loadCalibration(this, callback);
                return true;
            case R.id.menuSave:
                showEditCalibrationDetailsDialog(false);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_list);

        textSubtitle = (TextView) findViewById(R.id.textSubtitle);
        textSubtitle1 = (TextView) findViewById(R.id.textSubtitle1);
        textSubtitle2 = (TextView) findViewById(R.id.textSubtitle2);

        ((TextView) findViewById(R.id.textTitle)).setText(CaddisflyApp.getApp().
                getCurrentTestInfo().getName());

        fabEditCalibration =
                (FloatingActionButton) findViewById(R.id.fabEditCalibration);

        if (AppPreferences.isDiagnosticMode()) {
            fabEditCalibration.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cyan)));
        }

        fabEditCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditCalibrationDetailsDialog(true);
            }
        });

        loadDetails();
    }

    private void showEditCalibrationDetailsDialog(boolean isEdit) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SaveCalibrationDialogFragment saveCalibrationDialogFragment = SaveCalibrationDialogFragment.newInstance(isEdit);
        saveCalibrationDialogFragment.show(ft, "saveCalibrationDialog");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(R.string.calibrate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fabEditCalibration.setEnabled(true);
    }

    private void loadDetails() {

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        testInfo.setCalibrationDate(PreferencesUtil.getLong(this, testInfo.getId(), R.string.calibrationDateKey));

        if (testInfo.getCalibrationDate() >= 0) {
            textSubtitle1.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
                    .format(new Date(testInfo.getCalibrationDate())));
        }

        testInfo.setExpiryDate(PreferencesUtil.getLong(this, testInfo.getId(), R.string.calibrationExpiryDateKey));

        if (testInfo.getExpiryDate() >= 0) {
            textSubtitle2.setText(String.format("%s: %s", getString(R.string.expires),
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(testInfo.getExpiryDate()))));
        }

        testInfo.setBatchNumber(PreferencesUtil.getString(this, testInfo.getId(), R.string.batchNumberKey, ""));

        textSubtitle.setText(testInfo.getBatchNumber());

        CalibrateListFragment calibrateListFragment = (CalibrateListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCalibrateList);

        if (calibrateListFragment != null) {
            calibrateListFragment.refresh();
        }
    }

    /**
     * Callback method from {@link CalibrateListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {

        TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        fabEditCalibration.setEnabled(false);
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                fabEditCalibration.setEnabled(true);
            }
        }, FREEZE_BUTTON_DELAY_MILLIS);

        //Show edit calibration details dialog if required
        Long expiryDate = PreferencesUtil.getLong(this, currentTestInfo.getId(), R.string.calibrationExpiryDateKey);
        if (expiryDate < Calendar.getInstance().getTimeInMillis()) {
            showEditCalibrationDetailsDialog(true);
            return;
        }

        mPosition = id;
        Swatch swatch = currentTestInfo.getSwatch(mPosition);

        if (AppPreferences.useExternalCamera() || !ApiUtil.isCameraInUse(this, null)) {
            final Intent intent = new Intent(getIntent());
            intent.setClass(getBaseContext(), ColorimetryLiquidActivity.class);
            intent.putExtra("isCalibration", true);
            intent.putExtra("swatchValue", swatch.getValue());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, REQUEST_CALIBRATE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CALIBRATE:

                Swatch swatch = CaddisflyApp.getApp().getCurrentTestInfo().getSwatch(mPosition);

                long calibratedDate = PreferencesUtil.getLong(this,
                        CaddisflyApp.getApp().getCurrentTestInfo().getId(),
                        R.string.calibrationDateKey);

                if (resultCode == Activity.RESULT_OK) {
                    saveCalibratedData(swatch, data.getIntExtra("color", 0));

                    //Save date if this is the first swatch calibrated
                    if (calibratedDate < 0 || SwatchHelper.getCalibratedSwatchCount(
                            CaddisflyApp.getApp().getCurrentTestInfo().getSwatches()) == 1) {
                        PreferencesUtil.setLong(this, CaddisflyApp.getApp().getCurrentTestInfo().getId(),
                                R.string.calibrationDateKey, Calendar.getInstance().getTimeInMillis());
                        loadDetails();
                    }

                    ((CalibrateListFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.fragmentCalibrateList)).setAdapter();
                }

                break;
            default:
                break;
        }
    }

    /**
     * Save a single calibrated color
     *
     * @param swatch      The swatch object
     * @param resultColor The color value
     */
    private void saveCalibratedData(@NonNull Swatch swatch, final int resultColor) {
        String colorKey = String.format(Locale.US, "%s-%.2f",
                CaddisflyApp.getApp().getCurrentTestInfo().getId(), swatch.getValue());

        if (resultColor == 0) {
            PreferencesUtil.removeKey(getApplicationContext(), colorKey);
        } else {
            swatch.setColor(resultColor);
            PreferencesUtil.setInt(getApplicationContext(), colorKey, resultColor);
        }

        String testCode = CaddisflyApp.getApp().getCurrentTestInfo().getId();

        //Save a backup of the calibration details
        final String calibrationDetails = SwatchHelper.generateCalibrationFile(this,
                testCode,
                PreferencesUtil.getString(this, testCode, R.string.batchNumberKey, ""),
                PreferencesUtil.getLong(this, testCode, R.string.calibrationDateKey),
                PreferencesUtil.getLong(this, testCode, R.string.calibrationExpiryDateKey),
                PreferencesUtil.getString(this, testCode, R.string.ledRgbKey, "255 255 255"),
                !PreferencesUtil.getBoolean(this, R.string.noBackdropDetectionKey, false));

        final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, testCode);

        FileUtil.saveToFile(path, "_AutoBackup", calibrationDetails);

        Toast.makeText(this, R.string.calibrated, Toast.LENGTH_LONG).show();
    }

    /**
     * Load the calibrated swatches from the calibration text file
     *
     * @param callback callback to be initiated once the loading is complete
     */
    private void loadCalibration(@NonNull final Context context, @NonNull final Handler.Callback callback) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                    R.layout.row_text);

            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                    CaddisflyApp.getApp().getCurrentTestInfo().getId());

            File[] listFilesTemp = null;
            if (path.exists() && path.isDirectory()) {
                listFilesTemp = path.listFiles();
            }

            final File[] listFiles = listFilesTemp;
            if (listFiles != null && listFiles.length > 0) {
                Arrays.sort(listFiles);

                for (File listFile : listFiles) {
                    arrayAdapter.add(listFile.getName());
                }

                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(@NonNull DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

                builder.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = listFiles[which].getName();
                                try {
                                    final List<Swatch> swatchList = SwatchHelper.loadCalibrationFromFile(getBaseContext(), fileName);

                                    (new AsyncTask<Void, Void, Void>() {
                                        @Nullable
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            SwatchHelper.saveCalibratedSwatches(context, swatchList);
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void result) {
                                            super.onPostExecute(result);
                                            callback.handleMessage(null);
                                        }
                                    }).execute();


                                } catch (Exception ex) {
                                    AlertUtil.showError(context, R.string.error, getString(R.string.errorLoadingFile),
                                            null, R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }, null, null);
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
                                                FileUtil.deleteFile(path, fileName);
                                                ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                                listAdapter.remove(listAdapter.getItem(position));
                                                alertDialog.dismiss();
                                                Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show();
                                            }
                                        }, null);
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

    @Override
    public void onCalibrationDetailsSaved() {
        loadDetails();
    }
}
