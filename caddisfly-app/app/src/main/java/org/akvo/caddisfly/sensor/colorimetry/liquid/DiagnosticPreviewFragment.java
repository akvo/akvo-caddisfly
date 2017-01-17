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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.CameraDialogFragment;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.util.ImageUtil;

public class DiagnosticPreviewFragment extends DialogFragment implements CameraDialog.Cancelled {

    private static final int MAX_SHAKE_DURATION = 2000;
    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;
    private SoundPoolPlayer sound;
    private CameraDialog mCameraDialog;

    @NonNull
    public static DiagnosticPreviewFragment newInstance() {
        return new DiagnosticPreviewFragment();
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnostic_preview, container, false);

        sound = new SoundPoolPlayer(getActivity());

        mCameraDialog = CameraDialogFragment.newInstance();

        final DiagnosticPreviewFragment currentDialog = this;

        mCameraDialog.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
            @Override
            public void onPictureTaken(@NonNull byte[] bytes, boolean completed) {

                sound.playShortResource(R.raw.beep);

                mCameraDialog.dismiss();

                Bitmap bitmap = ImageUtil.getBitmap(bytes);

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int rotation;
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        rotation = SensorConstants.DEGREES_90;
                        break;
                    case Surface.ROTATION_180:
                        rotation = SensorConstants.DEGREES_270;
                        break;
                    case Surface.ROTATION_270:
                        rotation = 180;
                        break;
                    case Surface.ROTATION_90:
                    default:
                        rotation = 0;
                        break;
                }

                bitmap = ImageUtil.rotateImage(bitmap, rotation);

                TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

                Bitmap croppedBitmap;

                if (testInfo.isUseGrayScale()) {
                    croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                            ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, false);

                    if (croppedBitmap != null) {
                        croppedBitmap = ImageUtil.getGrayscale(croppedBitmap);
                    }
                } else {
                    croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                            ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT, true);
                }

                DiagnosticDetailsFragment diagnosticDetailsFragment =
                        DiagnosticDetailsFragment.newInstance(
                                croppedBitmap,
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

        mShakeDetector.setMinShakeAcceleration(5);
        mShakeDetector.setMaxShakeDuration(MAX_SHAKE_DURATION);

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

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
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

    @Override
    public void dialogCancelled() {
        try {
            this.dismiss();
        } catch (Exception ignored) {

        }
    }
}
