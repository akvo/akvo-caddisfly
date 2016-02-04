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

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.NetUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UpdateHelper {

    public static UpdateCheckReceiver checkUpdate(final Context context, boolean showMessage) {

        final long FIVE_DAYS = 60000L * 60L * 24L * 5L;

        DownloadManager downloadManager;
        UpdateCheckReceiver updateCheckReceiver = null;
        long updateLastCheck = PreferencesUtil.getLong(context, R.string.lastUpdateCheckKey);
        Calendar currentDate = Calendar.getInstance();

        if (ApiUtil.isStoreVersion(context)) {

            if (currentDate.getTimeInMillis() - updateLastCheck > AppConfig.UPDATE_CHECK_INTERVAL) {

                int versionCode = ApiUtil.getAppVersionCode(context);
                if (PreferencesUtil.getInt(context, "serverVersionCode", 0) > versionCode) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.update);
                    if (showMessage) {
                        long updateInfoDate = PreferencesUtil.getLong(context, R.string.updateInfoDateKey);
                        if (currentDate.getTimeInMillis() - updateInfoDate > AppConfig.UPDATE_CHECK_INTERVAL) {
                            final Uri marketUrl = Uri.parse("market://details?id=" + context.getPackageName());
                            builder.setMessage(R.string.updateAvailable)
                                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            PreferencesUtil.setLong(context, R.string.lastUpdateCheckKey,
                                                    Calendar.getInstance().getTimeInMillis());

                                            context.startActivity(new Intent(Intent.ACTION_VIEW, marketUrl));
                                        }
                                    })
                                    .setNegativeButton(R.string.remindLater, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            PreferencesUtil.setLong(context, R.string.lastUpdateCheckKey,
                                                    Calendar.getInstance().getTimeInMillis());
                                        }
                                    });
                            builder.create().show();
                        }
                    }
                } else {
                    updateCheckReceiver = checkForUpdate(context);
                }
            }
        } else {

            //If five days since last server check then check server again instead of already downloaded file
            if (updateLastCheck > -1 && currentDate.getTimeInMillis() - updateLastCheck > FIVE_DAYS) {
                updateCheckReceiver = checkForUpdate(context);
            } else {

                if (currentDate.getTimeInMillis() - updateLastCheck > AppConfig.UPDATE_CHECK_INTERVAL) {

                    //Remove old items from the download manager queue
                    downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    Cursor cursor = downloadManager.query(new DownloadManager.Query());

                    boolean found = false;
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        if (context.getString(R.string.appName).equals(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)))) {
                            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                            switch (downloadStatus) {
                                case DownloadManager.STATUS_SUCCESSFUL:
                                    final String filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                                    final File file = new File(filePath);

                                    if (file.exists() && !found) {
                                        found = true;
                                        if (showMessage) {
                                            installUpdate(context, file);
                                        }
                                    } else {
                                        downloadManager.remove(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
                                    }
                                    break;
                                case DownloadManager.STATUS_FAILED:
                                    downloadManager.remove(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
                                    break;
                            }
                        }
                    }
                    cursor.close();

                    if (!found) {
                        //check server for updates
                        updateCheckReceiver = checkForUpdate(context);
                    }
                }
            }
        }
        return updateCheckReceiver;
    }

    private static UpdateCheckReceiver checkForUpdate(Context context) {
        if (NetUtil.isNetworkAvailable(context)) {
            IntentFilter filter = new IntentFilter(UpdateCheckReceiver.PROCESS_RESPONSE);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            UpdateCheckReceiver receiver = new UpdateCheckReceiver();
            context.registerReceiver(receiver, filter);
            context.startService(new Intent(context, UpdateIntentService.class));
            return receiver;
        }
        return null;
    }

    public static void installUpdate(final Context context, final File file) {

        //long downloadReference = PreferencesUtil.getLong(context, "downloadReference");
        //if (downloadReference == referenceId) {
        PreferencesUtil.setLong(context, R.string.lastUpdateCheckKey, Calendar.getInstance().getTimeInMillis());

        if (file != null) {

            int versionCode = 0;
            try {
                versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            int fileVersion;
            Pattern pattern = Pattern.compile("(\\d+).apk");
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                fileVersion = Integer.parseInt(matcher.group(1));
                if (fileVersion > versionCode) {
                    String fileChecksum = FileUtil.getMD5Checksum(file.getPath());
                    String checksum = PreferencesUtil.getString(context, "updateChecksum", "");
                    if (fileChecksum == null || !fileChecksum.equals(checksum.toUpperCase())) {
                        //Delete the file if the checksum does not match
                        // noinspection ResultOfMethodCallIgnored
                        file.delete();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.update);
                        builder.setMessage(R.string.updateAvailable)
                                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.remindLater, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        builder.create().show();
                    }
                } else {
                    //An older install file which can be deleted
                    // noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
        //}
    }
}