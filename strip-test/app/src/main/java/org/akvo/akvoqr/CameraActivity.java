package org.akvo.akvoqr;

import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.detector.FinderPatternInfoToJson;
import org.akvo.akvoqr.ui.FinderPatternIndicatorView;
import org.akvo.akvoqr.ui.ProgressIndicatorView;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.List;

/**
 * Created by linda on 7/7/15.
 */
public class CameraActivity extends AppCompatActivity implements CameraViewListener{

    private Camera mCamera;
    private FrameLayout preview;
    private BaseCameraView mPreview;
    MyPreviewCallback previewCallback;
    private final String TAG = "CameraActivity"; //NON-NLS
    private android.os.Handler handler;
    private TextView messageLightView;
    private TextView messageFocusView;
    private TextView messageContrastView;
    private ProgressIndicatorView progressIndicatorView;
    private FinderPatternIndicatorView finderPatternIndicatorView;
    private String brandName;
    private boolean hasTimeLapse;
    private List<StripTest.Brand.Patch> patches;
    private int numPatches;
    private Button startButton;
    private boolean startButtonClicked = false;
    private int countQualityCheckIteration = 0;
    private int countQualityCheckResult = 0;
    private Intent detectStripIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(getIntent().getStringExtra(Constant.BRAND)!=null)
        {
            this.brandName = getIntent().getStringExtra(Constant.BRAND);
        }

        detectStripIntent = new Intent(this, DetectStripActivity.class);

        handler = new Handler(Looper.getMainLooper());

        messageLightView = (TextView) findViewById(R.id.camera_preview_messageLightView);
        messageFocusView = (TextView) findViewById(R.id.camera_preview_messageFocusView);
        messageContrastView = (TextView) findViewById(R.id.camera_preview_messageContrastView);
        progressIndicatorView = (ProgressIndicatorView) findViewById(R.id.activity_cameraProgressIndicatorView);
        finderPatternIndicatorView =
                (FinderPatternIndicatorView) findViewById(R.id.activity_cameraFinderPatternIndicatorView);

        hasTimeLapse = StripTest.getInstance().getBrand(brandName).hasTimeLapse();
        numPatches = StripTest.getInstance().getBrand(brandName).getPatches().size();
        patches = StripTest.getInstance().getBrand(brandName).getPatches();

        TextView durationView = (TextView) findViewById(R.id.camera_preview_messageDurationView);

