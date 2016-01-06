package org.akvo.akvoqr;

import android.test.ActivityInstrumentationTestCase2;

import org.akvo.akvoqr.camera_strip.CameraActivity;

/**
 * Created by linda on 8/7/15.
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<CameraActivity> {

    public ActivityTest() {
        super(CameraActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void test1()
    {
       CameraActivity activity = getActivity();

        activity.onResume();


    }
}
