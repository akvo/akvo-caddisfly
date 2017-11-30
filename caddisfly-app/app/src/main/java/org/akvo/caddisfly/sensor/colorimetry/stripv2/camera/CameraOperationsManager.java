package org.akvo.caddisfly.sensor.colorimetry.stripv2.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.akvo.caddisfly.sensor.colorimetry.stripv2.ui.StripMeasureActivity;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.ui.StriptestHandler;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.MessageUtils;

/**
 * Created by markwestra on 19/07/2017
 */

public class CameraOperationsManager {
    private static final long AUTO_FOCUS_DELAY = 4000L;

//    private final Context context;

    // An additional thread for running camera tasks that shouldn't block the UI.
    private HandlerThread mCameraThread;

    // A Handler for running camera tasks in the background.
    private Handler mCameraHandler;

    private Camera mCamera;

    private boolean changingExposure = false;
    private StriptestHandler mStriptestHandler;
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] imageData, Camera arg1) {
            // store image for later use
            StriptestHandler.mDecodeData.setDecodeImageByteArray(imageData);
            MessageUtils.sendMessage(mStriptestHandler, StriptestHandler.DECODE_IMAGE_CAPTURED_MESSAGE, 0);
        }
    };
    private Runnable runAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null) {
                //mCamera.cancelAutoFocus();
                if (!changingExposure) {

                    mCamera.autoFocus((success, camera) -> {
                        // do Nothing
                    });
                }
                mCameraHandler.postDelayed(runAutoFocus, AUTO_FOCUS_DELAY);
            }
        }
    };

    public CameraOperationsManager() {
//        this.configManager = new CameraConfigurationManager();
//        this.context = context;
    }

    public CameraPreview initCamera(Context context) {
        startCameraThread();

        // open the camera and create a preview surface for it
        CameraPreview cameraPreview = new CameraPreview(context);
        mCamera = cameraPreview.getCamera();
        return cameraPreview;
    }

    public void setStriptestHandler(StriptestHandler striptestHandler) {
        this.mStriptestHandler = striptestHandler;
    }

    // TODO add cancel request

    public void setDecodeImageCaptureRequest() {
        if (mCameraHandler != null && mCamera != null) {
            mCameraHandler.post(() -> mCamera.setOneShotPreviewCallback(previewCallback));
        }
    }

    ///////////////////////////// autofocus handling ////////////////////////////////////////

    // TODO check if these boundaries are ok
    public void changeExposure(int exposureChange) {
        int expComp = mCamera.getParameters().getExposureCompensation();
        int newSetting = expComp + exposureChange;

        // if we are within bounds, change the capture request
        if (newSetting != expComp &&
                newSetting <= mCamera.getParameters().getMaxExposureCompensation() &&
                newSetting >= mCamera.getParameters().getMinExposureCompensation()) {
            changingExposure = true;
            mCamera.stopPreview();
            mCamera.cancelAutoFocus();
            stopAutoFocus();
            Camera.Parameters cameraParam = mCamera.getParameters();
            cameraParam.setExposureCompensation(newSetting);
            mCamera.setParameters(cameraParam);
            mCamera.startPreview();
            changingExposure = false;
            startAutofocus();
        }
    }

    public void startAutofocus() {
        if (mCameraHandler != null) {
            mCameraHandler.removeCallbacks(runAutoFocus);
            mCameraHandler.postDelayed(runAutoFocus, AUTO_FOCUS_DELAY);
        } else {
            throw new RuntimeException("can't start autofocus");
        }
    }

    public void stopAutoFocus() {
        if (mCameraHandler != null) {
            mCameraHandler.removeCallbacks(runAutoFocus);
        }
    }

    public void stopCamera() {
        mCamera = null;
        mCameraHandler = null;
    }
    ////////////////////////////////////////// background thread ///////////////////////////////////

    /**
     * Starts a background thread and its Handler.
     */
    private void startCameraThread() {
        if (StripMeasureActivity.DEBUG) {
            Log.d("Caddisfly", "Starting camera background thread");
        }
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

//    /**
//     * Stops the background thread and its {@link Handler}.
//     */
//    private void stopCameraThread() {
//        if (StripMeasureActivity.DEBUG) {
//            Log.d("Caddisfly", "Stopping camera background thread");
//        }
//        mCameraThread.quitSafely();
//        try {
//            mCameraThread.join();
//            mCameraThread = null;
//            mCameraHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
