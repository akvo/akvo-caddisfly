package org.akvo.akvoqr.calibration;

import org.akvo.akvoqr.detector.BitMatrix;
import org.akvo.akvoqr.detector.Detector;
import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.MathUtils;
import org.akvo.akvoqr.detector.ResultPoint;
import org.akvo.akvoqr.util.AssetsManager;
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
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import static org.akvo.akvoqr.opencv.OpenCVUtils.getOrderedPoints;

// Performs the calibration of the image
public class CalibrationCard{
    public static final int CODE_NOT_FOUND = 1;//temporary hack to work with current test card, was -1
    private final double ONE_OVER_NINE = 1.0/9;
    private static CalibrationCard instance;
    private static CalibrationData calData;
    private static int calVersionNumber = 1;//CODE_NOT_FOUND;

    //Constructor to use if versionNumber is set previously or does not matter
    public static CalibrationCard getInstance()
    {
        return getInstance(calVersionNumber);
    }

    public static CalibrationCard getInstance(int versionNumber)
    {
        if(instance==null) {
            instance = new CalibrationCard();
        }

       // if(calVersionNumber!=versionNumber) {
            calVersionNumber = versionNumber;
            calData = readCalibrationFile();
       // }

        return instance;
    }

    private static CalibrationData readCalibrationFile(){

        String calFileName = "calibration" + calVersionNumber + ".json";
        String json = AssetsManager.getInstance().loadJSONFromAsset(calFileName);

        try {

            CalibrationData calData = new CalibrationData();

            if(json!=null) {
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
                for (int i = 0; i < locJSON.length(); i++) {
                    JSONObject loc = locJSON.getJSONObject(i);
                    calData.addLocation(loc.getString("l"), loc.getDouble("x"), loc.getDouble("y"), loc.getBoolean("gray"));
                }

                // colours
                JSONArray colJSON = calDataJSON.getJSONArray("calValues");
                for (int i = 0; i < colJSON.length(); i++) {
                    JSONObject cal = colJSON.getJSONObject(i);
                    calData.addCal(cal.getString("l"), cal.getInt("R"), cal.getInt("G"), cal.getInt("B"));
                }

                // white lines
                JSONArray linesJSON = obj.getJSONObject("whiteData").getJSONArray("lines");
                for (int i = 0; i < linesJSON.length(); i++) {
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
            }

        } catch (JSONException e) {
            System.out.println("*** problem parsing JSON:" + e.toString());

        }

        return null;
    }

    private int capValue(int val, int min, int max){
        if (val > max) {
            return max;
        }
        return val < min ? min : val;
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

    public double[][] createWhitePointArray(Mat hls)
    {

        List<CalibrationData.WhiteLine> lines = calData.whiteLines;
        int numLines = lines.size() * 10; // on each line, we sample 10 points
        double[][] points = new double[numLines][3];
        int index = 0;

        calData.hsizePixel = hls.cols();
        double hfac = calData.hsizePixel / calData.hsize; // pixel per mm
        calData.vsizePixel = hls.rows();
        double vfac = calData.vsizePixel / calData.vsize; // pixel per mm

        for (CalibrationData.WhiteLine line : lines){
            double xStart = line.p[0];
            double yStart = line.p[1];
            double xEnd = line.p[2];
            double yEnd = line.p[3];
            double xdiff = (xEnd - xStart) * ONE_OVER_NINE;
            double ydiff = (yEnd - yStart) * ONE_OVER_NINE;
            int dp = (int) Math.round(line.width * hfac * 0.5);
            if (dp == 0){
                dp = 1; // minimum of one pixel
            }

            // sample line
            for (int i = 0; i <= 9 ; i++){
                int xp = (int) Math.round((xStart + i * xdiff) * hfac);
                int yp = (int) Math.round((yStart + i * ydiff) * vfac);

                points[index * 10 + i][0] = xp;
                points[index * 10 + i][1] = yp;
                points[index * 10 + i][2] = getLuminosity(hls, xp, yp, dp);
            }

            index++;
        }

        return points;
    }
    private RealMatrix createWhitePointMatrix(Mat hls) {

        double[][] points = createWhitePointArray(hls);
        return MatrixUtils.createRealMatrix(points);
    }

    public Mat doIlluminationCorrection(Mat imgMat){
        // create HLS image for homogenous illumination calibration
        int pheight =  imgMat.rows();
        int pwidth = imgMat.cols();
        Mat hls = new Mat(pheight, pwidth, CvType.CV_8UC3);

        Imgproc.cvtColor(imgMat, hls, Imgproc.COLOR_BGR2HLS_FULL, imgMat.channels());

        RealMatrix points = createWhitePointMatrix(hls);

        // create coefficient matrix
        // the model is Luminosity = ax + bx^2 + cy + dy^2 + exy + f
        // third row is the constant 1
        RealMatrix coef = new Array2DRowRealMatrix(points.getRowDimension(),6);
        coef.setColumnMatrix(0,points.getColumnMatrix(0));
        coef.setColumnMatrix(2,points.getColumnMatrix(1));

        //create constant, x^2, y^2 and xy terms
        for (int i = 0; i < points.getRowDimension(); i++){
            coef.setEntry(i,1,Math.pow(coef.getEntry(i,0),2)); // x^2
            coef.setEntry(i,3,Math.pow(coef.getEntry(i,2), 2)); // y^2
            coef.setEntry(i,4,coef.getEntry(i,0) * coef.getEntry(i,2));
            coef.setEntry(i,5,1d); // constant
        }

        RealVector L = points.getColumnVector(2);
        DecompositionSolver solver = new SingularValueDecomposition(coef).getSolver();
        RealVector solution = solver.solve(L);
        float a = (float)solution.getEntry(0);
        float b = (float)solution.getEntry(1);
        float c = (float)solution.getEntry(2);
        float d = (float)solution.getEntry(3);
        float e = (float)solution.getEntry(4);
        float f = (float)solution.getEntry(5);


        // compute mean (the luminosity value of the plane in the middle of the image)
        float Lmean = (float) (0.5 * a * pwidth + 0.5 * c * pheight + b * pwidth * pwidth / 3.0 + d * pheight * pheight / 3.0 + e * 0.25 * pheight * pwidth + f);

        // correct image
        // we do this per row. We tried to do it in one block, but there is no speed difference.
        byte[] temp = new byte[hls.cols() * hls.channels()];
        int val = 0;
        int ii;
        int ii3;
        int hlsCols = hls.cols();
        for (int i = 0; i < hls.rows(); i++){ // y
            hls.get(i, 0, temp);
            ii3 = 0;
            for (ii = 0; ii < hlsCols; ii++){  //x
                val = (int) Math.round((temp[ii3 + 1] & 0xFF) - (a * ii + b * ii * ii + c * i + d * i * i + e * i * ii + f) + Lmean);
                val = capValue(val, 0, 255);
                temp[ii3 + 1] = (byte) val;
                ii3 += 3;
            }
            hls.put(i, 0, temp);
        }

        Imgproc.cvtColor(hls, imgMat, Imgproc.COLOR_HLS2BGR_FULL, imgMat.channels());
        return imgMat;
    }

    private float[] measurePatch(Mat imgMat, double x, double y){
        float[] BGRresult = new float[3];
        float totB = 0;
        float totG = 0;
        float totR = 0;
        int totNum = 0;

        calData.hsizePixel = imgMat.cols();
        double hfac = calData.hsizePixel / calData.hsize; // pixel per mm
        calData.vsizePixel = imgMat.rows();
        double vfac = calData.vsizePixel / calData.vsize; // pixel per mm

        int xp = (int) Math.round(x * hfac);
        int yp = (int) Math.round(y * vfac);
        int dp = (int) Math.round(calData.patchSize * hfac * 0.25);
        byte[] temp = new byte[(2 * dp + 1) * imgMat.channels()];
        int ii3;
        for (int i = -dp; i <= dp; i++){
            imgMat.get(yp - i, xp - dp, temp);
            ii3 = 0;
            for (int ii = 0; ii <= 2 * dp; ii++){
                totB += temp[ii3] & 0xFF; //imgMat.get(yp + i, xp + ii)[0];
                totG += temp[ii3 + 1] & 0xFF; //imgMat.get(yp + i, xp + ii)[1];
                totR += temp[ii3 + 2] & 0xFF; //imgMat.get(yp + i, xp + ii)[2];
                totNum++;
                ii3 += 3;
            }
        }
        BGRresult[0] = totB/totNum;
        BGRresult[1] = totG/totNum;
        BGRresult[2] = totR/totNum;
        return BGRresult;
    }

    private Mat create1DLUT(double[] coeffB, double[] coeffG, double[] coeffR) {
        Mat lutMat = new Mat(1, 256, CvType.CV_8UC3);

        // create storage area for lut, and put it in an array
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

    private Mat create1DLUT2(double[] coeffB, double[] coeffG, double[] coeffR) {
        Mat lutMat = new Mat(1, 256, CvType.CV_8UC3);

        // create storage area for lut, and put it in an array
        int size = (int)(lutMat.total() * lutMat.channels());
        byte[] temp = new byte[size];
        lutMat.get(0, 0, temp);

        int B;
        int G;
        int R;
        int isq;
        for(int i = 0; i < 256; ++i){
            isq = i * i;
            B = (int) Math.round(coeffB[2] * isq + coeffB[1] * i + coeffB[0]);
            G = (int) Math.round(coeffG[2] * isq + coeffG[1] * i + coeffG[0]);
            R = (int) Math.round(coeffG[2] * isq + coeffR[1] * i + coeffR[0]);

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

    private Mat do1DLUTCorrection(Mat imgMat) {
        final WeightedObservedPoints obsB = new WeightedObservedPoints();
        final WeightedObservedPoints obsG = new WeightedObservedPoints();
        final WeightedObservedPoints obsR = new WeightedObservedPoints();

        // iterate over all patches
        for (String label : calData.calValues.keySet()){
            CalibrationData.CalValue cal = calData.calValues.get(label);
            CalibrationData.Location loc = calData.locations.get(label);
            if (loc.grayPatch){
                //include this point
                float[] BGRcol = measurePatch(imgMat,loc.x,loc.y); // measure patch colour
                obsB.add(BGRcol[0],cal.B);
                obsG.add(BGRcol[1],cal.G);
                obsR.add(BGRcol[2],cal.R);
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
//        Mat lut = create1DLUT(coeffB, coeffG, coeffR);
        Mat lut = create1DLUT2(coeffB, coeffG, coeffR);
        Mat imgcorr = imgMat.clone();

        try {
            Core.LUT(imgMat, lut, imgcorr);
        }
        catch (Exception e)
        {
            return null;
        }
        return imgcorr;
    }

    private Mat do3DLUTCorrection(Mat imgMat) {
        int total = calData.locations.keySet().size();
        RealMatrix coef = new Array2DRowRealMatrix(total,3);
        RealMatrix cal = new Array2DRowRealMatrix(total,3);
        int index = 0;

        for (String label : calData.calValues.keySet()){
            CalibrationData.CalValue calv = calData.calValues.get(label);
            CalibrationData.Location loc = calData.locations.get(label);
            float[] BGRcol = measurePatch(imgMat,loc.x,loc.y); // measure patch colour

            coef.setEntry(index,0,BGRcol[0]);
            coef.setEntry(index,1,BGRcol[1]);
            coef.setEntry(index,2,BGRcol[2]);

            cal.setEntry(index,0,calv.B);
            cal.setEntry(index,1,calv.G);
            cal.setEntry(index,2,calv.R);
            index++;
        }
        DecompositionSolver solver = new SingularValueDecomposition(coef).getSolver();
        RealMatrix sol = solver.solve(cal);

        float a_b, b_b, c_b, a_g, b_g, c_g, a_r, b_r, c_r;
        a_b = (float)sol.getEntry(0,0);
        b_b = (float)sol.getEntry(1,0);
        c_b = (float)sol.getEntry(2,0);
        a_g = (float)sol.getEntry(0,1);
        b_g = (float)sol.getEntry(1,1);
        c_g = (float)sol.getEntry(2,1);
        a_r = (float)sol.getEntry(0,2);
        b_r = (float)sol.getEntry(1,2);
        c_r = (float)sol.getEntry(2,2);

        // create luts
        float[] A_B = new float[256];
        float[] B_B = new float[256];
        float[] C_B = new float[256];
        float[] A_G = new float[256];
        float[] B_G = new float[256];
        float[] C_G = new float[256];
        float[] A_R = new float[256];
        float[] B_R = new float[256];
        float[] C_R = new float[256];

        for (int i = 0; i < 256; i++){
            A_B[i] = a_b * i;
            B_B[i] = b_b * i;
            C_B[i] = c_b * i;
            A_G[i] = a_g * i;
            B_G[i] = b_g * i;
            C_G[i] = c_g * i;
            A_R[i] = a_r * i;
            B_R[i] = b_r * i;
            C_R[i] = c_r * i;
        }

        //use the solution to correct the image
        int Btemp, Gtemp, Rtemp, Bnew, Gnew, Rnew;
        int ii3;
        byte[] temp = new byte[imgMat.cols() * imgMat.channels()];
        for (int i = 0; i < imgMat.rows(); i++){ // y
            imgMat.get(i, 0, temp);
            ii3 = 0;
            for (int ii = 0; ii < imgMat.cols(); ii++){  //x
                Btemp = temp[ii3] & 0xFF;
                Gtemp = temp[ii3 + 1] & 0xFF;
                Rtemp = temp[ii3 + 2] & 0xFF;

                Bnew = (int) Math.round(A_B[Btemp] + B_B[Gtemp] + C_B[Rtemp]);
                Gnew = (int) Math.round(A_G[Btemp] + B_G[Gtemp] + C_G[Rtemp]);
                Rnew = (int) Math.round(A_R[Btemp] + B_R[Gtemp] + C_R[Rtemp]);

                // cap values
                Bnew = capValue(Bnew,0,255);
                Gnew = capValue(Gnew,0,255);
                Rnew = capValue(Rnew,0,255);

                temp[ii3] = (byte) Bnew;
                temp[ii3 + 1] = (byte) Gnew;
                temp[ii3 + 2] = (byte) Rnew;

                ii3 += 3;
            }
            imgMat.put(i, 0, temp);
        }
        // System.out.println("*** 3D-LUT - ending 3D LUT");
        return imgMat;
    }

    private void addPatch(Mat imgMat, Double x, Double y, CalibrationData.CalValue calValue) {

        calData.hsizePixel = imgMat.cols();
        double hfac = calData.hsizePixel / calData.hsize; // pixel per mm
        calData.vsizePixel = imgMat.rows();
        double vfac = calData.vsizePixel / calData.vsize; // pixel per mm

        int xp = (int) Math.round(x * hfac);
        int yp = (int) Math.round(y * vfac);
        int dp = (int) Math.round(calData.patchSize * hfac * 0.150);
        for (int i = -dp; i <= dp; i++){
            for (int ii = -dp; ii <= dp; ii++){
                byte[] col = new byte[3];
                col[0] = (byte) calValue.B;
                col[1] = (byte) calValue.G;
                col[2] = (byte) calValue.R;
                imgMat.put(yp + i, xp + ii,col);
            }
        }
    }

    private void addCalColours(Mat imgMat) {
        for (String label : calData.locations.keySet()){
            CalibrationData.Location loc = calData.locations.get(label);
            addPatch(imgMat,loc.x,loc.y,calData.calValues.get(label));
        }
    }

    public Mat calibrateImage(Mat imgMat){
        System.out.println("*** start of calibration");
        // read calibration info
        // System.out.println("*** about to read calibration file");

        if(calData!=null) {
            try {

                // illumination correction
                System.out.println("*** ILLUM - starting illumination correction");
                imgMat = doIlluminationCorrection(imgMat);

                // 1D LUT gray balance
                System.out.println("*** ILLUM - starting gray balance");
                imgMat = do1DLUTCorrection(imgMat);

                // 3D LUT color balance
                System.out.println("*** ILLUM - starting 3D lut");
                imgMat = do3DLUTCorrection(imgMat);

                // insert calibration colours in image
                System.out.println("*** ILLUM - adding colours");
                addCalColours(imgMat);

                System.out.println("*** end of calibration");
                return imgMat;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * find and decode the code of the callibration card
     * The code is stored as a simple barcode. It starts 4.5 modules from the center of the bottom left finder pattern
     * and extends to module 29.5.
     * It has 12 bits, of 2 modules wide each.
     * It starts and ends with a 1 bit.
     * The remaining 10 bits are interpreted as a 9 bit number with the last bit as parity bit.
     *
     * @param patternInfo
     */
    public static int decodeCallibrationCardCode(List<FinderPattern> patternInfo, BitMatrix image) {
        // order points
        if (patternInfo.size() == 4) {
            double[] p1 = new double[]{patternInfo.get(0).getX(), patternInfo.get(0).getY()};
            double[] p2 = new double[]{patternInfo.get(1).getX(), patternInfo.get(1).getY()};
            double[] p3 = new double[]{patternInfo.get(2).getX(), patternInfo.get(2).getY()};
            double[] p4 = new double[]{patternInfo.get(3).getX(), patternInfo.get(3).getY()};

            // sort points in order top-left, bottom-left, top-right, bottom-right
            List<Point> points = getOrderedPoints(p1,p2,p3,p4);

            //if camera is in portrait mode, bottomLeft is third in the sorted list
//            ResultPoint bottomLeft = new ResultPoint((float) points.get(2).x,(float) points.get(2).y);
//            ResultPoint bottomRight = new ResultPoint((float) points.get(3).x,(float) points.get(3).y);

            //because camera is in portrait mode, we need bottom-right and top-right patterns:
            //the version number barcode lies next to bottom-right
            ResultPoint bottomLeft = new ResultPoint((float) points.get(3).x,(float) points.get(3).y);
            ResultPoint bottomRight = new ResultPoint((float) points.get(1).x,(float) points.get(1).y);

            // get estimated module size
            Detector detector = new Detector(image);
            float modSizeHor = detector.calculateModuleSize(bottomLeft, bottomRight, bottomRight);

            // go from one finder pattern to the other,
            //because camera is in portrait mode, we need to shift x and y
            double lrx = -bottomRight.getX() + bottomLeft.getX();
            double lry = -bottomRight.getY() + bottomLeft.getY();
            double hNorm = MathUtils.distance(bottomLeft.getX(), bottomLeft.getY(),
                    bottomRight.getX(), bottomRight.getY());

            // check if left and right are ok
            if (lry < 0) {
                System.out.println("***decodeCallibrationCard lry < 0");
                return CODE_NOT_FOUND;
            }

            // create vector of length 1 pixel, in the direction of the bottomRight finder pattern
            lrx /= hNorm;
            lry /= hNorm;

            // sample line into new row
            boolean[] bits = new boolean[image.getWidth()];
            int index = 0;
            double px = bottomLeft.getX();
            double py = bottomLeft.getY();
            try {
                while (px < bottomRight.getX() && px > 0 && py > 0 && px < image.getWidth() && py < image.getHeight()) {
                    bits[index] = image.get((int) Math.round(px), (int) Math.round(py));
                    px += lrx;
                    py += lry; //TODO we need to move up from bottom to top, not from left to right
                    index++;
                }
            }
            catch (Exception e)
            {
                System.out.println("***decodeCallibrationCard error sample line into new row");
                e.printStackTrace();
                return CODE_NOT_FOUND;
            }

            // starting index: 4.5 modules in the direction of the bottom right finder pattern
            // end index: our pattern ends at module 17, so we take 25 to be sure.
            int startIndex = (int) Math.round(4.5 * modSizeHor / lrx);
            int endIndex = (int) Math.round(25 * modSizeHor / lrx);

            // determine start of pattern: first black bit. Approach from the left
            try {
                int startI = startIndex;
                while (startI < endIndex && !bits[startI]) {
                    startI++;
                }

                // determine end of pattern: last black bit. Approach from the right
                int endI = endIndex;
                while (endI > startI && !bits[endI]){
                    endI--;
                }

                int lengthPattern = endI - startI + 1;

                // sanity check on length of pattern.
                // We put the minimum size at 20 pixels, which would correspond to a module size of less than 2 pixels,
                // which is too small.
                if (lengthPattern < 20) {
                    System.out.println("***decodeCallibrationCard lengthPattern < 20");
                    return CODE_NOT_FOUND;
                }

                double pWidth = lengthPattern / 12.0;

                // determine bits by majority voting
                int[] bitVote = new int[12];
                for (int i = 0; i < 12; i++){
                    bitVote[i] = 0;
                }

                int bucket;
                for (int i = startI; i <= endI; i++){
                    bucket = (int) Math.round(Math.floor((i - startI) / pWidth));
                    bitVote[bucket] += bits[i] ? 1 : -1;
                }

                // translate into information bits. Skip first and last, which are always 1
                boolean[] bitResult = new boolean[10]; // will contain the information bits
                for (int i = 1; i < 11; i++){
                    bitResult[i - 1] = bitVote[i] > 0;
                }

                // check parity bit
                if (parity(bitResult) != bitResult[9]) {
                    System.out.println("***decodeCallibrationCard parity(bitResult) != bitResult[9]");
                    return CODE_NOT_FOUND;
                }

                // compute result
                int code = 0;
                int count = 0;
                for (int i = 8; i >= 0; i--){
                    if (bitResult[i]){
                        code += (int) Math.pow(2,count);
                    }
                    count ++;
                }

                return code;
            }
            catch (Exception e)
            {
                System.out.println("***decodeCallibrationCard error ");
                e.printStackTrace();
                return CODE_NOT_FOUND;
            }
        }
        else {
            System.out.println("***decodeCallibrationCard finder patterns < 4");
            return CODE_NOT_FOUND;
        }
    }

    /**
     * Compute even parity, where last bit is the even parity bit
     */
    private static boolean parity(boolean[] bits){
        int oneCount = 0;
        for (int i = 0; i < bits.length - 1; i++) {  // skip parity bit in calculation of parity
            if (bits[i]) {
                oneCount++;
            }
        }
        return oneCount % 2 != 0; // returns true if parity is odd
    }

    public static CalibrationData getCalData() {
        return calData;
    }
}
