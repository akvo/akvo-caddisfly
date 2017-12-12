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

package org.akvo.caddisfly.sensor.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtil;

public class RunTestFragment extends BaseRunTest implements RunTest {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MAX_SHAKE_DURATION = 2000;
    private static final String TWO_SENTENCE_FORMAT = "%s%n%n%s";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private boolean mIgnoreShake;
    private boolean mWaitingForStillness;


    public RunTestFragment() {
        // Required empty public constructor
    }

    public static RunTestFragment newInstance(TestInfo param1, Calibration calibration) {
        RunTestFragment fragment = new RunTestFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);
        args.putParcelable(ARG_PARAM2, calibration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up the shake detector
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        mShakeDetector = new ShakeDetector(() -> {
            if ((mIgnoreShake) || mWaitingForStillness) {
                return;
            }

            if (getActivity().isDestroyed()) {
                return;
            }

            mWaitingForStillness = true;

//            showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestInterrupted),
//                    getString(R.string.doNotMoveDeviceDuringTest)), null);
        }, () -> {
            if (mWaitingForStillness) {
                mWaitingForStillness = false;
                dismissShakeAndStartTest();
            }
        });

        mSensorManager.unregisterListener(mShakeDetector);

        mShakeDetector.setMinShakeAcceleration(5);
        mShakeDetector.setMaxShakeDuration(MAX_SHAKE_DURATION);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected void initializeTest() {
        super.initializeTest();

        pictureCount = 0;

        binding.imageIllustration.setVisibility(View.VISIBLE);

        mSensorManager.unregisterListener(mShakeDetector);

        mIgnoreShake = false;

        mWaitingForStillness = true;

        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);

        mHandler = new Handler();
    }

    private void dismissShakeAndStartTest() {
        mSensorManager.unregisterListener(mShakeDetector);
        binding.cameraView.setVisibility(View.VISIBLE);
        binding.circleView.setVisibility(View.VISIBLE);
        binding.imageIllustration.setVisibility(View.GONE);
        startTest();
    }

    private void startTest() {
        if (!cameraStarted) {

            setupCamera();

            cameraStarted = true;

            sound.playShortResource(R.raw.beep);

            cameraSwitcher.start();

            startRepeatingTask();
        }
    }


    void startRepeatingTask() {
        mRunnableCode.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mRunnableCode);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRepeatingTask();
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    private void showError(String message, final Bitmap bitmap) {

//        releaseResources();

        sound.playShortResource(R.raw.err);

        AlertDialog alertDialogToBeDestroyed = AlertUtil.showError(getActivity(),
                R.string.error, message, bitmap, R.string.retry,
                (dialogInterface, i) -> initializeTest(),
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
//                    releaseResources();
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }, null
        );
    }

}
