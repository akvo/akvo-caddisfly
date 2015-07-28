/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.usb;

import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import org.akvo.caddisfly.usb.USBMonitor.UsbControlBlock;

public class UVCCamera {

    public static final int DEFAULT_PREVIEW_MODE = 0;
    public static final int PIXEL_FORMAT_RAW = 0;
    private static final int DEFAULT_PREVIEW_WIDTH = 640;
    private static final int DEFAULT_PREVIEW_HEIGHT = 480;
    private static final String TAG = UVCCamera.class.getSimpleName();
    private static final String DEFAULT_USBFS = "/dev/bus/com.serenegiant.usb";
    private static boolean isLoaded;

    static {
        if (!isLoaded) {
            System.loadLibrary("usb100");
            System.loadLibrary("uvc");
            System.loadLibrary("UVCCamera");
            isLoaded = true;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected long mNativePtr; // this field is accessed from native code do not change
    private UsbControlBlock mCtrlBlock;

    /**
     * the constructor of this class should be call within the thread that has a looper
     * (UI thread or a thread that called Looper.prepare)
     */
    public UVCCamera() {
        mNativePtr = nativeCreate();
    }

    @SuppressWarnings("JniMissingFunction")
    private static native int nativeConnect(long id_camera, int vendorId, int productId, int fileDescriptor, String usbfs);

    @SuppressWarnings("JniMissingFunction")
    private static native int nativeRelease(long id_camera);

    @SuppressWarnings("JniMissingFunction")
    private static native int nativeSetPreviewSize(long id_camera, int width, int height, int mode);

    @SuppressWarnings("JniMissingFunction")
    private static native int nativeStartPreview(long id_camera);

    @SuppressWarnings("JniMissingFunction")
    private static native int nativeStopPreview(long id_camera);

    @SuppressWarnings("JniMissingFunction")
    private static native int nativeSetPreviewDisplay(long id_camera, Surface surface);

    //private static native int nativeSetFrameCallback(long mNativePtr, IFrameCallback callback, int pixelFormat);

    //private static native int nativeSetCaptureDisplay(long id_camera, Surface surface);

    /**
     * connect to a UVC camera
     * USB permission is necessary before this method is called
     *
     * @param ctrlBlock
     */
    public void open(final UsbControlBlock ctrlBlock) {
        mCtrlBlock = ctrlBlock;
        nativeConnect(mNativePtr,
                mCtrlBlock.getVendorId(), mCtrlBlock.getProductId(),
                mCtrlBlock.getFileDescriptor(),
                getUSBFSName(mCtrlBlock));
        nativeSetPreviewSize(mNativePtr, DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT, DEFAULT_PREVIEW_MODE);
    }

    /**
     * close and release UVC camera
     */
    private void close() {
        stopPreview();
        if (mNativePtr != 0) {
            nativeRelease(mNativePtr);
        }
        mCtrlBlock = null;
    }

    /**
     * Set preview size and preview mode
     *
     * @param width
     * @param height
     * @param mode   0:yuyv, other:MJPEG
     */
    @SuppressWarnings("SameParameterValue")
    public void setPreviewSize(final int width, final int height, final int mode) {
        if ((width == 0) || (height == 0))
            throw new IllegalArgumentException("invalid preview size");
        if (mNativePtr != 0) {
            final int result = nativeSetPreviewSize(mNativePtr, width, height, mode);
            if (result != 0)
                throw new IllegalArgumentException("Failed to set preview size");
        }
    }

    /**
     * set preview surface with SurfaceHolder</br>
     * you can use SurfaceHolder came from SurfaceView/GLSurfaceView
     *
     * @param holder
     */
    public void setPreviewDisplay(final SurfaceHolder holder) {
        nativeSetPreviewDisplay(mNativePtr, holder.getSurface());
    }

    /**
     * set preview surface with SurfaceTexture.
     * this method require API >= 14
     *
     * @param texture
     */
    public void setPreviewTexture(final SurfaceTexture texture) {    // API >= 11
        final Surface surface = new Surface(texture);    // XXX API >= 14
        nativeSetPreviewDisplay(mNativePtr, surface);
    }

    /**
     * set preview surface with Surface
     *
     * @param surface the surface to display
     */
    public void setPreviewDisplay(final Surface surface) {
        nativeSetPreviewDisplay(mNativePtr, surface);
    }

    /**
     * set frame callback
     *
     * @param callback
     * @param pixelFormat
     */
//    private void setFrameCallback(final IFrameCallback callback, final int pixelFormat) {
//        if (mNativePtr != 0) {
//            nativeSetFrameCallback(mNativePtr, callback, pixelFormat);
//        }
//    }

    /**
     * start preview
     */
    public void startPreview() {
        if (mCtrlBlock != null) {
            nativeStartPreview(mNativePtr);
        }
    }

    /**
     * stop preview
     */
    public void stopPreview() {
        //setFrameCallback(null, 0);
        if (mCtrlBlock != null) {
            nativeStopPreview(mNativePtr);
        }
    }

    /**
     * destroy UVCCamera object
     */
    public void destroy() {
        close();
        if (mNativePtr != 0) {
            nativeDestroy(mNativePtr);
            mNativePtr = 0;
        }
    }

    private String getUSBFSName(final UsbControlBlock ctrlBlock) {
        String result = null;
        final String name = ctrlBlock.getDeviceName();
        final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
        if ((v != null) && (v.length > 2)) {
            final StringBuilder sb = new StringBuilder(v[0]);
            for (int i = 1; i < v.length - 2; i++)
                sb.append("/").append(v[i]);
            result = sb.toString();
        }
        if (TextUtils.isEmpty(result)) {
            Log.w(TAG, "failed to get USBFS path, try to use default path:" + name);
            result = DEFAULT_USBFS;
        }
        return result;
    }

    // #nativeCreate and #nativeDestroy are not static methods.
    @SuppressWarnings("JniMissingFunction")
    private native long nativeCreate();

//**********************************************************************

    @SuppressWarnings("JniMissingFunction")
    private native void nativeDestroy(long id_camera);

    /**
     * start movie capturing(this should call while previewing)
     *
     * @param surface
     */
//    public void startCapture(final Surface surface) {
//        if (mCtrlBlock != null && surface != null) {
//            nativeSetCaptureDisplay(mNativePtr, surface);
//        } else
//            throw new NullPointerException("startCapture");
//    }
//
//    /**
//     * stop movie capturing
//     */
//    public void stopCapture() {
//        if (mCtrlBlock != null) {
//            nativeSetCaptureDisplay(mNativePtr, null);
//        }
//    }

}
