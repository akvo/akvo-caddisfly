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

package org.akvo.caddisfly.sensor.colorimetry.strip.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;

/**
 * Created by linda on 9/21/15
 */
public class PageIndicatorView extends View {

    private static final float DISTANCE = 24;
    private static final int BULLET_RADIUS = 6;

    @NonNull
    private final Paint fillPaint;
    private int pageCount;
    private int activePage;

    public PageIndicatorView(@NonNull Context context) {
        this(context, null);
    }

    public PageIndicatorView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(ContextCompat.getColor(context, R.color.cyan));
        fillPaint.setAntiAlias(true);
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
    public void onDraw(@NonNull Canvas canvas) {

        if (pageCount > 1) {
            for (int i = 0; i < pageCount; i++) {
                if (activePage == i) {
                    canvas.drawCircle(DISTANCE * i + BULLET_RADIUS * 2, canvas.getHeight() / 2f,
                            BULLET_RADIUS * 2, fillPaint);
                } else {
                    canvas.drawCircle(DISTANCE * i + BULLET_RADIUS * 2, canvas.getHeight() / 2f,
                            BULLET_RADIUS, fillPaint);
                }
            }
        }
    }
}
