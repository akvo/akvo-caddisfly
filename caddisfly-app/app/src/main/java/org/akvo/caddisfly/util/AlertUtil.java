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

package org.akvo.caddisfly.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.akvo.caddisfly.R;

/**
 * Utility functions to show alert messages.
 */
@SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
public final class AlertUtil {

    private AlertUtil() {
    }

    /**
     * Displays an alert dialog.
     *
     * @param context   the context
     * @param title     the title
     * @param message   the message
     */
    public static void showMessage(@NonNull Context context, @StringRes int title, @StringRes int message) {
        showAlert(context, title, message, null, null, null);
    }

    /**
     * Displays an alert dialog.
     *
     * @param context   the context
     * @param title     the title
     * @param message   the message
     */
    public static void showMessage(@NonNull Context context, @StringRes int title, String message) {
        showAlert(context, title, message, null, null, null);
    }

    public static void askQuestion(@NonNull Context context, @StringRes int title, @StringRes int message,
                                   @StringRes int okButtonText, @StringRes int cancelButtonText,
                                   boolean isDestructive,
                                   DialogInterface.OnClickListener positiveListener,
                                   @Nullable DialogInterface.OnClickListener cancelListener) {

        if (cancelListener == null) {
            cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(@NonNull DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            };
        }

        showAlert(context, context.getString(title), context.getString(message), okButtonText,
                cancelButtonText, true, isDestructive, positiveListener, cancelListener, null);
    }

    public static AlertDialog showAlert(@NonNull Context context, @StringRes int title, String message,
                                        @StringRes int okButtonText,
                                        DialogInterface.OnClickListener positiveListener,
                                        DialogInterface.OnClickListener negativeListener,
                                        DialogInterface.OnCancelListener cancelListener) {
        return showAlert(context, context.getString(title), message, okButtonText, R.string.cancel,
                true, false, positiveListener, negativeListener, cancelListener);
    }

    public static AlertDialog showAlert(@NonNull Context context, @StringRes int title, @StringRes int message,
                                                               @StringRes int okButtonText,
                                                               DialogInterface.OnClickListener positiveListener,
                                                               DialogInterface.OnClickListener negativeListener,
                                                               DialogInterface.OnCancelListener cancelListener) {
        return showAlert(context, context.getString(title), context.getString(message), okButtonText,
                R.string.cancel, true, false, positiveListener, negativeListener, cancelListener);
    }

    public static void showAlert(@NonNull Context context, @StringRes int title, @StringRes int message,
                                 DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener,
                                 DialogInterface.OnCancelListener cancelListener) {

        showAlert(context, context.getString(title), context.getString(message), R.string.ok, R.string.cancel,
                true, false, positiveListener, negativeListener, cancelListener);
    }

    private static void showAlert(@NonNull Context context, @StringRes int title, String message,
                                  DialogInterface.OnClickListener positiveListener,
                                  DialogInterface.OnClickListener negativeListener,
                                  DialogInterface.OnCancelListener cancelListener) {

        showAlert(context, context.getString(title), message, R.string.ok, R.string.cancel,
                true, false, positiveListener, negativeListener, cancelListener);
    }

    /**
     * Displays an alert dialog.
     *
     * @param context          the context
     * @param title            the title
     * @param message          the message
     * @param okButtonText     ok button text
     * @param positiveListener ok button listener
     * @param negativeListener cancel button listener
     * @return the alert dialog
     */
    private static AlertDialog showAlert(@NonNull final Context context, String title, String message,
                                         @StringRes int okButtonText, @StringRes int cancelButtonText,
                                         boolean cancelable, boolean isDestructive,
                                         @Nullable DialogInterface.OnClickListener positiveListener,
                                         @Nullable DialogInterface.OnClickListener negativeListener,
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
                public void onClick(@NonNull DialogInterface dialogInterface, int i) {
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
     * Displays an alert with error layout.
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
    @SuppressLint("InflateParams")
    public static AlertDialog showError(@NonNull Context context, @StringRes int title, String message, @Nullable Bitmap bitmap,
                                        @StringRes int okButtonText, @Nullable DialogInterface.OnClickListener positiveListener,
                                        @Nullable DialogInterface.OnClickListener negativeListener,
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
