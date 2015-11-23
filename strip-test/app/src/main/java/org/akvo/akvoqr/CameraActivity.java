package org.akvo.akvoqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.ui.FinderPatternIndicatorView;
import org.akvo.akvoqr.ui.LevelView;
import org.akvo.akvoqr.ui.QualityCheckView;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 7/7/15.
 */
public class CameraActivity extends AppCompatActivity implements CameraViewListener, DetectStripListener {

    private Camera mCamera;
    private FrameLayout preview;
    private BaseCameraView mPreview;
    MyPreviewCallback previewCallback;
    private final String TAG = "CameraActivity"; //NON-NLS
    private android.os.Handler handler;
    private FinderPatternIndicatorView finderPatternIndicatorView;
    private String brandName;
    private boolean start = false;
    private int countQualityCheckIteration = 0;
    private int countQualityCheckResult = 0;
    private LinearLayout progressLayout;
    private Fragment currentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (getIntent().getStringExtra(Constant.BRAND) != null) {
            this.brandName = getIntent().getStringExtra(Constant.BRAND);
        }
        else
        {
            throw new NullPointerException("Cannot proceed without brand.");
        }

        handler = new Handler(Looper.getMainLooper());

        progressLayout = (LinearLayout) findViewById(R.id.activity_cameraInitCameraProgressBar);

