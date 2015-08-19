package org.akvo.caddisfly.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.AppPreferences;
import org.akvo.caddisfly.util.ApiUtil;

/**
 * The base activity with common functions
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiUtil.lockScreenOrientation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        changeActionBarStyleBasedOnCurrentMode();
    }

    /**
     * Changes the action bar style depending on if the app is in user mode or diagnostic mode
     * This serves as a visual indication as to what mode the app is running in
     */
    void changeActionBarStyleBasedOnCurrentMode() {
        if (AppPreferences.isDiagnosticMode(this)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                        ContextCompat.getColor(this, R.color.diagnostic)));
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                        ContextCompat.getColor(this, R.color.action_bar)));
            }
        }
    }
}
