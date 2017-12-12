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

package org.akvo.caddisfly.sensor.striptest.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.GroupType;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.utils.ColorUtils;

import java.util.List;

import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueString;

public class SwatchView extends View {

    private final static int GUTTER = 5;
    private final static int VAL_BAR_HEIGHT = 20;
    private final static int TEXT_SIZE = 20;
    private final static int MARGIN = 10;

    int blockWidth = 0;
    int lineHeight = 0;
    Paint paintColor;
    float[] lab = new float[3];
    private TestInfo testInfo;
    private Paint blackText;


    public SwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackText = new Paint();
        blackText.setColor(android.graphics.Color.BLACK);
        blackText.setTextSize(TEXT_SIZE);
        blackText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        blackText.setTextAlign(Paint.Align.CENTER);

        paintColor = new Paint();
        paintColor.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (testInfo != null) {
            for (int resultIndex = 0; resultIndex < testInfo.Results().size(); resultIndex++) {

                List<ColorItem> colors = testInfo.Results().get(resultIndex).getColors();
                if (colors.size() > 0) {
                    int colorCount = colors.size();

                    int[][] rgbCols = new int[colorCount][3];
                    float[] values = new float[colorCount];

                    // get lab colours and turn them to RGB
                    for (int i = 0; i < colorCount; i++) {

                        values[i] = colors.get(i).getValue().floatValue();

                        List<Double> patchColorValues = colors.get(i).getLab();
                        if (patchColorValues != null) {
                            lab[0] = patchColorValues.get(0).floatValue();
                            lab[1] = patchColorValues.get(1).floatValue();
                            lab[2] = patchColorValues.get(2).floatValue();

                            rgbCols[i] = ColorUtils.XYZtoRGBint(ColorUtils.Lab2XYZ(lab));
                            colors.get(i).setRgb(Color.rgb(rgbCols[i][0], rgbCols[i][1], rgbCols[i][2]));
                        }
                    }

                    int totWidth = GUTTER + blockWidth;
                    for (int i = 0; i < colorCount; i++) {

                        paintColor.setColor(colors.get(i).getRgb());

                        canvas.drawRect(MARGIN + (i * totWidth), MARGIN + (resultIndex * lineHeight),
                                i * totWidth + blockWidth, (resultIndex * lineHeight) + blockWidth, paintColor);

                        if (testInfo.getGroupingType() == GroupType.INDIVIDUAL || resultIndex == testInfo.Results().size() - 1) {
                            canvas.drawText(createValueString(values[i]), MARGIN + (i * totWidth + blockWidth / 2),
                                    MARGIN + (resultIndex * lineHeight) + blockWidth + VAL_BAR_HEIGHT, blackText);
                        }
                    }
                }
            }
        }
    }

    public void setPatch(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int lineCount = 0;
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));


        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {

            int width = getMeasuredWidth() - (MARGIN * 2);

            if (testInfo != null) {
                for (int resultIndex = 0; resultIndex < testInfo.Results().size(); resultIndex++) {
                    List<ColorItem> colors = testInfo.Results().get(resultIndex).getColors();
                    if (colors.size() > 0) {
                        int colorCount = colors.size();

                        if (blockWidth == 0) {
                            blockWidth = Math.round((width - (colorCount - 2) * GUTTER) / colorCount);
                            if (testInfo.getGroupingType() == GroupType.GROUP) {
                                lineHeight = blockWidth + (VAL_BAR_HEIGHT / 2);
                            } else {
                                lineHeight = blockWidth + VAL_BAR_HEIGHT + VAL_BAR_HEIGHT;
                            }
                        }
                    }
                }
            }
        }

        if (testInfo != null) {
            lineCount = testInfo.Results().size();
        }
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec((lineCount * lineHeight) +
                        VAL_BAR_HEIGHT, MeasureSpec.EXACTLY));
    }
}