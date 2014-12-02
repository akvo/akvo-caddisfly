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

                JSONArray ranges = item.getJSONArray("ranges");

                if (ranges.length() > 1) {
                    highRangeStart = ranges.getJSONObject(1).getDouble("start");
                    highRangeEnd = ranges.getJSONObject(1).getDouble("end");
                }

                testInfo = new TestInfo(
                        item.getString("name"),
                        item.getString("code").toUpperCase(),
                        item.getString("unit"),
                        ranges.getJSONObject(0).getDouble("start"),
                        ranges.getJSONObject(0).getDouble("end"),
                        highRangeStart,
                        highRangeEnd);

                testInfo.setIncrement((int) (item.getDouble("step") * 10));

                tests.add(testInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tests;
    }
}
