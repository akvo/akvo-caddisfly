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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.ColorDetected;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.ResultUtil;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

public class ResultActivity extends BaseActivity {

    private static final int MAX_RGB_INT_VALUE = 255;
    private static final double LAB_COLOR_NORMAL_DIVISOR = 2.55;
    private final JSONObject resultJsonObj = new JSONObject();
    private final JSONArray resultJsonArr = new JSONArray();
    private Button buttonSave;
    private Mat resultImage = null;
    private FileStorage fileStorage;
    private String resultImageUrl;
    private StripTest.Brand brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setTitle(R.string.result);

        if (savedInstanceState == null) {
            resultImageUrl = UUID.randomUUID().toString() + ".png";
            Intent intent = getIntent();
            fileStorage = new FileStorage(this);
            String uuid = intent.getStringExtra(Constant.UUID);

            Mat strip;
            StripTest stripTest = new StripTest();

            // get information on the strip test from JSON
            brand = stripTest.getBrand(this, uuid);
            List<StripTest.Brand.Patch> patches = brand.getPatches();

            // get the JSON describing the images of the patches that were stored before
            JSONArray imagePatchArray = null;
            try {
                String json = fileStorage.readFromInternalStorage(Constant.IMAGE_PATCH + ".txt");
                if (json != null) {
                    imagePatchArray = new JSONArray(json);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // cycle over the patches and interpret them
            if (imagePatchArray != null) {
                // if this strip is of type 'GROUP', take the first image and use that for all the patches
                if (brand.getGroupingType() == StripTest.GroupType.GROUP) {
                    // handle grouping case

                    // get the first patch image
                    JSONArray array;
                    try {
                        // get strip image into Mat object
                        array = imagePatchArray.getJSONArray(0);
                        int imageNo = array.getInt(0);

                        boolean isInvalidStrip = fileStorage.fileExists(Constant.STRIP + imageNo + Constant.ERROR);
                        strip = ResultUtil.getMatFromFile(fileStorage, imageNo);
                        if (strip != null) {
                            // create empty mat to serve as a template
                            resultImage = new Mat(0, strip.cols(), CvType.CV_8UC3,
                                    new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));
                            new BitmapTask(isInvalidStrip, strip, true, brand, patches, 0).execute(strip);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // if this strip is of type 'INDIVIDUAL' handle patch by patch
                    for (int i = 0; i < patches.size(); i++) { // handle patch
                        JSONArray array;
                        try {
                            array = imagePatchArray.getJSONArray(i);

                            // get the image number from the json array
                            int imageNo = array.getInt(0);
                            boolean isInvalidStrip = fileStorage.fileExists(Constant.STRIP + imageNo + Constant.ERROR);

                            // read strip from file
                            strip = ResultUtil.getMatFromFile(fileStorage, imageNo);

                            if (strip != null) {
                                if (i == 0) {
                                    // create empty mat to serve as a template
                                    resultImage = new Mat(0, strip.cols(), CvType.CV_8UC3,
                                            new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));
                                }
                                new BitmapTask(isInvalidStrip, strip, false, brand, patches, i).execute(strip);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText(R.string.noData);
                LinearLayout layout = (LinearLayout) findViewById(R.id.layout_results);

                layout.addView(textView);
            }
        }

        buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "";
                try {
                    // store image on sd card
                    path = FileStorage.writeBitmapToExternalStorage(
                            ResultUtil.makeBitmap(resultImage), "/result-images", resultImageUrl);

                    resultJsonObj.put(SensorConstants.TYPE, SensorConstants.TYPE_NAME);
                    resultJsonObj.put(SensorConstants.NAME, brand.getName());
                    resultJsonObj.put(SensorConstants.UUID, brand.getUuid());
                    if (path.length() > 0) {
                        resultJsonObj.put(SensorConstants.IMAGE, resultImageUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getIntent());
                intent.putExtra(SensorConstants.RESPONSE, resultJsonObj.toString());
                intent.putExtra("image", path);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button buttonCancel = (Button) findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileStorage != null) {
                    try {
                        fileStorage.deleteFromInternalStorage(Constant.INFO);
                        fileStorage.deleteFromInternalStorage(Constant.DATA);
                        fileStorage.deleteFromInternalStorage(Constant.STRIP);
                        fileStorage.deleteFromInternalStorage(Constant.IMAGE_PATCH);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(getIntent());
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        boolean isInternal = getIntent().getBooleanExtra("internal", false);
        buttonSave.setVisibility(isInternal ? View.GONE : View.VISIBLE);
        buttonCancel.setVisibility(isInternal ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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
        private String unit;
        private int id;
        private String patchDescription;
        private boolean invalid;
        private Bitmap stripBitmap = null;
        private Mat combined;
        private Mat resultPatchAreas;
        private ColorDetected colorDetected;
        private ColorDetected[] colorsDetected;
        private double resultValue = -1;

        BitmapTask(boolean invalid, Mat strip, Boolean grouped, StripTest.Brand brand,
                   List<StripTest.Brand.Patch> patches, int patchNum) {
            this.invalid = invalid;
            this.grouped = grouped;
            this.strip = strip;
            this.brand = brand;
            this.patches = patches;
            this.patchNum = patchNum;
        }

        @Override
        protected Void doInBackground(Mat... params) {
            Mat analyzedArea;
            Mat mat = params[0];
            int subMatSize = (int) patches.get(patchNum).getWidth();
            if (mat.empty() || mat.height() < subMatSize) {
                return null;
            }

            // get the name and unit of the patch
            patchDescription = patches.get(patchNum).getDesc();
            unit = patches.get(patchNum).getUnit();

            if (invalid) {
                if (!mat.empty()) {
                    //done with lab schema, make rgb to show in image view
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);
                    stripBitmap = ResultUtil.makeBitmap(mat);
                }
                return null;
            }

            id = patches.get(patchNum).getId();

            // depending on the boolean grouped, we either handle all patches at once, or we handle only a single one

            JSONArray colors;
            Point patchCenter = null;

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int resultMatWidth = Math.max(MIN_DISPLAY_WIDTH, Math.min(displayMetrics.widthPixels, MAX_DISPLAY_WIDTH));

            // compute location of point to be sampled
            Mat patchArea;
            if (grouped) {
                // collect colors
                double ratioW = strip.width() / brand.getStripLength();
                colorsDetected = new ColorDetected[patches.size()];
                double[][] colorsValueLab = new double[patches.size()][3];
                for (int p = 0; p < patches.size(); p++) {
                    double x = patches.get(p).getPosition() * ratioW;
                    double y = strip.height() / 2d;
                    patchCenter = new Point(x, y);

                    colorDetected = ResultUtil.getPatchColor(mat, patchCenter, subMatSize);
                    double[] colorValueLab = colorDetected.getLab().val;

                    colorsDetected[p] = colorDetected;
                    colorsValueLab[p] = colorValueLab;
                }

                resultPatchAreas = new Mat(0, Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE),
                        CvType.CV_8UC3, new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

                patchArea = ResultUtil.getPatch(mat, patchCenter, (strip.height() / 2) + 4);
                Imgproc.resize(patchArea, patchArea,
                        new Size(Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE) + MAT_SIZE_MULTIPLIER,
                                Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE)), 0, 0, INTER_CUBIC);

                analyzedArea = ResultUtil.getPatch(mat, patchCenter, subMatSize);
                Imgproc.resize(analyzedArea, analyzedArea, new Size(Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE),
                        Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE)), 0, 0, INTER_CUBIC);

                Imgproc.cvtColor(analyzedArea, analyzedArea, Imgproc.COLOR_Lab2RGB);
                Imgproc.cvtColor(patchArea, patchArea, Imgproc.COLOR_Lab2RGB);
                resultPatchAreas = ResultUtil.concatenate(resultPatchAreas, patchArea);
                resultPatchAreas = ResultUtil.concatenateHorizontal(resultPatchAreas, analyzedArea);

                try {
                    resultValue = ResultUtil.calculateResultGroup(colorsValueLab, patches);
                } catch (Exception e) {
                    e.printStackTrace();
                    resultValue = Double.NaN;
                }

                // calculate size of each color range block
                // divide the original strip width by the number of colors
                colors = patches.get(0).getColors();

            } else {
                double ratioW = strip.width() / brand.getStripLength();
                double x = patches.get(patchNum).getPosition() * ratioW;
                double y = strip.height() / 2d;
                patchCenter = new Point(x, y);

                resultPatchAreas = new Mat(0, Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE),
                        CvType.CV_8UC3, new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

                patchArea = ResultUtil.getPatch(mat, patchCenter, (strip.height() / 2) + 4);
                Imgproc.cvtColor(patchArea, patchArea, Imgproc.COLOR_Lab2RGB);

                Imgproc.resize(patchArea, patchArea,
                        new Size(Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE) + MAT_SIZE_MULTIPLIER,
                                Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE)), 0, 0, INTER_CUBIC);

                analyzedArea = ResultUtil.getPatch(mat, patchCenter, subMatSize);
                Imgproc.resize(analyzedArea, analyzedArea, new Size(Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE),
                        Math.min(subMatSize * MAT_SIZE_MULTIPLIER, MAX_MAT_SIZE)), 0, 0, INTER_CUBIC);

                Imgproc.cvtColor(analyzedArea, analyzedArea, Imgproc.COLOR_Lab2RGB);

                resultPatchAreas = ResultUtil.concatenate(resultPatchAreas, patchArea);
                resultPatchAreas = ResultUtil.concatenateHorizontal(resultPatchAreas, analyzedArea);

                colorDetected = ResultUtil.getPatchColor(mat, patchCenter, subMatSize);
                double[] colorValueLab = colorDetected.getLab().val;

                //set the colors needed to calculate resultValue
                colors = patches.get(patchNum).getColors();

                try {
                    resultValue = ResultUtil.calculateResultSingle(colorValueLab, colors);
                } catch (Exception e) {
                    resultValue = Double.NaN;
                }
            }

            ////////////// Create Image ////////////////////

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
            Imgproc.cvtColor(valueMeasuredMat, valueMeasuredMat, Imgproc.COLOR_Lab2RGB);

            // create empty mat to serve as a template
            combined = new Mat(0, resultMatWidth, CvType.CV_8UC3,
                    new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

            combined = ResultUtil.concatenate(combined, mat); // add strip
            combined = ResultUtil.concatenate(combined, valueMeasuredMat); // add measured value
            combined = ResultUtil.concatenate(combined, colorRangeMat); // add color range

            Core.copyMakeBorder(combined, combined, 0, 0, 10, 0,
                    Core.BORDER_CONSTANT, new Scalar(MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE,
                            MAX_RGB_INT_VALUE, MAX_RGB_INT_VALUE));

            //make bitmap to be rendered on screen, which doesn't contain the patch patchDescription
            if (!combined.empty()) {
                stripBitmap = ResultUtil.makeBitmap(combined);
            }

            //add patchDescription of patch to combined mat, at the top
            combined = ResultUtil.concatenate(descMat, combined);
//            combined = ResultUtil.concatenate(combined, resultPatchAreas); // add patch

            //make bitmap to be sent to server
            if (!combined.empty()) {
                resultImage = ResultUtil.concatenate(resultImage, combined);
            }

            //put resultValue in resultJsonArr
            if (!combined.empty()) {
                try {
                    JSONObject object = new JSONObject();
                    object.put(SensorConstants.NAME, patchDescription);
                    object.put(SensorConstants.VALUE, ResultUtil.roundSignificant(resultValue));
                    object.put(SensorConstants.UNIT, unit);
                    object.put(SensorConstants.ID, id);
                    resultJsonArr.put(object);
                    resultJsonObj.put(SensorConstants.RESULT, resultJsonArr);

                    //TESTING write image string to external storage
                    //FileStorage.writeLogToSDFile("base64.txt", img, false);
//                    FileStorage.writeLogToSDFile("json.txt", resultJsonObj.toString(), false);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // End making mats to put into image to be send back as an String to server

            combined.release();
            mat.release();
            valueMeasuredMat.release();
            colorRangeMat.release();
            analyzedArea.release();
            patchArea.release();
            resultPatchAreas.release();
            descMat.release();

            return null;
        }

        /*
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

            if (stripBitmap != null) {
                imageResult.setImageBitmap(stripBitmap);
                TextView textResult = (TextView) itemResult.findViewById(R.id.text_result);

                if (!invalid) {

                    if (AppPreferences.isDiagnosticMode()) {
                        TextView textColor = (TextView) itemResult.findViewById(R.id.text_color);
                        double[] colorValues = colorDetected.getLab().val;
                        double[] labPoint = new double[]{colorValues[0] / LAB_COLOR_NORMAL_DIVISOR,
                                colorValues[1] - 128, colorValues[2] - 128};
                        textColor.setText(String.format(Locale.US, "%.2f, %.2f, %.2f", labPoint[0],
                                labPoint[1], labPoint[2]));
                        textColor.setVisibility(View.VISIBLE);
                    }

                    if (resultValue > -1) {
                        if (resultValue < 1.0) {
                            textResult.setText(String.format(Locale.getDefault(), "%.2f %s", resultValue, unit));
                        } else {
                            textResult.setText(String.format(Locale.getDefault(), "%.1f %s", resultValue, unit));
                        }
                    } else {
                        invalid = true;
                    }
                }
            } else {
                textTitle.append("\n\n" + getResources().getString(R.string.no_data));
                invalid = true;
            }

            if (invalid) {
                buttonSave.setVisibility(View.GONE);
            }

            LinearLayout layout = (LinearLayout) findViewById(R.id.layout_results);
            layout.addView(itemResult);
        }
    }
}
