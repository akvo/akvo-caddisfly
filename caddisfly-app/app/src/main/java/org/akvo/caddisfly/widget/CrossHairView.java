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

package org.akvo.caddisfly.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

//http://stackoverflow.com/questions/20589267/draw-a-transparent-circle-onto-a-filled-android-canvas
public class CrossHairView extends View {

    Bitmap bitmap;
    Canvas canvas;
    Paint eraser;
    Paint circleStroke;
    int backgroundColor;

    public CrossHairView(Context context) {
        this(context, null);
    }

    public CrossHairView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CrossHairView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        circleStroke = new Paint();
        circleStroke.setColor(Color.YELLOW);
        circleStroke.setStyle(Paint.Style.STROKE);
        circleStroke.setStrokeWidth(5);

        eraser = new Paint();
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setAntiAlias(true);

        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            backgroundColor = a.data;
        } else {
            backgroundColor = Color.WHITE;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (w != oldw || h != oldh) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int w = getWidth();
        int h = getHeight();
        int radius = w > h ? h / 2 : w / 2;

        bitmap.eraseColor(Color.TRANSPARENT);
        canvas.drawColor(backgroundColor);
        canvas.drawCircle(w / 2, h / 2, radius, eraser);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawCircle(w / 2, h / 2, 40, circleStroke);
        super.onDraw(canvas);
    }

}
