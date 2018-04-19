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

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.FragmentRunTestBinding;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;

import java.util.ArrayList;
import java.util.Date;

import static org.akvo.caddisfly.common.Constants.DEGREES_180;
import static org.akvo.caddisfly.common.Constants.DEGREES_270;
import static org.akvo.caddisfly.common.Constants.DEGREES_90;

//import timber.log.Timber;

public class BaseRunTest extends Fragment implements RunTest {
    private static final double SHORT_DELAY = 1;
    private final ArrayList<ResultDetail> results = new ArrayList<>();
    private final Handler delayHandler = new Handler();
    protected FragmentRunTestBinding binding;
    protected boolean cameraStarted;
    protected int pictureCount = 0;
    private SoundPoolPlayer sound;
    private Handler mHandler;
    private AlertDialog alertDialogToBeDestroyed;
    private TestInfo mTestInfo;
    private Calibration mCalibration;
    private int dilution;
    private Camera mCamera;
    private OnResultListener mListener;
    private final Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            mCamera.startPreview();

            Bitmap bitmap = ImageUtil.getBitmap(data);

            getAnalyzedResult(bitmap);

            if (mTestInfo.getResults().get(0).getTimeDelay() > 0) {
                // test has time delay so take the pictures quickly with short delay
                mHandler.postDelayed(mRunnableCode, (long) (SHORT_DELAY * 1000));
            } else {
                mHandler.postDelayed(mRunnableCode, ChamberTestConfig.DELAY_BETWEEN_SAMPLING * 1000);
            }
        }
    };
    private ChamberCameraPreview mCameraPreview;
    private final Runnable mRunnableCode = () -> {
        if (pictureCount < AppPreferences.getSamplingTimes()) {
            pictureCount++;
            sound.playShortResource(R.raw.beep);
            takePicture();
        } else {
            releaseResources();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sound = new SoundPoolPlayer(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mCalibration != null && getActivity() != null) {

            // disable the key guard when device wakes up and shake alert is displayed
            getActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
    }

    protected void initializeTest() {
        results.clear();

        mHandler = new Handler();
    }

    protected void setupCamera() {
        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new ChamberCameraPreview(getActivity());
        mCamera = mCameraPreview.getCamera();
        mCameraPreview.setupCamera(mCamera);
        binding.cameraView.addView(mCameraPreview);
    }

    protected void stopPreview() {
        mCamera.stopPreview();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_run_test,
                container, false);

        pictureCount = 0;

        if (getArguments() != null) {
            mTestInfo = getArguments().getParcelable(ConstantKey.TEST_INFO);
        }

        if (mTestInfo != null) {
            mTestInfo.setDilution(dilution);
        }

        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        model.setTest(mTestInfo);

        binding.setVm(model);

        initializeTest();

        if (mCalibration != null) {
            binding.textDilution.setText(String.valueOf(mCalibration.value));
        } else {
            binding.textDilution.setText(getResources()
                    .getQuantityString(R.plurals.dilutions, dilution, dilution));
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultListener) {
            mListener = (OnResultListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void takePicture() {

        if (!cameraStarted) {
            return;
        }

        mCamera.startPreview();
        turnFlashOn();

        mCamera.takePicture(null, null, mPicture);

    }

    /**
     * Get the test result by analyzing the bitmap.
     *
     * @param bitmap the bitmap of the photo taken during analysis
     */
    private void getAnalyzedResult(@NonNull Bitmap bitmap) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int rotation;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                rotation = DEGREES_90;
                break;
            case Surface.ROTATION_180:
                rotation = DEGREES_270;
                break;
            case Surface.ROTATION_270:
                rotation = DEGREES_180;
                break;
            case Surface.ROTATION_90:
            default:
                rotation = 0;
                break;
        }

        Bitmap rotatedBitmap = ImageUtil.rotateImage(bitmap, rotation);
        Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(rotatedBitmap,
                ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        //Extract the color from the photo which will be used for comparison
        ColorInfo photoColor;
        if (croppedBitmap != null) {

            if (mTestInfo.getResults().get(0).getGrayScale()) {
                croppedBitmap = ImageUtil.getGrayscale(croppedBitmap);
            }

            photoColor = ColorUtil.getColorFromBitmap(croppedBitmap,
                    ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

            if (mCalibration != null) {
                mCalibration.color = photoColor.getColor();
                mCalibration.date = new Date().getTime();
            }

            ResultDetail resultDetail = SwatchHelper.analyzeColor(mTestInfo.getSwatches().size(),
                    photoColor, mTestInfo.getSwatches());
            resultDetail.setBitmap(rotatedBitmap);
            resultDetail.setCroppedBitmap(croppedBitmap);
            resultDetail.setDilution(dilution);

//            Timber.d("Result is: " + String.valueOf(resultDetail.getResult()));

            results.add(resultDetail);

            if (mListener != null && pictureCount >= AppPreferences.getSamplingTimes()) {
                // ignore the first two results
                for (int i = 0; i < ChamberTestConfig.SKIP_SAMPLING_COUNT; i++) {
                    if (results.size() > 1) {
                        results.remove(0);
                    }
                }

                mListener.onResult(results, mCalibration);
            }
        }
    }

    @Override
    public void setCalibration(Calibration item) {
        mCalibration = item;
    }

    @Override
    public void setDilution(int dilution) {
        this.dilution = dilution;
    }

    void startRepeatingTask() {
        mRunnableCode.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mRunnableCode);
    }

    protected void startTest() {
        if (!cameraStarted) {

            setupCamera();

            cameraStarted = true;

            sound.playShortResource(R.raw.futurebeep2);

            int timeDelay = ChamberTestConfig.DELAY_INITIAL + ChamberTestConfig.DELAY_BETWEEN_SAMPLING;

            // If the test has a time delay config then use that otherwise use standard delay
            if (mTestInfo.getResults().get(0).getTimeDelay() > 0) {
                (new Handler()).postDelayed(this::stopPreview, 1000);
                timeDelay = (int) Math.max(SHORT_DELAY, mTestInfo.getResults().get(0).getTimeDelay());
            }

            delayHandler.postDelayed(mRunnableCode, timeDelay * 1000);
        }
    }

    /**
     * Turn flash off.
     */
    public void turnFlashOff() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();

        String flashMode = Camera.Parameters.FLASH_MODE_OFF;
        parameters.setFlashMode(flashMode);

        mCamera.setParameters(parameters);
    }

    /**
     * Turn flash on.
     */
    public void turnFlashOn() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();

        String flashMode = Camera.Parameters.FLASH_MODE_TORCH;
        parameters.setFlashMode(flashMode);

        mCamera.setParameters(parameters);
    }


    protected void releaseResources() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
            mCamera.release();
            mCamera = null;
        }
        if (mCameraPreview != null) {
            mCameraPreview.destroyDrawingCache();
        }

        delayHandler.removeCallbacksAndMessages(null);

        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed.dismiss();
        }

        stopRepeatingTask();

        cameraStarted = false;
    }

    /**
     * Show an error message dialog.
     *
     * @param message the message to be displayed
     * @param bitmap  any bitmap image to displayed along with error message
     */
    protected void showError(String message,
                             @SuppressWarnings("SameParameterValue") final Bitmap bitmap,
                             Activity activity) {

        releaseResources();

        sound.playShortResource(R.raw.err);

        alertDialogToBeDestroyed = AlertUtil.showError(activity,
                R.string.error, message, bitmap, R.string.retry,
                (dialogInterface, i) -> initializeTest(),
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    activity.setResult(Activity.RESULT_CANCELED);
                    activity.finish();
                }, null
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseResources();
    }

    public interface OnResultListener {
        void onResult(ArrayList<ResultDetail> results, Calibration calibration);
    }
}
