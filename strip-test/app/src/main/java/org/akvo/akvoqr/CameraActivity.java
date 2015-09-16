package org.akvo.akvoqr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.detector.FinderPatternInfoToJson;
import org.akvo.akvoqr.ui.FinderPatternIndicatorView;
import org.akvo.akvoqr.ui.ProgressIndicatorView;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
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
    private boolean testing = false;
    private String brandName;
    private boolean hasTimeLapse;
    private List<StripTest.Brand.Patch> patches;
    private int numPatches;
    private int patchCount = 0;
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
        if(hasTimeLapse)
        {
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
                }
            });

            relativeLayout.addView(startButton, params);
            progressIndicatorView.setTotalSteps(numPatches);
        }
        else
        {
            progressIndicatorView.setVisibility(View.GONE);
            startButtonClicked = true;
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
        //MyPreviewCallback.firstTime = true;
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

        super.onPause();

        super.onStop();
    }

    public void onResume()
    {
        super.onResume();
        if(mCamera!=null)
        {
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
        patchCount = 0;
        Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }
    public void onStart()
    {
        super.onStart();

        Log.d(TAG, "onStart OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }

    @Override
    public boolean start()
    {
        return startButtonClicked;
    }

    @Override
    public void showFinderPatterns(final List<FinderPattern> patterns, final Camera.Size size)
    {
        Runnable showFinderPatternsRunnable = new Runnable() {
            @Override
            public void run() {
                if(patterns.size()==4)
                    finderPatternIndicatorView.setColor(Color.RED);
                else
                    finderPatternIndicatorView.setColor(Color.YELLOW);
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

    @Override
    public void sendMats(ArrayList<Mat> mats)
    {
//
//        detectStripIntent.putExtra(Constant.MAT, mats);
//        detectStripIntent.putExtra(Constant.BRAND, brandName);
//        startActivity(detectStripIntent);

        this.finish();
    }

    @Override
    public double getTimeLapseForPatch()
    {
        if(patchCount>0) {
            return patches.get(patchCount).getTimeLapse() - patches.get(patchCount-1).getTimeLapse();
        }
        else {
            return patches.get(patchCount).getTimeLapse();
        }
    }

    private Runnable startNextPreview = new Runnable() {
        @Override
        public void run() {
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
            System.out.println("***data was written: " + written);
        }
    }
    @Override
    public void sendData(final byte[] data, int format, int width, int height, final FinderPatternInfo info, double mSize) {

        System.out.println("***data sendData w, h: " + width + ", " + height + " format: " + format);
//        System.out.println("***info: " + info);
//        System.out.println("***data: " + data.length);

        if(hasTimeLapse) {

            new StoreDataTask(patchCount, data, info).execute();

            if (patchCount < numPatches -1) {

                patchCount++;
                showProgress(patchCount);

                Runnable clearFinderPatterns = new Runnable() {
                    @Override
                    public void run() {
                        finderPatternIndicatorView.showPatterns(null, null);
                    }
                };
                handler.postDelayed(clearFinderPatterns, 1000);

                handler.postDelayed(startNextPreview, (long) getTimeLapseForPatch() * 1000);

                return;
            }
            patchCount++;
            showProgress(patchCount);

        }
        else {

            detectStripIntent = new Intent(this, DetectStripActivity.class);
            detectStripIntent.putExtra(Constant.DATA, data);

            Bundle finderPatternBundle = new Bundle();
            finderPatternBundle.putDoubleArray(Constant.TOPLEFT,
                    new double[]{info.getTopLeft().getX(), info.getTopLeft().getY()});
            finderPatternBundle.putDoubleArray(Constant.TOPRIGHT,
                    new double[]{info.getTopRight().getX(), info.getTopRight().getY()});
            finderPatternBundle.putDoubleArray(Constant.BOTTOMLEFT,
                    new double[]{info.getBottomLeft().getX(), info.getBottomLeft().getY()});
            finderPatternBundle.putDoubleArray(Constant.BOTTOMRIGHT,
                    new double[]{info.getBottomRight().getX(), info.getBottomRight().getY()});

            detectStripIntent.putExtra(Constant.FINDERPATTERNBUNDLE, finderPatternBundle);
        }

        try {
            detectStripIntent.putExtra(Constant.BRAND, brandName);
            detectStripIntent.putExtra(Constant.FORMAT, format);
            detectStripIntent.putExtra(Constant.WIDTH, width);
            detectStripIntent.putExtra(Constant.HEIGHT, height);
            detectStripIntent.putExtra(Constant.MODULE_SIZE, mSize);

            detectStripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(detectStripIntent);

            this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public void setBitmap(Bitmap bitmap) {
//
//            double ratio = (double) bitmap.getHeight() / (double) bitmap.getWidth();
//            int width = 800;
//            int height = (int) Math.round(ratio * width);
////            System.out.println("***bitmap width: " + bitmap.getWidth() + " height: " + bitmap.getHeight());
////            System.out.println("***bitmap calc width: " + width + " height: " + height + " ratio: " + ratio);
//            try {
//                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
//            byte[] bitmapdata = bos.toByteArray();
//
//           // sendData(bitmapdata, ImageFormat.RGB_565, bitmap.getWidth(), bitmap.getHeight());
//
//            bitmap.recycle();
//
//            finish();
//
//    }

    @Override
    public Mat getCalibratedImage(Mat mat)
    {
        CalibrationCard calibrationCard = new CalibrationCard();
        return calibrationCard.calibrateImage(CameraActivity.this, mat);

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
    private ProgressDialog progress;
   /* @Override
    public void showProgress(final int which) {


        if(which == 0)
        {
            Runnable showMessage = new Runnable() {
                @Override
                public void run() {
                    if(messageLightView !=null)
                        messageLightView.setText("Looking for finder patterns.\nPlease hold camera above striptest.");
                }
            };
            handler.post(showMessage);
        }
        else {
            Runnable showProgress = new Runnable() {

                @Override
                public void run() {

                    progressIndicatorView.setStepsTaken(which);

                    if(messageLightView !=null)
                        messageLightView.setText("");

                    if (progress == null) {
                        progress = new ProgressDialog(CameraActivity.this);
                        switch (which) {
                            case 0:
                                progress.setTitle("Looking for finder patterns");
                                break;
                            case 1:
                                progress.setTitle(getString(R.string.calibrating));
                                break;
                            case 2:
                                progress.setTitle("Detecting strip");
                                break;
                            case 3:
                                progress.setTitle("Making bitmap");
                                break;
                            default:
                                progress.setTitle("Finished");
                        }
                        progress.setMessage(getString(R.string.please_wait));
                        progress.show();
                    }
                }
            };
            handler.post(showProgress);
        }
    }*/

    @Override
    public void dismissProgress() {
        handler.post(dismissProgress);
    }

    private Runnable dismissProgress = new Runnable() {
        @Override
        public void run() {
            if(progress!=null)
            {
                progress.dismiss();
                progress=null;
            }
        }
    };

    @Override
    public void playSound()
    {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.futurebeep2);
        mp.start();
    }

    @Override
    public String getBrandName() {
        return brandName;
    }
}
