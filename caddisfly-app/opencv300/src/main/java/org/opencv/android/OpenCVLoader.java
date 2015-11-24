/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.opencv.android;

import android.content.Context;

/**
 * Helper class provides common initialization methods for OpenCV library.
 */
public class OpenCVLoader {
    /**
     * OpenCV Library version 2.4.2.
     */
    public static final String OPENCV_VERSION_2_4_2 = "2.4.2";

    /**
     * OpenCV Library version 2.4.3.
     */
    public static final String OPENCV_VERSION_2_4_3 = "2.4.3";

    /**
     * OpenCV Library version 2.4.4.
     */
    public static final String OPENCV_VERSION_2_4_4 = "2.4.4";

    /**
     * OpenCV Library version 2.4.5.
     */
    public static final String OPENCV_VERSION_2_4_5 = "2.4.5";

    /**
     * OpenCV Library version 2.4.6.
     */
    public static final String OPENCV_VERSION_2_4_6 = "2.4.6";

    /**
     * OpenCV Library version 2.4.7.
     */
    public static final String OPENCV_VERSION_2_4_7 = "2.4.7";

    /**
     * OpenCV Library version 2.4.8.
     */
    public static final String OPENCV_VERSION_2_4_8 = "2.4.8";

    /**
     * OpenCV Library version 2.4.9.
     */
    public static final String OPENCV_VERSION_2_4_9 = "2.4.9";

    /**
     * OpenCV Library version 2.4.10.
     */
    public static final String OPENCV_VERSION_2_4_10 = "2.4.10";

    /**
     * OpenCV Library version 2.4.11.
     */
    public static final String OPENCV_VERSION_2_4_11 = "2.4.11";

    /**
     * OpenCV Library version 3.0.0.
     */
    public static final String OPENCV_VERSION_3_0_0 = "3.0.0";


    /**
     * Loads and initializes OpenCV library from current application package. Roughly, it's an analog of system.loadLibrary("opencv_java").
     *
     * @return Returns true is initialization of OpenCV was successful.
     */
    public static boolean initDebug() {
        return StaticHelper.initOpenCV(false);
    }

    /**
     * Loads and initializes OpenCV library from current application package. Roughly, it's an analog of system.loadLibrary("opencv_java").
     *
     * @param InitCuda load and initialize CUDA runtime libraries.
     * @return Returns true is initialization of OpenCV was successful.
     */
    public static boolean initDebug(boolean InitCuda) {
        return StaticHelper.initOpenCV(InitCuda);
    }

    /**
     * Loads and initializes OpenCV library using OpenCV Engine service.
     *
     * @param Version    OpenCV library version.
     * @param AppContext application context for connecting to the service.
     * @param Callback   object, that implements LoaderCallbackInterface for handling the connection status.
     * @return Returns true if initialization of OpenCV is successful.
     */
    public static boolean initAsync(String Version, Context AppContext,
                                    LoaderCallbackInterface Callback) {
        return AsyncServiceHelper.initOpenCV(Version, AppContext, Callback);
    }
}
