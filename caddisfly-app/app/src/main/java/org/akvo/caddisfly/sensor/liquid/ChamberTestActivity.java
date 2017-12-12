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

package org.akvo.caddisfly.sensor.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.diagnostic.DiagnosticSwatchActivity;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChamberTestActivity extends BaseActivity implements
        BaseRunTest.OnFragmentInteractionListener,
        CalibrationItemFragment.OnListFragmentInteractionListener,
        SaveCalibrationDialogFragment.OnCalibrationDetailsSavedListener,
        SelectDilutionFragment.OnFragmentInteractionListener,
        EditCustomDilution.OnFragmentInteractionListener {

    TestConfigRepository testConfigRepository;

    Fragment selectDilutionFragment;
    RunTest fragment;
    CalibrationItemFragment calibrationItemFragment;
    private FragmentManager fragmentManager;
    private TestInfo mTestInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chamber_test);

        testConfigRepository = new TestConfigRepository();

        fragmentManager = getSupportFragmentManager();

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            mTestInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
            if (mTestInfo == null) {
                finish();
                return;
            }

            List<Calibration> calibrations = CaddisflyApp.getApp().getDB()
                    .calibrationDao().getAll(mTestInfo.getUuid());

            if (calibrations.size() < 1) {
                testConfigRepository.addCalibration(mTestInfo);
                calibrations = CaddisflyApp.getApp().getDB()
                        .calibrationDao().getAll(mTestInfo.getUuid());
            }

            mTestInfo.setCalibrations(calibrations);

            if (mTestInfo.getCameraAbove()) {
                fragment = ChamberBelowFragment.newInstance(mTestInfo, null);
            } else {
                fragment = RunTestFragment.newInstance(mTestInfo, null);
            }

            if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                start();
            } else {
                calibrationItemFragment = CalibrationItemFragment.newInstance(mTestInfo);
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, calibrationItemFragment, this.getLocalClassName()).commit();
            }
        }
    }

    private void start() {
        if (mTestInfo.getDilutions().size() > 0) {
            selectDilutionFragment = SelectDilutionFragment.newInstance(mTestInfo);
            if (calibrationItemFragment != null && calibrationItemFragment.isVisible()) {
                fragmentManager.beginTransaction()
                        .addToBackStack("dilution")
                        .replace(R.id.fragment_container, selectDilutionFragment, this.getLocalClassName()).commit();
            }else{
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, selectDilutionFragment, this.getLocalClassName()).commit();
            }
        } else {
            runTest(1);
        }
    }

    private void runTest(int dilution) {
        fragment.setDilution(dilution);

        if (mTestInfo.getDilutions().size() > 0) {
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, (Fragment) fragment, this.getLocalClassName()).commit();
        }else{
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, (Fragment) fragment, this.getLocalClassName()).commit();
        }
    }

    public void runTestClick(View view) {
        fragment.setCalibration(null);
        start();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.calibrate);
    }

    @Override
    public void onListFragmentInteraction(Calibration item) {

        (new Handler()).postDelayed(() -> {
            fragment.setCalibration(item);
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.fragment_container, (Fragment) fragment, "camera").commit();
        }, 150);
    }

