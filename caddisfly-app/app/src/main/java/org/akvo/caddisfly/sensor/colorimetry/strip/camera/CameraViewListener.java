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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.hardware.Camera;
import android.support.annotation.NonNull;

import org.akvo.caddisfly.model.TestStatus;
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

    void showBrightness(double value);

    void showShadow(double value);

    void showLevel(float[] tilts);

    void addCountToQualityCheckCount(@NonNull int[] countArray);

    void startNextPreview();

    void takeNextPicture(long delay);

    void stopCallback();

    void nextFragment();

    boolean qualityChecksOK();

    void setQualityCheckCountZero();

    void toggleFlashMode(boolean userSelect);

    boolean isTorchModeOn();

    void stopPreview();

    void startPreview();

    void showError(String message);

    void timeOut(TestStatus status);

    void showCountdownTimer(int value, double max);
}
