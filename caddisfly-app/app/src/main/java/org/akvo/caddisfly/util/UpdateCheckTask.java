/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Check for app updates in the background and alert if the user if update is available
 */
public class UpdateCheckTask extends AsyncTask<Void, Void, Void> {

    private static UpdateChecker updateChecker;

    private final Context mContext;
    private final boolean mSilent;
    private JSONObject updateInfo;
    private ProgressDialog progressDialog;

    /**
     * @param context the context
     * @param silent  check silently without showing an alert if update not found
     */
    public UpdateCheckTask(Context context, boolean silent) {
        mContext = context;
        mSilent = silent;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (NetworkUtils.isOnline(mContext)) {
            updateChecker = new UpdateChecker(mContext, false);
            if (!mSilent) {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(mContext.getString(R.string.updateCheckingFor));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        } else {
            this.cancel(true);
            if (!mSilent) {
                NetworkUtils.checkInternetConnection(mContext);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        updateInfo = updateChecker.checkForUpdateByVersionCode(AppConfig.UPDATE_CHECK_URL);
        PreferencesUtils.setLong(mContext, R.string.lastUpdateCheckKey,
                Calendar.getInstance().getTimeInMillis());
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (updateInfo == null) {
            if (!mSilent) {
                alertUpdateNotFound();
            }
        } else {

            try {
                String location = updateInfo.getString("fileName");
                String checksum = updateInfo.getString("md5Checksum");
                String versionName = updateInfo.getString("versionName");

                alertUpdateAvailable(location, checksum, versionName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Alert for update not found shown only if the user has manually requested an update check
     */
    private void alertUpdateNotFound() {
        AlertUtils.showMessage(mContext, R.string.noUpdate, R.string.updatedAlready);
    }

    /**
     * Alert for update available
     */
    private void alertUpdateAvailable(final String url, final String checksum, final String version) {
        AlertUtils.askQuestion(mContext, R.string.updateAvailable, R.string.updateRequest,
                R.string.update, R.string.notNow, false,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateChecker.downloadAndInstall(url, checksum, version);
                    }
                }
        );
    }
}