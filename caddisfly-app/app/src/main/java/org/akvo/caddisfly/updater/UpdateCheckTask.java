/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.updater;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.NotificationScheduler;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateCheckTask extends AsyncTask<String, String, String> {

    private final WeakReference<Context> contextRef;

    public UpdateCheckTask(Context context) {
        contextRef = new WeakReference<>(context);
    }

    protected String doInBackground(String... params) {

        Context context = contextRef.get();
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (PreferencesUtil.getInt(contextRef.get(), "serverVersionCode", 0) < versionCode) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();


            } catch (IOException e) {
                e.printStackTrace();
                CaddisflyApp.setNextUpdateCheck(context, AlarmManager.INTERVAL_HALF_HOUR);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Context context = contextRef.get();
        int versionCode = ApiUtil.getAppVersionCode(context);
        int serverVersion = PreferencesUtil.getInt(context, "serverVersionCode", 0);

        if (result != null) {
            try {
                JSONObject jsonMessage = new JSONObject(result);
                if (jsonMessage.has("version")) {
                    serverVersion = jsonMessage.getInt("version");
                    if (serverVersion > versionCode) {
                        PreferencesUtil.setInt(context, "serverVersionCode", serverVersion);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (serverVersion > versionCode) {

            NotificationScheduler.showNotification(context,
                    contextRef.get().getString(R.string.updateTitle),
                    contextRef.get().getString(R.string.updateAvailable));
        }
    }
}