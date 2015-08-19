package org.akvo.akvoqr.calibration;

import android.content.Context;

import org.akvo.akvoqr.AssetsManager;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

// Performs the calibration of the image
public class CalibrationCard{
    private final double ONE_OVER_NINE = 1.0/9;


    private CalibrationData readCalibrationFile(Context context){
        CalibrationData calData = new CalibrationData();
        String json = AssetsManager.getInstance().loadJSONFromAsset("calibration.json");
        try {
            JSONObject obj = new JSONObject(json);

            // general data
            calData.date = obj.getString("date");
            calData.cardVersion = obj.getString("cardVersion");
            calData.unit = obj.getString("unit");

            // sizes
            JSONObject calDataJSON = obj.getJSONObject("calData");
            calData.patchSize = calDataJSON.getDouble("patchSize");
            calData.hsize = calDataJSON.getDouble("hsize");
            calData.vsize = calDataJSON.getDouble("vsize");

            // locations
            JSONArray locJSON = calDataJSON.getJSONArray("locations");
            for(int i=0;i < locJSON.length();i++){
                JSONObject loc = locJSON.getJSONObject(i);
                calData.addLocation(loc.getInt("n"),loc.getDouble("x"),loc.getDouble("y"),loc.getBoolean("gray"));
            }

            // colours
            JSONArray colJSON = calDataJSON.getJSONArray("calValues");
            for(int i=0;i < colJSON.length();i++){
                JSONObject cal = colJSON.getJSONObject(i);
                calData.addCal(cal.getInt("n"), cal.getInt("R"), cal.getInt("G"), cal.getInt("B"));
            }

            // white lines
            JSONArray linesJSON = obj.getJSONObject("whiteData").getJSONArray("lines");
            for(int i=0;i < linesJSON.length();i++){
                JSONObject line = linesJSON.getJSONObject(i);
                JSONArray p = line.getJSONArray("p");
                calData.addWhiteLine(p.getDouble(0), p.getDouble(1), p.getDouble(2), p.getDouble(3), line.getDouble("width"));
            }

            // strip area
            JSONArray stripArea = obj.getJSONObject("stripAreaData").getJSONArray("area");
            calData.stripArea[0] = stripArea.getDouble(0);
            calData.stripArea[1] = stripArea.getDouble(1);
            calData.stripArea[2] = stripArea.getDouble(2);
            calData.stripArea[3] = stripArea.getDouble(3);

            System.out.println("*** parsing complete: " + calData.toString());
            return calData;
        } catch (JSONException e) {
            System.out.println("*** problem parsing JSON:" + e.toString());
            // TODO handle unable to read calibration file
            e.printStackTrace();
        }

        return null;
    }

    private int capValue(int val, int min, int max){
        return Math.max(Math.min(val,max),min);
    }

    // computes the average luminosity around a point.
    // x and y in pixels
    // This method expects a hls file
    public double getLuminosity(Mat hls, int x, int y, int dp){
        double totLum = 0;
        int totNum = 0;
        for (int i = -dp; i <= dp; i++){
            for (int ii = -dp; ii <= dp; ii++){
                totLum = totLum + hls.get(y + i, x + ii)[1]; // L is the second in the array [H,L,S]
                totNum++;
            }
        }
        return totLum / totNum;
    }

    private RealMatrix createWhitePointList(Mat hls, CalibrationData calData) {
        List<CalibrationData.WhiteLine> lines = calData.whiteLines;
        int numLines = lines.size() * 10; // on each line, we sample 10 points
        double[][] points = new double[numLines][3];
        int index = 0;
        for (CalibrationData.WhiteLine line : lines){
            double xStart = line.p[0];
            double yStart = line.p[1];
            double xEnd = line.p[2];
            double yEnd = line.p[3];
            double xdiff = (xEnd - xStart) * ONE_OVER_NINE;
            double ydiff = (yEnd - yStart) * ONE_OVER_NINE;
            int dp = (int) Math.round(line.width * calData.hfac * 0.5);
            if (dp == 0){
                dp = 1; // minimum of one pixel
            }
            // sample line
            for (int i = 0; i <= 9 ; i++){
                int xp = (int) Math.round((xStart + i * xdiff) * calData.hfac);
                int yp = (int) Math.round((yStart + i * ydiff) * calData.vfac);

                points[index * 10 + i][0] = xp;
                points[index * 10 + i][1] = yp;
                points[index * 10 + i][2] = getLuminosity(hls, xp, yp, dp);
            }
            index++;
        }

        return MatrixUtils.createRealMatrix(points);
    }

