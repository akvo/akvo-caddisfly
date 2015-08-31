package org.akvo.caddisfly.sensor.turbidity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;

public class TurbidityStartActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 0;
    private static final int DELAY = 360000;
    private boolean alarmStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turbidity_start);

        setTitle(R.string.manageColiformsTest);

        final TextView textStatus = (TextView) findViewById(R.id.textStatus);

        final Button buttonStartTimer = (Button) findViewById(R.id.buttonManageTimer);
        if (isAlarmRunning()) {
            textStatus.setText("Status: Take photo every 1 minute");
            buttonStartTimer.setText(R.string.stop);
            alarmStarted = true;
        } else {
            textStatus.setText("Status: Inactive");
            buttonStartTimer.setText(R.string.start);
            alarmStarted = false;
        }

        buttonStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PendingIntent pendingIntent = getPendingIntent();
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                if (alarmStarted) {
                    buttonStartTimer.setEnabled(false);
                    buttonStartTimer.setText(R.string.start);
                    alarmStarted = false;

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();

                    buttonStartTimer.setEnabled(true);

                } else {

                    buttonStartTimer.setEnabled(false);
                    buttonStartTimer.setText(R.string.stop);

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, DELAY, pendingIntent);
                    } else {
                        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, DELAY, pendingIntent);
                    }

                    alarmStarted = true;
                    textStatus.setText("Status: Take photo every 1 minute");

                    buttonStartTimer.setEnabled(true);

                }
            }
        });
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, TurbidityActivity.class);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        return PendingIntent.getActivity(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

    }

    private boolean isAlarmRunning() {
        Intent intent = new Intent(getBaseContext(), TurbidityActivity.class);
        return PendingIntent.getActivity(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_NO_CREATE) != null;
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