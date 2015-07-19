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
import android.os.Bundle;
import android.util.Pair;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

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
        TestInfo testInfo = new TestInfo(null, "", "", 0);
        testInfo.addRange(new ResultRange(0, Color.rgb(255, 87, 181)));
        testInfo.addRange(new ResultRange(0.5, Color.rgb(255, 124, 157)));
        testInfo.addRange(new ResultRange(1, Color.rgb(255, 146, 139)));
        testInfo.addRange(new ResultRange(1.5, Color.rgb(250, 171, 130)));
        testInfo.addRange(new ResultRange(2, Color.rgb(245, 185, 122)));

        List list = ColorUtils.autoGenerateColors(testInfo);

        for (int i = 0; i < list.size(); i++) {
            testInfo.addSwatch(new ResultRange(
                    Double.parseDouble(((Pair) list.get(i)).first.toString()),
                    (int) ((Pair) list.get(i)).second));
        }

        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(-1.0, bundle.getDouble("resultValue"));
    }

    @Test
    public void testGetPpmValue2() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            if (i > 1500) {
                colors[i] = Color.rgb(255, 156, 149);
            } else {
                colors[i] = -1;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo(null, "", "", 0);
        testInfo.addRange(new ResultRange(0, Color.rgb(255, 87, 181)));
        testInfo.addRange(new ResultRange(0.5, Color.rgb(255, 124, 157)));
        testInfo.addRange(new ResultRange(1, Color.rgb(255, 146, 139)));
        testInfo.addRange(new ResultRange(1.5, Color.rgb(250, 171, 130)));
        testInfo.addRange(new ResultRange(2, Color.rgb(245, 185, 122)));

        List list = ColorUtils.autoGenerateColors(testInfo);

        for (int i = 0; i < list.size(); i++) {
            testInfo.addSwatch(new ResultRange(
                    Double.parseDouble(((Pair) list.get(i)).first.toString()),
                    (int) ((Pair) list.get(i)).second));
        }

        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(Color.rgb(255, 156, 149), bundle.getInt("resultColor"));
        assertEquals(1.1, bundle.getDouble("resultValue"));
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
        TestInfo testInfo = new TestInfo(null, "", "", 0);
        testInfo.addRange(new ResultRange(0, Color.rgb(255, 87, 181)));
        testInfo.addRange(new ResultRange(0.5, Color.rgb(255, 124, 157)));
        testInfo.addRange(new ResultRange(1, Color.rgb(255, 146, 139)));
        testInfo.addRange(new ResultRange(1.5, Color.rgb(250, 171, 130)));
        testInfo.addRange(new ResultRange(2, Color.rgb(245, 185, 122)));

        List list = ColorUtils.autoGenerateColors(testInfo);

        for (int i = 0; i < list.size(); i++) {
            testInfo.addSwatch(new ResultRange(
                    Double.parseDouble(((Pair) list.get(i)).first.toString()),
                    (int) ((Pair) list.get(i)).second));
        }

        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(Color.rgb(255, 146, 139), bundle.getInt("resultColor"));
        assertEquals(1.0, bundle.getDouble("resultValue"));
    }

    @Test
    public void testGetPpmValue4() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(250, 171, 130);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo(null, "", "", 0);
        testInfo.addRange(new ResultRange(0, Color.rgb(255, 87, 181)));
        testInfo.addRange(new ResultRange(0.5, Color.rgb(255, 124, 157)));
        testInfo.addRange(new ResultRange(1, Color.rgb(255, 146, 139)));
        testInfo.addRange(new ResultRange(1.5, Color.rgb(250, 171, 130)));
        testInfo.addRange(new ResultRange(2, Color.rgb(245, 185, 122)));

        List list = ColorUtils.autoGenerateColors(testInfo);

        for (int i = 0; i < list.size(); i++) {
            testInfo.addSwatch(new ResultRange(
                    Double.parseDouble(((Pair) list.get(i)).first.toString()),
                    (int) ((Pair) list.get(i)).second));
        }

        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(1.5, bundle.getDouble("resultValue"));
    }

    @Test
    public void testGetPpmValue5() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(254, 115, 138);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo(null, "", "", 0);
        testInfo.addRange(new ResultRange(0, Color.rgb(255, 88, 177)));
        testInfo.addRange(new ResultRange(0.5, Color.rgb(254, 101, 157)));
        testInfo.addRange(new ResultRange(1, Color.rgb(254, 115, 138)));
        testInfo.addRange(new ResultRange(1.5, Color.rgb(254, 128, 119)));
        testInfo.addRange(new ResultRange(2, Color.rgb(254, 142, 99)));

        List list = ColorUtils.autoGenerateColors(testInfo);

        for (int i = 0; i < list.size(); i++) {
            testInfo.addSwatch(new ResultRange(
                    Double.parseDouble(((Pair) list.get(i)).first.toString()),
                    (int) ((Pair) list.get(i)).second));
        }

        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(1.0, bundle.getDouble("resultValue"));
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
        TestInfo testInfo= JsonUtils.loadJson(jsonText, "fluor");
        assert testInfo != null;
        assertEquals("FLUOR", testInfo.getCode());
    }
}