package org.akvo.akvoqr;

import android.graphics.Bitmap;

import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by linda on 6/26/15.
 */
public interface CameraViewListener {

    void getMessage(int what);

    void sendData(byte[] data, int format, int width, int height);

    void setBitmap(Bitmap bitmap);

    void sendMats(ArrayList<Mat> mats);

    Mat getCalibratedImage(Mat mat);

    void showProgress(int which);

    void dismissProgress();

    void playSound();
}
