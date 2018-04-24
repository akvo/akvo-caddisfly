package org.akvo.caddisfly.updater;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.util.NetUtil;

import java.util.Calendar;
import java.util.Date;

public class AlarmService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (NetUtil.isNetworkAvailable(this)) {

            CaddisflyApp.setNextUpdateCheck(this, AlarmManager.INTERVAL_DAY * 3);

            UpdateCheckTask updateCheckTask = new UpdateCheckTask(this);
            Date todayDate = Calendar.getInstance().getTime();

            updateCheckTask.execute(AppConfig.EXPERIMENT_TESTS_FFEM_URL + "?" + todayDate.getTime());
        } else {
            CaddisflyApp.setNextUpdateCheck(this, AlarmManager.INTERVAL_HALF_HOUR);
        }
        return START_NOT_STICKY;
    }

}