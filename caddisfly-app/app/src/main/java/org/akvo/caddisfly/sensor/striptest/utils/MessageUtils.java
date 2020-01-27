package org.akvo.caddisfly.sensor.striptest.utils;

import android.os.Handler;
import android.os.Message;

public class MessageUtils {

    public static void sendMessage(Handler handler, int messageId, int data) {
        Message message = Message.obtain(handler, messageId);
        message.arg1 = data;
        message.sendToTarget();
    }

}
