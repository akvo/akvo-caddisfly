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
 */
public class MyPreviewCallback implements Camera.PreviewCallback {

    private final int messageRepeat = 0;
    private FinderPatternFinder finderPatternFinder;
    private List<FinderPattern> possibleCenters;
    private int finderPatternColor;
    private int versionNumber = CalibrationCard.CODE_NOT_FOUND;
    private CameraViewListener listener;
    private Camera camera;
    private Camera.Size previewSize;
//    private boolean focused = true;
    LinkedList<Double> lumTrack = new LinkedList<>();
    LinkedList<Double> shadowTrack = new LinkedList<>();

    public static MyPreviewCallback getInstance(Context context) {

        return new MyPreviewCallback(context);
    }

    private MyPreviewCallback(Context context) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement cameraviewListener");
        }

        finderPatternColor = Color.parseColor("#f02cb673"); //same as res/values/colors/springgreen

        possibleCenters = new ArrayList<>();

    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        this.camera = camera;
        previewSize = camera.getParameters().getPreviewSize();

        //focused = false;

        new SendDataTask().execute(data);

    }

    private class SendDataTask extends AsyncTask<byte[], Void, Void> {

        byte[] data;
        FinderPatternInfo info;

        boolean qualityOK = true;
        public SendDataTask()
        {

        }

        @Override
        protected Void doInBackground(byte[]... params) {

            data = params[0];
            try {

                info = findPossibleCenters(data, previewSize);

                setFocusAreas(info);

                // final check if quality of image is ok, if not, abort
                qualityOK = qualityChecks(data, info);

                listener.setStartButtonVisibility(qualityOK);

                if (info!=null && possibleCenters != null && possibleCenters.size() == 4)
                {
                    long timePictureTaken = System.currentTimeMillis();

                    if(listener.start()) // start is set to true if it is time for the next patch
                    {

                        if (qualityOK)
                        {
                            camera.stopPreview();
                            listener.playSound();

                            listener.sendData(data, timePictureTaken, info);

                        }
                        else
                        {
                            if (listener != null)
                                listener.getMessage(messageRepeat);
                        }
                    }
                    else
                    {
                        if (listener != null)
                            listener.getMessage(messageRepeat);
                    }

                }
                else
                {
                    if (listener != null)
                        listener.getMessage(messageRepeat);

                }
            }
            catch (Exception e) {
                e.printStackTrace();

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //do nothing
        }
    }

    private boolean qualityChecks(byte[] data, FinderPatternInfo info) {

        if(camera==null)
            return false;

        Mat bgr = null;
        List<Double> focusList = new ArrayList<>();
        List<double[]> lumList = new ArrayList<>();
        boolean luminosityQualOk = false;
        boolean shadowQualOk = false;
        boolean levelQualOk = false;

        try {
            if (possibleCenters != null && possibleCenters.size() > 0) {
                bgr = new Mat(previewSize.height, previewSize.width, CvType.CV_8UC3);

                //convert preview data to Mat object
                Mat convert_mYuv = new Mat(previewSize.height + previewSize.height / 2, previewSize.width, CvType.CV_8UC1);
                convert_mYuv.put(0, 0, data);
                Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, bgr.channels());

                Mat src_gray = new Mat();
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
                    addFocusQualToList(src_gray, focusList);
                }
            }
            else {
                //if no finder patterns are found, remove one from track
                //when device e.g. is put down, slowly the value becomes zero
                //'slowly' being about a second
                if (lumTrack.size() > 0) {
                    lumTrack.removeFirst();
                }
                if (shadowTrack.size() > 0){
                    shadowTrack.removeFirst();
                }
            }

            //brightness: do the checks
            double maxmaxLum = luminosityCheck(lumList);
            luminosityQualOk = maxmaxLum > Constant.MAX_LUM_LOWER && maxmaxLum < Constant.MAX_LUM_UPPER;

            //System.out.println("***lumTrack size: " + lumTrack.size());

            if(lumTrack.size()<1) {
                //-1 means 'no data'
                listener.showMaxLuminosity(false, -1);
            }
            else
            {
                listener.showMaxLuminosity(luminosityQualOk, lumTrack.getLast());
            }

            // DETECT SHADOWS
            double shadowPercentage = detectShadows(info, bgr);
            shadowQualOk = shadowPercentage < Constant.MAX_SHADOW_PERCENTAGE;
            if(shadowTrack.size()<1) {
                //101 means 'no data'
                listener.showShadow(101);
            }else {
                listener.showShadow(shadowTrack.getLast());
            }

            // focus: do the checks
            // if focus is too low, do another round of focussing
//            if(focusList.size() > 0) {
//                Collections.sort(focusList);
//                if (focusList.get(0) < Constant.MIN_FOCUS_PERCENTAGE) {
//
//                    focused = false;
//
//                } else {
//
//                    focused = true;
//
//                }
//            }


            //GET ANGLE
            if(info!=null) {
                float[] angles = PreviewUtils.getAngle(info);

                listener.showLevel(angles);
                //the sum of the angles should approach zero: then the camera is hold even with the card
                levelQualOk = Math.abs(angles[0]) + Math.abs(angles[1]) < Constant.MAX_LEVEL_DIFF;
            }

        }  catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(bgr!=null)
                bgr.release();
        }

        //count results only if checks have taken place
        if(info!=null && possibleCenters!=null && possibleCenters.size()>0) {
            //System.out.println("start button: " + focused + " " +  luminosityQualOk + "  " + shadowQualOk);
            listener.setCountQualityCheckResult(luminosityQualOk && shadowQualOk && levelQualOk? 1 : 0);
        }

        return luminosityQualOk && shadowQualOk && levelQualOk;

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
                    shadowPercentage = PreviewUtils.getShadowPercentage(warp, versionNumber);
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

            //System.out.println("***focusQual =  " + focusQual );

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

        double maxminLum = -1; //highest value of 'black'
        double minmaxLum = 256; //lowest value of 'white'
        double maxmaxLum = -1; //highest value of 'white'
        boolean luminosityQualOk = false;

        for(int i=0; i<lumList.size();i++) {
            //store lum min value that corresponds with highest; we use it to check over-exposure
            if (lumList.get(i)[0] > maxminLum) {
                maxminLum = lumList.get(i)[0];
            }
            //store lum max value that corresponds with lowest; we use it to check under-exposure
            if (lumList.get(i)[1] < minmaxLum) {
                minmaxLum = lumList.get(i)[1];
            }

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

            //compensate for under-exposure
            //if max values lower than 150
            if(maxmaxLum < Constant.MAX_LUM_LOWER)
            {
                //enlarge
                listener.adjustExposureCompensation(1);

                System.out.println("***under exposed. ");
            }
            //compensate for over-exposure
            //if max values larger than 200
            if(maxmaxLum > Constant.MAX_LUM_UPPER)
            {
                System.out.println("***over exposed. ");
                //Change direction in which to compensate
                listener.adjustExposureCompensation(-1);
            }
            //compare latest value with the previous one
//            else if(lumTrack.size()>1)
//            {
//                if(lumTrack.getLast() > lumTrack.get(lumTrack.size()-2)) {
//
//                    //difference is increasing; this is good, keep going in the same direction
//                    listener.adjustExposureCompensation(1);
//                }
//                else if(lumTrack.getLast() > Constant.MAX_LUM_LOWER)
//                {
//                    //optimum situation reached: remove last value to keep ideal situation
//                    if(lumTrack.size()>2)
//                        lumTrack.removeLast();
//
//                    //System.out.println("***optimum exposure reached. " + camera.getParameters().getExposureCompensation());
//
//                }
//            }

        }

        return maxmaxLum;
    }

    public FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) {

        FinderPatternInfo info = null;
        PlanarYUVLuminanceSource myYUV = new PlanarYUVLuminanceSource(data, size.width,
                size.height, 0, 0,
                (int) Math.round(size.height * Constant.CROP_CAMERAVIEW_FACTOR),
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
                //System.out.println("***possible centers size: " + possibleCenters.size());
                if (possibleCenters != null && previewSize != null) {

                    listener.showFinderPatterns(possibleCenters, previewSize, finderPatternColor);

                    //get the version number from the barcode printed on the card
                    try {
                        if (possibleCenters.size() == 4) {
                            versionNumber = CalibrationCard.decodeCallibrationCardCode(possibleCenters, bitMatrix);
//                            System.out.println("***versionNumber: " + versionNumber);
                            CalibrationCard.addVersionNumber(versionNumber);

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

//    private byte[] compressToJpeg(byte[] data)
//    {
//
//        if(previewFormat == ImageFormat.NV21) {
//            YuvImage yuvImage = new YuvImage(data, previewFormat, previewSize.width, previewSize.height, null);
//            Rect rect = new Rect(0, 0, previewSize.width, previewSize.height);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            yuvImage.compressToJpeg(rect, 100, baos);
//            return baos.toByteArray();
//        }
//
//        return null;
//    }


//    private void takePicture()
//    {
//        camera.takePicture(null, new Camera.PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] data, Camera camera) {
//                        if (data != null) {
//                            System.out.println("***raw: " + data.length);
//                            if(info!=null) {
//                                listener.sendData(data, camera.getParameters().getPictureFormat(),
//                                        camera.getParameters().getPictureSize().width,
//                                        camera.getParameters().getPictureSize().height, info);
//                            }
//                        }
//                        else
//                        {
//                            System.out.println("***raw is null");
//                        }
//                    }
//
//                },
//                null,
//                new Camera.PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] data, Camera camera) {
//                        if(data!=null)
//                        {
//                            System.out.println("***jpeg: " + data.length);
//                            if(info!=null) {
//                                listener.sendData(data, camera.getParameters().getPictureFormat(),
//                                        camera.getParameters().getPictureSize().width,
//                                        camera.getParameters().getPictureSize().height, info);
//
//                            }
//                        }
//                        else
//                        {
//                            System.out.println("***jpeg is null");
//                        }
//                    }
//                });
//
//    }


}




