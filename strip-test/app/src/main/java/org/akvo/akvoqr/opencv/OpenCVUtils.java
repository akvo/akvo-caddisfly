package org.akvo.akvoqr.opencv;

import android.graphics.Color;

import org.akvo.akvoqr.ResultActivity;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.video.BackgroundSubtractorMOG2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 7/23/15.
 */
public class OpenCVUtils {

    public static Mat perspectiveTransform(FinderPatternInfo info, Mat mbgra)
    {
        List<Point> srcList = new ArrayList<>();

        //coordinates for the rect (the finder pattern centers)
        srcList.add(new Point(info.getTopLeft().getX(),
                info.getTopLeft().getY()));
        srcList.add(new Point(info.getTopRight().getX(),
                info.getTopRight().getY()));
        srcList.add(new Point(info.getBottomLeft().getX(),
                info.getBottomLeft().getY()));
        srcList.add(new Point(info.getBottomRight().getX(),
                info.getBottomRight().getY()));

        System.out.println("***before sort:");
        System.out.println("***topleft: " + srcList.get(0).x + " ," + srcList.get(0).y);
        System.out.println("***topright: " + srcList.get(1).x + " ," + srcList.get(1).y);
        System.out.println("***bottomleft: " + srcList.get(2).x + " ," + srcList.get(2).y);
        System.out.println("***bottomright: " + srcList.get(3).x + ", " + srcList.get(3).y);

        //Sort the arraylist of finder patterns based on a comparison of the sum of x and y values. Lowest values come first,
        // so the result will be: top-left, bottom-left, top-right, bottom-right. Because top-left always has the lowest sum of x and y
        // and bottom-right always the highest
        Collections.sort(srcList, new PointComparator());

        System.out.println("***after sort:");
        System.out.println("***topleft: " + srcList.get(0).x +" ,"+ srcList.get(0).y);
        System.out.println("***bottomleft: " + srcList.get(1).x +" ,"+ srcList.get(1).y);
        System.out.println("***topright: " + srcList.get(2).x +" ,"+ srcList.get(2).y);
        System.out.println("***bottomright: "+ srcList.get(3).x + ", "+ srcList.get(3).y);

        //source quad
        //here we maintain the order: top-left, top-right, bottom-left, bottom-right
        Point[] srcQuad = new Point[4];
        srcQuad[0]=srcList.get(0);
        srcQuad[1]=srcList.get(2);
        srcQuad[2]=srcList.get(1);
        srcQuad[3]=srcList.get(3);
        //destination quad corresponding with srcQuad
        Point[] dstQuad = new Point[4];
        dstQuad[0] = new Point( 0,0 );
        dstQuad[1] = new Point( mbgra.cols() - 1, 0 );
        dstQuad[2] = new Point( 0, mbgra.rows() - 1 );
        dstQuad[3] = new Point(mbgra.cols()-1, mbgra.rows()-1);

        //srcQuad and destQuad to MatOfPoint2f objects, needed in perspective transform
        MatOfPoint2f srcMat2f = new MatOfPoint2f(srcQuad);
        MatOfPoint2f dstMat2f = new MatOfPoint2f(dstQuad);

        //make a destination mat for a warp
        Mat warp_dst = Mat.zeros(mbgra.rows(), mbgra.cols(), mbgra.type());

        //get a perspective transform matrix
        Mat warp_mat = Imgproc.getPerspectiveTransform(srcMat2f, dstMat2f);

        //do the warp
        Imgproc.warpPerspective(mbgra, warp_dst,warp_mat, warp_dst.size());

        return warp_dst;
    }
    public static Mat detectStrip(Mat striparea)
    {
        Mat dst = new Mat();
        Mat range = new Mat();
        List<Mat> channels = new ArrayList<>();

        Imgproc.cvtColor(striparea, dst, Imgproc.COLOR_RGB2Lab, 0);

//        Imgproc.GaussianBlur(dst, dst, new Size(17, 17), 0);
        Imgproc.medianBlur(dst, dst, 11);
        Core.split(dst, channels);
//        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Core.MinMaxLocResult result = Core.minMaxLoc(channels.get(0));

        System.out.println("***result L detect strip min val: " + result.minVal + " max val : " + result.maxVal);

        double treshold = (result.maxVal - result.minVal) / 2;

//        Imgproc.threshold(channels.get(0), channels.get(0), treshold, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.adaptiveThreshold(channels.get(0), channels.get(0), 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 11, 2);
        Core.merge(channels, dst);


//        Imgproc.cvtColor(dst, range, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(dst, range, 40, 120, 3, true);

        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_Lab2RGB);
//        Core.inRange(dst, new Scalar(200, 0, 0), new Scalar(255, 255, 255), range);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint innermostContours = new MatOfPoint();
        MatOfInt4 mContours = new MatOfInt4();
        MatOfPoint2f mMOP2f = new MatOfPoint2f();
        List<Point> pts = new ArrayList<>();

