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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
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

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.AppPreferences;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.usb.UVCCamera;
import org.akvo.caddisfly.util.SoundPoolPlayer;
import org.akvo.caddisfly.widget.CameraViewInterface;

import java.lang.ref.WeakReference;
import java.util.List;

public final class ExternalCameraFragment extends DialogFragment {
    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "ExternalCameraFragment";
    private static final String ARG_PREVIEW_ONLY = "preview";

    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 640;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 480;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_MODE = UVCCamera.PIXEL_FORMAT_RAW;
    private final Handler delayHandler = new Handler();
    public PictureCallback pictureCallback;
    private SoundPoolPlayer sound;
    private boolean mPreviewOnly;
    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera related methods sequentially on private thread
     */
    private CameraHandler mHandler;
    /**
     * capture still image when you long click on preview image(not on buttons)
     */
//    private final OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
//        @Override
//        public boolean onLongClick(final View view) {
//            switch (view.getId()) {
//                case R.id.camera_view:
//                    if (mHandler != null && mHandler.isCameraOpened()) {
//                        mHandler.captureStill();
//                        return true;
//                    }
//            }
//            return false;
//        }
//    };
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
            //Toast.makeText(ExternalCameraFragment.this.getActivity(), "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
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
            //Toast.makeText(ExternalCameraFragment.this.getActivity(), "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }


        @Override
        public void onCancel() {
        }
    };
    private int samplingCount;
    private int picturesTaken;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPreviewOnly = getArguments().getBoolean(ARG_PREVIEW_ONLY);
        }
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
        //cameraView.setOnLongClickListener(mOnLongClickListener);
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
                dismiss();
            }
        };

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
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
                if (!mPreviewOnly) {
                    startTakingPictures();
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

    private void startTakingPictures() {
        samplingCount = 0;
        picturesTaken = 0;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takePicture();
            }
        }, AppConfig.DELAY_BETWEEN_SAMPLING);
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
        }, AppConfig.DELAY_BETWEEN_SAMPLING);
    }

    public boolean hasTestCompleted() {
        return samplingCount > AppPreferences.getSamplingTimes(getActivity()) || picturesTaken > 10;
    }

    public interface PictureCallback {
        void onPictureTaken(Bitmap bitmap);
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
                parent.sound.playShortResource(mWeakParent.get().getActivity(), R.raw.beep);
                final Bitmap bitmap = mWeakCameraView.get().captureStillImage();

                parent.samplingCount++;
                parent.picturesTaken++;
                //if (!mCancelled) {
                if (parent.hasTestCompleted()) {
                    parent.pictureCallback.onPictureTaken(bitmap);
                } else {
                    parent.pictureCallback.onPictureTaken(bitmap);
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
