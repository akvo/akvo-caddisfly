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

import android.util.Log;

import org.opencv.core.Core;

import java.util.StringTokenizer;

class StaticHelper {

    private static final String TAG = "OpenCV/StaticHelper";

    public static boolean initOpenCV(boolean InitCuda) {
        boolean result;
        String libs = "";

        if (InitCuda) {
            loadLibrary("cudart");
            loadLibrary("nppc");
            loadLibrary("nppi");
            loadLibrary("npps");
            loadLibrary("cufft");
            loadLibrary("cublas");
        }

        Log.d(TAG, "Trying to get library list");

        try {
            System.loadLibrary("opencv_info");
            libs = getLibraryList();
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "OpenCV error: Cannot load info library for OpenCV");
        }

        Log.d(TAG, "Library list: \"" + libs + "\"");
        Log.d(TAG, "First attempt to load libs");
        if (initOpenCVLibs(libs)) {
            Log.d(TAG, "First attempt to load libs is OK");
            String eol = System.getProperty("line.separator");
            for (String str : Core.getBuildInformation().split(eol))
                Log.i(TAG, str);

            result = true;
        } else {
            Log.d(TAG, "First attempt to load libs fails");
            result = false;
        }

        return result;
    }

    private static boolean loadLibrary(String Name) {
        boolean result = true;

        Log.d(TAG, "Trying to load library " + Name);
        try {
            System.loadLibrary(Name);
            Log.d(TAG, "Library " + Name + " loaded");
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, "Cannot load library \"" + Name + "\"");
            e.printStackTrace();
            result &= false;
        }

        return result;
    }

    private static boolean initOpenCVLibs(String Libs) {
        Log.d(TAG, "Trying to init OpenCV libs");

        boolean result = true;

        if ((null != Libs) && (Libs.length() != 0)) {
            Log.d(TAG, "Trying to load libs by dependency list");
            StringTokenizer splitter = new StringTokenizer(Libs, ";");
            while (splitter.hasMoreTokens()) {
                result &= loadLibrary(splitter.nextToken());
            }
        } else {
            // If dependencies list is not defined or empty.
            result &= loadLibrary("opencv_java3");
        }

        return result;
    }

    private static native String getLibraryList();
}
