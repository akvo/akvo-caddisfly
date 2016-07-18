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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.hardware.Camera;

import org.akvo.caddisfly.util.detector.FinderPatternInfo;


/**
 * Created by linda on 6/26/15.
 * <p/>
 * This class is meant to be called in the setOnShotCameraPreviewCallback(Camera.PreviewCallback)
 * method of a class that holds an instance of the Android Camera.
 * <p/>
 * In the AsyncTask called 'SendDataTask', executed in every onPreviewFrame(),
 * this happens:
 * - find the FinderPatterns on the card
 * - do quality checks regarding luminosity, shadows and perspective
 * - communicate the result of finder patterns to the listener (==CameraActivity)
 * - communicate the result of quality checks to the listener
 * <p/>
 * Depending on the instance of the Fragment that is inflated by CameraActivity at this instance,
 * the global boolean 'takePicture' is set.
 * Fragment instance of CameraPrepareFragment -> false
 * Fragment instance of CameraStartTestFragment -> true
 * <p/>
 * If conditions under which to take a picture (==store Preview data in internal storage) fail,
 * communicate to the listener that it calls this class again,
 * - if we are in the 'takePicture' Fragment, call it like that
 * - if we are in the 'prepare' Fragment, call it like that
 * <p/>
 * The conditions under which to take a picture are:
 * - 'takePicture' must be true
 * - FinderPatternInfo object must not be null
 * - listener.countQualityOK must be true
 * <p/>
 * if takePicture is false, we tell the listener to call this callback again
 * with the startNextPreview() method.
 */
@SuppressWarnings("deprecation")
class CameraCallbackTakePicture extends CameraCallbackAbstract {
    private boolean running;

    public CameraCallbackTakePicture(Context context, Camera.Parameters parameters) {
        super(context, parameters);
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
            //if countQuality sums up to the limit set in Constant,
            //listener.qualityChecksOK will return true;
            if (listener != null)
                listener.addCountToQualityCheckCount(countQuality);

            //sumQuality should amount to 3, if all checks are OK: [1,1,1]
            int sumQuality = 0;
            if (countQuality != null) {
                for (int i : countQuality) {
                    sumQuality += i;
                }
            }

            if (listener != null) {
                if (info != null && sumQuality == 3 && listener.qualityChecksOK()) {
                    long timePictureTaken = System.currentTimeMillis();

                    //freeze the screen and play a sound
                    //camera.stopPreview();
                    listener.playSound();

                    //System.out.println("***!!!CameraPreviewCallback takePicture true: " + countInstance);
                    listener.sendData(data, timePictureTaken, info);

                    listener.startNextPreview();
                } else {
                    listener.takeNextPicture(500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.takeNextPicture(500);
        } finally {
            running = false;
        }
    }
}




