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
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;

/**
 * Created by linda on 9/21/15
 */
public class PageIndicatorView extends View {

    private static final float DISTANCE = 24;
    private static final int BULLET_RADIUS = 5;

    private final Paint fillPaint;
    private final Paint strokePaint;
    private int pageCount;
    private int activePage;

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(ContextCompat.getColor(context, R.color.cyan));
        fillPaint.setAntiAlias(true);

        strokePaint = new Paint(fillPaint);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);
    }

    public void setPageCount(int value) {
        pageCount = value;
        invalidate();
    }

    public void setActiveIndex(int value) {
        activePage = value;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthMeasureSpec = (int) Math.ceil(DISTANCE * pageCount);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {

        for (int i = 0; i < pageCount; i++) {
            if (activePage == i)
                canvas.drawCircle(DISTANCE * i + BULLET_RADIUS + 6, canvas.getHeight() / 2,
                        BULLET_RADIUS + 6, fillPaint);
            else
                canvas.drawCircle(DISTANCE * i + BULLET_RADIUS + 6, canvas.getHeight() / 2,
                        BULLET_RADIUS, fillPaint);
        }
    }
}
