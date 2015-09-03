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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.WindowManager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.Camera2DialogFragment;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.CameraDialogFragment;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TurbidityActivity extends Activity {

    private static final int DELAY_INITIAL = 6000;
    private CameraDialog mCameraDialog;
    private SoundPoolPlayer sound;
    private PowerManager.WakeLock wakeLock;
    private boolean mDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turbidity);

        acquireWakeLock();

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();

        sound = new SoundPoolPlayer(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                AppPreferences.getUseCamera2Api(this)) {
            mCameraDialog = Camera2DialogFragment.newInstance();
        } else {
            mCameraDialog = CameraDialogFragment.newInstance();
        }

        mCameraDialog.setPictureTakenObserver(new CameraDialogFragment.PictureTaken() {
            @Override
            public void onPictureTaken(byte[] bytes, boolean completed) {
                sound.playShortResource(R.raw.beep);
                saveImage(bytes, 0);
                releaseResources();
                finish();
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void saveImage(byte[] data, double result) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, intentFilter);
        int batteryPercent = -1;

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPercent = (int) ((level / (float) scale) * 100);
        }

        String startDate = getIntent().getStringExtra("startDateTime");
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date());
        ImageUtil.saveImage(data, "COLIF" + File.separator + startDate, date + "_"
                + "" + "_" + String.format("%.2f", result)
                + "_" + batteryPercent + "_" + ApiUtil.getEquipmentId(this));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(this, TurbidityStartReceiver.class);
            intent.setAction(TurbidityConfig.ACTION_ALARM_RECEIVER);
            intent.putExtra("startDateTime", startDate);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    TurbidityConfig.INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            int delayHour = PreferencesUtil.getInt(getBaseContext(),
                    getString(R.string.cameraRepeatIntervalHourKey), 0);
            int delayMinute = PreferencesUtil.getInt(getBaseContext(),
                    getString(R.string.cameraRepeatIntervalMinuteKey), 1);

            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            ((delayHour * 60) + delayMinute) * 60000, pendingIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mDestroyed) {
                    getFragmentManager().beginTransaction()
                            .add(R.id.layoutContainer, mCameraDialog)
                            .commitAllowingStateLoss();

                    mCameraDialog.takePictures(1, DELAY_INITIAL);
                }
            }
        }, DELAY_INITIAL);
    }

    /**
     * Acquire a wake lock to prevent the screen from turning off during the analysis process
     */
    private void acquireWakeLock() {

        if (wakeLock == null || !wakeLock.isHeld()) {

            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP, "CameraSensorWakeLock");
            wakeLock.acquire();
        }
    }

    private void releaseResources() {
        if (mCameraDialog != null) {
            mCameraDialog.stopCamera();
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
    }
}
