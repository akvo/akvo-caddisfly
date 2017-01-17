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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import org.akvo.caddisfly.util.detector.FinderPatternInfo;


/**
 * Created by linda on 6/26/15.
 * <p>
 * This class is meant to be called in the setOnShotCameraPreviewCallback(Camera.PreviewCallback)
 * method of a class that holds an instance of the Android Camera.
 * <p>
 * In the AsyncTask called 'SendDataTask', executed in every onPreviewFrame(),
 * this happens:
 * - find the FinderPatterns on the card
 * - do quality checks regarding luminosity, shadows and perspective
 * - communicate the result of finder patterns to the listener (==CameraActivity)
 * - communicate the result of quality checks to the listener
 * <p>
 * Depending on the instance of the Fragment that is inflated by CameraActivity at this instance,
 * the global boolean 'takePicture' is set.
 * Fragment instance of CameraPrepareFragment -> false
 * Fragment instance of CameraStartTestFragment -> true
 * <p>
 * If conditions under which to take a picture (==store Preview data in internal storage) fail,
 * communicate to the listener that it calls this class again,
 * - if we are in the 'takePicture' Fragment, call it like that
 * - if we are in the 'prepare' Fragment, call it like that
 * <p>
 * The conditions under which to take a picture are:
 * - 'takePicture' must be true
 * - FinderPatternInfo object must not be null
 * - listener.countQualityOK must be true
 * <p>
 * if takePicture is false, we tell the listener to call this callback again
 * with the startNextPreview() method.
 */
@SuppressWarnings("deprecation")
class CameraCallbackTakePicture extends CameraCallbackBase {

    private static final String TAG = "CamCallbackTakePicture";

    private static final int TAKE_PICTURE_DELAY_MILLIS = 500;
    private boolean sending;

    CameraCallbackTakePicture(Context context, Camera.Parameters parameters) {
        super(context, parameters);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        super.onPreviewFrame(data, camera);

        if (isRunning() && !sending) {
            sendData(data);
        }
    }

    private void sendData(byte[] data) {
        sending = true;

        CameraViewListener listener = getListener();
        try {
            if (listener != null) {
                FinderPatternInfo info = findPossibleCenters(data, getPreviewSize());

                // Get quality count and update UI via listener
                int[] countQuality = qualityChecks(data, info);

                if (info != null) {
                    listener.addCountToQualityCheckCount(countQuality);
                } else {
                    listener.showLevel(null);
                }

                //sumQuality should amount to 3, if all checks are OK: [1,1,1]
                int sumQuality = 0;
                if (countQuality != null) {
                    for (int i : countQuality) {
                        sumQuality += i;
                    }
                }

                if (info != null && sumQuality == 3 && listener.qualityChecksOK()) {

                    listener.playSound();

                    listener.sendData(data, System.currentTimeMillis(), info);

                    listener.startNextPreview();
                } else {
                    listener.takeNextPicture(TAKE_PICTURE_DELAY_MILLIS);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            listener.showError(e.getMessage());
            listener.takeNextPicture(TAKE_PICTURE_DELAY_MILLIS);
        } finally {
            sending = false;
        }
    }
}
