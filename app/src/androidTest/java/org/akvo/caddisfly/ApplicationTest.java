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

package org.akvo.caddisfly;

import android.app.Application;
import android.test.ApplicationTestCase;

import org.akvo.caddisfly.util.DataHelper;

import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testAverage1() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.5);
        results.add(0.0);
        results.add(1.7);
        results.add(0.0);
        results.add(1.8);
        results.add(0.0);
        results.add(1.7);
        results.add(1.5);
        results.add(1.5);
        results.add(1.5);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(1.54, result, 0);
    }

    public void testAverage2() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(0.0);
        results.add(1.5);
        results.add(1.7);
        results.add(0.0);
        results.add(1.8);
        results.add(0.0);
        results.add(1.7);
        results.add(1.5);
        results.add(1.5);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(1.58, result, 0);
    }

    public void testAverage3() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.5);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage4() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.5);
        results.add(1.5);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage5() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.5);
        results.add(1.5);
        results.add(1.7);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage6() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.5);
        results.add(1.5);
        results.add(1.7);
        results.add(1.7);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(1.64, result, 0);
    }

    public void testAverage7() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.7);
        results.add(1.7);
        results.add(1.5);
        results.add(1.5);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(-1, result, 0);
    }

    public void testAverage8() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.7);
        results.add(1.7);
        results.add(1.5);
        results.add(1.5);
        results.add(1.6);

        double result = DataHelper.getAverageResult(getApplication(), results);
        assertEquals(1.6, result, 0);
    }


}