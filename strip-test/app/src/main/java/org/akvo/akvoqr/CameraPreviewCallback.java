package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;

import org.akvo.akvoqr.calibration.CalibrationCard;
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
 * Created by linda on 6/26/15.
 *
 * This class is meant to be called in the setOnShotCameraPreviewCallback(Camera.PreviewCallback)
 * method of a class that holds an instance of the Android Camera.
 *
 * In the AsyncTask called 'SendDataTask', executed in every onPreviewFrame(),
 * this happens:
 * - find the FinderPatterns on the card
 * - do quality checks regarding luminosity, shadows and perspective
 * - communicate the result of finder patterns to the listener (==CameraActivity)
 * - communicate the result of quality checks to the listener
 *
 * Depending on the instance of the Fragment that is inflated by CameraActivity at this instance,
 * the global boolean 'takePicture' is set.
 * Fragment instance of CameraPrepareFragment -> false
 * Fragment instance of CameraStartTestFragment -> true
 *
 * If conditions under which to take a picture (==store Preview data in internal storage) fail,
 * comminicate to the listener that it calls this class again,
 * - if we are in the 'takePicture' Fragment, call it like that
 * - if we are in the 'prepare' Fragment, call it like that
 *
 * The conditions under which to take a picture are:
 * - 'takePicture' must be true
 * - FinderPatternInfo object must not be null
 * - listener.countQualityOK must be true
 *
 * if takePicture is false, we tell the listener to call this callback again
 * with the startNextPreview() method.
 */
public class CameraPreviewCallback implements Camera.PreviewCallback {

    //    private static int countInstance = 0;
    private int count;
    private FinderPatternFinder finderPatternFinder;
    private List<FinderPattern> possibleCenters;
    private int finderPatternColor;
    private int versionNumber = CalibrationCard.CODE_NOT_FOUND;
    private CameraViewListener listener;
    private Camera camera;
    private Camera.Size previewSize;
    private boolean takePicture;
    private LinkedList<Double> lumTrack = new LinkedList<>();
    private LinkedList<Double> shadowTrack = new LinkedList<>();
    private boolean running;
    private boolean stop;
    private Mat src_gray = new Mat();
    private Context context;


    public CameraPreviewCallback(Context context) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement cameraviewListener");
        }

        this.context = context;

        finderPatternColor = Color.parseColor("#f02cb673"); //same as res/values/colors/springgreen

        possibleCenters = new ArrayList<>();

        //countInstance ++;

    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        count ++;

        this.camera = camera;
        previewSize = camera.getParameters().getPreviewSize();

