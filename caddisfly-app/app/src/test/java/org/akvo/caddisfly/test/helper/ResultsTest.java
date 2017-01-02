package org.akvo.caddisfly.test.helper;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings("unused")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ResultsTest {

    @Test
    public void testColorimetryResult() throws Exception {

        TestInfo testInfo = TestConfigHelper.loadTestByUuid(SensorConstants.FLUORIDE_ID);
        assert testInfo != null;

        ArrayList<String> results = new ArrayList<>();
        results.add("> 2.0");

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, "");

        // Replace items that cannot be tested (e.g. currentTime)
        String json = resultJson.toString().replaceAll("(\"testDate\":\").*?\"", "$1today\"");
        json = json.replaceAll("(\"appVersion\":\").*?\"", "$1version\"");

        String expectedJson = "{\"type\":\"caddisfly\",\"name\":\"Fluoride\",\"uuid\":\"[f0f3c1dd-89af-49f1-83e7-bcc31c3006cf]\",\"result\":[{\"name\":\"Fluoride\",\"unit\":\"ppm\",\"id\":1,\"value\":\"> 2.0\"}],\"testDate\":\"today\",\"user\":{\"backDropDetection\":true,\"language\":\"\"},\"app\":{\"appVersion\":\"version\",\"language\":\"en\"},\"device\":{\"model\":\"unknown\",\"product\":\"unknown\",\"manufacturer\":\"unknown\",\"os\":\"Android - 6.0.0_r1 (23)\",\"country\":\"US\",\"language\":\"en\"}}";

        assertEquals(expectedJson, json);
    }

    @Test
    public void testEcSensorResult() throws Exception {

        TestInfo testInfo = TestConfigHelper.loadTestByUuid("f88237b7-be3d-4fac-bbee-ab328eefcd14");
        assert testInfo != null;

        ArrayList<String> results = new ArrayList<>();
        results.add("32432");
        results.add("29.5");

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, "");

        // Replace items that cannot be tested (e.g. currentTime)
        String json = resultJson.toString().replaceAll("(\"testDate\":\").*?\"", "$1today\"");
        json = json.replaceAll("(\"appVersion\":\").*?\"", "$1version\"");

        String expectedJson = "{\"type\":\"caddisfly\",\"name\":\"Electrical Conductivity\",\"uuid\":\"[f88237b7-be3d-4fac-bbee-ab328eefcd14]\",\"result\":[{\"name\":\"Electrical Conductivity\",\"unit\":\"μS\\/cm\",\"id\":1,\"value\":\"32432\"},{\"name\":\"Temperature\",\"unit\":\"°Celsius\",\"id\":2,\"value\":\"29.5\"}],\"testDate\":\"today\",\"user\":{\"backDropDetection\":true,\"language\":\"\"},\"app\":{\"appVersion\":\"version\",\"language\":\"en\"},\"device\":{\"model\":\"unknown\",\"product\":\"unknown\",\"manufacturer\":\"unknown\",\"os\":\"Android - 6.0.0_r1 (23)\",\"country\":\"US\",\"language\":\"en\"}}";

        assertEquals(expectedJson, json);
    }

}
