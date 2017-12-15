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

package org.akvo.caddisfly.common;

/**
 * Class to hold all public constants used by sensors
 */

public final class SensorConstants {

    /**
     * Serialization constants
     */
    public static final String TYPE_NAME = "caddisfly";
    public static final String RESOURCE_ID = "caddisflyResourceUuid";
    public static final String RESPONSE = "response";
    @Deprecated
    public static final String RESPONSE_COMPAT = "response_compat";
    public static final String LANGUAGE = "language";
    public static final String QUESTION_TITLE = "questionTitle";

    public static final String FREE_CHLORINE_ID = "c3535e72-ff77-4225-9f4a-41d3288780c6";
    public static final String FREE_CHLORINE_ID_2 = "a2413119-38eb-4959-92ee-cc169fdbb0fc";
    public static final String FLUORIDE_ID = "f0f3c1dd-89af-49f1-83e7-bcc31c3006cf";
    public static final String FLUORIDE_1_ID = "f0f3c1dd-89af-49f1-83e7-bcc31c3006cg";
    public static final String CBT_ID = "e40d4764-e73f-46dd-a598-ed4db0fd3386";

    public static final int DEGREES_90 = 90;
    public static final int DEGREES_270 = 270;
    public static final int DEGREES_180 = 180;

    public static final String MPN_TABLE_FILENAME = "most-probable-number.json";

    private SensorConstants() {
    }
}
