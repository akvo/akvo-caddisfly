package org.akvo.akvoqr.util;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.calibration.CalibrationData;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by linda on 10/3/15.
 */
public class PreviewUtils {


//    public static double focusStandardDev(Mat src)
//    {
//        MatOfDouble mean = new MatOfDouble();
//        MatOfDouble stddev = new MatOfDouble();
//
//        Core.meanStdDev(src, mean, stddev);
//
//        Scalar mu = new Scalar(0);
//        Scalar sigma = new Scalar(0);
//
//        double focusMeasure = 0;
//
//        for(int i=0;i<mean.rows();i++) {
//            for (int j = 0; j < mean.cols(); j++) {
//                double[] d = mean.get(i, j);
//                if (d[0] > 0 ) {
//                    mu = new Scalar(d);
//                }
//                System.out.println("***mu: " + mu.toString());
//
//            }
//        }
//
//        for(int i=0;i<stddev.rows();i++)
//        {
//            for(int j=0; j< stddev.cols();j++)
//            {
//                double[] d = stddev.get(i,j);
//                if(d[0] > 0) {
//                    sigma = new Scalar(d);
//                }
//                System.out.println("***sigma: " + sigma.toString());
//
//            }
//        }
//
//        focusMeasure = (sigma.val[0]*sigma.val[0]) / mu.val[0];
//
//        return focusMeasure;
//    }


//        System.out.println("*** qualityChecksOK ****************************************");
//        for (int j = 0; j < cols - 1; j++){
//            System.out.println("*** point:" + (temp[j] & 0xFF));
//        }


//
//        for (int i = 0; i < src_gray.rows(); i++){
//            src_gray.get(i, 0, temp);
//            for (int j = 0; j < cols - 1; j++){
//                diff = Math.abs(temp[j] & 0xFF - temp[j + 1] & 0xFF);
//                if (diff > maxDiff){
//                    maxDiff = diff;
//                    System.out.println("*** max changing!");
//                    System.out.println("*** point 1:" + (temp[j] & 0xFF) + ", point 2:" + (temp[j+1] & 0xFF);
//                    System.out.println("*** diff:" + diff + ", max diff:" + maxDiff);
//                }
//            }
//        }
//        return maxDiff;
//    }

//    public static double focusLaplacian(Mat src_gray) {
//
//        int kernel_size = 3;
//        int scale = 1;
//        int delta = 0;
//        int ddepth = CvType.CV_8UC1;
//        double maxLap = -32767;
//
//        Mat dst = new Mat();
//
//        Imgproc.Laplacian(src_gray, dst, ddepth, kernel_size, scale, delta, Core.BORDER_DEFAULT);
//
//        if (!dst.empty()) {
//            Core.MinMaxLocResult result = Core.minMaxLoc(dst);
//            maxLap = result.maxVal;
//        }
//        return maxLap;
//    }

    public static double focusLaplacian1(Mat src_gray) {
        double maxDiff = 0.0;
        double diff;
        byte[] temp = new byte[src_gray.cols()];
        int cols = src_gray.cols();

        // only sample the line in the middle of the finder pattern
        src_gray.get(Math.round(src_gray.rows() / 2), 0, temp);

        for (int j = 0; j < cols - 1; j++){
            diff = Math.abs((temp[j] & 0xFF) - (temp[j + 1] & 0xFF));
            if (diff > maxDiff){
                maxDiff = diff;
            }
        }
        return maxDiff;
    }


    public static double[] getDiffLuminosity(Mat src_gray)
    {
        //find min and max luminosity
        Core.MinMaxLocResult result = Core.minMaxLoc(src_gray);
        return new double[]{ result.minVal, result.maxVal};
    }

