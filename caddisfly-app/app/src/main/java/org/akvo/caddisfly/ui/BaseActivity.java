package org.akvo.caddisfly.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.AppPreferences;
import org.akvo.caddisfly.util.ApiUtils;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiUtils.lockScreenOrientation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkDiagnosticActionBar();
    }

    void checkDiagnosticActionBar() {
        if (AppPreferences.isDiagnosticMode(this)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                        getResources().getColor(R.color.diagnostic)));
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                        getResources().getColor(R.color.action_bar)));
            }
        }
    }
}
