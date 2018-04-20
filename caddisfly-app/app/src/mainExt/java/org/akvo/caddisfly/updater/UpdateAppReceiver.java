package org.akvo.caddisfly.updater;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class UpdateAppReceiver extends BroadcastReceiver {

    private static final String EXTRA_NOTIFICATION_ID = "android.intent.extra.NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
        }

        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        final Uri marketUrl = Uri.parse("market://details?id=" + context.getPackageName());
        Intent storeIntent = new Intent(Intent.ACTION_VIEW, marketUrl);
        storeIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(storeIntent);

    }
}
