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

    void sendData(byte[] data, long timeMillis,
                  int format, int width, int height, FinderPatternInfo info);

    void showProgress(int which);

    void playSound();

    void showFinderPatterns(List<FinderPattern> info, Camera.Size previewSize, int color);

    void showFocusValue(double value);

    void showMaxLuminosity(double value);

    void showShadow(double value);

    void showLevel(float[] angles);

    void setStartButtonVisibility(boolean show);

    boolean start();

    void setCountQualityCheckResult(int count);

    void setCountQualityCheckResultZero();

    void setCountQualityCheckIterationZero();
}
