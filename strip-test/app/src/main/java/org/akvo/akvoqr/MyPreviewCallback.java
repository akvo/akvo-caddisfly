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
    private boolean isRunning = false;
    private final int messageRepeat = 0;
    private FinderPatternFinder finderPatternFinder;
    private List<FinderPattern> possibleCenters;
    private int finderPatternColor;
    private FinderPatternInfo info;
    private CameraViewListener listener;
    private Camera camera;
    private Camera.Size previewSize;
    private boolean focused = false;
    private Handler handler;

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

    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        this.camera = camera;
        previewSize = camera.getParameters().getPreviewSize();

        focused = false;
        finderPatternColor = Color.RED;

        if(listener.start()) {
            info = findPossibleCenters(data, previewSize);
            if (possibleCenters != null && possibleCenters.size() == 4) {
                camera.stopPreview();
                listener.playSound();
                finderPatternColor = Color.RED;
                //handler.removeCallbacks(showFinderPatternRunnable);
                handler.post(showFinderPatternRunnable);
            }
        }

        new QualityChecksTask().execute(data);

        //if (!isRunning)
            new SendDataTask().execute(data);

    }

    private class SendDataTask extends AsyncTask<byte[], Void, Void> {

        byte[] data;

        boolean qualityOK = true;

        @Override
        protected Void doInBackground(byte[]... params) {

            isRunning = true;
            data = params[0];
            try {

                if (possibleCenters != null && possibleCenters.size() == 4)
                {
                    long timePictureTaken = System.currentTimeMillis();
                    qualityOK = qualityChecks(data);

                    if (qualityOK)
                    {

                        data = compressToJpeg(data);

                        double avgModuleSize = 0.25 * (possibleCenters.get(0).getEstimatedModuleSize() + possibleCenters.get(1).getEstimatedModuleSize() +
                                possibleCenters.get(2).getEstimatedModuleSize() + possibleCenters.get(3).getEstimatedModuleSize());

                        listener.sendData(data, timePictureTaken, ImageFormat.JPEG,
                                camera.getParameters().getPreviewSize().width,
                                camera.getParameters().getPreviewSize().height, info, avgModuleSize);

                    }
                    else
                    {
                        if (listener != null)
                            listener.getMessage(messageRepeat);

                    }
                    camera.startPreview();
                }
                else
                {
                    if (listener != null)
                        listener.getMessage(messageRepeat);

                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            finally {
                isRunning = false;

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
        @Override
        protected Void doInBackground(byte[]... params) {
            qualityChecks(params[0]);
            return null;
        }
    }
    private boolean qualityChecks(byte[] data) {

        if(camera==null)
            return false;

        focused = false;

        int height = camera.getParameters().getPreviewSize().height;
        int width = camera.getParameters().getPreviewSize().width;
        Mat bgr = new Mat(height, width, CvType.CV_8UC3);

        try {

            //convert preview data to Mat object
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

            double focusLaplacian = OpenCVUtils.focusLaplacian(bgr);
            listener.showFocusValue(focusLaplacian);

            if (focusLaplacian < 250) {

                int count = 0;
                while (!focused && camera!=null && count < 100) {

                    if (camera != null) {
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) focused = true;
                            }
                        });
                    }
                    count ++;
                }
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            return false;
        }
        finally {
            bgr.release();
        }

    }

    public FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) {

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
                    if (handler != null && possibleCenters != null && previewSize != null) {

                        handler.post(showFinderPatternRunnable);

                    }
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




