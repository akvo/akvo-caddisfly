package org.akvo.caddisfly.sensor.striptest.utils;

import org.akvo.caddisfly.sensor.striptest.models.CalibrationCardData;
import org.akvo.caddisfly.sensor.striptest.models.DecodeData;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.HashMap;
import java.util.Map;

public class CalibrationUtils {

    @SuppressWarnings("SameParameterValue")
    private static float capValue(float val, float min, float max) {
        if (val > max) {
            return max;
        }
        return val < min ? min : val;
    }

    public static Map<String, float[]> correctIllumination(Map<String, float[]> patchYUVMap, DecodeData decodeData, CalibrationCardData calCardData) {

        float[][] whitePoints = decodeData.getWhitePointArray();
        int numRows = whitePoints.length;
        RealMatrix coefficient = new Array2DRowRealMatrix(numRows, 6);
        RealVector Y = new ArrayRealVector(numRows);

        //create constant, x, y, x^2, y^2 and xy terms
        float x, y;
        for (int i = 0; i < numRows; i++) {
            x = whitePoints[i][0];
            y = whitePoints[i][1];
            coefficient.setEntry(i, 0, x);
            coefficient.setEntry(i, 1, y);
            coefficient.setEntry(i, 2, x * x);
            coefficient.setEntry(i, 3, y * y);
            coefficient.setEntry(i, 4, x * y);
            coefficient.setEntry(i, 5, 1.0f); // constant term

            Y.setEntry(i, whitePoints[i][2]);
        }

        // solve the least squares problem
        DecompositionSolver solver = new SingularValueDecomposition(coefficient).getSolver();
        RealVector solutionL = solver.solve(Y);

        // get individual coefficients
        float Ya = (float) solutionL.getEntry(0); // x
        float Yb = (float) solutionL.getEntry(1); // y
        float Yc = (float) solutionL.getEntry(2); // x^2
        float Yd = (float) solutionL.getEntry(3); // y^2
        float Ye = (float) solutionL.getEntry(4); // x y
        float Yf = (float) solutionL.getEntry(5); // constant

        // we now have a model:
        // Y = a x + b y + c x^2 + d y^2 + e x y + constant

        // Next step is to choose the Y level we will use for the whole image. For this, we
        // compute the mean luminosity of the whole image. To compute this, we compute the volume
        // underneath the curve, which gives us the average.

        float xMax = calCardData.hSize;
        float yMax = calCardData.vSize;

        float yMean = (float) (0.5 * Ya * xMax + 0.5 * Yb * yMax + Yc * xMax * xMax / 3.0
                + Yd * yMax * yMax / 3.0 + Ye * 0.25 * xMax * yMax + Yf);

        float[] illuminationData = new float[]{Ya, Yb, Yc, Yd, Ye, Yf, yMean};
        decodeData.setIlluminationData(illuminationData);

        // now we create a new map with corrected values. U and V are unchanged
        float yNew;
        Map<String, float[]> resultYUVMap = new HashMap<>();

        for (String label : calCardData.getCalValues().keySet()) {
            CalibrationCardData.Location loc = calCardData.getLocations().get(label);
            float[] YUV = patchYUVMap.get(label);
            x = loc.x;
            y = loc.y;
            yNew = capValue(YUV[0] - (Ya * x + Yb * y + Yc * x * x + Yd * y * y
                    + Ye * x * y + Yf) + yMean, 0.0f, 255.0f);
            resultYUVMap.put(label, new float[]{yNew, YUV[1], YUV[2]});
        }
        return resultYUVMap;
    }

    // Calibrates the map using root-polynomial regression.
    // following Mackiewicz M, Finlayson GD, Hurlbert AC, Color correction using root-polynomial
    // regression, IEEE Transactions on Image processing 2015 24(5) 1460-1470

