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

package org.akvo.caddisfly.component;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.PreferencesUtils;

public class ConfirmCheckBoxPreference extends CheckBoxPreference {

    public ConfirmCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        if (!PreferencesUtils.getBoolean(getContext(), R.string.saveOriginalPhotoKey, false)) {
            AlertUtils.askQuestion(getContext(),
                    R.string.saveOriginalPhoto,
                    R.string.saveOriginalPhotoConfirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PreferencesUtils
                                    .setBoolean(getContext(), R.string.saveOriginalPhotoKey, true);
                            setChecked(true);
                        }
                    }
            );
        } else {
            PreferencesUtils.setBoolean(getContext(), R.string.saveOriginalPhotoKey, false);
            setChecked(false);
        }
    }
}
