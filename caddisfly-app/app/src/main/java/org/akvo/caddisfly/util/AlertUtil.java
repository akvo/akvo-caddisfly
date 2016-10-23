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

package org.akvo.caddisfly.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.akvo.caddisfly.R;

/**
 * Utility functions to show alert messages
 */
public final class AlertUtil {

    private AlertUtil() {
    }

    public static void showMessage(Context context, @StringRes int title, @StringRes int message) {
        showAlert(context, title, message, null, null, null);
    }

    @SuppressWarnings("SameParameterValue")
    public static void showMessage(Context context, @StringRes int title, String message) {
        showAlert(context, title, message, null, null, null);
    }

    @SuppressWarnings("SameParameterValue")
    public static void askQuestion(Context context, @StringRes int title, @StringRes int message,
                                   @StringRes int okButtonText, @StringRes int cancelButtonText,
                                   boolean isDestructive,
                                   DialogInterface.OnClickListener positiveListener,
                                   DialogInterface.OnCancelListener cancelListener) {
        showAlert(context, context.getString(title), context.getString(message), okButtonText,
                cancelButtonText, true, isDestructive, positiveListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }, cancelListener);
    }

    @SuppressWarnings("SameParameterValue")
    public static void showAlert(Context context, @StringRes int title, String message,
                                 @StringRes int okButtonText,
                                 DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener,
                                 DialogInterface.OnCancelListener cancelListener) {
        showAlert(context, context.getString(title), message, okButtonText, R.string.cancel,
                true, false, positiveListener, negativeListener, cancelListener);
    }

    @SuppressWarnings("SameParameterValue")
    public static void showAlert(Context context, @StringRes int title, @StringRes int message,
                                 @StringRes int okButtonText,
                                 DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener,
                                 DialogInterface.OnCancelListener cancelListener) {
        showAlert(context, context.getString(title), context.getString(message), okButtonText,
                R.string.cancel, true, false, positiveListener, negativeListener, cancelListener);
    }

    @SuppressWarnings("SameParameterValue")
    public static void showAlert(Context context, @StringRes int title, @StringRes int message,
                                 DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener,
                                 DialogInterface.OnCancelListener cancelListener) {

        showAlert(context, context.getString(title), context.getString(message), R.string.ok, R.string.cancel,
                true, false, positiveListener, negativeListener, cancelListener);
    }

    @SuppressWarnings("SameParameterValue")
    private static void showAlert(Context context, @StringRes int title, String message,
                                  DialogInterface.OnClickListener positiveListener,
                                  DialogInterface.OnClickListener negativeListener,
                                  DialogInterface.OnCancelListener cancelListener) {

        showAlert(context, context.getString(title), message, R.string.ok, R.string.cancel,
                true, false, positiveListener, negativeListener, cancelListener);
    }

    /**
     * Displays an alert dialog
     *
     * @param context          the context
     * @param title            the title
     * @param message          the message
     * @param okButtonText     ok button text
     * @param positiveListener ok button listener
     * @param negativeListener cancel button listener
     * @return the alert dialog
     */
    @SuppressWarnings("SameParameterValue")
    private static AlertDialog showAlert(final Context context, String title, String message,
                                         @StringRes int okButtonText, @StringRes int cancelButtonText,
                                         boolean cancelable, boolean isDestructive,
                                         DialogInterface.OnClickListener positiveListener,
                                         DialogInterface.OnClickListener negativeListener,
                                         DialogInterface.OnCancelListener cancelListener) {

        AlertDialog.Builder builder;
        if (isDestructive) {

            TypedArray a = context.obtainStyledAttributes(R.styleable.BaseActivity);
            int style = a.getResourceId(R.styleable.BaseActivity_dialogDestructiveButton, 0);
            a.recycle();

            builder = new AlertDialog.Builder(context, style);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable);

        if (positiveListener != null) {
            builder.setPositiveButton(okButtonText, positiveListener);
        } else if (negativeListener == null) {
            builder.setNegativeButton(okButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }

        if (negativeListener != null) {
            builder.setNegativeButton(cancelButtonText, negativeListener);
        }

        builder.setOnCancelListener(cancelListener);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        return alertDialog;
    }

    /**
     * Displays an alert with error layout
     *
     * @param context          the context
     * @param title            the title
     * @param message          the message
     * @param bitmap           a bitmap to show along with message
     * @param okButtonText     ok button text
     * @param positiveListener ok button listener
     * @param negativeListener cancel button listener
     * @return the alert dialog
     */
    @SuppressWarnings("SameParameterValue")
    @SuppressLint("InflateParams")
    public static AlertDialog showError(Context context, @StringRes int title, String message, Bitmap bitmap,
                                        @StringRes int okButtonText, DialogInterface.OnClickListener positiveListener,
                                        DialogInterface.OnClickListener negativeListener,
                                        DialogInterface.OnCancelListener cancelListener) {

        if (bitmap == null) {
            return showAlert(context, context.getString(title), message, okButtonText,
                    R.string.cancel, false, false, positiveListener, negativeListener, cancelListener);
        }

        final AlertDialog alertDialog;
        View alertView;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);

        alertView = inflater.inflate(R.layout.dialog_error, null, false);
        builder.setView(alertView);

        builder.setTitle(R.string.error);

        builder.setMessage(message);

        ImageView image = (ImageView) alertView.findViewById(R.id.imageSample);
        image.setImageBitmap(bitmap);

        if (positiveListener != null) {
            builder.setPositiveButton(okButtonText, positiveListener);
        }

        if (negativeListener == null) {
            builder.setNegativeButton(R.string.cancel, null);
        } else {
            int buttonText = R.string.cancel;
            if (positiveListener == null) {
                buttonText = okButtonText;
            }
            builder.setNegativeButton(buttonText, negativeListener);
        }

        builder.setCancelable(false);
        alertDialog = builder.create();

        if (context instanceof Activity) {
            alertDialog.setOwnerActivity((Activity) context);
        }
        alertDialog.show();
        return alertDialog;
    }

}
