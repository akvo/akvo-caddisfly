package org.akvo.caddisfly.util;

import android.graphics.Color;

import junit.framework.TestCase;

import java.util.ArrayList;

public class DataHelperTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtils.assertUtilityClassWellDefined(DataHelper.class);
    }

    public void testGetAverageResult() throws Exception {

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

        double result = DataHelper.getAverageResult(results);
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

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1, result, 0);
    }

    public void testAverage3() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.5);

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage4() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.5);
        results.add(1.5);

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage5() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.5);
        results.add(1.5);
        results.add(1.7);

        double result = DataHelper.getAverageResult(results);
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

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1, result, 0);
    }

    public void testAverage7() {
        ArrayList<Double> results = new ArrayList<>();
        results.add(1.6);
        results.add(1.8);
        results.add(1.7);
        results.add(1.7);
        results.add(1.5);
        results.add(1.5);

        double result = DataHelper.getAverageResult(results);
        assertEquals(1.64, result, 0);
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

        double result = DataHelper.getAverageResult(results);
        assertEquals(1.66, result, 0);
    }

    public void testGetAverageColor1() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 255, 255));
        colors.add(Color.rgb(250, 250, 250));
        colors.add(Color.rgb(245, 245, 245));
        colors.add(Color.rgb(240, 240, 240));
        colors.add(Color.rgb(235, 235, 235));
        colors.add(Color.rgb(230, 230, 230));
        int color = DataHelper.getAverageColor(colors);
        assertEquals(Color.rgb(240, 240, 240), color);
    }

    public void testGetAverageColor2() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 255, 255));
        colors.add(Color.rgb(250, 250, 250));
        colors.add(Color.rgb(245, 245, 245));
        colors.add(Color.rgb(240, 240, 240));
        int color = DataHelper.getAverageColor(colors);
        assertEquals(0, color);
    }

    public void testGetAverageColor3() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 255, 255));
        colors.add(Color.rgb(250, 250, 250));
        colors.add(Color.rgb(239, 245, 245));
        colors.add(Color.rgb(240, 240, 240));
        colors.add(Color.rgb(235, 235, 235));
        colors.add(Color.rgb(210, 230, 210));
        int color = DataHelper.getAverageColor(colors);
        assertEquals(0, color);
    }


}