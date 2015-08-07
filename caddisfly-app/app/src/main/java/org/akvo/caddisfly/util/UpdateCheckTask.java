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

import java.util.Calendar;

public class UpdateCheckTask extends AsyncTask<Void, Void, Void> {

    private static UpdateChecker checker;

    private final Context mContext;
    private final String mVersion;

    private final boolean mBackground;


    private ProgressDialog progressDialog;

    public UpdateCheckTask(Context context, boolean background, String version) {
        mContext = context;
        mBackground = background;
        mVersion = version;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (NetworkUtils.isOnline(mContext)) {
            checker = new UpdateChecker(mContext, false);
            if (!mBackground) {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(mContext.getString(R.string.updateCheckingFor));
                progressDialog.setCancelable(false);
                progressDialog.show();
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
                .getBoolean(mContext, R.string.updateAvailableKey, false);

        if (!updateAvailable) {
            if (checker.checkForUpdateByVersionCode(AppConfig.UPDATE_CHECK_URL + "?" + mVersion)) {
                PreferencesUtils.setLong(mContext, R.string.lastUpdateCheckKey,
                        Calendar.getInstance().getTimeInMillis());
                if (checker.isUpdateAvailable()) {
                    PreferencesUtils.setBoolean(mContext, R.string.updateAvailableKey, true);
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
            alertUpdateAvailable();
        } else if (!mBackground) {
            alertUpdateNotFound();
        }

    }

    private void alertUpdateNotFound() {
        AlertUtils.showMessage(mContext, R.string.noUpdate, R.string.updatedAlready);
    }

    private void alertUpdateAvailable() {
        AlertUtils.askQuestion(mContext, R.string.updateAvailable, R.string.updateRequest,
                R.string.update, R.string.notNow, false,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checker.downloadAndInstall(AppConfig.UPDATE_URL + "?" + mVersion);
                        PreferencesUtils.removeKey(mContext, R.string.updateAvailableKey);
                    }
                }
        );
    }


}
