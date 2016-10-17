/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.detect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.os.AsyncTask;
import android.util.Log;

import org.akvo.caddisfly.sensor.colorimetry.strip.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.CalibrationData;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.CalibrationResultData;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.OpenCVUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by linda on 11/18/15.
 * reads in the YUV images, and extracts the strips
 */
public class DetectStripTask extends AsyncTask<Intent, Void, Void> {

    private final static boolean DEVELOP_MODE = false;
    private int format;
    private int width;
    private int height;
    private double ratioW = 1;
    private double ratioH = 1;
    private org.opencv.core.Rect roiStripArea = null;
    private Mat warp_dst;
    private DetectStripListener listener;
    private Context context;
    private FileStorage fileStorage;

    public DetectStripTask(Context listener) {
        try {
            this.listener = (DetectStripListener) listener;
            this.context = listener;
        } catch (ClassCastException e) {
            throw new ClassCastException("must implement DetectStripListener");
        }

        fileStorage = new FileStorage(context);
    }

    @Override
    protected void onPreExecute() {
        if (listener == null) {
            cancel(true);
        } else {
            try {
                listener.showSpinner();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Void doInBackground(Intent... params) {
        Intent intent = params[0];

        if (intent == null)
            return null;

        String uuid = intent.getStringExtra(Constant.UUID);

        StripTest stripTest = new StripTest();
        int numPatches = stripTest.getBrand(uuid).getPatches().size();

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
        } catch (Exception e) {
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

                        listener.showMessage();

                        byte[] data = fileStorage.readByteArray(Constant.DATA + imageNo);
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

                        //divide into calibration and strip areas
                        try {
                            if (context != null)
                                divideIntoCalibrationAndStripArea();
                        } catch (Exception e) {
                            listener.showError(1);
                            continue;
                        }

                        // save warped image to external storage
                        if (DEVELOP_MODE) {
                            Mat rgb = new Mat();
                            Imgproc.cvtColor(warp_dst, rgb, Imgproc.COLOR_Lab2RGB);
                            Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(rgb, bitmap);

                            if (FileStorage.isExternalStorageWritable()) {
                                FileStorage.writeBitmapToExternalStorage(bitmap, "/warp", UUID.randomUUID().toString() + ".png");
                            }
                            Bitmap.createScaledBitmap(bitmap, 800, 480, false);
                        }

                        //calibrate
                        Mat cal_dest;
                        try {
                            listener.showMessage();
                            CalibrationResultData calResult = getCalibratedImage(warp_dst);
                            cal_dest = calResult.calibratedImage;
                            Log.d(this.getClass().getSimpleName(), "E94 error mean: " + String.format(Locale.US, "%.2f", calResult.meanE94) +
                                    ", max: " + String.format(Locale.US, "%.2f", calResult.maxE94) +
                                    ", total: " + String.format(Locale.US, "%.2f", calResult.totalE94));

//                            if (DEVELOP_MODE) {
//                                listener.showMessage("E94 mean: " + String.format(Locale.US, "%.2f", calResult.meanE94) +
//                                        ", max: " + String.format(Locale.US, "%.2f", calResult.maxE94));
//                            }
                        } catch (Exception e) {
                            //System.out.println("cal. failed: " + e.getMessage());
                            e.printStackTrace();
                            listener.showError(3);
                            cal_dest = warp_dst.clone();
                        }

                        //show calibrated image
                        if (DEVELOP_MODE) {
                            Mat rgb = new Mat();
                            Imgproc.cvtColor(cal_dest, rgb, Imgproc.COLOR_Lab2RGB);
                            Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(rgb, bitmap);
                            Bitmap.createScaledBitmap(bitmap, 800, 480, false);
                        }

                        // cut out black area that contains the strip
                        Mat stripArea = null;
                        if (roiStripArea != null)
                            stripArea = cal_dest.submat(roiStripArea);

                        if (stripArea != null) {
                            listener.showMessage();
                            Mat strip = null;
                            try {
                                StripTest.Brand brand = stripTest.getBrand(uuid);
                                strip = OpenCVUtil.detectStrip(stripArea, brand, ratioW, ratioH);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String error = "";
                            if (strip != null) {
                                labStrip = strip.clone();
                            } else {
                                listener.showError(4);
                                labStrip = stripArea.clone();

                                error = Constant.ERROR;

                                //draw a red cross over the image
                                // red in Lab schema
                                Scalar red = new Scalar(135, 208, 195);
                                Imgproc.line(labStrip, new Point(0, 0), new Point(labStrip.cols(),
                                        labStrip.rows()), red, 2);
                                Imgproc.line(labStrip, new Point(0, labStrip.rows()), new Point(labStrip.cols(),
                                        0), red, 2);
                            }

                            try {
                                // create byte[] from Mat and store it in internal storage
                                // In order to restore the byte array, we also need the rows and columns dimensions
                                // these are stored in the last 8 bytes
                                int dataSize = labStrip.cols() * labStrip.rows() * 3;
                                byte[] payload = new byte[dataSize + 8];
                                byte[] matByteArray = new byte[dataSize];

                                labStrip.get(0, 0, matByteArray);

                                // pack cols and rows into byte arrays
                                byte[] rows = FileStorage.leIntToByteArray(labStrip.rows());
                                byte[] cols = FileStorage.leIntToByteArray(labStrip.cols());

                                // append them to the end of the array, in order rows, cols
                                System.arraycopy(matByteArray, 0, payload, 0, dataSize);
                                System.arraycopy(rows, 0, payload, dataSize, 4);
                                System.arraycopy(cols, 0, payload, dataSize + 4, 4);
                                fileStorage.writeByteArray(payload, Constant.STRIP + i + error);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {

                listener.showError(5);
            }
        }
        listener.showMessage();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        //System.out.println("***onPostExecute DetectStripTask");

        if (listener != null) {
            listener.showResults();
        }
        //else {
        //System.out.println("***listener is null");
        //}
    }

    // Creates a lab image out of the original YUV preview data
    // first casts to RGB, as we can't cast to LAB directly using openCV
    private Mat makeLab(byte[] data) {
        if (format == ImageFormat.NV21) {
            //convert preview data to Mat object in CIELab format
            Mat rgb = new Mat(height, width, CvType.CV_8UC3);
            Mat labImg = new Mat(height, width, CvType.CV_8UC3);
            Mat convert_mYuv = new Mat(height + height / 2, width, CvType.CV_8UC1);
            convert_mYuv.put(0, 0, data);
            Imgproc.cvtColor(convert_mYuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, rgb.channels());
            Imgproc.cvtColor(rgb, labImg, Imgproc.COLOR_RGB2Lab, rgb.channels());

            return labImg;
        }
        return null;
    }

    private void warp(Mat labImg, int i) throws Exception {
        if (labImg == null) {
            throw new Exception("no image");
        }

        String jsonInfo = fileStorage.readFromInternalStorage(Constant.INFO + i + ".txt");
        if (jsonInfo == null) {

            throw new Exception("no finder pattern info");
        }

        JSONObject jsonObject = new JSONObject(jsonInfo);
        JSONArray tl = jsonObject.getJSONArray(Constant.TOP_LEFT);
        JSONArray tr = jsonObject.getJSONArray(Constant.TOP_RIGHT);
        JSONArray bl = jsonObject.getJSONArray(Constant.BOTTOM_LEFT);
        JSONArray br = jsonObject.getJSONArray(Constant.BOTTOM_RIGHT);
        double[] topLeft = new double[]{tl.getDouble(0), tl.getDouble(1)};
        double[] topRight = new double[]{tr.getDouble(0), tr.getDouble(1)};
        double[] bottomLeft = new double[]{bl.getDouble(0), bl.getDouble(1)};
        double[] bottomRight = new double[]{br.getDouble(0), br.getDouble(1)};

        warp_dst = OpenCVUtil.perspectiveTransform(topLeft, topRight, bottomLeft, bottomRight, labImg);
    }

    private void divideIntoCalibrationAndStripArea() {
        CalibrationData data = CalibrationCard.readCalibrationFile();

        if (warp_dst != null && data != null) {
            double hSize = data.hSize;
            double vSize = data.vSize;
            double[] area = data.stripArea;

            if (area.length == 4) {
                ratioW = warp_dst.width() / hSize;
                ratioH = warp_dst.height() / vSize;
                Point stripTopLeft = new Point(area[0] * ratioW + Constant.PIXEL_MARGIN_STRIP_AREA_WIDTH,
                        area[1] * ratioH + Constant.PIXEL_MARGIN_STRIP_AREA_HEIGHT);
                Point stripBottomRight = new Point(area[2] * ratioW - Constant.PIXEL_MARGIN_STRIP_AREA_WIDTH,
                        area[3] * ratioH - Constant.PIXEL_MARGIN_STRIP_AREA_HEIGHT);

                //strip area rect
                roiStripArea = new org.opencv.core.Rect(stripTopLeft, stripBottomRight);

                //cal area rect
//                org.opencv.core.Rect roiCalArea = new org.opencv.core.Rect(new Point(0, 0),
//                        new Point(warp_dst.width(), area[1] * ratioH));
            }
        }
    }

    private CalibrationResultData getCalibratedImage(Mat mat) throws Exception {
        if (CalibrationCard.getMostFrequentVersionNumber() == CalibrationCard.CODE_NOT_FOUND) {
            throw new Exception("no version number set.");
        }
        CalibrationData data = CalibrationCard.readCalibrationFile();
        return CalibrationCard.calibrateImage(mat, data);
    }
}

