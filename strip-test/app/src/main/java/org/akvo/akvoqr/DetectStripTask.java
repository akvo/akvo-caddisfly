package org.akvo.akvoqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.os.AsyncTask;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.calibration.CalibrationData;
import org.akvo.akvoqr.calibration.CalibrationResultData;
import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

/**
 * Created by linda on 11/18/15.
 */
public class DetectStripTask extends AsyncTask<Intent,Void, Void> {

    int format;
    int width;
    int height;
    double ratioW = 1;
    double ratioH = 1;
    org.opencv.core.Rect roiStriparea = null;
    org.opencv.core.Rect roiCalarea = null;
    Mat warp_dst;
    Mat cal_dest;
    private DetectStripListener listener;
    private Context context;
    private FileStorage fileStorage;
    private Bitmap bitmap;
    private boolean develop = false;

    public DetectStripTask(Context listener) {

        try {
            this.listener = (DetectStripListener) listener;
            this.context = listener;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("must implement DetectStripListener");
        }

        fileStorage = new FileStorage(context);
    }

    @Override
    protected void onPreExecute() {

        if(listener==null) {
            cancel(true);
        }

        try {

            listener.showSpinner();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Intent... params) {

        Intent intent = params[0];

        if(intent==null)
            return null;

        String brandname = intent.getStringExtra(Constant.BRAND);

        StripTest stripTest = StripTest.getInstance(context);
        int numPatches = stripTest.getBrand(brandname).getPatches().size();

        format = intent.getIntExtra(Constant.FORMAT, ImageFormat.NV21);
        width = intent.getIntExtra(Constant.WIDTH, 0);
        height = intent.getIntExtra(Constant.HEIGHT, 0);

        if (width == 0 || height == 0) {

            return null;
        }

        JSONArray imagePatchArray = null;
        int imageCount = -1;
        Mat labImg; // Mat for image from NV21 data
        Mat labStrip; // Mat for detected strip

        try {
            String json = fileStorage.readFromInternalStorage(Constant.IMAGE_PATCH + ".txt");
            imagePatchArray = new JSONArray(json);
            //System.out.println("***imagePatchArray: " + imagePatchArray.toString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numPatches; i++) {
            try {
                if (imagePatchArray != null) {
                    // sub-array for each patch
                    JSONArray array = imagePatchArray.getJSONArray(i);

                    // get the image number from the json array
                    int imageNo = array.getInt(0);

                    if (imageNo > imageCount) {

                        // Set imageCount to current number
                        imageCount = imageNo;

                        listener.showMessage(0);

                        byte[] data = fileStorage.readByteArray( Constant.DATA + imageNo);
                        if (data == null)
                            throw new IOException();

                        //make a L,A,B Mat object from data
                        try {
                            labImg = makeLab(data);
                        } catch (Exception e) {
                            listener.showError(0);
                            continue;
                        }

                        //perspectiveTransform
                        try {
                            warp(labImg, imageNo);
                        } catch (Exception e) {
                            listener.showError(1);
                            continue;
                        }

                        //divide into calibration and stripareas
                        try {
                            if(context!=null)
                                divideIntoCalibrationAndStripArea(context);
                        } catch (Exception e) {
                            listener.showError(1);
                            continue;
                        }

                        // save warped image to external storage
                        if (develop) {
                            Mat rgb = new Mat();
                            Imgproc.cvtColor(warp_dst, rgb, Imgproc.COLOR_Lab2RGB);
                            Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(rgb, bitmap);

                            if (FileStorage.checkExternalMedia()) {
                                FileStorage.writeToSDFile(bitmap);
                            }
                            Bitmap.createScaledBitmap(bitmap, 800, 480, false);
                            listener.showImage(bitmap);
                        }

                        //calibrate
                        try {
                            listener.showMessage(1);
                            CalibrationResultData calResult = getCalibratedImage(warp_dst);
                            cal_dest = calResult.calibratedImage;
                            if(develop) {
                                listener.showMessage("E94 mean: " + String.format("%.2f", calResult.meanE94) + ", max: " + String.format("%.2f", calResult.maxE94));
                            }
                        } catch (Exception e) {
                            System.out.println("cal. failed: " + e.getMessage());
                            e.printStackTrace();
                            listener.showError(3);
                            cal_dest = warp_dst.clone();
                        }

                        //show calibrated image
                        if (develop) {
                            Mat rgb = new Mat();
                            Imgproc.cvtColor(cal_dest, rgb, Imgproc.COLOR_Lab2RGB);
                            Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(rgb, bitmap);
                            Bitmap.createScaledBitmap(bitmap, 800, 480, false);
                            listener.showImage(bitmap);
                        }

                        Mat striparea = null;
                        if (roiStriparea != null)
                            striparea = cal_dest.submat(roiStriparea);

                        if (striparea != null) {
                            listener.showMessage(2);

                            StripTest.Brand brand = stripTest.getBrand(brandname);

                            Mat strip = null;
                            try {
                                 strip = OpenCVUtils.detectStrip(striparea, brand, ratioW, ratioH);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            String error = "";
                            if (strip != null) {

                                labStrip = strip.clone();

                            } else {
                                listener.showError(4);
                                labStrip = striparea.clone();

                                error = Constant.ERROR;

                                //draw a red cross over the image
                                // red in Lab schema
                                Scalar red = new Scalar(135,208,195);
                                Imgproc.line(labStrip, new Point(0, 0), new Point(labStrip.cols(),
                                        labStrip.rows()), red, 2);
                                Imgproc.line(labStrip, new Point(0, labStrip.rows()), new Point(labStrip.cols(),
                                        0), red, 2);
                            }

                            try {
                                Mat rgb = new Mat();
                                Imgproc.cvtColor(labStrip, rgb, Imgproc.COLOR_Lab2RGB);
                                bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(rgb, bitmap);
                                fileStorage.writeBitmapToInternalStorage(Constant.STRIP + i + error, bitmap);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            } catch (Exception e) {

                listener.showError(5);
                continue;
            }
        }
        listener.showMessage(3);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        //System.out.println("***onPostExecute DetectStripTask");

        if(listener!=null) {

            listener.showResults();
        }
        else {
            //System.out.println("***listener is null");
            //TODO what now?
        }
    }

    private Mat makeLab(byte[] data) throws Exception
    {
        if (format == ImageFormat.NV21) {
            //convert preview data to Mat object in CIELAB format
            Mat bgr = new Mat(height, width, CvType.CV_8UC3);
            Mat labImg = new Mat(height, width, CvType.CV_8UC3);
            Mat convert_mYuv = new Mat(height + height / 2, width, CvType.CV_8UC1);
            convert_mYuv.put(0, 0, data);
            Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2RGB_NV21, bgr.channels());
            Imgproc.cvtColor(bgr, labImg, Imgproc.COLOR_RGB2Lab, bgr.channels());

            return labImg;
        }

        return null;
    }

    private void warp(Mat labImg, int i) throws Exception
    {
        if(labImg == null)
        {
            throw new Exception("no image");
        }

        String jsonInfo = fileStorage.readFromInternalStorage(Constant.INFO + i + ".txt");
        if (jsonInfo == null) {

            throw new Exception("no finder pattern info");
        }

        JSONObject jsonObject = new JSONObject(jsonInfo);
        JSONArray tl = jsonObject.getJSONArray(Constant.TOPLEFT);
        JSONArray tr = jsonObject.getJSONArray(Constant.TOPRIGHT);
        JSONArray bl = jsonObject.getJSONArray(Constant.BOTTOMLEFT);
        JSONArray br = jsonObject.getJSONArray(Constant.BOTTOMRIGHT);
        double[] topleft = new double[]{tl.getDouble(0), tl.getDouble(1)};
        double[] topright = new double[]{tr.getDouble(0), tr.getDouble(1)};
        double[] bottomleft = new double[]{bl.getDouble(0), bl.getDouble(1)};
        double[] bottomright = new double[]{br.getDouble(0), br.getDouble(1)};

        warp_dst = OpenCVUtils.perspectiveTransform(topleft, topright, bottomleft, bottomright, labImg);
    }

    private void divideIntoCalibrationAndStripArea(Context context) throws Exception{

        CalibrationCard calibrationCard = CalibrationCard.getInstance(context);
        CalibrationData data = calibrationCard.getCalData();

        if (warp_dst!=null && data != null) {

            double hsize = data.hsize;
            double vsize = data.vsize;
            double[] area = data.stripArea;

            if (area.length == 4) {

                ratioW = warp_dst.width() / hsize;
                ratioH = warp_dst.height() / vsize;
                Point stripTopLeft = new Point(area[0] * ratioW + Constant.PIXEL_MARGIN_STRIP_AREA_WIDTH,
                        area[1] * ratioH + Constant.PIXEL_MARGIN_STRIP_AREA_HEIGHT);
                Point stripBottomRight = new Point(area[2] * ratioW - Constant.PIXEL_MARGIN_STRIP_AREA_WIDTH,
                        area[3] * ratioH - Constant.PIXEL_MARGIN_STRIP_AREA_HEIGHT);

                //striparea rect
                roiStriparea = new org.opencv.core.Rect(stripTopLeft, stripBottomRight);

                //calarea rect
                roiCalarea = new org.opencv.core.Rect(new Point(0, 0),
                        new Point(warp_dst.width(), area[1] * ratioH));

            }
        }
    }

    public CalibrationResultData getCalibratedImage(Mat mat) throws Exception
    {
        //System.out.println("***version number detect: " + CalibrationCard.getMostFrequentVersionNumber());

        CalibrationCard calibrationCard = CalibrationCard.getInstance(context);
        if(CalibrationCard.getMostFrequentVersionNumber() == CalibrationCard.CODE_NOT_FOUND)
        {
            throw new Exception("no version number set.");
        }
        CalibrationData data = calibrationCard.getCalData();
        return calibrationCard.calibrateImage(mat, data);

    }
}



