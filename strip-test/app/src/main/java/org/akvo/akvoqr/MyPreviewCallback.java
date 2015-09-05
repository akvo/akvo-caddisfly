package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;

import org.akvo.akvoqr.detector.BinaryBitmap;
import org.akvo.akvoqr.detector.BitMatrix;
import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternFinder;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.detector.HybridBinarizer;
import org.akvo.akvoqr.detector.NotFoundException;
import org.akvo.akvoqr.detector.PlanarYUVLuminanceSource;
import org.akvo.akvoqr.detector.ResultPoint;
import org.akvo.akvoqr.detector.ResultPointCallback;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.akvo.akvoqr.opencv.StripTest;
import org.akvo.akvoqr.sensor.LightSensor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 6/26/15.
 */
public class MyPreviewCallback implements Camera.PreviewCallback {

    public static boolean firstTime = true;
    FinderPatternFinder finderPatternFinder;
    List<ResultPoint> resultPoints = new ArrayList<>();
    FinderPatternInfo info;
    List<FinderPattern> possibleCenters;
    CameraViewListener listener;
    Camera camera;
    private static boolean isRunning = false;
    private boolean focused = false;
    private boolean allOK = false;
    Bitmap bitmap;
    private ArrayList<Mat> mats;
    private Handler handler;
    private Runnable runAtListener = new Runnable() {
        @Override
        public void run() {
            if (listener != null) {

                if(allOK) {
                    if(bitmap!=null)
                        listener.setBitmap(bitmap);
                    else if(mats!=null)
                        listener.sendMats(mats);
                }
                else {
                    listener.getMessage(0);
                }
            }
            if(bitmap!=null)
                bitmap.recycle();
        }
    };
    private boolean testCalib = false;

    public static MyPreviewCallback getInstance(Context context) {

        return new MyPreviewCallback(context);

    }

    private MyPreviewCallback(Context context) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement cameraviewListener");
        }

        handler = new Handler();
    }

    final ResultPointCallback resultPointCallback = new ResultPointCallback() {
        @Override
        public void foundPossibleResultPoint(ResultPoint point) {
            resultPoints.add(point);

        }
    };

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        this.camera = camera;

        //System.out.println("***is Running: " + isRunning);
        if (!isRunning) {
            isRunning = true;
            allOK = false;
            focused = false;
            possibleCenters = null;

            new BitmapTask().execute(data);
        }

    }

    public FinderPatternInfo getInfo() {
        return info;
    }

    public List<FinderPattern> getPossibleCenters() {
        return possibleCenters;
    }

    private class BitmapTask extends AsyncTask<byte[], Void, Void> {

        @Override
        public void onCancelled()
        {
            //if(!isRunning)
            cancel(false);
            super.onCancelled();
        }
        @Override
        protected Void doInBackground(byte[]... params) {


            byte[] data = params[0];

            makeBitmap(data);

            return null;
        }

        protected void onPostExecute(Void result) {
            isRunning = false;
            handler.post(runAtListener);
        }

    }

    private void makeBitmap(byte[] data) {

        focused = false;
        try {

            //TODO CHECK EXPOSURE
            int number = 10;
            LightSensor lightSensor = new LightSensor();
            lightSensor.start();
            while (lightSensor.getLux()==-1 && number>0) {

                number --;
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("*** lux: " + lightSensor.getLux());
            lightSensor.stop();

            //TODO CHECK SHADOWS

            listener.showProgress(3);
            findPossibleCenters(data);

            if (possibleCenters != null && possibleCenters.size() > 3) {

                //if patterns are found, focus camera and return
                //this is a workaround to give camera time to adjust exposure
                //we assume that second time is immediately after first time, so patterns are found while the camera is
                //focused correctly

                if (firstTime) {
                    System.out.println("*** focussing!!!!!!!");
                    while (!focused) {
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) focused = true;
                            }
                        });
                    }
                    firstTime = false;
                    return;
                }
                listener.dismissProgress();

                listener.playSound();

                for (FinderPattern pattern : possibleCenters) {
                    System.out.println("***pattern estimated module size: " + pattern.getEstimatedModuleSize());
                }

                int pheight = camera.getParameters().getPreviewSize().height;
                int pwidth = camera.getParameters().getPreviewSize().width;

                //convert preview data to Mat object with highest possible quality
                Mat mbgra = new Mat(pheight, pwidth, CvType.CV_8UC3);
                Mat convert_mYuv = new Mat(pheight + pheight / 2, pwidth, CvType.CV_8UC1);
                convert_mYuv.put(0, 0, data);
                Imgproc.cvtColor(convert_mYuv, mbgra, Imgproc.COLOR_YUV2BGR_NV21, mbgra.channels());


                //noOfModules: constant that holds the time estimated module size is to be multiplied by, used to 'cut out' image not showing finder patterns
                //typical value is 3.5 as pattern is 1:1:3:1:1 which amounts to seven.
                final float noOfModules = 0f;
                final float adjustTL = noOfModules * possibleCenters.get(0).getEstimatedModuleSize();
                final float adjustTR = noOfModules * possibleCenters.get(1).getEstimatedModuleSize();
                final float adjustBL = noOfModules * possibleCenters.get(2).getEstimatedModuleSize();
                final float adjustBR = noOfModules * possibleCenters.get(3).getEstimatedModuleSize();

                //perspectiveTransform
                Mat warp_dst = OpenCVUtils.perspectiveTransform(info, mbgra);

                //find calibration patches
                listener.showProgress(0);
                Mat calMat = listener.getCalibratedImage(warp_dst);
                listener.dismissProgress();

                Mat dest;
                Mat striparea = null;
                if (calMat == null) {
                    System.out.println("***calibration failed");
                    dest = warp_dst.clone();
                } else {
                    dest = calMat.clone();
                }

                //show calibrated patches in gridview in ResultActivity
