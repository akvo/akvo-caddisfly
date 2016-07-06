package org.akvo.caddisfly.sensor.colorimetry.strip.camera_strip;

import android.content.Context;
import android.hardware.Camera;

import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.FinderPatternInfo;


/**
 * Created by linda on 6/26/15.
 * <p>
 * This class is meant to be called in the setOnShotCameraPreviewCallback(Camera.PreviewCallback)
 * method of a class that holds an instance of the Android Camera.
 */
@SuppressWarnings("deprecation")
class CameraPreviewCallbackSP extends CameraPreviewCallbackAbstract {
    private boolean stop;
    private boolean running;

    public CameraPreviewCallbackSP(Context context, Camera.Parameters parameters) {
        super(context, parameters);
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        super.onPreviewFrame(data, camera);

        if (!stop && !running)
            sendData(data);
    }

    protected void sendData(byte[] data) {
        running = true;
        try {
            FinderPatternInfo info = findPossibleCenters(data, previewSize);

            //check if quality of image is ok. if OK, value is 1, if not 0
            //the qualityChecks() method sends messages back to listener to update UI
            int[] countQuality = qualityChecks(data, info);

            //add countQuality to sum in listener
            if (listener != null)
                listener.addCountToQualityCheckCount(countQuality);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            running = false;
        }
    }
}




