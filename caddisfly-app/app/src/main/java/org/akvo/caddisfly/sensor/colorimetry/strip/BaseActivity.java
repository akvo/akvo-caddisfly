package org.akvo.caddisfly.sensor.colorimetry.strip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected static ResultListener listener;

    public static void setResultListener(AppCompatActivity activity) {
        listener = (ResultListener) activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public interface ResultListener {

        void onResult(String result, String imagePath);
    }


}
