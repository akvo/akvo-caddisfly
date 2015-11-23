package org.akvo.akvoqr;

import android.hardware.Camera;

import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternInfo;

import java.util.List;

/**
 * Created by linda on 6/26/15.
 */
public interface CameraViewListener {

    void getMessage(int what);

    void adjustExposureCompensation(int direction);

    void sendData(byte[] data, long timeMillis, FinderPatternInfo info);

    void dataSent();

    void playSound();

    void showFinderPatterns(List<FinderPattern> info, Camera.Size previewSize, int color);

    void showFocusValue(double value);

    void showMaxLuminosity(boolean ok, double value);

    void showShadow(double value);

    void showLevel(float[] angles);

    void setStartButtonVisibility(boolean show);

    void startNextPreview(long timeMillis);

    void takeNextPicture(long timeMillis);

    void ready();

    boolean start();

    void setCountQualityCheckResult(int count);

    void setCountQualityCheckResultZero();

    void setCountQualityCheckIterationZero();

    void setFocusAreas(List<Camera.Area> areas);
}
