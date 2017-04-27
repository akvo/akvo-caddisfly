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

package org.akvo.caddisfly.test.util;

import android.graphics.Color;

import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.ColorUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unused")
public class ColorUtilTest {

    @Before
    public void setUp() throws Exception {
        ClassUtil.assertUtilityClassWellDefined(ColorUtil.class);
    }

    @Test
    public void testGetDistance() {
        double distance = ColorUtil.getColorDistance(Color.rgb(200, 200, 200), Color.rgb(100, 100, 100));
        assertEquals(173.20508075688772, distance, 0);
    }

    @Test
    public void testGetColorRgbString() {
        String rgb = ColorUtil.getColorRgbString(-13850285);
        assertEquals("44  169  83", rgb);
    }

    @Test
    public void testAutoGenerateColors() {
        TestInfo testInfo = new TestInfo();

        for (int i = 0; i < 5; i++) {
            Swatch swatch = new Swatch(((double) i * 10) / 10f, Color.TRANSPARENT, Color.TRANSPARENT);
            testInfo.addSwatch(swatch);
        }

        List<Swatch> list = SwatchHelper.generateGradient(testInfo.getSwatches(), ColorUtil.DEFAULT_COLOR_MODEL);

        assertEquals(401, list.size());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(String.format(Locale.US, "%.2f", i * 0.01),
                    String.format(Locale.US, "%.2f", list.get(i).getValue()));
            assertEquals(true, list.get(i).getColor() == Color.BLACK ||
                    list.get(i).getColor() == Color.TRANSPARENT);
        }
    }

    @Test
    public void testGetColorFromRgb()  {
        int color = ColorUtil.getColorFromRgb("44  169  83");
        assertEquals(-13850285, color);

    }

    @Test
    public void testGetBrightness() {
        int brightness = ColorUtil.getBrightness(Color.rgb(200, 255, 30));
        assertEquals(233, brightness);
    }
}