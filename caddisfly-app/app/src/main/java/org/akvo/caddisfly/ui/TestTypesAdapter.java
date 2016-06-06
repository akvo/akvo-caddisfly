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

package org.akvo.caddisfly.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;

/**
 * Adapter to list the various contaminant test types
 */
class TestTypesAdapter extends ArrayAdapter<TestInfo> {

    private final Activity mActivity;

    private final TestInfo[] mTestInfoArray;

    public TestTypesAdapter(Activity activity, TestInfo[] testInfoArray) {
        super(activity, R.layout.row_calibrate, testInfoArray);
        mActivity = activity;
        mTestInfoArray = testInfoArray;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        LayoutInflater inflater = mActivity.getLayoutInflater();

        @SuppressLint("ViewHolder")
        TestInfo testInfo = mTestInfoArray[position];
        View rowView;

        rowView = inflater.inflate(R.layout.row_type, parent, false);

        if (testInfo.isGroup()) {
            rowView.findViewById(R.id.textGroup).setVisibility(View.VISIBLE);
            ((TextView) rowView.findViewById(R.id.textGroup)).setText(mActivity.getString(testInfo.getGroupName()));
            rowView.findViewById(R.id.typeLayout).setVisibility(View.GONE);
        } else {
            rowView.findViewById(R.id.textGroup).setVisibility(View.GONE);
            ((TextView) rowView.findViewById(R.id.textName)).setText(
                    testInfo.getName(mActivity.getResources().getConfiguration().locale.getLanguage()));
        }


        return rowView;
    }
}
