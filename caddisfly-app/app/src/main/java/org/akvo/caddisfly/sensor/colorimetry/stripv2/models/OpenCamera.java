/*
 * Copyright (C) 2015 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.akvo.caddisfly.sensor.colorimetry.stripv2.models;

import android.hardware.camera2.CameraDevice;
import android.util.Range;
import android.util.Size;

/**
 * Represents an open camera and its metadata
 */

public final class OpenCamera {

    private final String cameraId;
    private final boolean swappedDimensions;
    private final int naturalOrientation;
    private final Size[] surfaceSizeOptions;
    private final Size[] allocationSizeOptions;
    private final Size pixelArraySize;
    private final int AeRegions;
    private final int AfRegions;
    private Size previewSize;
    private Size decodeSize;
    private Size aspectRatio;
    private CameraDevice cameraDevice;
    private Range<Integer> expRange;
    private float expSetting;


    public OpenCamera(String cameraId, Size pixelArraySize, Size[] surfaceSizeOptions,
                      Size[] allocationSizeOptions, boolean swappedDimensions,
                      int naturalOrientation, int AeRegions, int AfRegions, Range<Integer> expRange) {
        this.cameraId = cameraId;
        this.pixelArraySize = pixelArraySize;
        this.surfaceSizeOptions = surfaceSizeOptions;
        this.allocationSizeOptions = allocationSizeOptions;
        this.swappedDimensions = swappedDimensions;
        this.naturalOrientation = naturalOrientation;
        this.AeRegions = AeRegions;
        this.AfRegions = AfRegions;
        this.expRange = expRange;
        this.expSetting = 0f;
    }

    public String getCameraId() {
        return cameraId;
    }

    public boolean getSwappedDimensions() {
        return swappedDimensions;
    }

    public int getNaturalOrientation() {
        return naturalOrientation;
    }

    public Size getPixelArraySize() {
        return pixelArraySize;
    }

    public Size[] getSurfaceSizeOptions() {
        return surfaceSizeOptions;
    }

    public Size[] getAllocationSizeOptions() {
        return allocationSizeOptions;
    }

    public int getAeRegions() {
        return AeRegions;
    }

    public int getAfRegions() {
        return AfRegions;
    }

    public Size getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(int width, int height) {
        this.aspectRatio = new Size(width, height);
    }

    public Size getPreviewSize() {
        return previewSize;
    }

    public void setPreviewSize(Size previewSize) {
        this.previewSize = previewSize;
    }

    public Size getDecodeSize() {
        return decodeSize;
    }

    public void setDecodeSize(Size decodeSize) {
        this.decodeSize = decodeSize;
    }

    public CameraDevice getCameraDevice() {
        return cameraDevice;
    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    public float getExpSetting() {
        return expSetting;
    }

    public void setExpSetting(float expSetting) {
        this.expSetting = expSetting;
    }

    public Range<Integer> getExpRange() {
        return expRange;
    }

    public void setExpRange(Range<Integer> expRange) {
        this.expRange = expRange;
    }

    @Override
    public String toString() {
        return "Camera #" + cameraId;
    }
}
