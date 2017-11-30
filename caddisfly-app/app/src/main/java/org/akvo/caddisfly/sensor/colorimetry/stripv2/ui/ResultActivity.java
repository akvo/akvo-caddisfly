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

package org.akvo.caddisfly.sensor.colorimetry.stripv2.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.DecodeData;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.PatchResult;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.StripTest.Brand.Patch;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.ColorUtils;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.Constants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.ResultUtils;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.MathUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.BitmapUtils.concatTwoBitmaps;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.BitmapUtils.createErrorImage;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.BitmapUtils.createResultImageGroup;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.BitmapUtils.createResultImageSingle;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.BitmapUtils.createValueBitmap;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.ResultUtils.createValueString;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.ResultUtils.createValueUnitString;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.ResultUtils.roundSignificant;

/**
 * Activity that displays the results.
 */
public class ResultActivity extends BaseActivity {
    private static final int IMG_WIDTH = 500;
    private static DecodeData mDecodeData;
    private final SparseArray<String> results = new SparseArray<>();
    private final SparseArray<String> brackets = new SparseArray<>();
    private Button buttonSave;
    private Button buttonCancel;
    private Bitmap totalImage;
    private String totalImageUrl;
    private LinearLayout layout;

    public static void setDecodeData(DecodeData decodeData) {
        mDecodeData = decodeData;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v2activity_result);
        setTitle(R.string.result);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {
            Intent intent = new Intent(getIntent());
            String path;

            if (getIntent().getBooleanExtra(Constants.SEND_IMAGE_IN_RESULT, false) && totalImage != null) {

                // store image on sd card
                path = FileUtil.writeBitmapToExternalStorage(totalImage, "/result-images", totalImageUrl);
                intent.putExtra(SensorConstants.IMAGE, path);

                if (path.length() == 0) {
                    totalImageUrl = "";
                }
            } else {
                totalImageUrl = "";
            }


            TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

            // get information on the strip test from JSON
            StripTest stripTest = new StripTest();
            StripTest.Brand brand = stripTest.getBrand(testInfo.getId());

            // adaptation to avoid strip / strip v2 type problem
            org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest.GroupType grpType;
            StripTest.GroupType groupType = brand.getGroupingType();
            if (groupType.equals(StripTest.GroupType.GROUP)) {
                grpType = org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest.GroupType.GROUP;
            } else {
                grpType = org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest.GroupType.INDIVIDUAL;
            }

            JSONObject resultJsonObj = TestConfigHelper.getJsonResult(testInfo, results, brackets, -1,
                    totalImageUrl, grpType);

            intent.putExtra(SensorConstants.RESPONSE, resultJsonObj.toString());
            setResult(RESULT_OK, intent);
            finish();
        });

        buttonCancel = findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(v -> {
            Intent intent = new Intent(getIntent());
            setResult(RESULT_CANCELED, intent);
            finish();
        });
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        layout = findViewById(R.id.layout_results);
        layout.removeAllViews();

        // get information on the strip test from JSON
        StripTest stripTest = new StripTest();
        Intent intent = getIntent();
        String uuid = intent.getStringExtra(Constants.UUID);
        StripTest.Brand brand = stripTest.getBrand(uuid);

        List<PatchResult> patchResultList = computeResults(brand);
        showResults(patchResultList, brand);
    }

    private List<PatchResult> computeResults(StripTest.Brand brand) {
        totalImageUrl = UUID.randomUUID().toString() + ".png";

        // get the images for the patches
        Map<Integer, float[][][]> patchImageMap = mDecodeData.getStripImageMap();

        // for display purposes sort the patches by physical position on the strip
        List<Patch> patches = brand.getPatchesSortedByPosition();

        // compute XYZ colours of patches and store as lab and xyz
        List<PatchResult> patchResultList = new ArrayList<>();
        for (Patch patch : patches) {
            PatchResult patchResult = new PatchResult(patch.getId());
            int delay = (int) patch.getTimeLapse();

            // check if we have the corresponding image
            if (!patchImageMap.containsKey(delay)) {
                Log.d("Caddisfly", "patch not found!");
                patchResult.setMeasured(false);
                return null;
            }

            float[][][] img = patchImageMap.get(delay);
            // check if the image is not null
            if (img == null) {
                patchResult.setMeasured(false);
                return null;
            }

            patchResult.setImage(img);
            float[] xyz = getPatchColour(patchImageMap.get(delay), patch, brand);
            float[] lab = ColorUtils.XYZtoLAB(xyz);
            patchResult.setXyz(xyz);
            patchResult.setLab(lab);
            patchResult.setPatch(patch);

            // the patches are sorted by position here
            patchResultList.add(patchResult);
        }

        // compare to known colours, using Lab (as this is perceptually uniform)
        // here, we have to distinguish between GROUP and INDIVIDUAL
        float value;
        int index;
        String bracket;

        if (brand.getGroupingType() == StripTest.GroupType.GROUP) {
            float[] result = ResultUtils.calculateResultGroup(patchResultList, patches);
            index = (int) result[0];
            value = result[1];
            bracket = createBracket(result[2], result[3]);

            // apply formula when present
            value = applyFormula(value, patchResultList.get(0));

            patchResultList.get(0).setValue(value);
            patchResultList.get(0).setIndex(index);
            patchResultList.get(0).setBracket(bracket);

            results.put(patchResultList.get(0).getId(),
                    Float.isNaN(value) ? ""
                            : String.valueOf(roundSignificant(value)));
            brackets.put(patchResultList.get(0).getId(), bracket);
        } else {
            for (PatchResult patchResult : patchResultList) {
                // get colours from strip json description for this patch
                JSONArray colors = patchResult.getPatch().getColors();
                float[] result = ResultUtils.calculateResultSingle(patchResult.getLab(), colors);
                index = (int) result[0];
                value = result[1];
                bracket = createBracket(result[2], result[3]);

                // apply formula when present
                value = applyFormula(value, patchResult);

                patchResult.setValue(value);
                patchResult.setIndex(index);
                patchResult.setBracket(bracket);

                // Put the result into results list
                results.put(patchResult.getPatch().getId(),
                        Float.isNaN(value) ? ""
                                : String.valueOf(roundSignificant(value)));
                brackets.put(patchResultList.get(0).getId(), bracket);
            }
        }
        return patchResultList;
    }

    // displays results and creates image to send to database
    // if patchResultList is null, it means that the strip was not found.
    // In that case, we show a default error image.
    private void showResults(List<PatchResult> patchResultList, StripTest.Brand brand) {
        // create empty result image to which everything is concatenated
        totalImage = Bitmap.createBitmap(IMG_WIDTH, 1, Bitmap.Config.ARGB_8888);

        // create and display view with results
        // here, the total result image is also created
        createView(brand, patchResultList);

        // show buttons
        buttonSave.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.VISIBLE);
    }

    private void createView(StripTest.Brand brand, List<PatchResult> patchResultList) {
        // create view in case the strip was not found
        if (patchResultList == null) {
            String patchDescription = "Strip not detected";
            Bitmap resultImage = createErrorImage();
            inflateView(patchDescription, "", resultImage);
        }
        // else create view in case the strip is of type GROUP
        else if (brand.getGroupingType() == StripTest.GroupType.GROUP) {
            PatchResult patchResult = patchResultList.get(0);

            String patchDescription = patchResult.getPatch().getDesc();
            String unit = patchResult.getPatch().getUnit();
            String valueString = createValueUnitString(patchResult.getValue(), unit);

            // create image to display on screen
            Bitmap resultImage = createResultImageGroup(patchResultList);
            inflateView(patchDescription, valueString, resultImage);

            Bitmap valueImage = createValueBitmap(patchResult);
            totalImage = concatTwoBitmaps(valueImage, resultImage);
        } else {
            // create view in case the strip is of type INDIVIDUAL
            for (PatchResult patchResult : patchResultList) {
                // create strings for description, unit, and value
                String patchDescription = patchResult.getPatch().getDesc();
                String unit = patchResult.getPatch().getUnit();
                String valueString = createValueUnitString(patchResult.getValue(), unit);

                // create image to display on screen
                Bitmap resultImage = createResultImageSingle(patchResult, brand);
                inflateView(patchDescription, valueString, resultImage);

                Bitmap valueImage = createValueBitmap(patchResult);
                resultImage = concatTwoBitmaps(valueImage, resultImage);
                totalImage = concatTwoBitmaps(totalImage, resultImage);
            }
        }
    }

    @SuppressLint("InflateParams")
    private void inflateView(String patchDescription, String valueString, Bitmap resultImage) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemResult;
        if (inflater != null) {
            itemResult = (LinearLayout) inflater.inflate(R.layout.v2item_result, null, false);
            TextView textTitle = itemResult.findViewById(R.id.text_title);
            textTitle.setText(patchDescription);

            ImageView imageResult = itemResult.findViewById(R.id.image_result);
            imageResult.setImageBitmap(resultImage);

            TextView textResult = itemResult.findViewById(R.id.text_result);
            textResult.setText(valueString);
            layout.addView(itemResult);
        }
    }

    // measure the average colour of a single patch.
    private float[] getPatchColour(float[][][] img, Patch patch, StripTest.Brand brand) {
        // pixels per mm on the strip image
        double stripRatio = mDecodeData.getStripPixelWidth() / brand.getStripLength();

        double x = patch.getPosition() * stripRatio;
        double y = 0.5 * patch.getWidth() * stripRatio;

        double halfSize = 0.5 * Constants.STRIP_WIDTH_FRACTION * patch.getWidth();
        int tlx = (int) Math.round(x - halfSize);
        int tly = (int) Math.round(y - halfSize);
        int brx = (int) Math.round(x + halfSize);
        int bry = (int) Math.round(y + halfSize);
        int total = 0;

        float X = 0, Y = 0, Z = 0;
        for (int i = tlx; i <= brx; i++) {
            for (int j = tly; j <= bry; j++) {
                X += img[j][i][0];
                Y += img[j][i][1];
                Z += img[j][i][2];
                total++;
            }
        }
        return new float[]{X / total, Y / total, Z / total};
    }

    private float applyFormula(float value, PatchResult patchResult) {
        // if we don't have a valid result, return the value unchanged
        if (value == -1 || Float.isNaN(value)) {
            return value;
        }

        if (!patchResult.getPatch().getFormula().isEmpty()) {
            return (float) MathUtil.eval(String.format(Locale.US,
                    patchResult.getPatch().getFormula(), value));
        }
        // if we didn't have a formula, return the unchanged value.
        return value;
    }

    private String createBracket(float left, float right) {
        return "[" + createValueString(left) + "," + createValueString(right) + "]";
    }
}