        finderPatternIndicatorView =
                (FinderPatternIndicatorView) findViewById(R.id.activity_cameraFinderPatternIndicatorView);

    }

    private void init() {

        // Create an instance of Camera
        mCamera = TheCamera.getCameraInstance();

        previewCallback = MyPreviewCallback.getInstance(this);

        if (mCamera != null) {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new BaseCameraView(this, mCamera);
            preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.removeAllViews();
            preview.addView(mPreview);

            //make 'initialising camera' message invisible
            if (progressLayout.getVisibility() == View.VISIBLE) {
                progressLayout.setVisibility(View.GONE);
            }

            //inflate fragment
            currentFragment = CameraPrepareFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.activity_cameraFragmentPlaceholder, currentFragment
            ).commit();

            //use brightness view as a button to switch on and off the flash
            QualityCheckView exposureView = (QualityCheckView) findViewById(R.id.activity_cameraImageViewExposure);
            if(exposureView!=null) {
                exposureView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mPreview.switchFlashMode();
                    }
                });
            }

            //startNextPreview(0);
        }
    }

    public void onPause() {

        if (mCamera != null) {

            mCamera.setOneShotPreviewCallback(null);

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (mPreview != null) {
            preview.removeView(mPreview);
            mPreview = null;
        }
        Log.d(TAG, "onPause OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

        super.onPause();

    }

    public void onStop() {
        Log.d(TAG, "onStop OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

        super.onPause();

        super.onStop();
    }

    public void onResume() {
        progressLayout.setVisibility(View.VISIBLE);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        super.onResume();

        //Delete all finder pattern info and image data from internal storage
        FileStorage.deleteFromInternalStorage(Constant.INFO);
        FileStorage.deleteFromInternalStorage(Constant.DATA);

        Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }

    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }

    private Runnable startNextPreview = new Runnable() {
        @Override
        public void run() {

            if(mCamera!=null && previewCallback!=null) {
                mCamera.startPreview();
                mCamera.setOneShotPreviewCallback(previewCallback);

            }
        }
    };

    private Runnable takeNextPicture = new Runnable() {
        @Override
        public void run() {
            if(mCamera!=null && previewCallback!=null) {
                mCamera.startPreview();
                mCamera.setOneShotPreviewCallback(previewCallback);
                start = true;
            }
        }
    };


    //START CAMERAVIEWLISTENER INTERFACE METHODS
    @Override
    public void takeNextPicture(long timeMillis) {
        if(handler!=null)
            handler.postDelayed(takeNextPicture, timeMillis);
    }

    @Override
    public void startNextPreview(long timeMillis) {
        if(handler!=null)
            handler.postDelayed(startNextPreview, timeMillis);
    }

    @Override
    public void setFocusAreas(List<Camera.Area> areas) {
        if (mPreview != null) {
            mPreview.setFocusAreas(areas);
        }
    }

    @Override
    public void setCountQualityCheckResult(int count) {
        countQualityCheckIteration++;
        countQualityCheckResult += count;
    }

    public void setCountQualityCheckResultZero() {
        countQualityCheckResult = 0;
    }

    @Override
    public void setCountQualityCheckIterationZero() {
        countQualityCheckIteration = 0;
    }

    @Override
    public void setStartButtonVisibility(boolean show) {

        Runnable showRunnable = new Runnable() {
            @Override
            public void run() {

                if (currentFragment instanceof CameraPrepareFragment) {
                    ((CameraPrepareFragment) currentFragment).ready();
                }
                else if(currentFragment instanceof  CameraStartTestFragment)
                {
                   // start = true;
                    ((CameraStartTestFragment) currentFragment).ready();
                }
            }
        };

//        System.out.println("***countQualityCheckResult: " + countQualityCheckResult +
//        " countQualityCheckIteration: " + countQualityCheckIteration);

        if (countQualityCheckResult > Constant.COUNT_QUALITY_CHECK_LIMIT) {
            handler.post(showRunnable);
        } else if (countQualityCheckIteration > Constant.COUNT_QUALITY_CHECK_LIMIT * 1.5) {

            setCountQualityCheckIterationZero();

        }
    }

    @Override
    public void ready() {

        if(currentFragment instanceof CameraPrepareFragment) {
            currentFragment = CameraInstructionFragment.newInstance(brandName);
        }
        else if(currentFragment instanceof CameraInstructionFragment) {
            currentFragment = CameraStartTestFragment.newInstance(brandName);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_cameraFragmentPlaceholder, currentFragment).commit();

    }

    @Override
    public boolean start() {

        return start;

    }
    @Override
    public void showFinderPatterns(final List<FinderPattern> patterns, final Camera.Size size, final int color)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                finderPatternIndicatorView.setColor(color);
                finderPatternIndicatorView.showPatterns(patterns, size);
            }
        };

        if(handler!=null)
            handler.post(runnable);
    }

    @Override
    public void getMessage(int what) {
        if(mCamera!=null && mPreview!=null && !isFinishing()) {

            mCamera.startPreview();
            if (what == 0) {
                startNextPreview(0);

            } else {

                handler.removeCallbacks(startNextPreview);
                mCamera.setOneShotPreviewCallback(null);
            }
        }
    }

    @Override
    public void showFocusValue(final double value)
    {

    }
    @Override
    public void showMaxLuminosity(final boolean ok, final double value){

        final QualityCheckView exposureView = (QualityCheckView) findViewById(R.id.activity_cameraImageViewExposure);

        Runnable show = new Runnable() {
            @Override
            public void run() {

                if(exposureView!=null)
                    exposureView.setPercentage((float)value);
            }
        };

        if(handler!=null) {
            handler.post(show);
        }
    }

    @Override
    public void showShadow(final double value){

        final QualityCheckView contrastView = (QualityCheckView) findViewById(R.id.activity_cameraImageViewContrast);

        Runnable show = new Runnable() {
            @Override
            public void run() {

                if(contrastView!=null)
                    contrastView.setPercentage((float) value);
            }
        };

        if(handler!=null) {
            handler.post(show);
        }
    }

    @Override
    public void showLevel(final float[] angles)
    {
        final LevelView levelView = (LevelView) findViewById(R.id.activity_cameraImageViewLevel);

        Runnable levelRunnable = new Runnable() {
            @Override
            public void run() {
                levelView.setAngles(angles);
            }
        };

        if(handler!=null)
        {
            handler.post(levelRunnable);
        }
    }

    @Override
    public void adjustExposureCompensation(int direction)
    {
        mPreview.adjustExposure(direction);
    }


    @Override
    public void sendData(final byte[] data, long timeMillis,
                         final FinderPatternInfo info) {

        //stop myPreviewCallback take next picture
        start = false;

        if(currentFragment instanceof CameraStartTestFragment)
        {
            ((CameraStartTestFragment) currentFragment).sendData(data, timeMillis, info);
        }

        //clear the finder pattern view after one second and start the preview again
        showFinderPatterns(null, null, 0);

        //clear level indicator
        showLevel(null);

        mCamera.startPreview();
    }

    @Override
    public void dataSent() {

        if(currentFragment instanceof CameraStartTestFragment)
        {
            ((CameraStartTestFragment) currentFragment).dataSent(mCamera.getParameters().getPreviewFormat(), mCamera.getParameters().getPreviewSize().width,
                    mCamera.getParameters().getPreviewSize().height);
        }

    }

    @Override
    public void playSound()
    {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.futurebeep2);
        mp.start();
    }
    //END CAMERAVIEWLISTENER INTERFACE METHODS

    //DETECTSTRIPLISTENER INTERFACE METHODS

    @Override
    public void showSpinner() {
        ImageView finishImage = (ImageView) findViewById(R.id.activity_cameraFinishImage);

        if(finishImage!=null) {
            finishImage.setImageResource(R.drawable.spinner);
            Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
            finishImage.startAnimation(rotate);
        }
    }

    @Override
    public void showMessage(int what) {
        String[] messages = new String[]
                {
                        getString(R.string.reading_data), //0
                        getString(R.string.calibrating), //1
                        getString(R.string.cut_out_strip), //2
                        "\n\n" + getString(R.string.finished) //3

                };

        final TextView finish = (TextView) findViewById(R.id.activity_cameraFinishText);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(finish!=null)
                finish.setText(R.string.analysing);

            }
        };
        handler.post(runnable);
    }

    @Override
    public void showMessage(final String message) {
        final TextView finish = (TextView) findViewById(R.id.activity_cameraFinishText);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(finish!=null)
                finish.setText(R.string.analysing);

            }
        };
        handler.post(runnable);

    }

    @Override
    public void showError(final int what) {

        final String[] messages = new String[]
                {
                        getString(R.string.error_conversion), //0
                        getString(R.string.error_no_finder_pattern_info), //1
                        getString(R.string.error_warp), //2
                        getString(R.string.error_detection), //3
                        getString(R.string.error_calibrating), //4
                        getString(R.string.error_cut_out_strip), //5
                        getString(R.string.error_unknown) //5
                };

        final TextView finish = (TextView) findViewById(R.id.activity_cameraFinishText);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(finish!=null) {
                    int mesNo= what < messages.length? what: messages.length - 1;
                    finish.setText(messages[mesNo]);
                }

            }
        };
        handler.post(runnable);
    }

    @Override
    public void showImage(Bitmap bitmap){ }

    @Override
    public void showResults(ArrayList<Mat> resultList) {
        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra(Constant.BRAND, brandName);
        resultIntent.putExtra(Constant.MAT, resultList);
        startActivity(resultIntent);
    }

    //END DETECTSTRIPLISTENER INTERFACE METHODS

    //OPENCV MANAGER
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("", "OpenCV loaded successfully");

                    if(mCamera!=null)
                    {
                        Log.d(TAG, "mCamera is not null");

                        try {
                            mCamera.reconnect();
                            mCamera.startPreview();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        init();
                    }

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }

                break;
            }
        }
    };
}
