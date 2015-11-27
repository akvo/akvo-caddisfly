package org.akvo.akvoqr;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
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
    private CameraActivity activity;
    private Camera.Parameters parameters;

    public BaseCameraView(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        try {
            activity = (CameraActivity) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("must have CameraActivity as Context.");
        }
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

        //preview size
        System.out.println("***best preview size w, h: " + bestSize.width + ", " + bestSize.height);
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

        //flashmode
        //switchFlashMode();

        //white balance
        if(parameters.getWhiteBalance()!=null)
        {
            //TODO check if this optimise the code
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }

        // qualityChecksOK preview with new settings
        try {

            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);

            activity.setPreviewProperties();

        } catch (Exception e){
            Log.d("", "Error starting camera preview: " + e.getMessage());
        }

    }

    public void switchFlashMode()
    {
        if(mCamera==null)
            return;
        parameters = mCamera.getParameters();

        String flashmode = mCamera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)?
                Camera.Parameters.FLASH_MODE_TORCH: Camera.Parameters.FLASH_MODE_OFF;
        parameters.setFlashMode(flashmode);

        mCamera.setParameters(parameters);
    }

    //exposure compensation
    public void adjustExposure(int direction)
    {
        if(mCamera==null)
            return;
        parameters = mCamera.getParameters();

        int compPlus = Math.min(parameters.getMaxExposureCompensation(), Math.round(parameters.getExposureCompensation() + 1));
        int compMinus = Math.max(parameters.getMinExposureCompensation(), Math.round(parameters.getExposureCompensation() - 1));

        if(direction > 0)
        {
            parameters.setExposureCompensation(compPlus);
        }
        else if(direction < 0)
        {
            parameters.setExposureCompensation(compMinus);
        }
        else if(direction == 0) {
            parameters.setExposureCompensation(0);
        }

        //System.out.println("***Exposure compensation index: " + parameters.getExposureCompensation());

        mCamera.setParameters(parameters);
    }

    public void setFocusAreas(List<Camera.Area> areas)
    {
        if(mCamera==null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            if(mCamera.getParameters().getMaxNumFocusAreas() > 0 && areas != null && areas.size() > 0) {
                try {
                    //make sure area list does not exceed max num areas allowed
                    int length = Math.min(areas.size(), mCamera.getParameters().getMaxNumFocusAreas());
                    List<Camera.Area> subAreas = areas.subList(0, length);

                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFocusAreas(subAreas);
                    mCamera.setParameters(parameters);
                } catch (Exception e) {
                    System.out.println("***Exception setting parameters for focus areas.");
                    e.printStackTrace();

                }
            }
        }
    }
}

