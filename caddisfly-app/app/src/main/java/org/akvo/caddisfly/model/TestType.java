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

package org.akvo.caddisfly.model;

import com.google.gson.annotations.SerializedName;

/**
 * The different types of testing methods.
 */
public enum TestType {
    /**
     * Liquid reagent is mixed with sample and color is analysed from the resulting
     * color change in the solution.
     */
    @SerializedName("cuvette")
    COLORIMETRIC_LIQUID,

    /**
     * Strip paper is dipped into the sample and color is analysed from the resulting
     * color change on the strip paper.
     */
    @SerializedName("striptest")
    COLORIMETRIC_STRIP,

    /**
     * External sensors connected to the phone/device.
     */
    @SerializedName("sensor")
    SENSOR,

    /**
     * External sensors/meters not connected to the phone/device.
     */
    @SerializedName("manual")
    MANUAL,

    /**
     * External bluetooth testing device.
     */
    @SerializedName("bluetooth")
    BLUETOOTH,

    /**
     * Aquagenx CBT.
     */
    @SerializedName("cbt")
    CBT,

}

