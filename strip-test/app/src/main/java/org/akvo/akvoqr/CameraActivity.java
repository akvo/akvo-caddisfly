package org.akvo.akvoqr;

import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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

import java.io.IOException;
import java.util.List;

/**
 * Created by linda on 7/7/15.
 */
public class CameraActivity extends BaseCameraActivity implements CameraViewListener{

    private Camera mCamera;
    private FrameLayout preview;
    private BaseCameraView mPreview;
    MyPreviewCallback previewCallback;
    private final String TAG = "CameraActivity";
    private android.os.Handler handler;
    private TextView messageLightView;
    private TextView messageFocusView;
    private ProgressIndicatorView progressIndicatorView;
    private FinderPatternIndicatorView finderPatternIndicatorView;
    private String brandName;
    private boolean hasTimeLapse;
    private List<StripTest.Brand.Patch> patches;
    private int numPatches;
    // private int patchCount = 0;
    private boolean startButtonClicked = false;
    private Intent detectStripIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(getIntent().getStringExtra(Constant.BRAND)!=null)
        {
            this.brandName = getIntent().getStringExtra(Constant.BRAND);
        }

        detectStripIntent = new Intent(this, DetectStripTimeLapseActivity.class);

        handler = new Handler(Looper.getMainLooper());

        messageLightView = (TextView) findViewById(R.id.camera_preview_messageLightView);
        messageFocusView = (TextView) findViewById(R.id.camera_preview_messageFocusView);
        progressIndicatorView = (ProgressIndicatorView) findViewById(R.id.activity_cameraProgressIndicatorView);
        finderPatternIndicatorView =
                (FinderPatternIndicatorView) findViewById(R.id.activity_cameraFinderPatternIndicatorView);

        hasTimeLapse = StripTest.getInstance().getBrand(brandName).hasTimeLapse();
        numPatches = StripTest.getInstance().getBrand(brandName).getPatches().size();
        patches = StripTest.getInstance().getBrand(brandName).getPatches();

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.activity_cameraMainRelativeLayout);
        Button startButton = new Button(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        startButton.setText(getResources().getString(R.string.start));
        startButton.setBackgroundResource(R.drawable.button_start_selector);

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

        relativeLayout.addView(startButton, params);

        if(hasTimeLapse)
        {
            int duration = (int) Math.ceil(StripTest.getInstance().getBrand(brandName).getDuration());
            progressIndicatorView.setTotalSteps(numPatches);
            progressIndicatorView.setDuration(duration);
            progressIndicatorView.setPatches(patches);

            TextView durationView = (TextView) findViewById(R.id.camera_preview_messageDurationView);
            durationView.append(String.valueOf(duration) + " sec.");
        }
        else
        {
            progressIndicatorView.setVisibility(View.GONE);

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
            preview.addView(mPreview);
        }


    }
    public void onPause()
    {
        mCamera.setOneShotPreviewCallback(null);

        if(mCamera!=null) {

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
        super.onResume();
        if(mCamera!=null)
        {
            Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

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

        FileStorage.deleteAll();

        //Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

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

        for(StripTest.Brand.Patch p: patches) {
            handler.postDelayed(startNextPreview, (long) p.getTimeLapse() * 1000);
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

                if(previewCallback!=null)
                    mCamera.setOneShotPreviewCallback(previewCallback);

            } else {

                mCamera.setOneShotPreviewCallback(null);
            }
        }
    }

    @Override
    public void showFocusValue(final double value)
    {
        Runnable showMessage = new Runnable() {
            @Override
            public void run() {
                if(messageFocusView !=null)
                    messageFocusView.setText("Sharpness: " + value);
            }
        };
        handler.post(showMessage);
    }
    @Override
    public void showMaxLuminosity(final double value){

        Runnable showMessage = new Runnable() {
            @Override
            public void run() {
                if(messageLightView !=null)
                    messageLightView.setText("Light: " + value);
            }
        };
        handler.post(showMessage);
    }

    private Runnable startNextPreview = new Runnable() {
        @Override
        public void run() {
            if(mCamera!=null && previewCallback!=null)
                mCamera.setOneShotPreviewCallback(previewCallback);
        }
    };

    private class StoreDataTask extends AsyncTask<Void, Void, Boolean> {

        private int patchCount;
        private byte[] data;
        private FinderPatternInfo info;

        private StoreDataTask(int patchCount, byte[] data, FinderPatternInfo info) {
            this.patchCount = patchCount;
            this.data = data;
            this.info = info;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean written = FileStorage.writeByteArray(data, patchCount);
            String json = FinderPatternInfoToJson.toJson(info);
            FileStorage.writeFinderPatternInfoJson(patchCount, json);

            return written;
        }

        @Override
        protected void onPostExecute(Boolean written) {
//            System.out.println("***data was written: " + written);
        }
    }
    @Override
    public void sendData(final byte[] data, long timeMillis, int format, int width, int height,
                         final FinderPatternInfo info, double mSize) {

        int patchCount = 0;

        //check if picture is taken on time for the patch
        //assumed is that some tests require time for a color to develop
        //reading may be done after that time, but not before
        for(int i=0;i<patches.size();i++)
        {
            //System.out.println("***patchCount time diff milliseconds: " + (timeMillis  - (initTimeMillis + patches.get(i).getTimeLapse()*1000)));

            if(timeMillis > initTimeMillis + patches.get(i).getTimeLapse()*1000)
            {
                patchCount = i;
                new StoreDataTask(patchCount, data, info).execute();
            }
        }

        //System.out.println("***patchCount: " + patchCount);

        showProgress(patchCount+1);

        if (patchCount < numPatches -1) {

            Runnable clearFinderPatterns = new Runnable() {
                @Override
                public void run() {
                    finderPatternIndicatorView.showPatterns(null, null);
                }
            };
            handler.postDelayed(clearFinderPatterns, 1000);

            return;
        }

        detectStripIntent.putExtra(Constant.BRAND, brandName);
        detectStripIntent.putExtra(Constant.FORMAT, format);
        detectStripIntent.putExtra(Constant.WIDTH, width);
        detectStripIntent.putExtra(Constant.HEIGHT, height);
        detectStripIntent.putExtra(Constant.MODULE_SIZE, mSize);

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


}
