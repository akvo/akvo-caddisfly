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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.hardware.Camera;

import org.akvo.caddisfly.util.detector.FinderPattern;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;

import java.util.List;

/**
 * Created by linda on 6/26/15
 */
@SuppressWarnings("deprecation")
interface CameraViewListener {

    void adjustExposureCompensation(int direction);

    void sendData(byte[] data, long timeMillis, FinderPatternInfo info);

    void dataSent();

    void playSound();

    void showFinderPatterns(List<FinderPattern> info, Camera.Size previewSize, int color);

    //void showFocusValue(double value);

    void showBrightness(double value);

    void showShadow(double value);

    void showLevel(float[] tilts);

    void addCountToQualityCheckCount(int[] countArray);

    void startNextPreview();

    void takeNextPicture(long timeMillis);

    void stopCallback();

    void nextFragment();

    boolean qualityChecksOK();

    void setQualityCheckCountZero();

    //void setFocusAreas(List<Camera.Area> areas);

    void switchFlash();
}
