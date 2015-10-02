package org.akvo.akvoqr;

import android.app.Activity;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * Created by linda on 7/6/15.
 */
public abstract class BaseCameraActivity  extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{

    //static{ System.loadLibrary("opencv_java3"); }
    static {

        if (!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");

        } else {
            Log.d("SUCCESS", "OpenCV loaded");
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("", "OpenCV loaded successfully");
                    // mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return null;
    }
}
