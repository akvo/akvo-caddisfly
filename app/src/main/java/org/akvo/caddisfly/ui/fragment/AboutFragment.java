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

package org.akvo.caddisfly.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;

public class AboutFragment extends DialogFragment {

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    /*
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_about, container, false);


            getDialog().setTitle(R.string.about);

            if (view != null) {
                TextView productView = (TextView) view.findViewById(R.id.textVersion);
                productView.setText(MainApp.getVersion(getActivity()));
                ImageView organizationView = (ImageView) view.findViewById(R.id.organizationImage);

                productView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openWebBrowser(Config.PRODUCT_WEBSITE);
                    }
                });

                organizationView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openWebBrowser(Config.ORG_WEBSITE);
                    }
                });
            }
            if (((MainApp) getActivity().getApplicationContext()).CurrentTheme
                    == R.style.AppTheme_Dark) {
                view.findViewById(R.id.layoutAboutCompany).setAlpha(0.5f);
            }


            return view;
        }
    */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.about)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );


        LayoutInflater i = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams") View view = i.inflate(R.layout.fragment_about, null);

        if (view != null) {
            TextView productView = (TextView) view.findViewById(R.id.textVersion);
            productView.setText(MainApp.getVersion(getActivity()));
            ImageView organizationView = (ImageView) view.findViewById(R.id.organizationImage);

            productView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    openWebBrowser(Config.PRODUCT_WEBSITE);
                }
            });

            organizationView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    openWebBrowser(Config.ORG_WEBSITE);
                }
            });
        }

     /*   if (((MainApp) getActivity().getApplicationContext()).CurrentTheme
                == R.style.AppTheme_Dark) {
            view.findViewById(R.id.layoutAboutCompany).setAlpha(0.5f);
        }
*/
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getActivity() != null;
        getActivity().setTitle(R.string.about);
    }

    /**
     * Open a web Browser and navigate to given url
     *
     * @param url The url to navigate to
     */
    private void openWebBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
