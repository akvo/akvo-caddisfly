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
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Created by linda on 11/5/15
 */
public class PercentageMeterView extends View {

    private static final int NUMBER_OF_BARS = 10;
    @NonNull
    private final Paint paint;
    private float percentage = Float.NaN;

    // Red to green colour scale
    private final int[][] colours = {{230, 53, 46}, {234, 91, 47}, {240, 132, 45}, {232, 168, 52}, {247, 211, 43}, {212, 216, 57}, {169, 204, 57},
            {112, 186, 68}, {58, 171, 75}, {6, 155, 85}};

    public PercentageMeterView(@NonNull Context context) {
        this(context, null);
    }

    public PercentageMeterView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentageMeterView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {

        if (Float.isNaN(percentage)) {
            return;
        }
        canvas.save();

        float barWidth = getHeight() / 3.0f;
        float gutterWidth = 0.2f * barWidth;
        float distHor = 0.5f * (getWidth() - (barWidth + gutterWidth) * NUMBER_OF_BARS);

        canvas.translate(distHor, 0);
        for (int i = 0; i < NUMBER_OF_BARS; i++) {

            // Reset color to gray
            paint.setColor(Color.LTGRAY);

            if (percentage > 10 * i)
                paint.setARGB(255, colours[i][0], colours[i][1], colours[i][2]);
            canvas.drawRect(0f, 0f, 20, getHeight(), paint);

            // Position next bar
            canvas.translate(barWidth + gutterWidth, 0);
        }

        canvas.restore();

        super.onDraw(canvas);
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
        invalidate();
    }
}
