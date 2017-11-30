package org.akvo.caddisfly.sensor.colorimetry.stripv2.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextSwitcher;

import org.akvo.caddisfly.sensor.colorimetry.stripv2.camera.CameraOperationsManager;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.decode.DecodeProcessor;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.CalibrationCardData;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.CalibrationCardException;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.DecodeData;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.CalibrationCardUtils;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.Constants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.widget.FinderPatternIndicatorView;

import java.util.List;

public final class StriptestHandler extends Handler {
    // Message types
    public static final int START_PREVIEW_MESSAGE = 1;
    public static final int DECODE_IMAGE_CAPTURED_MESSAGE = 2;
    public static final int DECODE_SUCCEEDED_MESSAGE = 4;
    public static final int DECODE_FAILED_MESSAGE = 5;
    public static final int EXPOSURE_OK_MESSAGE = 6;
    public static final int CHANGE_EXPOSURE_MESSAGE = 7;
    public static final int SHADOW_QUALITY_OK_MESSAGE = 8;
    public static final int SHADOW_QUALITY_FAILED_MESSAGE = 9;
    public static final int CALIBRATION_DONE_MESSAGE = 10;
    public static final int IMAGE_SAVED_MESSAGE = 11;
    private static final int DECODE_IMAGE_CAPTURE_FAILED_MESSAGE = 3;
    public static DecodeData mDecodeData;
    private static CalibrationCardData mCalCardData;
    private static State mState;
    private List<Integer> mPatchTimeLapses;
    private String TAG = "Caddisfly - handler";
    // camera manager instance
    private CameraOperationsManager mCameraOpsManager;
    // decode processor instance
    private DecodeProcessor mDecodeProcessor;
    // finder pattern indicator view
    private FinderPatternIndicatorView mFinderPatternIndicatorView;
    private StripMeasureFragment mFragment;
    private TextSwitcher mTextSwitcher;
    private int shadowQualityFailedCount = 0;
    private int tiltFailedCount = 0;
    private int distanceFailedCount = 0;
    private int decodeFailedCount = 0;
    //boolean showShadowMessage = false;
    private String currentMessage = "";
    private String currentShadowMessage = "";
    private String newMessage = "";
    private String defaultMessage;
    private int mQualityScore = 0;
    private int successCount = 0;
    private StripMeasureListener mListener;
    private long startTimeMillis;
    private int nextPatch;
    private int numPatches;
    private boolean captureNextImage;
    private Context context;

    StriptestHandler(Context context1, Context context, CameraOperationsManager cameraOpsManager,
                     FinderPatternIndicatorView finderPatternIndicatorView, StripTest.Brand brand) {
        if (StripMeasureActivity.DEBUG) {
            Log.d(TAG, "in constructor striptestHandler");
        }

        mListener = (StripMeasureListener) context1;
        this.mCameraOpsManager = cameraOpsManager;
        this.mFinderPatternIndicatorView = finderPatternIndicatorView;

        // create instance of the decode processor
        if (mDecodeProcessor == null) {
            mDecodeProcessor = new DecodeProcessor(this);
        }
        mDecodeData = new DecodeData();
        mDecodeData.setStripBrand(brand);
        mCalCardData = new CalibrationCardData();
        this.context = context;
    }

    public static DecodeData getDecodeData() {
        return mDecodeData;
    }

    public static CalibrationCardData getCalCardData() {
        return mCalCardData;
    }

    public void setTextSwitcher(TextSwitcher textSwitcher) {
        this.mTextSwitcher = textSwitcher;
    }

    public void setFragment(StripMeasureFragment fragment) {
        mFragment = fragment;
    }

    public void setTestData(List<Integer> timeLapses) {
        mPatchTimeLapses = timeLapses;
    }

    public void setStatus(State state) {
        mState = state;
    }

