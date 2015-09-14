package org.akvo.akvoqr.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 7/4/15.
 * http://www.answers.opencv.org/question/63470/how-to-detect-and-remove-shadow-of-a-object/
 */
public class ShadowDetector {

    public static Mat ShadowDetection(Mat bgr)
    {
        Mat imageShadow = bgr.clone();

        int iW = (int)Math.round(bgr.size().width);
        int iH = (int)Math.round(bgr.size().height);

        double[] data;
        int channel = imageShadow.channels();
        float H = 0;
        float S;
        float V;

        for(int i=5; i< iH-5; i++) //
        {
            for(int j=5; j< iW-5; j++)
            {
                data = imageShadow.get(i,j);

                int B = (int)data[0];
                int G = (int)data[1];
                int R = (int)data[2];
                //Convert RGB to HSV
                float var_R = ( R / 255.0f ) ;                    //RGB from 0 to 255
                float var_G = ( G / 255.0f );
                float var_B = ( B / 255.0f );

                float var_Min = Math.min(Math.min(var_R, var_G), var_B )  ;  //Min. value of RGB
                float   var_Max = Math.max( Math.max(var_R, var_G), var_B )  ;  //Max. value of RGB
                float   del_Max = var_Max - var_Min      ;       //Delta RGB value

                V = var_Max;

                if ( del_Max == 0 )                     //This is a gray, no chroma...
                {
                    H = 0;                              //HSV results from 0 to 1
                    S = 0;
                }
                else                                    //Chromatic data...
                {
                    S = del_Max / var_Max;

                    float del_R = ( ( ( var_Max - var_R ) / 6 ) + ( del_Max / 2 ) ) / del_Max;
                    float del_G = ( ( ( var_Max - var_G ) / 6 ) + ( del_Max / 2 ) ) / del_Max;
                    float del_B = ( ( ( var_Max - var_B ) / 6 ) + ( del_Max / 2 ) ) / del_Max;

                    if      ( var_R == var_Max ) H = del_B - del_G;
                    else if ( var_G == var_Max ) H = ( 1 / 3 ) + del_R - del_B;
                    else if ( var_B == var_Max ) H = ( 2 / 3 ) + del_G - del_R;

                    if ( H < 0 ) H += 1;
                    if ( H > 1 ) H -= 1;
                }

                //if(V>0.3 && V<0.85 && H<85 && S<0.15)
                //if(V>0.5 && V<0.95 &&  S<0.2)
                if(V>0.3 && V<0.95 &&  S<0.2)
                {
                    data[0] = 0;// dataTmp[channel*(i*iW+j)];
                    data[1] = 0;// dataTmp[channel*(i*iW+j)+1];
                    data[2] = 0;// dataTmp[channel*(i*iW+j)+2];
                    imageShadow.put(i, j, data);
                }
                else
                {
                    data[0] = 255;
                    data[1] = 255;
                    data[2] = 255;
                    imageShadow.put(i, j, data);
                }
            }
        }

        //Find big area of shadow
        Mat imageGray = new Mat();
        Imgproc.cvtColor(imageShadow, imageGray, Imgproc.COLOR_RGB2GRAY);

        int dilation_size =2;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
                new Size(2 * dilation_size + 1, 2 * dilation_size + 1),
                new Point(dilation_size, dilation_size));
        /// Apply the dilation operation to remove small areas
        Imgproc.dilate( imageGray, imageGray, element );

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        /// Find contours
        MatOfInt4 hierarchy = new MatOfInt4();
        // Imgproc.findContours(mHSVMat, contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.findContours(imageGray, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // vector<vector<Point> > contoursResult;
        MatOfPoint contoursResult = new MatOfPoint();
        Scalar colorWhite = new Scalar(255, 255, 255, 255);

        for( int m = 0; m < contours.size(); m++ )
        {
            double area = Imgproc.contourArea(contours.get(m));

            System.out.println("***area: " + area + "  iW*iH/10: "+ (iW*iH/10));
            if(area>400 && area < iW*iH)
            {
                contoursResult.push_back(contours.get(m));

            }


            Imgproc.drawContours(bgr, contours, m, colorWhite, 1);

        }
//        Core.subtract(bgr, contoursResult, bgr);

