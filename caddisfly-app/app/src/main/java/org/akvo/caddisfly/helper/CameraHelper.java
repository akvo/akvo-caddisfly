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

package org.akvo.caddisfly.helper;

import android.content.Context;
import android.hardware.Camera;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import timber.log.Timber;

public final class CameraHelper {

    private static final float ONE_MILLION = 1000000f;

    private CameraHelper() {
    }

    public static int getMaxSupportedMegaPixelsByCamera(Context context) {

        int cameraMegaPixels = 0;

        if (PreferencesUtil.containsKey(context, R.string.cameraMegaPixelsKey)) {
            cameraMegaPixels = PreferencesUtil.getInt(context, R.string.cameraMegaPixelsKey, 0);
        } else {

            Camera camera = ApiUtil.getCameraInstance();
            try {

                // make sure the camera is not in use
                if (camera != null) {
                    Camera.Parameters allParams = camera.getParameters();
                    for (Camera.Size pictureSize : allParams.getSupportedPictureSizes()) {
                        int sizeInMegaPixel = (int) Math.ceil((pictureSize.width * pictureSize.height) / ONE_MILLION);
                        if (sizeInMegaPixel > cameraMegaPixels) {
                            cameraMegaPixels = sizeInMegaPixel;
                        }
                    }
                }

                PreferencesUtil.setInt(context, R.string.cameraMegaPixelsKey, cameraMegaPixels);

            } catch (Exception e) {
                Timber.e(e);
            } finally {
                if (camera != null) {
                    camera.release();
                }
            }
        }
        return cameraMegaPixels;
    }

}
