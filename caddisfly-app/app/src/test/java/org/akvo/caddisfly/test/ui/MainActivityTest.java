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

package org.akvo.caddisfly.test.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.ColorUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("unused")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class MainActivityTest {

    @Test
    public void titleIsCorrect() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        assertTrue(activity.getTitle().toString().equals("Akvo Caddisfly"));
    }

    @Test
    public void testGetResultValue() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = -1;
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122), Color.TRANSPARENT));

        ColorInfo photoColor = ColorUtil.getColorFromBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = SwatchHelper.analyzeColor(5, photoColor,
                testInfo.getSwatches(),
                ColorUtil.ColorModel.RGB);

        assertEquals(-1.0, resultDetail.getResult());
    }

    @Test
    public void testGetResultValue2() throws Exception {
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
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122), Color.TRANSPARENT));

        ColorInfo photoColor = ColorUtil.getColorFromBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = SwatchHelper.analyzeColor(5, photoColor,
                testInfo.getSwatches(),
                ColorUtil.DEFAULT_COLOR_MODEL);

        assertEquals(Color.rgb(255, 156, 149), resultDetail.getColor());
        assertEquals(1.1, resultDetail.getResult());
    }

    @Test
    public void testGetResultValue3() throws Exception {
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
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122), Color.TRANSPARENT));

        ColorInfo photoColor = ColorUtil.getColorFromBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = SwatchHelper.analyzeColor(5, photoColor,
                testInfo.getSwatches(),
                ColorUtil.ColorModel.RGB);

        assertEquals(Color.rgb(255, 146, 139), resultDetail.getColor());
        assertEquals(1.0, resultDetail.getResult());
    }

    @Test
    public void testGetResultValue4() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(250, 171, 130);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122), Color.TRANSPARENT));

        ColorInfo photoColor = ColorUtil.getColorFromBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = SwatchHelper.analyzeColor(5, photoColor,
                testInfo.getSwatches(),
                ColorUtil.ColorModel.RGB);

        assertEquals(1.5, resultDetail.getResult());
    }

    @Test
    public void testGetResultValue5() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(254, 115, 138);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 177), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(254, 101, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(254, 115, 138), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(254, 128, 119), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 142, 99), Color.TRANSPARENT));

        ColorInfo photoColor = ColorUtil.getColorFromBitmap(bitmap, ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = SwatchHelper.analyzeColor(5, photoColor,
                testInfo.getSwatches(),
                ColorUtil.ColorModel.RGB);

        assertEquals(1.0, resultDetail.getResult());
    }

    @Test
    public void testLoadJson() throws Exception {
        String jsonText = "{\n" +
                "  \"tests\": [\n" +
                "    {\n" +
                "      \"name\": [\n" +
                "        {\n" +
                "          \"en\": \"Fluoride\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"fr\": \"Fluorure\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"subtype\": \"color\",\n" +
                "      \"uuid\": [\n" +
                "        \"f0f3c1dd-89af-49f1-83e7-bcc31c3006cf\"\n" +
                "      ],\n" +
                "      \"calibrate\": \"true\",\n" +
                "      \"ranges\": \"0,0.5,1,1.5,2\",\n" +
                "      \"defaultColors\": \"FD13AB,FE217C,F53A48,E75D28,D36B0B\",\n" +
                "      \"dilutions\": \"0,50,20\",\n" +
                "      \"monthsValid\": \"12\",\n" +
                "      \"results\": [\n" +
                "        {\n" +
                "          \"description\": \"Fluoride\",\n" +
                "          \"id\": 1,\n" +
                "          \"unit\": \"ppm\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": [\n" +
                "        {\n" +
                "          \"en\": \"Electrical Conductivity\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"fr\": \"Conductivité Electrique\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"subtype\": \"sensor\",\n" +
                "      \"uuid\": [\n" +
                "        \"f88237b7-be3d-4fac-bbee-ab328eefcd14\"\n" +
                "      ],\n" +
                "      \"code\": \"econd\",\n" +
                "      \"calibrate\": \"true\",\n" +
                "      \"ranges\": \"0\",\n" +
                "      \"results\": [\n" +
                "        {\n" +
                "          \"description\": \"Electrical Conductivity\",\n" +
                "          \"id\": 1,\n" +
                "          \"unit\": \"μS/cm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"description\": \"Temperature\",\n" +
                "          \"id\": 2,\n" +
                "          \"unit\": \"°Celsius\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": [\n" +
                "        {\n" +
                "          \"en\": \"Temperature\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"fr\": \"Température\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"subtype\": \"sensor\",\n" +
                "      \"uuid\": [\n" +
                "        \"d40d013a-10b1-44c4-bcea-02f8f7024105\"\n" +
                "      ],\n" +
                "      \"code\": \"tempe\",\n" +
                "      \"calibrate\": \"false\",\n" +
                "      \"ranges\": \"0\",\n" +
                "      \"results\": [\n" +
                "        {\n" +
                "          \"description\": \"Temperature\",\n" +
                "          \"id\": 1,\n" +
                "          \"unit\": \"°Celsius\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": [\n" +
                "        {\n" +
                "          \"en\": \"Strip Tests\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"subtype\": \"striptest\",\n" +
                "      \"uuid\": [\n" +
                "        \"bf1c19c0-9788-4e26-999e-1b5c6ca28111\",\n" +
                "        \"c801b70c-39e4-493a-b20e-6843158b47b4\",\n" +
                "        \"459a42d9-0834-4656-9e5a-e3bde46bdcff\",\n" +
                "        \"debbbf5e-fe0c-4b9c-be1f-6ed8142b6b07\",\n" +
                "        \"f4c57d34-dd17-4750-9fcd-fe26af2621a7\",\n" +
                "        \"c2669bb7-ad63-4a69-a16a-798b81d2b019\",\n" +
                "        \"32b6d11b-c7f3-475f-adf9-32d9b8f4aecf\",\n" +
                "        \"beca9731-63f4-434f-a3c2-ac33b44f9992\"\n" +
                "      ],\n" +
                "      \"calibrate\": \"false\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
        TestInfo testInfo = TestConfigHelper.loadTestByUuid("f0f3c1dd-89af-49f1-83e7-bcc31c3006cf");
        assert testInfo != null;
        assertEquals(SensorConstants.FLUORIDE_ID, testInfo.getCode());
    }

    @Test
    public void testValidateCalibration() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122), Color.TRANSPARENT));

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration1() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 175, 123), Color.TRANSPARENT));

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testRangeSlope() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 175, 113), Color.TRANSPARENT));

        assertEquals(31.82420654296875, SwatchHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration2() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 87, 121), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 175, 123), Color.TRANSPARENT));

        assertEquals(22.2095458984375, SwatchHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration3() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 146, 139), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 87, 181), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 124, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(250, 171, 130), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(245, 185, 122), Color.TRANSPARENT));

        assertEquals(21.658697509765624, SwatchHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration4() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 177), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 110, 15), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 139, 137), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(253, 174, 74), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(244, 180, 86), Color.TRANSPARENT));

        assertEquals(29.022808837890626, SwatchHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration5() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 110, 15), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 88, 177), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 139, 137), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(253, 174, 74), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(244, 180, 86), Color.TRANSPARENT));

        assertEquals(17.87760009765625, SwatchHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration6() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 177), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(254, 101, 157), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(254, 115, 138), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(254, 128, 119), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 142, 99), Color.TRANSPARENT));

        assertEquals(24.640643310546874, SwatchHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration7() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 0, 159), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(254, 28, 142), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(254, 56, 126), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(254, 84, 110), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 112, 93), Color.TRANSPARENT));

        assertEquals(22.0148193359375, SwatchHelper.calculateSlope(testInfo.getSwatches()));
        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }

    @Test
    public void testValidateCalibration8() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(250, 0, 0), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(240, 0, 0), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(225, 0, 0), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(210, 0, 0), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(195, 0, 0), Color.TRANSPARENT));

        assertEquals(0.0, SwatchHelper.calculateSlope(testInfo.getSwatches()));

        testInfo.setHueTrend(1);

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));

        testInfo.setHueTrend(-1);

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));

    }


    @Test
    public void testValidateCalibration9() {
        TestInfo testInfo = new TestInfo();
        testInfo.addSwatch(new Swatch(0, Color.rgb(255, 88, 47), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(255, 60, 37), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 35, 27), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1.5, Color.rgb(253, 17, 17), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(2, Color.rgb(254, 0, 0), Color.TRANSPARENT));

        assertEquals(-5.996826171875, SwatchHelper.calculateSlope(testInfo.getSwatches()));

        testInfo.setHueTrend(1);

        assertEquals(false, SwatchHelper.isSwatchListValid(testInfo));

        testInfo.setHueTrend(-1);

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));
    }


    @Test
    public void testValidateCalibration10() {
        TestInfo testInfo = new TestInfo();

        testInfo.addSwatch(new Swatch(0, Color.rgb(196, 178, 112), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.25, Color.rgb(209, 168, 138), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(219, 152, 125), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.75, Color.rgb(248, 105, 123), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(255, 62, 108), Color.TRANSPARENT));

        assertEquals(-62.31651611328125, SwatchHelper.calculateSlope(testInfo.getSwatches()));

        testInfo.setHueTrend(1);

        assertEquals(false, SwatchHelper.isSwatchListValid(testInfo));

        testInfo.setHueTrend(-1);

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));

        testInfo.setHueTrend(0);

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));

    }

    @Test
    public void testValidateCalibration11() {
        TestInfo testInfo = new TestInfo();

        testInfo.addSwatch(new Swatch(0, Color.rgb(196, 178, 112), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.25, Color.rgb(209, 168, 138), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.5, Color.rgb(219, 152, 125), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(0.75, Color.rgb(255, 62, 108), Color.TRANSPARENT));
        testInfo.addSwatch(new Swatch(1, Color.rgb(248, 105, 123), Color.TRANSPARENT));

        testInfo.setHueTrend(1);

        assertEquals(false, SwatchHelper.isSwatchListValid(testInfo));

        testInfo.setHueTrend(-1);

        assertEquals(false, SwatchHelper.isSwatchListValid(testInfo));

        testInfo.setHueTrend(0);

        assertEquals(true, SwatchHelper.isSwatchListValid(testInfo));

    }
}