    /*method for shadow detection
   * @param Mat : a 'cut-out' of the test card between the centers of the finder patterns.
   * @return :  percentage of the points that deviate more than @link Constant.CONTRAST_DEVIATION_PERCENTAGE from the average luminosity
   *  points with luminosity with a larger difference than Constant.CONTRAST_MAX_DEVIATION_PERCENTAGE count 10 times in the result.
    */
    public static double getShadowPercentage(Mat bgr,CalibrationCard card, CalibrationData data) {

        double sumLum = 0;
        int countDev = 0;
        int countMaxDev = 0;
        double deviation;

        Mat lab = new Mat();
        Imgproc.cvtColor(bgr, lab, Imgproc.COLOR_BGR2Lab);

//        CalibrationCard card = CalibrationCard.getInstance(versionNumber);
//        CalibrationData data = card.readCalibrationFile(context);
        double[][] points = card.createWhitePointArray(lab, data);

        //get the sum total of luminosity values
        for(int i=0; i< points.length; i++) {
            sumLum += points[i][2];
        }

        double avgLum = sumLum / points.length;
        double avgLumReciproc = 1.0 / avgLum;

        for(int i=0; i < points.length; i++) {
            double lum = points[i][2];
            deviation = Math.abs(lum - avgLum) * avgLumReciproc;

            // count number of points that differ more than CONTRAST_DEVIATION_FRACTION from the average
            if(deviation > Constant.CONTRAST_DEVIATION_FRACTION)
            {
                countDev++;
            }

            // count number of points that differ more than CONTRAST_MAX_DEVIATION_FRACTION from the average
            if(deviation > Constant.CONTRAST_MAX_DEVIATION_FRACTION)
            {
                countMaxDev++;
            }
        }

        // the countMaxDev is already counted once in countDev. The following formula
        // lets points that are way off count 10 times as heavy in the result.
        // maximise to 100%
        double result = Math.min(countDev + 9 * countMaxDev,points.length);

        return (result / points.length) * 100.0;
    }

//    /*method for shadow detection
//    * @param Mat : a 'cut-out' of the test card between the centers of the finder patterns.
//    * @return : a percentage of how many lines have shadow.
//     */
//    public static double getShadowPercentage(Mat warpMat) throws JSONException
//    {
//        //NB: warpMat should be bgr color scheme
//        Mat workMat = warpMat.clone();
//        Mat gray = new Mat();
//        List<Double> minValList = new ArrayList<>();
//        List<Double> maxValList = new ArrayList<>();
//
//        //how much shadow do we tolerate?
//        double maxDiff = 30;
//        double totalLinesWithShadow = 0;
//
//        String json = AssetsManager.getInstance().loadJSONFromAsset("calibration1.json");
//        JSONObject calObj = new JSONObject(json);
//
//        JSONObject calData = calObj.getJSONObject("calData");
//        double hsize = calData.getDouble("hsize");
//        double vsize = calData.getDouble("vsize");
//        double hratio = warpMat.width()/hsize;
//        double vratio = warpMat.height()/vsize;
//
//        JSONObject whiteData = calObj.getJSONObject("whiteData");
//        JSONArray lines = whiteData.getJSONArray("lines");
//
//        JSONArray pArr;
//        for(int i=0; i < lines.length(); i++)
//        {
//            JSONObject lineObj = lines.getJSONObject(i);
//            pArr = lineObj.getJSONArray("p");
//            double width = lineObj.getDouble("width");
//
//            //if line is vertical, xdiff will be zero. Then we take the value of width to be the xdiff
//            //subtract x values
//            double xdiff = Math.max(width * hratio, (pArr.getDouble(2) - pArr.getDouble(0)) * hratio);
//
//            //if line is horizontal, ydiff will be zero. Then we take the value of width to be the ydiff
//            //subtract y values
//            double ydiff = Math.max(width * vratio, (pArr.getDouble(3) - pArr.getDouble(1))*vratio);
//
//
//            //create rectangle to make a submat
//            Rect whiteRect = new Rect(
//                    (int)Math.floor(pArr.getDouble(0) * hratio),
//                    (int)Math.floor(pArr.getDouble(1) * vratio),
//                    (int)Math.floor(xdiff),
//                    (int)Math.floor(ydiff));
//
//            Mat submat = workMat.submat(whiteRect).clone();
//
//            //convert to gray
//            Imgproc.cvtColor(submat, gray, Imgproc.COLOR_BGR2GRAY);
//
//            //blur the image to exclude noise
//            Imgproc.medianBlur(gray, gray, 5);
//
//            //get the min and max value of the gray mat
//            Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(gray);
//
//            //add min and max values to list for later cross-checking
//            minValList.add(minMaxLocResult.minVal);
//            maxValList.add(minMaxLocResult.maxVal);
//
//            //calculate the difference between min and max values
//            double grayDiff = minMaxLocResult.maxVal - minMaxLocResult.minVal;
//
//            //System.out.println("***shadow grayDiff: " + i + " = " + grayDiff);
//
//            //if the difference between min gray and max gray is higher than maxDiff
//            //we have a shadow
//            if(grayDiff > maxDiff)
//            {
//                totalLinesWithShadow ++;
//            }
//
//            //visualise for debugging purposes
//           /* Point point1 = new Point((int)Math.floor(pArr.getDouble(0) * hratio),
//                    (int)Math.floor(pArr.getDouble(1) * vratio));
//            Point point2 = new Point((int)Math.floor(pArr.getDouble(2) * hratio),
//                    (int)Math.floor(pArr.getDouble(3) * vratio));
//
//            Imgproc.rectangle(warpMat, point1, point2, new Scalar(0, 0, 255, 255), 1);
//            //visualise min and max
//            Point minLoc = new Point(minMaxLocResult.minLoc.x + (pArr.getDouble(0) * hratio),
//                    minMaxLocResult.minLoc.y + (pArr.getDouble(1) * vratio));
//            Point maxLoc = new Point(minMaxLocResult.maxLoc.x + (pArr.getDouble(2) * hratio),
//                    minMaxLocResult.maxLoc.y);
//
//            Scalar color;
//            if(xdiff>ydiff)
//            {
//               color = new Scalar(255, 0, 255, 255);
//            }
//            else
//            {
//               color = new Scalar(0, 255, 255, 255);
//            }
//            Imgproc.circle(warpMat, minLoc, 5, new Scalar(0, 255, 0, 255), -1);
//           Imgproc.circle(warpMat, maxLoc, 10, color, -1);
//           */
//        }
//
//        /*do a cross check */
//        //sort min and max values arraylist ascending
//        Collections.sort(minValList);
//        Collections.sort(maxValList);
//
//        //what is the difference between lowest and highest
//        double minValDiff = minValList.get(minValList.size()-1) - minValList.get(0);
//        double maxValDiff = maxValList.get(maxValList.size()-1) - maxValList.get(0);
//
//        //if difference is larger than maxDiff, we have a shadow
//        if(minValDiff > maxDiff || maxValDiff > maxDiff)
//        {
//            totalLinesWithShadow ++;
//        }
//        workMat.release();
//        gray.release();
//
//        return totalLinesWithShadow/(lines.length()+2) * 100;
//    }