    public Mat doIlluminationCorrection(Mat imgMat, CalibrationData calData){
        System.out.println("*** ILLUM - starting illumination correction");
        // create HLS image for homogenous illumination calibration
        int pheight =  imgMat.rows();
        int pwidth = imgMat.cols();
        Mat hls = new Mat(pheight, pwidth, CvType.CV_8UC3);
        Imgproc.cvtColor(imgMat, hls, Imgproc.COLOR_BGR2HLS_FULL, imgMat.channels());

        RealMatrix points = createWhitePointList(hls, calData);

        // create coefficient matrix
        // third row is the constant 1
        RealMatrix coef = new Array2DRowRealMatrix(points.getRowDimension(),3);
        coef.setColumnMatrix(0,points.getColumnMatrix(0));
        coef.setColumnMatrix(1,points.getColumnMatrix(1));
        for (int i = 0; i < points.getRowDimension(); i++){
            coef.setEntry(i,2,1d);
        }

        RealVector L = points.getColumnVector(2);
        DecompositionSolver solver = new SingularValueDecomposition(coef).getSolver();
        RealVector solution = solver.solve(L);
        double a = solution.getEntry(0);
        double b = solution.getEntry(1);
        double c = solution.getEntry(2);

        // compute mean (the luminosity value of the plane in the middle of the image)
        double Lmean = a * calData.hsizePixel * 0.5 + b * calData.vsizePixel * 0.5 + c;

        // correct image
        // we do this per row. We tried to do it in one block, but there is no speed difference.
        byte[] temp = new byte[hls.cols() * hls.channels()];
        for (int i = 0; i < hls.rows(); i++){ // y
            hls.get(i, 0, temp);
            for (int ii = 0; ii < hls.cols(); ii++){  //x
                int val = (int) Math.round((temp[ii * 3 + 1] & 0xFF) - (a * ii + b * i + c) + Lmean);
                val = capValue(val, 0, 255);
                temp[ii * 3 + 1] = (byte) val;
            }
            hls.put(i, 0, temp);
        }
        System.out.println("*** ILLUM - illumination correction done ");

        Imgproc.cvtColor(hls, imgMat, Imgproc.COLOR_HLS2BGR_FULL, imgMat.channels());
        return imgMat;
    }

    private double[] measurePatch(Mat imgMat, double x, double y, CalibrationData calData){
        double[] BGRresult = new double[3];
        double totB = 0;
        double totG = 0;
        double totR = 0;
        int totNum = 0;

        int xp = (int) Math.round(x * calData.hfac);
        int yp = (int) Math.round(y * calData.vfac);
        int dp = (int) Math.round(calData.patchSize * calData.hfac * 0.25);
        for (int i = -dp; i <= dp; i++){
            for (int ii = -dp; ii <= dp; ii++){
                totB += imgMat.get(yp + i, xp + ii)[0];
                totG += imgMat.get(yp + i, xp + ii)[1];
                totR += imgMat.get(yp + i, xp + ii)[2];
                totNum++;
            }
        }
        BGRresult[0] = totB/totNum;
        BGRresult[1] = totG/totNum;
        BGRresult[2] = totR/totNum;
        return BGRresult;
    }

