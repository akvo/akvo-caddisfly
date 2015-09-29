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
    private final String TAG = "CameraActivity"; //NON-NLS
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

        detectStripIntent = new Intent(this, DetectStripActivity.class);

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
        Log.d(TAG, "init");

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
            if(i==0 || (i>0 && patches.get(i).getTimeLapse() - patches.get(i-1).getTimeLapse() > 0)) {
                handler.postDelayed(startNextPreview, (long) patches.get(i).getTimeLapse() * 1000);
            }
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
                    messageFocusView.setText(getString(R.string.focus) + ": " + String.format("%.0f",value) + " %");
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
                    messageLightView.setText(getString(R.string.light) +": " + String.format("%.0f",value) + " %");
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

    private static boolean written;
    private class StoreDataTask extends AsyncTask<Void, Void, Boolean> {

        private int patchCount;
        private byte[] data;
        private FinderPatternInfo info;
        private int format;
        private int width;
        private int height;
        private double mSize;

        private StoreDataTask(int patchCount, byte[] data, FinderPatternInfo info, int format, int width, int height, double mSize) {
            this.patchCount = patchCount;
            this.data = data;
            this.info = info;
            this.format = format;
            this.width = width;
            this.height = height;
            this.mSize = mSize;
        }
        @Override
        protected void onPreExecute()
        {
            written = false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            written = FileStorage.writeByteArray(data, patchCount);
            String json = FinderPatternInfoToJson.toJson(info);
            FileStorage.writeFinderPatternInfoJson(patchCount, json);

            return written;
        }

        @Override
        protected void onPostExecute(Boolean written) {
            System.out.println("***data was written: " + patchesCovered + written);

            if(patchesCovered == patches.size()-1)
            {
                startDetectActivity(format, width, height, mSize);
            }
        }
    }

    //private int to keep track of preview data already stored
    private int patchesCovered = -1;
    @Override
    public void sendData(final byte[] data, long timeMillis, int format, int width, int height,
                         final FinderPatternInfo info, double mSize) {

        int patchCount = 0;

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
                patchCount = i;

                //...but we do not want to replace the already saved data with new
                patchesCovered = i;

                new StoreDataTask(patchCount, data, info, format, width, height, mSize).execute();

                System.out.println("***patchCount: " + patchCount + " patchesCovered: " + patchesCovered);

            }
        }

        showProgress(patchCount+1);

        //continue until all patches are covered
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

    }
    private void startDetectActivity(int format, int width, int height, double mSize)
    {
        //put Extras into intent
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
