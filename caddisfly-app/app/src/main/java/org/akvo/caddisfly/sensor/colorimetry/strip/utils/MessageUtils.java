package org.akvo.caddisfly.sensor.colorimetry.strip.utils;

import android.os.Handler;
import android.os.Message;

/**
 * Created by markwestra on 19/05/2017
 */
public class MessageUtils {

    public static void sendMessage(Handler handler, int messageId, int data) {
        Message message = Message.obtain(handler, messageId);
        message.arg1 = data;
        message.sendToTarget();
    }

}
