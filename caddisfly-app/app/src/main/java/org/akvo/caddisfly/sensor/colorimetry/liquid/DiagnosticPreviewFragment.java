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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.CameraDialogFragment;
import org.akvo.caddisfly.usb.DeviceFilter;
import org.akvo.caddisfly.usb.USBMonitor;
import org.akvo.caddisfly.util.ImageUtil;

import java.util.List;

public class DiagnosticPreviewFragment extends DialogFragment {

    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;
    private SoundPoolPlayer sound;
    private CameraDialog mCameraDialog;

    public static DiagnosticPreviewFragment newInstance() {
        return new DiagnosticPreviewFragment();
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnostic_preview, container, false);

        sound = new SoundPoolPlayer(getActivity());

        USBMonitor usbMonitor = new USBMonitor(getActivity(), null);

        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getActivity(), R.xml.camera_device_filter);
        List<UsbDevice> usbDeviceList = usbMonitor.getDeviceList(filter.get(0));
        if (usbDeviceList.size() > 0) {
            mCameraDialog = ExternalCameraFragment.newInstance();
        } else {
            mCameraDialog = CameraDialogFragment.newInstance();
        }

        final DiagnosticPreviewFragment currentDialog = this;

        mCameraDialog.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
            @Override
            public void onPictureTaken(byte[] bytes, boolean completed) {

                sound.playShortResource(R.raw.beep);

                mCameraDialog.dismiss();

                Bitmap bitmap = ImageUtil.getBitmap(bytes);

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int rotation = 0;
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        rotation = 90;
                        break;
                    case Surface.ROTATION_90:
                        rotation = 0;
                        break;
                    case Surface.ROTATION_180:
                        rotation = 270;
                        break;
                    case Surface.ROTATION_270:
                        rotation = 180;
                        break;
                }

                bitmap = ImageUtil.rotateImage(bitmap, rotation);

                DiagnosticDetailsFragment diagnosticDetailsFragment =
                        DiagnosticDetailsFragment.newInstance(
                                ImageUtil.getCroppedBitmap(bitmap,
                                        ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT),
                                bitmap, bitmap.getWidth() + " x " + bitmap.getHeight());

                final FragmentTransaction ft = getFragmentManager().beginTransaction();

                Fragment fragment = getFragmentManager().findFragmentByTag("resultDialog");
                if (fragment != null) {
                    ft.remove(fragment);
                }

                diagnosticDetailsFragment.setCancelable(true);
                diagnosticDetailsFragment.show(ft, "resultDialog");
                currentDialog.dismiss();
            }
        });

        getChildFragmentManager().beginTransaction()
                .add(R.id.layoutContainer, mCameraDialog)
                .commit();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraDialog.takePictureSingle();
            }
        });

        //in diagnostic mode allow user to long press or place device face down to to run a quick test
        //Set up the shake detector
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        final Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
            }
        }, new ShakeDetector.OnNoShakeListener() {
            @Override
            public void onNoShake() {
                unregisterShakeDetector();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCameraDialog.takePictureSingle();
                    }
                }, ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING);
            }
        });

        mShakeDetector.minShakeAcceleration = 5;
        mShakeDetector.maxShakeDuration = 2000;

        mSensorManager.registerListener(mShakeDetector, accelerometer,
                SensorManager.SENSOR_DELAY_UI);

        return view;
    }

    private void unregisterShakeDetector() {
        try {
            mSensorManager.unregisterListener(mShakeDetector);
        } catch (Exception ignored) {

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterShakeDetector();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterShakeDetector();

    }
}
