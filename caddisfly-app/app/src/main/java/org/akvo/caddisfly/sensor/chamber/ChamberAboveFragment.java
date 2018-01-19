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

package org.akvo.caddisfly.sensor.chamber;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.helper.ShakeDetector;
import org.akvo.caddisfly.model.TestInfo;

public class ChamberAboveFragment extends BaseRunTest implements RunTest {

    private static final int MAX_SHAKE_DURATION = 2000;
    private static final String TWO_SENTENCE_FORMAT = "%s%n%n%s";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private boolean mIgnoreShake;
    private boolean mWaitingForStillness;

    /**
     * Get the instance.
     *
     * @param testInfo The test info
     * @return the instance
     */
    public static ChamberAboveFragment newInstance(TestInfo testInfo) {
        ChamberAboveFragment fragment = new ChamberAboveFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
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

                showError(String.format(TWO_SENTENCE_FORMAT, getString(R.string.errorTestInterrupted),
                        getString(R.string.doNotMoveDeviceDuringTest)), null, getActivity());
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
    }

    private void dismissShakeAndStartTest() {
        mSensorManager.unregisterListener(mShakeDetector);
        binding.cameraView.setVisibility(View.VISIBLE);
        binding.circleView.setVisibility(View.VISIBLE);
        binding.imageIllustration.setVisibility(View.GONE);
        startTest();
    }

    protected void startTest() {

        if (!cameraStarted) {
            mShakeDetector.setMinShakeAcceleration(1);
            mShakeDetector.setMaxShakeDuration(MAX_SHAKE_DURATION);
            mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        }

        super.startTest();

        stopPreview();
    }

    @Override
    protected void releaseResources() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.releaseResources();
    }
}
