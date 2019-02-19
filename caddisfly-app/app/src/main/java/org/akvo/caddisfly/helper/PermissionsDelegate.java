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

package org.akvo.caddisfly.helper;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsDelegate {

    private static final int REQUEST_CODE = 100;
    private final Activity activity;

    public PermissionsDelegate(Activity activity) {
        this.activity = activity;
    }

    public boolean hasPermissions(String[] permissions) {

        for (String permission :
                permissions) {
            int permissionCheckResult = ContextCompat.checkSelfPermission(
                    activity, permission
            );

            if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

        }
        return true;
    }

    public void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                REQUEST_CODE
        );
    }

    public void requestPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
        );
    }

    public boolean resultGranted(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean resultGranted(int requestCode, int[] grantResults) {

        if (requestCode != REQUEST_CODE) {
            return false;
        }

        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
