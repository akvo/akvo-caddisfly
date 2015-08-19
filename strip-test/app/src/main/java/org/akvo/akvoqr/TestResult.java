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
    public Bitmap resultUpper;
    public Bitmap resultLower;
    public boolean testOK;
    public double minChroma;
    public double[] minChromaLab = new double[3];
    public int minChromaColor = 0;
    public int numPatchesFound;

    public TestResult() {

    }

    public void setOriginal(Mat mat)
    {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        double ratio = (double) bitmap.getHeight() / (double) bitmap.getWidth();
        int width = 400;
        int height = (int) Math.round(ratio * width);
        bitmap = Bitmap.createScaledBitmap(bitmap,width,height, false);

        this.original = bitmap;

    }
    public void setResultBitmap(Mat mat, int which)
    {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        double ratio = (double) bitmap.getHeight() / (double) bitmap.getWidth();
        int width = 400;
        int height = (int) Math.round(ratio * width);
        bitmap = Bitmap.createScaledBitmap(bitmap,width,height, false);

        switch (which)
        {
            case 0:
                this.resultUpper = bitmap;
                break;
            case 1:
                this.resultLower = bitmap;
                break;
            case 2:
                this.resultBitmap = bitmap;
        }


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
