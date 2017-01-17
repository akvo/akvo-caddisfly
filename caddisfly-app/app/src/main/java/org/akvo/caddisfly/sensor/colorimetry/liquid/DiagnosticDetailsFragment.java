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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.util.ColorUtil;

import java.util.Locale;

public class DiagnosticDetailsFragment extends DialogFragment {

    private static final double WIDTH_PERCENTAGE = 0.98;
    private static final double HEIGHT_PERCENTAGE = 0.9;
    private Bitmap mExtractBitmap;
    private Bitmap mPhotoBitmap;
    private String mDimension;

    public static DiagnosticDetailsFragment newInstance(Bitmap extractBitmap, Bitmap photoBitmap,
                                                        String dimension) {
        DiagnosticDetailsFragment fragment = new DiagnosticDetailsFragment();
        fragment.mExtractBitmap = extractBitmap;
        fragment.mPhotoBitmap = photoBitmap;
        fragment.mDimension = dimension;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.result);
        TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        final View view = inflater.inflate(R.layout.dialog_diagnostic_details, container, false);

        ImageView imageExtract = (ImageView) view.findViewById(R.id.imageExtract);
        ImageView imagePhoto = (ImageView) view.findViewById(R.id.imagePhoto);

        TextView textResult = (TextView) view.findViewById(R.id.textResult);
        Button buttonColorExtract = (Button) view.findViewById(R.id.buttonColorExtract);
        Button buttonSwatchColor = (Button) view.findViewById(R.id.buttonSwatchColor);

        TextView textExtractedRgb = (TextView) view.findViewById(R.id.textExtractedRgb);
        TextView textSwatchRgb = (TextView) view.findViewById(R.id.textSwatchRgb);
        TextView textDimension = (TextView) view.findViewById(R.id.textDimension);
        TextView textDistance = (TextView) view.findViewById(R.id.textDistance);
        TextView textQuality = (TextView) view.findViewById(R.id.textQuality);

        imageExtract.setImageBitmap(mExtractBitmap);
        imagePhoto.setImageBitmap(mPhotoBitmap);
        textDimension.setText(mDimension);

        if (currentTestInfo == null || currentTestInfo.getId().isEmpty()
                || currentTestInfo.getType() != TestType.COLORIMETRIC_LIQUID) {
            CaddisflyApp.getApp().setDefaultTest();
        }

        if (mExtractBitmap != null) {
            ColorInfo photoColor = ColorUtil.getColorFromBitmap(mExtractBitmap,
                    ColorimetryLiquidConfig.SAMPLE_CROP_LENGTH_DEFAULT);


            if (currentTestInfo != null) {
                ResultDetail resultDetail = SwatchHelper.analyzeColor(currentTestInfo.getSwatches().size(),
                        photoColor, currentTestInfo.getSwatches(), ColorUtil.ColorModel.RGB);

                double result = resultDetail.getResult();
                int color = resultDetail.getColor();
                int swatchColor = resultDetail.getMatchedColor();
                textDistance.setText(String.format(Locale.getDefault(), "D: %.2f", resultDetail.getDistance()));
                buttonSwatchColor.setBackgroundColor(resultDetail.getMatchedColor());
                textSwatchRgb.setText(String.format("r: %s", ColorUtil.getColorRgbString(swatchColor)));

                textQuality.setText(String.format(Locale.getDefault(), "Q: %.0f%%", photoColor.getQuality()));

                if (result > -1) {
                    textResult.setText(String.format(Locale.getDefault(), "%s : %.2f %s", currentTestInfo.getName(),
                            result, currentTestInfo.getUnit()));
                } else {
                    textResult.setText(String.format("%s", currentTestInfo.getName()));
                }

                buttonColorExtract.setBackgroundColor(color);

                textExtractedRgb.setText(String.format("r: %s", ColorUtil.getColorRgbString(color)));
            }
        }

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        int dialogWidth = (int) (WIDTH_PERCENTAGE * getResources().getDisplayMetrics().widthPixels);
        int dialogHeight = (int) (HEIGHT_PERCENTAGE * getResources().getDisplayMetrics().heightPixels);

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        }

    }

    @Override
    public void onDestroy() {
        if (mExtractBitmap != null) {
            mExtractBitmap.recycle();
        }
        if (mPhotoBitmap != null) {
            mPhotoBitmap.recycle();
        }
        super.onDestroy();
    }

}
