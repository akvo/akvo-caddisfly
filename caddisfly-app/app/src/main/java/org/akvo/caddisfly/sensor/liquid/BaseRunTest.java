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
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

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

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.parameter.update.UpdateRequest;
import io.fotoapparat.result.PhotoResult;

import static io.fotoapparat.parameter.selector.AspectRatioSelectors.standardRatio;
import static io.fotoapparat.parameter.selector.FlashSelectors.off;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;
import static io.fotoapparat.result.transformer.SizeTransformers.scaled;

public class BaseRunTest extends Fragment implements RunTest {
    //    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00## ");
    private static final int DELAY = 1000; // 1 second
    private final ArrayList<ResultDetail> results = new ArrayList<>();
    protected FotoapparatSwitcher cameraSwitcher;
    protected FragmentRunTestBinding binding;
    protected boolean cameraStarted;
    protected int pictureCount = 0;
    private SoundPoolPlayer sound;
    private Context mContext;
    private Handler mHandler;
    private AlertDialog alertDialogToBeDestroyed;
    private TestInfo mTestInfo;
    private Calibration mCalibration;
    private int dilution;
    private OnResultListener mListener;
    private final Runnable mRunnableCode = () -> {
        if (pictureCount < AppPreferences.getSamplingTimes()) {
            pictureCount++;
            takePicture();
        }
    };

    public BaseRunTest() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();

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

    private void alertCouldNotLoadConfig() {

    }

    protected void initializeTest() {
        results.clear();

        mHandler = new Handler();
    }

    protected void setupCamera() {
        Fotoapparat camera = createCamera(LensPosition.BACK);
        cameraSwitcher = FotoapparatSwitcher.withDefault(camera);
        cameraSwitcher.switchTo(camera);
    }

    private Fotoapparat createCamera(LensPosition position) {
        return Fotoapparat
                .with(mContext)
                .into(binding.cameraView)
                .previewScaleType(ScaleType.CENTER_CROP)
                .photoSize(standardRatio(biggestSize()))
                .lensPosition(lensPosition(position))
                .focusMode(firstAvailable(
                        continuousFocus(),
                        autoFocus(),
                        fixed()
                ))
                .flash(torch())
                .cameraErrorCallback(e -> Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show())
                .build();
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

        if (mTestInfo.getUuid().isEmpty()) {
            alertCouldNotLoadConfig();
        } else {
            initializeTest();
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultListener) {
            mListener = (OnResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
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

        PhotoResult photoResult = cameraSwitcher.getCurrentFotoapparat().takePicture();

        //photoResult.saveToFile(new File(getExternalFilesDir("photos"), "photo.jpg"));

        photoResult
                .toBitmap(scaled(0.25f))
                .whenAvailable(result -> {
                    (new Handler()).postDelayed(() -> {
                        getAnalyzedResult(result.bitmap);
                        mHandler.postDelayed(mRunnableCode, DELAY);
                    }, 100);

                    sound.playShortResource(R.raw.beep);
                });
    }

    /**
     * Get the test result by analyzing the bitmap.
     *
     * @param bitmap the bitmap of the photo taken during analysis
     */
    private void getAnalyzedResult(@NonNull Bitmap bitmap) {

        Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(bitmap,
                ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        //Extract the color from the photo which will be used for comparison
        ColorInfo photoColor;
        if (croppedBitmap != null) {
            photoColor = ColorUtil.getColorFromBitmap(croppedBitmap,
                    ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

            if (mCalibration != null) {
                mCalibration.color = photoColor.getColor();
                mCalibration.date = new Date().getTime();
            }

            ResultDetail resultDetail = SwatchHelper.analyzeColor(mTestInfo.getSwatches().size(),
                    photoColor, mTestInfo.getSwatches());
            resultDetail.setDilution(dilution);

            results.add(resultDetail);

            if (mListener != null && pictureCount >= AppPreferences.getSamplingTimes()) {
                try {

                    if (cameraStarted) {
                        cameraSwitcher.getCurrentFotoapparat().updateParameters(
                                UpdateRequest.builder()
                                        .flash(off())
                                        .build()
                        );

                        cameraSwitcher.getCurrentFotoapparat().stop();
                        cameraSwitcher.stop();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // ignore the first two results
                for (int i = 0; i < ChamberTestConfig.SKIP_SAMPLING_COUNT; i++) {
                    if (results.size() > 1) {
                        results.remove(0);
                    }
                }

                mListener.onResult(results, mCalibration, croppedBitmap);
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

            sound.playShortResource(R.raw.beep);

            cameraSwitcher.start();

            startRepeatingTask();
        }
    }

    protected void releaseResources() {

        if (alertDialogToBeDestroyed != null) {
            alertDialogToBeDestroyed.dismiss();
        }

        stopRepeatingTask();
        try {

            cameraSwitcher.getCurrentFotoapparat().updateParameters(
                    UpdateRequest.builder()
                            .flash(off())
                            .build()
            );

            cameraSwitcher.getCurrentFotoapparat().stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        void onResult(ArrayList<ResultDetail> results, Calibration calibration, Bitmap croppedBitmap);
    }
}
