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
 * Class to hold all public constants used by sensors.
 */
public final class SensorConstants {

    /**
     * Serialization constants.
     */
    public static final String TYPE_NAME = "caddisfly";
    public static final String RESOURCE_ID = "caddisflyResourceUuid";
    public static final String RESPONSE = "response";
    public static final String VALUE = "value";
    @Deprecated
    public static final String RESPONSE_COMPAT = "response_compat";
    public static final String LANGUAGE = "language";
    public static final String QUESTION_TITLE = "questionTitle";

    private SensorConstants() {
    }
}
