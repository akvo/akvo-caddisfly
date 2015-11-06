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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.util.PreferencesUtil;

public class DownloadReceiver extends BroadcastReceiver {

    public DownloadReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Delete all older apk install files and keep only the latest one
        FileHelper.cleanInstallFolder(true);

        //To Force an update check so that the update dialog gets shown
        PreferencesUtil.removeKey(context, R.string.lastUpdateCheckKey);
    }
}