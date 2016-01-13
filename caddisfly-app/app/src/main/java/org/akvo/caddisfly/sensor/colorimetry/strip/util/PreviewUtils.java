package org.akvo.caddisfly.sensor.colorimetry.strip.util;

import org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration.CalibrationData;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.FinderPatternInfo;
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
    public static double getShadowPercentage(Mat bgr, CalibrationData data) {

        double sumLum = 0;
        int countDev = 0;
        int countMaxDev = 0;
        double deviation;

        Mat lab = new Mat();
        Imgproc.cvtColor(bgr, lab, Imgproc.COLOR_BGR2Lab);

        double[][] points = CalibrationCard.createWhitePointArray(lab, data);

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
        double result = Math.min(countDev + 9 * countMaxDev, points.length);

        lab.release();
        return (result / points.length) * 100.0;
    }

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

    private static List<Point> sortFinderPatternInfo(FinderPatternInfo info)
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

        int m = (int) Math.floor(seconds / 60);
        int s = seconds - (m * 60);

        String mm = m>0? String.format("%2d", m) + ":": "";
        String ss = m>0? String.format("%02d", s): String.format("%2d", s);

        return mm + ss;
    }
}
