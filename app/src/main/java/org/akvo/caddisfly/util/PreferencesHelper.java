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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.akvo.caddisfly.R;

public class PreferencesHelper {

    public static final String FOLDER_NAME_KEY = "currentFolderName";

    public static final String CURRENT_LOCATION_ID_KEY = "currentLocationId";

    public static final String CURRENT_TEST_ID_KEY = "currentTestId";

    public static final String CURRENT_TEST_TYPE_KEY = "currentTestTypeId";

    private PreferencesHelper() {
    }

    public static long getCurrentTestId(Context context, Intent intent, Bundle bundle) {
        long id = -1;
        if (bundle != null) {
            id = bundle.getLong(CURRENT_TEST_ID_KEY, -1);
        }
        if (intent != null && id == -1) {
            id = intent.getLongExtra(CURRENT_TEST_ID_KEY, -1);
        }
        if (id == -1) {
            return PreferencesUtils.getLong(context, CURRENT_TEST_ID_KEY);
        }
        return id;
    }

    public static long getCurrentLocationId(Context context, Intent intent) {
        long id = -1;
        if (intent != null) {
            id = intent.getLongExtra(CURRENT_LOCATION_ID_KEY, -1);
        }
        if (id == -1) {
            return PreferencesUtils.getLong(context, CURRENT_LOCATION_ID_KEY);
        }
        return id;
    }

 /*   public static int getCurrentTestTypeId(Context context, Intent intent) {
        int id = -1;
        if (intent != null) {
            id = intent.getIntExtra(CURRENT_TEST_TYPE_KEY, -1);
        }
        if (id == -1) {
            return PreferencesUtils.getInt(context, CURRENT_TEST_TYPE_KEY, -1);
        }
        return id;

    }*/

    public static void incrementPhotoTakenCount(Context context) {
        int count = PreferencesUtils.getInt(context, R.string.currentSamplingCountKey, 0);
        PreferencesUtils.setInt(context, R.string.currentSamplingCountKey, ++count);
    }
}
