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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

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

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.util.ColorUtil;

public class DiagnosticDetailsFragment extends DialogFragment {

    private Bitmap mExtractBitmap;
    private Bitmap mPhotoBitmap;
    private String mDimension;
    private Fragment mParentFragment;

    public static DiagnosticDetailsFragment newInstance(Bitmap extractBitmap, Bitmap photoBitmap,
                                                       String dimension, Fragment parentFragment) {
        DiagnosticDetailsFragment fragment = new DiagnosticDetailsFragment();
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

        final View view = inflater.inflate(R.layout.dialog_diagnostic_details, container, false);

        ImageView imageExtract = (ImageView) view.findViewById(R.id.imageExtract);
        ImageView imagePhoto = (ImageView) view.findViewById(R.id.imagePhoto);

        TextView textResult = (TextView) view.findViewById(R.id.textResult);
        Button buttonColorExtract = (Button) view.findViewById(R.id.buttonColorExtract);
        Button buttonSwatchColor = (Button) view.findViewById(R.id.buttonSwatchColor);

        TextView textColorRgb = (TextView) view.findViewById(R.id.textExtractedRgb);
        TextView swatchRgbValue = (TextView) view.findViewById(R.id.textSwatchRgb);
        TextView textDimension = (TextView) view.findViewById(R.id.textDimension);
        TextView textDistance = (TextView) view.findViewById(R.id.textDistance);
        TextView textQuality = (TextView) view.findViewById(R.id.textQuality);

        imageExtract.setImageBitmap(mExtractBitmap);
        imagePhoto.setImageBitmap(mPhotoBitmap);
        textDimension.setText(mDimension);

        CaddisflyApp caddisflyApp = CaddisflyApp.getApp();

        if (caddisflyApp.currentTestInfo.getCode().isEmpty() ||
                caddisflyApp.currentTestInfo.getType() != CaddisflyApp.TestType.COLORIMETRIC_LIQUID) {
            caddisflyApp.setDefaultTest();
        }

        ColorInfo photoColor = ColorUtil.getColorFromBitmap(mExtractBitmap,
                AppConfig.SAMPLE_CROP_LENGTH_DEFAULT);

        ResultDetail resultDetail = SwatchHelper.analyzeColor(photoColor,
                caddisflyApp.currentTestInfo.getSwatches(),
                ColorUtil.ColorModel.RGB);

        double result = resultDetail.getResult();
        int color = resultDetail.getColor();
        int swatchColor = resultDetail.getMatchedColor();

        textQuality.setText(String.format("Q: %.0f%%", photoColor.getQuality()));

        if (result > -1) {
            textResult.setText(String.format("%s : %.2f %s", caddisflyApp.currentTestInfo.getName("en"),
                    result, caddisflyApp.currentTestInfo.getUnit()));
        } else {
            textResult.setText(String.format("%s", caddisflyApp.currentTestInfo.getName("en")));
        }

        textDistance.setText(String.format("D: %.2f", resultDetail.getDistance()));
        buttonSwatchColor.setBackgroundColor(resultDetail.getMatchedColor());
        swatchRgbValue.setText(String.format("r: %s", ColorUtil.getColorRgbString(swatchColor)));

        buttonColorExtract.setBackgroundColor(color);

        textColorRgb.setText(String.format("r: %s", ColorUtil.getColorRgbString(color)));

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
//        ResultDialogListener listener = (ResultDialogListener) mParentFragment;
//        listener.onSuccessFinishDialog();
    }

    @Override
    public void onDestroy() {
        mExtractBitmap.recycle();
        mPhotoBitmap.recycle();
        super.onDestroy();
    }

    public interface ResultDialogListener {
        void onSuccessFinishDialog();
    }
}
