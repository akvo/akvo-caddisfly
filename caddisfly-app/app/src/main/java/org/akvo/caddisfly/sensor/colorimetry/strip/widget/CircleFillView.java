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

package org.akvo.caddisfly.sensor.colorimetry.strip.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by linda on 9/4/15
 */
public class CircleFillView extends View {

    private final Paint paint;

    public CircleFillView(Context context) {
        this(context, null);
    }

    public CircleFillView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleFillView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    @Override
    public void onMeasure(int w, int h) {
        int smallest = w > h ? h : w;

        setMeasuredDimension(smallest, smallest);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getWidth() / 3, paint);
    }
}
