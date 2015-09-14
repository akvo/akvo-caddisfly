package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;

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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 6/26/15.
 */
public class MyPreviewCallback implements Camera.PreviewCallback {

    //public static boolean firstTime = true;
    private boolean isRunning = true;
    private final int messageRepeat = 0;
    private FinderPatternFinder finderPatternFinder;
    private List<FinderPattern> possibleCenters;
    private FinderPatternInfo info;
    private CameraViewListener listener;
    private Camera camera;
    private Camera.Size previewSize;
    private boolean focused = false;
    private int countFrame = 0;

    private Thread showFinderPatternThread = new Thread( new Runnable() {
        @Override
        public void run() {

            while (isRunning) {
                if (listener != null && possibleCenters != null && previewSize != null) {

                   // System.out.println("***possibleCenters: " + possibleCenters.size());
                    listener.showFinderPatterns(possibleCenters, previewSize);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    });

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
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        this.camera = camera;
        previewSize = camera.getParameters().getPreviewSize();

        //countFrame ++;
       // System.out.println("***is Running: " + isRunning + " countFrame: " + countFrame);

        focused = false;

        new BitmapTask().execute(data);

    }

    private class BitmapTask extends AsyncTask<byte[], Void, Void> {

        byte[] data;

        boolean qualityOK = true;

        @Override
        protected Void doInBackground(byte[]... params) {

            data = params[0];
            try {

                qualityOK = qualityChecks(data);
                System.out.println("*** starting findPossibleCenters");
                if(listener.start()) {
                    System.out.println("*** really starting findPossibleCenters");
                    info = findPossibleCenters(data, previewSize);
                }

                System.out.println("*** check if we found some centers");
                if (possibleCenters != null && possibleCenters.size() == 4) {
                    if (qualityOK) {
                        System.out.println("*** have centers, now analysis");
                        //isRunning = false;

                        listener.playSound();
                        data = compressToJpeg(data);

                        double avgModuleSize = 0.25 * (possibleCenters.get(0).getEstimatedModuleSize() + possibleCenters.get(1).getEstimatedModuleSize() +
                                possibleCenters.get(2).getEstimatedModuleSize() + possibleCenters.get(3).getEstimatedModuleSize());

                        listener.sendData(data, ImageFormat.JPEG,
                                camera.getParameters().getPreviewSize().width,
                                camera.getParameters().getPreviewSize().height, info, avgModuleSize);
                        // takePicture();

                    }
                } else {

                    if (listener != null)
                        listener.getMessage(messageRepeat);


                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //do nothing
        }
    }

    private boolean qualityChecks(byte[] data) {

        focused = false;
        Mat bgr;
        int height = camera.getParameters().getPreviewSize().height;
        int width = camera.getParameters().getPreviewSize().width;

        try {

            //convert preview data to Mat object
            bgr = new Mat(height, width, CvType.CV_8UC3);
            Mat convert_mYuv = new Mat(height + height / 2, width, CvType.CV_8UC1);
            convert_mYuv.put(0, 0, data);
            Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, bgr.channels());

            //CHECK EXPOSURE.
            LightSensor lightSensor = new LightSensor();
            if(lightSensor.hasLightSensor())
            {
                lightSensor.start();
                double lux = lightSensor.getLux();
                listener.showMaxLuminosity(lux);
            }
            else {
                // find maximum of L-channel
                double maxLum = OpenCVUtils.getMaxLuminosity(bgr);
                listener.showMaxLuminosity(maxLum);
            }

            //TODO CHECK SHADOWS

            //if patterns are found, focus camera and return
            //this is a workaround to give camera time to adjust exposure
            //we assume that second time is immediately after first time, so patterns are found while the camera is
            //focused correctly

            double focusLaplacian = OpenCVUtils.focusLaplacian(bgr);
            listener.showFocusValue(focusLaplacian);

            if (focusLaplacian < 250) {
                System.out.println("***focussing");
                while (!focused) {
                    System.out.println("***Trying to get focus");
                    if (camera != null) {
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) focused = true;
                            }
                        });
                    }
                }
//                firstTime = false;

            }


            return true;

        }
        catch (Exception e)
        {
            e.printStackTrace();

            return false;

        }
    }

    public FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listener.showFinderPatterns(possibleCenters, size);
            }
        };
        if (camera != null) {

            FinderPatternInfo info = null;
            PlanarYUVLuminanceSource myYUV = new PlanarYUVLuminanceSource(data, size.width,
                    size.height, 0, 0,
                    size.width,
                    size.height, false);

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

                    //remove centers that are to small in order to get rid of noise
//                    for(int i=0;i<possibleCenters.size();i++) {
//                        if (possibleCenters.get(i).getEstimatedModuleSize() < 2) {
//                            possibleCenters.remove(i);
//                            //System.out.println("***removed possible center no. " + i);
//                        }
//                    }
                    //System.out.println("***possible centers size: " + possibleCenters.size());

                    return info;
                }
            }
        }

        return null;
    }

    private byte[] compressToJpeg(byte[] data)
    {
        int format = camera.getParameters().getPreviewFormat();
        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;

        if(format == ImageFormat.NV21) {
            YuvImage yuvImage = new YuvImage(data, format, width, height, null);
            Rect rect = new Rect(0, 0, width, height);
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




