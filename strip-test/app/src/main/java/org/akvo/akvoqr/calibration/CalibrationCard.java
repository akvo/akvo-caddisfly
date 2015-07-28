package org.akvo.akvoqr.calibration;

import org.opencv.core.Mat;

/**
 * Created by linda on 7/27/15.
 */
public class CalibrationCard {

    // myQPCard 201 measurements
    final float P_SIZE = 9.9f; // mm
    final float P_DIST = 2f; // mm
    final float XOFF = 5.6f; // mm
    final float YOFF = 5.6f; // mm
    final float F_DIST_HOR = 131.8f; // mm
    final float F_DIST_VER = 72.3f; // mm

    private Patch[] createQPCardCalTable(){
        Patch[] calArray = new Patch[30];
        calArray[0] = new Patch(184,186,186,0,0,0);
        calArray[1] = new Patch(142,89,66,0,0,0);
        calArray[2] = new Patch(64,201,230,0,0,0);
        calArray[3] = new Patch(64,64,64,0,0,0);
        calArray[4] = new Patch(94,94,94,0,0,0);
        calArray[5] = new Patch(117,116,115,0,0,0);
        calArray[6] = new Patch(157,159,159,0,0,0);
        calArray[7] = new Patch(212,214,214,0,0,0);
        calArray[8] = new Patch(238,241,239,0,0,0);
        calArray[9] = new Patch(184,186,186,0,0,0);
        calArray[10] = new Patch(46,180,179,0,0,0);
        calArray[11] = new Patch(109,161,92,0,0,0);
        calArray[12] = new Patch(122,81,171,0,0,0);
        calArray[13] = new Patch(80,66,76,0,0,0);
        calArray[14] = new Patch(69,104,93,0,0,0);
        calArray[15] = new Patch(62,121,136,0,0,0);
        calArray[16] = new Patch(71,83,105,0,0,0);
        calArray[17] = new Patch(128,148,177,0,0,0);
        calArray[18] = new Patch(216,219,203,0,0,0);
        calArray[19] = new Patch(212,193,202,0,0,0);
        calArray[20] = new Patch(184,186,186,0,0,0);
        calArray[21] = new Patch(57,50,130,0,0,0);
        calArray[22] = new Patch(189,149,81,0,0,0);
        calArray[23] = new Patch(69,61,101,0,0,0);
        calArray[24] = new Patch(157,126,108,0,0,0);
        calArray[25] = new Patch(49,112,209,0,0,0);
        calArray[26] = new Patch(37,166,233,0,0,0);
        calArray[27] = new Patch(170,207,234,0,0,0);
        calArray[28] = new Patch(193,219,217,0,0,0);
        calArray[29] = new Patch(184,186,186,0,0,0);

        return calArray;
    }


    public Patch measurePatch(Mat imgMat, int x, int y, int d)
    {
        float totalB = 0;
        float totalG = 0;
        float totalR = 0;
        int totalNum = 0;

        for (int i=-d;i<=d;i++) {
            for(int ii=-d;ii<=d;ii++) {
                totalB += imgMat.get(y,x)[2];
                totalG += imgMat.get(y,x)[1];
                totalR += imgMat.get(y,x)[0];
                totalNum ++;
            }
        }
        Patch patch = new Patch(totalB/totalNum, totalG/totalNum, totalR/totalNum, x, y, d);
        return patch;
    }


    public Patch[] measurePatches(Mat imgMat){
        int bWidth = imgMat.cols();
        int bHeight = imgMat.rows();
        float facX = bWidth / F_DIST_HOR;
        float facY = bHeight / F_DIST_VER;
        int d = (int) Math.round(0.25f * (P_SIZE * facX));
        int x;
        int y;
        int pNum;
        Patch[] patchArray = new Patch[30];
        for (int i = 0 ; i < 3; i++){
            for (int ii = 0; ii < 10 ; ii++) {
                pNum = 29 - 10 * i -ii;
                x = (int) Math.round(facX * (XOFF + ii * (P_DIST + P_SIZE) + 0.5 * P_SIZE));
                y = (int) Math.round(facY * (YOFF + i * (P_DIST + P_SIZE) + 0.5 * P_SIZE));;
                patchArray[pNum] = measurePatch(imgMat, x, y, d);
            }
        }
        return patchArray;
    }

    public void calibrateImage(Mat imgMat){
        // measure color patches
        Patch[] patchArray = measurePatches(imgMat);

        // get gray patch values

    }
}
