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

package org.akvo.caddisfly.sensor.striptest.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextSwitcher;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.camera.CameraOperationsManager;
import org.akvo.caddisfly.sensor.striptest.decode.DecodeProcessor;
import org.akvo.caddisfly.sensor.striptest.models.CalibrationCardData;
import org.akvo.caddisfly.sensor.striptest.models.CalibrationCardException;
import org.akvo.caddisfly.sensor.striptest.models.DecodeData;
import org.akvo.caddisfly.sensor.striptest.models.TimeDelayDetail;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.striptest.utils.CalibrationCardUtils;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;
import org.akvo.caddisfly.sensor.striptest.widget.FinderPatternIndicatorView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class StriptestHandler extends Handler {
    public static final int DECODE_IMAGE_CAPTURED_MESSAGE = 2;
    public static final int DECODE_SUCCEEDED_MESSAGE = 4;
    public static final int DECODE_FAILED_MESSAGE = 5;
    public static final int EXPOSURE_OK_MESSAGE = 6;
    public static final int CHANGE_EXPOSURE_MESSAGE = 7;
    public static final int SHADOW_QUALITY_OK_MESSAGE = 8;
    public static final int SHADOW_QUALITY_FAILED_MESSAGE = 9;
    public static final int CALIBRATION_DONE_MESSAGE = 10;
    public static final int IMAGE_SAVED_MESSAGE = 11;
    // Message types
    static final int START_PREVIEW_MESSAGE = 1;
    private static final DecodeData mDecodeData = new DecodeData();
    private static final int DECODE_IMAGE_CAPTURE_FAILED_MESSAGE = 3;
    private static final CalibrationCardData mCalCardData = new CalibrationCardData();
    private static State mState;
    private static int decodeFailedCount = 0;
    private static int successCount = 0;
    private static int nextPatch;
    private static int numPatches;
    private static boolean captureNextImage;
    private final List<TimeDelayDetail> mPatchTimeDelays = new ArrayList<>();
    // camera manager instance
    private final CameraOperationsManager mCameraOpsManager;
    // finder pattern indicator view
    private final FinderPatternIndicatorView mFinderPatternIndicatorView;
    private final StripMeasureListener mListener;
    private final Context context;
    private List<TimeDelayDetail> mPatchTimeDelaysUnfiltered;
    // decode processor instance
    private DecodeProcessor mDecodeProcessor;
    private StripMeasureFragment mFragment;
    private TextSwitcher mTextSwitcher;
    private int shadowQualityFailedCount = 0;
    private int tiltFailedCount = 0;
    private int distanceFailedCount = 0;
    private String currentMessage = "";
    private String currentShadowMessage = "";
    private String newMessage = "";
    private String defaultMessage;
    private int mQualityScore = 0;
    private long startTimeMillis;
    private int currentTestStage = 1;
    private int totalTestStages = 1;

    StriptestHandler(Context context, CameraOperationsManager cameraOpsManager,
                     FinderPatternIndicatorView finderPatternIndicatorView, TestInfo testInfo) {
        if (StripMeasureActivity.DEBUG) {
            Timber.d("in constructor striptestHandler");
        }

        mListener = (StripMeasureListener) context;
        this.mCameraOpsManager = cameraOpsManager;
        this.mFinderPatternIndicatorView = finderPatternIndicatorView;

        // create instance of the decode processor
        if (mDecodeProcessor == null) {
            mDecodeProcessor = new DecodeProcessor(this);
        }
        mDecodeData.setTestInfo(testInfo);
        this.context = context;
    }

    public static DecodeData getDecodeData() {
        return mDecodeData;
    }

    public static CalibrationCardData getCalCardData() {
        return mCalCardData;
    }

    void setTextSwitcher(TextSwitcher textSwitcher) {
        this.mTextSwitcher = textSwitcher;
    }

    public void setFragment(StripMeasureFragment fragment) {
        mFragment = fragment;
    }

    void setTestData(List<TimeDelayDetail> timeDelays) {
        mPatchTimeDelaysUnfiltered = timeDelays;
    }

    void setStatus(State state) {
        mState = state;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case START_PREVIEW_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("START_PREVIEW_MESSAGE received in striptest handler");
                }
                // start the image capture request.
                mCameraOpsManager.startAutofocus();
                successCount = 0;
                mQualityScore = 0;

                startTimeMillis = System.currentTimeMillis();
                nextPatch = 0;
                mPatchTimeDelays.clear();
                for (TimeDelayDetail timeDelayDetail : mPatchTimeDelaysUnfiltered) {
                    // get only the patches that have to be analyzed at the current stage
                    if (timeDelayDetail.getTestStage() == currentTestStage) {
                        mPatchTimeDelays.add(timeDelayDetail);
                    }

                    totalTestStages = Math.max(timeDelayDetail.getTestStage(), totalTestStages);
                }
                numPatches = mPatchTimeDelays.size();

                captureNextImage = false;

                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case DECODE_IMAGE_CAPTURED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("DECODE_IMAGE_CAPTURED_MESSAGE received in striptest handler");
                }

                // update timer
                int secondsLeft = mPatchTimeDelays.get(nextPatch).getTimeDelay() - timeElapsedSeconds();
                if (mState.equals(State.MEASURE) && secondsLeft > Constants.MIN_SHOW_TIMER_SECONDS) {
                    mListener.showTimer();
                }

                mListener.updateTimer(secondsLeft);
                if (timeElapsedSeconds() > mPatchTimeDelays.get(nextPatch).getTimeDelay()) {
                    captureNextImage = true;
                }

                // set message shown if there are no problems
                if (mState.equals(State.MEASURE)) {
                    if (secondsLeft > Constants.GET_READY_SECONDS) {
                        defaultMessage = context.getString(R.string.waiting);
                    } else if (secondsLeft < 2) {
                        defaultMessage = context.getString(R.string.taking_photo);
                    } else {
                        defaultMessage = context.getString(R.string.ready_for_photo);
                    }
                } else {
                    defaultMessage = context.getString(R.string.checking_image_quality);
                }

                // start trying to find finder patterns on decode thread
                mDecodeProcessor.startFindPossibleCenters();
                break;

            case DECODE_IMAGE_CAPTURE_FAILED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("DECODE_IMAGE_CAPTURE_FAILED_MESSAGE received in striptest handler");
                    mDecodeData.clearData();
                    mFinderPatternIndicatorView.clearAll();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                }
                break;

            case DECODE_SUCCEEDED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("DECODE_SUCCEEDED_MESSAGE received in striptest handler");
                    FinderPatternInfo fpInfo = mDecodeData.getPatternInfo();
                    if (fpInfo != null) {
                        Timber.d("found codes:" + fpInfo.getBottomLeft().toString() + "," +
                                fpInfo.getBottomRight().toString() + "," +
                                fpInfo.getTopLeft().toString() + "," +
                                fpInfo.getTopRight().toString() + ",");
                    }
                }

                boolean showTiltMessage = false;
                boolean showDistanceMessage = false;
                decodeFailedCount = 0;

                if (mDecodeData.getTilt() != DecodeProcessor.NO_TILT) {
                    tiltFailedCount = Math.min(8, tiltFailedCount + 1);
                    if (tiltFailedCount > 4) showTiltMessage = true;
                } else {
                    tiltFailedCount = Math.max(0, tiltFailedCount - 1);
                }

                if (!mDecodeData.getDistanceOk()) {
                    distanceFailedCount = Math.min(8, distanceFailedCount + 1);
                    if (distanceFailedCount > 4) showDistanceMessage = true;
                } else {
                    distanceFailedCount = Math.max(0, distanceFailedCount - 1);
                }

                if (showTiltMessage) {
                    newMessage = context.getString(R.string.tilt_camera_in_direction);
                    showDistanceMessage = false;
                } else if (showDistanceMessage) {
                    newMessage = context.getString(R.string.move_camera_closer);
                }

                if (!showTiltMessage && !showDistanceMessage) {
                    newMessage = defaultMessage;
                }

                if (!newMessage.equals(currentMessage)) {
                    mTextSwitcher.setText(newMessage);
                    currentMessage = newMessage;
                }

                // show patterns
                mFinderPatternIndicatorView.showPatterns(mDecodeData.getFinderPatternsFound(),
                        mDecodeData.getTilt(), showTiltMessage, showDistanceMessage, mDecodeData.getDecodeWidth(), mDecodeData.getDecodeHeight());

                // move on to exposure quality check

                // if tilt or distance are not ok, start again
                if (mDecodeData.getTilt() != DecodeProcessor.NO_TILT || !mDecodeData.getDistanceOk()) {
                    mDecodeData.clearData();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                    break;
                }

                // if we are here, all is well and we can proceed
                mDecodeProcessor.startExposureQualityCheck();
                break;

            case DECODE_FAILED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("DECODE_FAILED_MESSAGE received in striptest handler");
                }
                decodeFailedCount++;
                mDecodeData.clearData();
                if (decodeFailedCount > 5) {
                    mFinderPatternIndicatorView.clearAll();
                }
                mQualityScore *= 0.9;
                mFragment.showQuality(mQualityScore);
                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case CHANGE_EXPOSURE_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("exposure - CHANGE_EXPOSURE_MESSAGE received in striptest handler, with argument:%s", message.arg1);
                }

                int direction = message.arg1;

                // change exposure
                mCameraOpsManager.changeExposure(direction);
                mDecodeData.clearData();
                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case EXPOSURE_OK_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("exposure - EXPOSURE_OK_MESSAGE received in striptest handler");
                }

                if (mDecodeData.isCardVersionEstablished()) {
                    int version = mDecodeData.getMostFrequentVersionNumber();

                    // if this is the first time we read the card, or if we use a card with a different
                    // version number
                    if (mCalCardData.getVersion() != version) {
                        try {
                            CalibrationCardUtils.readCalibrationFile(mCalCardData, version);
                        } catch (CalibrationCardException e) {
                            // keep going
                            mDecodeData.clearData();
                            mCameraOpsManager.setDecodeImageCaptureRequest();
                            break;
                        }
                    }
                    // if we arrive here, we both have loaded a valid calibration card file,
                    // and the exposure is ok. So we can proceed to the next step: checking shadows.
                    mDecodeProcessor.startShadowQualityCheck();

                } else {
                    // start again
                    mDecodeData.clearData();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                }
                break;

            case SHADOW_QUALITY_FAILED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("SHADOW_QUALITY_FAILED_MESSAGE received in striptest handler");
                }

                shadowQualityFailedCount = Math.min(8, shadowQualityFailedCount + 1);
                String newShadowMessage;
                if (shadowQualityFailedCount > 4) {
                    newShadowMessage = context.getString(R.string.avoid_shadows_on_card);
                } else {
                    newShadowMessage = "";
                }

                if (!currentShadowMessage.equals(newShadowMessage)) {
                    mTextSwitcher.setText(newShadowMessage);
                    currentShadowMessage = newShadowMessage;
                }

                mFinderPatternIndicatorView.showShadow(mDecodeData.getShadowPoints(),
                        mDecodeData.getCardToImageTransform());

                // start another decode image capture request
                mDecodeData.clearData();
                mCameraOpsManager.setDecodeImageCaptureRequest();

                break;

            case SHADOW_QUALITY_OK_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("SHADOW_QUALITY_OK_MESSAGE received in striptest handler");
                }

                shadowQualityFailedCount = Math.max(0, shadowQualityFailedCount - 1);
                if (shadowQualityFailedCount > 4) {
                    newShadowMessage = context.getString(R.string.avoid_shadows_on_card);
                } else {
                    newShadowMessage = "";
                }

                if (!currentShadowMessage.equals(newShadowMessage)) {
                    mTextSwitcher.setText(newShadowMessage);
                    currentShadowMessage = newShadowMessage;
                }

                mFinderPatternIndicatorView.showShadow(mDecodeData.getShadowPoints(),
                        mDecodeData.getCardToImageTransform());

                mDecodeProcessor.startCalibration();
                break;

            case CALIBRATION_DONE_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Timber.d("CALIBRATION_DONE_MESSAGE received in striptest handler");
                }
                int quality = qualityPercentage(mDecodeData.getDeltaEStats());
                if (mFragment != null) {
                    mFragment.showQuality(quality);
                    if (mState.equals(State.PREPARE) && quality > Constants.CALIB_PERCENTAGE_LIMIT) {
                        successCount++;
                        mFragment.setProgress(successCount);
                    }
                }

                if (mState.equals(State.PREPARE) && successCount > Constants.COUNT_QUALITY_CHECK_LIMIT) {
                    mCameraOpsManager.stopAutofocus();
                    mListener.moveToInstructions(currentTestStage);
                    break;
                }

                if (mState.equals(State.MEASURE) && captureNextImage && quality > Constants.CALIB_PERCENTAGE_LIMIT) {
                    captureNextImage = false;

                    mDecodeProcessor.storeImageData(mPatchTimeDelays.get(nextPatch).getTimeDelay());
                } else {
                    // start another decode image capture request
                    mDecodeData.clearData();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                }
                break;

            case IMAGE_SAVED_MESSAGE:
                mListener.playSound();
                if (nextPatch < numPatches - 1) {
                    nextPatch++;

                    // start another decode image capture request
                    mDecodeData.clearData();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                } else {
                    if (currentTestStage < totalTestStages) {
                        // if all are stages are not completed then show next instructions
                        currentTestStage++;
                        mListener.moveToInstructions(currentTestStage);
                    } else {

                        // debug code
                        // mDecodeData.saveImage();

                        // we are done
                        mListener.moveToResults();
                    }
                }
                break;
            default:
        }
    }

    private int qualityPercentage(float[] deltaEStats) {
        // we consider anything lower than 2.5 to be good.
        // anything higher than 4.5 is red.
        double score = 0;
        if (deltaEStats != null) {
            score = Math.round(100.0 * (1 - Math.min(1.0, (Math.max(0, deltaEStats[1] - 2.5) / 2.0))));
        }

        // if the quality is improving, we show a high number quickly, if it is
        // going down, we go down more slowly.
        if (score > mQualityScore) {
            mQualityScore = (int) Math.round((mQualityScore + score) / 2.0);
        } else {
            mQualityScore = (int) Math.round((5 * mQualityScore + score) / 6.0);
        }
        return mQualityScore;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private int timeElapsedSeconds() {
        return (int) Math.floor((Constants.MEASURE_TIME_COMPENSATION_MILLIS
                + System.currentTimeMillis() - startTimeMillis) / 1000);
    }

    void quitSynchronously() {
        mDecodeProcessor.stopDecodeThread();
    }

    public enum State {
        PREPARE, MEASURE
    }
}