    @Override
    public void handleMessage(Message message) {
//        if (StripMeasureActivity.DEBUG){
//            Log.d(TAG, "in handle message of striptestHandler");
//        }
        switch (message.what) {
            case START_PREVIEW_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Log.d(TAG, "START_PREVIEW_MESSAGE received in striptest handler");
                }
                // start the image capture request.
                mCameraOpsManager.startAutofocus();
                successCount = 0;
                mQualityScore = 0;

                startTimeMillis = System.currentTimeMillis();
                nextPatch = 0;
                numPatches = mPatchTimeLapses.size();

                captureNextImage = false;

                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case DECODE_IMAGE_CAPTURED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Log.d(TAG, "DECODE_IMAGE_CAPTURED_MESSAGE received in striptest handler");
                }

                // update timer
                int secondsLeft = mPatchTimeLapses.get(nextPatch) - timeElapsedSeconds();
                if (mState.equals(State.MEASURE) && secondsLeft > Constants.MIN_SHOW_TIMER_SECONDS) {
                    mListener.showTimer();
                }

                mListener.updateTimer(secondsLeft);
                if (timeElapsedSeconds() > mPatchTimeLapses.get(nextPatch)) {
                    captureNextImage = true;
                }

                // set message shown if there are no problems
                if (mState.equals(State.MEASURE)) {
                    if (secondsLeft > Constants.GET_READY_SECONDS) {
                        defaultMessage = "Waiting";
                    } else if (secondsLeft < 2) {
                        defaultMessage = "Taking photo";
                    } else {
                        defaultMessage = "Ready for photo";
                    }
                } else {
                    defaultMessage = "Checking image quality";
                }

                // start trying to find finder patterns on decode thread
                mDecodeProcessor.startFindPossibleCenters();
                break;

