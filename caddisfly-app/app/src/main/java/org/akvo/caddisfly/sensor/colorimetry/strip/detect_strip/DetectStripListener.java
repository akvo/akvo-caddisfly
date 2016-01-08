package org.akvo.caddisfly.sensor.colorimetry.strip.detect_strip;

import android.graphics.Bitmap;

/**
 * Created by linda on 6/26/15.
 */
public interface DetectStripListener {

    void showSpinner();

    void showMessage(int what);

    void showMessage(String message);

    void showError(int what);

    void showImage(Bitmap bitmap);

    void showResults();

}
