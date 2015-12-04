package org.akvo.akvoqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private FrameLayout previewLayout;
    private BaseCameraView baseCameraView;
    CameraPreviewCallback previewCallback;
    private final String TAG = "CameraActivity"; //NON-NLS
    private android.os.Handler handler;
    private FinderPatternIndicatorView finderPatternIndicatorView;
    private String brandName;
    private int qualityCheckCount = 0;
    private LinearLayout progressLayout;
    private CameraSharedFragment currentFragment;
    private MediaPlayer mp;
    private int previewFormat= -1;
    private int previewWidth = 0;
    private int previewHeight = 0;

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

        if(previewCallback==null) {
            previewCallback = new CameraPreviewCallback(this);
        }

        if (mCamera != null) {

            // Create our Preview view and set it as the content of our activity.
            baseCameraView = new BaseCameraView(this, mCamera);
            previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
            previewLayout.removeAllViews();
            previewLayout.addView(baseCameraView);

            //make 'initialising camera' message invisible
            if (progressLayout.getVisibility() == View.VISIBLE) {
                progressLayout.setVisibility(View.GONE);
            }

            //inflate fragment
            try {
                //currentFragment = CameraPrepareFragment.newInstance();
                currentFragment = CameraStartTestFragment.newInstance(brandName);
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.activity_cameraFragmentPlaceholder, currentFragment
                ).commit();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    public void onPause() {

        if(previewCallback!=null) {
            previewCallback.setStop(true);
            previewCallback = null;
        }

        if (mCamera != null) {

            mCamera.setOneShotPreviewCallback(null);

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (baseCameraView != null && previewLayout!=null) {
            previewLayout.removeView(baseCameraView);
            baseCameraView = null;
        }

        Log.d(TAG, "onPause OUT mCamera, mCameraPreview: " + mCamera + ", " + baseCameraView);

        super.onPause();

    }

    public void onStop() {
        Log.d(TAG, "onStop OUT mCamera, mCameraPreview: " + mCamera + ", " + baseCameraView);

       // super.onPause();

        super.onStop();
    }

    public void onResume() {

        progressLayout.setVisibility(View.VISIBLE);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        //Delete all finder pattern info and image data from internal storage
        FileStorage fileStorage = new FileStorage(this);
        fileStorage.deleteFromInternalStorage(Constant.INFO);
        fileStorage.deleteFromInternalStorage(Constant.DATA);

        super.onResume();

        Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + baseCameraView);

    }

    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart OUT mCamera, mCameraPreview: " + mCamera + ", " + baseCameraView);

    }

    //store previewLayout info in global properties for later use
    //called at end of baseCameraView surfaceChanged()
    public void setPreviewProperties()
    {
        if(mCamera!=null && baseCameraView!=null) {

            previewFormat = mCamera.getParameters().getPreviewFormat();
            previewWidth = mCamera.getParameters().getPreviewSize().width;
            previewHeight = mCamera.getParameters().getPreviewSize().height;
            //System.out.println("***previewLayout data : " + previewFormat + " " + previewWidth + " " + previewHeight);
        }
    }

    //in the instance of CameraPreviewCallback set takePicture to false
    //and do a oneShotPreviewCallback.
    //do not do this if currentFragment is instructions, only for prepare and starttest fragments
    private Runnable startNextPreview = new Runnable() {
        @Override
        public void run() {

            if(mCamera!=null && previewCallback!=null) {

                mCamera.startPreview();
                if(currentFragment!=null) {
                    if (currentFragment instanceof CameraPrepareFragment) {

                        previewCallback.setTakePicture(false);
                        mCamera.setOneShotPreviewCallback(previewCallback);
                    }
                    if (currentFragment instanceof CameraStartTestFragment) {

                        previewCallback.setTakePicture(false);
                        mCamera.setOneShotPreviewCallback(previewCallback);
                    }
                }
                //System.out.println("***calling startNextPreview runnable");

            }
        }
    };

    //set takePicture to true in CameraPreviewCallback and start oneShotPreviewCallback with that.
    private Runnable takeNextPicture = new Runnable() {
        @Override
        public void run() {

            if(mCamera!=null && previewCallback!=null) {

                if(handler!=null)
                    handler.removeCallbacks(startNextPreview);

                mCamera.startPreview();
                previewCallback.setTakePicture(true);
                mCamera.setOneShotPreviewCallback(previewCallback);

                //System.out.println("***calling takeNextPicture runnable");
            }
        }
    };


    //START CAMERAVIEWLISTENER INTERFACE METHODS
    @Override
    public void switchFlash() {
        baseCameraView.switchFlashMode();
    }

    //prevents or enables the CameraPreviewCallback to execute its AsyncTask
    @Override
    public void stopCallback(boolean stop) {
        if(previewCallback!=null)
            previewCallback.setStop(stop);

        if(handler!=null)
        {
            handler.removeCallbacks(startNextPreview);
            handler.removeCallbacks(takeNextPicture);
        }
    }

    @Override
    public void takeNextPicture(long timeMillis) {

        if(handler!=null) {
            handler.postDelayed(takeNextPicture, timeMillis);
        }
    }

    @Override
    public void startNextPreview(long timeMillis) {

        if(handler!=null) {
            handler.postDelayed(startNextPreview, timeMillis);
        }
    }

    @Override
    public void setFocusAreas(List<Camera.Area> areas) {
        if (mCamera!=null && baseCameraView != null) {
            baseCameraView.setFocusAreas(areas);
        }
    }

    @Override
    public void setQualityCheckCountZero() {
        qualityCheckCount = 0;
    }

    @Override
    public void addCountToQualityCheckCount(int count) {

        Runnable showRunnable = new Runnable() {
            @Override
            public void run() {
                currentFragment.showStartButton();
            }

        };

        Runnable countQualityRunnable = new Runnable() {
            @Override
            public void run() {
                currentFragment.countQuality(qualityCheckCount);
            }
        };

        if(handler!=null)
            handler.post(countQualityRunnable);

        qualityCheckCount += count;
        //System.out.println("***count quality check: " + qualityCheckCount);

        if (qualityCheckCount >= Constant.COUNT_QUALITY_CHECK_LIMIT) {

            handler.post(showRunnable);
        }

    }

    @Override
    public void nextFragment() {

        if(currentFragment instanceof CameraPrepareFragment) {
            //start instruction fragment
            currentFragment = CameraInstructionFragment.newInstance(brandName);

        }
        else if(currentFragment instanceof CameraInstructionFragment) {
            currentFragment = CameraStartTestFragment.newInstance(brandName);

        }
       // System.out.println("***currentFragment: " + currentFragment.toString());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_cameraFragmentPlaceholder, currentFragment)
                        //.addToBackStack(null)
                .commit();

    }

    @Override
    public boolean qualityChecksOK() {

        return qualityCheckCount > Constant.COUNT_QUALITY_CHECK_LIMIT;

    }
    @Override
    public void showFinderPatterns(final List<FinderPattern> patterns, final Camera.Size size, final int color)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(finderPatternIndicatorView!=null) {
                    finderPatternIndicatorView.setColor(color);
                    finderPatternIndicatorView.showPatterns(patterns, size);
                }
            }
        };

        if(handler!=null)
            handler.post(runnable);
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
                if(levelView!=null)
                    levelView.setAngles(angles);
            }
        };

        if(handler!=null) {
            handler.post(levelRunnable);
        }
    }

    @Override
    public void adjustExposureCompensation(int direction)
    {
        if(baseCameraView!=null)
            baseCameraView.adjustExposure(direction);
    }


    @Override
    public void sendData(final byte[] data, long timeMillis,
                         final FinderPatternInfo info) {


        if(currentFragment instanceof CameraStartTestFragment)
        {
            ((CameraStartTestFragment) currentFragment).sendData(data, timeMillis, info);
        }

        //clear the finder pattern view after one second and qualityChecksOK the previewLayout again
        showFinderPatterns(null, null, 0);

        //clear level indicator
        showLevel(null);

        if(mCamera!=null)
            mCamera.startPreview();
    }

    @Override
    public void dataSent() {

        System.out.println("***previewLayout data dataSent(): " + previewFormat + " " + previewWidth + " " + previewHeight);

        if(currentFragment instanceof CameraStartTestFragment)
        {
            if(previewFormat > 0 && previewWidth > 0 && previewHeight > 0 ) {
                ((CameraStartTestFragment) currentFragment).dataSent(previewFormat,
                        previewWidth,
                        previewHeight);
            }
        }

    }

    @Override
    public void playSound()
    {
        mp = MediaPlayer.create(getApplicationContext(), R.raw.futurebeep2);
        mp.start();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
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
        if(handler!=null)
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
        if(handler!=null)
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
                        getString(R.string.error_unknown) //6
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
        if(handler!=null)
            handler.post(runnable);
    }

    @Override
    public void showImage(Bitmap bitmap){ }

    @Override
    public void showResults(ArrayList<Mat> resultList) {
        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra(Constant.BRAND, brandName);
        //resultIntent.putExtra(Constant.MAT, resultList);
        startActivity(resultIntent);

        this.finish();
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

                    }

                    init();
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
