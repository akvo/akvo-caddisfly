package org.akvo.caddisfly.sensor.colorimetry.strip.camera_strip;

import android.hardware.Camera;

import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.FinderPattern;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.FinderPatternInfo;

import java.util.List;

/**
 * Created by linda on 6/26/15.
 */
interface CameraViewListener {

    void adjustExposureCompensation(int direction);

    void sendData(byte[] data, long timeMillis, FinderPatternInfo info);

    void dataSent();

    void playSound();

    void showFinderPatterns(List<FinderPattern> info, Camera.Size previewSize, int color);

    void showFocusValue(double value);

    void showBrightness(double value);

    void showShadow(double value);

    void showLevel(float[] angles);

    void addCountToQualityCheckCount(int[] countArray);

    void startNextPreview(long timeMillis);

    void takeNextPicture(long timeMillis);

    void stopCallback(boolean stop);

    void nextFragment();

    boolean qualityChecksOK();

    void setQualityCheckCountZero();

    void setFocusAreas(List<Camera.Area> areas);

    void switchFlash();
}
