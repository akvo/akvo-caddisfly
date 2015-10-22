package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.akvo.akvoqr.detector.CameraConfigurationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 7/7/15.
 */
public class BaseCameraView extends SurfaceView implements SurfaceHolder.Callback{

    /** A basic Camera preview class */

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private CameraActivity activity;
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


    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);

        } catch (Exception e) {
            Log.d("", "Error setting camera preview: " + e.getMessage());
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {

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
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        int maxWidth = 0;
        for(Camera.Size size: sizes) {
            System.out.println("***supported preview sizes w, h: " + size.width + ", " + size.height);
            if(size.width>1300)
               continue;
            if (size.width > maxWidth) {
                bestSize = size;
                maxWidth = size.width;
            }
        }

        //portrait mode
        mCamera.setDisplayOrientation(90);
        //parameters.setRotation(90);
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        //parameters.setPreviewFormat(ImageFormat.NV21);

        boolean canAutoFocus = false;
        boolean disableContinuousFocus = true;
        List<String> modes = mCamera.getParameters().getSupportedFocusModes();
        for(String s: modes) {

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            List<Camera.Area> areas = new ArrayList<>();
            Rect roi = new Rect(bestSize.width/3, bestSize.height/3, bestSize.width/3*2, bestSize.height/3*2);
            areas.add(new Camera.Area(roi, 1));
            parameters.setFocusAreas(areas);
        }

        // start preview with new settings
        try {

            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);

            activity.getMessage(0);

        } catch (Exception e){
            Log.d("", "Error starting camera preview: " + e.getMessage());
        }

    }
}