    // Following http://docs.scipy.org/doc/scipy/reference/tutorial/linalg.html#solving-linear-least-squares-problems-and-pseudo-inverses
    // we will solve P = M x
    public static Map<String, float[]> rootPolynomialCalibration(DecodeData decodeData, Map<String, float[]> calibrationXYZMap, Map<String, float[]> patchRGBMap) {
        int numPatch = calibrationXYZMap.keySet().size();
        RealMatrix coefficient = new Array2DRowRealMatrix(numPatch, 13);
        RealMatrix cal = new Array2DRowRealMatrix(numPatch, 3);
        int index = 0;

        float[] calibrationXYZ, patchRGB;

        // create coefficient and calibration vectors
        final float ONE_THIRD = 1.0f / 3.0f;
        for (String label : calibrationXYZMap.keySet()) {
            calibrationXYZ = calibrationXYZMap.get(label);
            patchRGB = patchRGBMap.get(label);

            coefficient.setEntry(index, 0, patchRGB[0]);
            coefficient.setEntry(index, 1, patchRGB[1]);
            coefficient.setEntry(index, 2, patchRGB[2]);
            coefficient.setEntry(index, 3, Math.sqrt(patchRGB[0] * patchRGB[1])); // sqrt(R * G)
            coefficient.setEntry(index, 4, Math.sqrt(patchRGB[1] * patchRGB[2])); // sqrt(G * B)
            coefficient.setEntry(index, 5, Math.sqrt(patchRGB[0] * patchRGB[2])); // sqrt(R * B)
            coefficient.setEntry(index, 6, Math.pow(patchRGB[0] * patchRGB[1] * patchRGB[1], ONE_THIRD)); // RGG ^ 1/3
            coefficient.setEntry(index, 7, Math.pow(patchRGB[1] * patchRGB[2] * patchRGB[2], ONE_THIRD)); // GBB ^ 1/3
            coefficient.setEntry(index, 8, Math.pow(patchRGB[0] * patchRGB[2] * patchRGB[2], ONE_THIRD)); // RBB ^ 1/3
            coefficient.setEntry(index, 9, Math.pow(patchRGB[1] * patchRGB[0] * patchRGB[0], ONE_THIRD)); // GRR ^ 1/3
            coefficient.setEntry(index, 10, Math.pow(patchRGB[2] * patchRGB[1] * patchRGB[1], ONE_THIRD)); // BGG ^ 1/3
            coefficient.setEntry(index, 11, Math.pow(patchRGB[2] * patchRGB[0] * patchRGB[0], ONE_THIRD)); // BRR ^ 1/3
            coefficient.setEntry(index, 12, Math.pow(patchRGB[0] * patchRGB[1] * patchRGB[2], ONE_THIRD)); // RGB ^ 1/3

            cal.setEntry(index, 0, calibrationXYZ[0]);
            cal.setEntry(index, 1, calibrationXYZ[1]);
            cal.setEntry(index, 2, calibrationXYZ[2]);
            index++;
        }

        // we solve A X = B. First we decompose A, which is the measured patches
        DecompositionSolver solver = new SingularValueDecomposition(coefficient).getSolver();

        // then we get the solution matrix X, in the case of B = calibrated values of the patches
        RealMatrix sol = solver.solve(cal);

        decodeData.setCalMatrix(sol);

        //use the solution to correct the image
        float xNew, YNew, zNew;
        Map<String, float[]> resultXYZMap = new HashMap<>();

        index = 0;
        for (String label : calibrationXYZMap.keySet()) {
            xNew = 0;
            YNew = 0;
            zNew = 0;
            for (int i = 0; i <= 12; i++) {
                xNew += coefficient.getEntry(index, i) * sol.getEntry(i, 0);
                YNew += coefficient.getEntry(index, i) * sol.getEntry(i, 1);
                zNew += coefficient.getEntry(index, i) * sol.getEntry(i, 2);
            }

            resultXYZMap.put(label, new float[]{xNew, YNew, zNew});
            index++;
        }
        return resultXYZMap;
    }
}
