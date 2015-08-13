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
import android.graphics.Bitmap;
import android.graphics.Color;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.DataHelper;
import org.akvo.caddisfly.util.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("unused")
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {

    @Test
    public void titleIsCorrect() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        assertTrue(activity.getTitle().toString().equals("Akvo Caddisfly"));
    }

    @Test
    public void testGetPpmValue() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = -1;
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122)));

        ColorInfo photoColor = ColorUtils.getColorFromBitmap(bitmap, AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = DataHelper.analyzeColor(photoColor,
                testInfo.getSwatches(),
                AppConfig.MAX_COLOR_DISTANCE,
                AppConfig.ColorModel.RGB);

        assertEquals(-1.0, resultDetail.getResult());
    }

    @Test
    public void testGetPpmValue2() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            if (i > 1000) {
                colors[i] = Color.rgb(255, 156, 149);
            } else {
                colors[i] = -1;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122)));

        ColorInfo photoColor = ColorUtils.getColorFromBitmap(bitmap, AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = DataHelper.analyzeColor(photoColor,
                testInfo.getSwatches(),
                AppConfig.MAX_COLOR_DISTANCE,
                AppConfig.ColorModel.LAB);

        assertEquals(Color.rgb(255, 156, 149), resultDetail.getColor());
        assertEquals(1.01, resultDetail.getResult());
    }

    @Test
    public void testGetPpmValue3() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            if (i > 1000) {
                colors[i] = Color.rgb(255, 146, 139);
            } else {
                colors[i] = -1;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122)));

        ColorInfo photoColor = ColorUtils.getColorFromBitmap(bitmap, AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = DataHelper.analyzeColor(photoColor,
                testInfo.getSwatches(),
                AppConfig.MAX_COLOR_DISTANCE,
                AppConfig.ColorModel.RGB);

        assertEquals(Color.rgb(255, 146, 139), resultDetail.getColor());
        assertEquals(1.0, resultDetail.getResult());
    }

    @Test
    public void testGetPpmValue4() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(250, 171, 130);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122)));

        ColorInfo photoColor = ColorUtils.getColorFromBitmap(bitmap, AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = DataHelper.analyzeColor(photoColor,
                testInfo.getSwatches(),
                AppConfig.MAX_COLOR_DISTANCE,
                AppConfig.ColorModel.RGB);

        assertEquals(1.5, resultDetail.getResult());
    }

    @Test
    public void testGetPpmValue5() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(254, 115, 138);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 177)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(254, 101, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(254, 115, 138)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(254, 128, 119)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 142, 99)));

        ColorInfo photoColor = ColorUtils.getColorFromBitmap(bitmap, AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = DataHelper.analyzeColor(photoColor,
                testInfo.getSwatches(),
                AppConfig.MAX_COLOR_DISTANCE,
                AppConfig.ColorModel.RGB);

        assertEquals(1.0, resultDetail.getResult());
    }

    @Test
    public void testLoadJson() throws Exception {
        String jsonText = "{\n" +
                "    \"tests\": {\n" +
                "        \"test\": [\n" +
                "            {\n" +
                "                \"name\":[\n" +
                "                            { \"en\": \"Fluoride\"},\n" +
                "                            { \"fr\": \"Fluorure\"},\n" +
                "                            { \"ar\": \"??????\"},\n" +
                "                            { \"hi\": \"????????\"},\n" +
                "                  {\"kn\": \"????????\"}\n" +
                "                        ],\n" +
                "                \"type\": \"0\",\n" +
                "                \"code\": \"fluor\",\n" +
                "                \"unit\": \"ppm\",\n" +
                "                \"ranges\": \"0,.5,1,1.5,2\",\n" +
                "                \"dilutions\":\"0,50,25\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": [\n" +
                "                            { \"en\": \"pH\"}\n" +
                "                        ],\n" +
                "                \"type\": \"0\",\n" +
                "                \"code\": \"phydr\",\n" +
                "                \"unit\": \"pH\",\n" +
                "              \"ranges\": \"3,4,5,6,7,8,9\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        TestInfo testInfo = JsonUtils.loadTestConfigurationByCode(jsonText, "fluor");
        assert testInfo != null;
        assertEquals("FLUOR", testInfo.getCode());
    }

    @Test
    public void testValidateCalibration() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122)));

        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration1() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 175, 123)));

        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testRangeSlope() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 175, 113)));

        assertEquals(31.82420654296875, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration2() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 121)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 175, 123)));

        assertEquals(22.2095458984375, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration3() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 146, 139)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 87, 181)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 124, 157)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122)));

        assertEquals(21.658697509765624, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration4() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 177)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 110, 15)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 139, 137)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(253, 174, 74)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(244, 180, 86)));

        assertEquals(29.022808837890626, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration5() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 110, 15)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 88, 177)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 139, 137)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(253, 174, 74)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(244, 180, 86)));

        assertEquals(17.87760009765625, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration6() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 177)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(254, 101, 157)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(254, 115, 138)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(254, 128, 119)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 142, 99)));

        assertEquals(24.640643310546874, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration7() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 0, 159)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(254, 28, 142)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(254, 56, 126)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(254, 84, 110)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 112, 93)));

        assertEquals(22.0148193359375, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

    @Test
    public void testValidateCalibration8() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(250, 0, 0)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(240, 0, 0)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(225, 0, 0)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(210, 0, 0)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(195, 0, 0)));

        assertEquals(0.0, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }


    @Test
    public void testValidateCalibration9() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 47)));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 60, 37)));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 35, 27)));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(253, 17, 17)));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 0, 0)));

        assertEquals(-5.996826171875, DataHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, DataHelper.validateSwatchList(testInfo.getSwatches()));
    }

}