        if(hasTimeLapse)
        {
            int duration = (int) Math.ceil(StripTest.getInstance().getBrand(brandName).getDuration());
            progressIndicatorView.setTotalSteps(numPatches);
            progressIndicatorView.setDuration(duration);
            progressIndicatorView.setPatches(patches);

            durationView.append(": " + String.valueOf(duration) + " " + getString(R.string.seconds_abbr));
        }
        else
        {
            progressIndicatorView.setVisibility(View.GONE);
            durationView.setVisibility(View.INVISIBLE);
        }
    }

    private void init()
    {

        // Create an instance of Camera
        mCamera = TheCamera.getCameraInstance();

        previewCallback = MyPreviewCallback.getInstance(this);

        if(mCamera!=null) {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new BaseCameraView(this, mCamera);
            preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.removeAllViews();
            preview.addView(mPreview);

            //find the transparent view in which to show part of the preview
            final View transView = findViewById(R.id.transparent_window);

            //enlarge the transparent view based on a factor of its width
            transView.post(new Runnable() {

                @Override
                public void run() {
                    RelativeLayout.LayoutParams params;
                    params = (RelativeLayout.LayoutParams) transView.getLayoutParams();
                    params.height = (int) Math.round(transView.getWidth() * Constant.CROP_CAMERAVIEW_FACTOR);
                    transView.setLayoutParams(params);
                    transView.postInvalidate();

                }
            });
        }



    }
    public void onPause()
    {

        if(mCamera!=null) {

            mCamera.setOneShotPreviewCallback(null);

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if(mPreview!=null)
        {
            preview.removeView(mPreview);
            mPreview = null;
        }
        Log.d(TAG, "onPause OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

        super.onPause();

    }

    public void onStop()
    {
        Log.d(TAG, "onStop OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

        if(handler!=null)
            handler.removeCallbacks(startCountdownRunnable);

        super.onPause();

        super.onStop();
    }

    public void onResume()
    {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        super.onResume();

        FileStorage.deleteAll();

        Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }
    public void onStart()
    {
        super.onStart();

        Log.d(TAG, "onStart OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }

    private int timeLapsed = 0;
    private long initTimeMillis;
    private Runnable startCountdownRunnable = new Runnable() {
        @Override
        public void run() {

            progressIndicatorView.setTimeLapsed(timeLapsed);
            timeLapsed++;
            handler.postDelayed(this, 1000);
        }
    };
    private void startCountdown() {

        timeLapsed = 0;
        initTimeMillis = System.currentTimeMillis();

        handler.post(startCountdownRunnable);

        for(int i=0;i< patches.size();i++) {
            //there must be a timelapse between patches
            if(i>0 && patches.get(i).getTimeLapse() - patches.get(i-1).getTimeLapse() > 0) {
                handler.postDelayed(startNextPreview, (long) patches.get(i).getTimeLapse() * 1000);
            }
        }

    }

    @Override
    public void setCountQualityCheckResult(int count)
    {
        countQualityCheckIteration ++;
        countQualityCheckResult += count;
    }

    public void setCountQualityCheckResultZero()
    {
        countQualityCheckResult = 0;
    }

    @Override
    public void setCountQualityCheckIterationZero()
    {
        countQualityCheckIteration = 0;
    }

    @Override
    public void setStartButtonVisibility(boolean show)
    {

        startButton = (Button) findViewById(R.id.activity_cameraStartButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                v.setOnClickListener(null);
                v.setVisibility(View.GONE);

                startButtonClicked = true;

                startCountdown();
            }
        });

        Runnable showRunnable = new Runnable() {
            @Override
            public void run() {

                if(!startButtonClicked) {
                    startButton.setVisibility(View.VISIBLE);
                }
            }
        };


        Runnable hideRunnable = new Runnable() {
            @Override
            public void run() {
                startButton.setVisibility(View.GONE);

            }
        };

//        Runnable warnRunnable = new Runnable() {
//            @Override
//            public void run() {
//
//                getMessage(1);
//                handler.removeCallbacks(startNextPreview);
//                CustomDialog cd = new CustomDialog();
//                AlertDialog dialog = cd.createDialog(CameraActivity.this);
//
//                dialog.show();
//            }
//        };

//        System.out.println("***countQualityCheckResult: " + countQualityCheckResult +
//        " countQualityCheckIteration: " + countQualityCheckIteration);

        if(countQualityCheckResult > Constant.COUNT_QUALITY_CHECK_LIMIT)
        {
            handler.post(showRunnable);
        }
        else if(countQualityCheckIteration > Constant.COUNT_QUALITY_CHECK_LIMIT * 1.5)
        {
            handler.post(hideRunnable);
            setCountQualityCheckResultZero();
            setCountQualityCheckIterationZero();
            //handler.post(warnRunnable);
        }
        else
        {
            handler.post(hideRunnable);
        }

    }
    @Override
    public boolean start()
    {
        return startButtonClicked;
    }

    @Override
    public void showFinderPatterns(final List<FinderPattern> patterns, final Camera.Size size, final int color)
    {
        Runnable showFinderPatternsRunnable = new Runnable() {
            @Override
            public void run() {
                finderPatternIndicatorView.setColor(color);
                finderPatternIndicatorView.showPatterns(patterns, size);
            }
        };
        handler.post(showFinderPatternsRunnable);
    }
    @Override
    public void getMessage(int what) {
        if(mCamera!=null && mPreview!=null && !isFinishing()) {

            if (what == 0) {
                handler.post(startNextPreview);

            } else {

                handler.removeCallbacks(startNextPreview);
                mCamera.setOneShotPreviewCallback(null);
            }
        }
    }

    @Override
    public void showFocusValue(final double value)
    {
        final ImageView focusView = (ImageView) findViewById(R.id.activity_cameraImageViewFocus);

        Runnable showMessage = new Runnable() {
            @Override
            public void run() {

                if(messageFocusView !=null)
                    messageFocusView.setText(getString(R.string.focus) + ": " + String.format("%.0f",value) + " %");

                if(value > Constant.MIN_FOCUS_PERCENTAGE)
                {
                    focusView.setImageResource(R.drawable.focus_green);
                }
                else
                {
                    focusView.setImageResource(R.drawable.focus_red);
                }
            }
        };
        handler.post(showMessage);

    }
    @Override
    public void showMaxLuminosity(final double value){

        final ImageView exposureView = (ImageView) findViewById(R.id.activity_cameraImageViewExposure);

        Runnable showMessage = new Runnable() {
            @Override
            public void run() {
                if(messageLightView !=null)
                    messageLightView.setText(getString(R.string.light) +": " + String.format("%.0f",value) + " %");

                if(value > Constant.MIN_LUMINOSITY_PERCENTAGE)
                {
                    exposureView.setImageResource(R.drawable.exposure_green);
                }
                else
                {
                    exposureView.setImageResource(R.drawable.exposure_red);
                }

            }
        };
        handler.post(showMessage);
    }

    @Override
    public void showShadow(final double value){

        final ImageView contrastView = (ImageView) findViewById(R.id.activity_cameraImageViewContrast);

        Runnable showMessage = new Runnable() {
            @Override
            public void run() {
                if(messageContrastView !=null) {
                    messageContrastView.setText(getString(R.string.contrast));

                    if(value<101)
                    {
                        messageContrastView.append( ": " + String.format("%.0f", 100 - value) + " %");
                    }
                }
                if(value < Constant.MAX_SHADOW_PERCENTAGE)
                {
                    contrastView.setImageResource(R.drawable.contrast_green);
                }
                else
                {
                    contrastView.setImageResource(R.drawable.contrast_red);
                }

            }
        };
        handler.post(showMessage);
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

    private class StoreDataTask extends AsyncTask<Void, Void, Boolean> {

        private int patchCount;
        private byte[] data;
        private FinderPatternInfo info;
        private int format;
        private int width;
        private int height;

        private StoreDataTask(int patchCount, byte[] data, FinderPatternInfo info, int format, int width, int height) {
            this.patchCount = patchCount;
            this.data = data;
            this.info = info;
            this.format = format;
            this.width = width;
            this.height = height;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            FileStorage.writeByteArray(data, patchCount);
            String json = FinderPatternInfoToJson.toJson(info);
            FileStorage.writeFinderPatternInfoJson(patchCount, json);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean written) {
            //System.out.println("***data was written: " + patchCount + written);

            if(patchesCovered == patches.size()-1)
            {
                startDetectActivity(format, width, height);
            }
        }
    }

    //private int to keep track of preview data already stored
    private int patchesCovered = -1;
    @Override
    public void sendData(final byte[] data, long timeMillis, int format, int width, int height,
                         final FinderPatternInfo info) {


        //check if picture is taken on time for the patch.
        //assumed is that some tests require time for a color to develop.
        //reading may be done after that time, but not before.
        //NB: in case a strip is designed in a manner where the order in time is different from the
        //order in the json-array, this will not work. Patches with lower value for time-lapse
        //should come before others.
        for(int i=patchesCovered+1;i<patches.size();i++)
        {
            //in case the reading is done after the time lapse we want to save the data for all patches before the time-lapse...
            if(timeMillis > initTimeMillis + patches.get(i).getTimeLapse()*1000)
            {

                //...but we do not want to replace the already saved data with new
                patchesCovered = i;

                new StoreDataTask(i, data, info, format, width, height).execute();

                //System.out.println("***patchCount: " + i + " patchesCovered: " + patchesCovered);

            }
        }

        showProgress(patchesCovered+1);

        //continue until all patches are covered
        if (patchesCovered< numPatches -1) {

            Runnable clearFinderPatterns = new Runnable() {
                @Override
                public void run() {
                    mCamera.startPreview();
                    finderPatternIndicatorView.showPatterns(null, null);
                }
            };
            handler.postDelayed(clearFinderPatterns, 1000);

            return;
        }

    }
    private void startDetectActivity(int format, int width, int height)
    {
        //put Extras into intent
        detectStripIntent.putExtra(Constant.BRAND, brandName);
        detectStripIntent.putExtra(Constant.FORMAT, format);
        detectStripIntent.putExtra(Constant.WIDTH, width);
        detectStripIntent.putExtra(Constant.HEIGHT, height);

        detectStripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(detectStripIntent);

        this.finish();
    }

    @Override
    public void showProgress(final int which) {
        Runnable showProgress = new Runnable() {

            @Override
            public void run() {

                progressIndicatorView.setStepsTaken(which);

            }
        };
        handler.post(showProgress);
    }

    @Override
    public void playSound()
    {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.futurebeep2);
        mp.start();
    }

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
