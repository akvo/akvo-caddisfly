package org.akvo.akvoqr.calibration;

import android.content.Context;

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
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.akvo.akvoqr.util.OpenCVUtils.getOrderedPoints;

// Performs the calibration of the image
public class CalibrationCard{
    public static final int CODE_NOT_FOUND = -1;
    private static final double ONE_OVER_NINE = 1.0/9;
    private static Map<Integer, Integer> versionNumberMap = new HashMap<>();

    //put version number in HashMap: number, frequency
    public static void addVersionNumber(Integer number)
    {
        int existingFrequency = versionNumberMap.get(number)==null? 0: versionNumberMap.get(number);
        versionNumberMap.put(number, existingFrequency+1);
    }

    public static int getMostFrequentVersionNumber()
    {
        int mostFreq = 0;
        List<Integer> versionNumbers = new ArrayList<>();
        //what is the most frequent value
        for(Integer freq: versionNumberMap.values())
        {
            if(freq > mostFreq)
            {
                mostFreq = freq;

            }
        }

        //collect the keys that have mostFreq as value
        for(Map.Entry<Integer, Integer> entry: versionNumberMap.entrySet())
        {
//            System.out.println("***saved version number: " + entry.getKey());

            if(entry.getValue().equals(mostFreq))
                versionNumbers.add(entry.getKey());
        }

        //return the first match (hopefully there will be one and only one match)
        if(versionNumbers.size()>0) {

            return versionNumbers.get(0);
        }

        return CODE_NOT_FOUND;
    }

    public static CalibrationData readCalibrationFile(Context context){

        String calFileName = "calibration" + getMostFrequentVersionNumber() + ".json";
        String json = AssetsManager.getInstance().loadJSONFromAsset(calFileName);

        if(json!=null) {
            try {

                CalibrationData calData = new CalibrationData();

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
                    // we scale the Lab values in the same way as openCV does
                    calData.addCal(cal.getString("l"), cal.getDouble("CIE_L") * 2.55, cal.getDouble("CIE_A") + 128, cal.getDouble("CIE_B") + 128);
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


            } catch (JSONException e) {
                System.out.println("*** problem parsing JSON:" + e.toString());

            }
        }
        return null;
    }

    private static int capValue(int val, int min, int max){
        if (val > max) {
            return max;
        }
        return val < min ? min : val;
    }

    // computes the colour around a single point
    // x and y in pixels
    // This method expects a cielab file
    private static double[] getWhiteVal(Mat lab, int x, int y, int dp){
        double totLum = 0;
        double totA = 0;
        double totB = 0;

        int totNum = 0;
        for (int i = -dp; i <= dp; i++){
            for (int ii = -dp; ii <= dp; ii++){
                totLum = totLum + lab.get(y + i, x + ii)[0];
                totA = totA + lab.get(y + i, x + ii)[1];
                totB = totB + lab.get(y + i, x + ii)[2];
                totNum++;
            }
        }
        return new double[]{totLum / totNum, totA / totNum, totB / totNum};
    }

