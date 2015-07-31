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

        Imgproc.cvtColor(striparea, dst, Imgproc.COLOR_RGB2GRAY, 0);
        System.out.println("***bgra dst w, h: " + dst.width() + " , " + dst.height() + CvType.typeToString(dst.type()) + " dst :" + dst.toString());

        Imgproc.threshold(dst, dst, 40, 255, Imgproc.THRESH_BINARY);
        Imgproc.Canny(dst, dst, 20, 140, 3, true);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint innermostContours = new MatOfPoint();
        MatOfInt4 mContours = new MatOfInt4();
        contours.clear();

        Imgproc.findContours(dst, contours, mContours, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        double maxParent = -Double.MAX_VALUE;
        for (int x = 0; x < contours.size(); x++) {

            // System.out.println("***contours rows: " + x + "  " + contours.get(x).rows() + " " + contours.get(x).get(0, 0));

            double areasize = Imgproc.contourArea(contours.get(x));
            if(mContours.get(0,x)[3] >= 0)//has parent, inner (hole) contour of a closed edge
            {
                if(mContours.get(0,x)[3] > maxParent)
                {
                    if(areasize > 250) {
                        double[] d = mContours.get(0,x);

                        for (double v : d)
                        {
                            System.out.println("*** value of d  " + x + ":  = " + v);
                        }
                        innermostContours = contours.get(x);
                        maxParent = mContours.get(0, x)[3];
                    }
                }

                //Imgproc.drawContours(striparea, contours, x-1, new Scalar(0, 0, 255, 255), -1);
                // Imgproc.drawContours(striparea, contours, x, new Scalar(255, 0, 0, 255), 2);
            }
            else
            {
                // Imgproc.drawContours(striparea, contours, x, new Scalar(0, 255, 0, 255), 2);
            }
        }

        List<Point> innermostList = innermostContours.toList();
        if(innermostList.size()>0) {
            System.out.println("*** innermostList  " + innermostList.get(0).x + "," + innermostList.get(0).y);

            Imgproc.drawContours(striparea, contours, (int) maxParent + 1, new Scalar(255, 0, 0, 255), 2);

            int x = OpenCVUtils.getMinX(innermostList);
            int y = OpenCVUtils.getMinY(innermostList);
            int width = OpenCVUtils.getMaxX(innermostList) - x;
            int height = OpenCVUtils.getMaxY(innermostList) - y;
            Rect roi = new Rect(x, y, width, height);
            return striparea.submat(roi);

        }
        return null;
    }

    public static Mat detectColor(Mat mRgba, List<ResultActivity.ColorDetected> colors) {
        // Convert the image into an HSV image
        Mat mHSVMat = new Mat();
        Mat gray = new Mat();
//        int[] byteColourTrackCentreHue = new int[3];
//        // green = 60 // mid yellow  27
//        byteColourTrackCentreHue[0] = 180;
//        byteColourTrackCentreHue[1] = 100;
//        byteColourTrackCentreHue[2] = 255;
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat mContours = new Mat();
        double d;
        int iContourAreaMin = 10;
        MatOfPoint2f mMOP2f1, mMOP2f2, mMOP2fptsPrev, mMOP2fptsThis, mMOP2fptsSafe;

        mMOP2f1 = new MatOfPoint2f();
        mMOP2f2 = new MatOfPoint2f();

        List<Point> pts = new ArrayList<Point>();
        Scalar colorRed = new Scalar(255, 0, 0, 255);
        Scalar colorGreen = new Scalar(0, 255, 0, 255);
        int iLineThickness = 1;

        System.out.println("*** detct color imGE TYPE: " + CvType.typeToString(mRgba.type()));
        Imgproc.cvtColor(mRgba, mHSVMat, Imgproc.COLOR_RGB2HSV, 3);

//        Core.inRange(mHSVMat, new Scalar(0, 5, 40),
//                new Scalar(180, 255, 255), mHSVMat);

        Mat dst = new Mat();


        int tresh = 15;
        List<Mat> channels = new ArrayList<>();
        //Imgproc.cvtColor(mRgba, dst, Imgproc.COLOR_RGB2HSV);
        // Core.split(dst, channels);

        //Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0, 0);
        //  Imgproc.threshold(channels.get(2), channels.get(2), 40, 255, Imgproc.THRESH_BINARY);
        // Imgproc.Canny(channels.get(2), dst, 80, 120, 3, true);
        //Core.inRange(channels.get(2), new Scalar(0,0,100), new Scalar(180,255,255), channels.get(2));
        //Core.merge(channels, dst);

        // Imgproc.cvtColor(dst, dst, Imgproc.COLOR_HSV2RGB);
        Imgproc.cvtColor(mRgba, dst, Imgproc.COLOR_RGB2GRAY);
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
//        Imgproc.dilate(gray, gray, kernel);



//
//        MatOfPoint lines = new MatOfPoint();
//        Imgproc.HoughLinesP(dst, lines, 1, 1, 1, 1, 1);
//
//        for(int j=0;j<lines.rows();j++)
//        {
//            for(int i=0;i<lines.cols();i++)
//            {
//                double[] line = lines.get(j,i);
//                Core.line(mRgba, new Point(line[0], line[1]), new Point(line[2], line[3]), new Scalar(255, 0,0,255), 5);
//            }
//        }
        //  Imgproc.Sobel(gray, dst, CvType.CV_64F, 1, 0, 5, 1, 1);

//        Imgproc.Laplacian(gray, dst, CvType.CV_64F);
//        sobelx = cv2.Sobel(img,cv2.CV_64F,1,0,ksize=5)  # x
//        sobely = cv2.Sobel(img,cv2.CV_64F,0,1,ksize=5)  # y

        //mRgba.copyTo(dst, gray);
        contours.clear();

        // dst.convertTo(dst, CvType.CV_8UC1);
        Imgproc.findContours(dst, contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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

                    Collections.sort(pts, new PointComparator());

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

                    Mat sub = mRgba.submat(top , top + (maxY - top), left, left + (maxX-left));

                    Scalar mean = Core.mean(sub);

                    for(double val: mean.val) {

                        System.out.println("***Scalar colors: "+ x + "  " + val);
                    }

                    int color = Color.argb((int) mean.val[3], (int) Math.round(mean.val[0]), (int) Math.round(mean.val[1]), (int) Math.round(mean.val[2]));
                    colors.add(new ResultActivity.ColorDetected(color, left));

                }
            }
        }

        return dst;
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
    public static void enhanceContrast(Mat src) {
        Mat equalsrc = new Mat();
        Mat dest = new Mat();
        List<Mat> channels = new ArrayList<>();
        Imgproc.cvtColor(src, equalsrc, Imgproc.COLOR_RGB2YCrCb);
        Core.split(equalsrc, channels);
        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Core.merge(channels, equalsrc);
        Imgproc.cvtColor(equalsrc, dest, Imgproc.COLOR_YCrCb2RGB);
    }

    //sharpen image
    public static void sharpen(Mat src) {
        Mat dest = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
        Imgproc.filter2D(src, dest, -1, kernel);
        Imgproc.GaussianBlur(dest, dest, new Size(3, 3), 0, 0);
        Core.addWeighted(src, 1.5, dest, -0.5, 0, dest);
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