        Imgproc.findContours(channels.get(0), contours, mContours, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));
        double maxParent = -Double.MAX_VALUE;
        for (int x = 0; x < contours.size(); x++) {


//            if(areasize > 100)
//                Imgproc.drawContours(dst, contours, x, new Scalar(0, 255, 0, 255), 1);
            //make square
            contours.get(x).convertTo(mMOP2f, CvType.CV_32FC2);
            Imgproc.approxPolyDP(mMOP2f, mMOP2f, 0.01 * Imgproc.arcLength(mMOP2f, true), true);
            mMOP2f.convertTo(contours.get(x), CvType.CV_32S);

            double areasize = Imgproc.contourArea(contours.get(x));

//            if(areasize > 10) {
//                if (contours.get(x).rows() == 4) {
//                    Converters.Mat_to_vector_Point2f(contours.get(x), pts);
//                    Rect roi = new Rect();
//                    Mat submat = striparea.submat(roi);
//                    Core.rectangle(dst, pts.get(0), pts.get(2), new Scalar(255, 0, 0, 255), 2);
//
//            }

            if(mContours.get(0,x)[3] >= 0)//has parent, inner (hole) contour of a closed edge
            {

                if(areasize > 10000)
                {

                    if(mContours.get(0,x)[3] > maxParent)
                    {

                        double[] d = mContours.get(0,x);

                        for (double v : d)
                        {
                            //System.out.println("*** value of d  " + x + ":  = " + v);
                        }
                        innermostContours = contours.get(x);
                        maxParent = mContours.get(0, x)[3];
                    }
                    Imgproc.drawContours(dst, contours, x, new Scalar(255, 0, 255, 255), 1);
                }

                //Imgproc.drawContours(striparea, contours, x-1, new Scalar(0, 0, 255, 255), -1);

            } else {
                Imgproc.drawContours(dst, contours, x, new Scalar(0, 255, 0, 255), 1);
            }
        }

        List<Point> innermostList = innermostContours.toList();
        if(innermostList.size()>0) {

            Point point1 = new Point(OpenCVUtils.getMinX(innermostList), OpenCVUtils.getMinY(innermostList));
            Point point2 = new Point(OpenCVUtils.getMaxX(innermostList), OpenCVUtils.getMaxY(innermostList));

            System.out.println("*** innermostList 0 : " + innermostList.get(0).x + "," + innermostList.get(0).y);
            System.out.println("*** innermostList min :" + point1.x + "," + point1.y);
            System.out.println("*** innermostList max :" + point2.x + "," + point2.y);

            double d = Imgproc.contourArea(innermostContours);
            System.out.println("***contour area innermost: " + d);
//            Imgproc.drawContours(dst, contours, (int) maxParent + 1, new Scalar(255, 0, 0, 255), 2);

            int x = OpenCVUtils.getMinX(innermostList);
            int y = OpenCVUtils.getMinY(innermostList);
            int width = OpenCVUtils.getMaxX(innermostList) - x;
            int height = OpenCVUtils.getMaxY(innermostList) - y;
            Rect roi = new Rect(x, y, width, height);

            Core.rectangle(dst, point1, point2, new Scalar(255, 0,0,255),3);
            return striparea.submat(roi);

        }

        return striparea;
    }

    public static Mat detectStripPatchesAdaptiveTresh(Mat strip)
    {
        Mat edges = new Mat();
        Mat range = new Mat();
        List<Mat> channels = new ArrayList<>();
        List<Point> pts = new ArrayList<Point>();
        MatOfPoint2f mMOP2f = new MatOfPoint2f();

        ResultActivity.stripColors.clear();

        Imgproc.cvtColor(strip, edges, Imgproc.COLOR_RGB2Lab, 0);

//        Imgproc.GaussianBlur(edges, edges, new Size(5, 5), 0);
        Imgproc.medianBlur(edges, edges, 11);
        Core.split(edges, channels);

        Core.MinMaxLocResult result0 = Core.minMaxLoc(channels.get(0));
        System.out.println("***channel L . min val: " + result0.minVal + " max val: " + result0.maxVal);
        Core.MinMaxLocResult result1 = Core.minMaxLoc(channels.get(1));
        System.out.println("***channel a . min val: " + result1.minVal + " max val: " + result1.maxVal);
        Core.MinMaxLocResult result2 = Core.minMaxLoc(channels.get(2));
        System.out.println("***channel b . min val: " + result2.minVal + " max val: " + result2.maxVal);

        Imgproc.threshold(channels.get(0), channels.get(0), 0, 175, Imgproc.THRESH_BINARY);
//        Imgproc.adaptiveThreshold(channels.get(1), channels.get(1), 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
//                Imgproc.THRESH_BINARY, 11, 2);
//        Imgproc.adaptiveThreshold(channels.get(2), channels.get(2), 0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
//                Imgproc.THRESH_BINARY, 11, 2);

//        Imgproc.threshold(channels.get(1), channels.get(1), 0, 128, Imgproc.THRESH_BINARY);
//        Imgproc.threshold(channels.get(2), channels.get(2), 0, 0, Imgproc.THRESH_BINARY);

//        Imgproc.threshold(channels.get(1), channels.get(1), 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
//        Imgproc.threshold(channels.get(2), channels.get(2), 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Core.merge(channels, edges);

        Mat temp = edges.clone();
        double minChroma = Double.MAX_VALUE;
        for(int j=5;j<edges.rows()-5;j++) {
            for (int i = 5; i < edges.cols() - 5; i += 1) {
                double[] val = edges.get(j, i);
                double chromaPix = Math.sqrt(val[1] * val[1] + val[2] * val[2]);

                if(chromaPix < minChroma)
                    minChroma = chromaPix;
            }
        }
        System.out.println("***minChroma: " + minChroma);
        for(int j=5;j<edges.rows()-5;j++)
        {
            for(int i=5;i<edges.cols()-5;i+=1) {
//                Mat sub = edges.submat(new Range(j - 1, edges.rows()-1), new Range(i - 1, i));

                double[] val = edges.get(j, i);
                double chromaPix = Math.sqrt(val[1] * val[1] + val[2] * val[2]);

                if(Math.abs(chromaPix - minChroma) > 5)
                {
                    val[0] = 0;
//                    val[1] = 0;
//                    val[2] = 0;
                    temp.put(j, i, val);
                }
                else
                {
                    val[0] = 255;
                    val[1] = 129;
                    val[2] = 129;
                    temp.put(j, i, val);
                }
            }
        }
        edges = temp.clone();

        Core.split(edges, channels);
//        Imgproc.cvtColor(edges, edges, Imgproc.COLOR_RGB2HSV);
//        Core.split(edges, channels);
//        Imgproc.threshold(channels.get(0), channels.get(0), 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
//        Core.merge(channels, edges);
//        Imgproc.cvtColor(edges, edges, Imgproc.COLOR_HSV2RGB);

        Core.inRange(channels.get(0), new Scalar(0), new Scalar(1), range);

        Imgproc.cvtColor(edges, edges, Imgproc.COLOR_Lab2RGB);
//        Imgproc.cvtColor(range, range, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.cvtColor(edges, edges, Imgproc.COLOR_Lab2RGB);

//        Imgproc.Canny(range, range, 40, 120, 3, true);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        MatOfInt4 hierarchy = new MatOfInt4();

        Imgproc.findContours(range, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));

        List<Mat> outermostContoursList = new ArrayList<>();

        for (int x = 0; x < contours.size(); x++)
        {
            double areasize = Imgproc.contourArea(contours.get(x));

//            Imgproc.drawContours(edges, contours, x, new Scalar(0, 250, 0, 255),2);

            if (hierarchy.get(0,x)!=null && hierarchy.get(0, x)[3] < 0)//has no parent, outer contour
            {
                System.out.println("***areasize: " + areasize);
                if (areasize > 250)
                {
                    double[] d = hierarchy.get(0, x);

                    for (double v : d)
                    {
                        System.out.println("*** value of outer contour  " +  x + ":  = " + v);
                    }
                    outermostContoursList.add(contours.get(x));
//                    Imgproc.drawContours(edges, contours, x, new Scalar(255, 0, 0, 255), 1);
                }
            }

        }
        if(outermostContoursList.size()>0)
        {
            for(Mat outer: outermostContoursList) {
                //make square
//                outer.convertTo(mMOP2f, CvType.CV_32FC2);
//                Imgproc.approxPolyDP(mMOP2f, mMOP2f, 0.01 * Imgproc.arcLength(mMOP2f, true), true);
//                mMOP2f.convertTo(outer, CvType.CV_32S);

                if (outer.rows() > 3) {
                    System.out.println("***contour area outer: " + Imgproc.contourArea(outer));
                    if (Imgproc.contourArea(outer) > 2) {

                        Converters.Mat_to_vector_Point2f(outer, pts);
                        detectColor(strip, pts);
                        Point point1 = new Point(getMinX(pts), getMinY(pts));
                        Point point2 = new Point(getMaxX(pts), getMaxY(pts));
                        Core.rectangle(edges,point1, point2, new Scalar(0,255,0,255), 1);
                    }
                }
            }
        }

        return edges;
    }

    public static Mat detectStripPatchesOTSUTresh(Mat strip)
    {
        Mat edges = new Mat();
        List<Mat> channels = new ArrayList<>();
        List<Point> pts = new ArrayList<Point>();
        MatOfPoint2f mMOP2f = new MatOfPoint2f();

        ResultActivity.stripColors.clear();
        Imgproc.cvtColor(strip, edges, Imgproc.COLOR_RGB2Lab, 0);

        Imgproc.GaussianBlur(edges, edges, new Size(5, 5), 0);
        Core.split(edges, channels);

        Core.MinMaxLocResult result0 = Core.minMaxLoc(channels.get(0));
        System.out.println("***channel L . min val: " + result0.minVal + " max val: " + result0.maxVal);
        Core.MinMaxLocResult result1 = Core.minMaxLoc(channels.get(1));
        System.out.println("***channel a . min val: " + result1.minVal + " max val: " + result1.maxVal);
        Core.MinMaxLocResult result2 = Core.minMaxLoc(channels.get(2));
        System.out.println("***channel b . min val: " + result2.minVal + " max val: " + result2.maxVal);

        Imgproc.threshold(channels.get(0), channels.get(0), 0, 128, Imgproc.THRESH_BINARY);
        Imgproc.threshold(channels.get(1), channels.get(1), 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels.get(2), channels.get(2), 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Core.merge(channels, edges);
        Imgproc.cvtColor(edges, edges, Imgproc.COLOR_Lab2RGB);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 mContours = new MatOfInt4();

        Imgproc.findContours(channels.get(1), contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));
        Imgproc.findContours(channels.get(2), contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));

        for (int x = 0; x < contours.size(); x++) {

            //make square
            contours.get(x).convertTo(mMOP2f, CvType.CV_32FC2);
            Imgproc.approxPolyDP(mMOP2f, mMOP2f, 0.1 * Imgproc.arcLength(mMOP2f, true), true);
            mMOP2f.convertTo(contours.get(x), CvType.CV_32S);

            if(contours.get(x).rows() == 4) {
                if (Imgproc.contourArea(contours.get(x)) > 200) {

                    Converters.Mat_to_vector_Point2f(contours.get(x), pts);
                    detectColor(strip, pts);
                    Imgproc.drawContours(edges, contours, x, new Scalar(0,255,0, 255), 1);
                }
            }
        }


        return edges;
    }
    public static void detectColor(Mat rgb,  List<Point> pts) {

        int left = getMinX(pts);
        int top = getMinY(pts);
        int maxX = getMaxX(pts);
        int maxY = getMaxY(pts);

        System.out.println("*** top: " + top);
        System.out.println("*** left: " + left);
        System.out.println("*** maxY: " + maxY);
        System.out.println("*** maxX: " + maxX);

        Mat sub = rgb.submat(top , top + (maxY - top), left, left + (maxX-left));

        Scalar mean = Core.mean(sub);

        for(double val: mean.val) {

            System.out.println("***Scalar colors: "+  val);
        }

        int color = Color.rgb((int) Math.round(mean.val[0]), (int) Math.round(mean.val[1]), (int) Math.round(mean.val[2]));
        ResultActivity.stripColors.add(new ResultActivity.ColorDetected(color, left));

    }

    public static int getMinX(List<Point> list)
    {
        int min = Integer.MAX_VALUE;
        for(Point p: list){
            if(p.x < min)
                min = (int) Math.round(p.x);
        }

        return min;
    }
    public static int getMaxX(List<Point> list)
    {
        int max = Integer.MIN_VALUE;
        for(Point p: list){
            if(p.x>max)
                max = (int) Math.round(p.x);
        }

        return max;
    }
    public static int getMinY(List<Point> list)
    {
        int min = Integer.MAX_VALUE;
        for(Point p: list){
            if(p.y < min)
                min = (int) Math.round(p.y);
        }

        return min;
    }
    public static int getMaxY(List<Point> list)
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

    //enhance contrast
    public static Mat enhanceContrast(Mat src) {
        Mat equalsrc = new Mat();
        Mat dest = new Mat();
        List<Mat> channels = new ArrayList<>();
        Imgproc.cvtColor(src, equalsrc, Imgproc.COLOR_RGB2YCrCb);
        Core.split(equalsrc, channels);
        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Core.merge(channels, equalsrc);
        Imgproc.cvtColor(equalsrc, dest, Imgproc.COLOR_YCrCb2RGB);

        return dest;
    }

    //sharpen image
    public static Mat sharpen(Mat src) {
        Mat dest = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
        Imgproc.filter2D(src, dest, -1, kernel);
        Imgproc.GaussianBlur(dest, dest, new Size(3, 3), 0, 0);
        Core.addWeighted(src, 1.5, dest, -0.5, 0, dest);

        return dest;
    }

    public static class PointComparator implements Comparator<Point>
    {

        @Override
        public int compare(Point lhs, Point rhs) {

            if(lhs.x + lhs.y < rhs.x + rhs.y)
            {
                return -1;
            }

            else
            {
                return 1;
            }

        }
    }
}