    /*
    * Samples the white area of a calibration card. This information is used to straighten out the illumination.
    * @result: array of point vectors, with the structure [x,y,L,A,B]
     */
    public static double[][] createWhitePointArray(Mat lab, CalibrationData calData)
    {
        List<CalibrationData.WhiteLine> lines = calData.whiteLines;
        int numLines = lines.size() * 10; // on each line, we sample 10 points
        double[][] points = new double[numLines][5];
        int index = 0;

        calData.hsizePixel = lab.cols();
        double hfac = calData.hsizePixel / calData.hsize; // pixel per mm in the horizontal direction
        calData.vsizePixel = lab.rows();
        double vfac = calData.vsizePixel / calData.vsize; // pixel per mm in the vertical direction

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
                double[] whiteVal = getWhiteVal(lab,xp, yp, dp);
                points[index * 10 + i][2] = whiteVal[0];
                points[index * 10 + i][3] = whiteVal[1];
                points[index * 10 + i][4] = whiteVal[2];
            }
            index++;
        }
        return points;
    }

    /*
    * Turns the whitepoint array into a matrix
     */
    private static RealMatrix createWhitePointMatrix(Mat lab, CalibrationData calData) {
        double[][] points = createWhitePointArray(lab, calData);
        return MatrixUtils.createRealMatrix(points);
    }

    /*
    * Straightens the illumination of the calibration file. It does this by sampling the white areas
    * and compute a quadratic profile. The image is then corrected using this profile.
     */
    private static Mat doIlluminationCorrection(Mat imgLab, CalibrationData calData){
        // create HLS image for homogenous illumination calibration
        int pheight =  imgLab.rows();
        int pwidth = imgLab.cols();


        RealMatrix points = createWhitePointMatrix(imgLab, calData);

        // create coefficient matrix for all three variables L,A,B
        // the model for all three is y = ax + bx^2 + cy + dy^2 + exy + f
        // 6th row is the constant 1
        RealMatrix coef = new Array2DRowRealMatrix(points.getRowDimension(),6);
        coef.setColumnMatrix(0,points.getColumnMatrix(0));
        coef.setColumnMatrix(2,points.getColumnMatrix(1));

        //create constant, x^2, y^2 and xy terms
        for (int i = 0; i < points.getRowDimension(); i++){
            coef.setEntry(i,1,Math.pow(coef.getEntry(i,0),2)); // x^2
            coef.setEntry(i,3,Math.pow(coef.getEntry(i,2), 2)); // y^2
            coef.setEntry(i,4,coef.getEntry(i,0) * coef.getEntry(i,2)); // xy
            coef.setEntry(i,5,1d); // constant = 1
        }

        // create vectors
        RealVector L = points.getColumnVector(2);
        RealVector A = points.getColumnVector(3);
        RealVector B = points.getColumnVector(4);

        // solve the least squares problem for all three variables
        DecompositionSolver solver = new SingularValueDecomposition(coef).getSolver();
        RealVector solutionL = solver.solve(L);
        RealVector solutionA = solver.solve(A);
        RealVector solutionB = solver.solve(B);

        // get individual coefficients
        float La = (float)solutionL.getEntry(0);
        float Lb = (float)solutionL.getEntry(1);
        float Lc = (float)solutionL.getEntry(2);
        float Ld = (float)solutionL.getEntry(3);
        float Le = (float)solutionL.getEntry(4);
        float Lf = (float)solutionL.getEntry(5);

        float Aa = (float)solutionA.getEntry(0);
        float Ab = (float)solutionA.getEntry(1);
        float Ac = (float)solutionA.getEntry(2);
        float Ad = (float)solutionA.getEntry(3);
        float Ae = (float)solutionA.getEntry(4);
        float Af = (float)solutionA.getEntry(5);

        float Ba = (float)solutionB.getEntry(0);
        float Bb = (float)solutionB.getEntry(1);
        float Bc = (float)solutionB.getEntry(2);
        float Bd = (float)solutionB.getEntry(3);
        float Be = (float)solutionB.getEntry(4);
        float Bf = (float)solutionB.getEntry(5);

        // compute mean (the luminosity value of the plane in the middle of the image)
        float Lmean = (float) (0.5 * La * pwidth + 0.5 * Lc * pheight + Lb * pwidth * pwidth / 3.0 + Ld * pheight * pheight / 3.0 + Le * 0.25 * pheight * pwidth + Lf);
        float Amean = (float) (0.5 * Aa * pwidth + 0.5 * Ac * pheight + Ab * pwidth * pwidth / 3.0 + Ad * pheight * pheight / 3.0 + Ae * 0.25 * pheight * pwidth + Af);
        float Bmean = (float) (0.5 * Ba * pwidth + 0.5 * Bc * pheight + Bb * pwidth * pwidth / 3.0 + Bd * pheight * pheight / 3.0 + Be * 0.25 * pheight * pwidth + Bf);

        // Correct image
        // we do this per row. We tried to do it in one block, but there is no speed difference.
        byte[] temp = new byte[imgLab.cols() * imgLab.channels()];
        int valL, valA, valB;
        int ii, ii3;
        float iiSq, iSq;
        int imgCols = imgLab.cols();
        int imgRows = imgLab.rows();

        // use lookup tables to speed up computation
        // create lookup tables
        float[] Laii = new float[imgCols];
        float[] LbiiSq = new float[imgCols];
        float[] Aaii = new float[imgCols];
        float[] AbiiSq = new float[imgCols];
        float[] Baii = new float[imgCols];
        float[] BbiiSq = new float[imgCols];

        float[] Lci = new float[imgRows];
        float[] LdiSq = new float[imgRows];
        float[] Aci = new float[imgRows];
        float[] AdiSq = new float[imgRows];
        float[] Bci = new float[imgRows];
        float[] BdiSq = new float[imgRows];

        for (ii = 0; ii < imgCols; ii++){
            iiSq = ii * ii;
            Laii[ii] = La * ii;
            LbiiSq[ii] = Lb * iiSq;
            Aaii[ii] = Aa * ii ;
            AbiiSq[ii] = Ab * iiSq;
            Baii[ii] = Ba * ii;
            BbiiSq[ii] = Bb * iiSq;
        }

        for (int i = 0; i < imgRows; i++){
            iSq = i * i;
            Lci[i] = Lc * i;
            LdiSq[i] = Ld * iSq;
            Aci[i] = Ac * i;
            AdiSq[i] =Ad * iSq;
            Bci[i] = Bc * i;
            BdiSq[i] = Bd * iSq;
        }

        // TODO we can also improve the performance of the i,ii term, if we want, but it won't make much difference.
        for (int i = 0; i < imgRows; i++){ // y
            imgLab.get(i, 0, temp);
            ii3 = 0;
            for (ii = 0; ii < imgCols; ii++){  //x
                valL = capValue((int) Math.round((temp[ii3 + 0] & 0xFF) - (Laii[ii] + LbiiSq[ii] + Lci[i] + LdiSq[i] + Le * i * ii + Lf) + Lmean),0,255);
                valA = capValue((int) Math.round((temp[ii3 + 1] & 0xFF) - (Aaii[ii] + AbiiSq[ii] + Aci[i] + AdiSq[i] + Ae * i * ii + Af) + Amean),0,255);
                valB = capValue((int) Math.round((temp[ii3 + 2] & 0xFF) - (Baii[ii] + BbiiSq[ii] + Bci[i] + BdiSq[i] + Be * i * ii + Bf) + Bmean),0,255);

                temp[ii3] = (byte) valL;
                temp[ii3 + 1] = (byte) valA;
                temp[ii3 + 2] = (byte) valB;
                ii3 += 3;
            }
            imgLab.put(i, 0, temp);
        }

        return imgLab;
    }

    private static float[] measurePatch(Mat imgMat, double x, double y, CalibrationData calData){
        float[] LABresult = new float[3];
        float totL = 0;
        float totA = 0;
        float totB = 0;
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
                totL += temp[ii3] & 0xFF; //imgMat.get(yp + i, xp + ii)[0];
                totA += temp[ii3 + 1] & 0xFF; //imgMat.get(yp + i, xp + ii)[1];
                totB += temp[ii3 + 2] & 0xFF; //imgMat.get(yp + i, xp + ii)[2];
                totNum++;
                ii3 += 3;
            }
        }
        LABresult[0] = totL / totNum;
        LABresult[1] = totA / totNum;
        LABresult[2] = totB / totNum;
        return LABresult;
    }

    /*
    * Perform two calibration steps:
    * 1) a 1D calibration which looks at the individual L, A, B channels and corrects them
    * 2) a 3d calibration which can mix the L,A,B channels to arrive at optimal results
     */
    private static Mat do1D_3DCorrection(Mat imgMat, CalibrationData calData) throws Exception{

        if(calData==null) {
            throw new Exception("no calibration data.");
        }

        final WeightedObservedPoints obsL = new WeightedObservedPoints();
        final WeightedObservedPoints obsA = new WeightedObservedPoints();
        final WeightedObservedPoints obsB = new WeightedObservedPoints();

        Map<String,double[]> calResultIllum = new HashMap<String,double[]>();
        // iterate over all patches
        try {
            for (String label : calData.calValues.keySet()) {
                CalibrationData.CalValue cal = calData.calValues.get(label);
                CalibrationData.Location loc = calData.locations.get(label);
                float[] LABcol = measurePatch(imgMat, loc.x, loc.y, calData); // measure patch colour
                obsL.add(LABcol[0], cal.CIE_L);
                obsA.add(LABcol[1], cal.CIE_A);
                obsB.add(LABcol[2], cal.CIE_B);
                calResultIllum.put(label,new double[]{LABcol[0],LABcol[1],LABcol[2]});
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception("1D calibration: error iterating over all patches.");
        }

        // Instantiate a second-degree polynomial fitter.
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);

        // Retrieve fitted parameters (coefficients of the polynomial function).
        // order of coefficients is (c + bx + ax^2), so [c,b,a]
        try {
            final double[] coeffL = fitter.fit(obsL.toList());
            final double[] coeffA = fitter.fit(obsA.toList());
            final double[] coeffB = fitter.fit(obsB.toList());

            double[] valIllum;
            double Lorig, Aorig, Borig, Lnew, Anew, Bnew;

            // transform patch values using the 1d calibration results
            Map<String,double[]> calResult1D = new HashMap<String,double[]>();
            for (String label : calData.calValues.keySet()) {
                valIllum = calResultIllum.get(label);

                Lorig = valIllum[0];
                Aorig = valIllum[1];
                Borig = valIllum[2];

                Lnew = coeffL[2] * Lorig * Lorig + coeffL[1] * Lorig + coeffL[0];
                Anew = coeffA[2] * Aorig * Aorig + coeffA[1] * Aorig + coeffA[0];
                Bnew = coeffB[2] * Borig * Borig + coeffB[1] * Borig + coeffB[0];

                calResult1D.put(label,new double[]{Lnew,Anew,Bnew});
            }

            // use the 1D calibration result for the second calibration step
            // Following http://docs.scipy.org/doc/scipy/reference/tutorial/linalg.html#solving-linear-least-squares-problems-and-pseudo-inverses
            // we will solve P = M x
            int total = calData.locations.keySet().size();
            RealMatrix coef = new Array2DRowRealMatrix(total,3);
            RealMatrix cal = new Array2DRowRealMatrix(total,3);
            int index = 0;

            // create coefficient and calibration vectors
            for (String label : calData.calValues.keySet()){
                CalibrationData.CalValue calv = calData.calValues.get(label);
                double[] cal1dResult = calResult1D.get(label);
                coef.setEntry(index,0,cal1dResult[0]);
                coef.setEntry(index,1,cal1dResult[1]);
                coef.setEntry(index,2,cal1dResult[2]);

                cal.setEntry(index,0,calv.CIE_L);
                cal.setEntry(index,1,calv.CIE_A);
                cal.setEntry(index,2,calv.CIE_B);
                index++;
            }

            DecompositionSolver solver = new SingularValueDecomposition(coef).getSolver();
            RealMatrix sol = solver.solve(cal);

            float a_L, b_L, c_L, a_A, b_A, c_A, a_B, b_B, c_B;
            a_L = (float)sol.getEntry(0,0);
            b_L = (float)sol.getEntry(1,0);
            c_L = (float)sol.getEntry(2,0);
            a_A = (float)sol.getEntry(0,1);
            b_A = (float)sol.getEntry(1,1);
            c_A = (float)sol.getEntry(2,1);
            a_B = (float)sol.getEntry(0,2);
            b_B = (float)sol.getEntry(1,2);
            c_B = (float)sol.getEntry(2,2);

            //use the solution to correct the image
            double Ltemp, Atemp, Btemp, Lmid, Amid, Bmid;
            int Lfin, Afin, Bfin;
            int ii3;
            byte[] temp = new byte[imgMat.cols() * imgMat.channels()];
            for (int i = 0; i < imgMat.rows(); i++){ // y
                imgMat.get(i, 0, temp);
                ii3 = 0;
                for (int ii = 0; ii < imgMat.cols(); ii++){  //x
                    Ltemp = temp[ii3] & 0xFF;
                    Atemp = temp[ii3 + 1] & 0xFF;
                    Btemp = temp[ii3 + 2] & 0xFF;

                    Lmid = coeffL[2] * Ltemp * Ltemp + coeffL[1] * Ltemp + coeffL[0];
                    Amid = coeffA[2] * Atemp * Atemp + coeffA[1] * Atemp + coeffA[0];
                    Bmid = coeffB[2] * Btemp * Btemp + coeffB[1] * Btemp + coeffB[0];

                    Lfin = (int) Math.round(a_L * Lmid + b_L * Amid + c_L * Bmid);
                    Afin = (int) Math.round(a_A * Lmid + b_A * Amid + c_A * Bmid);
                    Bfin = (int) Math.round(a_B * Lmid + b_B * Amid + c_B * Bmid);

                    // cap values
                    Lfin = capValue(Lfin,0,255);
                    Afin = capValue(Afin,0,255);
                    Bfin = capValue(Bfin,0,255);

                    temp[ii3] = (byte) Lfin;
                    temp[ii3 + 1] = (byte) Afin;
                    temp[ii3 + 2] = (byte) Bfin;

                    ii3 += 3;
                }
                imgMat.put(i, 0, temp);
            }

            return imgMat;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception("error while performing calibration: ",e);
        }
    }

    private static void addPatch(Mat imgMat, Double x, Double y, CalibrationData calData, String label) {

        Map<String, CalibrationData.CalValue> calValueMap = calData.calValues;
        CalibrationData.CalValue calValue = calData.calValues.get(label);
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
                col[0] = (byte) Math.round(calValue.CIE_L);
                col[1] = (byte) Math.round(calValue.CIE_A);
                col[2] = (byte) Math.round(calValue.CIE_B);
                imgMat.put(yp + i, xp + ii,col);
            }
        }
    }

    private static void addCalColours(Mat imgMat, CalibrationData calData) {
        for (String label : calData.locations.keySet()){
            CalibrationData.Location loc = calData.locations.get(label);
            addPatch(imgMat,loc.x,loc.y,calData, label);
        }
    }

    /*
    * Calibrate an image using a 3-step process. The steps are:
     * 1) straighten illumination profile
     * 2) 1D colour balance
     * 3) 3D colour balance
     *
     * As a final step, we put the calibration colours in the image, so that the quality can be judged later
     *
     * imgMat: CV_8UC3 (8-bit) Mat object, in BGR encoding.
     * @result: calibrated image
     */
    public static CalibrationResultData calibrateImage(Mat labImg, CalibrationData calData) throws Exception{
        System.out.println("*** qualityChecksOK of calibration");

        if(calData!=null) {
            // illumination correction
            if(labImg!=null) {
                System.out.println("*** ILLUM - starting illumination correction");
                labImg = doIlluminationCorrection(labImg, calData);
            }

            // 1D and 3D colour balance
            if(labImg!=null) {
                System.out.println("*** ILLUM - starting 1D and 3D balance");
                labImg = do1D_3DCorrection(labImg, calData);
            }

            // measure quality of the calibration
            double[] E94Result = computeE94Error(labImg, calData);

            // insert calibration colours in image
            if(labImg!=null) {
                System.out.println("*** ILLUM - adding colours");
                addCalColours(labImg, calData);
            }

            CalibrationResultData calResult = new CalibrationResultData(labImg,E94Result[0],E94Result[1],E94Result[2]);

            return calResult;
        }
        return null;
    }

    /*
    * Computes E94 distance between two colours.
    * First normalises the colours as follows:
    * L : 0...100
    * a,b: -128 ... 128
    * follows http://colormine.org/delta-e-calculator/cie94
    *
    * @returns: E94 distance
     */
    public static double E94(double l1, double a1, double b1, double l2, double a2, double b2, boolean normalise){

        if (normalise){
            // normalise values to standard ranges
            l1 = l1 / 2.55;
            l2 = l2 / 2.55;
            a1 = a1 - 128;
            a2 = a2 - 128;
            b1 = b1 - 128;
            b2 = b2 - 128;
        }

        double dL = l1 - l2;
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double dC = C1 - C2;
        double da = a1 - a2;
        double db = b1 - b2;
        double dH2 = da * da + db * db - dC * dC;
        double dH;

        if (dH2 < 0){
            dH = 0;
        } else {
            dH = Math.sqrt(dH2);
        }

        double SL = 1.0;
        double SC = 1.0 + 0.045 * C1;
        double SH = 1.0 + 0.015 * C1;

        double dE94 = Math.sqrt(Math.pow(dL/SL,2) + Math.pow(dC/SC,2) + Math.pow(dH/SH,2));
        return dE94;
    }

    /*
    * Computes mean and max E94 distance of calibrated image and the calibration patches
    * @returns: vector of double, with [mean E94, max E94]
     */
    private static double[] computeE94Error(Mat labImg, CalibrationData calData) throws Exception{
        try {
            int num = 0;
            double totE94 = 0;
            double maxE94 = 0;
            for (String label : calData.calValues.keySet()) {
                CalibrationData.CalValue cal = calData.calValues.get(label);
                CalibrationData.Location loc = calData.locations.get(label);
                float[] LABcol = measurePatch(labImg, loc.x, loc.y, calData); // measure patch colour
                // as both measured and calibration values are in openCV range, we need to normalise the values
                double E94Dist = E94(LABcol[0], LABcol[1], LABcol[2], cal.CIE_L, cal.CIE_A, cal.CIE_B, true);
                totE94 += E94Dist;
                if (E94Dist > maxE94){
                    maxE94 = E94Dist;
                }
                num++;
            }
            return new double[]{totE94/num, maxE94, totE94};
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception("Error determining E94 calibration distance");
        }

    }

    /**
     * find and decode the code of the callibration card
     * The code is stored as a simple barcode. It starts 4.5 modules from the center of the bottom left finder pattern
     * and extends to module 29.5.
     * It has 12 bits, of 2 modules wide each.
     * It starts and ends with a 1 bit.
     * The remaining 10 bits are interpreted as a 9 bit number with the last bit as parity bit.
     * Position barcode:
     * _________________________________________________________
     * |                                                        |
     * |________________                                        |
     * ||0             1|                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||              b|                                       |
     * ||              b|                                       |
     * ||2_____________3|                                       |
     * |________________________________________________________|
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

            //because camera is in portrait mode, we need bottom-right and top-right patterns:
            //the version number barcode lies next to bottom-right
            ResultPoint bottomLeft = new ResultPoint((float) points.get(3).x,(float) points.get(3).y);
            ResultPoint bottomRight = new ResultPoint((float) points.get(1).x,(float) points.get(1).y);

            // get estimated module size
            Detector detector = new Detector(image);
            float modSize = detector.calculateModuleSize(bottomLeft, bottomRight, bottomRight);

            // go from one finder pattern to the other,
            //because camera is in portrait mode, we need to shift x and y
            double lrx = bottomRight.getX() - bottomLeft.getX();
            double lry = bottomRight.getY() - bottomLeft.getY();
            double hNorm = MathUtils.distance(bottomLeft.getX(), bottomLeft.getY(),
                    bottomRight.getX(), bottomRight.getY());

            // check if left and right are ok
            if (lry > 0) {
                System.out.println("***decodeCallibrationCard lry > 0");
                return CODE_NOT_FOUND;
            }

            // create vector of length 1 pixel, in the direction of the bottomRight finder pattern
            lrx /= hNorm;
            lry /= hNorm;

            // sample line into new row
            boolean[] bits = new boolean[image.getHeight()];
            int index = 0;
            double px = bottomLeft.getX();
            double py = bottomLeft.getY();
            try {
                while (px > 0 && py > 0 && px < image.getWidth() && py < image.getHeight()) {
                    bits[index] = image.get((int) Math.round(px), (int) Math.round(py));
                    px += lrx;
                    py += lry;
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
            int startIndex = (int) Math.abs(Math.round(4.5 * modSize / lry));
            int endIndex = (int) Math.abs(Math.round(25 * modSize / lry));

            // determine qualityChecksOK of pattern: first black bit. Approach from the left
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

//    public CalibrationData getCalData() {
//        return calData;
//    }
}
