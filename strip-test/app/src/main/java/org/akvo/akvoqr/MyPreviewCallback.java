package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

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
import org.akvo.akvoqr.sensor.LightSensor;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.PreviewUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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
    private int previewFormat;
    private boolean focused = true;
    private Handler handler;
    private  LightSensor lightSensor;
    double shadowPercentage = 101;
    double lumDiff = 0;
    double lumQual = 100;
    LinkedList<Double> lumTrack = new LinkedList<>();

    private Thread showFinderPatternThread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Looper.prepare();

                        handler = new Handler();

                        Looper.loop();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            }
    );

    Runnable showFinderPatternRunnable = new Runnable() {
        @Override
        public void run() {

            if (listener != null && possibleCenters != null && previewSize != null) {

                listener.showFinderPatterns(possibleCenters, previewSize, finderPatternColor);
            }
        }
    };

    public static MyPreviewCallback getInstance(Context context) {

        return new MyPreviewCallback(context);
    }

    private MyPreviewCallback(Context context) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement cameraviewListener");
        }

        possibleCenters = new ArrayList<>();

        showFinderPatternThread.start();

        lightSensor = new LightSensor();
        lightSensor.start();

    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        this.camera = camera;
        previewSize = camera.getParameters().getPreviewSize();
        previewFormat = camera.getParameters().getPreviewFormat();

        focused = false;
        finderPatternColor = Color.GREEN;

        FinderPatternInfo info;
        info = findPossibleCenters(data, previewSize);

        // checks the quality of the image data, updates the icons, and if the
        // quality is ok, shows the start button
        new QualityChecksTask(info).execute(data);

        // if the start button has been clicked and the quality is ok, use this data
        // to start the iamge processing activity.
        new SendDataTask(info).execute(data);

    }


    private class SendDataTask extends AsyncTask<byte[], Void, Void> {

        byte[] data;
        FinderPatternInfo info;

        boolean qualityOK = true;
        public SendDataTask(FinderPatternInfo info)
        {
            this.info = info;
        }

        @Override
        protected Void doInBackground(byte[]... params) {

            data = params[0];
            try {


                if (info!=null && possibleCenters != null && possibleCenters.size() == 4)
                {
                    long timePictureTaken = System.currentTimeMillis();

                    if(listener.start()) // someone clicked the start button
                    {
                        // final check if quality of image is ok, if not, abort
                        qualityOK = qualityChecks(data, info);

                        if (qualityOK)
                        {
                            camera.stopPreview();
                            listener.playSound();
                            finderPatternColor = Color.parseColor("#f02cb673");
                            handler.post(showFinderPatternRunnable);

                            data = compressToJpeg(data);

                            listener.sendData(data, timePictureTaken, ImageFormat.JPEG,
                                    previewSize.width,
                                    previewSize.height, info);

                            lightSensor.stop();
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

    private class QualityChecksTask extends AsyncTask<byte[], Void, Void>
    {
        FinderPatternInfo info;
        public QualityChecksTask(FinderPatternInfo info)
        {
            this.info = info;
        }
        @Override
        protected Void doInBackground(byte[]... params) {

            boolean ok = qualityChecks(params[0], info);
            listener.setStartButtonVisibility(ok);

            return null;
        }
    }

    private boolean qualityChecks(byte[] data, FinderPatternInfo info) {

        if(camera==null)
            return false;

        Mat bgr = null;
        List<Double> lumList = new ArrayList<>();
        List<Double> focusList = new ArrayList<>();
        double[] lumMinMax;
        double minLum = -1;
        double maxLum = 256;
        boolean luminosityQualOk = false;
        boolean shadowQualOk = false;
        boolean levelQualOk = false;
        double laplacian;
        double focusQual;

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

                    //brightness
                    lumMinMax = PreviewUtils.getDiffLuminosity(src_gray);
                    lumDiff = lumMinMax[1] - lumMinMax[0];
                    lumQual = 100 * lumDiff / 255;
                    lumList.add(lumQual);

//                    System.out.println("***lumMinMax: " + lumMinMax[0] + ", " + lumMinMax[1]);

                    //store lum min value that corresponds with highest; we use it to check over-exposure
                    if(lumMinMax[0]>minLum)
                    {
                        minLum = lumMinMax[0];
                    }
                    //store lum max value that corresponds with lowest; we use it to check under-exposure
                    if(lumMinMax[1]<maxLum)
                    {
                        maxLum = lumMinMax[1];
                    }

                    //focus
                    laplacian = PreviewUtils.focusLaplacian1(src_gray);
                    // correct the focus quality parameter for the total luminosity range of the finder pattern
                    // the factor of 0.5 means that 100% corresponds to the pattern goes from black to white within 2 pixels
                    focusQual = 100 * (laplacian / (0.5 * lumDiff));
                    focusList.add(focusQual);
                }

//                System.out.println("***lumMinMax result: " + minLum + ", " + maxLum);

            }

            //fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
            if(lumTrack.size()>25) {
                lumTrack.removeFirst();
            }


            if(lumList.size() > 0) {
                //lowest value for brightness comes first
                Collections.sort(lumList);

                //add lowest value to track list
                lumTrack.addLast(lumList.get(0));


                //compensate for over-exposure
                //if min values are larger than 20 or max values larger than 254
                if(minLum > Constant.MIN_LUM_LOWER || maxLum > Constant.MIN_LUM_UPPER)
                {
                    System.out.println("***over exposed. ");
                    //Change direction in which to compensate
                    listener.setExposureCompensation(-1);
                }
                //compare latest value with the previous one
                else if(lumTrack.getLast() > lumTrack.get(lumTrack.size()-2)) {

                    //difference is increasing; this is good, keep going in the same direction
                    listener.setExposureCompensation(1);
                }
                else if(lumTrack.getLast() > Constant.MIN_LUMINOSITY_PERCENTAGE)
                {
                    //optimum situation reached: remove last value to keep ideal situation
                    if(lumTrack.size()>2)
                        lumTrack.removeLast();

                    luminosityQualOk = lumList.get(0) > Constant.MIN_LUMINOSITY_PERCENTAGE;

                    System.out.println("***optimum exposure reached. ");

                }
                else if(lumTrack.getLast() < Constant.MIN_LUMINOSITY_PERCENTAGE)
                {
                    //we are no where near a good exposure, just keep on trying
                    listener.setExposureCompensation(1);

                    System.out.println("***adjusting exposure. ");
                }

                if(luminosityQualOk)
                    listener.showMaxLuminosity(true, 100 * maxLum/254);
                else
                    listener.showMaxLuminosity(false, 100 * maxLum/254);


                //logging
//                if(lumTrack.size()>2) {
//                    for (int i = 0; i < lumTrack.size(); i++) {
//                        System.out.println("***exp lumtrack: " + i + "  " + lumTrack.get(i) + " > " +
//                                (lumTrack.getLast() > lumTrack.get(lumTrack.size() - 2))
//                                + " > min_lum_perc: " + (lumTrack.getLast() > Constant.MIN_LUMINOSITY_PERCENTAGE));
//                    }
//                }
            }
            else //no finder patterns found
            {
                //if no finder patterns are found, remove one from track
                //when device e.g. is put down, slowly the value becomes zero
                //'slowly' being about a second
                if(lumTrack.size()>0) {
                    lumTrack.removeFirst();
                }

            }
            lumList.clear();

            if(lumTrack.size()<1) {
                //show zero values
                listener.showMaxLuminosity(false, 0);
                //reset exposure comp. to zero
                listener.setExposureCompensation(0);
                //reinitialise camera
                listener.resetSurfaceHolder();
            }

            // update quality icon of the focus
            // if focus is too low, do another round of focussing
            if(focusList.size() > 0) {
                Collections.sort(focusList);
                listener.showFocusValue(focusList.get(0));
                if (focusList.get(0) < Constant.MIN_FOCUS_PERCENTAGE)
                {
                    focused = false;
                    int count = 0;
                    // TODO Check if this actually focusses the camera.
                    while (!focused && camera != null && count < 100) {
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) focused = true;
                            }
                        });
                        count++;
                    }
                }
                else
                {
                    focused = true;
                }
            } else {

                listener.showFocusValue(0);
            }

            // DETECT SHADOWS
            if(info != null) {
                double[] tl = new double[]{info.getTopLeft().getX(), info.getTopLeft().getY()};
                double[] tr = new double[]{info.getTopRight().getX(), info.getTopRight().getY()};
                double[] bl = new double[]{info.getBottomLeft().getX(), info.getBottomLeft().getY()};
                double[] br = new double[]{info.getBottomRight().getX(), info.getBottomRight().getY()};
                Mat warp = OpenCVUtils.perspectiveTransform(tl, tr, bl, br, bgr).clone();

                try
                {
                    if(versionNumber!=CalibrationCard.CODE_NOT_FOUND)
                        shadowPercentage = PreviewUtils.getShadowPercentage(warp, versionNumber);
                    //System.out.println("***versionNumber 2: " + versionNumber);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            shadowQualOk = shadowPercentage < Constant.MAX_SHADOW_PERCENTAGE;
            listener.showShadow(shadowPercentage);

            //GET ANGLE
            if(info!=null) {
                float[] angles = PreviewUtils.getAngle(info);

                listener.showLevel(angles);
                //the sum of the angles should approach zero: then the camera is hold even with the card
                levelQualOk = Math.abs(angles[0]) + Math.abs(angles[1]) < 2 * Constant.MAX_LEVEL_DIFF;
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
            listener.setCountQualityCheckResult(focused && luminosityQualOk && shadowQualOk && levelQualOk? 1 : 0);
        }

        return true;

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
                if (handler != null && possibleCenters != null && previewSize != null) {

                    handler.post(showFinderPatternRunnable);

                    try {
                        if (possibleCenters.size() == 4) {
                            versionNumber = CalibrationCard.decodeCallibrationCardCode(possibleCenters, bitMatrix);
                            System.out.println("***versionNumber: " + versionNumber);
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

    private byte[] compressToJpeg(byte[] data)
    {

        if(previewFormat == ImageFormat.NV21) {
            YuvImage yuvImage = new YuvImage(data, previewFormat, previewSize.width, previewSize.height, null);
            Rect rect = new Rect(0, 0, previewSize.width, previewSize.height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(rect, 100, baos);
            return baos.toByteArray();
        }

        return null;
    }


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




