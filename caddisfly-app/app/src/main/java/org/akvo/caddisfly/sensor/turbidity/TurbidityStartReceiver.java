package org.akvo.caddisfly.sensor.turbidity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TurbidityStartReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_RECEIVER = "ACTION_ALARM_RECEIVER";

    public TurbidityStartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (ACTION_ALARM_RECEIVER.equals(intent.getAction())) {
                Intent intentActivity = new Intent(context, TurbidityActivity.class);
                intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentActivity);
            }
        }
    }
}
