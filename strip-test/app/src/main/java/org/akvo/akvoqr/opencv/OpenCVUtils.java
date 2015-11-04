package org.akvo.akvoqr.opencv;

import android.graphics.Color;

import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.color.ColorDetected;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 7/23/15.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class OpenCVUtils {

    public static Mat rotateImage(Mat src, RotatedRect rotatedRect, Size brandSize)
    {
        Mat rot_mat;
        Mat cropped = new Mat();

        /// Set the dst image the same type and size as src
        // Mat warp_rotate_dst = Mat.zeros(src.rows(), src.cols(), src.type());
        Mat warp_rotate_dst = new Mat(src.rows(), src.cols(), src.type());
        double angle = rotatedRect.angle;
        Size rect_size = rotatedRect.size;
        // thanks to http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/
        // we need to swap height and width if angle is lower than 45 degrees
        if (angle < -45) {
            angle += 90;
            rect_size.set(new double[]{rect_size.height, rect_size.width});
        }
        // get the rotation matrix
        rot_mat = Imgproc.getRotationMatrix2D(rotatedRect.center, angle, 1.0);

        // perform the affine transformation
        Imgproc.warpAffine(src, warp_rotate_dst, rot_mat, src.size(), Imgproc.INTER_CUBIC);

        // crop the resulting image
        if(!warp_rotate_dst.empty()) {
            Point centerBrand = new Point(
                    rotatedRect.center.x + (rotatedRect.size.width - brandSize.width)/2,
                    rotatedRect.center.y - (rotatedRect.size.height - brandSize.height)/4);

            System.out.println("***centerBrand x,y: " + centerBrand.x + ", " + centerBrand.y
                    + " diff width: " + (rotatedRect.size.width - brandSize.width)/2
                    + " diff height: " + (rotatedRect.size.height - brandSize.height)/4);

            Imgproc.getRectSubPix(warp_rotate_dst, brandSize, centerBrand, cropped);
        }
        return cropped;
    }

    public static List<Point> getOrderedPoints(double[] topleft, double[] topright,
                                               double[] bottomleft, double[] bottomright)
    {
        List<Point> srcList = new ArrayList<Point>();

        //coordinates for the rect (the finder pattern centers)
        srcList.add(new Point(topleft));
        srcList.add(new Point(topright));
        srcList.add(new Point(bottomleft));
        srcList.add(new Point(bottomright));

//        System.out.println("***before sort:");
//        System.out.println("***topleft: " + srcList.get(0).x + " ," + srcList.get(0).y);
//        System.out.println("***topright: " + srcList.get(1).x + " ," + srcList.get(1).y);
//        System.out.println("***bottomleft: " + srcList.get(2).x + " ," + srcList.get(2).y);
//        System.out.println("***bottomright: " + srcList.get(3).x + ", " + srcList.get(3).y);


        return getOrderedPoints(srcList);
    }

    /*Sort the arraylist of finder patterns based on a comparison of the sum of x and y values. Lowest values come first,
       * so the result will be: top-left, bottom-left, top-right, bottom-right in case of landscape view.
       * and: top-left, top-right, bottom-left, bottom-right in case of portrait view.
       * Because top-left always has the lowest sum of x and y
       * and bottom-right always the highest, they always come first and last.
       */
    public static List<Point> getOrderedPoints(List<Point> srcList)
    {
        Collections.sort(srcList, new PointComparator());

//        System.out.println("***after sort:");
//        System.out.println("***topleft: " + srcList.get(0).x +" ,"+ srcList.get(0).y);
//        System.out.println("***second: " + srcList.get(1).x +" ,"+ srcList.get(1).y);
//        System.out.println("***third: " + srcList.get(2).x + " ," + srcList.get(2).y);
//        System.out.println("***bottomright: "+ srcList.get(3).x + ", "+ srcList.get(3).y);

        return srcList;
    }


    public static Mat perspectiveTransform(double[] topleft, double[] topright,
                                           double[] bottomleft, double[] bottomright, Mat bgr)
            throws Exception
    {


        List<Point> srcList = getOrderedPoints(topleft, topright, bottomleft, bottomright);

        //source quad
        Point[] srcQuad = new Point[4];
        //destination quad corresponding with srcQuad
        Point[] dstQuad = new Point[4];

        //second and third Points in the list are top-right and bottom-left, but there order changes
        //depending on if portrait or landscape
        if(srcList.get(1).x > srcList.get(2).x) //it is portrait view
        {
            //clockwise: top-left, top-right, bottom-right, bottom-left
            srcQuad[0]=srcList.get(0);
            srcQuad[1]=srcList.get(1);
            srcQuad[2]=srcList.get(3);
            srcQuad[3]=srcList.get(2);

            //Because camera is in portrait mode, we need to alter the order of the positions:
            //rotating clockwise 90 degrees, bottom-left becomes top-left, top-left becomes top-right, etc.
            dstQuad[0] = new Point( bgr.cols() - 1, 0 );
            dstQuad[1] = new Point(bgr.cols()-1, bgr.rows()-1);
            dstQuad[2] = new Point( 0, bgr.rows() - 1 );
            dstQuad[3] = new Point( 0,0 );

        }
        else
        {
            //clockwise: top-left, top-right, bottom-right, bottom-left
            srcQuad[0]=srcList.get(0);
            srcQuad[1]=srcList.get(2);
            srcQuad[2]=srcList.get(3);
            srcQuad[3]=srcList.get(1);

            dstQuad[0] = new Point( 0,0 );
            dstQuad[1] = new Point( bgr.cols() - 1, 0 );
            dstQuad[2] = new Point(bgr.cols()-1, bgr.rows()-1);
            dstQuad[3] = new Point( 0, bgr.rows() - 1 );

        }

        //srcQuad and destQuad to MatOfPoint2f objects, needed in perspective transform
        MatOfPoint2f srcMat2f = new MatOfPoint2f(srcQuad);
        MatOfPoint2f dstMat2f = new MatOfPoint2f(dstQuad);

        //make a destination mat for a warp
        Mat warp_dst = Mat.zeros(bgr.rows(), bgr.cols(), bgr.type());

        //get a perspective transform matrix
        Mat warp_mat = Imgproc.getPerspectiveTransform(srcMat2f, dstMat2f);

        //do the warp
        Imgproc.warpPerspective(bgr, warp_dst,warp_mat, warp_dst.size());

        //dst width and height taken from the position of the finder patterns
        double dstWidth = srcList.get(2).y - srcList.get(0).y;
        double dstHeight = srcList.get(1).x - srcList.get(0).x;
        Size dstSize = new Size(dstWidth, dstHeight);

        if(warp_dst!=null) {
            if(dstHeight > 0 && dstWidth > 0)
                Imgproc.resize(warp_dst, warp_dst, dstSize);
        }

        return warp_dst;
    }

    public static Mat detectStrip(Mat striparea, StripTest.Brand brand, double ratioW, double ratioH)
    {

        Mat lab = new Mat();
        List<Mat> channels = new ArrayList<>();

        Imgproc.cvtColor(striparea, lab, Imgproc.COLOR_BGR2Lab, 0);

        Imgproc.medianBlur(lab, lab, 11);

        Mat temp = lab.clone();

        List<Double> maxVals = new ArrayList<>();
        for(int c=0;c<lab.cols()-4;c+=4)
        {
            Mat submat = lab.submat(0, lab.rows(), c, c+4).clone();

            Core.split(submat, channels);
            Core.MinMaxLocResult result = Core.minMaxLoc(channels.get(0));
            if(result.maxVal>50)
                maxVals.add(result.maxVal);
        }
        Collections.sort(maxVals);

//        System.out.println("***lowest maxVal: " + maxVals.get(0));

//        System.out.println("***start submat");
        for(int c=0; c<lab.cols()-4; c+=4) {
            Mat submat = lab.submat(0, lab.rows(), c, c+4).clone();

            Core.split(submat, channels);
            Core.MinMaxLocResult result = Core.minMaxLoc(channels.get(0));

//            Scalar mean = Core.mean(submat);
            double tresholdMax = maxVals.get(0);
            double tresholdMin = result.minVal * 1.25;

//            System.out.println("***result L detect strip min val: " + result.minVal + " max val : " + result.maxVal
//                    + " mean: " + mean.val[0] + " treshold: " + treshold);

            Mat upper = new Mat();
            Mat lower = new Mat();
            Imgproc.threshold(channels.get(0), upper, tresholdMax, 255, Imgproc.THRESH_BINARY);
            Imgproc.threshold(channels.get(0), lower, tresholdMin, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_and(upper,lower, channels.get(0));
            Core.merge(channels, submat);

            for(int sc=0;sc<submat.cols();sc++) {
                for (int sr = 0; sr < submat.rows(); sr++) {

                    double[] vals = submat.get(sr, sc);
                    temp.put(sr, c+sc, vals);

                }
            }
            submat.release();
        }
//        System.out.println("***end submat");

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint innermostContours = new MatOfPoint();
        MatOfInt4 mContours = new MatOfInt4();
        MatOfPoint2f mMOP2f = new MatOfPoint2f();
        RotatedRect rotatedRect;

        Core.split(temp, channels);

        //need to make binary image for findContours
        Imgproc.threshold(channels.get(0), channels.get(0), 254, 255, Imgproc.THRESH_BINARY);
//        Core.inRange(lab, new Scalar(254, 0, 0), new Scalar(256, 255, 255), lab);

        Imgproc.findContours(channels.get(0), contours, mContours, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        double maxParent = -Double.MAX_VALUE;

        //Sort on areasize, so the largest contour is the last one in the arraylist
        Collections.sort(contours, new ContoursComparator());

        for (int x = 0; x < contours.size(); x++) {

            double areasize = Imgproc.contourArea(contours.get(x));

            System.out.println("***areasize contour strip: " + areasize);

//            Imgproc.drawContours(striparea, contours, x, new Scalar(255, 2, 255, 255), 1);

            if(mContours.get(0,x)[3] >= 0)//has parent, inner (hole) contour of a closed edge
            {
                if(areasize > 1000)
                {

                    if(mContours.get(0,x)[3] > maxParent)
                    {
                        contours.get(x).copyTo(innermostContours);

                        maxParent = mContours.get(0, x)[3];
                    }
                }

//                Imgproc.drawContours(striparea, contours, x, new Scalar(0, 255, 0, 255), 2);

            }
            else if(areasize > 1000)
            {
                //could not find innermost, take the largest
                contours.get(x).copyTo(innermostContours);
//                Imgproc.drawContours(striparea, contours, x, new Scalar(0, 255, 255, 255), 2);
            }
        }

        List<Point> innermostList = innermostContours.toList();
        if(innermostList.size()>0) {

            //only needed for logging
            Point point1 = new Point(OpenCVUtils.getMinX(innermostList), OpenCVUtils.getMinY(innermostList));
            Point point2 = new Point(getMaxX(innermostList), getMinY(innermostList));
            Point point3 = new Point(OpenCVUtils.getMaxX(innermostList), OpenCVUtils.getMaxY(innermostList));

//            System.out.println("*** innermostList 0 : " + innermostList.get(0).x + "," + innermostList.get(0).y);
//            System.out.println("*** innermostList topleft :" + point1.x + "," + point1.y);
//            System.out.println("*** innermostList topright :" + point2.x + "," + point2.y);
//            System.out.println("*** innermostList bottomleft :" + point3.x + "," + point3.y);

            //demo
//            Imgproc.drawContours(striparea, contours, (int) maxParent + 1, new Scalar(0, 0, 255, 255), 2);

            // we need the mMOP2f object for our rotated Rect
            innermostContours.convertTo(mMOP2f, CvType.CV_32FC2);
            rotatedRect = Imgproc.minAreaRect(mMOP2f);
            if(rotatedRect!=null) {

                //only needed for demo
//                Point[] rotPoints = new Point[4];
//                rotatedRect.points(rotPoints);
//                Core.line(lab, rotPoints[0], rotPoints[1], new Scalar(0, 0, 255, 255), 2);
//                Core.line(lab, rotPoints[1], rotPoints[2], new Scalar(0,0,255,255), 2);
//                Core.line(lab, rotPoints[2], rotPoints[3], new Scalar(0,0,255,255), 2);
//                Core.line(lab, rotPoints[3], rotPoints[0], new Scalar(0,0,255,255), 2);

                //make sure detected area is not smaller than known strip size of brand

                Size brandSize = new Size(brand.getStripLenght()*ratioW, brand.getStripHeight()*ratioH);

                Mat rotated = rotateImage(striparea, rotatedRect, brandSize);
//                System.out.println("***rotated mat: " + CvType.typeToString(rotated.type())
//                        + " size: " + rotated.size().toString() + " width: " + rotated.size().width
//                        + " height: " + rotated.size().height);

                Imgproc.rectangle(striparea, point1, point3, new Scalar(255, 0, 0, 255), 1);

                point1 = new Point(point3.x - rotated.width(), point3.y - rotated.height());

                Imgproc.rectangle(striparea, point1, point3, new Scalar(0, 255, 0, 255), 1);

                return rotated;
            }
        }

        return null;
    }

    public static ColorDetected detectStripColorBrandKnown(Mat submat)
    {

        Scalar mean = Core.mean(submat);

        ColorDetected colorDetected = new ColorDetected(0);
        colorDetected.setRgb(mean);

        int color = Color.rgb((int)Math.round(mean.val[0]),(int)Math.round(mean.val[1]),
                (int)Math.round(mean.val[2]));

        colorDetected.setColor(color);

        Mat lab = new Mat();
        Imgproc.cvtColor(submat, lab, Imgproc.COLOR_RGB2Lab);
        mean = Core.mean(lab);
        colorDetected.setLab(mean);
        submat.release();

        return colorDetected;
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
