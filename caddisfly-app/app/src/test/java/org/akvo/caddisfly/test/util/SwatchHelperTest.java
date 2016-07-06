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

package org.akvo.caddisfly.test.util;

import android.graphics.Color;

import junit.framework.TestCase;

import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.ResultDetail;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class SwatchHelperTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtil.assertUtilityClassWellDefined(SwatchHelper.class);
    }

    private Result createNewResult(double value) {
        return createNewResult(value, 0);
    }

    private Result createNewResult(double value, int color) {
        ArrayList<ResultDetail> results = new ArrayList<>();
        results.add(new ResultDetail(value, color));
        return new Result(null, results);
    }

    public void testGetAverageResult() throws Exception {

        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.5));
        results.add(createNewResult(0.0));
        results.add(createNewResult(1.7));
        results.add(createNewResult(0.0));
        results.add(createNewResult(1.8));
        results.add(createNewResult(0.0));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(-1, result, 0);
    }

    public void testAverage2() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.8));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage3() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.8));
        results.add(createNewResult(1.5));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(-1, result, 0);
    }

    public void testAverage4() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.8));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage5() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.8));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.7));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage6() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.8));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.7));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(-1, result, 0);
    }

    public void testAverage7() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.8));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(1.63, result, 0);
    }

    public void testAverage8() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6));
        results.add(createNewResult(1.8));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.7));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.5));
        results.add(createNewResult(1.6));

        double result = SwatchHelper.getAverageResult(results);
        assertEquals(1.63, result, 0);
    }

    public void testGetAverageColor1() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6, Color.rgb(255, 255, 255)));
        results.add(createNewResult(1.8, Color.rgb(250, 250, 250)));
        results.add(createNewResult(1.7, Color.rgb(245, 245, 245)));
        results.add(createNewResult(1.7, Color.rgb(240, 240, 240)));
        results.add(createNewResult(1.5, Color.rgb(235, 235, 235)));
        results.add(createNewResult(1.5, Color.rgb(235, 235, 235)));
        results.add(createNewResult(1.6, Color.rgb(230, 230, 230)));

        int color = SwatchHelper.getAverageColor(results);
        assertEquals(-16777216, color);
    }

    public void testGetAverageColor2() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6, Color.rgb(255, 255, 255)));
        results.add(createNewResult(1.8, Color.rgb(250, 250, 250)));
        results.add(createNewResult(1.7, Color.rgb(245, 245, 245)));
        results.add(createNewResult(1.7, Color.rgb(240, 240, 240)));

        int color = SwatchHelper.getAverageColor(results);
        assertEquals(-16777216, color);
    }

    public void testGetAverageColor3() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6, Color.rgb(255, 255, 255)));
        results.add(createNewResult(1.8, Color.rgb(250, 250, 250)));
        results.add(createNewResult(1.7, Color.rgb(245, 245, 245)));
        results.add(createNewResult(1.7, Color.rgb(240, 240, 240)));
        results.add(createNewResult(1.5, Color.rgb(235, 235, 235)));
        results.add(createNewResult(1.5, Color.rgb(210, 230, 210)));

        int color = SwatchHelper.getAverageColor(results);
        assertEquals(-16777216, color);
    }

    public void testGetAverageColor4() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6, Color.rgb(255, 255, 255)));
        results.add(createNewResult(1.8, Color.rgb(250, 254, 250)));
        results.add(createNewResult(1.8, Color.rgb(251, 253, 250)));
        results.add(createNewResult(1.8, Color.rgb(252, 252, 250)));
        results.add(createNewResult(1.8, Color.rgb(253, 251, 250)));
        results.add(createNewResult(1.8, Color.rgb(254, 250, 250)));

        int color = SwatchHelper.getAverageColor(results);
        assertEquals(-16777216, color);
    }

    public void testGetAverageColor5() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6, Color.rgb(225, 1, 1)));
        results.add(createNewResult(1.8, Color.rgb(225, 1, 2)));
        results.add(createNewResult(1.8, Color.rgb(215, 1, 1)));
        results.add(createNewResult(1.8, Color.rgb(225, 1, 1)));
        results.add(createNewResult(1.8, Color.rgb(225, 1, 1)));
        results.add(createNewResult(1.8, Color.rgb(225, 1, 3)));

        int color = SwatchHelper.getAverageColor(results);
        assertEquals(-16777216, color);
    }

    public void testGetAverageColor6() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(createNewResult(1.6, Color.rgb(179, 128, 81)));
        results.add(createNewResult(1.8, Color.rgb(176, 126, 77)));
        results.add(createNewResult(1.8, Color.rgb(177, 125, 77)));
        results.add(createNewResult(1.8, Color.rgb(177, 125, 77)));
        results.add(createNewResult(1.8, Color.rgb(175, 125, 76)));
        results.add(createNewResult(1.8, Color.rgb(175, 124, 77)));

        int color = SwatchHelper.getAverageColor(results);
        assertEquals(-16777216, color);
    }
}