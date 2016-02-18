package org.akvo.caddisfly.sensor.colorimetry.strip.result_strip;

import android.graphics.Bitmap;

import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.OpenCVUtils;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.color.ColorDetected;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by markwestra on 18/02/16.
 */
public class ResultUtils {
    static Scalar labWhite = new Scalar(255, 128, 128);
    static Scalar labGrey = new Scalar(128, 128, 128);
    static Scalar labBlack = new Scalar(0, 128, 128);
    static double yColorRect = 20d; //distance from top Mat to top color rectangles
    static int circleRadius = 10;
    static double xMargin = 10d;

    public static Mat getMatFromFile(FileStorage fileStorage, int imageNo) {
        //if in DetectStripTask, no strip was found, an image was saved with the String Constant.ERROR
        boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);

        String error = isInvalidStrip ? Constant.ERROR : "";

        // read the Mat object from internal storage
        byte[] data = new byte[0];
        try {
            data = fileStorage.readByteArray(Constant.STRIP + imageNo + error);

            if (data != null) {
                // determine cols and rows dimensions
                byte[] rows = new byte[4];
                byte[] cols = new byte[4];

                int length = data.length;
                System.arraycopy(data, length - 8, rows, 0, 4);
                System.arraycopy(data, length - 4, cols, 0, 4);

                int rowsNum = fileStorage.byteArrayToLeInt(rows);
                int colsNum = fileStorage.byteArrayToLeInt(cols);

                // remove last part
                byte[] imgData = Arrays.copyOfRange(data, 0, data.length - 8);

                // reserve Mat of proper size:
                Mat result = new Mat(rowsNum, colsNum, CvType.CV_8UC3);

                // put image data back in Mat:
                result.put(0, 0, imgData);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap makeBitmap(Mat mat)
    {
        try {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);

            double max = bitmap.getHeight()>bitmap.getWidth()? bitmap.getHeight(): bitmap.getWidth();
            double min = bitmap.getHeight()<bitmap.getWidth()? bitmap.getHeight(): bitmap.getWidth();
            double ratio = (double) min / (double) max;
            int width = (int) Math.max(400, max);
            int height = (int) Math.round(ratio * width);

            return Bitmap.createScaledBitmap(bitmap, width, height, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Mat createStripMat(Mat mat, int borderSize, Point centerPatch, boolean grouped){
        //done with lab shema, make rgb to show in imageview
        // mat holds the strip image
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);

        //extend the strip with a border, so we can draw a circle around each patch that is
        //wider than the strip itself. That is just because it looks nice.


        Core.copyMakeBorder(mat, mat, borderSize, borderSize, borderSize, borderSize, Core.BORDER_CONSTANT, new Scalar(255, 255, 255, 255));

        // Draw a green circle at a particular location patch
        // only draw if this is not a 'grouped' strip
        if (!grouped) {
            Imgproc.circle(mat, new Point(centerPatch.x + borderSize, mat.height() / 2), (int) Math.ceil(mat.height() * 0.4),
                new Scalar(0, 255, 0, 255), 2);
        }
        return mat;
    }

    public static Mat createDescriptionMat(String desc,int width){
        int[] baseline = new int[1];
        Size textSizeDesc = Imgproc.getTextSize(desc, Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, baseline);
        Mat descMat = new Mat((int) Math.ceil(textSizeDesc.height) * 3, width, CvType.CV_8UC3, labWhite);
        Imgproc.putText(descMat, desc, new Point(2, descMat.height() - textSizeDesc.height), Core.FONT_HERSHEY_SIMPLEX, 0.35d, labBlack, 1, Core.LINE_8, false);

        return descMat;
    }

    /*
      * COLOR RANGE AS IN JSON FILE (FROM MANUFACTURER)
      * Create Mat to hold a rectangle for each color
      * the corresponding value written as text above that rectangle
      */
    public static Mat createColourRangeMatSingle(List<StripTest.Brand.Patch> patches, int patchNum, int width, double xtrans){
        // horizontal size of mat: width
        // vertical size of mat: size of colour block - xmargin + top distance
        Mat colorRangeMat = new Mat((int) Math.ceil(xtrans - xMargin + yColorRect), width, CvType.CV_8UC3, labWhite);
        JSONArray colours = null;
        colours = patches.get(patchNum).getColours();

        for (int d = 0; d < colours.length(); d++) {
            try {

                JSONObject colourObj = colours.getJSONObject(d);

                double value = colourObj.getDouble("value");
                JSONArray lab = colourObj.getJSONArray("lab");
                Scalar scalarLab = new Scalar((lab.getDouble(0) / 100) * 255, lab.getDouble(1) + 128, lab.getDouble(2) + 128);
                Size textSizeValue = Imgproc.getTextSize(roundAxis(value), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

                //draw a rectangle filled with color for ppm value
                Point topleft = new Point(xtrans * d, yColorRect);
                Point bottomright = new Point(topleft.x + xtrans - xMargin, yColorRect + xtrans);
                Imgproc.rectangle(colorRangeMat, topleft, bottomright, scalarLab, -1);

                //draw color value above rectangle
                Point centerText = new Point(topleft.x + (bottomright.x - topleft.x) / 2 - textSizeValue.width / 2, yColorRect - textSizeValue.height);
                Imgproc.putText(colorRangeMat, roundAxis(value), centerText, Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return colorRangeMat;
    }

    /*
  * COLOR RANGE AS IN JSON FILE (FROM MANUFACTURER)
  * Create Mat to hold a rectangle for each color
  * the corresponding value written as text above that rectangle
  */
    public static Mat createColourRangeMatGroup(List<StripTest.Brand.Patch> patches, int width,double xtrans){
        // horizontal size of mat: width
        // vertical size of mat: size of colour block - xmargin + top distance

        int numPatches = patches.size();
        Mat colorRangeMat = new Mat((int) Math.ceil(numPatches * (xtrans + xMargin) - xMargin + yColorRect), width, CvType.CV_8UC3, labWhite);

        JSONArray colours;
        int offset = 0;
        System.out.println("*** number of patches:" + numPatches);
        for (int p = 0; p < numPatches; p++) {
            colours = patches.get(p).getColours();
            for (int d = 0; d < colours.length(); d++) {
                try {

                    JSONObject colourObj = colours.getJSONObject(d);

                    double value = colourObj.getDouble("value");
                    JSONArray lab = colourObj.getJSONArray("lab");
                    Scalar scalarLab = new Scalar((lab.getDouble(0) / 100) * 255, lab.getDouble(1) + 128, lab.getDouble(2) + 128);
                    Size textSizeValue = Imgproc.getTextSize(roundAxis(value), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

                    //draw a rectangle filled with color for ppm value
                    Point topleft = new Point(xtrans * d, yColorRect + offset);
                    Point bottomright = new Point(topleft.x + xtrans - xMargin, yColorRect + xtrans + offset);
                    Imgproc.rectangle(colorRangeMat, topleft, bottomright, scalarLab, -1);

                    //draw color value above rectangle
                    if (p == 0) {
                        Point centerText = new Point(topleft.x + (bottomright.x - topleft.x) / 2 - textSizeValue.width / 2, yColorRect - textSizeValue.height);
                        Imgproc.putText(colorRangeMat, roundAxis(value), centerText, Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            offset += xtrans + xMargin;
        }
        return colorRangeMat;
    }

    /*
       * VALUE MEASURED
       * Create Mat to hold a line between min and max values, on it a circle filled with
       * the color detected below which the ppm value measured
       */
    public static Mat createValueMeasuredMatSingle(JSONArray colours,double ppm, ColorDetected colorDetected, int width,double xtrans){
        Mat valueMeasuredMat = new Mat(50, width, CvType.CV_8UC3, labWhite);
        JSONObject colourObj;
        JSONObject nextcolourObj;
        boolean ppmIsDrawn = false;

        //grey line with ppm values at left and right
        Imgproc.line(valueMeasuredMat, new Point(xMargin, 25), new Point(valueMeasuredMat.cols() - 2 * xMargin, 25), labGrey, 2, Core.LINE_AA, 0);

        //get values for lowest and highest ppm values from striptest range
        double leftValue = 0;
        try {
            leftValue = colours.getJSONObject(0).getDouble("value");

            double rightValue = colours.getJSONObject(colours.length() - 1).getDouble("value");
            Size textSizeLeftValue = Imgproc.getTextSize(String.format("%.0f", leftValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);
            Size textSizeRightValue = Imgproc.getTextSize(String.format("%.0f", rightValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

            Imgproc.putText(valueMeasuredMat, String.format("%.0f", leftValue), new Point((xtrans - xMargin) / 2 - textSizeLeftValue.width / 2, 15), Core.FONT_HERSHEY_SIMPLEX,
                0.3d, labGrey, 1, Core.LINE_AA, false);
            Imgproc.putText(valueMeasuredMat, String.format("%.0f", rightValue),
                new Point(valueMeasuredMat.cols() - xMargin - (xtrans - xMargin) / 2 - textSizeRightValue.width / 2, 15),
                Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);


            // we need to iterate over the ppm values to determine where the circle should be placed
            for (int d = 0; d < colours.length(); d++) {
                colourObj = colours.getJSONObject(d);
                if (d < colours.length() - 1) {
                    nextcolourObj = colours.getJSONObject(d + 1);
                } else nextcolourObj = colourObj;

                double value = colourObj.getDouble("value");
                double nextvalue = nextcolourObj.getDouble("value");

                if (ppm < nextvalue && !ppmIsDrawn) {

                    double restPPM = ppm - (value); //calculate the amount above the lowest value
                    double transX = xtrans * (restPPM / (nextvalue - value)); //calculate number of pixs needed to translate in x direction

                    Scalar ppmColor = colorDetected.getLab();
                    //calculate where the center of the circle should be
                    double left = xtrans * d;
                    double right = left + xtrans - xMargin;
                    Point centerCircle = (transX) + xtrans * d < xMargin ? new Point(xMargin, 25d) :
                        new Point(left + (right - left) / 2 + transX, 25d);

                    //get text size of value test
                    Size textSizePPM = Imgproc.getTextSize(roundMeas(ppm), Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, null);

                    Imgproc.circle(valueMeasuredMat, centerCircle, circleRadius, ppmColor, -1, Imgproc.LINE_AA, 0);
                    Imgproc.putText(valueMeasuredMat, roundMeas(ppm), new Point(centerCircle.x - textSizePPM.width / 2, 47d), Core.FONT_HERSHEY_SIMPLEX, 0.35d,
                        labGrey, 1, Core.LINE_AA, false);

                    ppmIsDrawn = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return valueMeasuredMat;
    }


    /*
       * VALUE MEASURED
       * Create Mat to hold a line between min and max values, on it a circle filled with
       * the color detected below which the ppm value measured
       */
    public static Mat createValueMeasuredMatGroup(JSONArray colours,double ppm, ColorDetected[] colorsDetected, int width, double xtrans){
        int size = 50 + colorsDetected.length * (2 * circleRadius + 5);
        Mat valueMeasuredMat = new Mat(size, width, CvType.CV_8UC3, labWhite);

        JSONObject colourObj;
        JSONObject nextcolourObj;
        boolean ppmIsDrawn = false;

        //grey line with ppm values at left and right
        Imgproc.line(valueMeasuredMat, new Point(xMargin, 25), new Point(valueMeasuredMat.cols() - 2 * xMargin, 25), labGrey, 2, Core.LINE_AA, 0);

        //get values for lowest and highest ppm values from striptest range
        double leftValue = 0;
        try {
            leftValue = colours.getJSONObject(0).getDouble("value");

            double rightValue = colours.getJSONObject(colours.length() - 1).getDouble("value");
            Size textSizeLeftValue = Imgproc.getTextSize(String.format("%.0f", leftValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);
            Size textSizeRightValue = Imgproc.getTextSize(String.format("%.0f", rightValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

            Imgproc.putText(valueMeasuredMat, String.format("%.0f", leftValue), new Point((xtrans - xMargin) / 2 - textSizeLeftValue.width / 2, 15), Core.FONT_HERSHEY_SIMPLEX,
                0.3d, labGrey, 1, Core.LINE_AA, false);
            Imgproc.putText(valueMeasuredMat, String.format("%.0f", rightValue),
                new Point(valueMeasuredMat.cols() - xMargin - (xtrans - xMargin) / 2 - textSizeRightValue.width / 2, 15),
                Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);


            // we need to iterate over the ppm values to determine where the circle should be placed
            for (int d = 0; d < colours.length(); d++) {
                colourObj = colours.getJSONObject(d);
                if (d < colours.length() - 1) {
                    nextcolourObj = colours.getJSONObject(d + 1);
                } else nextcolourObj = colourObj;

                double value = colourObj.getDouble("value");
                double nextvalue = nextcolourObj.getDouble("value");

                if (ppm < nextvalue && !ppmIsDrawn) {

                    double restPPM = ppm - (value); //calculate the amount above the lowest value
                    double transX = xtrans * (restPPM / (nextvalue - value)); //calculate number of pixs needed to translate in x direction


                    //calculate where the center of the circle should be
                    double left = xtrans * d;
                    double right = left + xtrans - xMargin;
                    Point centerCircle = (transX) + xtrans * d < xMargin ? new Point(xMargin, 25d) :
                        new Point(left + (right - left) / 2 + transX, 25d);

                    //get text size of value test
                    Size textSizePPM = Imgproc.getTextSize(String.format("%.1f", ppm), Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, null);
                    Imgproc.putText(valueMeasuredMat, String.format("%.1f", ppm), new Point(centerCircle.x - textSizePPM.width / 2, 15d), Core.FONT_HERSHEY_SIMPLEX, 0.35d,
                        labGrey, 1, Core.LINE_AA, false);

                    double offset = circleRadius + 5;
                    for (int p = 0; p < colorsDetected.length; p++) {
                        Scalar ppmColor = colorsDetected[p].getLab();
                        Imgproc.circle(valueMeasuredMat, new Point(centerCircle.x,centerCircle.y + offset), circleRadius, ppmColor, -1, Imgproc.LINE_AA, 0);
                        offset += 2 * circleRadius + 5;
                    }

                    ppmIsDrawn = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return valueMeasuredMat;
    }

    public static Mat concatenate(Mat m1, Mat m2){
        int width = Math.max(m1.cols(),m2.cols());
        int height = m1.rows() + m2.rows();

        Mat result = new Mat(height, width, CvType.CV_8UC3, new Scalar(255, 255, 255));

        // rect works with x, y, width, height
        Rect roi1 = new Rect(0, 0, m1.cols(), m1.rows());
        Mat roiMat1 = result.submat(roi1);
        m1.copyTo(roiMat1);

        Rect roi2 = new Rect(0, m1.rows(), m2.cols(), m2.rows());
        Mat roiMat2 = result.submat(roi2);
        m2.copyTo(roiMat2);

        return result;
    }

    public static ColorDetected getPatchColour(Mat mat, Point centerPatch, int submatSize){

        //make a submat around center of the patch
        int minRow = (int) Math.round(Math.max(centerPatch.y - submatSize, 0));
        int maxRow = (int) Math.round(Math.min(centerPatch.y + submatSize, mat.height()));
        int minCol = (int) Math.round(Math.max(centerPatch.x - submatSize, 0));
        int maxCol = (int) Math.round(Math.min(centerPatch.x + submatSize, mat.width()));

        //  create submat
        Mat patch = mat.submat(minRow, maxRow, minCol, maxCol);

        // compute the mean colour and return it
        return OpenCVUtils.detectStripColorBrandKnown(patch);
    }

    /*
   * Restricts number of significant digits depending on size of number
    */
    public static double roundSignificant(double ppm) {
        if (ppm < 1.0) {
            return Math.round(ppm * 100) / 100.0;
        } else {
            return Math.round(ppm * 10) / 10.0;
        }
    }

    public static double[][] createInterpolTable(JSONArray colours){
        JSONArray patchColorValues;
        double ppmPatchValueStart,ppmPatchValueEnd;
        double[] pointStart;
        double[] pointEnd;
        double LInter, aInter, bInter, vInter;
        int INTERPOLNUM = 10;
        double[][] interpolTable = new double[(colours.length() - 1) * INTERPOLNUM + 1][4];
        int count = 0;

        for (int i = 0; i < colours.length() - 1; i++) {
            try {
                patchColorValues = colours.getJSONObject(i).getJSONArray("lab");
                ppmPatchValueStart = colours.getJSONObject(i).getDouble("value");
                pointStart = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                patchColorValues = colours.getJSONObject(i + 1).getJSONArray("lab");
                ppmPatchValueEnd = colours.getJSONObject(i + 1).getDouble("value");
                pointEnd = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                double LStart = pointStart[0];
                double aStart = pointStart[1];
                double bStart = pointStart[2];

                double dL = (pointEnd[0] - pointStart[0]) / INTERPOLNUM;
                double da = (pointEnd[1] - pointStart[1]) / INTERPOLNUM;
                double db = (pointEnd[2] - pointStart[2]) / INTERPOLNUM;
                double dV = (ppmPatchValueEnd - ppmPatchValueStart) / INTERPOLNUM;

                // create 10 interpolation points, including the start point,
                // but excluding the end point

                for (int ii = 0; ii < INTERPOLNUM; ii++) {
                    LInter = LStart + ii * dL;
                    aInter = aStart + ii * da;
                    bInter = bStart + ii * db;
                    vInter = ppmPatchValueStart + ii * dV;

                    interpolTable[count][0] = LInter;
                    interpolTable[count][1] = aInter;
                    interpolTable[count][2] = bInter;
                    interpolTable[count][3] = vInter;
                    count++;
                }

                // add final point
                patchColorValues = colours.getJSONObject(colours.length() - 1).getJSONArray("lab");
                interpolTable[count][0] = patchColorValues.getDouble(0);
                interpolTable[count][1] = patchColorValues.getDouble(1);
                interpolTable[count][2] = patchColorValues.getDouble(2);
                interpolTable[count][3] = colours.getJSONObject(colours.length() - 1).getDouble("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return interpolTable;
    }

    public static double calculatePpmSingle(double[] colorValues, JSONArray colours) throws Exception {
        double[][] interpolTable = createInterpolTable(colours);

        // determine closest value
        // create interpolation and extrapolation tables using linear approximation
        if (colorValues == null || colorValues.length < 3){
            throw new Exception("no valid lab colour data.");
        }

        // normalise lab values to standard ranges L:0..100, a and b: -127 ... 128
        double[] labPoint = new double[]{colorValues[0] / 2.55, colorValues[1] - 128, colorValues[2] - 128};

        double dist;
        int minPos = 0;
        double smallestE94Dist = Double.MAX_VALUE;

        for (int j = 0; j < interpolTable.length; j++) {
            // Find the closest point using the E94 distance
            // the values are already in the right range, so we don't need to normalize
            dist = CalibrationCard.E94(labPoint[0], labPoint[1], labPoint[2], interpolTable[j][0], interpolTable[j][1], interpolTable[j][2], false);
            if (dist < smallestE94Dist){
                smallestE94Dist = dist;
                minPos = j;
            }
        }

        return interpolTable[minPos][3];
    }

    public static double calculatePpmGroup(double[][] colorsValueLab, List<StripTest.Brand.Patch>patches) throws Exception{
        double[][][] interpolTables = new double[patches.size()][][];

        // create all interpol tables
        for (int p = 0; p < patches.size(); p++){
            JSONArray colours = patches.get(p).getColours();

            // create interpol table for this patch
            interpolTables[p] = createInterpolTable(colours);

            if (colorsValueLab[p] == null || colorsValueLab[p].length < 3){
                throw new Exception("no valid lab colour data.");
            }
        }

        // normalise lab values to standard ranges L:0..100, a and b: -127 ... 128
        double[][] labPoint = new double[patches.size()][];
        for (int p = 0; p < patches.size(); p++) {
            labPoint[p] = new double[]{colorsValueLab[p][0] / 2.55, colorsValueLab[p][1] - 128, colorsValueLab[p][2] - 128};
        }

        double dist;
        int minPos = 0;
        double smallestE94Dist = Double.MAX_VALUE;

        // compute smallest distance, combining all interpolation tables as we want the global minimum
        // all interpol tables should have the same length here, so we use the length of the first one

        for (int j = 0; j < interpolTables[0].length; j++) {
            dist = 0;
            for (int p = 0; p < patches.size(); p++){
                dist += CalibrationCard.E94(labPoint[p][0],labPoint[p][1],
                    labPoint[p][2],interpolTables[p][j][0], interpolTables[p][j][1], interpolTables[p][j][2], false);
            }
            if (dist < smallestE94Dist){
                smallestE94Dist = dist;
                minPos = j;
            }
        }

        return interpolTables[0][minPos][3];
    }

    public static String roundMeas(double value) {
        if (value < 1.0) {
            return String.format("%.2f", value);
        } else {
            return String.format("%.1f", value);
        }
    }

    public static String roundAxis(double value) {
        if (value < 1.0) {
            return String.format("%.1f", value);
        } else {
            return String.format("%.0f", value);
        }
    }
}
