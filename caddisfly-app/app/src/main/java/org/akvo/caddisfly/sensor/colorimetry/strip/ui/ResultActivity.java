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

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.detect.DetectStripListener;
import org.akvo.caddisfly.sensor.colorimetry.strip.detect.DetectStripTask;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.ColorDetected;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.ResultUtil;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.MathUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

/**
 * Activity that displays the results.
 */
public class ResultActivity extends BaseActivity implements DetectStripListener {

    private static final Scalar GREEN_COLOR = new Scalar(0, 255, 0);
    private static final int MAX_RGB_INT_VALUE = 255;
    private static final double LAB_COLOR_NORMAL_DIVISOR = 2.55;
    private final SparseArray<String> results = new SparseArray<>();
    private Button buttonSave;
    private Button buttonCancel;
    @Nullable
    private Mat resultImage = null;
    private String resultImageUrl;
    private TextView finalResultTextView;
    private StripTest.Brand.Patch finalResultPatch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setTitle(R.string.result);

        buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getIntent());
                String path;

                if (getIntent().getBooleanExtra(Constant.SEND_IMAGE_IN_RESULT, false) && resultImage != null) {

                    // store image on sd card
                    path = FileUtil.writeBitmapToExternalStorage(
                            ResultUtil.makeBitmap(resultImage), "/result-images", resultImageUrl);
                    intent.putExtra(SensorConstants.IMAGE, path);

                    if (path.length() == 0) {
                        resultImageUrl = "";
                    }
                } else {
                    resultImageUrl = "";
                }


                TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

                StripTest stripTest = new StripTest();
                // get information on the strip test from JSON
                StripTest.Brand brand = stripTest.getBrand(testInfo.getId());

                JSONObject resultJsonObj = TestConfigHelper.getJsonResult(testInfo, results, -1,
                        resultImageUrl, brand.getGroupingType());

                intent.putExtra(SensorConstants.RESPONSE, resultJsonObj.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        buttonCancel = (Button) findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getIntent());
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        if (savedInstanceState == null) {
            int format = getIntent().getIntExtra(Constant.FORMAT, 0);
            int width = getIntent().getIntExtra(Constant.WIDTH, 0);
            int height = getIntent().getIntExtra(Constant.HEIGHT, 0);
            Intent detectStripIntent = createDetectStripIntent(format, width, height);

            new DetectStripTask(this).execute(detectStripIntent);
        }
    }

    /**
     * Creates an Intent that holds information about the preview data.
     *
     * @param format the format
     * @param width  the width
     * @param height the height
     * @return the intent
     */
    @NonNull
    private Intent createDetectStripIntent(int format, int width, int height) {
        Intent detectStripIntent = new Intent();

        String uuid = getIntent().getStringExtra(Constant.UUID);

        //put Extras into intent
        detectStripIntent.putExtra(Constant.UUID, uuid);
        detectStripIntent.putExtra(Constant.FORMAT, format);
        detectStripIntent.putExtra(Constant.WIDTH, width);
        detectStripIntent.putExtra(Constant.HEIGHT, height);

        return detectStripIntent;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void showSpinner() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void showResults() {
        resultImageUrl = UUID.randomUUID().toString() + ".png";
        Intent intent = getIntent();
        String uuid = intent.getStringExtra(Constant.UUID);

        Mat strip;
        StripTest stripTest = new StripTest();

        // get information on the strip test from JSON
        StripTest.Brand brand = stripTest.getBrand(uuid);

        // for display purposes sort the patches by position on the strip
        List<StripTest.Brand.Patch> patches = brand.getPatchesSortedByPosition();

        // get the JSON describing the images of the patches that were stored before
        JSONArray imagePatchArray = null;
        try {
            String json = FileUtil.readFromInternalStorage(this, Constant.IMAGE_PATCH);
            if (json != null) {
                imagePatchArray = new JSONArray(json);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }

        // cycle over the patches and interpret them
        if (imagePatchArray != null) {
            // if this strip is of type 'GROUP', take the first image and use that for all the patches
            AtomicInteger workCounter;
            if (brand.getGroupingType() == StripTest.GroupType.GROUP) {
                workCounter = new AtomicInteger(1);
                // handle grouping case
                boolean isInvalidStrip = FileUtil.fileExists(this, Constant.STRIP + "0" + Constant.ERROR);
                strip = ResultUtil.getMatFromFile(this, patches.get(0).getId());
                if (strip != null) {
                    // create empty mat to serve as a template
                    resultImage = new Mat(0, strip.cols(), CvType.CV_8UC3,
                            new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));
                    new BitmapTask(isInvalidStrip, strip, true, brand, patches, 0, workCounter).execute(strip);
                }
            } else {
                workCounter = new AtomicInteger(patches.size());
                // if this strip is of type 'INDIVIDUAL' handle patch by patch
                for (int i = 0; i < patches.size(); i++) { // handle patch
                    // read strip from file
                    strip = ResultUtil.getMatFromFile(this, patches.get(i).getId());

                    if (strip != null) {
                        if (i == 0) {
                            // create empty mat to serve as a template
                            resultImage = new Mat(0, strip.cols(), CvType.CV_8UC3,
                                    new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));
                        }
                        new BitmapTask(false, strip, false, brand, patches, i, workCounter).execute(strip);
                    }
                }
            }
        } else {
            TextView textView = new TextView(this);
            textView.setText(R.string.noData);
            LinearLayout layout = (LinearLayout) findViewById(R.id.layout_results);

            layout.addView(textView);
        }

        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.testProgress).setVisibility(View.GONE);
    }

    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        @Nullable
        @Override
        protected Void doInBackground(Void... params) {
            try {
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.INFO);
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.DATA);
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.STRIP);
                FileUtil.deleteFromInternalStorage(getBaseContext(), Constant.IMAGE_PATCH);
            } catch (IOException e) {
                Timber.e(e);
            }

            return null;
        }
    }

    private class BitmapTask extends AsyncTask<Mat, Void, Void> {
        private static final int MIN_DISPLAY_WIDTH = 420;
        private static final int MAX_DISPLAY_WIDTH = 600;
        private static final int MAT_SIZE_MULTIPLIER = 50;
        private static final int MAX_MAT_SIZE = 150;
        private final Boolean grouped;
        private final StripTest.Brand brand;
        private final List<StripTest.Brand.Patch> patches;
        private final int patchNum;
        private final Mat strip;
        private final AtomicInteger workCounter;
        private String unit;
        private int id;
        private String patchDescription;
        private boolean invalid;
        @Nullable
        private Bitmap stripBitmap = null;
        private Mat combined;
        private Mat resultPatchAreas;
        private ColorDetected colorDetected;
        private ColorDetected[] colorsDetected;
        private double resultValue = -1;

        BitmapTask(boolean invalid, Mat strip, Boolean grouped, StripTest.Brand brand,
                   List<StripTest.Brand.Patch> patches, int patchNum, AtomicInteger workCounter) {
            this.invalid = invalid;
            this.grouped = grouped;
            this.strip = strip;
            this.brand = brand;
            this.patches = patches;
            this.patchNum = patchNum;
            this.workCounter = workCounter;
        }

        @Nullable
        @Override
        protected Void doInBackground(Mat... params) {

            Mat mat = params[0];
            int subMatSize = (int) patches.get(patchNum).getWidth();
            if (mat.empty() || mat.height() < subMatSize) {
                return null;
            }

            patchDescription = patches.get(patchNum).getDesc();

            if (invalid) {
                if (!mat.empty()) {
                    //done with lab schema, make rgb to show in image view
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);
                    stripBitmap = ResultUtil.makeBitmap(mat);
                }
                return null;
            }

            JSONArray colors;
            Point patchCenter = null;
            Mat analyzedArea = null;
            Mat patchArea = null;

            unit = patches.get(patchNum).getUnit();
            id = patches.get(patchNum).getId();

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int resultMatWidth = Math.max(MIN_DISPLAY_WIDTH, Math.min(displayMetrics.widthPixels, MAX_DISPLAY_WIDTH));

            // compute location of point to be sampled
            // depending on the boolean grouped, we either handle all patches at once, or we handle only a single one
            if (grouped) {
                // collect colors
                double ratioW = strip.width() / brand.getStripLength();
                colorsDetected = new ColorDetected[patches.size()];
                double[][] colorsValueLab = new double[patches.size()][3];

                Mat clonedMat = null;
                if (AppPreferences.isDiagnosticMode()) {
                    resultPatchAreas = new Mat(0, Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE),
                            CvType.CV_8UC3, new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));
                    clonedMat = mat.clone();
                    Imgproc.cvtColor(clonedMat, clonedMat, Imgproc.COLOR_Lab2RGB);
                }

                for (int p = 0; p < patches.size(); p++) {
                    double x = patches.get(p).getPosition() * ratioW;
                    double y = strip.height() / 2d;
                    patchCenter = new Point(x, y);

                    if (AppPreferences.isDiagnosticMode()) {

                        Imgproc.rectangle(clonedMat,
                                new Point(patchCenter.x - subMatSize - 1, patchCenter.y - subMatSize - 1),
                                new Point(patchCenter.x + subMatSize, patchCenter.y + subMatSize),
                                GREEN_COLOR, 1, Imgproc.LINE_AA, 0);
                    }

                    colorDetected = ResultUtil.getPatchColor(mat, patchCenter, subMatSize);
                    double[] colorValueLab = colorDetected.getLab().val;

                    colorsDetected[p] = colorDetected;
                    colorsValueLab[p] = colorValueLab;
                }

                if (AppPreferences.isDiagnosticMode() && clonedMat != null) {
                    clonedMat = new Mat(clonedMat, new Rect(0, 0, clonedMat.cols() / 2, clonedMat.rows()));
                    double ratio = (double) (resultMatWidth - 10) / (double) clonedMat.width();
                    Imgproc.resize(clonedMat, clonedMat, new Size(clonedMat.width() * ratio, clonedMat.height() * ratio));
                    resultPatchAreas = ResultUtil.concatenate(resultPatchAreas, clonedMat);
                }

                try {
                    resultValue = ResultUtil.calculateResultGroup(colorsValueLab, patches, id);
                } catch (Exception e) {
                    Timber.e(e);
                    resultValue = Double.NaN;
                }

                // calculate size of each color range block
                // divide the original strip width by the number of colors
                colors = patches.get(0).getColors();

            } else {

                //set the colors needed to calculate resultValue
                colors = patches.get(patchNum).getColors();

                if (colors.length() < 1) {
                    return null;
                }

                double ratioW = strip.width() / brand.getStripLength();
                double x = patches.get(patchNum).getPosition() * ratioW;
                double y = strip.height() / 2d;
                patchCenter = new Point(x, y);

                if (AppPreferences.isDiagnosticMode()) {
                    PatchMat patchMat = new PatchMat(mat, subMatSize, patchCenter).invoke();
                    analyzedArea = patchMat.getAnalyzedArea();
                    patchArea = patchMat.getPatchArea();
                }

                colorDetected = ResultUtil.getPatchColor(mat, patchCenter, subMatSize);
                double[] colorValueLab = colorDetected.getLab().val;

                try {
                    resultValue = ResultUtil.calculateResultSingle(colorValueLab, colors, id);
                } catch (Exception e) {
                    resultValue = Double.NaN;
                }
            }

            if (patchCenter == null) {
                return null;
            } else {
                // Create the image that displays result with other color information
                createResultImage(mat, colors, patchCenter, analyzedArea, patchArea, resultMatWidth);
            }

            // Calculate the true value of results if formula exists
            if (resultValue > -1) {

                try {
                    if (!patches.get(patchNum).getFormula().isEmpty() && !Double.isNaN(resultValue)) {
                        resultValue = MathUtil.eval(String.format(patches.get(patchNum).getFormula(), resultValue));
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }

            } else {
                invalid = true;
            }

            // Put the result into results list
            results.put(patches.get(patchNum).getId(),
                    Double.isNaN(resultValue) ? ""
                            : String.valueOf(ResultUtil.roundSignificant(resultValue)));

            return null;
        }

        private void createResultImage(Mat mat, JSONArray colors, Point patchCenter,
                                       Mat analyzedArea, Mat patchArea, int resultMatWidth) {

            // create Mat to hold strip itself
            mat = ResultUtil.createStripMat(mat, patchCenter, grouped, resultMatWidth);

            // Create Mat to hold patchDescription of patch
            Mat descMat = ResultUtil.createDescriptionMat(patchDescription, resultMatWidth);

            // Create Mat to hold the color range
            Mat colorRangeMat;
            if (grouped) {
                colorRangeMat = ResultUtil.createColorRangeMatGroup(patches, resultMatWidth);
            } else {
                colorRangeMat = ResultUtil.createColorRangeMatSingle(patches.get(patchNum).getColors(),
                        resultMatWidth);
            }

            // create Mat to hold value measured
            Mat valueMeasuredMat;
            if (grouped) {
                valueMeasuredMat = ResultUtil.createValueMeasuredMatGroup(
                        colors, resultValue, colorsDetected, resultMatWidth);
            } else {
                valueMeasuredMat = ResultUtil.createValueMeasuredMatSingle(
                        colors, resultValue, colorDetected, resultMatWidth);
            }

            // PUTTING IT ALL TOGETHER
            // transform all mats to RGB. The strip Mat is already RGB
            Imgproc.cvtColor(descMat, descMat, Imgproc.COLOR_Lab2RGB);
            Imgproc.cvtColor(colorRangeMat, colorRangeMat, Imgproc.COLOR_Lab2RGB);

            // create empty mat to serve as a template
            combined = new Mat(0, resultMatWidth, CvType.CV_8UC3,
                    new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

            combined = ResultUtil.concatenate(combined, mat); // add strip

            // display extracted color only if there was a match
            if (!Double.isNaN(resultValue)) {
                Imgproc.cvtColor(valueMeasuredMat, valueMeasuredMat, Imgproc.COLOR_Lab2RGB);
                combined = ResultUtil.concatenate(combined, valueMeasuredMat); // add measured value
            }

            combined = ResultUtil.concatenate(combined, colorRangeMat); // add color range

            if (AppPreferences.isDiagnosticMode()) {
                combined = ResultUtil.concatenate(combined, resultPatchAreas); // add patch
            }

            Core.copyMakeBorder(combined, combined, 0, 0, 10, 0,
                    Core.BORDER_CONSTANT, new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE,
                            MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

            //make bitmap to be rendered on screen, which doesn't contain the patch patchDescription
            if (!combined.empty()) {
                stripBitmap = ResultUtil.makeBitmap(combined);
            }

            //add patchDescription of patch to combined mat, at the top
            combined = ResultUtil.concatenate(descMat, combined);

            //make bitmap to be sent to server
            if (!combined.empty() && resultImage != null) {
                resultImage = ResultUtil.concatenate(resultImage, combined);
            }

            combined.release();
            mat.release();
            valueMeasuredMat.release();
            colorRangeMat.release();
            if (AppPreferences.isDiagnosticMode()) {
                if (analyzedArea != null) {
                    analyzedArea.release();
                }
                if (patchArea != null) {
                    patchArea.release();
                }
                resultPatchAreas.release();
            }
            descMat.release();
        }

        /**
         * Puts the result on screen.
         * data is taken from the globals stripBitmap, resultValue, colorDetected and unit variables
         */
        protected void onPostExecute(Void result) {
            LayoutInflater inflater = (LayoutInflater) ResultActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final ViewGroup nullParent = null;
            LinearLayout itemResult = (LinearLayout) inflater.inflate(R.layout.item_result, nullParent, false);

            TextView textTitle = (TextView) itemResult.findViewById(R.id.text_title);
            textTitle.setText(patchDescription);

            ImageView imageResult = (ImageView) itemResult.findViewById(R.id.image_result);

            TextView textResult = (TextView) itemResult.findViewById(R.id.text_result);

            if (stripBitmap != null) {
                imageResult.setImageBitmap(stripBitmap);

                if (!invalid) {

                    if (AppPreferences.isDiagnosticMode()) {
                        TextView textColor = (TextView) itemResult.findViewById(R.id.text_color);
                        double[] colorValues = colorDetected.getLab().val;
                        double[] labPoint = new double[]{colorValues[0] / LAB_COLOR_NORMAL_DIVISOR,
                                colorValues[1] - 128, colorValues[2] - 128};

                        String calibrationInfo = PreferencesUtil.getString(getBaseContext(), Constant.CALIBRATION_INFO, "");

                        String distanceInfo = PreferencesUtil.getString(
                                CaddisflyApp.getApp().getApplicationContext(), Constant.DISTANCE_INFO + id, "");

                        textColor.setText(String.format(Locale.US, "Lab: %.2f, %.2f, %.2f%nDistance: %s%n%s%n",
                                labPoint[0], labPoint[1], labPoint[2], distanceInfo, calibrationInfo));
                        textColor.setVisibility(View.VISIBLE);

                        String diagnosticInfo = PreferencesUtil.getString(
                                CaddisflyApp.getApp().getApplicationContext(), Constant.DIAGNOSTIC_INFO, "");

                        String resultJson = String.format(Locale.getDefault(), "\"type\" : \"%s\",", patchDescription);

                        resultJson += String.format(Locale.getDefault(), "\"color\" : \"%s\",", colorDetected.getLab());

                        resultJson += String.format(Locale.getDefault(), "\"result\" : \"%.2f\",", resultValue);

                        resultJson += String.format(Locale.getDefault(), "\"version\" : \"%s\",", CaddisflyApp.getAppVersion());

                        resultJson += String.format(Locale.getDefault(), "\"phone\" : \"%s\",", Build.MODEL);

                        resultJson += String.format(Locale.getDefault(), "\"colorCard\" : \"%s\",",
                                String.valueOf(CalibrationCard.getMostFrequentVersionNumber()));

                        diagnosticInfo = diagnosticInfo.replace("{Result}", resultJson);

                        File path = FileHelper.getFilesDir(FileHelper.FileType.CARD, "");
                        FileUtil.saveToFile(path, new SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.US)
                                        .format(Calendar.getInstance().getTimeInMillis())
                                        + "_" + id + "_" + Build.MODEL.replace("_", "-") + "_CC"
                                        + String.valueOf(CalibrationCard.getMostFrequentVersionNumber()) + ".json",
                                diagnosticInfo);
                    }

                    if (resultValue > -1) {

                        if (resultValue < 1.0) {
                            textResult.setText(String.format(Locale.getDefault(), "%.2f %s", resultValue, unit));
                        } else {
                            textResult.setText(String.format(Locale.getDefault(), "%.1f %s", resultValue, unit));
                        }
                    } else {
                        textResult.setText(R.string.no_result);
                        invalid = true;
                    }
                } else {
                    textResult.setText(R.string.no_result);
                }
            } else {
                if (!patches.get(patchNum).getFormula().isEmpty()) {
                    finalResultPatch = patches.get(patchNum);
                    finalResultTextView = textResult;
                } else {
                    textTitle.append("\n\n" + getResources().getString(R.string.no_data));
                }
                invalid = true;
            }

            LinearLayout layout = (LinearLayout) findViewById(R.id.layout_results);
            layout.addView(itemResult);

            // Job is done, decrement the work counter.
            int tasksLeft = this.workCounter.decrementAndGet();
            // If the count has reached zero, all async tasks have finished.
            if (tasksLeft == 0) {

                if (finalResultTextView != null) {
                    try {
                        // todo: fix the hardcoding of the result indexes
                        if (!finalResultPatch.getFormula().isEmpty()) {

                            resultValue = MathUtil.eval(String.format(finalResultPatch.getFormula(),
                                    Double.parseDouble(results.get(2)), Double.parseDouble(results.get(3))));

                            results.put(1, String.valueOf(ResultUtil.roundSignificant(resultValue)));

                            finalResultTextView.setText(String.format(Locale.getDefault(), "%.2f %s",
                                    resultValue, finalResultPatch.getUnit()));

                        }
                    } catch (Exception e) {
                        // Set all values to null as this result should not be sent to server
                        results.put(2, null);
                        results.put(3, null);
                        finalResultTextView.setText(R.string.no_result);
                        Timber.e(e);
                    }
                }

                boolean resultAvailable = false;
                // check if at least one result was returned
                for (int i = 0; i < results.size(); i++) {
                    int key = results.keyAt(i);

                    String value = results.get(key);
                    resultAvailable = value != null && !value.isEmpty();
                    if (resultAvailable) {
                        break;
                    }
                }

                boolean isInternal = getIntent().getBooleanExtra("internal", false);

                // show the save button only if at least one result is available
                buttonSave.setVisibility(isInternal || !resultAvailable ? View.GONE : View.VISIBLE);
                buttonCancel.setVisibility(isInternal ? View.GONE : View.VISIBLE);

                new DeleteTask().execute();
            }
        }

        private class PatchMat {
            private final Mat mat;
            private final int subMatSize;
            private final Point patchCenter;
            private Mat analyzedArea;
            private Mat patchArea;

            PatchMat(Mat mat, int subMatSize, Point patchCenter) {
                this.mat = mat;
                this.subMatSize = subMatSize;
                this.patchCenter = patchCenter;
            }

            Mat getAnalyzedArea() {
                return analyzedArea;
            }

            Mat getPatchArea() {
                return patchArea;
            }

            PatchMat invoke() {
                resultPatchAreas = new Mat(0, Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE),
                        CvType.CV_8UC3, new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

                Mat clonedMat = mat.clone();
                Imgproc.cvtColor(clonedMat, clonedMat, Imgproc.COLOR_Lab2RGB);
                Imgproc.rectangle(clonedMat,
                        new Point(patchCenter.x - subMatSize - 1, patchCenter.y - subMatSize - 1),
                        new Point(patchCenter.x + subMatSize, patchCenter.y + subMatSize),
                        GREEN_COLOR, 1, Imgproc.LINE_AA, 0);

                patchArea = ResultUtil.getPatch(clonedMat, patchCenter, (strip.height() / 2) + 4);

                Imgproc.resize(patchArea, patchArea,
                        new Size(Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE) + MAT_SIZE_MULTIPLIER,
                                Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE)), 0, 0, INTER_CUBIC);

                analyzedArea = ResultUtil.getPatch(mat, patchCenter, subMatSize);
                Imgproc.resize(analyzedArea, analyzedArea, new Size(Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE),
                        Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE)), 0, 0, INTER_CUBIC);

                Imgproc.cvtColor(analyzedArea, analyzedArea, Imgproc.COLOR_Lab2RGB);

                resultPatchAreas = ResultUtil.concatenate(resultPatchAreas, patchArea);
                resultPatchAreas = ResultUtil.concatenateHorizontal(resultPatchAreas, analyzedArea);
                return this;
            }
        }
    }
}
