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

package org.akvo.caddisfly.util;

import android.graphics.Color;
import android.util.Pair;

import junit.framework.TestCase;

import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;

import java.util.Hashtable;
import java.util.List;

@SuppressWarnings("unused")
public class ColorUtilsTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtils.assertUtilityClassWellDefined(ColorUtils.class);
    }

    public void testGetDistance() throws Exception {
        double distance = ColorUtils.getColorDistance(Color.rgb(200, 200, 200), Color.rgb(100, 100, 100));
        assertEquals(173.20508075688772, distance, 0);
    }

    public void testGetColorRgbString() throws Exception {
        String rgb = ColorUtils.getColorRgbString(-13850285);
        assertEquals("44  169  83", rgb);
    }

    public void testAutoGenerateColors() throws Exception {
        Hashtable hashtable = new Hashtable();
        TestInfo testInfo = new TestInfo(hashtable, "FLUOR", "ppm", MainApp.TestType.COLORIMETRIC);

        for (int i = 0; i < 5; i++) {
            ResultRange resultRange = new ResultRange(((int) ((double) i * 10)) / 10f, Color.TRANSPARENT);
            testInfo.addRange(resultRange);
        }

        List list = ColorUtils.autoGenerateColors(testInfo);

        assertEquals(400, list.size());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(String.format("%.2f", i * 0.01), String.format("%.2f",(double)((Pair) list.get(i)).first));
            assertEquals(-16777216, ((Pair) list.get(i)).second);
        }
    }

    public void testGetColorFromRgb() throws Exception {
        int color = ColorUtils.getColorFromRgb("44  169  83");
        assertEquals(-13850285, color);

    }

    public void testGetBrightness() throws Exception {
        int brightness = ColorUtils.getBrightness(Color.rgb(200, 255, 30));
        assertEquals(233, brightness);
    }
}