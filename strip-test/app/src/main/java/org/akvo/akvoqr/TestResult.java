package org.akvo.akvoqr;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 * Created by linda on 8/8/15.
 */
public class TestResult {
    public Bitmap original;
    public Bitmap calibrated;
    public boolean testOK;
    public double minChroma;
    public int minChromaColor;
    public int numPatchesFound;

    public TestResult(Bitmap bitmap, boolean calibrated,
                      boolean testOK, double minChroma, Point minChromaPixPos,  int numPatchesFound) {

        if(calibrated)
        {
            this.calibrated = bitmap;
        }
        else
        {
            this.original = bitmap;
        }
        this.testOK = testOK;
        this.minChroma = minChroma;
        this.numPatchesFound = numPatchesFound;

       minChromaColor = bitmap.getPixel((int)minChromaPixPos.x, (int)minChromaPixPos.y);
    }

    public static TestResult getTestResultFromMat(Mat mat, boolean calibrated, boolean testOK,
                                                  double minChroma, Point minChromaPixPos, int numPatchesFound)
    {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        return new TestResult(bitmap, calibrated, testOK, minChroma, minChromaPixPos, numPatchesFound);
    }
}
