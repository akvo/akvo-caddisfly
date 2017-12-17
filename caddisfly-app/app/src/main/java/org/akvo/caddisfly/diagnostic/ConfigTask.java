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

package org.akvo.caddisfly.diagnostic;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.ui.TestListActivity;
import org.akvo.caddisfly.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConfigTask extends AsyncTask<String, String, String> {

    private WeakReference<Context> contextRef;
    private TestListActivity.SyncCallbackInterface configSyncHandler;
    private ProgressDialog pd;

    public ConfigTask(Context context, TestListActivity.SyncCallbackInterface syncCallback) {
        contextRef = new WeakReference<>(context);
        this.configSyncHandler = syncCallback;
    }

    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(contextRef.get());
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();
    }

    protected String doInBackground(String... params) {


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
                Log.d("Response: ", "> " + line);

            }

            return buffer.toString();


        } catch (IOException e) {
            e.printStackTrace();
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
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (pd.isShowing()) {
            pd.dismiss();
        }

        File path = FileHelper.getFilesDir(FileHelper.FileType.EXP_CONFIG, "");
        FileUtil.saveToFile(path, "tests.json", result);

        configSyncHandler.onDownloadFinished();
        Toast.makeText(contextRef.get(), "Experimental tests synced", Toast.LENGTH_LONG).show();
    }
}