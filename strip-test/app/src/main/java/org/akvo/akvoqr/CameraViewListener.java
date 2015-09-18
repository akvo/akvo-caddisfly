package org.akvo.akvoqr;

import android.hardware.Camera;

import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 6/26/15.
 */
public interface CameraViewListener {

    void getMessage(int what);

    void sendData(byte[] data, long timeMillis,
                  int format, int width, int height, FinderPatternInfo info, double mSize);

    void sendMats(ArrayList<Mat> mats);

    Mat getCalibratedImage(Mat mat);

    void showProgress(int which);

    void dismissProgress();

    void playSound();

    String getBrandName();

    void showFinderPatterns(List<FinderPattern> info, Camera.Size previewSize, int color);

    void showFocusValue(double value);

    void showMaxLuminosity(double value);

    double getTimeLapseForPatch();

    boolean start();
}
