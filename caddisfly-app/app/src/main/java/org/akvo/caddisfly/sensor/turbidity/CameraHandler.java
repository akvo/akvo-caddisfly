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

package org.akvo.caddisfly.sensor.turbidity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import org.akvo.caddisfly.helper.FileHelper;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
class CameraHandler implements Camera.PictureCallback {

    private final PowerManager.WakeLock wakeLock;
    private final Context mContext;
    private Camera mCamera;
    private String mSavePath;

    public CameraHandler(Context context) {
        mContext = context;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "CameraSensorWakeLock");

        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WakeLock");
        wakeLock.acquire();

    }

    public Boolean takePicture(String savePath) {

        mSavePath = savePath;
        try {

            if (mCamera == null) {
                mCamera = Camera.open();
            }
            mCamera.enableShutterSound(false);

            try {
                mCamera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception ex) {
                return false;
            }

            setCamera(mCamera);
            mCamera.startPreview();
            try {
                Thread.sleep(3000);
            } catch (Exception ignored) {
            }

            mCamera.takePicture(null, null, this);

            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                }
            }, 10000);

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        mCamera.release();
        mCamera = null;
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        saveImage(data);

        try {
            Thread.sleep(2000);
        } catch (Exception ignored) {
        }
    }

    private void setCamera(Camera camera) {
        mCamera = camera;
        List<String> mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
        Camera.Parameters parameters = mCamera.getParameters();

        List<String> supportedWhiteBalance = mCamera.getParameters().getSupportedWhiteBalance();
        if (supportedWhiteBalance != null && supportedWhiteBalance.contains(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT)) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
        }

        List<String> supportedSceneModes = mCamera.getParameters().getSupportedSceneModes();
        if (supportedSceneModes != null && supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO)) {
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }

        List<String> supportedColorEffects = mCamera.getParameters().getSupportedColorEffects();
        if (supportedColorEffects != null && supportedColorEffects.contains(Camera.Parameters.EFFECT_NONE)) {
            parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
        }

        List<Integer> supportedPictureFormats = mCamera.getParameters().getSupportedPictureFormats();
        if (supportedPictureFormats != null && supportedPictureFormats.contains(ImageFormat.JPEG)) {
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
        }

        if (mSupportedFlashModes != null) {
            if (mSupportedFlashModes.contains((Camera.Parameters.FLASH_MODE_ON))) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
        }

        parameters.setZoom(0);
        parameters.setRotation(90);

        parameters.setPictureSize(1280, 960);

        try {
            mCamera.setParameters(parameters);
        } catch (Exception ex) {
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();

            parameters.setPictureSize(
                    supportedPictureSizes.get(supportedPictureSizes.size() - 1).width,
                    supportedPictureSizes.get(supportedPictureSizes.size() - 1).height);

            for (Camera.Size size : supportedPictureSizes) {
                if (size.width > 400 && size.width < 1000) {
                    parameters.setPictureSize(size.width, size.height);
                    break;
                }
            }

            mCamera.setParameters(parameters);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void saveImage(byte[] data) {

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, intentFilter);
        int batteryPercent = -1;

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPercent = (int) ((level / (float) scale) * 100);
        }

        int blurriness = getBlurriness(data);

        String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date());

        String fileName = date + "_" + blurriness + "_" + batteryPercent;

        File folder = FileHelper.getFilesDir(FileHelper.FileType.TURBIDITY_IMAGE, mSavePath);

        File photo = new File(folder, fileName + ".jpg");

        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());

            fos.write(data);
            fos.close();
        } catch (Exception ignored) {

        }

        Intent intent = new Intent("custom-event-name");
        intent.putExtra(Intent.EXTRA_TEXT, mSavePath);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private int getBlurriness(byte[] data) {

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inDither = true;
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        int l = CvType.CV_8UC1; //8-bit grey scale image
        Mat matImage = new Mat();
        Utils.bitmapToMat(image, matImage);
        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

        Bitmap destImage;
        destImage = Bitmap.createBitmap(image);
        Mat dst2 = new Mat();
        Utils.bitmapToMat(destImage, dst2);
        Mat laplacianImage = new Mat();
        dst2.convertTo(laplacianImage, l);
        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
        Mat laplacianImage8bit = new Mat();
        laplacianImage.convertTo(laplacianImage8bit, l);

        Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
                laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(laplacianImage8bit, bmp);
        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        int maxLap = -16777216;

        for (int pixel : pixels) {
            if (pixel > maxLap)
                maxLap = pixel;
        }

        return maxLap;
    }
}