            case DECODE_IMAGE_CAPTURE_FAILED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Log.d(TAG, "DECODE_IMAGE_CAPTURE_FAILED_MESSAGE received in striptest handler");
                    mDecodeData.clearData();
                    mFinderPatternIndicatorView.clearAll();
                    mCameraOpsManager.setDecodeImageCaptureRequest();
                }
                break;

            case DECODE_SUCCEEDED_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Log.d(TAG, "DECODE_SUCCEEDED_MESSAGE received in striptest handler");
                    FinderPatternInfo fpInfo = mDecodeData.getPatternInfo();
                    if (fpInfo != null) {
                        Log.d(TAG, "found codes:" + fpInfo.getBottomLeft().toString() + "," +
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
                    else {
                        if (tiltFailedCount < 4) showTiltMessage = false;
                    }
                } else {
                    tiltFailedCount = Math.max(0, tiltFailedCount - 1);
                }

                if (!mDecodeData.getDistanceOk()) {
                    distanceFailedCount = Math.min(8, distanceFailedCount + 1);
                    if (distanceFailedCount > 4) showDistanceMessage = true;
                    else {
                        if (distanceFailedCount < 4) showDistanceMessage = false;
                    }
                } else {
                    distanceFailedCount = Math.max(0, distanceFailedCount - 1);
                }

                if (showTiltMessage) {
                    newMessage = "Move camera in direction of arrow";
                    showDistanceMessage = false;
                } else {
                    if (showDistanceMessage) {
                        newMessage = "Move camera closer to the card";
                    }
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
                    Log.d(TAG, "DECODE_FAILED_MESSAGE received in striptest handler");
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
                    Log.d(TAG, "exposure - CHANGE_EXPOSURE_MESSAGE received in striptest handler, with argument:" + message.arg1);
                }

                int direction = message.arg1;

                // change exposure
                mCameraOpsManager.changeExposure(direction);
                mDecodeData.clearData();
                mCameraOpsManager.setDecodeImageCaptureRequest();
                break;

            case EXPOSURE_OK_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Log.d(TAG, "exposure - EXPOSURE_OK_MESSAGE received in striptest handler");
                }

                if (mDecodeData.isCardVersionEstablished()) {
                    int version = mDecodeData.getMostFrequentVersionNumber();

                    // if this is the first time we read the card, or if we use a card with a different
                    // version number
                    if (mCalCardData != null && mCalCardData.getVersion() != version) {
                        try {
                            CalibrationCardUtils.readCalibrationFile(context, mCalCardData, version);
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
                    Log.d(TAG, "SHADOW_QUALITY_FAILED_MESSAGE received in striptest handler");
                }

                shadowQualityFailedCount = Math.min(8, shadowQualityFailedCount + 1);
                String newShadowMessage;
                if (shadowQualityFailedCount > 4) {
//                    showShadowMessage = true;
                    newShadowMessage = "Please avoid shadows on the card";
                } else {
//                    showShadowMessage = false;
                    newShadowMessage = "";
                }

                if (!currentShadowMessage.equals(newShadowMessage)) {
                    mTextSwitcher.setText(newShadowMessage);
                    currentShadowMessage = newShadowMessage;
                }

                mFinderPatternIndicatorView.showShadow(mDecodeData.getShadowPoints(),
                        mDecodeData.getPercentageShadow(), mDecodeData.getCardToImageTransform());

                // start another decode image capture request
                mDecodeData.clearData();
                mCameraOpsManager.setDecodeImageCaptureRequest();

                break;

            case SHADOW_QUALITY_OK_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Log.d(TAG, "SHADOW_QUALITY_OK_MESSAGE received in striptest handler");
                }

                shadowQualityFailedCount = Math.max(0, shadowQualityFailedCount - 1);
                if (shadowQualityFailedCount > 4) {
//                    showShadowMessage = true;
                    newShadowMessage = "Please avoid shadows on the card";
                } else {
//                    showShadowMessage = false;
                    newShadowMessage = "";
                }

                if (!currentShadowMessage.equals(newShadowMessage)) {
                    mTextSwitcher.setText(newShadowMessage);
                    currentShadowMessage = newShadowMessage;
                }

                mFinderPatternIndicatorView.showShadow(mDecodeData.getShadowPoints(),
                        mDecodeData.getPercentageShadow(), mDecodeData.getCardToImageTransform());

                mDecodeProcessor.startCalibration();
                break;

            case CALIBRATION_DONE_MESSAGE:
                if (StripMeasureActivity.DEBUG) {
                    Log.d(TAG, "CALIBRATION_DONE_MESSAGE received in striptest handler");
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
                    if (mState.equals(StriptestHandler.State.PREPARE)) {
                        mCameraOpsManager.stopAutoFocus();
                        mListener.moveToInstructions();
                        break;
                    }
                }

                if (mState.equals(State.MEASURE) && captureNextImage && quality > Constants.CALIB_PERCENTAGE_LIMIT) {
                    captureNextImage = false;

                    mDecodeProcessor.storeImageData(mPatchTimeLapses.get(nextPatch));
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
                    // we are done
                    mListener.moveToResults();
                }
                break;
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

    private int timeElapsedSeconds() {
        return (int) Math.floor((Constants.MEASURE_TIME_COMPENSATION_MILLIS + System.currentTimeMillis() - startTimeMillis) / 1000);
    }

    public void quitSynchronously() {
        mDecodeProcessor.stopDecodeThread();
    }

    public enum State {
        PREPARE, MEASURE
    }

}

//    public void quitSynchronously() {
//        if (StriptestActivity.DEBUG){
//            Log.d(TAG, "trying to quit striptestHandler");
//        }
//
//        mState = State.DONE;
//
//        Message quit = Message.obtain(decodeThread.getHandler(), DecodeThread.QUIT);
//        quit.sendToTarget();
//        try {
//            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
//            decodeThread.join(500L);
//        } catch (InterruptedException e) {
//            // continue
//        }
//
//        // Be absolutely sure we don't send any queued up messages
////        removeMessages(R.id.decode_succeeded);
////        removeMessages(R.id.decode_failed);
//        if (StriptestActivity.DEBUG){
//            Log.d(TAG, "Done with quiting striptest handler and Decode handler");
//        }
//
//    }
