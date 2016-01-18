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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.usb.UVCCamera;
import org.akvo.caddisfly.widget.CameraViewInterface;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public final class ExternalCameraFragment extends CameraDialog {
    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "ExternalCameraFragment";
    private static final String ARG_PREVIEW_ONLY = "preview";
    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 320;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 240;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_MODE = UVCCamera.PIXEL_FORMAT_RAW;
    private final Handler delayHandler = new Handler();
    private int mNumberOfPhotosToTake;
    private int mPhotoCurrentCount = 0;
    private SoundPoolPlayer sound;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera related methods sequentially on private thread
     */
    private CameraHandler mHandler;
    /**
     * for camera preview display
     */
    private CameraViewInterface mUVCCameraView;
    /**
     * for open&start / stop&close camera preview
     */
    private Surface mSurface;
    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            //Toast.makeText(getActivity(), "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
            mHandler.openCamera(ctrlBlock);
            startPreview();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
            if (mHandler != null) {
                mHandler.closeCamera();
            }
        }

        @Override
        public void onDetach(final UsbDevice device) {
            //Toast.makeText(getActivity(), "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
        }
    };
    private long mSamplingDelay;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param previewOnly if true will display preview only, otherwise start taking pictures
     * @return A new instance of fragment CameraFragment.
     */
    @SuppressWarnings("SameParameterValue")
    public static ExternalCameraFragment newInstance(boolean previewOnly) {
        ExternalCameraFragment fragment = new ExternalCameraFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PREVIEW_ONLY, previewOnly);
        fragment.setArguments(args);
        return fragment;
    }

    public static ExternalCameraFragment newInstance() {
        return new ExternalCameraFragment();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mPreviewOnly = getArguments().getBoolean(ARG_PREVIEW_ONLY);
//        }
        sound = new SoundPoolPlayer(getActivity());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sound.release();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_external_camera, container, false);
        final View cameraView = view.findViewById(R.id.uvcCameraView);
        mUVCCameraView = (CameraViewInterface) cameraView;
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);
        mUSBMonitor = new USBMonitor(getActivity(), mOnDeviceConnectListener);
        mHandler = CameraHandler.createHandler(this, mUVCCameraView);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (getActivity() != null && getActivity() instanceof Cancelled) {
                    ((Cancelled) getActivity()).dialogCancelled();
                }
                dismiss();
            }
        };

