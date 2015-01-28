package org.akvo.caddisfly.ui.activity;

import android.app.Activity;

// dummy activity to get permission for usb device
public class UsbPermissionActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
