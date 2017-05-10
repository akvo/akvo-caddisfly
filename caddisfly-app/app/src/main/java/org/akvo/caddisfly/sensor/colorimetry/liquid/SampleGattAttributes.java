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

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
final class SampleGattAttributes {

    //static String HEART_RATE_MEASUREMENT = "e7add780-b042-4876-aae1-112855353cc1";
    static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    static final String LOVIBOND_DATA_CHARACTERISTIC = "e7add780-b042-4876-aae1-112855353cc1";
    private static HashMap<String, String> attributes = new HashMap<>();

    static {
        // Sample Services.
//        attributes.put("e7add780-b042-4876-aae1-112855353cc1", "Heart Rate Service");
//        attributes.put("e7add780-b042-4876-aae1-112855353cc1", "Device Information Service");
//        // Sample Characteristics.
//        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
//        attributes.put("e7add780-b042-4876-aae1-112855353cc1", "Manufacturer Name String");

        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer name");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model number");
        attributes.put("590c4bd9-b5e2-4a1a-867e-9b033ed1eadb", "DFU Reboot trigger");
        attributes.put(LOVIBOND_DATA_CHARACTERISTIC, "Data");

    }

    private SampleGattAttributes() {
    }

    static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
