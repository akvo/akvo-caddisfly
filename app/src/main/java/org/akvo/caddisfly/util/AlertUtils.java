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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.akvo.caddisfly.R;

public class AlertUtils {

    protected AlertUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    public static void showMessage(Context context, int title, int message) {
        showAlert(context, title, message, null, null);
    }

    public static void askQuestion(Context context, int title, int message,
                                   DialogInterface.OnClickListener callback,
                                   DialogInterface.OnClickListener cancelListener) {
        showAlert(context, title, message, callback, cancelListener);
    }

/*    public static void askQuestion(Context context, int title, String message,
                                   DialogInterface.OnClickListener callback,
                                   DialogInterface.OnClickListener cancelListener) {
        showAlert(context, title, message, R.string.ok, callback, cancelListener);
    }*/

    public static void showAlert(Context context, int title, int message,
                                 DialogInterface.OnClickListener callback,
                                 DialogInterface.OnClickListener cancelListener) {

        showAlert(context, title, context.getString(message), R.string.ok, callback, cancelListener);
    }

    private static void showAlert(final Context context, int title, String message, int okButtonText,
                                  DialogInterface.OnClickListener callback,
                                  DialogInterface.OnClickListener cancelListener) {
        //if ( title == null ) title = context.getResources().getString(R.string.app_name);

/*
        int iconId;
        assert context.getApplicationContext() != null;
        if (((MainApp) context.getApplicationContext()).CurrentTheme
                == R.style.AppTheme_Dark) {
            iconId = R.drawable.ic_action_warning_dark;
        } else {
            iconId = R.drawable.ic_action_warning;
        }
*/

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                        //.setIcon(iconId)
                .setCancelable(false);

        if (callback != null) {
            builder.setPositiveButton(okButtonText, callback);
        }

        if (cancelListener == null) {
            int buttonText = R.string.cancel;
            if (callback == null) {
                buttonText = okButtonText;
            }
            builder.setNegativeButton(buttonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        } else {
            builder.setNegativeButton(R.string.cancel, cancelListener);
        }

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);

                //float textSize = context.getResources().getDimension(R.dimen.textSize);
                // btnPositive.setTextSize(textSize);

                //Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
                //btnNegative.setTextSize(textSize);
            }
        });

        alert.show();

    }

    /**
     * Generic error dialog with a close button.
     */
   /* public static void showDialog(String title, String message, Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }*/
    @SuppressLint("InflateParams")
    public static void showError(Context context, int title, String message, Bitmap bitmap, int okButtonText,
                                 DialogInterface.OnClickListener callback,
                                 DialogInterface.OnClickListener cancelListener) {

        if (bitmap == null) {
            showAlert(context, title, message, okButtonText, callback, cancelListener);
            return;
        }

        AlertDialog myDialog;
        View alertView;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);

        alertView = inflater.inflate(R.layout.dialog_error, null, false);
        builder.setView(alertView);

        builder.setTitle(R.string.error);

        builder.setMessage(message);

        ImageView image = (ImageView) alertView.findViewById(R.id.image);
        //image.setImageResource(R.drawable.ic_launcher);
        image.setImageBitmap(bitmap);

        if (callback != null) {
            builder.setPositiveButton(okButtonText, callback);
        }

        if (cancelListener == null) {
            int buttonText = R.string.cancel;
            if (callback == null) {
                buttonText = okButtonText;
            }
            builder.setNegativeButton(buttonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        } else {
            builder.setNegativeButton(R.string.cancel, cancelListener);
        }

        builder.setCancelable(false);
        myDialog = builder.create();

        myDialog.show();

    }

}
