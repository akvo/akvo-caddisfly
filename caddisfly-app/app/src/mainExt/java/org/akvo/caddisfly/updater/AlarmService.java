package org.akvo.caddisfly.updater;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.helper.ApkHelper;
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

        if (!ApkHelper.isNonStoreVersion(this)) {
            if (NetUtil.isNetworkAvailable(this)) {

                UpdateCheck.setNextUpdateCheck(this, AlarmManager.INTERVAL_DAY * 3);

                UpdateCheckTask updateCheckTask = new UpdateCheckTask(this);
                Date todayDate = Calendar.getInstance().getTime();

                updateCheckTask.execute(AppConfig.UPDATE_CHECK_URL + "?" + todayDate.getTime());
            } else {
                UpdateCheck.setNextUpdateCheck(this, AlarmManager.INTERVAL_HALF_HOUR);
            }
        }
        return START_NOT_STICKY;
    }

}