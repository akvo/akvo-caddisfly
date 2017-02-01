/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.detect;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.CalibrationException;
import org.akvo.caddisfly.sensor.colorimetry.strip.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.CalibrationData;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.CalibrationResultData;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.OpenCVUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by linda on 11/18/15.
 * reads in the YUV images, and extracts the strips
 */
public class DetectStripTask extends AsyncTask<Intent, Void, Void> {

    private static final Scalar RED_LAB_COLOR = new Scalar(135, 208, 195);
    private final DetectStripListener listener;
    private final Context context;
    private int format;
    private int width;
    private int height;
    private double ratioW = 1;
    private double ratioH = 1;
    @Nullable
    private org.opencv.core.Rect roiStripArea = null;
    private Mat warpMat;

    public DetectStripTask(Context listener) {
        try {
            this.listener = (DetectStripListener) listener;
            this.context = listener;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("must implement DetectStripListener", e);
        }
    }

    @Override
    protected void onPreExecute() {
        if (listener == null) {
            cancel(true);
        } else {
            listener.showSpinner();
        }
    }

    @Nullable
    @Override
    protected Void doInBackground(Intent... params) {
        Intent intent = params[0];

        if (intent == null) {
            return null;
        }

        String uuid = intent.getStringExtra(Constant.UUID);

        StripTest stripTest = new StripTest();
        int numPatches = stripTest.getPatchCount(uuid);

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
            String json = FileUtil.readFromInternalStorage(context, Constant.IMAGE_PATCH);
            imagePatchArray = new JSONArray(json);
        } catch (Exception e) {
            Timber.e(e);
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

                        byte[] data = FileUtil.readByteArray(context, Constant.DATA + imageNo);
                        if (data == null) {
                            throw new IOException();
                        }

                        //make a L,A,B Mat object from data
                        try {
                            labImg = makeLab(data);
                        } catch (Exception e) {
                            if (context != null) {
                                Timber.e(e);
                            }
                            continue;
                        }

                        //perspectiveTransform
                        try {
                            if (labImg != null) {
                                warp(labImg, imageNo);
                            }
                        } catch (Exception e) {
                            if (context != null) {
                                Timber.e(e);
                            }
                            continue;
                        }

                        //divide into calibration and strip areas
                        try {
                            if (context != null) {
                                divideIntoCalibrationAndStripArea();
                            }
                        } catch (Exception e) {
                            Timber.e(e);
                            continue;
                        }

                        //save warped image to external storage
//                        if (DEVELOP_MODE) {
//                        Mat rgb = new Mat();
//                        Imgproc.cvtColor(warpMat, rgb, Imgproc.COLOR_Lab2RGB);
//                        Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(rgb, bitmap);
//
//                        //if (FileUtil.isExternalStorageWritable()) {
//                        FileUtil.writeBitmapToExternalStorage(bitmap, "/warp", UUID.randomUUID().toString() + ".png");
                        //}
//                            //Bitmap.createScaledBitmap(bitmap, BITMAP_SCALED_WIDTH, BITMAP_SCALED_HEIGHT, false);
//                        }

                        //calibrate
                        Mat calibrationMat;
                        try {
                            CalibrationResultData calResult = getCalibratedImage(warpMat);
                            if (calResult == null) {
                                return null;
                            } else {
                                calibrationMat = calResult.getCalibratedImage();
                            }

//                            Log.d(this.getClass().getSimpleName(), "E94 error mean: " + String.format(Locale.US, "%.2f", calResult.meanE94)
//                                    + ", max: " + String.format(Locale.US, "%.2f", calResult.maxE94)
//                                    + ", total: " + String.format(Locale.US, "%.2f", calResult.totalE94));

//                            if (AppPreferences.isDiagnosticMode()) {
//                                listener.showError("E94 mean: " + String.format(Locale.US, "%.2f", calResult.meanE94)
//                                        + ", max: " + String.format(Locale.US, "%.2f", calResult.maxE94)
//                                        + ", total: " + String.format(Locale.US, "%.2f", calResult.totalE94));
//                            }
                        } catch (Exception e) {
                            Timber.e(e);
                            return null;
                        }

                        //show calibrated image
//                        if (DEVELOP_MODE) {
//                            Mat rgb = new Mat();
//                            Imgproc.cvtColor(calibrationMat, rgb, Imgproc.COLOR_Lab2RGB);
//                            Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
//                            Utils.matToBitmap(rgb, bitmap);
//                            if (FileUtil.isExternalStorageWritable()) {
//                                FileUtil.writeBitmapToExternalStorage(bitmap, "/warp", UUID.randomUUID().toString() + "_cal.png");
//                            }
//                            //Bitmap.createScaledBitmap(bitmap, BITMAP_SCALED_WIDTH, BITMAP_SCALED_HEIGHT, false);
//                        }

                        // cut out black area that contains the strip
                        Mat stripArea = null;
                        if (roiStripArea != null) {
                            stripArea = calibrationMat.submat(roiStripArea);
                        }

                        if (stripArea != null) {
                            Mat strip = null;
                            try {
                                StripTest.Brand brand = stripTest.getBrand(uuid);
                                strip = OpenCVUtil.detectStrip(stripArea, brand, ratioW, ratioH);
                            } catch (Exception e) {
                                Timber.e(e);
                            }

                            String error = "";
                            if (strip != null) {
                                labStrip = strip.clone();
                            } else {
                                if (context != null) {
                                    Timber.e(context.getString(R.string.error_calibrating));
                                }
                                labStrip = stripArea.clone();

                                error = Constant.ERROR;

                                //draw a red cross over the image
                                Scalar red = RED_LAB_COLOR; // Lab color
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
                                byte[] rows = FileUtil.leIntToByteArray(labStrip.rows());
                                byte[] cols = FileUtil.leIntToByteArray(labStrip.cols());

                                // append them to the end of the array, in order rows, cols
                                System.arraycopy(matByteArray, 0, payload, 0, dataSize);
                                System.arraycopy(rows, 0, payload, dataSize, 4);
                                System.arraycopy(cols, 0, payload, dataSize + 4, 4);
                                FileUtil.writeByteArray(context, payload, Constant.STRIP + imageNo + error);
                            } catch (Exception e) {
                                Timber.e(e);
                            }
                        }
                    }
                }
            } catch (@NonNull JSONException | IOException e) {

                if (context != null) {
                    Timber.e(context.getString(R.string.error_cut_out_strip));
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (listener != null) {
            listener.showResults();
        }
    }

    // Creates a lab image out of the original YUV preview data
    // first casts to RGB, as we can't cast to LAB directly using openCV
    private Mat makeLab(byte[] data) {
        if (format == ImageFormat.NV21) {
            //convert preview data to Mat object in CIELab format
            Mat rgb = new Mat(height, width, CvType.CV_8UC3);
            Mat labImg = new Mat(height, width, CvType.CV_8UC3);
            Mat previewMat = new Mat(height + height / 2, width, CvType.CV_8UC1);
            previewMat.put(0, 0, data);
            Imgproc.cvtColor(previewMat, rgb, Imgproc.COLOR_YUV2RGB_NV21, rgb.channels());
            Imgproc.cvtColor(rgb, labImg, Imgproc.COLOR_RGB2Lab, rgb.channels());

            return labImg;
        }
        return null;
    }

    private void warp(@NonNull Mat labImg, int i) throws IOException, JSONException {

        String jsonInfo = FileUtil.readFromInternalStorage(context, Constant.INFO + i);
        if (jsonInfo == null) {
            throw new IOException("no finder pattern info");
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

        warpMat = OpenCVUtil.perspectiveTransform(topLeft, topRight, bottomLeft, bottomRight, labImg);
    }

    private void divideIntoCalibrationAndStripArea() throws CalibrationException {
        CalibrationData data = CalibrationCard.readCalibrationFile();

        if (warpMat != null && data != null) {
            double hSize = data.hSize;
            double vSize = data.vSize;
            double[] area = data.getStripArea();

            if (area.length == 4) {
                ratioW = warpMat.width() / hSize;
                ratioH = warpMat.height() / vSize;
                Point stripTopLeft = new Point(area[0] * ratioW + Constant.PIXEL_MARGIN_STRIP_AREA_WIDTH,
                        area[1] * ratioH + Constant.PIXEL_MARGIN_STRIP_AREA_HEIGHT);
                Point stripBottomRight = new Point(area[2] * ratioW - Constant.PIXEL_MARGIN_STRIP_AREA_WIDTH,
                        area[3] * ratioH - Constant.PIXEL_MARGIN_STRIP_AREA_HEIGHT);

                //strip area rect
                roiStripArea = new org.opencv.core.Rect(stripTopLeft, stripBottomRight);

                //cal area rect
//                org.opencv.core.Rect roiCalArea = new org.opencv.core.Rect(new Point(0, 0),
//                        new Point(warpMat.width(), area[1] * ratioH));
            }
        }
    }

    @Nullable
    private CalibrationResultData getCalibratedImage(Mat mat) throws CalibrationException {
        CalibrationData data = CalibrationCard.readCalibrationFile();
        return CalibrationCard.calibrateImage(mat, data);
    }
}
