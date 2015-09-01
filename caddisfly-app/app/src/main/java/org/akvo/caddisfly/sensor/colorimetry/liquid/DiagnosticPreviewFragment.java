package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.ImageUtil;

public class DiagnosticPreviewFragment extends DialogFragment {

    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;
    private SoundPoolPlayer sound;
    private CameraDialog mCameraDialog;

    public static DiagnosticPreviewFragment newInstance() {
        return new DiagnosticPreviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnostic_preview, container, false);

        sound = new SoundPoolPlayer(getActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                AppPreferences.getUseCamera2Api(getActivity())) {
            mCameraDialog = Camera2DialogFragment.newInstance();
        } else {
            mCameraDialog = CameraDialogFragment.newInstance();
        }

        final DiagnosticPreviewFragment currentDialog = this;

        mCameraDialog.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
            @Override
            public void onPictureTaken(byte[] bytes, boolean completed) {

                mCameraDialog.dismiss();

                sound.playShortResource(R.raw.beep);

                Bitmap bitmap = ImageUtil.getBitmap(bytes);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
                        !AppPreferences.getUseCamera2Api(getActivity())) {

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
                }

                DiagnosticDetailsFragment diagnosticDetailsFragment =
                        DiagnosticDetailsFragment.newInstance(
                                ImageUtil.getCroppedBitmap(bitmap,
                                        AppConfig.SAMPLE_CROP_LENGTH_DEFAULT),
                                bitmap, bitmap.getWidth() + " x " + bitmap.getHeight(), getParentFragment());

                final FragmentTransaction ft = getFragmentManager().beginTransaction();

                Fragment prev = getFragmentManager().findFragmentByTag("resultDialog");
                if (prev != null) {
                    ft.remove(prev);
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
                }, AppConfig.DELAY_BETWEEN_SAMPLING);
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
