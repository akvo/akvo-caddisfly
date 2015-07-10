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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.util.ColorUtils;

public class VerboseResultFragment extends DialogFragment {

    private Bitmap mExtractBitmap;
    private Bitmap mPhotoBitmap;
    private String mDimension;
    private Fragment mParentFragment;

    public static VerboseResultFragment newInstance(Bitmap extractBitmap, Bitmap photoBitmap,
                                                    String dimension, Fragment parentFragment) {
        VerboseResultFragment fragment = new VerboseResultFragment();
        fragment.mExtractBitmap = extractBitmap;
        fragment.mPhotoBitmap = photoBitmap;
        fragment.mDimension = dimension;
        fragment.mParentFragment = parentFragment;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.result);

        final View view = inflater.inflate(R.layout.dialog_verbose_error, container, false);

        ImageView imageExtract = (ImageView) view.findViewById(R.id.imageExtract);
        ImageView imagePhoto = (ImageView) view.findViewById(R.id.imagePhoto);

        TextView textResult = (TextView) view.findViewById(R.id.textResult);
        Button buttonColorExtract = (Button) view.findViewById(R.id.buttonColorExtract);
        Button buttonSwatchColor = (Button) view.findViewById(R.id.buttonSwatchColor);

        TextView textColorRgb = (TextView) view.findViewById(R.id.rgbValue);
        TextView swatchRgbValue = (TextView) view.findViewById(R.id.swatchRgbValue);
        TextView textDimension = (TextView) view.findViewById(R.id.textDimension);
        TextView textDistance = (TextView) view.findViewById(R.id.textDistance);

        imageExtract.setImageBitmap(mExtractBitmap);
        imagePhoto.setImageBitmap(mPhotoBitmap);
        textDimension.setText(mDimension);

        MainApp mainApp = (MainApp) getActivity().getApplicationContext();

        //todo: remove hard coding of fluor
        if (mainApp.currentTestInfo.getCode().isEmpty() || mainApp.currentTestInfo.getType() != 0) {
            mainApp.setSwatches("FLUOR");
        }

        Bundle resultValue = ColorUtils.getPpmValue(mExtractBitmap, mainApp.currentTestInfo, Config.SAMPLE_CROP_LENGTH_DEFAULT);
        double result = resultValue.getDouble(Config.RESULT_VALUE_KEY, -1);
        int color = resultValue.getInt(Config.RESULT_COLOR_KEY, 0);
        int swatchColor = resultValue.getInt("matchedColor", 0);

        if (result > -1) {
            textResult.setText(String.format("%s : %.2f %s", mainApp.currentTestInfo.getName("en"),
                    result, mainApp.currentTestInfo.getUnit()));
            textDistance.setText(String.format("distance: %.2f", resultValue.getDouble("Distance")));
            buttonSwatchColor.setBackgroundColor(resultValue.getInt("MatchedColor", 0));
        }
        buttonColorExtract.setBackgroundColor(color);

        textColorRgb.setText(String.format("rgb: %s", ColorUtils.getColorRgbString(color)));
        swatchRgbValue.setText(String.format("rgb: %s", ColorUtils.getColorRgbString(swatchColor)));

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null)
            return;

        int dialogWidth = (int) (0.98 * getResources().getDisplayMetrics().widthPixels);
        int dialogHeight = (int) (0.9 * getResources().getDisplayMetrics().heightPixels);

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ResultDialogListener listener = (ResultDialogListener) mParentFragment;
        listener.onSuccessFinishDialog();
    }

    public interface ResultDialogListener {
        void onSuccessFinishDialog();
    }
}
