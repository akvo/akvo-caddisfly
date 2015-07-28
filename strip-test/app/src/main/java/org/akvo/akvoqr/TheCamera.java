package org.akvo.akvoqr;

import android.hardware.Camera;

/**
 * Created by linda on 7/7/15.
 */
public class TheCamera
{

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        if(c==null) {
            try {
                c = Camera.open(); // attempt to get a Camera instance
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
            }
        }
        return c; // returns null if camera is unavailable
    }
}
