package org.akvo.akvoqr;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.test.ApplicationTestCase;
import android.test.MoreAsserts;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    private Application application;

    public ApplicationTest() {
        super(Application.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        application = getApplication();

    }

    public void testCorrectVersion() throws Exception {
        PackageInfo info = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
        assertNotNull(info);
        MoreAsserts.assertMatchesRegex("\\d\\.\\d", info.versionName);
    }

    public void teststripTest()
    {
        Intent intent = new Intent(application.getApplicationContext(), CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);


    }
}