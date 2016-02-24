package org.akvo.caddisfly.sensor.colorimetry.strip;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BaseActivity extends AppCompatActivity
{

    public interface ResultListener{

        void onResult(String result, String imagePath);
    }

    protected static ResultListener listener;

    public static void setResultListener(AppCompatActivity activity)
    {
        listener = (ResultListener) activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}
