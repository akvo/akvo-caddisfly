package org.akvo.caddisfly.util;

import android.graphics.Color;

import junit.framework.TestCase;

import org.akvo.caddisfly.model.Result;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class DataHelperTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtils.assertUtilityClassWellDefined(DataHelper.class);
    }

    public void testGetAverageResult() throws Exception {

        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(0.0, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(0.0, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(0.0, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(1.54, result, 0);
    }

    public void testAverage2() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(0.0, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(0.0, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(0.0, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1, result, 0);
    }

    public void testAverage3() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(1.5, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage4() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage5() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.7, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1.0, result, 0);
    }

    public void testAverage6() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(1.7, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(-1, result, 0);
    }

    public void testAverage7() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(1.64, result, 0);
    }

    public void testAverage8() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, 0, null, null));
        results.add(new Result(1.8, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(1.7, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.5, 0, null, null));
        results.add(new Result(1.6, 0, null, null));

        double result = DataHelper.getAverageResult(results);
        assertEquals(1.66, result, 0);
    }

    public void testGetAverageColor1() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, Color.rgb(255, 255, 255), null, null));
        results.add(new Result(1.8, Color.rgb(250, 250, 250), null, null));
        results.add(new Result(1.7, Color.rgb(245, 245, 245), null, null));
        results.add(new Result(1.7, Color.rgb(240, 240, 240), null, null));
        results.add(new Result(1.5, Color.rgb(235, 235, 235), null, null));
        results.add(new Result(1.5, Color.rgb(235, 235, 235), null, null));
        results.add(new Result(1.6, Color.rgb(230, 230, 230), null, null));

        int color = DataHelper.getAverageColor(results);
        assertEquals(Color.rgb(240, 240, 240), color);
    }

    public void testGetAverageColor2() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, Color.rgb(255, 255, 255), null, null));
        results.add(new Result(1.8, Color.rgb(250, 250, 250), null, null));
        results.add(new Result(1.7, Color.rgb(245, 245, 245), null, null));
        results.add(new Result(1.7, Color.rgb(240, 240, 240), null, null));

        int color = DataHelper.getAverageColor(results);
        assertEquals(0, color);
    }

    public void testGetAverageColor3() {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(1.6, Color.rgb(255, 255, 255), null, null));
        results.add(new Result(1.8, Color.rgb(250, 250, 250), null, null));
        results.add(new Result(1.7, Color.rgb(239, 245, 245), null, null));
        results.add(new Result(1.7, Color.rgb(240, 240, 240), null, null));
        results.add(new Result(1.5, Color.rgb(235, 235, 235), null, null));
        results.add(new Result(1.5, Color.rgb(210, 230, 210), null, null));
        int color = DataHelper.getAverageColor(results);
        assertEquals(0, color);
    }

    //temporary check to be replaced with crc check
    public void testValidDouble() {
        assertEquals(DataHelper.validDouble("23.56"), true);
        assertEquals(DataHelper.validDouble("1723.56"), true);
        assertEquals(DataHelper.validDouble("23.5"), false);
        assertEquals(DataHelper.validDouble("12323.567"), false);
        assertEquals(DataHelper.validDouble("34423.00"), true);
        assertEquals(DataHelper.validDouble(".56"), false);
        assertEquals(DataHelper.validDouble("56"), false);
        assertEquals(DataHelper.validDouble("56334"), false);
    }

}