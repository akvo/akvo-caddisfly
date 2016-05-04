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
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
class CameraHandler implements Camera.PictureCallback {

    private final PowerManager.WakeLock wakeLock;
    private final Context mContext;
    private final String mTestCode;
    private Camera mCamera;
    private String mSavePath;

    public CameraHandler(Context context, String testCode) {
        mContext = context;
        mTestCode = testCode;

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

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

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

        List<String> focusModes = parameters.getSupportedFocusModes();

        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else {
            // Attempt to set focus to infinity if supported
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            }
        }

        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            Rect areaRect1 = new Rect(-100, -100, 100, 100);
            meteringAreas.add(new Camera.Area(areaRect1, 1000));
            parameters.setMeteringAreas(meteringAreas);
        }

        if (mSupportedFlashModes != null) {
            if (mSupportedFlashModes.contains((Camera.Parameters.FLASH_MODE_ON))) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
        }

        parameters.setExposureCompensation(-2);

        parameters.setZoom(0);

        //parameters.setRotation(90);

        //parameters.setPictureSize(1280, 960);

        parameters.setPictureSize(640, 480);

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

        String date = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date());

        String fileName = "";
        File folder = null;

        switch (mTestCode) {
            case "fluor":

                Bitmap bitmap = ImageUtil.getBitmap(data);

                Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                        ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, true);

                //Extract the color from the photo which will be used for comparison
                ColorInfo photoColor = ColorUtil.getColorFromBitmap(croppedBitmap,
                        ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                CaddisflyApp.getApp().setDefaultTest();
                TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
                ResultDetail resultDetail = SwatchHelper.analyzeColor(testInfo.getSwatches().size(), photoColor,
                        testInfo.getSwatches(), ColorUtil.DEFAULT_COLOR_MODEL);
                fileName = date + "_" + resultDetail.getResult() + "_" + batteryPercent;

                folder = FileHelper.getFilesDir(FileHelper.FileType.FLUORIDE_IMAGE, mSavePath);

                break;

            case "colif":

                bitmap = ImageUtil.getBitmap(data);
                bitmap = ImageUtil.rotateImage(bitmap, 90);
                bitmap = ImageUtil.getCroppedBitmap(bitmap, 180, false);

                Bitmap croppedGrayBitmap = getGrayscale(bitmap);
                int[] brightnessValues = getContrast(croppedGrayBitmap);

                int blackIndex = 0;
                for (int i = 0; i < brightnessValues.length; i++) {
                    if (brightnessValues[blackIndex] > brightnessValues[i]) {
                        blackIndex = i;
                    }
                }

                int whiteIndex = 0;
                for (int i = 0; i < brightnessValues.length; i++) {
                    if (brightnessValues[whiteIndex] < brightnessValues[i]) {
                        whiteIndex = i;
                    }
                }

                fileName = date + "_" + blackIndex + "_" + whiteIndex + "_" + batteryPercent;

                folder = FileHelper.getFilesDir(FileHelper.FileType.TURBIDITY_IMAGE, mSavePath);

                break;
        }


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

    private Bitmap getGrayscale(Bitmap src) {

        //Custom color matrix to convert to GrayScale
        float[] matrix = new float[]{
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0,};

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(),
                src.getHeight(),
                src.getConfig());

        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);

        return dest;
    }

    public int[] getContrast(Bitmap image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int posX1 = width / 4;
        int posX2 = posX1 * 3;

        int posY1 = height / 4;
        int posY2 = posY1 * 3;


        int pixel1 = ColorUtil.getBrightness(image.getPixel(posX1, posY1));
        int pixel2 = ColorUtil.getBrightness(image.getPixel(posX2, posY1));
        int pixel3 = ColorUtil.getBrightness(image.getPixel(posX1, posY2));
        int pixel4 = ColorUtil.getBrightness(image.getPixel(posX2, posY2));

        return new int[]{pixel1, pixel2, pixel3, pixel4};

    }
}