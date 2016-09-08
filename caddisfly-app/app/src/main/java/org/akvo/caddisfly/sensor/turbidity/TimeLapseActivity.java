/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.turbidity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.liquid.LiquidTimeLapsePreferenceFragment;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeLapseActivity extends BaseActivity {

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private SoundPoolPlayer sound;
    private TextView textSampleCount;
    private TextView textCountdown;
    private Calendar futureDate;
    private Runnable runnable;
    private Handler handler;
    private String mUuid;
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            sound.playShortResource(R.raw.beep);

            mUuid = getIntent().getDataString();

            int delayMinute;
            int numberOfSamples;

            File folder = FileHelper.getFilesDir(FileHelper.FileType.IMAGE,
                    intent.getStringExtra("savePath"));

            delayMinute = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    mUuid + "_IntervalMinutes", "1"));

            numberOfSamples = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    mUuid + "_NumberOfSamples", "1"));


            File[] files = folder.listFiles();
            if (files != null) {
                if (files.length >= numberOfSamples) {
                    TurbidityConfig.stopRepeatingAlarm(context, mUuid);
                    finish();
                } else {
                    textSampleCount.setText(String.format(Locale.getDefault(), "%s: %d of %d",
                            "Samples done", files.length, numberOfSamples));
                    futureDate = Calendar.getInstance();
                    futureDate.add(Calendar.MINUTE, delayMinute);
                }
            }
        }
    };

    private boolean showTimer = true;

    private void startCountdownTimer() {
        showTimer = true;
        handler = new Handler();
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (showTimer) {
                    handler.postDelayed(this, 1000);
                    try {
                        Calendar currentDate = Calendar.getInstance();
                        if (futureDate != null && !currentDate.after(futureDate)) {
                            long diff = futureDate.getTimeInMillis() - currentDate.getTimeInMillis();
                            long days = diff / (24 * 60 * 60 * 1000);
                            diff -= days * (24 * 60 * 60 * 1000);
                            long hours = diff / (60 * 60 * 1000);
                            diff -= hours * (60 * 60 * 1000);
                            long minutes = diff / (60 * 1000);
                            diff -= minutes * (60 * 1000);
                            long seconds = diff / 1000;
                            textCountdown.setText(String.format(Locale.getDefault(),
                                    "%02d:%02d:%02d", hours, minutes, seconds));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lapse);

        mUuid = getIntent().getDataString();

        setTitle("Analyzing");

        sound = new SoundPoolPlayer(this);

        final LinearLayout layoutWait = (LinearLayout) findViewById(R.id.layoutWait);
        final LinearLayout layoutDetails = (LinearLayout) findViewById(R.id.layoutDetails);

        Fragment fragment;
        Bundle bundle = new Bundle();


        final TextView textTitle = (TextView) findViewById(R.id.textTitle);

        CaddisflyApp.getApp().loadTestConfigurationByUuid(mUuid);
        final TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        switch (testInfo.getCode()) {
            case SensorConstants.FLUORIDE_ID:
                fragment = new LiquidTimeLapsePreferenceFragment();
                break;

            default:
                fragment = new TimeLapsePreferenceFragment();
                break;
        }

        bundle.putString("uuid", mUuid);
        fragment.setArguments(bundle);

        textTitle.setText(testInfo.getName());

        getFragmentManager().beginTransaction()
                .add(R.id.layoutContent4, fragment)
                .commit();

        layoutWait.setVisibility(View.VISIBLE);
        layoutDetails.setVisibility(View.GONE);

        if (TurbidityConfig.isAlarmRunning(this, mUuid)) {
            Log.e("TimeLapse", "Already Running Alarm");
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

        textSampleCount = (TextView) findViewById(R.id.textSampleCount);

        final Context context = this;

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutWait.setVisibility(View.GONE);
                layoutDetails.setVisibility(View.VISIBLE);

                Calendar startDate = Calendar.getInstance();
                PreferencesUtil.setString(context, R.string.turbiditySavePathKey,
                        testInfo.getName() + File.separator +
                                new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(startDate.getTime()));

                TurbidityConfig.setRepeatingAlarm(context, 25000, mUuid);

                String date = new SimpleDateFormat("dd MMM yyy HH:mm", Locale.US).format(startDate.getTime());
                ((TextView) findViewById(R.id.textSubtitle))
                        .setText(String.format(Locale.getDefault(), "%s %s", "Started", date));

                futureDate = Calendar.getInstance();
                futureDate.add(Calendar.SECOND, 25);

                TextView textInterval = (TextView) findViewById(R.id.textInterval);
                int interval = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                        mUuid + "_IntervalMinutes", "1"));

                textInterval.setText(String.format(Locale.getDefault(), "Every %d minutes", interval));

                startCountdownTimer();
            }
        });
        textCountdown = (TextView) findViewById(R.id.textCountdown);
    }

    @Override
    protected void onPause() {
        super.onPause();
        showTimer = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        startCountdownTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showTimer = false;
        handler.removeCallbacks(runnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, mUuid);
        overridePendingTransition(R.anim.slide_back_out, R.anim.slide_back_in);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        showTimer = false;
        handler.removeCallbacks(runnable);

        Toast.makeText(this, "Test cancelled", Toast.LENGTH_LONG).show();

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, mUuid);
    }
}