package org.akvo.caddisfly.util;

import org.akvo.caddisfly.model.TestInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtils {

    public static TestInfo loadJson(String jsonText, String testCode) {
        JSONObject jsonObject;
        TestInfo testInfo = null;

        try {
            jsonObject = new JSONObject(jsonText).getJSONObject("tests");
            JSONArray array = jsonObject.getJSONArray("test");
            for (int i = 0; i < array.length(); i++) {
                if (testCode.equalsIgnoreCase(array.getJSONObject(i).getString("code"))) {
                    testInfo = new TestInfo(
                            array.getJSONObject(i).getString("name"),
                            array.getJSONObject(i).getString("code"),
                            array.getJSONObject(i).getJSONObject("lowRange").getDouble("start"),
                            array.getJSONObject(i).getJSONObject("lowRange").getDouble("end"));

                    try {
                        testInfo.setIncrement(array.getJSONObject(i).getJSONObject("lowRange").getInt("increment"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return testInfo;
    }


    public static ArrayList<TestInfo> loadTests(String jsonText) {
        JSONObject jsonObject;
        TestInfo testInfo;

        ArrayList<TestInfo> tests = new ArrayList<TestInfo>();

        try {
            jsonObject = new JSONObject(jsonText).getJSONObject("tests");
            JSONArray array = jsonObject.getJSONArray("test");
            for (int i = 0; i < array.length(); i++) {
                testInfo = new TestInfo(
                        array.getJSONObject(i).getString("name"),
                        array.getJSONObject(i).getString("code"),
                        array.getJSONObject(i).getJSONObject("lowRange").getDouble("start"),
                        array.getJSONObject(i).getJSONObject("lowRange").getDouble("end"));

                try {
                    testInfo.setIncrement(array.getJSONObject(i).getJSONObject("lowRange").getInt("increment"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                tests.add(testInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tests;
    }
}
