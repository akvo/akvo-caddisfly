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

package org.akvo.caddisfly.helper;

import android.util.SparseArray;

import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings("unused")
@RunWith(RobolectricTestRunner.class)
public class ResultsTest {

    @Test
    public void testEcSensorResult() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo("f88237b7-be3d-4fac-bbee-ab328eefcd14");

        assert testInfo != null;

        SparseArray<String> results = new SparseArray<>();
        results.put(1, "32432");
        results.put(2, "29.5");

        JSONObject resultJson = TestConfigHelper.getJsonResult(null, testInfo, results, null, "");

        // Replace items that cannot be tested (e.g. currentTime)
        String json = resultJson.toString().replaceAll("(\"testDate\":\").*?\"", "$1today\"");
        json = json.replaceAll("(\"appVersion\":\").*?\"", "$1version\"");
        json = json.replaceAll("(\"country\":\").*?\"", "$1\"");

        String expectedJson = "{\"type\":\"caddisfly\",\"name\":\"Water - Electrical Conductivity\",\"uuid\":\"f88237b7-be3d-4fac-bbee-ab328eefcd14\",\"result\":[{\"name\":\"Water Electrical Conductivity\",\"unit\":\"μS\\/cm\",\"id\":1,\"value\":\"32432\"},{\"name\":\"Temperature\",\"unit\":\"°Celsius\",\"id\":2,\"value\":\"29.5\"}],\"testDate\":\"today\",\"app\":{\"appVersion\":\"version\"}}";

        assertEquals(expectedJson, json);
    }

}
