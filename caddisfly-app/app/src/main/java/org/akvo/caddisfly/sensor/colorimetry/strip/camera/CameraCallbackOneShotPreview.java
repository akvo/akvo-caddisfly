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

import org.akvo.caddisfly.util.detector.FinderPatternInfo;


/**
 * Created by linda on 6/26/15.
 * <p/>
 * This class is meant to be called in the setOnShotCameraPreviewCallback(Camera.PreviewCallback)
 * method of a class that holds an instance of the Android Camera.
 */
@SuppressWarnings("deprecation")
class CameraCallbackOneShotPreview extends CameraCallbackBase {
    private boolean sending;

    CameraCallbackOneShotPreview(Context context, Camera.Parameters parameters) {
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
            FinderPatternInfo info = findPossibleCenters(data, getPreviewSize());

            if (info != null) {

                // Get quality count and update UI via listener
                int[] countQuality = qualityChecks(data, info);

                if (listener != null) {
                    listener.addCountToQualityCheckCount(countQuality);
                }
            } else {
                listener.showLevel(null);
            }

        } catch (Exception e) {
            listener.showError(e.getMessage());
        } finally {
            sending = false;
        }
    }

}
