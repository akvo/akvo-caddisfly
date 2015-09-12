package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.akvo.akvoqr.detector.CameraConfigurationUtils;

import java.util.List;

/**
 * Created by linda on 7/7/15.
 */
public class BaseCameraView extends SurfaceView implements SurfaceHolder.Callback{

    /** A basic Camera preview class */

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private MyPreviewCallback previewCallback;
    private CameraActivity activity;
    private Context context;
    private Camera.Parameters parameters;

    public BaseCameraView(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        activity = (CameraActivity) context;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        previewCallback = MyPreviewCallback.getInstance(context);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("", "Error setting camera preview: " + e.getMessage());
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        if(mCamera == null)
        {
            //Camera was released
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        try {
            parameters = mCamera.getParameters();
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        if(parameters == null)
        {
            return;
        }
        Camera.Size bestSize = null;
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
        int maxWidth = 0;
        for(Camera.Size size: sizes) {
            if(size.width>800)
               continue;
            if (size.width > maxWidth) {
                bestSize = size;
                maxWidth = size.width;
            }

            double ratio = (double)size.width/(double)size.height;
            System.out.println("***supported size: " + size.width + ", " + size.height + " ratio: " + String.format("%.2f", ratio));

        }
        System.out.print("***standard preview size: " + parameters.getPreviewSize().width + " , " + parameters.getPreviewSize().height);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;
        int screenWidth = displaymetrics.widthPixels;

        System.out.println("***size w,h: " + screenWidth + ", " + screenHeight);

        Point screenResolution = new Point(screenWidth, screenHeight);

        Point bestResolution = CameraConfigurationUtils.findBestPreviewSizeValue(mCamera.getParameters(), screenResolution);
        parameters.setPreviewSize(bestResolution.x, bestResolution.y);

        parameters.setPictureSize(bestSize.width, bestSize.height);
//        parameters.setPictureFormat(ImageFormat.JPEG);
        System.out.println("***bestsize: " + bestSize.width + ", " + bestSize.height);

        boolean canAutoFocus = false;
        boolean disableContinuousFocus = true;
        List<String> modes = mCamera.getParameters().getSupportedFocusModes();
        for(String s: modes) {
            System.out.println("***supported mode: " + s);
            if(s.equals(Camera.Parameters.FOCUS_MODE_AUTO))
            {
                canAutoFocus = true;
            }
            if(s.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            {
                disableContinuousFocus = false;
            }
        }

        CameraConfigurationUtils.setFocus(parameters, canAutoFocus, disableContinuousFocus, false);

        // start preview with new settings
        try {

            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

            mCamera.setOneShotPreviewCallback(previewCallback);

        } catch (Exception e){
            Log.d("", "Error starting camera preview: " + e.getMessage());
        }

    }
}

