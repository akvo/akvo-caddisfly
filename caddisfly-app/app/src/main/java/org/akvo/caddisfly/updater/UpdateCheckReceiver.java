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

package org.akvo.caddisfly.updater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

public class UpdateCheckReceiver extends BroadcastReceiver {

    public static final String PROCESS_RESPONSE = "org.akvo.caddisfly.intent.action.PROCESS_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            JSONObject jsonMessage = new JSONObject(intent.getStringExtra(UpdateIntentService.RESPONSE_MESSAGE));
            if (jsonMessage.has("version")) {
                int serverVersion = jsonMessage.getInt("version");
                PreferencesUtil.setLong(context, R.string.lastUpdateCheckKey, Calendar.getInstance().getTimeInMillis());
                int versionCode = 0;
                try {
                    versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (serverVersion > versionCode) {
                    PreferencesUtil.setInt(context, "serverVersionCode", serverVersion);
                    PreferencesUtil.setLong(context, R.string.updateInfoDateKey, Calendar.getInstance().getTimeInMillis());

                    if (!ApiUtil.isStoreVersion(context)) {
                        final String location = jsonMessage.getString("fileName");
                        String checksum = jsonMessage.getString("md5Checksum");
                        PreferencesUtil.setString(context, "updateChecksum", checksum);
                        //String versionName = jsonMessage.getString("versionName");

                        String fileName = location.substring(location.lastIndexOf('/') + 1);
                        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.DOWNLOAD, ""), fileName);

                        if (!file.exists()) {

                            FileHelper.cleanInstallFolder(false);

                            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                            Uri Download_Uri = Uri.parse(location);
                            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                            request.setAllowedOverRoaming(false);
                            request.setTitle(context.getString(R.string.appName));

                            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "Install/" + fileName);
                            PreferencesUtil.setLong(context, "downloadReference", downloadManager.enqueue(request));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            PreferencesUtil.setLong(context, R.string.lastUpdateCheckKey, Calendar.getInstance().getTimeInMillis());
            e.printStackTrace();
        }
    }
}