    private Mat create1DLUT(double[] coeffB, double[] coeffG, double[] coeffR) {
        Mat lutMat = new Mat(1, 256, CvType.CV_8UC3);

        int size = (int)(lutMat.total() * lutMat.channels());
        byte[] temp = new byte[size];
        lutMat.get(0, 0, temp);
        int B;
        int G;
        int R;
        for(int i = 0; i < 256; ++i){
            double detB = coeffB[1]*coeffB[1] - 4.0 * coeffB[2] * (coeffB[0]-i);
            double detG = coeffG[1]*coeffG[1] - 4.0 * coeffG[2] * (coeffG[0]-i);
            double detR = coeffR[1]*coeffR[1] - 4.0 * coeffR[2] * (coeffR[0]-i);

            if (detB < 0) {
                B = i;
            } else {
                B = (int) Math.round((-coeffB[1] + Math.sqrt(detB)) / (2 * coeffB[2]));
            }

            if (detG < 0) {
                G = i;
            } else {
                G = (int) Math.round((-coeffG[1] + Math.sqrt(detG)) / (2 * coeffG[2]));
            }

            if (detR < 0) {
                R = i;
            } else {
                R = (int) Math.round((-coeffR[1] + Math.sqrt(detR)) / (2 * coeffR[2]));
            }

            // cap values
            B = capValue(B,0,255);
            G = capValue(G,0,255);
            R = capValue(R,0,255);

            // put value in lut
            temp[3 * i] = (byte) B;
            temp[3 * i + 1] = (byte) G;
            temp[3 * i + 2] = (byte) R;
        }
        lutMat.put(0, 0, temp);
        return lutMat;
    }

    private Mat do1DLUTCorrection(Mat imgMat, CalibrationData calData) {
        // System.out.println("*** 1D-LUT - starting 1D LUT");
        final WeightedObservedPoints obsB = new WeightedObservedPoints();
        final WeightedObservedPoints obsG = new WeightedObservedPoints();
        final WeightedObservedPoints obsR = new WeightedObservedPoints();

        // iterate over all patches
        for (int no : calData.locations.keySet()){
            CalibrationData.Location loc = calData.locations.get(no);
            if (loc.grayPatch){
                //include this point
                double[] BGRcol = measurePatch(imgMat,loc.x,loc.y,calData); // measure patch colour

                obsB.add(calData.calValues.get(no).B, BGRcol[0]);
                obsG.add(calData.calValues.get(no).G, BGRcol[1]);
                obsR.add(calData.calValues.get(no).R, BGRcol[2]);
            }
        }
        // Instantiate a second-degree polynomial fitter.
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);

        // Retrieve fitted parameters (coefficients of the polynomial function).
        // order of coefficients is (c + bx + ax^2), so [c,b,a]
        final double[] coeffB = fitter.fit(obsB.toList());
        final double[] coeffG = fitter.fit(obsG.toList());
        final double[] coeffR = fitter.fit(obsR.toList());

