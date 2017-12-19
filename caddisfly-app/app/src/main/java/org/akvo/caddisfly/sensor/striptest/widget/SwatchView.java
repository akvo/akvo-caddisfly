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

    private final static int VAL_BAR_HEIGHT = 15;
    private final static int TEXT_SIZE = 20;
    private final static float MARGIN = 10;
    private static float gutterSize = 5;
    float blockWidth = 0;
    float lineHeight = 0;
    Paint paintColor;
    float[] lab = new float[3];
    int lineCount = 0;
    int extraHeight = 0;
    float totalWidth = 0;
    int[][] rgbCols;
    private TestInfo testInfo;
    private Paint blackText;

    public SwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackText = new Paint();
        blackText.setColor(Color.BLACK);
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
            for (int resultIndex = 0; resultIndex < testInfo.getResults().size(); resultIndex++) {

                List<ColorItem> colors = testInfo.getResults().get(resultIndex).getColors();
                if (colors.size() > 0) {
                    int colorCount = colors.size();

                    for (int i = 0; i < colorCount; i++) {

                        ColorItem colorItem = colors.get(i);
                        if (colorItem != null) {
                            paintColor.setColor(colorItem.getRgb());

                            canvas.drawRect(MARGIN + (i * totalWidth), MARGIN + (resultIndex * lineHeight),
                                    i * totalWidth + blockWidth, (resultIndex * lineHeight) + blockWidth, paintColor);

                            if (testInfo.getGroupingType() == GroupType.INDIVIDUAL || resultIndex == testInfo.getResults().size() - 1) {
                                canvas.drawText(createValueString(colorItem.getValue().floatValue()),
                                        MARGIN + (i * totalWidth + blockWidth / 2),
                                        MARGIN + (resultIndex * lineHeight) + blockWidth + VAL_BAR_HEIGHT, blackText);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setPatch(TestInfo testInfo) {
        this.testInfo = testInfo;
        if (testInfo.getGroupingType() == GroupType.GROUP) {
            gutterSize = 1;
            extraHeight = 40;
        }

        lineCount = testInfo.getResults().size();

        for (int resultIndex = 0; resultIndex < testInfo.getResults().size(); resultIndex++) {
            List<ColorItem> colors = testInfo.getResults().get(resultIndex).getColors();
            if (colors.size() > 0) {
                int colorCount = colors.size();

                rgbCols = new int[colorCount][3];

                // get lab colours and turn them to RGB
                for (int i = 0; i < colorCount; i++) {

                    List<Double> patchColorValues = colors.get(i).getLab();
                    if (patchColorValues != null) {
                        lab[0] = patchColorValues.get(0).floatValue();
                        lab[1] = patchColorValues.get(1).floatValue();
                        lab[2] = patchColorValues.get(2).floatValue();

                        rgbCols[i] = ColorUtils.XYZtoRGBint(ColorUtils.Lab2XYZ(lab));
                        int color = Color.rgb(rgbCols[i][0], rgbCols[i][1], rgbCols[i][2]);

                        colors.get(i).setRgb(color);
                    }
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {

            float width = getMeasuredWidth() - (MARGIN * 2);

            if (testInfo != null) {
                for (int resultIndex = 0; resultIndex < testInfo.getResults().size(); resultIndex++) {
                    List<ColorItem> colors = testInfo.getResults().get(resultIndex).getColors();
                    if (colors.size() > 0) {
                        float colorCount = colors.size();

                        if (blockWidth == 0) {
                            blockWidth = (width - (colorCount - 4) * gutterSize) / colorCount;
                            if (testInfo.getGroupingType() == GroupType.GROUP) {
                                lineHeight = blockWidth + (VAL_BAR_HEIGHT / 3);
                            } else {
                                lineHeight = blockWidth + VAL_BAR_HEIGHT + VAL_BAR_HEIGHT;
                            }
                        }
                    }
                }
            }

            totalWidth = gutterSize + blockWidth;
        }

        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec((int) ((lineCount * lineHeight) + extraHeight), MeasureSpec.EXACTLY));
    }
}