//        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getActivity() != null && getActivity() instanceof Cancelled) {
            ((Cancelled) getActivity()).dialogCancelled();
        }
        super.onCancel(dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Runnable delayRunnable = new Runnable() {
            @Override
            public void run() {
                if (mHandler != null && !mHandler.isCameraOpened()) {
                    final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getActivity(), R.xml.camera_device_filter);
                    List<UsbDevice> usbDeviceList = mUSBMonitor.getDeviceList(filter.get(0));
                    if (usbDeviceList.size() > 0) {
                        final Object item = usbDeviceList.get(0);
                        mUSBMonitor.requestPermission((UsbDevice) item);
                    }
                }
            }
        };

        delayHandler.postDelayed(delayRunnable, 500);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume:");
        mUSBMonitor.register();
        if (mUVCCameraView != null)
            mUVCCameraView.onResume();
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:");
        mHandler.closeCamera();
        if (mUVCCameraView != null)
            mUVCCameraView.onPause();
        mUSBMonitor.unregister();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        if (mHandler != null) {
            mHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraView = null;
        super.onDestroy();
    }

    private void startPreview() {
        final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = new Surface(st);
        mHandler.startPreview(mSurface);
    }

    private void takePicture() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (getActivity() != null) {

                    if (mHandler != null && mHandler.isCameraOpened()) {
                        mHandler.captureStill();
                    }
                }
            }
        }, mSamplingDelay);
    }

    public boolean hasTestCompleted() {
        return mPhotoCurrentCount >= mNumberOfPhotosToTake;
    }

    @Override
    public void takePictureSingle() {
        mNumberOfPhotosToTake = 1;
        mPhotoCurrentCount = 0;
        takePicture();
    }

    @Override
    public void takePictures(int count, long delay) {
        mNumberOfPhotosToTake = count;
        mPhotoCurrentCount = 0;
        mSamplingDelay = delay;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takePicture();
            }
        }, delay);
    }

    @Override
    public void stopCamera() {

    }

    /**
     * Handler class to execute camera related methods sequentially on private thread
     */
    private static final class CameraHandler extends Handler {
        private static final int MSG_OPEN = 0;
        private static final int MSG_CLOSE = 1;
        private static final int MSG_PREVIEW_START = 2;
        private static final int MSG_PREVIEW_STOP = 3;
        private static final int MSG_CAPTURE_STILL = 4;
        private static final int MSG_RELEASE = 9;

        private final WeakReference<CameraThread> mWeakThread;

        private CameraHandler(final CameraThread thread) {
            mWeakThread = new WeakReference<>(thread);
        }

        public static CameraHandler createHandler(final ExternalCameraFragment parent, final CameraViewInterface cameraView) {
            final CameraThread thread = new CameraThread(parent, cameraView);
            thread.start();
            return thread.getHandler();
        }

        public boolean isCameraOpened() {
            final CameraThread thread = mWeakThread.get();
            return thread != null && thread.isCameraOpened();
        }

        public void openCamera(final USBMonitor.UsbControlBlock ctrlBlock) {
            sendMessage(obtainMessage(MSG_OPEN, ctrlBlock));
        }

        public void closeCamera() {
            stopPreview();
            sendEmptyMessage(MSG_CLOSE);
        }

        public void startPreview(final Surface surface) {
            if (surface != null)
                sendMessage(obtainMessage(MSG_PREVIEW_START, surface));
        }

        public void stopPreview() {
            final CameraThread thread = mWeakThread.get();
            if (thread == null) return;
            synchronized (thread.mSync) {
                sendEmptyMessage(MSG_PREVIEW_STOP);
                // wait for actually preview stopped to avoid releasing Surface/SurfaceTexture
                // while preview is still running.
                // therefore this method will take a time to execute
                try {
                    thread.mSync.wait();
                } catch (final InterruptedException ignored) {
                }
            }
        }

        public void captureStill() {
            sendEmptyMessage(MSG_CAPTURE_STILL);
        }

        @Override
        public void handleMessage(final Message msg) {
            final CameraThread thread = mWeakThread.get();
            if (thread == null) return;
            switch (msg.what) {
                case MSG_OPEN:
                    thread.handleOpen((USBMonitor.UsbControlBlock) msg.obj);
                    break;
                case MSG_CLOSE:
                    thread.handleClose();
                    break;
                case MSG_PREVIEW_START:
                    thread.handleStartPreview((Surface) msg.obj);
                    break;
                case MSG_PREVIEW_STOP:
                    thread.handleStopPreview();
                    break;
                case MSG_CAPTURE_STILL:
                    thread.handleCaptureStill();
                    break;
                case MSG_RELEASE:
                    thread.handleRelease();
                    break;
                default:
                    throw new RuntimeException("unsupported message:what=" + msg.what);
            }
        }

        private static final class CameraThread extends Thread {
            private static final String TAG_THREAD = "CameraThread";
            private final Object mSync = new Object();
            private final WeakReference<ExternalCameraFragment> mWeakParent;
            private final WeakReference<CameraViewInterface> mWeakCameraView;
            /**
             * shutter sound
             */
            private CameraHandler mHandler;
            /**
             * for accessing UVC camera
             */
            private UVCCamera mUVCCamera;
            //private boolean mCancelled = false;

            private CameraThread(final ExternalCameraFragment parent, final CameraViewInterface cameraView) {
                super("CameraThread");
                mWeakParent = new WeakReference<>(parent);
                mWeakCameraView = new WeakReference<>(cameraView);
            }

            @Override
            protected void finalize() throws Throwable {
                Log.i(TAG, "CameraThread#finalize");
                super.finalize();
            }

            public CameraHandler getHandler() {
                if (DEBUG) Log.v(TAG_THREAD, "getHandler:");
                synchronized (mSync) {
                    if (mHandler == null)
                        try {
                            mSync.wait();
                        } catch (final InterruptedException ignored) {
                        }
                }
                return mHandler;
            }

            public boolean isCameraOpened() {
                return mUVCCamera != null;
            }

            public void handleOpen(final USBMonitor.UsbControlBlock ctrlBlock) {
                if (DEBUG) Log.v(TAG_THREAD, "handleOpen:");
                handleClose();
                mUVCCamera = new UVCCamera();
                mUVCCamera.open(ctrlBlock);
            }

            public void handleClose() {
                if (DEBUG) Log.v(TAG_THREAD, "handleClose:");
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                }
            }

            public void handleStartPreview(final Surface surface) {
                if (DEBUG) Log.v(TAG_THREAD, "handleStartPreview:");
                if (mUVCCamera == null) return;
                try {
                    mUVCCamera.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);
                } catch (final IllegalArgumentException e) {
                    try {
                        // fallback to YUV mode
                        mUVCCamera.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                    } catch (final IllegalArgumentException e1) {
                        handleClose();
                    }
                }
                if (mUVCCamera != null) {
//					mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_YUV);
                    mUVCCamera.setPreviewDisplay(surface);
                    mUVCCamera.startPreview();
                }
            }

            public void handleStopPreview() {
                if (DEBUG) Log.v(TAG_THREAD, "handleStopPreview:");
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                synchronized (mSync) {
                    mSync.notifyAll();
                }
            }


            public void handleCaptureStill() {
                if (DEBUG) Log.v(TAG_THREAD, "handleCaptureStill:");
                final ExternalCameraFragment parent = mWeakParent.get();
                if (parent == null) return;
                //parent.sound.playShortResource(R.raw.beep);
                final Bitmap bitmap = mWeakCameraView.get().captureStillImage();

                parent.mPhotoCurrentCount++;

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                for (PictureTaken pictureTakenObserver : parent.pictureTakenObservers) {
                    pictureTakenObserver.onPictureTaken(byteArray, parent.hasTestCompleted());
                }

                //if (!mCancelled) {
                if (!parent.hasTestCompleted()) {
                    //parent.pictureCallback.onPictureTaken(bitmap);
                    parent.takePicture();
                }
                //}
            }

            public void handleRelease() {
                if (DEBUG) Log.v(TAG_THREAD, "handleRelease:");
                handleClose();
            }


            @Override
            public void run() {
                Looper.prepare();
                synchronized (mSync) {
                    mHandler = new CameraHandler(this);
                    mSync.notifyAll();
                }
                Looper.loop();
                synchronized (mSync) {
                    mHandler = null;
                    mSync.notifyAll();
                }
            }
        }
    }
}
