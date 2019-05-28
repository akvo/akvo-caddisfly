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

package org.akvo.caddisfly.sensor.striptest.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.GroupType;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.models.DecodeData;
import org.akvo.caddisfly.sensor.striptest.models.PatchResult;
import org.akvo.caddisfly.sensor.striptest.utils.ColorUtils;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;
import org.akvo.caddisfly.sensor.striptest.utils.ResultUtils;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.MathUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.concatTwoBitmaps;
import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.createErrorImage;
import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.createResultImageGroup;
import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.createResultImageSingle;
import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.createValueBitmap;
import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueString;
import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueUnitString;
import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.roundSignificant;

/**
 * Activity that displays the results.
 */
public class ResultActivity extends BaseActivity {
    private static final int IMG_WIDTH = 500;
    private static DecodeData mDecodeData;
    private final SparseArray<String> resultStringValues = new SparseArray<>();
    private final SparseArray<String> brackets = new SparseArray<>();
    private Button buttonSave;
    private Bitmap totalImage;
    private String totalImageUrl;
    private LinearLayout layout;
    private TestInfo testInfo;

    public static void setDecodeData(DecodeData decodeData) {
        mDecodeData = decodeData;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_result);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {
            Intent intent = new Intent();
            String path;

            if (totalImage != null) {

                // store image on sd card
                path = FileUtil.writeBitmapToExternalStorage(totalImage,
                        FileHelper.FileType.RESULT_IMAGE, totalImageUrl);

                intent.putExtra(SensorConstants.IMAGE, path);

                if (path.length() == 0) {
                    totalImageUrl = "";
                }
            } else {
                totalImageUrl = "";
            }

            JSONObject resultJsonObj = TestConfigHelper.getJsonResult(this, testInfo,
                    resultStringValues, brackets, totalImageUrl);

            intent.putExtra(SensorConstants.RESPONSE, resultJsonObj.toString());
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setTitle(R.string.result);
    }

    @Override
    public void onResume() {
        super.onResume();
        layout = findViewById(R.id.layout_results);
        layout.removeAllViews();

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        List<PatchResult> patchResultList = computeResults(testInfo);
        showResults(patchResultList, testInfo);
    }

