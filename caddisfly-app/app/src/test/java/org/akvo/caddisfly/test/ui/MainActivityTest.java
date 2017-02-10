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
    public void testGetResultValue() {
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
    public void testGetResultValue2() {
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
    public void testGetResultValue3() {
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
    public void testGetResultValue4() {
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
    public void testGetResultValue5() {
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
    public void testLoadJson() {
        TestInfo testInfo = TestConfigHelper.loadTestByUuid("f0f3c1dd-89af-49f1-83e7-bcc31c3006cf");
        assert testInfo != null;
        assertEquals(SensorConstants.FLUORIDE_ID, testInfo.getId());
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
