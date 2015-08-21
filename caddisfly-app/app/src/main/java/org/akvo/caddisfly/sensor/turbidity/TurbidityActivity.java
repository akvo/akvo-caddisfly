package org.akvo.caddisfly.sensor.turbidity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;

public class TurbidityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turbidity);
    }

    @Override
    protected void onStart() {
        super.onStart();

        TestInfo testInfo = CaddisflyApp.getApp().currentTestInfo;

        Resources res = getResources();
        Configuration conf = res.getConfiguration();

        //set the title to the test contaminant name
        ((TextView) findViewById(R.id.textTitle)).setText(testInfo.getName(conf.locale.getLanguage()));

    }
}
