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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import org.akvo.caddisfly.R;

public class NetworkUtils {

    public static boolean checkInternetConnection(final Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {

            AlertUtils.showAlert(context, R.string.noInternetConnection, R.string.enableInternet,
                    R.string.settings,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
        }

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isOnline(Context context) {
        if (context != null) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                return cm.getActiveNetworkInfo().isConnectedOrConnecting();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}