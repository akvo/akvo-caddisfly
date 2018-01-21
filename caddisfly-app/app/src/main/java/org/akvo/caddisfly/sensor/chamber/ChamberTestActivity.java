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

package org.akvo.caddisfly.sensor.chamber;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.diagnostic.DiagnosticResultDialog;
import org.akvo.caddisfly.diagnostic.DiagnosticSwatchActivity;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class ChamberTestActivity extends BaseActivity implements
        BaseRunTest.OnResultListener,
        CalibrationItemFragment.OnCalibrationSelectedListener,
        SaveCalibrationDialogFragment.OnCalibrationDetailsSavedListener,
        SelectDilutionFragment.OnDilutionSelectedListener,
        EditCustomDilution.OnCustomDilutionListener {

    private static final String TWO_SENTENCE_FORMAT = "%s%n%n%s";

    private RunTest runTestFragment;
    private CalibrationItemFragment calibrationItemFragment;
    private FragmentManager fragmentManager;
    private TestInfo testInfo;
    private boolean cameraIsOk = false;
    private int currentDilution = 1;
    private Bitmap mCroppedBitmap;
    private SoundPoolPlayer sound;
    private AlertDialog alertDialogToBeDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chamber_test);

        sound = new SoundPoolPlayer(this);

        fragmentManager = getSupportFragmentManager();

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
            if (testInfo == null) {
                finish();
                return;
            }

            if (testInfo.getCameraAbove()) {
                runTestFragment = ChamberBelowFragment.newInstance(testInfo);
            } else {
                runTestFragment = ChamberAboveFragment.newInstance(testInfo);
            }

            if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                start();
            } else {
                calibrationItemFragment = CalibrationItemFragment.newInstance(testInfo);
                goToFragment(calibrationItemFragment);
            }
        }
    }

    private void goToFragment(Fragment fragment) {
        if (fragmentManager.getFragments().size() > 0) {
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, fragment).commit();
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment).commit();
        }
    }

    private void start() {

        if (testInfo.getDilutions().size() > 0) {
            Fragment selectDilutionFragment = SelectDilutionFragment.newInstance(testInfo);
            goToFragment(selectDilutionFragment);
        } else {
            runTest();
        }

        invalidateOptionsMenu();

    }

    private void runTest() {
        if (cameraIsOk) {
            runTestFragment.setDilution(currentDilution);
            goToFragment((Fragment) runTestFragment);
        } else {
            checkCameraMegaPixel();
        }

        invalidateOptionsMenu();
    }

    @SuppressWarnings("unused")
    public void runTestClick(View view) {
        runTestFragment.setCalibration(null);
        start();
    }

    @Override
    public void onCalibrationSelected(Calibration item) {

        CalibrationDetail calibrationDetail = CaddisflyApp.getApp().getDb()
                .calibrationDao().getCalibrationDetails(testInfo.getUuid());

        if (calibrationDetail == null) {
            showEditCalibrationDetailsDialog(true);
        } else {
            long milliseconds = calibrationDetail.expiry;
            //Show edit calibration details dialog if required
            if (milliseconds <= new Date().getTime()) {
                showEditCalibrationDetailsDialog(true);
            } else {
                (new Handler()).postDelayed(() -> {
                    runTestFragment.setCalibration(item);
                    runTest();
                    invalidateOptionsMenu();
                }, 150);
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.calibrate);
    }

    @Override
    protected void onDestroy() {
        sound.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (!fragmentManager.popBackStackImmediate()) {
            super.onBackPressed();
        }

        invalidateOptionsMenu();
    }

    @SuppressWarnings("unused")
    public void onEditCalibration(View view) {
        showEditCalibrationDetailsDialog(true);
    }

    private void showEditCalibrationDetailsDialog(boolean isEdit) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SaveCalibrationDialogFragment saveCalibrationDialogFragment =
                SaveCalibrationDialogFragment.newInstance(testInfo, isEdit);
        saveCalibrationDialogFragment.show(ft, "saveCalibrationDialog");
    }

    @Override
    public void onCalibrationDetailsSaved() {
        loadDetails();
        calibrationItemFragment.loadDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode()
                && (calibrationItemFragment != null && calibrationItemFragment.isVisible())) {
            getMenuInflater().inflate(R.menu.menu_calibrate_dev, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSwatches:
                final Intent intent = new Intent(this, DiagnosticSwatchActivity.class);
                intent.putExtra(ConstantKey.TEST_INFO, testInfo);
                startActivity(intent);
                return true;
            case R.id.menuLoad:
                loadCalibrationFromFile(this);
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

        List<Calibration> calibrations = CaddisflyApp.getApp().getDb()
                .calibrationDao().getAll(testInfo.getUuid());

        testInfo.setCalibrations(calibrations);
        if (calibrationItemFragment != null) {
            calibrationItemFragment.setAdapter(testInfo);
        }

        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        model.setTest(testInfo);
    }

    /**
     * Load the calibrated swatches from the calibration text file.
     */
    private void loadCalibrationFromFile(@NonNull final Context context) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.row_text);

            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, testInfo.getUuid());

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
                                SwatchHelper.loadCalibrationFromFile(testInfo, fileName);
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
                                    //noinspection unchecked
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
            // do nothing
        }
    }

    @Override
    public void onResult(ArrayList<ResultDetail> resultDetails, Calibration calibration) {

        if (calibration == null) {

            int dilution = resultDetails.get(0).getDilution();

            double value = SwatchHelper.getAverageResult(resultDetails);

            if (value > -1) {

                Result result = testInfo.getResults().get(0);
                result.setResult(value, dilution, testInfo.getMaxDilution());

                if (result.highLevelsFound() && testInfo.getDilution() != testInfo.getMaxDilution()) {
                    sound.playShortResource(R.raw.beep_long);
                } else {
                    sound.playShortResource(R.raw.done);
                }

                fragmentManager.popBackStack();
                fragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container,
                                ResultFragment.newInstance(testInfo), null).commit();

                mCroppedBitmap = resultDetails.get(0).getBitmap();

                if (AppPreferences.isDiagnosticMode()) {
                    showDiagnosticResultDialog(false, result, resultDetails, false, 0);
                }

            } else {

                if (AppPreferences.isDiagnosticMode()) {
                    sound.playShortResource(R.raw.err);

                    releaseResources();

                    setResult(Activity.RESULT_CANCELED);

                    fragmentManager.popBackStack();

                    showDiagnosticResultDialog(true, new Result(), resultDetails, false, 0);

                } else {

                    showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestFailed),
                            getString(R.string.checkChamberPlacement)),
                            resultDetails.get(resultDetails.size() - 1).getBitmap());
                }
            }

        } else {

            int color = SwatchHelper.getAverageColor(resultDetails);

            if (color == Color.TRANSPARENT) {

                if (AppPreferences.isDiagnosticMode()) {
                    showDiagnosticResultDialog(false, new Result(), resultDetails, true, color);
                }

                showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.couldNotCalibrate),
                        getString(R.string.checkChamberPlacement)),
                        resultDetails.get(resultDetails.size() - 1).getBitmap());
            } else {

                CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();
                calibration.color = color;
                calibration.date = new Date().getTime();
                dao.insert(calibration);
                CalibrationFile.saveCalibratedData(this, testInfo, calibration, color);
                loadDetails();

                sound.playShortResource(R.raw.done);

                if (AppPreferences.isDiagnosticMode()) {
                    showDiagnosticResultDialog(false, new Result(), resultDetails, true, color);
                }
            }
            fragmentManager.popBackStackImmediate();
        }
    }

    /**
     * In diagnostic mode show the diagnostic results dialog.
     *
     * @param testFailed    if test has failed then dialog knows to show the retry button
     * @param result        the result shown to the user
     * @param resultDetails the result details
     * @param isCalibration is this a calibration result
     * @param color         the matched color
     */
    private void showDiagnosticResultDialog(boolean testFailed, Result result,
                                            ArrayList<ResultDetail> resultDetails, boolean isCalibration, int color) {
        DialogFragment resultFragment = DiagnosticResultDialog.newInstance(
                testFailed, result, resultDetails, isCalibration, color);
        final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();

        android.app.Fragment prev = getFragmentManager().findFragmentByTag("gridDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        resultFragment.setCancelable(false);
        resultFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        resultFragment.show(ft, "gridDialog");
    }

    /**
     * Create result json to send back.
     */
    @SuppressWarnings("unused")
    public void onClickAcceptChamberResult(View view) {

        Intent resultIntent = new Intent(getIntent());
        final SparseArray<String> results = new SparseArray<>();

        for (int i = 0; i < testInfo.getResults().size(); i++) {
            Result result = testInfo.getResults().get(i);
            results.put(i + 1, result.getResult());
        }

        // Save photo taken during the test
        String resultImageUrl = UUID.randomUUID().toString() + ".png";
        String path = FileUtil.writeBitmapToExternalStorage(mCroppedBitmap, "/result-images", resultImageUrl);
        resultIntent.putExtra(ConstantKey.IMAGE, path);

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo,
                results, null, -1, resultImageUrl);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

        // TODO: Remove this when obsolete
        // Backward compatibility. Return plain text result
        resultIntent.putExtra(SensorConstants.RESPONSE_COMPAT, results.get(1));

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    private void showError(String message, final Bitmap bitmap) {

        sound.playShortResource(R.raw.err);

        releaseResources();

        alertDialogToBeDestroyed = AlertUtil.showError(this, R.string.error, message, bitmap, R.string.retry,
                (dialogInterface, i) -> {
                    if (getIntent().getBooleanExtra(ConstantKey.RUN_TEST, false)) {
                        start();
                    } else {
                        runTest();
                    }
                },
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    releaseResources();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }, null
        );
    }

    private void releaseResources() {
        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed.dismiss();
        }
    }

    /**
     * Navigate back to the dilution selection screen if re-testing.
     */
    @SuppressWarnings("unused")
    public void onTestWithDilution(View view) {
        if (!fragmentManager.popBackStackImmediate("dilution", 0)) {
            super.onBackPressed();
        }
    }

    @Override
    public void onDilutionSelected(int dilution) {
        currentDilution = dilution;
        runTest();
    }

    @Override
    public void onCustomDilution(Integer dilution) {
        currentDilution = dilution;
        runTest();
    }

    private void checkCameraMegaPixel() {

        cameraIsOk = true;
        if (PreferencesUtil.getBoolean(this, R.string.showMinMegaPixelDialogKey, true)) {
            try {

                if (CameraHelper.getMaxSupportedMegaPixelsByCamera(this) < Constants.MIN_CAMERA_MEGA_PIXELS) {

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    View checkBoxView = View.inflate(this, R.layout.dialog_message, null);
                    CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                            -> PreferencesUtil.setBoolean(getBaseContext(),
                            R.string.showMinMegaPixelDialogKey, !isChecked));

                    android.support.v7.app.AlertDialog.Builder builder
                            = new android.support.v7.app.AlertDialog.Builder(this);

                    builder.setTitle(R.string.warning);
                    builder.setMessage(R.string.camera_not_good)
                            .setView(checkBoxView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_anyway, (dialog, id) -> runTest())
                            .setNegativeButton(R.string.stop_test, (dialog, id) -> {
                                dialog.dismiss();
                                cameraIsOk = false;
                                finish();
                            }).show();

                } else {
                    runTest();
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        } else {
            runTest();
        }
    }

}