//                ResultActivity.colors.clear();
//
//                for(Patch patch: patches) {
//                    Point point1 = new Point(patch.x - patch.d/2, patch.y - patch.d/2);
//                    Point point2 = new Point(patch.x + patch.d/2, patch.y + patch.d/2);
//                    Core.rectangle(warp_dst, point1, point2, new Scalar(255, 0, 0, 255));
//
//                    int color = Color.rgb((int)patch.red,(int) patch.green,(int) patch.blue);
//                    ResultActivity.colors.add(new ResultActivity.ColorDetected(color, patch.x));
//                }

                if (testCalib) {
                    Imgproc.cvtColor(calMat, calMat, Imgproc.COLOR_BGR2RGBA);
                    mats = new ArrayList<>();
                    mats.add(calMat.clone());

                    allOK = true;
                    return;
                }

                //detect strip
                Mat orig = new Mat();

                double ratioW = 1;
                double ratioH = 1;
                String json = AssetsManager.getInstance().loadJSONFromAsset("calibration.json");
                if (json != null) {

                    double hsize = 1;
                    double vsize = 1;
                    JSONObject object = new JSONObject(json);
                    if (!object.isNull("calData")) {
                        JSONObject calData = object.getJSONObject("calData");
                        hsize = calData.getDouble("hsize");
                        vsize = calData.getDouble("vsize");
                    }
                    if (!object.isNull("stripAreaData")) {
                        JSONObject stripAreaData = object.getJSONObject("stripAreaData");
                        if (!stripAreaData.isNull("area")) {
                            JSONArray area = stripAreaData.getJSONArray("area");
                            if (area.length() == 4) {

                                ratioW = dest.width() / hsize;
                                ratioH = dest.height() / vsize;
                                Point stripTopLeft = new Point(area.getDouble(0) * ratioW + 2,
                                        area.getDouble(1) * ratioH + 2);
                                Point stripBottomRight = new Point(area.getDouble(2) * ratioW - 2,
                                        area.getDouble(3) * ratioH - 2);

                                Rect roi = new Rect(stripTopLeft, stripBottomRight);
                                striparea = dest.submat(roi);
                                orig = warp_dst.submat(roi);
                            }
                        }
                    }
                } else {
//                float stripareaSize = 31; //mm
//                float vertSize = 75; //mm
//                float ratio = stripareaSize/vertSize;
//                int stripH = (int)Math.round(dest.height() * ratio);
//                Rect rect = new Rect(0, (int)Math.round(dest.height() - stripH), dest.width(), stripH);

                }

                if (striparea != null) {

                    StripTest stripTestBrand = StripTest.getInstance();
                    StripTest.Brand brand = stripTestBrand.getBrand(listener.getBrand());

                    listener.showProgress(1);
                    Mat strip = OpenCVUtils.detectStrip(striparea, brand, ratioW, ratioH);
                    listener.dismissProgress();

                    if (strip != null) {
                        listener.showProgress(2);

                        mats = new ArrayList<>();
//                        if (!orig.empty()) {
//                            Imgproc.cvtColor(orig, orig, Imgproc.COLOR_BGR2RGBA);
//                            mats.add(orig);
//                        }
                        Imgproc.cvtColor(strip, strip, Imgproc.COLOR_BGR2RGBA);
                        //mats.add(striparea);
                        mats.add(strip);

                        listener.dismissProgress();
                    } else {
                        mats = new ArrayList<>();
                        mats.add(striparea.clone());

                        //draw a red cross over the image
                        Imgproc.line(striparea, new Point(0, 0), new Point(striparea.cols(),
                                striparea.rows()), new Scalar(255, 0, 0, 255), 2);
                        Imgproc.line(striparea, new Point(0, striparea.rows()), new Point(striparea.cols(),
                                0), new Scalar(255, 0, 0, 255), 2);

                        mats.add(striparea);

                    }

                    allOK = true;

                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            listener.dismissProgress();

            allOK = false;


        }
    }

    public void findPossibleCenters(byte[] data) {
        if (camera != null) {
            final Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

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
                finderPatternFinder = new FinderPatternFinder(bitMatrix, resultPointCallback);

                try {

                    if (possibleCenters != null)
                        possibleCenters = null;

                    info = finderPatternFinder.find(null);
                    possibleCenters = finderPatternFinder.getPossibleCenters();

                } catch (Exception e) {
                    // ignore. this only means no patterns are detected.
                }
            }
        }
    }




}




