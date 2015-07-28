package org.akvo.akvoqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

       intent = new Intent(this, ResultActivity.class);
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

        MyPreviewCallback.firstTime = true;
        super.onPause();

    }

    public void onStop()
    {
        Log.d(TAG, "onStop OUT mCamera, mCameraPreview: " + mCamera + ", " + mPreview);

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

    boolean focused;
    @Override
    public void getMessage(int what) {
        if(mCamera!=null && !isFinishing()) {
            if (what == 0) {


//                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
//                        @Override
//                        public void onAutoFocus(boolean success, Camera camera) {
//                            if (success) focused = true;
//
//                        }
//                    });
                mCamera.setOneShotPreviewCallback(previewCallback);

            } else {

                mCamera.setOneShotPreviewCallback(null);
            }
        }
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
        mCamera.setOneShotPreviewCallback(null);


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        sendData(bitmapdata, ImageFormat.RGB_565, bitmap.getWidth(), bitmap.getHeight());

        bitmap.recycle();

        finish();
    }
}