//        System.out.println("***CameraPreviewCallback stop: " + count + " " + stop);
//        System.out.println("***CameraPreviewCallback running: " + count + " " + running);

        if(!stop && !running) {
            new SendDataTask().execute(data);
        }

    }

    public void setTakePicture(boolean takePicture) {
        this.takePicture = takePicture;
    }

    public void setStop(boolean stop)
    {
        this.stop = stop;
    }
    private class SendDataTask extends AsyncTask<byte[], Void, Void> {

        FinderPatternInfo info;

        @Override
        protected Void doInBackground(byte[]... params) {

            running = true;
            byte[] data = params[0];

//            try {

            info = findPossibleCenters(data, previewSize);

            //check if quality of image is ok. if OK, value is 1, if not 0
            //the qualityChecks() method sends messages back to listener to update UI
            int[] countQuality = qualityChecks(data, info);

            //add countQuality to sum in listener
            //if countQuality sums up to the limit set in Constant,
            //listener.qualityChecksOK will return true;
            if(listener!=null)
                listener.addCountToQualityCheckCount(countQuality);

            //logging
//                System.out.println("***CameraPreviewCallback takePicture: " + count + " " + takePicture);
//                System.out.println("***CameraPreviewCallback count quality: " + count + " " + countQuality);
//            System.out.println("***CameraPreviewCallback listener quality checks ok: " + count + " " + listener.qualityChecksOK());
//            System.out.println("***CameraPreviewCallback info: " + count + " " + info);


            if(takePicture)
            {
                //sumQual should amount to 3, if all checks are OK: [1,1,1]
                int sumQual = 0;
                if(countQuality!=null) {
                    for (int i : countQuality) {
                        sumQual += i;
                    }
                }

                if(listener!=null) {
                    if (info != null && sumQual == 3 && listener.qualityChecksOK()) {

                        long timePictureTaken = System.currentTimeMillis();

                        //freeze the screen and play a sound
                        //camera.stopPreview();
                        listener.playSound();

                        //System.out.println("***!!!CameraPreviewCallback takePicture true: " + countInstance);
                        listener.sendData(data, timePictureTaken, info);


                    } else {

                        listener.takeNextPicture(100);
                    }
                }
            }
            else //we are not taking any picture, just looking at the quality checks
            {

                if (listener != null)
                    listener.startNextPreview(0);

            }
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//
//            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            running = false;
        }
    }

    private int[] qualityChecksArray = new int[]{0,0,0};//array containing brightness, shadow, level check values

    private int[] qualityChecks(byte[] data, FinderPatternInfo info) {

        if(camera==null)
            return null;

        Mat bgr = null;
        // List<Double> focusList = new ArrayList<>();
        List<double[]> lumList = new ArrayList<>();
        float[] angles = null;
        int lumVal = 0;
        int shadVal = 0;
        int levVal = 0;

        try {
            if (possibleCenters != null && possibleCenters.size() > 3) {
                bgr = new Mat(previewSize.height, previewSize.width, CvType.CV_8UC3);

                //convert preview data to Mat object
                Mat convert_mYuv = new Mat(previewSize.height + previewSize.height / 2, previewSize.width, CvType.CV_8UC1);
                convert_mYuv.put(0, 0, data);
                Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, bgr.channels());


                for (int i = 0; i < possibleCenters.size(); i++) {
                    double esModSize = possibleCenters.get(i).getEstimatedModuleSize();

                    // find top left and bottom right coordinates of finder pattern
                    double minX = Math.max(possibleCenters.get(i).getX() - 4 * esModSize, 0);
                    double minY = Math.max(possibleCenters.get(i).getY() - 4 * esModSize, 0);
                    double maxX = Math.min(possibleCenters.get(i).getX() + 4 * esModSize, bgr.width());
                    double maxY = Math.min(possibleCenters.get(i).getY() + 4 * esModSize, bgr.height());
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

            //DETECT BRIGHTNESS
            double maxmaxLum = luminosityCheck(lumList);
            lumVal = maxmaxLum > Constant.MAX_LUM_LOWER && maxmaxLum < Constant.MAX_LUM_UPPER ? 1 : 0;

            if(info!=null) {

                // DETECT SHADOWS
                double shadowPercentage = detectShadows(info, bgr);
                //shadowQualOk = shadowPercentage < Constant.MAX_SHADOW_PERCENTAGE;
                shadVal = shadowPercentage < Constant.MAX_SHADOW_PERCENTAGE ? 1 : 0;

                //GET ANGLE
                angles = PreviewUtils.getAngle(info);
                //the sum of the angles should approach zero: then the camera is hold even with the card
                //levelQualOk = Math.abs(angles[0]) + Math.abs(angles[1]) < Constant.MAX_LEVEL_DIFF;
                levVal = Math.abs(angles[0]) + Math.abs(angles[1]) < Constant.MAX_LEVEL_DIFF ? 1 : 0;

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
                    listener.showMaxLuminosity(-1);
                } else {
                    listener.showMaxLuminosity(lumTrack.getLast());
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
            e.printStackTrace();
            return null;
        } finally {
            if(bgr!=null)
                bgr.release();

            if(src_gray!=null)
                src_gray.release();
        }

        return qualityChecksArray;

    }

    private double detectShadows(FinderPatternInfo info, Mat bgr) throws Exception
    {
        double shadowPercentage = 101;

        //fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
        if(shadowTrack.size()>25) {
            shadowTrack.removeFirst();
        }

        if(info != null) {
            double[] tl = new double[]{info.getTopLeft().getX(), info.getTopLeft().getY()};
            double[] tr = new double[]{info.getTopRight().getX(), info.getTopRight().getY()};
            double[] bl = new double[]{info.getBottomLeft().getX(), info.getBottomLeft().getY()};
            double[] br = new double[]{info.getBottomRight().getX(), info.getBottomRight().getY()};
            Mat warp = OpenCVUtils.perspectiveTransform(tl, tr, bl, br, bgr).clone();

            try
            {
                if(versionNumber!=CalibrationCard.CODE_NOT_FOUND) {
                    CalibrationCard card = CalibrationCard.getInstance(context);
                    shadowPercentage = PreviewUtils.getShadowPercentage(warp, card);
                    shadowTrack.add(shadowPercentage);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                if(warp!=null)
                    warp.release();
            }
        }

        return shadowPercentage;

    }

    private void addFocusQualToList(Mat src_gray, List<Double> focusList)
    {
        double laplacian;
        double focusQual;

        laplacian = PreviewUtils.focusLaplacian1(src_gray);
        // correct the focus quality parameter for the total luminosity range of the finder pattern
        // the factor of 0.5 means that 100% corresponds to the pattern going from black to white within 2 pixels
        double[] lumMinMax = PreviewUtils.getDiffLuminosity(src_gray);

        if(lumMinMax.length==2) {
            focusQual = (100 * (laplacian / (0.5d * (lumMinMax[1] - lumMinMax[0]))));

            //never more than 100%
            focusQual = Math.min(focusQual,100);

            if(focusQual!=Double.NaN && focusQual!=Double.POSITIVE_INFINITY && focusQual!=Double.NEGATIVE_INFINITY) {
                try {
                    focusList.add(Double.valueOf(focusQual));
                } catch (Exception e) {
                    System.out.println("***exception adding to focuslist.");
                    e.printStackTrace();
                }
            }
        }
    }

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

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                Camera.Parameters parameters = camera.getParameters();
//                parameters.setAutoExposureLock(true);
//                camera.setParameters(parameters);
//                System.out.println("***locking auto-exposure. ");
//            }


            //System.out.println("***exp maxmaxLum: " + maxmaxLum);

            //compensate for under-exposure
            //if max values lower than 150
            if(maxmaxLum < Constant.MAX_LUM_LOWER)
            {
                //enlarge
                listener.adjustExposureCompensation(1);

                //System.out.println("***under exposed. ");
            }

            //compensate for over-exposure
            //if max values larger than 240
            if(maxmaxLum > Constant.MAX_LUM_UPPER)
            {
                //System.out.println("***over exposed. ");
                //Change direction in which to compensate
                listener.adjustExposureCompensation(-1);
            }
            //compare latest value with the previous one
            else if(lumTrack.size()>1)
            {
                //get EV to use in order to avoid over exposure while trying to optimise brightness
                float step = camera.getParameters().getExposureCompensationStep();
                //make sure it never becomes zero
                float EV = Math.max(step, step * camera.getParameters().getExposureCompensation());

                //System.out.println("***exp EV = " + EV + " EV * 255 = " + EV * 255 + "  " + maxmaxLum + EV * 255);

                //we want to get it as bright as possible but without risking overexposure
                // we assume that EV will be a factor that determines the amount with which brightness will increase
                // after adjusting exp. comp.
                // we do not want to increase exp. comp. if the current brightness plus the max. brightness time the EV
                // becomes larger that the UPPER limit
                if(maxmaxLum + EV * 255 < Constant.MAX_LUM_UPPER) {

                    //luminosity is increasing; this is good, keep going in the same direction
                    // System.out.println("***increasing exposure." );

                    listener.adjustExposureCompensation(1);
                }
//                else
//                {
//                    //optimum situation reached
//
//                    // System.out.println("***optimum exposure reached. " + camera.getParameters().getExposureCompensation());
//
//                }
            }

        }

        return maxmaxLum;
    }

    public FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) {

        FinderPatternInfo info = null;

        // crop preview image to only contain the known region for the finder patterns
        PlanarYUVLuminanceSource myYUV = new PlanarYUVLuminanceSource(data, size.width,
                size.height, 0, 0,
                (int) Math.round(size.height * Constant.CROP_FINDERPATTERN_FACTOR),
                size.height,
                false);

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(myYUV));

        BitMatrix bitMatrix = null;
        try {
            bitMatrix = binaryBitmap.getBlackMatrix();
        } catch (NotFoundException e) {
            e.printStackTrace();

        } catch (NullPointerException e) {
            e.printStackTrace();

        }

        if (bitMatrix != null) {
            finderPatternFinder = new FinderPatternFinder(bitMatrix, null);

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
                            versionNumber = CalibrationCard.decodeCallibrationCardCode(possibleCenters, bitMatrix);
//                            System.out.println("***versionNumber: " + versionNumber);
                            if(versionNumber!= CalibrationCard.CODE_NOT_FOUND) {
                                CalibrationCard.addVersionNumber(versionNumber);
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

                return info;
            }
        }


        return null;
    }

    private void setFocusAreas(FinderPatternInfo info)
    {
        //set focus area to where finder patterns are
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            if(info!=null) {
                List<Camera.Area> areas = new ArrayList<>();

                int ratioW = Math.round(1000f/previewSize.width);
                int ratioH = Math.round(1000f / previewSize.height);

                List<Point> points = PreviewUtils.sortFinderPatternInfo(info);

                Rect focusArea = new Rect(
                        -1000 + ratioW * (int) Math.round(points.get(0).x),
                        -1000 + ratioH * (int) Math.round(points.get(0).y),
                        -1000 + ratioW * (int) Math.round(points.get(3).x),
                        -1000 + ratioH * (int) Math.round(points.get(3).y)
                );

                areas.add(new Camera.Area(focusArea, 1));

                listener.setFocusAreas(areas);
            }
        }
    }
}




