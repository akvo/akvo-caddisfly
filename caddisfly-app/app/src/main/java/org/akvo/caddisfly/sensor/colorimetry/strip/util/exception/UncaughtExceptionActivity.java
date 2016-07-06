package org.akvo.caddisfly.sensor.colorimetry.strip.util.exception;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

public class UncaughtExceptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uncaught_exception);

        String error = getIntent().getStringExtra(Constant.ERROR);

        if (error != null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_uncaught_exceptionLinearLayout);
            TextView view = new TextView(this);
            view.setText(error);
            layout.addView(view);

        }
    }
}
