/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;

import java.util.Calendar;

public class UpdateCheckTask extends AsyncTask<Void, Void, Void> {

    private static UpdateChecker checker;

    private final Context mContext;

    private final boolean mBackground;

    private final boolean mPreviousVersion;

    private ProgressDialog progressDialog;

    public UpdateCheckTask(Context context, boolean background, boolean previousVersion) {
        mContext = context;
        mBackground = background;
        mPreviousVersion = previousVersion;
    }

/*
    public static String getPreviousVersion(Context context) {
        try {
            int version = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
            return String.valueOf(version - 1);

        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
*/

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (NetworkUtils.isOnline(mContext)) {
            checker = new UpdateChecker(mContext, false);
            if (mPreviousVersion) {
                String url = Config.UPDATE_URL.replace(".apk", "_prev" + ".apk");
                checker.downloadAndInstall(url, true);
                this.cancel(true);
            } else {
                if (!mBackground) {
                    progressDialog = new ProgressDialog(mContext);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage(mContext.getString(R.string.checkingForUpdates));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            }
        } else {
            this.cancel(true);
            if (!mBackground) {
                NetworkUtils.checkInternetConnection(mContext);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {

        boolean updateAvailable = PreferencesUtils
                .getBoolean(mContext, R.string.updateAvailable, false);

        if (!updateAvailable) {
            if (checker.checkForUpdateByVersionCode(Config.UPDATE_CHECK_URL)) {
                PreferencesUtils.setLong(mContext, R.string.lastUpdateCheck,
                        Calendar.getInstance().getTimeInMillis());
                if (checker.isUpdateAvailable()) {
                    PreferencesUtils.setBoolean(mContext, R.string.updateAvailable, true);
                }
            }
        } else {
            checker.setUpdateAvailable();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (checker.isUpdateAvailable()) {

            AlertUtils.askQuestion(mContext, R.string.appUpdate, R.string.askForUpdate,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checker.downloadAndInstall(Config.UPDATE_URL, false);
                            PreferencesUtils.removeKey(mContext, R.string.updateAvailable);
                        }
                    }, null
            );
        } else {

            if (!mBackground) {
                AlertUtils.showMessage(mContext, R.string.appName, R.string.alreadyHaveLatest);
            }
        }
    }
}
