package org.akvo.caddisfly.util;

import org.akvo.caddisfly.model.TestInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtils {

    public static TestInfo loadJson(String jsonText, String testCode) throws JSONException {
        ArrayList<TestInfo> tests = loadTests(jsonText);

        for (TestInfo test : tests) {
            if (test.getCode().equalsIgnoreCase(testCode)) {
                return test;
            }
        }
        return null;
    }


    public static ArrayList<TestInfo> loadTests(String jsonText) throws JSONException {
        JSONObject jsonObject;
        TestInfo testInfo;

        ArrayList<TestInfo> tests = new ArrayList<TestInfo>();
        jsonObject = new JSONObject(jsonText).getJSONObject("tests");
        JSONArray array = jsonObject.getJSONArray("test");
        for (int i = 0; i < array.length(); i++) {

            try {
                JSONObject item = array.getJSONObject(i);

                double highRangeStart = -1;
                double highRangeEnd = -1;

                if (item.has("highRange")) {
                    highRangeStart = item.getJSONObject("highRange").getDouble("start");
                    highRangeEnd = item.getJSONObject("highRange").getDouble("end");
                }

                JSONObject lowRange = item.getJSONObject("lowRange");

                testInfo = new TestInfo(
                        item.getString("name"),
                        item.getString("code").toUpperCase(),
                        item.getString("unit"),
                        lowRange.getDouble("start"),
                        lowRange.getDouble("end"),
                        highRangeStart,
                        highRangeEnd);

                if (lowRange.has("increment")) {
                    testInfo.setIncrement(lowRange.getInt("increment"));
                }
                tests.add(testInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tests;
    }
}