        // create lookup table
        Mat lut = create1DLUT(coeffB, coeffG, coeffR);
        Mat imgcorr = imgMat.clone();
        Core.LUT(imgMat, lut, imgcorr);
        // System.out.println("*** 1D-LUT - ending 1D LUT");
        return imgcorr;
    }

    private Mat do3DLUTCorrection(Mat imgMat, CalibrationData calData) {
        // System.out.println("*** 3D-LUT - starting 3D LUT");

        int total = calData.locations.keySet().size();
        RealMatrix coef = new Array2DRowRealMatrix(total,3);
        RealMatrix cal = new Array2DRowRealMatrix(total,3);
        int index = 0;
        for (int no : calData.locations.keySet()){
            CalibrationData.Location loc = calData.locations.get(no);
            double[] BGRcol = measurePatch(imgMat,loc.x,loc.y,calData); // measure patch colour

            coef.setEntry(index,0,BGRcol[0]);
            coef.setEntry(index,1,BGRcol[1]);
            coef.setEntry(index,2,BGRcol[2]);
            cal.setEntry(index,0,calData.calValues.get(no).B);
            cal.setEntry(index,1,calData.calValues.get(no).G);
            cal.setEntry(index,2,calData.calValues.get(no).R);
            index++;
        }
        DecompositionSolver solver = new SingularValueDecomposition(coef).getSolver();
        RealMatrix sol = solver.solve(cal);

        double a_b, b_b, c_b, a_g, b_g, c_g, a_r, b_r, c_r;
        a_b = sol.getEntry(0,0);
        b_b = sol.getEntry(1,0);
        c_b = sol.getEntry(2,0);
        a_g = sol.getEntry(0,1);
        b_g = sol.getEntry(1,1);
        c_g = sol.getEntry(2,1);
        a_r = sol.getEntry(0,2);
        b_r = sol.getEntry(1,2);
        c_r = sol.getEntry(2,2);

        //use the solution to correct the patch
        int Btemp, Gtemp, Rtemp, Bnew, Gnew, Rnew;

        byte[] temp = new byte[imgMat.cols() * imgMat.channels()];
        for (int i = 0; i < imgMat.rows(); i++){ // y
            imgMat.get(i, 0, temp);
            for (int ii = 0; ii < imgMat.cols(); ii++){  //x
                Btemp = temp[ii * 3] & 0xFF;
                Gtemp = temp[ii * 3 + 1] & 0xFF;
                Rtemp = temp[ii * 3 + 2] & 0xFF;

                Bnew = (int) Math.round(c_b * Btemp + b_b * Gtemp + a_b * Rtemp);
                Gnew = (int) Math.round(c_g * Btemp + b_g * Gtemp + a_g * Rtemp);
                Rnew = (int) Math.round(c_r * Btemp + b_r * Gtemp + a_r * Rtemp);

                // cap values
                Bnew = capValue(Bnew,0,255);
                Gnew = capValue(Gnew,0,255);
                Rnew = capValue(Rnew,0,255);

                temp[ii * 3] = (byte) Bnew;
                temp[ii * 3 + 1] = (byte) Gnew;
                temp[ii * 3 + 2] = (byte) Rnew;
            }
            imgMat.put(i, 0, temp);
        }
        // System.out.println("*** 3D-LUT - ending 3D LUT");
        return imgMat;
    }

    // TODO fix order of R,G,B, by fixing the 3D lut order
    private void addPatch(Mat imgMat, Double x, Double y, CalibrationData.CalValue calValue,CalibrationData calData) {
        int xp = (int) Math.round(x * calData.hfac);
        int yp = (int) Math.round(y * calData.vfac);
        int dp = (int) Math.round(calData.patchSize * calData.hfac * 0.125);
        for (int i = -dp; i <= dp; i++){
            for (int ii = -dp; ii <= dp; ii++){
                byte[] col = new byte[3];
                // accidentaly puts RGB in right order for move to bitmap
                col[0] = (byte) calValue.R;
                col[1] = (byte) calValue.G;
                col[2] = (byte) calValue.B;
                imgMat.put(yp + i, xp + ii,col);
            }
        }
    }

    private void addCalColours(Mat imgMat, CalibrationData calData) {
        for (int no : calData.locations.keySet()){
            CalibrationData.Location loc = calData.locations.get(no);
            addPatch(imgMat,loc.x,loc.y,calData.calValues.get(no),calData);
        }
    }

    public Mat calibrateImage(Context context, Mat imgMat){
        System.out.println("*** start of calibration");
        // read calibration info
        // System.out.println("*** about to read calibration file");
        CalibrationData calData = readCalibrationFile(context);

        if(calData!=null) {
            calData.hsizePixel = imgMat.cols();
            calData.hfac = calData.hsizePixel / calData.hsize; // pixel per mm
            calData.vsizePixel = imgMat.rows();
            calData.vfac = calData.vsizePixel / calData.vsize; // pixel per mm

            // illumination correction
            imgMat = doIlluminationCorrection(imgMat, calData);

            // 1D LUT gray balance
            imgMat = do1DLUTCorrection(imgMat, calData);

            // 3D LUT color balance
            imgMat = do3DLUTCorrection(imgMat, calData);

            // insert calibration colours in image
            addCalColours(imgMat, calData);

            System.out.println("*** end of calibration");
            return imgMat;
        }
        return null;
    }
}
