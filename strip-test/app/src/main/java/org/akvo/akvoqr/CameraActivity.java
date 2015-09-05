package org.akvo.akvoqr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.ui.ProgressIndicatorView;
import org.akvo.akvoqr.util.Constant;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by linda on 7/7/15.
 */
public class CameraActivity extends BaseCameraActivity implements CameraViewListener{

    private Camera mCamera;
    private FrameLayout preview;
    private BaseCameraView mPreview;
    MyPreviewCallback previewCallback;
    private String TAG = "CameraActivity";
    private Intent intent;
    private android.os.Handler handler;
    private TextView messageView;
    private ProgressIndicatorView progressIndicatorView;
    private boolean testing = false;
    private String brand;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(getIntent().getStringExtra(Constant.BRAND)!=null)
        {
            this.brand = getIntent().getStringExtra(Constant.BRAND);
        }
        intent = new Intent(this, ResultActivity.class);
        handler = new Handler(Looper.getMainLooper());

        messageView = (TextView) findViewById(R.id.camera_preview_messageView);
        progressIndicatorView = (ProgressIndicatorView) findViewById(R.id.activity_cameraProgressIndicatorView);
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
        MyPreviewCallback.firstTime = true;
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
        Log.d(TAG, "onResume OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }
    public void onStart()
    {
        super.onStart();

        Log.d(TAG, "onStart OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

    }

    @Override
    public void getMessage(int what) {
        if(mCamera!=null && !isFinishing()) {
            if (what == 0) {

                mCamera.setOneShotPreviewCallback(previewCallback);

            } else {

                mCamera.setOneShotPreviewCallback(null);
            }
        }
    }

    @Override
    public void sendMats(ArrayList<Mat> mats)
    {

        intent.putExtra(Constant.MAT, mats);
        intent.putExtra(Constant.BRAND, brand);
        startActivity(intent);

        this.finish();
    }

    @Override
    public void sendData(byte[] data, int format, int width, int height) {

        try {
            System.out.println("***image format in CameraActivity: " + format +
                    " data: " + data.length + "size: " + width + ", " + height);


            intent.putExtra("data", data);
            intent.putExtra("format", format);
            intent.putExtra("width", width);
            intent.putExtra("height", height);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setBitmap(Bitmap bitmap) {

            double ratio = (double) bitmap.getHeight() / (double) bitmap.getWidth();
            int width = 800;
            int height = (int) Math.round(ratio * width);
//            System.out.println("***bitmap width: " + bitmap.getWidth() + " height: " + bitmap.getHeight());
//            System.out.println("***bitmap calc width: " + width + " height: " + height + " ratio: " + ratio);
            try {
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            sendData(bitmapdata, ImageFormat.RGB_565, bitmap.getWidth(), bitmap.getHeight());

            bitmap.recycle();

            finish();

    }

    @Override
    public Mat getCalibratedImage(Mat mat)
    {
        CalibrationCard calibrationCard = new CalibrationCard();
        return calibrationCard.calibrateImage(CameraActivity.this, mat);

    }

    private ProgressDialog progress;
    @Override
    public void showProgress(final int which) {


        if(which == 0)
        {
            Runnable showMessage = new Runnable() {
                @Override
                public void run() {
                    if(messageView!=null)
                        messageView.setText("Looking for finder patterns.\nPlease hold camera above striptest.");
                }
            };
            handler.post(showMessage);
        }
        else {
            Runnable showProgress = new Runnable() {

                @Override
                public void run() {

                    progressIndicatorView.setStepsTaken(which);

                    if(messageView!=null)
                        messageView.setText("");

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
    }

    @Override
    public void dismissProgress() {
        handler.post(dismissProgress);
    }

//
//    private Runnable showProgress = new Runnable() {
//
//        @Override
//        public void run() {
//            if(progress==null)
//            {
//                progress = new ProgressDialog(CameraActivity.this);
//                progress.setTitle(getString(R.string.calibrating));
//                progress.setMessage(getString(R.string.please_wait));
//                progress.show();
//            }
//        }
//    };

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
    public String getBrand() {
        return brand;
    }
}