    private List<PatchResult> computeResults(TestInfo testInfo) {
        totalImageUrl = UUID.randomUUID().toString() + ".png";

        // TODO: check null mDecodeData here
        // get the images for the patches
        Map<Integer, float[][][]> patchImageMap = mDecodeData.getStripImageMap();

        // todo check order of patches
        // for display purposes sort the patches by physical position on the strip
        List<Result> results = testInfo.getResults();

        // compute XYZ colours of patches and store as lab and xyz
        List<PatchResult> patchResultList = new ArrayList<>();
        for (Result patch : results) {
            PatchResult patchResult = new PatchResult(patch.getId());
            if (patch.getColors().size() == 0) {
                continue;
            }
            int delay = patch.getTimeDelay();

            // check if we have the corresponding image
            if (!patchImageMap.containsKey(delay)) {
                Timber.d("patch not found!");
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
            float[] xyz = getPatchColour(patchImageMap.get(delay), patch, testInfo);
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

        if (testInfo.getGroupingType() == GroupType.GROUP) {
            float[] result = ResultUtils.calculateResultGroup(patchResultList, testInfo.getResults());
            index = (int) result[0];
            value = result[1];
            bracket = createBracket(result[2], result[3]);

            // apply formula when present
            value = applyFormula(value, patchResultList.get(0));

            patchResultList.get(0).setValue(value);
            patchResultList.get(0).setIndex(index);
            patchResultList.get(0).setBracket(bracket);

            resultStringValues.put(patchResultList.get(0).getId(),
                    Float.isNaN(value) ? ""
                            : String.valueOf(roundSignificant(value)));
            brackets.put(patchResultList.get(0).getId(), bracket);
        } else {
            for (PatchResult patchResult : patchResultList) {
                // get colours from strip json description for this patch
                List<ColorItem> colors = patchResult.getPatch().getColors();
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
                resultStringValues.put(patchResult.getPatch().getId(),
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
    private void showResults(List<PatchResult> patchResultList, TestInfo testInfo) {
        // create empty result image to which everything is concatenated
        totalImage = Bitmap.createBitmap(IMG_WIDTH, 1, Bitmap.Config.ARGB_8888);

        // create and display view with results
        // here, the total result image is also created
        createView(testInfo, patchResultList);

        // show buttons
        buttonSave.setVisibility(View.VISIBLE);
    }

    private void createView(TestInfo testInfo, List<PatchResult> patchResultList) {
        // create view in case the strip was not found
        if (patchResultList == null) {
            String patchDescription = getString(R.string.strip_not_detected);
            Bitmap resultImage = createErrorImage();
            inflateView(patchDescription, "", resultImage);
        }
        // else create view in case the strip is of type GROUP
        else if (testInfo.getGroupingType() == GroupType.GROUP) {

            // handle any calculated result to be displayed
            displayCalculatedResults(testInfo, patchResultList);

            PatchResult patchResult = patchResultList.get(0);

            String patchDescription = patchResult.getPatch().getName();
            String unit = patchResult.getPatch().getUnit();
            String valueString = createValueUnitString(patchResult.getValue(), unit, getString(R.string.no_result));

            // create image to display on screen
            Bitmap resultImage = createResultImageGroup(patchResultList);
            inflateView(patchDescription, valueString, resultImage);

            Bitmap valueImage = createValueBitmap(patchResult, getString(R.string.no_result));
            totalImage = concatTwoBitmaps(valueImage, resultImage);
        } else {
            // create view in case the strip is of type INDIVIDUAL

            // handle any calculated result to be displayed
            displayCalculatedResults(testInfo, patchResultList);

            for (PatchResult patchResult : patchResultList) {
                // create strings for description, unit, and value
                String patchDescription = patchResult.getPatch().getName();
                String unit = patchResult.getPatch().getUnit();
                String valueString = createValueUnitString(patchResult.getValue(), unit, getString(R.string.no_result));

                // create image to display on screen
                Bitmap resultImage = createResultImageSingle(patchResult, testInfo);
                inflateView(patchDescription, valueString, resultImage);

                Bitmap valueImage = createValueBitmap(patchResult, getString(R.string.no_result));
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
            itemResult = (LinearLayout) inflater.inflate(R.layout.item_result, null, false);
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
    private float[] getPatchColour(float[][][] img, Result patch, TestInfo brand) {
        // pixels per mm on the strip image
        double stripRatio = mDecodeData.getStripPixelWidth() / brand.getStripLength();

        double x = patch.getPatchPos() * stripRatio;
        double y = 0.5 * patch.getPatchWidth() * stripRatio;

        double halfSize = 0.5 * Constants.STRIP_WIDTH_FRACTION * patch.getPatchWidth();
        int tlx = (int) Math.round(x - halfSize);
        int tly = (int) Math.round(y - halfSize);
        int brx = (int) Math.round(x + halfSize);
        int bry = (int) Math.round(y + halfSize);
        int total = 0;

        float X = 0;
        float Y = 0;
        float Z = 0;
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

    /**
     * When result item does not have a patch but exists solely to display a calculated result
     *
     * @param testInfo        - the test info
     * @param patchResultList - results for the patches
     */
    private void displayCalculatedResults(TestInfo testInfo, List<PatchResult> patchResultList) {
        // if testInfo has a displayResult items
        if (testInfo.getResults() != null) {
            for (Result displayResult : testInfo.getResults()) {

                // if displayResult formula exists then calculate and display
                if (displayResult.getColors().size() == 0 && !displayResult.getFormula().isEmpty()) {
                    String patchDescription = displayResult.getName();
                    String unit = displayResult.getUnit();

                    Object[] results;
                    if (testInfo.getGroupingType() == GroupType.GROUP) {
                        results = new Object[1];
                        results[0] = patchResultList.get(0).getValue();
                    } else {

                        // get all the other results for this test
                        results = new Object[patchResultList.size()];
                        for (int i = 0; i < patchResultList.size(); i++) {
                            results[i] = patchResultList.get(i).getValue();
                        }
                    }

                    double calculatedResult;
                    try {
                        calculatedResult = MathUtil.eval(String.format(Locale.US,
                                displayResult.getFormula(), results));
                    } catch (Exception e) {
                        inflateView(patchDescription, createValueUnitString(
                                -1, unit, getString(R.string.no_result)), null);
                        return;
                    }

                    resultStringValues.put(displayResult.getId(), String.valueOf(calculatedResult));

                    inflateView(patchDescription, createValueUnitString(
                            (float) calculatedResult, unit, getString(R.string.no_result)), null);
                }
            }
        }
    }

    private String createBracket(float left, float right) {
        return "[" + createValueString(left) + "," + createValueString(right) + "]";
    }
}