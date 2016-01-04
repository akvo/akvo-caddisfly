package org.akvo.akvoqr;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.ui.FinderPatternIndicatorView;
import org.akvo.akvoqr.ui.LevelView;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linda on 7/7/15.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements CameraViewListener, DetectStripListener {

    private WeakReference<CameraActivity> mActivity;
    private Camera mCamera;
    private  WeakReference<Camera> wrCamera;
    private FrameLayout previewLayout;
    private BaseCameraView baseCameraView;
    private final String TAG = "CameraActivity"; //NON-NLS
    private final MyHandler handler = new MyHandler();
    private FinderPatternIndicatorView finderPatternIndicatorView;
    private LevelView levelView;
    private String brandName;
    private LinearLayout progressLayout;
    private CameraSharedFragmentAbstract currentFragment;
    private int previewFormat= -1;
    private int previewWidth = 0;
    private int previewHeight = 0;
    private final Map<String, Integer> qualityCountMap = new LinkedHashMap<>(3); // <Type, count>
    private ShowFinderPatternRunnable showFinderPatternRunnable;
    private ShowLevelRunnable showLevelRunnable;
    private CameraScheduledExecutorService cameraScheduledExecutorService;
    private CameraPreviewCallbackSP cameraPreviewCallbackSP;
    private CameraPreviewCallbackTP cameraPreviewCallbackTP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //LOGGING SCREEN SIZE
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        String toastMsg;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                toastMsg = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                toastMsg = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                toastMsg = "Small screen";
                break;
            default:
                toastMsg = "Screen size is neither large, normal or small";
        }
        System.out.println("***screen size: " + toastMsg);
        //END LOGGING SCREEN SIZE

        //LOG DENSITY
        int density= getResources().getDisplayMetrics().densityDpi;
        String msg;
        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                msg = "LDPI";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                msg =  "MDPI";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                msg =  "HDPI";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                msg = "XHDPI";
                break;
            default:
                msg = "Unknown";
        }
        System.out.println("***screen density: " + msg);
        //END LOGGING DENSITY

        //LOG SYSTEM BUILD
        System.out.println("***System info: VERSION CODENAME = " + Build.VERSION.CODENAME);
        System.out.println("***System info: VERSION RELEASE = " + Build.VERSION.RELEASE);
        System.out.println("***System info: VERSION SDK = " + Build.VERSION.SDK_INT);
        //END LOG SYSTEM BUILD

        //VIEWS
        progressLayout = (LinearLayout) findViewById(R.id.activity_cameraInitCameraProgressBar);
        finderPatternIndicatorView =
                (FinderPatternIndicatorView) findViewById(R.id.activity_cameraFinderPatternIndicatorView);
        levelView = (LevelView) findViewById(R.id.activity_cameraImageViewLevel);

        //RUNNABLES
        showFinderPatternRunnable = new ShowFinderPatternRunnable();
        showLevelRunnable = new ShowLevelRunnable();

        //INIT COUNT QUALITY MAP
        qualityCountMap.put("B", 0);
        qualityCountMap.put("S", 0);
        qualityCountMap.put("L", 0);

        mActivity = new WeakReference<CameraActivity>(this);

        cameraScheduledExecutorService = new CameraScheduledExecutorService();

    }

    private void init() {

        // Create an instance of Camera
        mCamera = TheCamera.getCameraInstance();

        if(mCamera == null)
        {
            Toast.makeText(this.getApplicationContext(), "Could not instantiate the camera", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            try {

                wrCamera = new WeakReference<Camera>(mCamera);

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

                currentFragment = CameraPrepareFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.activity_cameraFragmentPlaceholder, currentFragment
                ).commit();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            //schedule focus at fixed intervals
//            if (cameraScheduledExecutorService!=null)
//                cameraScheduledExecutorService.scheduleRunnableWithFixedDelay(focus, 1000, 5000);

        }
    }

    public void onPause() {

        cameraScheduledExecutorService.shutdown();

        if (mCamera != null) {

            mCamera.setOneShotPreviewCallback(null);

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if(mActivity!=null)
        {
            mActivity.clear();
            mActivity = null;
        }
        if(wrCamera!=null)
        {
            wrCamera.clear();
            wrCamera = null;
        }

        if (baseCameraView != null && previewLayout!=null) {
            previewLayout.removeView(baseCameraView);
            baseCameraView = null;
        }

        Log.d(TAG, "onPause OUT mCamera, mCameraPreview: " + mCamera + ", " + baseCameraView);

        super.onPause();

    }

    public void onResume() {

        if (getIntent().getStringExtra(Constant.BRAND) != null) {
            this.brandName = getIntent().getStringExtra(Constant.BRAND);
        }
        else
        {
            throw new NullPointerException("Cannot proceed without brand.");

        }

        progressLayout.setVisibility(View.VISIBLE);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        //Delete all finder pattern info and image data from internal storage
        new DeleteTask().execute();

        super.onResume();

        Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + baseCameraView);

    }
    private class  DeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final FileStorage fileStorage = new FileStorage(CameraActivity.this);

            fileStorage.deleteFromInternalStorage(Constant.INFO);
            fileStorage.deleteFromInternalStorage(Constant.DATA);
            fileStorage.deleteFromInternalStorage(Constant.STRIP);

            return null;
        }
    }
    //store previewLayout info in global properties for later use
    //called at end of baseCameraView surfaceChanged()
    public void setPreviewProperties()
    {
        if(mCamera!=null && baseCameraView!=null) {

            previewFormat = mCamera.getParameters().getPreviewFormat();
            previewWidth = mCamera.getParameters().getPreviewSize().width;
            previewHeight = mCamera.getParameters().getPreviewSize().height;

        }
    }

    /*********************************************
     *
     * START CAMERAVIEWLISTENER INTERFACE METHODS
     *
     *********************************************/
    @Override
    public void switchFlash() {
        baseCameraView.switchFlashMode();
    }

    //prevents or enables the CameraPreviewCallback to execute ...
    @Override
    public void stopCallback(boolean stop) {
        if(cameraPreviewCallbackSP!=null)
            cameraPreviewCallbackSP.setStop(true);
        if(cameraPreviewCallbackTP!=null)
            cameraPreviewCallbackTP.setStop(true);
    }

    @Override
    public void takeNextPicture(long timeMillis) {

        if(cameraScheduledExecutorService!=null) {
            cameraScheduledExecutorService.cancelTasks(timeMillis);
            cameraScheduledExecutorService.scheduleRunnable(takeNextPictureRunnable, timeMillis);
        }

    }

    @Override
    public void startNextPreview(long timeMillis) {

        if(cameraScheduledExecutorService!=null) {
            cameraScheduledExecutorService.scheduleRunnableWithFixedDelay(startNextPreviewRunnable, timeMillis, 500);
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

        for(Map.Entry<String, Integer> entry: qualityCountMap.entrySet()) {
            entry.setValue(0);
        }
    }

    @Override
    public void addCountToQualityCheckCount(int[] countArray) {

        if(countArray==null)
        {
            throw new NullPointerException("quality checks array is NULL");
        }

        int ci = 0;
        for(Map.Entry<String, Integer> entry: qualityCountMap.entrySet())
        {
            entry.setValue(entry.getValue() + countArray[ci]);
            ci ++;
        }

        if (currentFragment != null) {
            currentFragment.countQuality(qualityCountMap);
        }

        boolean show = true;
        for(int i: qualityCountMap.values())
        {
            if(i < Constant.COUNT_QUALITY_CHECK_LIMIT / qualityCountMap.size()) {

                show = false;
            }

        }
        if (show) {

            if (currentFragment != null) {
                currentFragment.showStartButton();
            }
        }
    }

    @Override
    public void nextFragment() {

        System.out.println("***brandname CameraActivity nextFragment: " + brandName);

        if(currentFragment instanceof CameraPrepareFragment) {
            //start instruction fragment
            currentFragment = CameraInstructionFragment.newInstance(brandName);

        }
        else if(currentFragment instanceof CameraInstructionFragment) {
            currentFragment = CameraStartTestFragment.newInstance(brandName);

        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_cameraFragmentPlaceholder, currentFragment)
                .commit();

    }

    @Override
    public boolean qualityChecksOK() {

        boolean OK = true;

        for(int i: qualityCountMap.values())
        {
            if(i < Constant.COUNT_QUALITY_CHECK_LIMIT / qualityCountMap.size())
            {
                OK = false;
            }
        }

        return OK;
    }


    @Override
    public void showFinderPatterns(final List<FinderPattern> patterns, final Camera.Size size, final int color)
    {
        if(handler!=null) {
            showFinderPatternRunnable.setPatterns(patterns);
            showFinderPatternRunnable.setSize(size);
            showFinderPatternRunnable.setColor(color);
            handler.post(showFinderPatternRunnable);
        }
    }

    @Override
    public void showFocusValue(final double value) {}

    @Override
    public void showBrightness(final double value){

        if(currentFragment!=null)
            currentFragment.showBrightness(value);

    }

    @Override
    public void showShadow(final double value){

        if(currentFragment!=null)
            currentFragment.showShadow(value);

    }

    @Override
    public void showLevel(final float[] angles)
    {
        if(handler!=null) {
            showLevelRunnable.setAngles(angles);
            //handler.post(showLevelRunnable);
        }
    }

    @Override
    public void adjustExposureCompensation(int direction)
    {
        if(baseCameraView!=null) {

            baseCameraView.adjustExposure(direction);
        }
    }


    @Override
    public void sendData(final byte[] data, long timeMillis,
                         final FinderPatternInfo info) {

        if(currentFragment instanceof CameraStartTestFragment)
        {
            ((CameraStartTestFragment) currentFragment).sendData(data, timeMillis, info);
        }

        //clear the finder pattern view after one second and qualityChecksOK the previewLayout again
        showFinderPatterns(null,null,1);

        //clear level indicator
        showLevel(null);

    }

    @Override
    public void dataSent() {

        //System.out.println("***previewLayout data dataSent(): " + previewFormat + " " + previewWidth + " " + previewHeight);

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
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.futurebeep2);
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
        if(currentFragment instanceof CameraStartTestFragment)
        {
            ((CameraStartTestFragment) currentFragment).showSpinner();
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
                    try {
                        int mesNo = what < messages.length ? what : messages.length - 1;
                        finish.setText(messages[mesNo]);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        };
        if(handler!=null)
            handler.post(runnable);
    }

    @Override
    public void showImage(Bitmap bitmap){ }

    @Override
    public void showResults() {

        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra(Constant.BRAND, brandName);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(resultIntent);

    }

    //END DETECTSTRIPLISTENER INTERFACE METHODS

    /*********************************
     *
     * RUNNABLE DECLARATIONS
     *
     **********************************/
    private class ShowFinderPatternRunnable implements Runnable
    {
        private int color;
        private List<FinderPattern> patterns;
        private Camera.Size size;
        private WeakReference<FinderPatternIndicatorView> wrFinderPatternIndicatorView =
                new WeakReference<FinderPatternIndicatorView>(finderPatternIndicatorView);

        @Override
        public void run() {
            if(wrFinderPatternIndicatorView != null) {

                wrFinderPatternIndicatorView.get().setColor(color);
                wrFinderPatternIndicatorView.get().showPatterns(patterns, size==null? 0: size.width, size==null? 0: size.height);
            }
        }

        public void setColor(int color) {
            this.color = color;
        }

        public void setPatterns(List<FinderPattern> patterns) {
            this.patterns = patterns;
        }

        public void setSize(Camera.Size size) {
            this.size = size;
        }

    }

    private class ShowLevelRunnable implements Runnable
    {
        private float[] angles;
        @Override
        public void run() {
            if(levelView!=null)
                levelView.setAngles(angles);
        }

        public void setAngles(float[] angles) {
            this.angles = angles;
        }
    }

    //get instance of CameraPreviewCallback
    //and do a oneShotPreviewCallback.
    //do not do this if currentFragment is instructions, only for prepare and starttest fragments
    private final Runnable startNextPreviewRunnable = new Runnable() {
        @Override
        public void run() {

            if(wrCamera!=null ) {

                if(cameraPreviewCallbackSP==null)
                    cameraPreviewCallbackSP = new CameraPreviewCallbackSP(mActivity.get(), wrCamera.get().getParameters());

                if(currentFragment!=null) {
                    if (currentFragment instanceof CameraPrepareFragment) {

                        wrCamera.get().setOneShotPreviewCallback(cameraPreviewCallbackSP);
                    }
                    if (currentFragment instanceof CameraStartTestFragment) {

                        wrCamera.get().setOneShotPreviewCallback(cameraPreviewCallbackSP);
                    }
                }
                // System.out.println("***calling startNextPreviewRunnable runnable");

            }
        }
    };

    //set takePicture to true in CameraPreviewCallback and start oneShotPreviewCallback with that.
    private final Runnable takeNextPictureRunnable = new Runnable() {
        @Override
        public void run() {
            if (wrCamera != null) {

                if(cameraPreviewCallbackTP==null)
                    cameraPreviewCallbackTP = new CameraPreviewCallbackTP(mActivity.get(), wrCamera.get().getParameters());

                wrCamera.get().setOneShotPreviewCallback(cameraPreviewCallbackTP);

                System.out.println("***calling takeNextPictureRunnable runnable");

            }
        }
    };

    private final Runnable focus = new Runnable() {

        boolean focused;
        //String focusMode = mCamera.getParameters().getFocusMode();
        @Override
        public void run() {

            try {
                if (mCamera != null) {

                    WeakReference<Camera> wrCamera = new WeakReference<Camera>(mCamera);

                    if(!focused)
                        wrCamera.get().autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                focused = success;

                                System.out.println("***focused: " + focused);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                    System.out.println("***auto exposure: " + camera.getParameters().getAutoExposureLock());
                                }
                            }
                        });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    /*********************************
     *
     * END RUNNABLE DECLARATIONS
     *
     **********************************/

    /**
     * Instances of static inner classes do not hold an implicit
     * reference to their outer class.
     */
    private static class MyHandler extends Handler {

//        public MyHandler(CameraActivity activity) {
//
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            CameraActivity activity = mActivity.get();
//            if (activity != null) {
//                System.out.println("***got your message.");
//            }
//        }
    }

    //OPENCV MANAGER
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("", "OpenCV loaded successfully");

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