//    @Override
//    public void onFragmentInteraction(ArrayList<ResultDetail> results, Calibration calibration) {
//
//        mTestInfo.Results().get(0).setResult(String.valueOf(results.get(0).getResult()));
//
//        //todo fix this
//        if (calibration == null) {
//            fragmentManager
//                    .beginTransaction()
//                    .replace(R.id.fragment_container,
//                            ResultFragment.newInstance(mTestInfo, "50"), "result").commit();
//        } else {
//
//            CalibrationDao dao = CaddisflyApp.getApp().getDB().calibrationDao();
//            dao.insert(calibration);
//
//            fragmentManager.popBackStackImmediate();
//        }
//    }

    @Override
    public void onBackPressed() {

        if (!fragmentManager.popBackStackImmediate()) {
            super.onBackPressed();
        }
    }


    public void onEditCalibration(View view) {
        showEditCalibrationDetailsDialog(true);
    }

    private void showEditCalibrationDetailsDialog(boolean isEdit) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SaveCalibrationDialogFragment saveCalibrationDialogFragment =
                SaveCalibrationDialogFragment.newInstance(mTestInfo, isEdit);
        saveCalibrationDialogFragment.show(ft, "saveCalibrationDialog");
    }

    @Override
    public void onCalibrationDetailsSaved() {
        loadDetails();
        calibrationItemFragment.loadDetails();
    }

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
                intent.putExtra(ConstantKey.TEST_INFO, mTestInfo);
                startActivity(intent);
                return true;
            case R.id.menuLoad:
                loadCalibration(this);
                loadDetails();
                return true;
            case R.id.menuSave:
                showEditCalibrationDetailsDialog(false);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadDetails() {

        List<Calibration> calibrations = CaddisflyApp.getApp().getDB()
                .calibrationDao().getAll(mTestInfo.getUuid());

        mTestInfo.setCalibrations(calibrations);
        if (calibrationItemFragment != null) {
            calibrationItemFragment.setAdapter(mTestInfo);
        }

        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        model.setTest(mTestInfo);
    }

    /**
     * Load the calibrated swatches from the calibration text file
     */
    private void loadCalibration(@NonNull final Context context) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.row_text);

            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, mTestInfo.getUuid());

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
                        (dialog, which) -> dialog.dismiss()
                );

                builder.setAdapter(arrayAdapter,
                        (dialog, which) -> {
                            String fileName = listFiles[which].getName();
                            try {
                                SwatchHelper.loadCalibrationFromFile(context, mTestInfo, fileName);
                                loadDetails();
                            } catch (Exception ex) {
                                AlertUtil.showError(context, R.string.error, getString(R.string.errorLoadingFile),
                                        null, R.string.ok,
                                        (dialog1, which1) -> dialog1.dismiss(), null, null);
                            }
                        }
                );

                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(dialogInterface -> {
                    final ListView listView = alertDialog.getListView();
                    listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
                        final int position = i;

                        AlertUtil.askQuestion(context, R.string.delete,
                                R.string.deleteConfirm, R.string.delete, R.string.cancel, true,
                                (dialogInterface1, i1) -> {
                                    String fileName = listFiles[position].getName();
                                    FileUtil.deleteFile(path, fileName);
                                    ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                    listAdapter.remove(listAdapter.getItem(position));
                                    alertDialog.dismiss();
                                    Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show();
                                }, null);
                        return true;
                    });

                });
                alertDialog.show();
            } else {
                AlertUtil.showMessage(context, R.string.notFound, R.string.loadFilesNotAvailable);
            }
        } catch (ActivityNotFoundException ignored) {
        }
    }

    @Override
    public void onFragmentInteraction(ArrayList<ResultDetail> resultDetails, Calibration calibration) {

        if (calibration == null) {
            for (Result result : mTestInfo.Results()) {
                ResultDetail resultDetail = resultDetails.get(result.getId() - 1);
                result.setResult(resultDetail.getResult(),
                        resultDetail.getDilution(),
                        mTestInfo.getDilutions().get(mTestInfo.getDilutions().size() - 1));
            }

            fragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container,
                            ResultFragment.newInstance(mTestInfo), "result").commit();
        } else {

            CalibrationDao dao = CaddisflyApp.getApp().getDB().calibrationDao();
            dao.insert(calibration);

            fragmentManager.popBackStackImmediate();
        }
    }

    public void onClickAcceptChamberResult(View view) {

        Intent resultIntent = new Intent(getIntent());
        final SparseArray<String> results = new SparseArray<>();

        for (int i = 0; i < mTestInfo.Results().size(); i++) {
            Result result = mTestInfo.Results().get(i);
            results.put(i + 1, result.getResult());
        }

        JSONObject resultJson = TestConfigHelper.getJsonResult(mTestInfo,
                results, null, -1, "");
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

        // TODO: Remove this when obsolete
        // Backward compatibility. Return plain text result
        resultIntent.putExtra(SensorConstants.RESPONSE_COMPAT, results.get(1));

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    public void onTestWithDilution(View view) {
        if (!fragmentManager.popBackStackImmediate("dilution", 0)) {
            super.onBackPressed();
        }

    }

    @Override
    public void onFragmentInteraction(int dilution) {
        runTest(dilution);
    }

    @Override
    public void onFragmentInteraction(Integer dilution) {
        runTest(dilution);
    }
}
