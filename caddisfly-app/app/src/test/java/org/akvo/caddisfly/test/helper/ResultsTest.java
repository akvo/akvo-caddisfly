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

package org.akvo.caddisfly.test.helper;

import android.util.SparseArray;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings("unused")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ResultsTest {

    @Test
    public void testColorimetryResult() {

        TestInfo testInfo = TestConfigHelper.loadTestByUuid(SensorConstants.FLUORIDE_ID);
        assert testInfo != null;

        SparseArray<String> results = new SparseArray<>();
        results.put(1, "> 2.0");

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, "", null);

        // Replace items that cannot be tested (e.g. currentTime)
        String json = resultJson.toString().replaceAll("(\"testDate\":\").*?\"", "$1today\"");
        json = json.replaceAll("(\"appVersion\":\").*?\"", "$1version\"");

        String expectedJson = "{\"type\":\"caddisfly\",\"name\":\"Water - Fluoride\",\"uuid\":\"f0f3c1dd-89af-49f1-83e7-bcc31c3006cf\",\"result\":[{\"name\":\"Fluoride\",\"unit\":\"ppm\",\"id\":1,\"value\":\"> 2.0\"}],\"testDate\":\"today\",\"user\":{\"backDropDetection\":true,\"language\":\"\"},\"app\":{\"appVersion\":\"version\",\"language\":\"en\"},\"device\":{\"model\":\"unknown\",\"product\":\"unknown\",\"manufacturer\":\"unknown\",\"os\":\"Android - 6.0.1_r3 (23)\",\"country\":\"US\",\"language\":\"en\"}}";

        assertEquals(expectedJson, json);
    }

    @Test
    public void testEcSensorResult() {

        TestInfo testInfo = TestConfigHelper.loadTestByUuid("f88237b7-be3d-4fac-bbee-ab328eefcd14");
        assert testInfo != null;

        SparseArray<String> results = new SparseArray<>();
        results.put(1, "32432");
        results.put(2, "29.5");

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, "", null);

        // Replace items that cannot be tested (e.g. currentTime)
        String json = resultJson.toString().replaceAll("(\"testDate\":\").*?\"", "$1today\"");
        json = json.replaceAll("(\"appVersion\":\").*?\"", "$1version\"");

        String expectedJson = "{\"type\":\"caddisfly\",\"name\":\"Water - Electrical Conductivity\",\"uuid\":\"f88237b7-be3d-4fac-bbee-ab328eefcd14\",\"result\":[{\"name\":\"Electrical Conductivity\",\"unit\":\"μS\\/cm\",\"id\":1,\"value\":\"32432\"},{\"name\":\"Temperature\",\"unit\":\"°Celsius\",\"id\":2,\"value\":\"29.5\"}],\"testDate\":\"today\",\"user\":{\"backDropDetection\":true,\"language\":\"\"},\"app\":{\"appVersion\":\"version\",\"language\":\"en\"},\"device\":{\"model\":\"unknown\",\"product\":\"unknown\",\"manufacturer\":\"unknown\",\"os\":\"Android - 6.0.1_r3 (23)\",\"country\":\"US\",\"language\":\"en\"}}";

        assertEquals(expectedJson, json);
    }

}
