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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

/**
 * Created by linda on 10/27/15
 */
public class LevelView extends View {
    private final Paint redPaint;
    private final Bitmap arrowBitmap;
    private float[] tilts;

    public LevelView(Context context) {
        this(context, null);
    }

    public LevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(3);

        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.level);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (tilts != null) {
            float degrees = getDegrees(tilts);
            if (degrees != 0) {
                canvas.save();
                canvas.rotate(degrees, canvas.getWidth() / 2f, canvas.getHeight() / 2f);
                canvas.drawBitmap(arrowBitmap, 0, 0, redPaint);
                canvas.restore();
            }
        }
        super.onDraw(canvas);
    }

    public void setAngles(float[] tiltValues) {
        this.tilts = tiltValues;
        invalidate();
    }

    private float getDegrees(float[] tiltValues) {
        float degrees = 0f;

        // if the horizontal tilt is too large, indicate it
        if (Math.abs(tiltValues[0] - 1) > Constant.MAX_TILT_DIFF) {
            degrees = tiltValues[0] - 1 < 0 ? -90 : 90;
        }

        // if the vertical tilt is too large, indicate it
        if (Math.abs(tiltValues[1] - 1) > Constant.MAX_TILT_DIFF) {
            degrees = tiltValues[1] - 1 < 0 ? 180 : 1;
        }
        return degrees;
    }
}