    //method to calculate angle the camera has to the test card
    public static float[] getAngle(FinderPatternInfo info)
    {
        if(info==null)
            return null;

        //sort the patterns
        //in portrait mode the result will be: topleft-topright-bottomleft-bottomright
        List<Point> points = sortFinderPatternInfo(info);

        //angle in vertical direction of the device (= horizontal in preview data)
        //between topleft and topright
        double distanceTopHor = points.get(1).x - points.get(0).x;
        double distanceTopVer = points.get(1).y - points.get(0).y;
        float atan2Top = (float) Math.atan2(distanceTopVer, distanceTopHor);

        //System.out.println("***atan2Top: " + atan2Top + " deg.: " + Math.toDegrees(atan2Top));

        //angle in horizontal direction of the device (= vertical in preview)
        //between topleft and bottomleft
        double distanceLeftHor = points.get(2).x - points.get(0).x;
        double distanceLeftVer = points.get(2).y - points.get(0).y;
        float atan2Left = (float) Math.atan2(distanceLeftHor, distanceLeftVer); //switch hor and ver to make it approach zero

        //System.out.println("***atan2Left: " + atan2Left + " deg.: " + Math.toDegrees(atan2Left));

        return new float[]{(float) Math.toDegrees(atan2Top), (float) Math.toDegrees(atan2Left)};
    }

    public static List<Point> sortFinderPatternInfo(FinderPatternInfo info)
    {
        List<Point> points = new ArrayList<>();
        points.add(new Point(info.getTopRight().getX(),  info.getTopRight().getY()));
        points.add(new Point(info.getTopLeft().getX(), info.getTopLeft().getY()));
        points.add(new Point(info.getBottomLeft().getX(), info.getBottomLeft().getY()));
        points.add(new Point(info.getBottomRight().getX(), info.getBottomRight().getY()));

        Collections.sort(points, new OpenCVUtils.PointComparator());

        return points;
    }

    public static String fromSecondsToMMSS(int seconds) throws Exception
    {
        if(seconds>3600)
            throw new Exception("more than an hour");

        int m = (int)Math.floor(seconds/60);
        int s = seconds - (m * 60);

        String mm = m>0? String.format("%2d", m) + ":": "";
        String ss = m>0? String.format("%02d",s): String.format("%2d",s);

        return mm + ss;
    }
}