        return bgr;
    }

    public static Mat test(Mat image)
    {
        Mat imageGray = new Mat();
//        Mat mHSVMat = new Mat();
        Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.cvtColor(image, mHSVMat, Imgproc.COLOR_RGB2HSV, 3);

        Imgproc.threshold(imageGray, imageGray, 175, 255, Imgproc.THRESH_BINARY);
//        Core.inRange(imageGray, new Scalar(200, 255, 255), new Scalar(255, 255, 255), imageGray);

//        int dilation_size =2;
//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
//                new Size(2 * dilation_size + 1, 2 * dilation_size + 1),
//                new Point(dilation_size, dilation_size));
//        /// Apply the dilation operation to remove small areas
//         Imgproc.dilate( imageGray, imageGray, element );

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imageGray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Scalar colorRed = new Scalar(255, 0, 0, 255);

        Mat result = image.clone();
        for( int m = 0; m < contours.size(); m++ ) {

            double area = Imgproc.contourArea(contours.get(m));

            if(area>400 && area < image.size().width*image.size().height)
            {

                //if (contours.get(m).rows() == 4) {
                Imgproc.drawContours(image, contours, m, colorRed, 2);
                //}

            }
        }

        return result;
    }

    /*
        http://www.ijcsi.org/papers/IJCSI-10-4-2-270-273.pdf
        To detect shadow first a RGB image has to be converted to
    a LAB image. Then the  mean values of the pixels in L, A
    and B planes of the image have to be computed separately.
    Now if mean (A) + mean (B) ≤ 256, then the pixels with a
    value in L ≤ (mean (L) –
     standard deviation (L)/3) can be
    classified  as  shadow  pixels  and  others  as  non-shadow
    pixels.  Otherwise  the  pixels  with  lower  values  in  both  L
    and B planes can be classified as shadow pixels and others
    as non-shadow pixels [16].
    */
    public static void detectShadows(Mat bgr)
    {
        Mat Lab = new Mat();
        List<Mat> channels = new ArrayList<>();

        Imgproc.cvtColor(bgr, Lab, Imgproc.COLOR_RGB2Lab);
        Core.split(Lab, channels);

        Scalar meanL = Core.mean(channels.get(0));
        Scalar meanA = Core.mean(channels.get(1));
        Scalar meanB = Core.mean(channels.get(2));
        MatOfDouble stddev = new MatOfDouble();
        MatOfDouble meanLoutput = new MatOfDouble();

        Core.meanStdDev(channels.get(0), meanLoutput, stddev);

        double treshold;
        Mat dst = new Mat();
        MatOfPoint2f mMOP2f = new MatOfPoint2f();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        if((meanA.val[0] - 128) + (meanB.val[0] -128) < 256) {
            treshold = meanLoutput.get(0, 0)[0] - stddev.get(0, 0)[0] / 3;
            System.out.println("***SHADOW treshold: " + treshold);

            Imgproc.threshold(channels.get(0), channels.get(0), treshold, 255, Imgproc.THRESH_BINARY);

            //Mat black = new Mat();
            //Core.inRange(channels.get(0), new Scalar(250), new Scalar(255), black);

            Imgproc.findContours(channels.get(0), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            Scalar colorRed = new Scalar(255, 0, 0, 255);

            for( int m = 0; m < contours.size(); m++ ) {

                //make square
                contours.get(m).convertTo(mMOP2f, CvType.CV_32FC2);
                Imgproc.approxPolyDP(mMOP2f, mMOP2f, 0.01 * Imgproc.arcLength(mMOP2f, true), true);
                mMOP2f.convertTo(contours.get(m), CvType.CV_32S);

                double area = Imgproc.contourArea(contours.get(m));
                System.out.println("***SHADOW area: " + area);

                if(area>400 && area < bgr.size().width*bgr.size().height)
                {
                    Imgproc.drawContours(bgr, contours, m, colorRed, 2);

                }
            }
        }
        else {
            double tresholdL = 200;
            double tresholdB = 128;

            System.out.println("***tresholdL: " + tresholdL);

            Imgproc.threshold(channels.get(0), channels.get(0), tresholdL, 255, Imgproc.THRESH_BINARY);
            Imgproc.threshold(channels.get(2), channels.get(2), tresholdB, 255, Imgproc.THRESH_BINARY);

            Core.merge(channels, dst);

            Mat gray = new Mat();
            Imgproc.cvtColor(dst, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(gray, gray, 254, 255, Imgproc.THRESH_BINARY);

            Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            Scalar colorRed = new Scalar(0, 0, 255, 255);

            for (int m = 0; m < contours.size(); m++) {

                double area = Imgproc.contourArea(contours.get(m));

                if (area > 400 && area < bgr.size().width * bgr.size().height) {
                    Imgproc.drawContours(bgr, contours, m, colorRed, 2);

                }
            }
        }
    }
}
