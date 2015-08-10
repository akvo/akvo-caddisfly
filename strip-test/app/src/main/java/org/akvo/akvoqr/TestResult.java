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
    public Bitmap resultBitmap;
    public boolean testOK;
    public double minChroma;
    public double[] minChromaLab = new double[3];
    public int minChromaColor = 0;
    public int numPatchesFound;

    public TestResult(Bitmap bitmap, boolean resultBitmap,
                      boolean testOK, double minChroma,
                      double[] minChromaLab, int numPatchesFound) {

        if(resultBitmap)
        {
            this.resultBitmap = bitmap;
        }
        else
        {
            this.original = bitmap;
        }
        this.testOK = testOK;
        this.minChroma = minChroma;
        this.numPatchesFound = numPatchesFound;
        this.minChromaLab = minChromaLab;



    }

    public static TestResult getTestResultFromMat(Mat mat, boolean calibrated, boolean testOK,
                                                  double minChroma, double[] minChromaLab, int numPatchesFound)
    {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        return new TestResult(bitmap, calibrated, testOK, minChroma, minChromaLab, numPatchesFound);
    }

    public void setOriginal(Mat mat)
    {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        this.original = bitmap;

    }
    public void setResultBitmap(Mat mat)
    {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        this.resultBitmap = bitmap;
    }

    public void setNumPatchesFound(int num)
    {
        this.numPatchesFound = num;
    }
    public void setMinChromaColor(Point minChromaPixPos)
    {
        if(original!=null)
            minChromaColor = original.getPixel((int)minChromaPixPos.x, (int)minChromaPixPos.y);

    }
}
