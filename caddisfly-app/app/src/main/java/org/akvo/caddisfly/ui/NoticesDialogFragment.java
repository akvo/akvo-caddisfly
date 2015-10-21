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

package org.akvo.caddisfly.ui;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

import org.akvo.caddisfly.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoticesDialogFragment extends DialogFragment {


    public NoticesDialogFragment() {
        // Required empty public constructor
    }

    public static NoticesDialogFragment newInstance() {
        return new NoticesDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0); // remove title from dialogfragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_notices_dialog, container, false);

        WebView webView = (WebView) view.findViewById(R.id.webNotices);
        webView.loadUrl("file:///android_asset/open_source_licenses.html");

        return view;
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//
//        dialog.setTitle("Open Source Licenses");
//        TextView titleTextView = (TextView) dialog.findViewById(android.R.id.title);
//        titleTextView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
//        return dialog;
//    }

    @Override
    public void onResume() {
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(params);

        super.onResume();
    }
}