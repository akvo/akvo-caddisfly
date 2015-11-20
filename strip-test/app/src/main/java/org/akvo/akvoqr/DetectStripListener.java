package org.akvo.akvoqr;

import android.graphics.Bitmap;

import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by linda on 6/26/15.
 */
public interface DetectStripListener {

    void showSpinner();

    void showMessage(int what);

    void showMessage(String message);

    void showError(int what);

    void showImage(Bitmap bitmap);

    void showResults(ArrayList<Mat> resultList);

}
