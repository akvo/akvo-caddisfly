package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.calibration.CalibrationData;
import org.akvo.akvoqr.detector.BinaryBitmap;
import org.akvo.akvoqr.detector.BitMatrix;
import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternFinder;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.detector.HybridBinarizer;
import org.akvo.akvoqr.detector.NotFoundException;
import org.akvo.akvoqr.detector.PlanarYUVLuminanceSource;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.PreviewUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by linda on 12/17/15.
 */
public abstract class CameraPreviewCallbackAbstract implements Camera.PreviewCallback
{
    //private int count;
    protected List<FinderPattern> possibleCenters;
    protected int finderPatternColor;
    protected CameraViewListener listener;
    protected CalibrationData caldata;
    protected Camera.Size previewSize;
    protected final LinkedList<Double> lumTrack = new LinkedList<>();
    protected final LinkedList<Double> shadowTrack = new LinkedList<>();
    protected Context context;
    protected float EV;
    private Camera.Parameters parameters;

    public CameraPreviewCallbackAbstract(Context context, Camera.Parameters parameters) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement cameraviewListener");
        }

        this.context = context;

        finderPatternColor = Color.parseColor("#f02cb673"); //same as res/values/colors/springgreen

        possibleCenters = new ArrayList<>();

        this.parameters = parameters;

        previewSize = parameters.getPreviewSize();
    }

    public void setStop(boolean stop) {}

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if(camera==null)
            return;

        //get EV to use in order to avoid over exposure while trying to optimise brightness
        float step = parameters.getExposureCompensationStep();
        //make sure it never becomes zero
        EV = Math.max(step, step * parameters.getExposureCompensation());

    }

    protected void sendData(byte[] data){};

    final int[] qualityChecksArray = new int[]{0,0,0};//array containing brightness, shadow, level check values
    Mat bgr = null;
    Mat convert_mYuv = null;
    Mat src_gray = new Mat();
    final List<double[]> lumList = new ArrayList<>();
    double minX;
    double minY;
    double maxX;
    double maxY;
    double esModSize;

    protected int[] qualityChecks(byte[] data, FinderPatternInfo info) {

        lumList.clear();
        float[] angles = null;
        int lumVal = 0;
        int shadVal = 0;
        int levVal = 0;

        try {

            if (possibleCenters != null && possibleCenters.size() > 3) {

                bgr = new Mat(previewSize.height, previewSize.width, CvType.CV_8UC3);

                //convert preview data to Mat object
                convert_mYuv = new Mat(previewSize.height + previewSize.height / 2, previewSize.width, CvType.CV_8UC1);
                convert_mYuv.put(0, 0, data);
                Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, bgr.channels());

                for (int i = 0; i < possibleCenters.size(); i++) {
                     esModSize = possibleCenters.get(i).getEstimatedModuleSize();

                    // find top left and bottom right coordinates of finder pattern
                    minX = Math.max(possibleCenters.get(i).getX() - 4 * esModSize, 0);
                    minY = Math.max(possibleCenters.get(i).getY() - 4 * esModSize, 0);
                    maxX = Math.min(possibleCenters.get(i).getX() + 4 * esModSize, bgr.width());
                    maxY = Math.min(possibleCenters.get(i).getY() + 4 * esModSize, bgr.height());
                    Point topLeft = new Point(minX, minY);
                    Point bottomRight = new Point(maxX, maxY);

                    // make grayscale submat of finder pattern
                    org.opencv.core.Rect roi = new org.opencv.core.Rect(topLeft, bottomRight);

                    Imgproc.cvtColor(bgr.submat(roi), src_gray, Imgproc.COLOR_BGR2GRAY);

                    //brightness: add lum. values to list
                    addLumToList(src_gray, lumList);

                    //focus: add values to list
                    //addFocusQualToList(src_gray, focusList);
                }
            }
            else {
                //if no finder patterns are found, remove one from track
                //when device e.g. is put down, slowly the value becomes zero
                //'slowly' being about a second
                if(possibleCenters.size()==0) {
                    if (lumTrack.size() > 0) {
                        lumTrack.removeFirst();
                    }
                    if (shadowTrack.size() > 0) {
                        shadowTrack.removeFirst();
                    }
                }
            }

            if(info!=null) {

                //DETECT BRIGHTNESS
                double maxmaxLum = luminosityCheck(lumList);
                lumVal = maxmaxLum > Constant.MAX_LUM_LOWER && maxmaxLum < Constant.MAX_LUM_UPPER ? 1 : 0;

                // DETECT SHADOWS
                if(bgr!=null) {
                    double shadowPercentage = detectShadows(info, bgr);
                    shadVal = shadowPercentage < Constant.MAX_SHADOW_PERCENTAGE ? 1 : 0;
                }

                //GET ANGLE
                angles = PreviewUtils.getAngle(info);
                //the sum of the angles should approach zero: then the camera is hold even with the card
                // System.out.println("***Angles: 0 = " + angles[0] + " 1 = " + angles[1]);
                levVal = Math.abs(angles[0]) + Math.abs(angles[1]) < 2*Constant.MAX_LEVEL_DIFF ? 1 : 0;

            }

            //UPDATE VALUES IN ARRAY
            qualityChecksArray[0] = lumVal;
            qualityChecksArray[1] = shadVal;
            qualityChecksArray[2] = levVal;

            //SHOW VALUES ON SCREEN
            if(listener!=null) {
                //brightness: show the values on device
                if (lumTrack.size() < 1) {
                    //-1 means 'no data'
                    listener.showBrightness(-1);
                } else {
                    listener.showBrightness(lumTrack.getLast());
                }

                //shadows: show the values on device
                if (shadowTrack.size() < 1) {
                    //101 means 'no data'
                    listener.showShadow(101);
                } else {
                    listener.showShadow(shadowTrack.getLast());
                }

                //level: show on device
                listener.showLevel(angles);
            }

        }  catch (Exception e) {
            // throw new RuntimeException(e);
            e.printStackTrace();

        } finally {
            if(bgr!=null) {
                bgr.release();
                bgr = null;
            }

            if(src_gray!=null)
                src_gray.release();

            if(convert_mYuv!=null)
                convert_mYuv.release();
        }

        return qualityChecksArray;

    }


    private double detectShadows(FinderPatternInfo info, Mat bgr) throws Exception
    {
        double shadowPercentage = 101;

        if(bgr==null)
            return shadowPercentage;

        //fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
        if(shadowTrack.size()>25) {
            shadowTrack.removeFirst();
        }

        if(info != null) {
            double[] tl = new double[]{info.getTopLeft().getX(), info.getTopLeft().getY()};
            double[] tr = new double[]{info.getTopRight().getX(), info.getTopRight().getY()};
            double[] bl = new double[]{info.getBottomLeft().getX(), info.getBottomLeft().getY()};
            double[] br = new double[]{info.getBottomRight().getX(), info.getBottomRight().getY()};
            bgr = OpenCVUtils.perspectiveTransform(tl, tr, bl, br, bgr).clone();

            try
            {
                //if(versionNumber!=CalibrationCard.CODE_NOT_FOUND) {
                if(caldata!=null){
                    shadowPercentage = PreviewUtils.getShadowPercentage(bgr, caldata);
                    shadowTrack.add(shadowPercentage);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                if(bgr!=null)
                    bgr.release();
            }
        }

        return shadowPercentage;

    }

//    private void addFocusQualToList(Mat src_gray, List<Double> focusList)
//    {
//        double laplacian;
//        double focusQual;
//
//        laplacian = PreviewUtils.focusLaplacian1(src_gray);
//        // correct the focus quality parameter for the total luminosity range of the finder pattern
//        // the factor of 0.5 means that 100% corresponds to the pattern going from black to white within 2 pixels
//        double[] lumMinMax = PreviewUtils.getDiffLuminosity(src_gray);
//
//        if(lumMinMax.length==2) {
//            focusQual = (100 * (laplacian / (0.5d * (lumMinMax[1] - lumMinMax[0]))));
//
//            //never more than 100%
//            focusQual = Math.min(focusQual,100);
//
//            if(focusQual!=Double.NaN && focusQual!=Double.POSITIVE_INFINITY && focusQual!=Double.NEGATIVE_INFINITY) {
//                try {
//                    focusList.add(Double.valueOf(focusQual));
//                } catch (Exception e) {
//                    System.out.println("***exception adding to focuslist.");
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private void addLumToList(Mat src_gray, List<double[]> lumList)
    {
        double[] lumMinMax;

        lumMinMax = PreviewUtils.getDiffLuminosity(src_gray);
        if(lumMinMax.length == 2) {

            lumList.add(lumMinMax);
        }
    }

    private double luminosityCheck(List<double[]> lumList)
    {

        double maxmaxLum = -1; //highest value of 'white'

        for(int i=0; i<lumList.size();i++) {

            //store lum max value that corresponds with highest: we use it to check over- and under exposure
            if (lumList.get(i)[1] > maxmaxLum) {
                maxmaxLum = lumList.get(i)[1];
            }
        }
        //fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
        if(lumTrack.size()>25) {
            lumTrack.removeFirst();
        }

        if(lumList.size() > 0) {

            //add highest value of 'white' to track list
            lumTrack.addLast(100 * maxmaxLum/255);

            //System.out.println("***exp maxmaxLum: " + maxmaxLum);

            //compensate for under-exposure
            //if max values lower than 150
            if(maxmaxLum < Constant.MAX_LUM_LOWER)
            {
                //enlarge
                listener.adjustExposureCompensation(1);

                //System.out.println("***under exposed. " + count);
            }

            //compensate for over-exposure
            //if max values larger than 240
            if(maxmaxLum > Constant.MAX_LUM_UPPER)
            {
                //System.out.println("***over exposed. " + count);
                //Change direction in which to compensate
                listener.adjustExposureCompensation(-1);
            }
            else
            {

                //System.out.println("***exp EV = " + EV + " EV * 255 = " + EV * 255 + "  " + maxmaxLum + EV * 255);

                //we want to get it as bright as possible but without risking overexposure
                // we assume that EV will be a factor that determines the amount with which brightness will increase
                // after adjusting exp. comp.
                // we do not want to increase exp. comp. if the current brightness plus the max. brightness time the EV
                // becomes larger that the UPPER limit
                if(maxmaxLum + EV * 255 < Constant.MAX_LUM_UPPER) {

                    //luminosity is increasing; this is good, keep going in the same direction
//                    System.out.println("***increasing exposure."  + count);
//                    System.out.println("***"  + count + " maxmaxLum: " + maxmaxLum + " EV * 255: " + (EV*255) + " sum: " + (maxmaxLum + EV*255));

                    listener.adjustExposureCompensation(1);
                }
//                else
//                {
                //optimum situation reached

                // System.out.println("***optimum exposure reached. " + count + "  exp.comp. = " + camera.getParameters().getExposureCompensation());

//                }
            }

        }

        return maxmaxLum;
    }

    FinderPatternInfo info = null;
    PlanarYUVLuminanceSource myYUV;
    BinaryBitmap binaryBitmap;
    BitMatrix bitMatrix=null;
    FinderPatternFinder finderPatternFinder;

    protected FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) {


        // crop preview image to only contain the known region for the finder patterns
        myYUV = new PlanarYUVLuminanceSource(data, size.width,
                size.height, 0, 0,
                (int) Math.round(size.height * Constant.CROP_FINDERPATTERN_FACTOR),
                size.height,
                false);

        binaryBitmap = new BinaryBitmap(new HybridBinarizer(myYUV));


        try {
            bitMatrix = binaryBitmap.getBlackMatrix();
        } catch (NotFoundException | NullPointerException e) {
            e.printStackTrace();

        }

        if (bitMatrix != null) {
             finderPatternFinder = new FinderPatternFinder(bitMatrix);

            try {

                info = finderPatternFinder.find(null);

            } catch (Exception e) {
                // this only means not all patterns (=4) are detected.
            }
            finally {

                possibleCenters = finderPatternFinder.getPossibleCenters();

                //detect centers that are to small in order to get rid of noise
                for(int i=0;i<possibleCenters.size();i++) {
                    if (possibleCenters.get(i).getEstimatedModuleSize() < 2) {
                        return null;

                    }
                }

                if (possibleCenters != null && previewSize != null) {

                    if(listener!=null) {
                        listener.showFinderPatterns(possibleCenters, previewSize, finderPatternColor);
                    }
                    //get the version number from the barcode printed on the card
                    try {
                        if (possibleCenters.size() == 4) {
                            int versionNumber = CalibrationCard.decodeCallibrationCardCode(possibleCenters, bitMatrix);
//                            System.out.println("***versionNumber: " + versionNumber);
                            if(versionNumber!= CalibrationCard.CODE_NOT_FOUND) {
                                CalibrationCard.addVersionNumber(versionNumber);
                                //if(caldata==null) //less overhead, but what if caldata is for wrong version number?
                                    caldata = CalibrationCard.readCalibrationFile(context);
                            }
                            //testing
//                            Random random = new Random();
//                            CalibrationCard.addVersionNumber(random.nextInt(10));
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }


            }
        }


        return info;
    }

}
