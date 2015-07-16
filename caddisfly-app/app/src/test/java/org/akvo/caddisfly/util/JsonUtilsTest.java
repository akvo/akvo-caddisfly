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

package org.akvo.caddisfly.util;

import junit.framework.TestCase;

@SuppressWarnings({"unused", "EmptyMethod"})
public class JsonUtilsTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtils.assertUtilityClassWellDefined(JsonUtils.class);
    }

//    public void testLoadJson() throws Exception {
//        String jsonText = "{\n" +
//                "    \"tests\": {\n" +
//                "        \"test\": [\n" +
//                "            {\n" +
//                "                \"name\":[\n" +
//                "                            { \"en\": \"Fluoride\"},\n" +
//                "                            { \"fr\": \"Fluorure\"},\n" +
//                "                            { \"ar\": \"??????\"},\n" +
//                "                            { \"hi\": \"????????\"},\n" +
//                "                  {\"kn\": \"????????\"}\n" +
//                "                        ],\n" +
//                "                \"type\": \"0\",\n" +
//                "                \"code\": \"fluor\",\n" +
//                "                \"unit\": \"ppm\",\n" +
//                "                \"ranges\": \"0,.5,1,1.5,2\",\n" +
//                "                \"dilutions\":\"0,50,75\"\n" +
//                "            },\n" +
//                "            {\n" +
//                "                \"name\": [\n" +
//                "                            { \"en\": \"pH\"}\n" +
//                "                        ],\n" +
//                "                \"type\": \"0\",\n" +
//                "                \"code\": \"phydr\",\n" +
//                "                \"unit\": \"pH\",\n" +
//                "              \"ranges\": \"3,4,5,6,7,8,9\"\n" +
//                "            }\n" +
//                "        ]\n" +
//                "    }\n" +
//                "}";
//        TestInfo testInfo= JsonUtils.loadJson(jsonText, "fluor");
//        assertEquals("FLUOR", testInfo.getCode());
//    }

    public void testLoadTests() throws Exception {

    }
}