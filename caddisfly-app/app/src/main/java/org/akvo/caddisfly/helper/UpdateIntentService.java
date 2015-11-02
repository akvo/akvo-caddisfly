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

package org.akvo.caddisfly.helper;

import android.app.IntentService;
import android.content.Intent;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.ui.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateIntentService extends IntentService {

    public static final String REQUEST_STRING = "myRequest";
    public static final String RESPONSE_MESSAGE = "myResponseMessage";
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 30000;

    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String responseMessage;

        try {
            URL url = new URL(AppConfig.UPDATE_CHECK_URL);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);

            StringBuilder total = new StringBuilder();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            responseMessage = total.toString();
        } catch (Exception e) {
            responseMessage = e.getMessage();
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.UpdateCheckReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESPONSE_MESSAGE, responseMessage);
        sendBroadcast(broadcastIntent);
    }
}
