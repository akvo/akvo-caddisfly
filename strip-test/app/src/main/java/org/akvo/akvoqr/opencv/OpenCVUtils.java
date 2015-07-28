package org.akvo.akvoqr.opencv;

import android.graphics.Color;

import org.akvo.akvoqr.MyPreviewCallback;
import org.akvo.akvoqr.ResultActivity;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.video.BackgroundSubtractorMOG2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by linda on 7/23/15.
 */
public class OpenCVUtils {

    public static void detectColor(Mat mRgba, List<ResultActivity.ColorDetected> colors) {
        // Convert the image into an HSV image
        Mat mHSVMat = new Mat();
//        int[] byteColourTrackCentreHue = new int[3];
//        // green = 60 // mid yellow  27
//        byteColourTrackCentreHue[0] = 180;
//        byteColourTrackCentreHue[1] = 100;
//        byteColourTrackCentreHue[2] = 255;
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat mContours = new Mat();
        double d;
        int iContourAreaMin = 100;
        MatOfPoint2f mMOP2f1, mMOP2f2, mMOP2fptsPrev, mMOP2fptsThis, mMOP2fptsSafe;

        mMOP2f1 = new MatOfPoint2f();
        mMOP2f2 = new MatOfPoint2f();

        List<Point> pts = new ArrayList<Point>();
        Scalar colorRed = new Scalar(255, 0, 0, 255);
        Scalar colorGreen = new Scalar(0, 255, 0, 255);
        int iLineThickness = 1;

        System.out.println("*** detct color imGE TYPE: " + CvType.typeToString(mRgba.type()));
        Imgproc.cvtColor(mRgba, mHSVMat, Imgproc.COLOR_RGB2HSV, 3);

        Core.inRange(mHSVMat, new Scalar(0,5,40),
                new Scalar(180, 255, 255), mHSVMat);

        contours.clear();

        Imgproc.findContours(mHSVMat, contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int x = 0; x < contours.size(); x++) {
            d = Imgproc.contourArea(contours.get(x));

            if (d > iContourAreaMin) {
                // get an approximation of the contour (last but one param is the min required
                // distance between the real points and the new approximation (in pixels)

                contours.get(x).convertTo(mMOP2f1, CvType.CV_32FC2);

                Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 10, true);

                // convert back to MatOfPoint and put it back in the list
                mMOP2f2.convertTo(contours.get(x), CvType.CV_32S);

                if (contours.get(x).rows() == 4) {

                    Converters.Mat_to_vector_Point2f(contours.get(x), pts);

                    Imgproc.drawContours(mRgba, contours, x, colorRed, iLineThickness);

                    Collections.sort(pts, new MyPreviewCallback.PointComparator());

                    Core.line(mRgba, pts.get(0), pts.get(3), colorRed, iLineThickness);
                    Core.line(mRgba, pts.get(1), pts.get(2), colorRed, iLineThickness);
                    Core.circle(mRgba, pts.get(0), 5, colorGreen);


                    int left = (int) Math.round(pts.get(0).x);
                    int top = (int) Math.round(pts.get(0).y);
                    int maxX = getMaxX(pts);
                    int maxY = getMaxY(pts);

                    System.out.println("*** top: " + top);
                    System.out.println("*** left: " + left);
                    System.out.println("*** maxY: " + maxY);
                    System.out.println("*** maxX: " + maxX);

                    Mat sub = mRgba.submat(top, top + (maxY - top), left, left + (maxX-left));

                    Scalar mean = Core.mean(sub);

                    for(double val: mean.val) {

                        System.out.println("***Scalar colors: "+ x + "  " + val);
                    }

                    int color = Color.rgb((int) (mean.val[0]), (int) mean.val[1], (int) mean.val[2]);
                    colors.add(new ResultActivity.ColorDetected(color, left));

                }
            }
        }

    }

    private static int getMaxX(List<Point> list)
    {
        int max = Integer.MIN_VALUE;
        for(Point p: list){
            if(p.x>max)
                max = (int) Math.round(p.x);
        }

        return max;
    }

    private static int getMaxY(List<Point> list)
    {
        int max = Integer.MIN_VALUE;
        for(Point p: list){
            if(p.y>max)
                max = (int) Math.round(p.y);
        }

        return max;
    }



    public double focusStandardDev(Mat src)
    {
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();

        Core.meanStdDev(src, mean, stddev);

        Scalar mu = new Scalar(0);
        Scalar sigma = new Scalar(0);

        double focusMeasure = 0;

        for(int i=0;i<mean.rows();i++) {
            for (int j = 0; j < mean.cols(); j++) {
                double[] d = mean.get(i, j);
                if (d[0] > 0 ) {
                    mu = new Scalar(d);
                }
                System.out.println("***mu: " + mu.toString());

            }
        }

        for(int i=0;i<stddev.rows();i++)
        {
            for(int j=0; j< stddev.cols();j++)
            {
                double[] d = stddev.get(i,j);
                if(d[0] > 0) {
                    sigma = new Scalar(d);
                }
                System.out.println("***sigma: " + sigma.toString());

            }
        }

        focusMeasure = (sigma.val[0]*sigma.val[0]) / mu.val[0];


        return focusMeasure;
    }

    public double focusLaplacian(Mat src) {

        int kernel_size = 3;
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_8UC1;
        double maxLap = -32767;

        Mat src_gray = new Mat();
        Mat dst = new Mat();

        Imgproc.GaussianBlur(src, src, new Size(3, 3), 0, 0, Imgproc.BORDER_DEFAULT);
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.Laplacian(src_gray, dst, ddepth, kernel_size, scale, delta, Imgproc.BORDER_DEFAULT);

        if (!dst.empty()) {

            for (int i = 0; i < dst.rows(); i++) {

                for (int j = 0; j < dst.cols(); j++) {

                    double[] pixelData = dst.get(i, j);
                    if (pixelData != null && pixelData.length > 0) {
                        if (pixelData[0] > maxLap)
                            maxLap = pixelData[0];
                    }

                }
            }
            System.out.println("***maxLap: " + maxLap);

        }
        return maxLap;
    }

    public Mat shadowDetection(Mat src)
    {
        Mat fgmask = new Mat();
        BackgroundSubtractorMOG backgroundSubtractor = new BackgroundSubtractorMOG();
        backgroundSubtractor.apply(src, fgmask);

        return fgmask;
    }

    public Mat shadowDetectionMOG2(Mat src)
    {
        Mat fgmask = new Mat();
        BackgroundSubtractorMOG2 backgroundSubtractorMOG2 = new BackgroundSubtractorMOG2(5, 0.5f, true);

        backgroundSubtractorMOG2.apply(src, fgmask, 0);

        return fgmask;
    }
}
